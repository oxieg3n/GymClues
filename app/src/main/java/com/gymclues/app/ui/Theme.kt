package com.gymclues.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymclues.app.R

val Green = Color(0xFF7CE4AE)
private val Scheme = darkColorScheme(
  primary = Green, onPrimary = Color(0xFF0B2819),
  primaryContainer = Color(0xFF203F31), onPrimaryContainer = Color(0xFFBAF4D1),
  secondary = Color(0xFFABD3B9), onSecondary = Color(0xFF152D20),
  secondaryContainer = Color(0xFF2B3C30), onSecondaryContainer = Color(0xFFD4EBD8),
  tertiary = Color(0xFFE6BD7A), onTertiary = Color(0xFF30220F),
  tertiaryContainer = Color(0xFF413421), onTertiaryContainer = Color(0xFFF7D9AA),
  background = Color(0xFF101512), onBackground = Color(0xFFF0F5EC),
  surface = Color(0xFF101512), onSurface = Color(0xFFF0F5EC),
  surfaceVariant = Color(0xFF2B3C30), onSurfaceVariant = Color(0xFFBCCABD),
  surfaceContainerLowest = Color(0xFF0C100D), surfaceContainerLow = Color(0xFF1C231E),
  surfaceContainer = Color(0xFF1C231E), surfaceContainerHigh = Color(0xFF26312B), surfaceContainerHighest = Color(0xFF354139),
  outline = Color(0xFF7F8F80), outlineVariant = Color(0xFF415046),
  inverseSurface = Color(0xFFF5F5F5), inverseOnSurface = Color(0xFF111111), inversePrimary = Color(0xFF246C48),
  error = Color(0xFFFFAAA3), onError = Color(0xFF3B1412),
  errorContainer = Color(0xFF301D1C), onErrorContainer = Color(0xFFFFDAD5),
  surfaceTint = Color.Transparent
)

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

@Composable fun GymCluesTheme(content: @Composable () -> Unit) = MaterialTheme(Scheme, Shape, Type, content)
