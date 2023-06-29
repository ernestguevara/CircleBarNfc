package com.simplifier.circlebarnfc.presentation

import android.content.Context
import android.nfc.Tag
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.simplifier.circlebarnfc.R
import com.simplifier.circlebarnfc.domain.model.CustomerModel
import com.simplifier.circlebarnfc.presentation.components.CustomSwitch
import com.simplifier.circlebarnfc.presentation.components.CustomText
import com.simplifier.circlebarnfc.presentation.components.CustomerDetails
import com.simplifier.circlebarnfc.presentation.components.PaymentDetails
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_BAL_INSUFFICIENT
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_CARD_CONNECTION
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_CARD_INVALID
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_CLOSE_TAB
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_ERROR_READ
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_INCORRECT_ACCESS
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_INVALID_ACCESS
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_MAX_BAL
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_OPENING_TAB
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_TAB_OPEN_ERROR
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_TAP_CARD
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_THANK_YOU
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_WELCOME
import com.simplifier.circlebarnfc.presentation.utils.CoroutineHelper
import com.simplifier.circlebarnfc.presentation.utils.MifareClassicHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "ernesthor24 MainScreen"

@Composable
fun MainScreen(mainViewModel: MainViewModel) {

    val tag: Tag? by mainViewModel.tag.collectAsState()

    val context = LocalContext.current

    val taskJob = remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val message: String by mainViewModel.message.collectAsState()

    val messageCode: Int by mainViewModel.messageCode.collectAsState()

    val access: Boolean by mainViewModel.isAccess.collectAsState()
    val customerDetails: CustomerModel by mainViewModel.customerDetails.collectAsState()

    LaunchedEffect(tag) {
        taskJob.value?.cancel()
        if (tag != null) {
            CoroutineHelper.runOnIOThread {
                cardPolling(mainViewModel, tag, context, taskJob, coroutineScope)
            }
            authenticate(mainViewModel, tag)
            Log.i(TAG, "MainScreen: launched effect called tag not null")
        } else {
            mainViewModel.tagRemoved()
            mainViewModel.setMessageCode(CODE_TAP_CARD)
            Log.i(TAG, "MainScreen: launched effect called tag null")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CustomSwitch(
            textArray = listOf(
                stringResource(id = R.string.label_access),
                stringResource(id = R.string.label_payment)
            )
        ) { isChecked ->
            mainViewModel.setAccess(isChecked)
        }

        // gap between switch and the text
        Spacer(modifier = Modifier.height(height = 8.dp))

        if (messageCode == CODE_OPENING_TAB) {
            mainViewModel.getCustomerBalance()?.let { bal ->
                CustomText(text = stringResource(id = R.string.message_opening_tab, bal))
            }
        } else if (messageCode != 0){
            CustomText(text = stringResource(id = getMessage(messageCode)))
        }


        if (mainViewModel.checkCustomerFields(customerDetails)) {
            if (access) {
                CustomerDetails(customer = customerDetails)
            } else {
                PaymentDetails(customer = customerDetails)
            }
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
        MifareClassicHelper.polling(mainViewModel, tag)

        //wait 5 seconds to poll again
        delay(5000)

        //pass mainViewModel tag as it can change value
        cardPolling(mainViewModel, mainViewModel.tag.value, context, taskJob, coroutineScope)
    }
}

private fun authenticate(
    mainViewModel: MainViewModel,
    tag: Tag?
) {
    mainViewModel.authenticate(tag)
}

private fun getMessage(code: Int): Int {
    return when (code) {
        CODE_TAP_CARD -> {
            R.string.message_tap_card
        }

        CODE_CARD_CONNECTION -> {
            R.string.message_card_connection
        }

        CODE_ERROR_READ -> {
            R.string.message_error_read
        }


        CODE_INVALID_ACCESS -> {
            R.string.message_invalid_access
        }

        CODE_CARD_INVALID -> {
            R.string.message_card_invalid
        }

        CODE_MAX_BAL -> {
            R.string.message_max_balance
        }

        CODE_BAL_INSUFFICIENT -> {
            R.string.message_enough_balance
        }

        CODE_INCORRECT_ACCESS -> {
            R.string.message_incorrect_access
        }

        CODE_WELCOME -> {
            R.string.message_welcome
        }

        CODE_THANK_YOU -> {
            R.string.message_thank_you
        }

        CODE_OPENING_TAB -> {
            R.string.message_opening_tab
        }

        CODE_CLOSE_TAB -> {
            R.string.message_closing_tab
        }

        CODE_TAB_OPEN_ERROR -> {
            R.string.message_tab_open
        }

        else -> {
            0
        }
    }
}

