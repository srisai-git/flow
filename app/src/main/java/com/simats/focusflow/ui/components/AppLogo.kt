package com.simats.focusflow.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.simats.focusflow.R

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = "FocusFlow Logo",
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size)
    )
}