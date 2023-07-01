package com.simplifier.circlebarnfc.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

import androidx.compose.ui.unit.sp

@Composable
fun CustomText(
    text: String,
    isBold: Boolean = false,
    fontSize: TextUnit = 24.sp,
    color: Color = Color.White,
    alignment: TextAlign = TextAlign.Center,
    modifier: Modifier = Modifier
) {
    val style = TextStyle(
        fontWeight = if (isBold) FontWeight.Bold else null,
        fontSize = fontSize,
        color = color
    )

    Text(
        text = buildAnnotatedString {
            pushStyle(style.toSpanStyle())
            append(text)
        },
        modifier = modifier,
        style = style,
        textAlign = alignment,
        overflow = TextOverflow.Ellipsis
    )
}