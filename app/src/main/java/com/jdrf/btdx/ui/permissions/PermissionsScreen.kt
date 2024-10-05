package com.jdrf.btdx.ui.permissions

import android.content.res.Configuration
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jdrf.btdx.R
import com.jdrf.btdx.ui.theme.BtdxTheme

@Composable
fun PermissionsScreen(
    modifier: Modifier = Modifier,
    viewModel: PermissionsViewModel = hiltViewModel(),
    onPermissionsGranted: () -> Unit
) {
    val mviState by viewModel.state.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        viewModel.fetchPermissionsState()
    }

    if (mviState.allPermissionsGranted) {
        onPermissionsGranted()
    } else {
        PermissionsScreen(
            modifier = modifier,
            permissionLauncher = permissionLauncher,
            mviState = mviState
        )
    }
}

@Composable
private fun PermissionsScreen(
    modifier: Modifier,
    permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>,
    mviState: PermissionsState,
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
                text = stringResource(R.string.please_grant_permissions),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Visible
            )
            Spacer(Modifier.padding(vertical = 5.dp))
            Button(
                onClick = {
                    permissionLauncher.launch(
                        mviState.permissions.keys.toList().toTypedArray()
                    )
                }
            ) {
                Text(
                    text = stringResource(R.string.grant),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Visible
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light theme")
@Composable
fun DeviceScannerScreenPreview() =
    BtdxTheme {
        PermissionsScreen {}
    }
