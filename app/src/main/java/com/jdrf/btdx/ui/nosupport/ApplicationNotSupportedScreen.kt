package com.jdrf.btdx.ui.nosupport

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jdrf.btdx.R
import com.jdrf.btdx.ui.theme.BtdxTheme

@Composable
fun ApplicationNotSupportedScreen(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.warning),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Visible
            )
            Spacer(Modifier.padding(vertical = 5.dp))
            Text(
                text = stringResource(R.string.application_not_supported),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Visible
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light theme")
@Composable
fun DeviceScannerScreenPreview() =
    BtdxTheme {
        ApplicationNotSupportedScreen()
    }
