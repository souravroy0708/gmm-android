package com.gmmdistributors.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.gmmdistributors.app.R

// Typography is deliberately one step larger than Material defaults: many customers read Bengali
// on small budget phones, often outdoors. Minimum touch target in the app is 48dp.

// GMM Brand Palette (from gmm/static/css/premium.css & theme.css)
val Forest = Color(0xFF0F2F1E)
val Forest2 = Color(0xFF14472C)
val Forest3 = Color(0xFF1B5A34)
val Leaf = Color(0xFF1F6B3A)
val Leaf2 = Color(0xFF2A8247)
val SunGold = Color(0xFFF59A23)
val SunDark = Color(0xFFE8731A)
val SunInk = Color(0xFF1B1200)
val WhatsAppGreen = Color(0xFF1FA855)
val MintLite = Color(0xFF7FD6A0)
val MintSoft = Color(0xFFE8F3EA)
val Danger = Color(0xFFB3261E)
val DangerBg = Color(0xFFFDE4E4)
val WarnBg = Color(0xFFFFF1CC)
val WarnInk = Color(0xFF6B4423)
// Category colours: fertilizer (earth green), bio-pesticide (neem amber), organic food (harvest terracotta)
val KindFert = Color(0xFF1F6B3A)
val KindPest = Color(0xFFB7791F)
val KindFood = Color(0xFFB4532A)
val KindAyush = Color(0xFF0F766E)  // tulsi teal: Ayush medicine

/** Dark mode state, set by GmmTheme so all surface tokens adapt automatically (Bas1Minute pattern). */
object ThemeState { var dark by mutableStateOf(false) }

val Paper: Color get() = if (ThemeState.dark) Color(0xFF0D1410) else Color(0xFFF5F7F2)
val Paper2: Color get() = if (ThemeState.dark) Color(0xFF1B271F) else Color(0xFFE6ECE3)
val CardBg: Color get() = if (ThemeState.dark) Color(0xFF151F19) else Color.White
val PaperLine: Color get() = if (ThemeState.dark) Color(0xFF2B3C32) else Color(0xFFDCE4D9)
val TextMain: Color get() = if (ThemeState.dark) Color(0xFFE6EEE8) else Color(0xFF14201A)
val Muted: Color get() = if (ThemeState.dark) Color(0xFF9DB0A4) else Color(0xFF566860)
val MutedOnDark = Color(0xFFCFE3D5)
val AccentGreen: Color get() = if (ThemeState.dark) MintLite else Leaf

val Display = FontFamily(
    Font(R.font.anek_bold, FontWeight.Bold),
    Font(R.font.anek_extrabold, FontWeight.ExtraBold),
)

val Body = FontFamily(
    Font(R.font.mukta_regular, FontWeight.Normal),
    Font(R.font.mukta_medium, FontWeight.Medium),
    Font(R.font.mukta_bold, FontWeight.Bold),
)

private val AppTypography = Typography(
    displayLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, lineHeight = 42.sp),
    displayMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 27.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 23.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 27.sp),
    titleMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 23.sp),
    bodySmall = TextStyle(fontFamily = Body, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp),
    labelLarge = TextStyle(fontFamily = Body, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp),
    labelMedium = TextStyle(fontFamily = Body, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 18.sp),
)

@Composable
fun GmmTheme(dark: Boolean = false, content: @Composable () -> Unit) {
    ThemeState.dark = dark
    MaterialTheme(
        colorScheme = if (dark) darkColorScheme(
            primary = MintLite,
            onPrimary = Forest,
            secondary = SunGold,
            onSecondary = SunInk,
            background = Color(0xFF0D1410),
            onBackground = Color(0xFFE6EEE8),
            surface = Color(0xFF151F19),
            onSurface = Color(0xFFE6EEE8),
            surfaceVariant = Color(0xFF1B271F),
            outline = Color(0xFF2B3C32),
        ) else lightColorScheme(
            primary = Leaf,
            onPrimary = Color.White,
            secondary = SunGold,
            onSecondary = SunInk,
            background = Color(0xFFF5F7F2),
            onBackground = Color(0xFF14201A),
            surface = Color.White,
            onSurface = Color(0xFF14201A),
            surfaceVariant = Color(0xFFE6ECE3),
            outline = Color(0xFFDCE4D9),
        ),
        typography = AppTypography,
    ) {
        CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides TextMain, content = content)
    }
}
