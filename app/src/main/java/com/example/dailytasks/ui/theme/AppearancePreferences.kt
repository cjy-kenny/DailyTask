package com.example.dailytasks.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.dailytasks.R

enum class ThemeMode(val storageKey: String, val labelRes: Int) {
    SYSTEM("system", R.string.theme_mode_system),
    LIGHT("light", R.string.theme_mode_light),
    DARK("dark", R.string.theme_mode_dark);

    companion object {
        fun fromStorage(value: String?): ThemeMode =
            entries.firstOrNull { it.storageKey == value } ?: SYSTEM
    }
}

data class AccentOption(
    val key: String,
    val color: Color,
    val labelRes: Int
)

val AccentOptions: List<AccentOption> = listOf(
    AccentOption("blue", Color(0xFF2D6CDF), R.string.accent_blue),
    AccentOption("teal", Color(0xFF0E8C8C), R.string.accent_teal),
    AccentOption("green", Color(0xFF2E9E6B), R.string.accent_green),
    AccentOption("purple", Color(0xFF7C5CE0), R.string.accent_purple),
    AccentOption("orange", Color(0xFFC76A1E), R.string.accent_orange),
    AccentOption("pink", Color(0xFFB84E86), R.string.accent_pink)
)

fun accentOption(key: String?): AccentOption =
    AccentOptions.firstOrNull { it.key == key } ?: AccentOptions.first()

class AppearancePreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var themeMode: ThemeMode
        get() = ThemeMode.fromStorage(prefs.getString(KEY_THEME_MODE, null))
        set(value) {
            prefs.edit().putString(KEY_THEME_MODE, value.storageKey).apply()
        }

    var accentKey: String
        get() = prefs.getString(KEY_ACCENT, null) ?: AccentOptions.first().key
        set(value) {
            prefs.edit().putString(KEY_ACCENT, value).apply()
        }

    private companion object {
        const val PREFS_NAME = "appearance"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_ACCENT = "accent"
    }
}
