package com.simplifier.circlebarnfc.presentation

import android.nfc.Tag
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplifier.circlebarnfc.R
import com.simplifier.circlebarnfc.domain.model.CustomerModel
import com.simplifier.circlebarnfc.presentation.components.CustomSwitch
import com.simplifier.circlebarnfc.presentation.components.CustomText
import com.simplifier.circlebarnfc.presentation.components.CustomerDetails
import com.simplifier.circlebarnfc.presentation.components.PaymentDetails
import com.simplifier.circlebarnfc.presentation.theme.ColMagenta
import com.simplifier.circlebarnfc.presentation.theme.ColYellow
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
import com.simplifier.circlebarnfc.presentation.utils.Constants.DEBOUNCE_TIME
import com.simplifier.circlebarnfc.presentation.utils.NFCManager

private const val TAG = "ernesthor24 MainScreen"

@Composable
fun MainScreen(mainViewModel: MainViewModel, nfcManager: NFCManager) {
    val context = LocalContext.current

    val tag: Tag? by mainViewModel.tag.collectAsState()
    val messageCode: Int by mainViewModel.messageCode.collectAsState()
    val access: Boolean by mainViewModel.isAccess.collectAsState()
    val customerDetails: CustomerModel by mainViewModel.customerDetails.collectAsState()

    val transactionStatus: Boolean by mainViewModel.transactionStatus.collectAsState()

    LaunchedEffect(tag) {
        tag?.let {
            Log.i(TAG, "MainScreen: launched effect called tag not null")
            authenticate(mainViewModel, it)
        }
    }

    if (transactionStatus) {
        tag?.let {
            nfcManager.ignore(it, DEBOUNCE_TIME, {
                Log.i(TAG, "MainScreen: card removed")
                mainViewModel.setMifareTransactionStatus(isComplete = false)
                mainViewModel.tagRemoved()
                mainViewModel.setMessageCode(CODE_TAP_CARD)
            }, Handler(Looper.getMainLooper())
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = getColor(access))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (messageCode == CODE_OPENING_TAB) {
            mainViewModel.getCustomerBalance()?.let { bal ->
                CustomText(
                    text = stringResource(id = R.string.message_opening_tab, bal),
                    isBold = true,
                    fontSize = 20.sp
                )
            }
        } else if (messageCode != 0) {
            CustomText(text = stringResource(id = getMessage(messageCode)), isBold = true)
        }

        Spacer(modifier = Modifier.height(height = 16.dp))

        CustomSwitch(
            textArray = listOf(
                stringResource(id = R.string.label_access),
                stringResource(id = R.string.label_payment)
            )
        ) { isChecked ->
            mainViewModel.setAccess(isChecked)
        }

        Spacer(modifier = Modifier.height(height = 16.dp))

        if (mainViewModel.checkCustomerFields(customerDetails)) {
            if (access) {
                CustomerDetails(customer = customerDetails)
            } else {
                PaymentDetails(customer = customerDetails)
            }
        }
    }
}

private fun authenticate(
    mainViewModel: MainViewModel,
    tag: Tag
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

private fun getColor(access: Boolean): Color {
    return if (access) ColMagenta else ColYellow
}

