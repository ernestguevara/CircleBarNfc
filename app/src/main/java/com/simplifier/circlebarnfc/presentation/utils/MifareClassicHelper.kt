package com.simplifier.circlebarnfc.presentation.utils

import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.util.Log
import com.simplifier.circlebarnfc.main
import com.simplifier.circlebarnfc.presentation.MainViewModel
import java.io.IOException

object MifareClassicHelper {
    private const val TAG = "MifareClassicHelper"

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
                Log.i(TAG, "handleMifareClassic: IO Exception")
                mainViewModel.setMessage("Please check card connection")
                mainViewModel.tagRemoved()
            } catch (e: Exception) {
                Log.i(TAG, "Error reading data: ${e.message}")
                mainViewModel.setMessage("Error reading data: ${e.message}")
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
        operations: (MifareClassic) -> Unit
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
                        mainViewModel.setMessage("Invalid Access!\nPlease check your card...")
                        mainViewModel.setAuthentication(false)
                        Log.i(TAG, "handleMifareClassic: authentication failed")
                    }
                } catch (e: IOException) {
                    Log.i(TAG, "handleMifareClassic: IO Exception")
                    mainViewModel.setMessage("Please Tap Card")
                } catch (e: Exception) {
                    mainViewModel.setMessage("Error reading data: ${e.message}")
                } finally {
                    mifareClassic.close()
                }
            }
        }
    }
}