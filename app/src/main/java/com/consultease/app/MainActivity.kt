package com.consultease.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.consultease.app.ui.AppViewModel
import com.consultease.app.ui.navigation.ConsultEaseApp
import com.consultease.app.ui.theme.ConsultEaseTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConsultEaseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ConsultEaseApp(viewModel = viewModel)
                }
            }
        }
    }
}
