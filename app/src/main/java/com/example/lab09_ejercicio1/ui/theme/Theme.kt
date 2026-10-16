package com.example.lab09_ejercicio1.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = RojoPokedex,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDCE1),
    onPrimaryContainer = AzulTinta,
    secondary = AzulTinta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3EDF7),
    onSecondaryContainer = AzulTinta,
    background = FondoClaro,
    onBackground = AzulTinta,
    surface = Color.White,
    onSurface = AzulTinta,
    surfaceVariant = Color(0xFFE8EEF4),
    onSurfaceVariant = AzulSuave,
    outline = Color(0xFFAAB8C5),
    outlineVariant = Color(0xFFD3DDE7)
)

private val DarkColors = darkColorScheme(
    primary = RojoNoche,
    onPrimary = FondoNoche,
    primaryContainer = Color(0xFF702A3B),
    onPrimaryContainer = Color(0xFFFFDDE4),
    secondary = AmarilloLuz,
    onSecondary = FondoNoche,
    secondaryContainer = Color(0xFF35465B),
    onSecondaryContainer = Color(0xFFE1ECF7),
    background = FondoNoche,
    onBackground = Color(0xFFF0F4F8),
    surface = SuperficieNoche,
    onSurface = Color(0xFFF0F4F8),
    surfaceVariant = Color(0xFF2A3B4D),
    onSurfaceVariant = Color(0xFFBDCCD9),
    outline = Color(0xFF708499),
    outlineVariant = Color(0xFF415368)
)

@Composable
fun Lab09ejercicio1Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val activity = LocalContext.current.findActivity()
    SideEffect {
        activity?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
