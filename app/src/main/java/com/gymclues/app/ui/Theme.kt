package com.gymclues.app.ui
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Green = Color(0xFF7CE4AE)
private val Scheme = darkColorScheme(
  primary = Green, onPrimary = Color(0xFF0A2C1C),
  primaryContainer = Color(0xFF203C30), onPrimaryContainer = Color(0xFFB6F3D0),
  secondary = Color(0xFFBCCBC3), onSecondary = Color(0xFF1B2B24),
  secondaryContainer = Color(0xFF26352F), onSecondaryContainer = Color(0xFFD3E8DA),
  tertiary = Color(0xFFA8C7FA), onTertiary = Color(0xFF152D4A),
  background = Color(0xFF0E1319), onBackground = Color(0xFFEDF2F7),
  surface = Color(0xFF0E1319), onSurface = Color(0xFFEDF2F7),
  surfaceVariant = Color(0xFF26313D), onSurfaceVariant = Color(0xFFAAB7C5),
  surfaceContainerLowest = Color(0xFF0A0F14), surfaceContainerLow = Color(0xFF141B23),
  surfaceContainer = Color(0xFF19222C), surfaceContainerHigh = Color(0xFF202B36), surfaceContainerHighest = Color(0xFF2A3744),
  outline = Color(0xFF465462), outlineVariant = Color(0xFF2D3A47),
  inverseSurface = Color(0xFFE2EAF1), inverseOnSurface = Color(0xFF202B36), inversePrimary = Color(0xFF246C48),
  error = Color(0xFFFFA39B), onError = Color(0xFF4B1D1A),
  errorContainer = Color(0xFF3D272A), onErrorContainer = Color(0xFFFFDAD5),
  surfaceTint = Green
)

/** System fonts keep every screen available offline and follow Android font scaling. */
val Condensed = FontFamily(Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight.Bold))
private val Sans = FontFamily.SansSerif
private val base = Typography()
private val Type = base.copy(
  displayLarge = base.displayLarge.copy(fontFamily = Sans, fontWeight = FontWeight.SemiBold, letterSpacing = (-1.5).sp, fontFeatureSettings = "tnum"),
  displayMedium = base.displayMedium.copy(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 46.sp, lineHeight = 52.sp, letterSpacing = (-1.5).sp, fontFeatureSettings = "tnum"),
  headlineMedium = base.headlineMedium.copy(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.5).sp, fontFeatureSettings = "tnum"),
  headlineSmall = base.headlineSmall.copy(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 25.sp, lineHeight = 32.sp, letterSpacing = (-0.5).sp),
  titleLarge = base.titleLarge.copy(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.3).sp),
  titleMedium = base.titleMedium.copy(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 23.sp, letterSpacing = 0.sp),
  titleSmall = base.titleSmall.copy(fontFamily = Sans, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
  bodyLarge = base.bodyLarge.copy(fontFamily = Sans, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.sp),
  bodyMedium = base.bodyMedium.copy(fontFamily = Sans, fontSize = 14.sp, lineHeight = 21.sp, letterSpacing = 0.sp),
  bodySmall = base.bodySmall.copy(fontFamily = Sans, fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.1.sp),
  labelLarge = base.labelLarge.copy(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
  labelMedium = base.labelMedium.copy(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.2.sp),
  labelSmall = base.labelSmall.copy(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp)
)
private val Shape = Shapes(extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(18.dp), large = RoundedCornerShape(24.dp), extraLarge = RoundedCornerShape(28.dp))

@Composable fun GymCluesTheme(content: @Composable () -> Unit) = MaterialTheme(Scheme, Shape, Type, content)
