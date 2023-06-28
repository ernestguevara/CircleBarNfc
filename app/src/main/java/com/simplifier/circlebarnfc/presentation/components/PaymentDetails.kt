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
import androidx.compose.ui.unit.dp
import com.simplifier.circlebarnfc.domain.model.CustomerModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDetails(customer: CustomerModel) {
    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = customer.customerName ?: "",
            onValueChange = { /* Handle name change */ },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = customer.customerTier?.toString() ?: "",
            onValueChange = { /* Handle tier change */ },
            label = { Text("Tier") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = customer.customerBalance?.toString() ?: "",
            onValueChange = { /* Handle balance change */ },
            label = { Text("Balance Left") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}