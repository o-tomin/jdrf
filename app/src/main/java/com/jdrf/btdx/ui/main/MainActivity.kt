package com.jdrf.btdx.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jdrf.btdx.ui.BtdxApp
import com.jdrf.btdx.ui.theme.BtdxTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fetchPermissionsState()
            }
        }

        setContent {
            val mviState by viewModel.state.collectAsStateWithLifecycle()

            BtdxTheme {
                BtdxApp(
                    startDestination = mviState.startDestination,
                    isAllPermissionsGranted = mviState.bluetoothPermissionsGranted
                )
            }
        }
    }
}
