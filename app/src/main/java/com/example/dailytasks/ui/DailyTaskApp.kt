package com.example.dailytasks.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.dailytasks.ui.theme.AppearancePreferences
import com.example.dailytasks.ui.theme.DailyTaskTheme
import com.example.dailytasks.ui.theme.accentOption

@Composable
fun DailyTaskApp() {
    val context = LocalContext.current
    val preferences = remember { AppearancePreferences(context) }
    var themeMode by remember { mutableStateOf(preferences.themeMode) }
    var accentKey by remember { mutableStateOf(preferences.accentKey) }

    DailyTaskTheme(
        themeMode = themeMode,
        accent = accentOption(accentKey).color
    ) {
        MainScreen(
            themeMode = themeMode,
            accentKey = accentKey,
            onThemeModeChange = { mode ->
                preferences.themeMode = mode
                themeMode = mode
            },
            onAccentChange = { key ->
                preferences.accentKey = key
                accentKey = key
            }
        )
    }
}
