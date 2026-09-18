package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ========================================================
// 2026 Saint Stephen Deacons Service Palette
// مستوحى بدقة من لوجو خدمة الشهيد استفانوس للشمامسة:
// كحلي ملكي عميق (Midnight Navy) + ذهبي وقاد (Radiant Gold & Amber) + أبيض ناصع
// ========================================================

// 1. ألوان اللوجو الأساسية: كحلي داكن ملكي من شريط اللوجو
val ModernPrimary = Color(0xFF001F54) // كحلي عميق ملكي
val ModernPrimaryDark = Color(0xFF001236)
val ModernPrimaryLight = Color(0xFF0A2E7A)
val ModernPrimaryContainer = Color(0xFFF0F4FA)

// 2. ألوان الهالة والكتابة: ذهبي وقاد مشرق وعنبر برتقالي
val ModernAccent = Color(0xFFF59E0B) // ذهبي طقسي مشرق
val ModernAccentContainer = Color(0xFFFEF3C7) // ذهبي ناعم للخلفيات
val ModernGold = Color(0xFFFFB800)
val ModernGoldDark = Color(0xFFD97706)
val ModernGoldContainer = Color(0xFFFEF3C7)
val ModernOrange = Color(0xFFEA580C)
val ModernSilver = Color(0xFFCBD5E1)

// 3. درجات اللون الأبيض النقي لعام 2026 (Pure White Canvas)
val CleanCanvas = Color(0xFFFFFFFF) // أبيض 100% لخلفيات الشاشات
val CleanCard = Color(0xFFFFFFFF) // أبيض نقي للكروت
val CleanSurfaceHigh = Color(0xFFFAFAFA) // أبيض عالي النقاوة
val CleanBorder = Color(0xFFE2E8F0) // حدود فائقة الرقة 1dp ناعمة
val CleanBorderSubtle = Color(0xFFF1F5F9)

// 4. ألوان النصوص والتباين البصري
val TextPrimary = Color(0xFF001F54) // كحلي عميق مقروء بوضوح فائق
val TextSecondary = Color(0xFF475569) // رمادي أزرق هادئ ومريح
val TextMuted = Color(0xFF94A3B8)

// 5. حالات المؤشرات (حضور، نجاح، تنبيه)
val ModernSuccess = Color(0xFF16A34A)
val ModernSuccessContainer = Color(0xFFF0FDF4)
val ModernWarning = Color(0xFFD97706)
val ModernWarningContainer = Color(0xFFFFFBEB)
val ModernError = Color(0xFFDC2626)
val ModernErrorContainer = Color(0xFFFEF2F2)

// Dark Theme (احتياطي فقط، السيم الافتراضي هو الأبيض بالكامل)
val DarkCanvas = Color(0xFF000E26)
val DarkCard = Color(0xFF00183F)
val DarkBorder = Color(0xFF0F2C66)

// Aliases for backward compatibility across all legacy components
val LiturgicalBurgundy = ModernPrimary
val LiturgicalBurgundyDark = ModernPrimaryDark
val LiturgicalBurgundyLight = ModernPrimaryLight
val LiturgicalBurgundyContainer = ModernPrimaryContainer

val LiturgicalGold = ModernGold
val LiturgicalGoldDark = ModernGoldDark
val LiturgicalGoldLight = ModernAccent
val LiturgicalGoldContainer = ModernGoldContainer

val ChurchNavy = ModernPrimary
val ChurchNavyLight = ModernPrimaryLight
val ChurchNavyDark = ModernPrimaryDark

val ParchmentBackground = CleanCanvas
val ParchmentCard = CleanCard
val ParchmentStroke = CleanBorder
val SurfaceTintWarm = CleanBorderSubtle

val DarkChurchCanvas = DarkCanvas
val DarkChurchSurface = DarkCard
val DarkChurchSurfaceVariant = Color(0xFF1E293B)
val DarkChurchBorder = DarkBorder

val LiturgicalGreen = ModernSuccess
val LiturgicalGreenContainer = ModernSuccessContainer
val LiturgicalAmber = ModernWarning
val LiturgicalAmberContainer = ModernWarningContainer
val LiturgicalBlue = ModernAccent
val LiturgicalRed = ModernError

val GreenSuccess = ModernSuccess
val AmberWarning = ModernWarning
