package com.simplifier.circlebarnfc.presentation

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.util.Log
import androidx.lifecycle.ViewModel
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