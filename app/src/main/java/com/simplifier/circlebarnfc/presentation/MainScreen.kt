package com.simplifier.circlebarnfc.presentation

import android.nfc.Tag
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import com.simplifier.circlebarnfc.presentation.components.EditableTextField
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

@OptIn(ExperimentalMaterial3Api::class)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = getColor(access))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
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
            } else{
                CustomText(text = stringResource(id = getMessage(CODE_TAP_CARD)), isBold = true)
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

        CustomSwitch(
            textArray = listOf(
                stringResource(id = R.string.label_access),
                stringResource(id = R.string.label_payment)
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) { isChecked ->
            mainViewModel.setAccess(isChecked)
        }

        EditableTextField(modifier = Modifier.align(Alignment.BottomCenter))

//        if (!access) {
//            EditableTextField(modifier = Modifier.align(Alignment.BottomCenter))
//        }
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

//@Composable
//fun testComposable() {
//    Box(
//        modifier = Modifier.fillMaxSize()
//            .background(color = getColor(access))
//    ) {
//        Column(
//            modifier = Modifier
//                .align(Alignment.Center)
//                .background(color = getColor(access))
//                .padding(horizontal = 16.dp, vertical = 8.dp),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            CustomText(text = stringResource(id = getMessage(CODE_TAP_CARD)), isBold = true)
//
//            Spacer(modifier = Modifier.height(height = 16.dp))
//
//            CustomerDetails(customer = customerDetails)
//        }
//
//        CustomSwitch(
//            textArray = listOf(
//                stringResource(id = R.string.label_access),
//                stringResource(id = R.string.label_payment)
//            ),
//            modifier = Modifier.align(Alignment.TopEnd)
//                .padding(8.dp)
//        ) { isChecked ->
//
//        }
//
//        if (!access) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter)
//                    .padding(16.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                TextField(
//                    value = "",
//                    onValueChange = { /* Handle name change */ },
//                    modifier = Modifier
//                        .weight(1f)
//                        .background(Color.White)
//                        .padding(8.dp),
//                    textStyle = MaterialTheme.typography.body1,
//                    colors = TextFieldDefaults.textFieldColors(
//                        backgroundColor = Color.White,
//                        focusedIndicatorColor = Color.Transparent,
//                        unfocusedIndicatorColor = Color.Transparent
//                    ),
//                    singleLine = true,
//                    placeholder = {
//                        Text(text = "Enter text")
//                    }
//                )
//
//                Spacer(modifier = Modifier.width(8.dp))
//
//                Button(
//                    onClick = { /* Handle button click */ },
//                    modifier = Modifier.wrapContentWidth()
//                ) {
//                    Text(text = "Submit")
//                }
//            }
//        }
//    }
//}

