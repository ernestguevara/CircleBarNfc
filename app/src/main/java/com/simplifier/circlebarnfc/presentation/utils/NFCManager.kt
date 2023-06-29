package com.simplifier.circlebarnfc.presentation.utils

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import com.simplifier.circlebarnfc.MainActivity
import com.simplifier.circlebarnfc.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NFCManager(
    private val activity: MainActivity,
    private val context: Context,
    private val lifecycleScope: LifecycleCoroutineScope,
    private var nfcTimeoutJob: Job?
) {

    private val TAG = "ernesthor24 NFCManager"
    private var nfcAdapter: NfcAdapter? = null

    fun checkNfcEnabled() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(context)

        if (nfcAdapter == null) {
            showErrorDialog(context.getString(R.string.label_no_nfc), context.getString(R.string.message_no_nfc), false)
        } else if (!nfcAdapter!!.isEnabled) {
            // NFC is not enabled, prompt the user to enable it
            showErrorDialog(context.getString(R.string.label_enable_nfc), context.getString(R.string.message_enable_nfc))
        } else {
            // NFC is enabled, start NFC operations
            startNfcOperations()

            nfcTimeoutJob = lifecycleScope.launch {
                delay(5000)
                // Timeout occurred, reset NFC reader
                resetNfcReader()
            }
        }
    }

    private fun startNfcOperations() {
        // Enable NFC foreground dispatch
        enableNfcForegroundDispatch()
    }

    private fun enableNfcForegroundDispatch() {
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )

        if (nfcAdapter != null && nfcAdapter!!.isEnabled) {
            try {
                nfcAdapter!!.enableForegroundDispatch(
                    activity,
                    pendingIntent,
                    null,
                    null
                )
            } catch (ex: IllegalStateException) {
                Log.i(TAG, "Error: Could not disable the NFC foreground dispatch system. The activity was not in foreground.")
            }
        }
    }

    fun disableNfcForegroundDispatch() {
        if (nfcAdapter != null && nfcAdapter!!.isEnabled) {
            try {
                nfcAdapter!!.disableForegroundDispatch(activity)
            } catch (ex: IllegalStateException) {
                Log.i(TAG, "Error: Could not disable the NFC foreground dispatch system. The activity was not in foreground.")
            }
        }
    }

    // Reset the NFC reader by disabling and re-enabling foreground dispatch
    fun resetNfcReader() {
        // Disable NFC foreground dispatch
        disableNfcForegroundDispatch()

        // Re-enable NFC foreground dispatch
        enableNfcForegroundDispatch()
    }

    // Call this method when a card is successfully read
    fun onCardRead() {
        // Cancel the timeout job
        nfcTimeoutJob?.cancel()
    }

    private fun showErrorDialog(
        title: String,
        message: String,
        showPositiveButton: Boolean = true
    ) {
        val builder = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(context.getString(R.string.label_cancel)) { _, _ ->
                // Handle cancellation or show alternative flow
            }

        if (showPositiveButton) {
            builder.setPositiveButton(context.getString(R.string.label_settings)) { _, _ ->
                // Open device settings screen to enable NFC
                val intent =
                    Intent(android.provider.Settings.ACTION_NFC_SETTINGS)
                context.startActivity(intent)
            }
        }
        builder.show()
    }
}