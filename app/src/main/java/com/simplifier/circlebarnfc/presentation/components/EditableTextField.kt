package com.simplifier.circlebarnfc.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.simplifier.circlebarnfc.R
import com.simplifier.circlebarnfc.presentation.MainViewModel
import com.simplifier.circlebarnfc.presentation.utils.Constants.CODE_NO_VAL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableTextField(
    modifier: Modifier,
    mainViewModel: MainViewModel
) {
    var reloadVal by remember { mutableStateOf("") }
    val maxLength = 9

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            value = reloadVal,
            onValueChange = { newValue ->
                if (newValue.length <= maxLength) {
                    reloadVal = transformValue(newValue.filter { it.isDigit() })
                }
            },
            modifier = Modifier
                .weight(1f)
                .background(Color.White)
                .padding(8.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            enabled = true,
            placeholder = {
                Text(text = stringResource(id = R.string.label_reload_placeholder))
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = {
                val finalVal = checkValues(reloadVal)
                if (finalVal <= Int.MAX_VALUE || finalVal > 0) {
                    mainViewModel.reload(finalVal)
                } else {
                    mainViewModel.setMessageCode(CODE_NO_VAL)
                }
            },
            modifier = Modifier.wrapContentWidth()
        ) {
            Text(text = stringResource(id = R.string.label_reload))
        }
    }
}

fun transformValue(input: String): String {
    if (input.isEmpty()) {
        return "0.00"
    }

    val numericValue = input.toLongOrNull() ?: return "0.00"
    val transformedValue = numericValue.toDouble() / 100

    return "%.2f".format(transformedValue)
}

fun checkValues(newValue: String): Int {
    val numericValue = newValue.toDoubleOrNull() ?: 0.0
    return (numericValue * 100).toInt()
}