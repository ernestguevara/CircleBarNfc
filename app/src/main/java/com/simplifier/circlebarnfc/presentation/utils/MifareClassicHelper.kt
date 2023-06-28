package com.simplifier.circlebarnfc.presentation.utils

import android.content.Context
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.util.Log
import android.widget.Toast
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

    fun handleMifareClassic(
        mainViewModel: MainViewModel,
        mifareClassic: MifareClassic?,
        context: Context,
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
                        mainViewModel.setAuthentication(false)
                        Log.i(TAG, "handleMifareClassic: authentication failed")
                    }
                } catch (e: IOException) {
                    Log.i(TAG, "handleMifareClassic: IO Exception")
                    CoroutineHelper.runOnMainThread {
                        Toast.makeText(
                            context,
                            "Please check card connection",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    CoroutineHelper.runOnMainThread {
                        Toast.makeText(
                            context,
                            "Error reading data: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } finally {
                    mifareClassic.close()
                }
            }
        }
    }
}