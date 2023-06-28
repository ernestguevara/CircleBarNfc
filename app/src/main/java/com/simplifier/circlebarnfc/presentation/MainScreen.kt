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
import androidx.compose.ui.unit.dp
import com.simplifier.circlebarnfc.domain.model.CustomerModel
import com.simplifier.circlebarnfc.presentation.components.CustomSwitch
import com.simplifier.circlebarnfc.presentation.components.CustomText
import com.simplifier.circlebarnfc.presentation.components.CustomerDetails
import com.simplifier.circlebarnfc.presentation.components.PaymentDetails
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
            mainViewModel.setMessage("Please Tap Card")
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
        CustomSwitch(textArray = listOf("Access", "Payment")) { isChecked ->
            mainViewModel.setAccess(isChecked)
        }

        // gap between switch and the text
        Spacer(modifier = Modifier.height(height = 8.dp))

        CustomText(text = message)

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

