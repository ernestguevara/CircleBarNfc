package com.simplifier.circlebarnfc.presentation

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.simplifier.circlebarnfc.byteArrayToDecimal
import com.simplifier.circlebarnfc.domain.model.CustomerModel
import com.simplifier.circlebarnfc.presentation.utils.ConversionHelper
import com.simplifier.circlebarnfc.presentation.utils.ConversionHelper.bytesToHexStringWithSpace
import com.simplifier.circlebarnfc.presentation.utils.MifareClassicHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {

    private val _tag = MutableStateFlow<Tag?>(null)
    val tag: StateFlow<Tag?> = _tag.asStateFlow()

    private val _key = MutableStateFlow(Pair(ByteArray(0), ByteArray(0)))
    val key: StateFlow<Pair<ByteArray, ByteArray>> = _key.asStateFlow()

    private val _authentication = MutableStateFlow(false)
    val authentication: StateFlow<Boolean> = _authentication.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _isAccess = MutableStateFlow(true)
    val isAccess: StateFlow<Boolean> = _isAccess.asStateFlow()

    fun setIntent(intent: Intent) {
        if (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            // NFC card is discovered
            // Handle your NFC operations here
            val intentTag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)

            //Check if MifareClassic Card
            if (intentTag?.techList?.contains(MifareClassic::class.java.name) == true) {
                setTag(intentTag)
            } else {
                Log.i(TAG, "setIntent: not a mifare classic card")
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
        _tag.value = null
        _key.value = Pair(byteArrayOf(0), byteArrayOf(0))
        _authentication.value = false
    }

    fun setAuthentication(auth: Boolean = true) {
        _authentication.value = auth
    }

    fun setMessage(message: String = "") {
        _message.value = message
    }

    fun setAccess(isAccess: Boolean) {
        _isAccess.value = isAccess
    }

    //end section setter

    fun authenticate(tag: Tag?) {
        tag?.id?.let {
            val keyA = ConversionHelper.getStaffKeyA(it)
            val keyB = ConversionHelper.getStaffKeyB(it)

            updateKey(keyA, keyB)
        }

        Log.i(
            TAG,
            "authenticate: keyA: ${bytesToHexStringWithSpace(key.value.first)} " +
                    "keyB ${bytesToHexStringWithSpace(key.value.second)}"
        )

        tag?.let {
            val mifareClassic = MifareClassicHelper.getMifareInstance(tag)

            val keys = key.value

            MifareClassicHelper.handleMifareClassic(this, mifareClassic) {
                val customerModel = CustomerModel()

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

                    customerModel.apply {
                        customerName = name
                        customerAddress = address
                        customerTier = tier
                    }

                    Log.i(TAG, "authenticate: userInfo name: $name tier $tier address $address")
                }

                if (mifareClassic.authenticateSectorWithKeyB(TRANSACTION_SECTOR, keys.second)) {
                    val visitBlockIndex =
                        mifareClassic.sectorToBlock(TRANSACTION_SECTOR) + VISIT_BLOCK
                    val visit = ConversionHelper.hexByteArrayToDecimal(
                        mifareClassic.readBlock(visitBlockIndex)
                    )

                    val balancerBlockIndex =
                        mifareClassic.sectorToBlock(TRANSACTION_SECTOR) + BALANCE_BLOCK
                    val balance = ConversionHelper.hexByteArrayToDecimal(
                        mifareClassic.readBlock(balancerBlockIndex)
                    )

                    val passFlagBlockIndex =
                        mifareClassic.sectorToBlock(TRANSACTION_SECTOR) + FLAG_BLOCK
                    val passFlag = ConversionHelper.hexByteArrayToDecimal(
                        mifareClassic.readBlock(passFlagBlockIndex)
                    )

                    customerModel.apply {
                        customerVisitCount = visit
                        customerBalance = ConversionHelper.decimalToDoubleWithCents(balance)
                        customerFlag = passFlag
                    }
                    Log.i(
                        TAG,
                        "authenticate: transaction visitCount: $visit balance $balance passFlag $passFlag"
                    )
                }

                val allPropertiesFilled = listOfNotNull(
                    customerModel.customerName,
                    customerModel.customerTier,
                    customerModel.customerAddress,
                    customerModel.customerVisitCount,
                    customerModel.customerBalance,
                    customerModel.customerFlag
                ).size == 6

                Log.i(
                    TAG,
                    "authenticate: customerModel is all filled $allPropertiesFilled \n model is ${
                        Gson().toJson(customerModel)
                    }"
                )

                if (allPropertiesFilled) {
//                    setAuthentication(true)
                    setMessage()
                    checkTransaction(this, mifareClassic, customerModel)
                } else {
                    setAuthentication(false)
                }
            }
        }
    }

    fun checkTransaction(
        mainViewModel: MainViewModel,
        mifareClassic: MifareClassic,
        customerModel: CustomerModel
    ) {
        when (customerModel.customerFlag) {
            FLAG_LOGGED_OUT -> {
                Log.i(TAG, "checkTransaction: user is logged out")
                if (isAccess.value) {
                    Log.i(TAG, "checkTransaction: login the user")
                    if (changeValueBlock(
                            mifareClassic,
                            TRANSACTION_SECTOR,
                            BALANCE_BLOCK,
                            3000,
                            false
                        )
                    ) {
                        changeFlag(mifareClassic, FLAG_LOGGED_IN)
                        loginUser(customerModel)
                    }
                } else {
                    Log.i(TAG, "Incorrect access please login at the terminal")
                    setMessage("Incorrect access please login at the terminal")
                }
            }

            FLAG_LOGGED_IN -> {
                Log.i(TAG, "checkTransaction: user is logged in")
                if (isAccess.value) {
                    Log.i(TAG, "checkTransaction: will logout")
                    changeFlag(mifareClassic, FLAG_LOGGED_OUT)
                } else {
                    Log.i(TAG, "checkTransaction: will open the user tab")
                    changeFlag(mifareClassic, FLAG_OPEN_TAB)
                }
            }

            FLAG_OPEN_TAB -> {
                Log.i(TAG, "checkTransaction: user tab is opened")
                if (isAccess.value) {
                    setMessage("Please settle the bill for the user's open tab")
                    Log.i(TAG, "checkTransaction: Please settle the bill for the user's open tab")
                } else {
                    Log.i(TAG, "checkTransaction: open tab will change to login")
                    if (changeValueBlock(
                            mifareClassic,
                            TRANSACTION_SECTOR,
                            BALANCE_BLOCK,
                            5000,
                            false
                        )
                    ) {
                        changeFlag(mifareClassic, FLAG_LOGGED_IN)
                    }
                }
            }

            else -> {
                Log.i(TAG, "checkTransaction: flag error")
                mainViewModel.setAuthentication(false)
            }
        }
    }

    fun changeFlag(mifareClassic: MifareClassic, flag: Int) {

        val sectorIndex = TRANSACTION_SECTOR
        val blockIndex = FLAG_BLOCK

        val blockAddress = mifareClassic.sectorToBlock(sectorIndex) + blockIndex

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

        // Write the value block data to the block
        mifareClassic.writeBlock(blockAddress, valueBlockData)
    }

    private fun changeValueBlock(
        mifareClassic: MifareClassic,
        sectorIndex: Int,
        blockIndex: Int,
        value: Int,
        increment: Boolean = true
    ): Boolean {
        val blockAddress = mifareClassic.sectorToBlock(sectorIndex) + blockIndex
        Log.i(TAG, "changeValueBlock: blockAddress $blockAddress")

        //read value block first
        // Read the block data

        val valueBlockVal = byteArrayToDecimal(mifareClassic.readBlock(blockAddress))
        Log.i(
            TAG,
            "readDataBlock success data is ${
                bytesToHexStringWithSpace(
                    mifareClassic.readBlock(blockAddress)
                )
            } " +
                    "\n decimal value is ${byteArrayToDecimal(mifareClassic.readBlock(blockAddress))}"
        )

        if (increment && valueBlockVal + value > 65536) {
            //higher than threshold
            Log.i(TAG, "changeValueBlock: Maximum Balance Reached")
            setMessage("Maximum Balance Reached")
            return false
        } else if ((!increment && valueBlockVal - value < 0)) {
            //lower than threshold
            Log.i(TAG, "changeValueBlock: Not Enough Balance")
            setMessage("Not Enough Balance")
            return false
        } else {
            try {
                if (increment) {
                    mifareClassic.increment(blockAddress, value)
                } else {
                    mifareClassic.decrement(blockAddress, value)
                }
                mifareClassic.transfer(blockAddress)
                Log.i(
                    TAG, "changeValueBlock: increment: $increment success " +
                            "\n old balance $valueBlockVal" +
                            "\n new balance ${
                                byteArrayToDecimal(
                                    mifareClassic.readBlock(
                                        blockAddress
                                    )
                                )
                            }"
                )
                return true
            } catch (e: Exception) {
                // Increment operation failed, handle error
                Log.i(TAG, "changeValueBlock: increment: $increment failed")
                e.message?.let { setMessage(it) }
                return false
            }
        }
    }

    //ui functions
    fun loginUser(customerModel: CustomerModel) {
        
    }


    companion object {
        const val TAG = "ernesthor24 MainViewModel"
        const val INFO_SECTOR = 6
        const val NAME_BLOCK = 0
        const val TIER_BLOCK = 1
        const val ADDRESS_BLOCK = 2

        const val TRANSACTION_SECTOR = 7
        const val VISIT_BLOCK = 0
        const val BALANCE_BLOCK = 1
        const val FLAG_BLOCK = 2

        const val FLAG_LOGGED_OUT = 0
        const val FLAG_LOGGED_IN = 1
        const val FLAG_OPEN_TAB = 2
    }
}