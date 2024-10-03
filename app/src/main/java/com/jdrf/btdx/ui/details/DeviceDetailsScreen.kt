package com.jdrf.btdx.ui.details

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun DeviceDetailsScreen(
    macAddress: String?,
) {
    Surface(Modifier.background(color = MaterialTheme.colorScheme.background)) {
        Text(text = "DeviceDetailsScreen(macAddress=$macAddress)")
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light theme")
@Composable
fun DeviceDetailsScreenPreview() = DeviceDetailsScreen("MAC")
