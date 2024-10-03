package com.jdrf.btdx.ui.scanner

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun DeviceScannerScreen(
    onDeviceDetails: (String) -> Unit,
) {
    Surface(Modifier.background(color = MaterialTheme.colorScheme.background)) {
        Button(
            modifier = Modifier,
            onClick = { onDeviceDetails("MAC") }
        ) {
            Text(text = "DeviceScannerScreen")
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light theme")
@Composable
fun DeviceScannerScreenPreview() = DeviceScannerScreen {}
