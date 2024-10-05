package com.jdrf.btdx.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jdrf.btdx.ui.BtdxApp
import com.jdrf.btdx.ui.theme.BtdxTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mviState by viewModel.state.collectAsStateWithLifecycle()

            BtdxTheme {
                BtdxApp(
                    startDestination = mviState.startDestination
                )
            }
        }
    }
}
