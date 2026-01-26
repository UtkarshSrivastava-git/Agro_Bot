package com.example.agrobot.ui.theme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.agrobot.DownloadState
import com.example.agrobot.R

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun LanguageSelectionScreen(
    onLanguageSelected: (String) -> Unit,
    viewModel: LanguageSelectionViewModel = viewModel()
) {
    val languages = listOf(
        "English" to "en",
        "हिंदी" to "hi",
        "తెలుగు" to "te",
        "தமிழ்" to "ta",
        "मराठी" to "mr",
        "ગુજરાતી" to "gu",
        "ಕನ್ನಡ" to "kn",
        "বাংলা" to "bn"
    )

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.language_animation))
    val downloadState by viewModel.downloadState.collectAsState()

    // Listen for navigation events from the ViewModel
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { langCode ->
            onLanguageSelected(langCode)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val state = downloadState) {
            is DownloadState.Idle -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.size(64.dp)
                    )
                    Text("Choose Your Preferred Language", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(Modifier.height(20.dp))

                languages.forEach { (name, code) ->
                    Button(
                        onClick = { viewModel.downloadLanguageModel(code) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) { Text(name) }
                }
            }
            is DownloadState.Downloading -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Preparing translations...")
            }
            is DownloadState.Success -> {
                // Navigation is now handled by the LaunchedEffect.
                // This state just shows a confirmation message to the user.
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Download successful! Starting the app...")
            }
            is DownloadState.Error -> {
                Text("Error: ${state.message}")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.resetDownloadState() }) {
                    Text("Try Again")
                }
            }
        }
    }
}
