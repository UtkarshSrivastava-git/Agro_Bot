package com.example.agrobot.ui.theme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.agrobot.ChatViewModel
import com.example.agrobot.TranslatorHelper
import com.example.agrobot.TtsManager

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Filled.Home, "Home")
    object Weather : BottomNavItem("weather", Icons.Filled.Cloud, "Weather")
    object Schemes : BottomNavItem("schemes", Icons.Filled.List, "Schemes")
    object Chat : BottomNavItem("chat", Icons.Filled.Message, "Chat")
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    onOpenDrawer: () -> Unit,
    chatViewModel: ChatViewModel,
    ttsManager: TtsManager,
    userLang: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.runtime.LaunchedEffect(userLang) {
        chatViewModel.userLangTag = userLang
    }

    val translate: suspend (String) -> String = {
        TranslatorHelper.translateText(it, "en", userLang)
    }

    NavHost(
        navController = navController,
        startDestination = "language",
        modifier = modifier
    ) {
        composable("language") {
            val languageSelectionViewModel: LanguageSelectionViewModel = viewModel()
            LanguageSelectionScreen(
                onLanguageSelected = {
                    onLanguageSelected(it)
                    navController.navigate(BottomNavItem.Home.route) {
                        popUpTo("language") { inclusive = true }
                    }
                },
                viewModel = languageSelectionViewModel
            )
        }

        composable(BottomNavItem.Home.route) {
            HomeScreen(
                onNavigate = { navController.navigate(it) },
                userLang = userLang,
                translate = translate,
                navController = navController,
                onOpenDrawer = onOpenDrawer,
            )
        }

        composable(BottomNavItem.Weather.route) {
            WeatherScreen(lang = userLang, navController = navController, translate = translate)
        }

        composable("cropList") {
            CropListScreen(navController = navController, userLang = userLang, translate = translate)
        }

        composable(
            "cropDetail/{cropId}",
            arguments = listOf(navArgument("cropId") { type = NavType.StringType })
        ) {
            CropDetailScreen(
                navController = navController,
                cropId = it.arguments?.getString("cropId"),
                userLang = userLang,
                translate = translate
            )
        }

        composable(BottomNavItem.Schemes.route) {
            val viewModel: ChatViewModel = viewModel()
            GovernmentSchemesScreen(viewModel)
        }

        composable("scan_crop") {
            PlaceholderScreen(navController = navController, title = "Scan Your Crop")
        }

        composable(BottomNavItem.Chat.route) {
            ChatScreen(
                viewModel = chatViewModel,
                ttsMgr = ttsManager,
                onSpeak = { reply, lang ->
                    ttsManager.speak(reply, lang)
                },
                navController = navController,
            )
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Weather,
        BottomNavItem.Schemes,
        BottomNavItem.Chat
    )
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        // Avoid multiple copies of the same destination when reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderScreen(navController: NavHostController, title: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("$title - Coming Soon!")
        }
    }
}
