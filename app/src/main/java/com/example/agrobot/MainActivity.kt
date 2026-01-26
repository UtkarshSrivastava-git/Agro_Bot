package com.example.agrobot

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue // Added import
import androidx.compose.runtime.mutableStateOf // Added import
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue // Added import
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.agrobot.ui.theme.AgroBotTheme
import com.example.agrobot.ui.theme.AppNavGraph
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var ttsMgr: TtsManager
    private lateinit var chatVm: ChatViewModel
    private var selectedLang by mutableStateOf("en") 

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.auth.signInAnonymously().addOnCompleteListener { }
        ttsMgr = TtsManager(this)

        val baseUrl = "https://us-central1-agro-bot-37d6e.cloudfunctions.net/api/"
        val chatApi = NetworkModule.create(baseUrl)
        val repo = ChatRepository(chatApi, FirebaseFirestore.getInstance())
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anon"
        chatVm = ChatViewModel(repo, userId)

        enableEdgeToEdge()
        setContent {
            AgroBotTheme {
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Text("AgroBot Menu", modifier = Modifier.padding(16.dp))
                            Spacer(Modifier.height(16.dp))
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                                label = { Text("Home") },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate("home") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                            NavigationDrawerItem(
                                icon = { Icon(Icons.Filled.Settings, contentDescription = "Language Settings") },
                                label = { Text("Change Language") },
                                selected = false,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate("language") { launchSingleTop = true }
                                }
                            )
                        }
                    }
                ) {
                    AppNavGraph(
                        modifier = Modifier.fillMaxSize(),
                        navController = navController,
                        userLang = selectedLang, // Now correctly observed by Compose
                        onLanguageSelected = { lang ->
                            Log.d("MainActivity", "Language selected: $lang, current selectedLang: $selectedLang")
                            selectedLang = lang
                            Log.d("MainActivity", "selectedLang updated to: $selectedLang")
                        },
                        chatViewModel = chatVm,
                        ttsManager = ttsMgr,
                        onOpenDrawer = {
                            scope.launch { drawerState.open() }
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        ttsMgr.shutdown()
        super.onDestroy()
    }
}
