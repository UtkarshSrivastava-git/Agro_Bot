package com.example.agrobot.ui.theme


import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.agrobot.R
import com.example.agrobot.ui.components.MyTopAppBar

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit,
    userLang: String,
    translate: suspend (String) -> String,
    navController: NavHostController,
    onOpenDrawer: (() -> Unit)? = null
) {

    var translatedLabels by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val labels = mapOf(
        "weather" to "Weather",
        "cropList" to "Crop Information",
        "schemes" to "Government Schemes",
        "chat" to "Chat with AgroBot",
        "scan_crop" to "Scan your crop"
    )

    LaunchedEffect(userLang) {

        try {
            if (userLang == "en" || userLang.isBlank()) {
                translatedLabels = labels
            } else {
                val newLabels = mutableMapOf<String, String>()
                for ((key, value) in labels) {

                    try {
                        val translatedValue = translate(value)
                        newLabels[key] = if (translatedValue.isNotBlank() && translatedValue != value) {
                            translatedValue
                        } else {
                            value
                        }
                    } catch (e: Exception) {

                        newLabels[key] = value
                    }
                }

                translatedLabels = newLabels
            }
        } catch (e: Exception) {

             if (translatedLabels.isEmpty()) {
                translatedLabels = labels
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            MyTopAppBar(
                title = "Home ($userLang)",
                navController = navController,
                onOpenDrawer = onOpenDrawer,
                forceDrawerMenu = true
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DashboardCard(
                    label = translatedLabels["weather"] ?: labels["weather"]!!,
                    iconResId = R.drawable.weather,
                    onClick = { onNavigate("weather") }
                )
            }
            item {
                DashboardCard(
                    label = translatedLabels["cropList"] ?: labels["cropList"]!!,
                    iconResId = R.drawable.wheat_plant,
                    onClick = { onNavigate("cropList") } // Corrected route
                )
            }
            item {
                DashboardCard(
                    label = translatedLabels["schemes"] ?: labels["schemes"]!!,
                    iconResId = R.drawable.bank,
                    onClick = { onNavigate("schemes") }
                )
            }
            item {
                DashboardCard(
                    label = translatedLabels["chat"] ?: labels["chat"]!!,
                    iconResId = R.drawable.assistant,
                    onClick = { onNavigate("chat") }
                )
            }
            item {
                DashboardCard(
                    label = translatedLabels["scan_crop"] ?: labels["scan_crop"]!!,
                    iconResId = R.drawable.camera,
                    onClick = { onNavigate("scan_crop") }
                )
            }
        }
    }
}

@Composable
fun DashboardCard(label: String, iconResId: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}