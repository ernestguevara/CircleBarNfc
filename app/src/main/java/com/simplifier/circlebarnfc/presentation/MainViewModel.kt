package com.simplifier.circlebarnfc.presentation

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.simplifier.circlebarnfc.domain.model.CustomerModel
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_BAL_INSUFFICIENT
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_CARD_INVALID
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_CLOSE_TAB
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_EMPTY
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_ERROR_READ
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_INCORRECT_ACCESS
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_MAX_BAL
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_NO_CARD
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_OPENING_TAB
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_TAB_OPEN_ERROR
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_TAP_CARD
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_THANK_YOU
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_WELCOME
import com.simplifier.circlebarnfc.presentation.utils.Constants.MAX_BALANCE
import com.simplifier.circlebarnfc.presentation.utils.ConversionHelper
import com.simplifier.circlebarnfc.presentation.utils.ConversionHelper.bytesToHexStringWithSpace
import com.simplifier.circlebarnfc.presentation.utils.ConversionHelper.hexByteArrayToDecimal
import com.simplifier.circlebarnfc.presentation.utils.MifareClassicHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _tag = MutableStateFlow<Tag?>(null)
    val tag: StateFlow<Tag?> = _tag.asStateFlow()

    private val _key = MutableStateFlow(Pair(ByteArray(0), ByteArray(0)))
    val key: StateFlow<Pair<ByteArray, ByteArray>> = _key.asStateFlow()

    //unused will not remove for future usage or separation of messages
    private val _authentication = MutableStateFlow(false)
    val authentication: StateFlow<Boolean> = _authentication.asStateFlow()

    private val _messageCode = MutableStateFlow(0)
    val messageCode: StateFlow<Int> = _messageCode.asStateFlow()

    private val _isAccess = MutableStateFlow(true)
    val isAccess: StateFlow<Boolean> = _isAccess.asStateFlow()

    private val _customerDetails = MutableStateFlow(CustomerModel())
    val customerDetails: StateFlow<CustomerModel> = _customerDetails.asStateFlow()

    private val _transactionStatus = MutableStateFlow(false)
    val transactionStatus: StateFlow<Boolean> = _transactionStatus.asStateFlow()

    private var customerModel = CustomerModel()
    private lateinit var mifareClassic: MifareClassic

    fun setIntent(intent: Intent) {
        if (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            if (_tag.value == null) {
                Log.i(TAG, "setIntent: no tag found will proceed")
                // NFC card is discovered
                // Handle your NFC operations here
                val intentTag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)

                //Check if MifareClassic Card
                if (intentTag?.techList?.contains(MifareClassic::class.java.name) == true) {
                    setTag(intentTag)
                } else {
                    viewModelScope.launch {
                        setMessageCode(CODE_CARD_INVALID)
                        delay(2000)
                        setMessageCode(CODE_TAP_CARD)
                    }
                    Log.i(TAG, "setIntent: not a mifare classic card")
                }
            } else {
                Log.i(TAG, "setIntent: tag found wont do anything")
            }
        }
    }

    private fun setTag(tag: Tag?) {
        _tag.value = tag
    }

    fun updateKey(keyA: ByteArray, keyB: ByteArray) {
        _key.value = Pair(keyA, keyB)
    }

    fun tagRemoved() {
        Log.i(TAG, "tagRemoved: called")
        _tag.value = null
        _key.value = Pair(byteArrayOf(0), byteArrayOf(0))
        _authentication.value = false
        _customerDetails.value = CustomerModel()
        customerModel = CustomerModel()
    }

    fun setAuthentication(auth: Boolean = true) {
        _authentication.value = auth
    }

    fun setMessageCode(code: Int = CODE_EMPTY) {
        _messageCode.value = code
    }

    fun setAccess(isAccess: Boolean) {
        _isAccess.value = isAccess
    }

    fun setUser() {
        getCustomerInfo()
        _customerDetails.value = customerModel
    }

    //end section setter

    //start mifare commands
    fun authenticate(tag: Tag) {
        tag.id?.let {
            val keyA = ConversionHelper.getStaffKeyA(it)
            val keyB = ConversionHelper.getStaffKeyB(it)

            updateKey(keyA, keyB)
        }

        Log.i(TAG, "authenticate: keyA: ${bytesToHexStringWithSpace(key.value.first)} " +
                    "keyB ${bytesToHexStringWithSpace(key.value.second)}")

        mifareClassic = MifareClassicHelper.getMifareInstance(tag)

        MifareClassicHelper.handleMifareClassic(this, mifareClassic) {
            getCustomerInfo()

            val allPropertiesFilled = checkCustomerFields(customerModel)

            Log.i(TAG,"authenticate: customerModel is all filled $allPropertiesFilled \n model is ${Gson().toJson(customerModel)}")

            if (checkCustomerFields(customerModel)) {
                setMessageCode()
                checkTransaction()
            } else {
                setMessageCode(CODE_ERROR_READ)
                setAuthentication(false)
            }
        }
    }

    private fun getCustomerInfo() {
        val keys = key.value

        customerModel.apply {

            //Authenticate with info sector
            if (mifareClassic.authenticateSectorWithKeyB(INFO_SECTOR, keys.second)) {
                val nameBlockIndex = mifareClassic.sectorToBlock(INFO_SECTOR) + NAME_BLOCK
                val name =
                    ConversionHelper.hexByteArrayToString(mifareClassic.readBlock(nameBlockIndex))

                val tierBlockIndex = mifareClassic.sectorToBlock(INFO_SECTOR) + TIER_BLOCK
                val tier = ConversionHelper.hexByteArrayToDecimal(
                    mifareClassic.readBlock(tierBlockIndex)
                )

                val addressBlockIndex = mifareClassic.sectorToBlock(INFO_SECTOR) + ADDRESS_BLOCK
                val address = ConversionHelper.hexByteArrayToString(
                    mifareClassic.readBlock(addressBlockIndex)
                )

                customerName = name
                customerAddress = address
                customerTier = tier

                Log.i(TAG, "getCustomerInfo: userInfo name: $name tier $tier address $address")
            }


            //authenticate with value sectors
            if (mifareClassic.authenticateSectorWithKeyB(VISIT_SECTOR, keys.second)) {
                val visitBlockIndex = mifareClassic.sectorToBlock(VISIT_SECTOR) + STORAGE_BLOCK
                val visit = ConversionHelper.hexByteArrayToDecimal(mifareClassic.readBlock(visitBlockIndex))
                customerVisitCount = visit
            }
            if (mifareClassic.authenticateSectorWithKeyB(BALANCE_SECTOR, keys.second)) {
                val balancerBlockIndex = mifareClassic.sectorToBlock(BALANCE_SECTOR) + STORAGE_BLOCK
                val balance = ConversionHelper.hexByteArrayToDecimal(mifareClassic.readBlock(balancerBlockIndex))
                customerBalance = ConversionHelper.decimalToDoubleWithCents(balance)
            }
            if (mifareClassic.authenticateSectorWithKeyB(FLAG_SECTOR, keys.second)) {
                val passFlagBlockIndex = mifareClassic.sectorToBlock(FLAG_SECTOR) + STORAGE_BLOCK
                val passFlag = ConversionHelper.hexByteArrayToDecimal(mifareClassic.readBlock(passFlagBlockIndex))
                customerFlag = passFlag
            }
            Log.i(TAG, "getCustomerInfo: transaction visitCount: $customerVisitCount balance $customerBalance passFlag $customerFlag")
        }

    }

    private fun checkTransaction() {
        when (customerModel.customerFlag) {
            FLAG_LOGGED_OUT -> {
                Log.i(TAG, "checkTransaction: user is logged out")
                if (isAccess.value) {
                    Log.i(TAG, "checkTransaction: login the user")
                    if (changeValueBlock(
                            BALANCE_SECTOR,
                            30000,
                            false
                        )
                    ) {
                        changeFlag(FLAG_LOGGED_IN)
                        changeValueBlock( VISIT_SECTOR, 1)
                        setUser()
                        setMessageCode(CODE_WELCOME)
                    }
                } else {
                    Log.i(TAG, "Incorrect access please login at the terminal")
                    setMessageCode(CODE_INCORRECT_ACCESS)
                }
            }

            FLAG_LOGGED_IN -> {
                Log.i(TAG, "checkTransaction: user is logged in")
                if (isAccess.value) {
                    Log.i(TAG, "checkTransaction: will logout")
                    changeFlag(FLAG_LOGGED_OUT)
                    setMessageCode(CODE_THANK_YOU)
                } else {
                    Log.i(TAG, "checkTransaction: will open the user tab")
                    changeFlag(FLAG_OPEN_TAB)
                    setMessageCode(CODE_OPENING_TAB)
                }
            }

            FLAG_OPEN_TAB -> {
                Log.i(TAG, "checkTransaction: user tab is opened")
                if (isAccess.value) {
                    setMessageCode(CODE_TAB_OPEN_ERROR)
                    Log.i(TAG, "checkTransaction: Please settle the bill for the user's open tab")
                } else {
                    Log.i(TAG, "checkTransaction: open tab will change to login")
                    if (changeValueBlock(
                            BALANCE_SECTOR,
                            50000,
                            false
                        )
                    ) {
                        changeFlag(FLAG_LOGGED_IN)
                        setUser()
                        setMessageCode(CODE_CLOSE_TAB)
                    }
                }
            }

            else -> {
                Log.i(TAG, "checkTransaction: flag error")
                setAuthentication(false)
            }
        }
    }

    private fun changeFlag(flag: Int) {
        val blockIndex = STORAGE_BLOCK
        val backupIndex = BACKUP_BLOCK

        val blockAddress = mifareClassic.sectorToBlock(FLAG_SECTOR) + blockIndex
        val backupAddress = mifareClassic.sectorToBlock(FLAG_SECTOR) + backupIndex

        val valueBlockData = ByteArray(16)

        // First four bytes in little-endian format
        valueBlockData[0] = flag.toByte()
        valueBlockData[1] = (flag shr 8).toByte()
        valueBlockData[2] = (flag shr 16).toByte()
        valueBlockData[3] = (flag shr 24).toByte()

        // Second four bytes are the two's complement
        val twosComplement = flag.inv()
        valueBlockData[4] = twosComplement.toByte()
        valueBlockData[5] = (twosComplement shr 8).toByte()
        valueBlockData[6] = (twosComplement shr 16).toByte()
        valueBlockData[7] = (twosComplement shr 24).toByte()

        // Copy the first four bytes again for the third four bytes
        System.arraycopy(valueBlockData, 0, valueBlockData, 8, 4)

        // Last four bytes: block index, two's complement, block index, two's complement
        valueBlockData[12] = blockAddress.toByte()
        valueBlockData[13] = blockAddress.inv().toByte()
        valueBlockData[14] = blockAddress.toByte()
        valueBlockData[15] = blockAddress.inv().toByte()

        Log.i(TAG, "setValueBlock: flag ${bytesToHexStringWithSpace(valueBlockData)}")

        //authenticate again
        if (mifareClassic.authenticateSectorWithKeyB(FLAG_SECTOR, key.value.second)) {
            // Write the value block data to the block
            mifareClassic.writeBlock(blockAddress, valueBlockData)

            //copy to memory
            mifareClassic.restore(blockAddress)

            //write to backup index
            mifareClassic.transfer(backupAddress)

            Log.i(TAG, "changeFlag value is ${hexByteArrayToDecimal(mifareClassic.readBlock(blockAddress))}" +
                    "\n backup value is ${hexByteArrayToDecimal(mifareClassic.readBlock(backupAddress))}")
        }
    }

    private fun changeValueBlock(
        sectorIndex: Int,
        value: Int,
        increment: Boolean = true
    ): Boolean {
        val blockAddress = mifareClassic.sectorToBlock(sectorIndex) + STORAGE_BLOCK
        val backupAddress = mifareClassic.sectorToBlock(sectorIndex) + BACKUP_BLOCK

        Log.i(TAG, "changeValueBlock: blockAddress $blockAddress \n backupAddress $backupAddress")


        //authenticate again
        if (mifareClassic.authenticateSectorWithKeyB(sectorIndex, key.value.second)) {

            // Read the block data
            val valueBlockVal = hexByteArrayToDecimal(mifareClassic.readBlock(blockAddress))

            Log.i(TAG, "readDataBlock success data is ${bytesToHexStringWithSpace(mifareClassic.readBlock(blockAddress))} "
                    + "\n decimal value is ${hexByteArrayToDecimal(mifareClassic.readBlock(blockAddress))}")

            if (increment && valueBlockVal + value > MAX_BALANCE) {
                //higher than threshold
                Log.i(TAG, "changeValueBlock: Maximum Balance Reached")
                setMessageCode(CODE_MAX_BAL)
                return false
            } else if ((!increment && valueBlockVal - value < 0)) {
                //lower than threshold
                Log.i(TAG, "changeValueBlock: Not Enough Balance")
                setMessageCode(CODE_BAL_INSUFFICIENT)
                return false
            } else {
                if (increment) {
                    mifareClassic.increment(blockAddress, value)
                } else {
                    mifareClassic.decrement(blockAddress, value)
                }
                mifareClassic.transfer(blockAddress)

                //copy
                mifareClassic.restore(blockAddress)

                //restore to backup
                mifareClassic.transfer(backupAddress)

                Log.i(TAG, "changeValueBlock: increment: $increment success " +
                        "\n old value $valueBlockVal" +
                        "\n new value ${hexByteArrayToDecimal(mifareClassic.readBlock(blockAddress))}" +
                        "\n backup value ${hexByteArrayToDecimal(mifareClassic.readBlock(backupAddress))}")
                return true
            }
        }
        return false
    }

    //logic
    fun checkCustomerFields(customerModel: CustomerModel) = listOfNotNull(
        customerModel.customerName,
        customerModel.customerTier,
        customerModel.customerAddress,
        customerModel.customerVisitCount,
        customerModel.customerBalance,
        customerModel.customerFlag
    ).size == 6

    //get values
    fun getCustomerBalance(): Double? {
        return customerModel.customerBalance
    }

    //mifare functions
    fun setMifareTransactionStatus(isComplete: Boolean = false) {
        _transactionStatus.value = isComplete
    }

    fun reload(value: Int) {
        if(::mifareClassic.isInitialized) {
            MifareClassicHelper.handleMifareClassic(this, mifareClassic) {
                changeValueBlock(BALANCE_SECTOR, value, true)
            }
        } else {
            setMessageCode(CODE_NO_CARD)
        }
    }

    companion object {
        const val TAG = "ernesthor24 MainViewModel"
        const val INFO_SECTOR = 6
        const val NAME_BLOCK = 0
        const val TIER_BLOCK = 1
        const val ADDRESS_BLOCK = 2


        const val VISIT_SECTOR = 7
        const val BALANCE_SECTOR = 8
        const val FLAG_SECTOR = 9

        const val STORAGE_BLOCK = 0
        const val BACKUP_BLOCK = 1

        const val FLAG_LOGGED_OUT = 0
        const val FLAG_LOGGED_IN = 1
        const val FLAG_OPEN_TAB = 2
    }
}