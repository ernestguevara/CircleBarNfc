package com.simplifier.circlebarnfc.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.simplifier.circlebarnfc.R
import com.simplifier.circlebarnfc.domain.model.CustomerModel


@Composable
fun PaymentDetails(customer: CustomerModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        CustomTextField(customer.customerName, stringResource(id = R.string.label_name))
        CustomTextField(customer.customerTier.toString(), stringResource(id = R.string.label_tier))
        CustomTextField(customer.customerBalance.toString(), stringResource(id = R.string.label_balance))
    }
}