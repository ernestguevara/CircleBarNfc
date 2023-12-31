package com.simplifier.circlebarnfc.presentation.utils

import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.util.Log
import com.simplifier.circlebarnfc.presentation.MainViewModel
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_CARD_CONNECTION
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_ERROR_READ
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_INVALID_ACCESS
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_TAP_CARD
import java.io.IOException

object MifareClassicHelper {
    private const val TAG = "ernesthor24 MifareClassicHelper"

    private lateinit var mifareClassic: MifareClassic

    fun getMifareInstance(tag: Tag): MifareClassic {
        if (!MifareClassicHelper::mifareClassic.isInitialized) {
            try {
                mifareClassic = MifareClassic.get(tag)
            } catch (e: Exception) {
                Log.e(TAG, "Could not create MIFARE Classic reader for the provided tag.")
                throw e
            }
        }
        return mifareClassic
    }

    fun polling(mainViewModel: MainViewModel, tag: Tag?) {
        if (tag != null) {
            val mifareClassic = getMifareInstance(tag)

            try {
                mifareClassic.connect()
                //do nothing as tag is present
                Log.i(TAG, "handleMifareClassic: tag connected")
            } catch (e: IOException) {
                Log.i(TAG, "handleMifareClassic: IO Exception polling")
                mainViewModel.setMessageCode(CODE_CARD_CONNECTION)
                mainViewModel.tagRemoved()
            } catch (e: Exception) {
                Log.i(TAG, "Error reading data: ${e.message}")
                mainViewModel.setMessageCode(CODE_ERROR_READ)
            } finally {
                mifareClassic.close()
            }
        } else {
            //no tag
            Log.i(TAG, "handleMifareClassic: no tag")
            mainViewModel.tagRemoved()
        }
    }

    fun handleMifareClassic(
        mainViewModel: MainViewModel,
        mifareClassic: MifareClassic?,
        operations: (MifareClassic) -> Unit,
    ) {
        mifareClassic?.let {
            if (MifareClassicHelper::mifareClassic.isInitialized) {
                try {
                    mifareClassic.connect()
                    if (mifareClassic.authenticateSectorWithKeyA(0, mainViewModel.key.value.first)
                        || mifareClassic.authenticateSectorWithKeyB(0, mainViewModel.key.value.second)) {
                        mainViewModel.setAuthentication(true)
                        operations.invoke(mifareClassic)
                    } else {
                        mainViewModel.setMessageCode(CODE_INVALID_ACCESS)
                        mainViewModel.setAuthentication(false)
                        Log.i(TAG, "handleMifareClassic: authentication failed")
                    }
                } catch (e: IOException) {
                    Log.i(TAG, "handleMifareClassic: IO Exception transaction ${e.message}")
                    mainViewModel.setMessageCode(CODE_ERROR_READ)
                } catch (e: Exception) {
                    Log.i(TAG, "handleMifareClassic: Exception transaction ${e.message}")
                    mainViewModel.setMessageCode(CODE_ERROR_READ)
                } finally {
                    Log.i(TAG, "handleMifareClassic: transaction complete")
                    mifareClassic.close()
//                    mainViewModel.setMifareTransactionStatus(isComplete = true)
                }
            }
        }
    }
}