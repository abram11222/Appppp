package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val ModernDarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = Color.White,
    secondary = ModernAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFF93C5FD),
    tertiary = Color(0xFF94A3B8),
    background = DarkCanvas,
    onBackground = Color(0xFFF8FAFC),
    surface = DarkCard,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = DarkBorder,
    outlineVariant = Color(0xFF1E293B)
)

// 2026 Pure White Minimalist & High-Precision Design System
private val ModernLightColorScheme = lightColorScheme(
    primary = ModernPrimary,
    onPrimary = Color.White,
    primaryContainer = ModernPrimaryContainer,
    onPrimaryContainer = ModernPrimary,
    secondary = ModernAccent,
    onSecondary = Color.White,
    secondaryContainer = ModernAccentContainer,
    onSecondaryContainer = Color(0xFF1E40AF),
    tertiary = TextSecondary,
    onTertiary = Color.White,
    background = CleanCanvas, // أبيض نقي 100% لكامل التطبيق
    onBackground = TextPrimary,
    surface = CleanCard, // كروت بيضاء نقية
    onSurface = TextPrimary,
    surfaceVariant = CleanSurfaceHigh,
    onSurfaceVariant = TextSecondary,
    outline = CleanBorder,
    outlineVariant = CleanBorderSubtle
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // تم تثبيت السيم الأساسي ليكون أبيض ناصع 2026 دائماً ومريح للعين
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> ModernDarkColorScheme
        else -> ModernLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
