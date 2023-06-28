package com.simplifier.circlebarnfc

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.simplifier.circlebarnfc.presentation.MainScreen
import com.simplifier.circlebarnfc.presentation.MainViewModel
import com.simplifier.circlebarnfc.presentation.theme.CircleBarNfcTheme
import com.simplifier.circlebarnfc.presentation.utils.NFCManager
import kotlinx.coroutines.Job

class MainActivity : ComponentActivity() {

    companion object {
        const val TAG = "ernesthor24 MainActivity"
    }

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var nfcManager: NFCManager

    private var nfcTimeoutJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        nfcManager = NFCManager(this, this, lifecycleScope, nfcTimeoutJob)

        //update everytime onNewIntent is called
        mainViewModel.setIntent(intent)

        setContent {
            CircleBarNfcTheme {
                MainScreen(mainViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i(TAG, "onNewIntent: $intent")
        setIntent(intent)
        mainViewModel.setIntent(intent)
        nfcManager.onCardRead()
    }


    override fun onResume() {
        super.onResume()
        nfcManager.checkNfcEnabled()
    }

    override fun onPause() {
        super.onPause()
        nfcManager.disableNfcForegroundDispatch()
    }
}



