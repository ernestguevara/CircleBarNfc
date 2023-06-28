package com.simplifier.circlebarnfc.presentation

import android.content.Context
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import com.simplifier.circlebarnfc.domain.model.CustomerModel
import com.simplifier.circlebarnfc.presentation.MainViewModel.Companion.ADDRESS_BLOCK
import com.simplifier.circlebarnfc.presentation.MainViewModel.Companion.BALANCE_BLOCK
import com.simplifier.circlebarnfc.presentation.MainViewModel.Companion.INFO_SECTOR
import com.simplifier.circlebarnfc.presentation.MainViewModel.Companion.NAME_BLOCK
import com.simplifier.circlebarnfc.presentation.MainViewModel.Companion.FLAG_BLOCK
import com.simplifier.circlebarnfc.presentation.MainViewModel.Companion.FLAG_LOGGED_IN
import com.simplifier.circlebarnfc.presentation.MainViewModel.Companion.FLAG_LOGGED_OUT
import com.simplifier.circlebarnfc.presentation.MainViewModel.Companion.FLAG_OPEN_TAB
import com.simplifier.circlebarnfc.presentation.MainViewModel.Companion.TIER_BLOCK
import com.simplifier.circlebarnfc.presentation.MainViewModel.Companion.TRANSACTION_SECTOR
import com.simplifier.circlebarnfc.presentation.MainViewModel.Companion.VISIT_BLOCK
import com.simplifier.circlebarnfc.presentation.utils.ConversionHelper
import com.simplifier.circlebarnfc.presentation.utils.ConversionHelper.bytesToHexStringWithSpace
import com.simplifier.circlebarnfc.presentation.utils.ConversionHelper.decimalToDoubleWithCents
import com.simplifier.circlebarnfc.presentation.utils.CoroutineHelper
import com.simplifier.circlebarnfc.presentation.utils.MifareClassicHelper
import com.simplifier.circlebarnfc.presentation.utils.ConversionHelper.hexByteArrayToDecimal
import com.simplifier.circlebarnfc.presentation.utils.ConversionHelper.hexByteArrayToString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

private const val TAG = "ernesthor24 MainScreen"

@Composable
fun MainScreen(mainViewModel: MainViewModel) {

    val tag: Tag? by mainViewModel.tag.collectAsState()

    val context = LocalContext.current

    val taskJob = remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(tag) {
        taskJob.value?.cancel()
        if (tag != null) {
            CoroutineHelper.runOnIOThread {
                cardPolling(mainViewModel, tag, context, taskJob, coroutineScope)
            }
            authenticate(mainViewModel, tag, context)
            Log.i(TAG, "MainScreen: launched effect called tag not null")
        } else {
            mainViewModel.tagRemoved()
            Log.i(TAG, "MainScreen: launched effect called tag null")
        }
    }
}

private fun cardPolling(
    mainViewModel: MainViewModel,
    tag: Tag?,
    context: Context,
    taskJob: MutableState<Job?>,
    coroutineScope: CoroutineScope
) {
    taskJob.value = coroutineScope.launch {
        if (tag != null) {
            val mifareClassic = MifareClassicHelper.getMifareInstance(tag)

            try {
                mifareClassic.connect()
                //do nothing as tag is present
                Log.i(TAG, "handleMifareClassic: tag connected")
            } catch (e: IOException) {
                Log.i(TAG, "handleMifareClassic: IO Exception")
                CoroutineHelper.runOnMainThread {
                    Toast.makeText(context, "Please check card connection", Toast.LENGTH_SHORT)
                        .show()
                }
                mainViewModel.tagRemoved()
            } catch (e: Exception) {
                CoroutineHelper.runOnMainThread {
                    Toast.makeText(context, "Error reading data: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            } finally {
                mifareClassic.close()
            }
        } else {
            //no tag
            Log.i(TAG, "handleMifareClassic: no tag")
            mainViewModel.tagRemoved()
        }

        delay(5000)
        cardPolling(mainViewModel, mainViewModel.tag.value, context, taskJob, coroutineScope)
    }
}

private fun authenticate(
    mainViewModel: MainViewModel,
    tag: Tag?,
    context: Context,
) {
    mainViewModel.tag.value?.id?.let {
        val keyA = ConversionHelper.getStaffKeyA(it)
        val keyB = ConversionHelper.getStaffKeyB(it)

        mainViewModel.updateKey(keyA, keyB)
    }


    Log.i(
        TAG,
        "authenticate: keyA: ${ConversionHelper.bytesToHexStringWithSpace(mainViewModel.key.value.first)} keyB ${
            ConversionHelper.bytesToHexStringWithSpace(
                mainViewModel.key.value.second
            )
        }"
    )

    tag?.let {
        val mifareClassic = MifareClassicHelper.getMifareInstance(tag)

        val keys = mainViewModel.key.value

        MifareClassicHelper.handleMifareClassic(mainViewModel, mifareClassic, context) {
            val customerModel = CustomerModel()

            if (mifareClassic.authenticateSectorWithKeyB(INFO_SECTOR, keys.second)) {
                val nameBlockIndex = mifareClassic.sectorToBlock(INFO_SECTOR) + NAME_BLOCK
                val name = hexByteArrayToString(mifareClassic.readBlock(nameBlockIndex))

                val tierBlockIndex = mifareClassic.sectorToBlock(INFO_SECTOR) + TIER_BLOCK
                val tier = hexByteArrayToDecimal(mifareClassic.readBlock(tierBlockIndex))

                val addressBlockIndex = mifareClassic.sectorToBlock(INFO_SECTOR) + ADDRESS_BLOCK
                val address = hexByteArrayToString(mifareClassic.readBlock(addressBlockIndex))

                customerModel.apply {
                    customerName = name
                    customerAddress = address
                    customerTier = tier
                }

                Log.i(TAG, "authenticate: userInfo name: $name tier $tier address $address")
            }

            if (mifareClassic.authenticateSectorWithKeyB(TRANSACTION_SECTOR, keys.second)) {
                val visitBlockIndex = mifareClassic.sectorToBlock(TRANSACTION_SECTOR) + VISIT_BLOCK
                val visit = hexByteArrayToDecimal(mifareClassic.readBlock(visitBlockIndex))

                val balancerBlockIndex = mifareClassic.sectorToBlock(TRANSACTION_SECTOR) + BALANCE_BLOCK
                val balance = hexByteArrayToDecimal(mifareClassic.readBlock(balancerBlockIndex))

                val passFlagBlockIndex = mifareClassic.sectorToBlock(TRANSACTION_SECTOR) + FLAG_BLOCK
                val passFlag =hexByteArrayToDecimal(mifareClassic.readBlock(passFlagBlockIndex))

                customerModel.apply {
                    customerVisitCount = visit
                    customerBalance = decimalToDoubleWithCents(balance)
                    customerFlag = passFlag
                }
                Log.i(TAG, "authenticate: transaction visitCount: $visit balance $balance passFlag $passFlag")
            }

            val allPropertiesFilled = listOfNotNull(
                customerModel.customerName,
                customerModel.customerTier,
                customerModel.customerAddress,
                customerModel.customerVisitCount,
                customerModel.customerBalance,
                customerModel.customerFlag
            ).size == 6

            Log.i(TAG, "authenticate: customerModel is all filled $allPropertiesFilled \n model is ${Gson().toJson(customerModel)}")

            if (allPropertiesFilled) {
                mainViewModel.setAuthentication(true)
                checkTransaction(mainViewModel, mifareClassic, customerModel)
            } else{
                mainViewModel.setAuthentication(false)
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
            Log.i(TAG, "checkTransaction: login the user")
            changeFlag(mifareClassic, FLAG_LOGGED_IN)
        }

        FLAG_LOGGED_IN -> {
            Log.i(TAG, "checkTransaction: logout the user")
            changeFlag(mifareClassic, FLAG_LOGGED_OUT)
        }

        FLAG_OPEN_TAB -> {
            Log.i(TAG, "checkTransaction: open tab can't logout")
//            changeFlag(mifareClassic, FLAG_LOGGED_IN)
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