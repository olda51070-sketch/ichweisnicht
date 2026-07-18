package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.ui.components.FrostedGlassBackground
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlatePrimary
import com.example.ui.theme.SlateSurface
import com.example.ui.viewmodel.PracticeViewModel

@Composable
fun MainScreen(
    viewModel: PracticeViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("home") }

    if (viewModel.isCallInProgress) {
        // Full screen immersive video call
        CallPracticeScreen(
            viewModel = viewModel,
            onBack = { viewModel.isCallInProgress = false },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // Standard Tabbed layout with unified Frosted Glass Background
        FrostedGlassBackground(modifier = modifier) {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = SlateSurface,
                        tonalElevation = NavigationBarDefaults.Elevation,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == "home",
                            onClick = { selectedTab = "home" },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = SlatePrimary,
                                indicatorColor = SlatePrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                unselectedTextColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier.testTag("nav_home")
                        )

                        NavigationBarItem(
                            selected = selectedTab == "history",
                            onClick = { selectedTab = "history" },
                            icon = { Icon(Icons.Default.History, contentDescription = "History") },
                            label = { Text("History") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = SlatePrimary,
                                indicatorColor = SlatePrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                unselectedTextColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier.testTag("nav_history")
                        )

                        NavigationBarItem(
                            selected = selectedTab == "tips",
                            onClick = { selectedTab = "tips" },
                            icon = { Icon(Icons.Default.MenuBook, contentDescription = "Guide") },
                            label = { Text("Guide") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = SlatePrimary,
                                indicatorColor = SlatePrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.secondary,
                                unselectedTextColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier.testTag("nav_tips")
                        )
                    }
                },
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxSize()
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (selectedTab) {
                        "home" -> HomeScreen(
                            viewModel = viewModel,
                            onStartPractice = { viewModel.startCall() }
                        )
                        "history" -> HistoryScreen(
                            viewModel = viewModel
                        )
                        "tips" -> TipsScreen()
                    }
                }
            }
        }
    }
}
