package com.gymclues.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymclues.app.R
import androidx.core.view.WindowCompat

val Green = Color(0xFF7CE4AE)
private val DefaultScheme = darkColorScheme(
  primary = Color(0xFF04F730), onPrimary = Color(0xFF0B2819),
  primaryContainer = Color(0xFF0C0C0C), onPrimaryContainer = Color(0xFFF5F5F5),
  secondary = Color(0xFFABD3B9), onSecondary = Color(0xFF152D20),
  secondaryContainer = Color(0xFF202020), onSecondaryContainer = Color(0xFFD4EBD8),
  tertiary = Color(0xFFE6BD7A), onTertiary = Color(0xFF30220F),
  tertiaryContainer = Color(0xFF413421), onTertiaryContainer = Color(0xFFF7D9AA),
  background = Color.Black, onBackground = Color(0xFFF5F5F5),
  surface = Color.Black, onSurface = Color(0xFFF5F5F5),
  surfaceVariant = Color(0xFF202020), onSurfaceVariant = Color(0xFFB4B4B4),
  surfaceContainerLowest = Color.Black, surfaceContainerLow = Color(0xFF0C0C0C),
  surfaceContainer = Color(0xFF0C0C0C), surfaceContainerHigh = Color(0xFF181818), surfaceContainerHighest = Color(0xFF282828),
  outline = Color(0xFF7F8F80), outlineVariant = Color(0xFF415046),
  inverseSurface = Color(0xFFF5F5F5), inverseOnSurface = Color(0xFF111111), inversePrimary = Color(0xFF246C48),
  error = Color(0xFFFFAAA3), onError = Color(0xFF3B1412),
  errorContainer = Color(0xFF301D1C), onErrorContainer = Color(0xFFFFDAD5),
  surfaceTint = Color.Transparent
)

enum class AppTheme(val id: String, val title: String, val description: String, val dark: Boolean, val colors: ColorScheme) {
  DEFAULT("default", "GymClues Default", "Black and charcoal, with logo-green outlines.", true, DefaultScheme),
  OCEAN("ocean", "Ocean", "Deep teal and aqua, with soft sand accents.", true, DefaultScheme.copy(
    primary = Color(0xFF7ADDD3), onPrimary = Color(0xFF082D2A),
    primaryContainer = Color(0xFF194641), onPrimaryContainer = Color(0xFFB7F4EB),
    secondary = Color(0xFFACD0CB), onSecondary = Color(0xFF17312E),
    secondaryContainer = Color(0xFF2A403D), onSecondaryContainer = Color(0xFFD1EDE8),
    tertiary = Color(0xFFEBC58F), onTertiary = Color(0xFF30230E),
    tertiaryContainer = Color(0xFF493820), onTertiaryContainer = Color(0xFFFFDFB2),
    background = Color(0xFF101717), onBackground = Color(0xFFECF5F1),
    surface = Color(0xFF101717), onSurface = Color(0xFFECF5F1),
    surfaceVariant = Color(0xFF2A403D), onSurfaceVariant = Color(0xFFB9CCC8),
    surfaceContainerLowest = Color(0xFF0B1211), surfaceContainerLow = Color(0xFF1B2624),
    surfaceContainer = Color(0xFF1B2624), surfaceContainerHigh = Color(0xFF273632), surfaceContainerHighest = Color(0xFF354740),
    outline = Color(0xFF859A93), outlineVariant = Color(0xFF435851), inversePrimary = Color(0xFF246D64))),
  EMBER("ember", "Ember", "Warm charcoal and apricot, with olive accents.", true, DefaultScheme.copy(
    primary = Color(0xFFF0B184), onPrimary = Color(0xFF352014),
    primaryContainer = Color(0xFF553322), onPrimaryContainer = Color(0xFFFFDCC4),
    secondary = Color(0xFFE0C2A3), onSecondary = Color(0xFF36291B),
    secondaryContainer = Color(0xFF473729), onSecondaryContainer = Color(0xFFF6E1CC),
    tertiary = Color(0xFFD7D28C), onTertiary = Color(0xFF2B2C10),
    tertiaryContainer = Color(0xFF414122), onTertiaryContainer = Color(0xFFF2EDB0),
    background = Color(0xFF18120F), onBackground = Color(0xFFFFF0E5),
    surface = Color(0xFF18120F), onSurface = Color(0xFFFFF0E5),
    surfaceVariant = Color(0xFF473729), onSurfaceVariant = Color(0xFFD2BEAD),
    surfaceContainerLowest = Color(0xFF120D0A), surfaceContainerLow = Color(0xFF281F19),
    surfaceContainer = Color(0xFF281F19), surfaceContainerHigh = Color(0xFF352921), surfaceContainerHighest = Color(0xFF483A30),
    outline = Color(0xFFA18A78), outlineVariant = Color(0xFF604C3E), inversePrimary = Color(0xFF8A4D2D))),
  ROSEWOOD("rosewood", "Rosewood", "Burgundy and blush, with gentle sage accents.", true, DefaultScheme.copy(
    primary = Color(0xFFF2B0BA), onPrimary = Color(0xFF391B23),
    primaryContainer = Color(0xFF51303A), onPrimaryContainer = Color(0xFFFFD9DF),
    secondary = Color(0xFFD9BDC4), onSecondary = Color(0xFF38262B),
    secondaryContainer = Color(0xFF46333A), onSecondaryContainer = Color(0xFFF5DFE4),
    tertiary = Color(0xFFBFD89A), onTertiary = Color(0xFF22300F),
    tertiaryContainer = Color(0xFF344528), onTertiaryContainer = Color(0xFFDEF0C4),
    background = Color(0xFF191215), onBackground = Color(0xFFFFF0F2),
    surface = Color(0xFF191215), onSurface = Color(0xFFFFF0F2),
    surfaceVariant = Color(0xFF46333A), onSurfaceVariant = Color(0xFFD0BBC3),
    surfaceContainerLowest = Color(0xFF120D0F), surfaceContainerLow = Color(0xFF2A2024),
    surfaceContainer = Color(0xFF2A2024), surfaceContainerHigh = Color(0xFF382A30), surfaceContainerHighest = Color(0xFF4B3941),
    outline = Color(0xFFA58A94), outlineVariant = Color(0xFF634952), inversePrimary = Color(0xFF91465A))),
  DAYLIGHT("daylight", "Daylight", "Warm ivory and pine, with copper accents.", false, lightColorScheme(
    primary = Color(0xFF276745), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD8EBDD), onPrimaryContainer = Color(0xFF153C24),
    secondary = Color(0xFF4E6A58), onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0E9DF), onSecondaryContainer = Color(0xFF293C2D),
    tertiary = Color(0xFF845917), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF5E2BD), onTertiaryContainer = Color(0xFF473109),
    background = Color(0xFFF5F3EB), onBackground = Color(0xFF243128),
    surface = Color(0xFFF5F3EB), onSurface = Color(0xFF243128),
    surfaceVariant = Color(0xFFE0E9DF), onSurfaceVariant = Color(0xFF526252),
    surfaceContainerLowest = Color(0xFFFFFFFF), surfaceContainerLow = Color(0xFFFFFEF8),
    surfaceContainer = Color(0xFFFFFEF8), surfaceContainerHigh = Color(0xFFE2E7DC), surfaceContainerHighest = Color(0xFFD6DDCF),
    outline = Color(0xFF737F70), outlineVariant = Color(0xFFBEC9B9),
    inverseSurface = Color(0xFF243128), inverseOnSurface = Color(0xFFF5F3EB), inversePrimary = Green,
    error = Color(0xFFAC3229), onError = Color.White,
    errorContainer = Color(0xFFFADBD5), onErrorContainer = Color(0xFF541A15), surfaceTint = Color.Transparent));

  companion object {
    fun fromId(id: String?) = entries.firstOrNull { it.id == id } ?: DEFAULT
  }
}

internal const val THEME_KEY = "theme"
internal val LocalAppTheme = staticCompositionLocalOf { AppTheme.DEFAULT }

// Bundled Inter stays available offline; resource fonts apply the declared variable-font weight.
val Inter = FontFamily(Font(R.font.inter, FontWeight.Normal), Font(R.font.inter, FontWeight.Medium), Font(R.font.inter, FontWeight.SemiBold))
val Condensed = Inter
private fun type(size: Int, height: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
  fontFamily = Inter, fontWeight = weight, fontSize = size.sp, lineHeight = height.sp,
  letterSpacing = 0.sp, fontFeatureSettings = "tnum")
private val Type = Typography(
  displayLarge = type(32, 38), displayMedium = type(30, 36), displaySmall = type(28, 34),
  headlineLarge = type(28, 34, FontWeight.SemiBold), headlineMedium = type(24, 30, FontWeight.Medium),
  headlineSmall = type(24, 30, FontWeight.SemiBold),
  titleLarge = type(20, 26, FontWeight.SemiBold), titleMedium = type(16, 23, FontWeight.Medium),
  titleSmall = type(14, 20, FontWeight.Medium),
  bodyLarge = type(16, 24), bodyMedium = type(14, 21), bodySmall = type(12, 18),
  labelLarge = type(14, 20, FontWeight.Medium), labelMedium = type(12, 18, FontWeight.Medium), labelSmall = type(12, 16)
)
private val Shape = Shapes(extraSmall = RoundedCornerShape(6.dp), small = RoundedCornerShape(8.dp),
  medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(20.dp), extraLarge = RoundedCornerShape(24.dp))

@Composable fun GymCluesTheme(content: @Composable () -> Unit) {
  val context = LocalContext.current
  val prefs = remember(context) { context.getSharedPreferences("ui", Context.MODE_PRIVATE) }
  var theme by remember(prefs) { mutableStateOf(AppTheme.fromId(prefs.getString(THEME_KEY, null))) }
  DisposableEffect(prefs) {
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
      if (key == THEME_KEY || key == null) theme = AppTheme.fromId(prefs.getString(THEME_KEY, null))
    }
    prefs.registerOnSharedPreferenceChangeListener(listener)
    onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
  }
  val window = (LocalView.current.context as? Activity)?.window
  SideEffect {
    window?.let {
      it.statusBarColor = theme.colors.background.toArgb()
      it.navigationBarColor = theme.colors.background.toArgb()
      WindowCompat.getInsetsController(it, it.decorView).apply {
        isAppearanceLightStatusBars = !theme.dark
        isAppearanceLightNavigationBars = !theme.dark
      }
    }
  }
  CompositionLocalProvider(LocalAppTheme provides theme) { MaterialTheme(theme.colors, Shape, Type, content) }
}
