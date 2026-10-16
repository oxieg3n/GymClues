package com.gymclues.app.ui
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Green = Color(0xFF00FF41)
private val Scheme = darkColorScheme(
  primary = Green, onPrimary = Color(0xFF03200B),
  primaryContainer = Color(0xFF0D3A1B), onPrimaryContainer = Color(0xFF8DFFB0),
  secondary = Color(0xFF7DDC9A), onSecondary = Color(0xFF03200B),
  background = Color(0xFF141618), onBackground = Color(0xFFE7EBE6),
  surface = Color(0xFF141618), onSurface = Color(0xFFE7EBE6),
  surfaceVariant = Color(0xFF262A2E), onSurfaceVariant = Color(0xFFA3ABA4),
  surfaceContainerLowest = Color(0xFF101213), surfaceContainerLow = Color(0xFF191C1E),
  surfaceContainer = Color(0xFF1D2023), surfaceContainerHigh = Color(0xFF23272A), surfaceContainerHighest = Color(0xFF2A2F33),
  outline = Color(0xFF3B4247), outlineVariant = Color(0xFF2C3236),
  error = Color(0xFFFF6E6E)
)

/** Condensed system face (no font download) for numbers and headings: reads like a scoreboard. */
val Condensed = FontFamily(Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight.Bold))
private val base = Typography()
private val Type = base.copy(
  displayLarge = base.displayLarge.copy(fontFamily = Condensed, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp),
  displayMedium = base.displayMedium.copy(fontFamily = Condensed, fontWeight = FontWeight.Bold),
  headlineMedium = base.headlineMedium.copy(fontFamily = Condensed, fontWeight = FontWeight.Bold),
  headlineSmall = base.headlineSmall.copy(fontFamily = Condensed, fontWeight = FontWeight.Bold),
  titleLarge = base.titleLarge.copy(fontFamily = Condensed, fontWeight = FontWeight.Bold),
  titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
  labelLarge = base.labelLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
)
private val Shape = Shapes(small = RoundedCornerShape(10.dp), medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(24.dp))

@Composable fun GymCluesTheme(content: @Composable () -> Unit) = MaterialTheme(Scheme, Shape, Type, content)
