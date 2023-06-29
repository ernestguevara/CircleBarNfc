package com.simplifier.circlebarnfc.presentation.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simplifier.circlebarnfc.presentation.theme.ColMagenta
import com.simplifier.circlebarnfc.presentation.theme.ColYellow

@Composable
fun CustomSwitch(
    width: Dp = 72.dp,
    height: Dp = 40.dp,
    checkedTrackColor: Color = ColMagenta,
    uncheckedTrackColor: Color = ColYellow,
    gapBetweenThumbAndTrackEdge: Dp = 8.dp,
    borderWidth: Dp = 4.dp,
    cornerSize: Int = 50,
    iconInnerPadding: Dp = 4.dp,
    thumbSize: Dp = 24.dp,
    textArray: List<String>,
    onCheckedChange: (Boolean) -> Unit
) {

    // this is to disable the ripple effect
    val interactionSource = remember {
        MutableInteractionSource()
    }

    // state of the switch
    var isChecked by remember {
        mutableStateOf(true)
    }

    // for moving the thumb
    val alignment by animateAlignmentAsState(if (isChecked) 1f else -1f)

    Row(
        modifier = Modifier
            .background(
                Color.White,
                shape = RoundedCornerShape(8.dp)
            ) // Set the background color to white
            .padding(16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {

        CustomText(
            text = if (isChecked) textArray[0] else textArray[1],
            fontSize = 18.sp,
            color = Color.Black,
            isBold = true
        )

        // gap between switch and the text
        Spacer(modifier = Modifier.width(width = 8.dp))

        // outer rectangle with border
        Box(
            modifier = Modifier
                .size(width = width, height = height)
                .border(
                    width = borderWidth,
                    color = if (isChecked) checkedTrackColor else uncheckedTrackColor,
                    shape = RoundedCornerShape(percent = cornerSize)
                )
                .clickable(
                    indication = null,
                    interactionSource = interactionSource
                ) {
                    isChecked = !isChecked
                    onCheckedChange(isChecked)
                },
            contentAlignment = Alignment.Center
        ) {

            // this is to add padding at the each horizontal side
            Box(
                modifier = Modifier
                    .padding(
                        start = gapBetweenThumbAndTrackEdge,
                        end = gapBetweenThumbAndTrackEdge
                    )
                    .fillMaxSize(),
                contentAlignment = alignment
            ) {

                // thumb with icon
                Icon(
                    imageVector = if (isChecked) Icons.Filled.Person else Icons.Filled.ShoppingCart,
                    contentDescription = if (isChecked) textArray[0] else textArray[1],
                    modifier = Modifier
                        .size(size = thumbSize)
                        .background(
                            color = if (isChecked) checkedTrackColor else uncheckedTrackColor,
                            shape = CircleShape
                        )
                        .padding(all = iconInnerPadding),
                    tint = Color.White
                )
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
private fun animateAlignmentAsState(
    targetBiasValue: Float
): State<BiasAlignment> {
    val bias by animateFloatAsState(targetBiasValue)
    return derivedStateOf { BiasAlignment(horizontalBias = bias, verticalBias = 0f) }
}