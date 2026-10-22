package com.gymclues.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gymclues.app.data.*

private val GuideGold: Color
  @Composable get() = if (LocalAppTheme.current.dark) Color(0xFFFFD18A) else Color(0xFF845917)
private val GuideBlue: Color
  @Composable get() = if (LocalAppTheme.current.dark) Color(0xFF91B5D9) else Color(0xFF315E80)

@Composable
private fun GuideDialog(title: String, dismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
  val maxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.9f
  Dialog(dismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
    Surface(Modifier.padding(16.dp).widthIn(max = 640.dp).fillMaxWidth().heightIn(max = maxHeight),
      shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surfaceContainer, border = informationBorder()) {
      Column(Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
          IconButton(dismiss) { Icon(Icons.Filled.Close, "Close training guide") }
        }
        Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
        TextButton(dismiss, Modifier.align(Alignment.End)) { Text("Done") }
      }
    }
  }
}

@Composable
fun EquipmentSetupGuideCard(guide: EquipmentSetupGuide, library: TrainingGuideLibrary) {
  var diagramOpen by rememberSaveable(guide.image) { mutableStateOf(false) }
  AppCard {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Filled.Tune, null, tint = MaterialTheme.colorScheme.primary)
        Text("Equipment setup guide", style = MaterialTheme.typography.titleMedium)
      }
      SetupDiagram(guide)
      Text("Match the numbered parts to the adjustments below.", style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
      TextButton({ diagramOpen = true }) {
        Icon(Icons.Filled.ZoomIn, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Enlarge setup diagram")
      }
      SetupInstructions(guide, library)
    }
  }
  if (diagramOpen) EquipmentSetupGuideDialog(guide, library) { diagramOpen = false }
}

@Composable
private fun SetupDiagram(guide: EquipmentSetupGuide, modifier: Modifier = Modifier) {
  val ctx = LocalContext.current
  val image = remember(guide.image) { ctx.resources.getIdentifier(guide.image, "drawable", ctx.packageName) }
  Image(painterResource(image), "Numbered equipment setup diagram for ${guide.name}",
    modifier.fillMaxWidth().aspectRatio(640f / 400f))
}

@Composable
private fun SetupInstructions(guide: EquipmentSetupGuide, library: TrainingGuideLibrary) {
  Text(library.equipmentCheck, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
  guide.steps.forEachIndexed { i, step ->
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Surface(shape = MaterialTheme.shapes.small, color = GuideGold.copy(alpha = 0.12f)) {
        Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
          Text("${i + 1}", style = MaterialTheme.typography.labelLarge, color = GuideGold)
        }
      }
      Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(step.title, style = MaterialTheme.typography.titleSmall)
        Text(step.text, style = MaterialTheme.typography.bodyMedium)
      }
    }
  }
  if (guide.caution.isNotBlank()) Text(guide.caution, style = MaterialTheme.typography.bodyMedium, color = GuideGold)
  Text("Schematic: controls, adjustment ranges and limits vary by model. Follow the machine's placard and ask gym staff if a control is unfamiliar.",
    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
  GuideReferences(library, equipmentOnly = true)
}

@Composable
private fun EquipmentSetupGuideDialog(guide: EquipmentSetupGuide, library: TrainingGuideLibrary, dismiss: () -> Unit) {
  var scale by remember { mutableFloatStateOf(1f) }
  var offset by remember { mutableStateOf(Offset.Zero) }
  val transform = rememberTransformableState { zoom, pan, _ ->
    scale = (scale * zoom).coerceIn(1f, 3f)
    offset = if (scale == 1f) Offset.Zero else offset + pan
  }
  GuideDialog("Equipment setup", dismiss) {
    Text(guide.name, style = MaterialTheme.typography.titleMedium)
    Box(Modifier.fillMaxWidth().aspectRatio(640f / 400f).clipToBounds().transformable(transform)) {
      SetupDiagram(guide, Modifier.graphicsLayer(scaleX = scale, scaleY = scale,
        translationX = offset.x, translationY = offset.y))
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton({ scale = (scale - 0.5f).coerceAtLeast(1f); if (scale == 1f) offset = Offset.Zero }) {
        Icon(Icons.Filled.ZoomOut, "Zoom out setup diagram")
      }
      TextButton({ scale = 1f; offset = Offset.Zero }) { Text("Reset") }
      IconButton({ scale = (scale + 0.5f).coerceAtMost(3f) }) { Icon(Icons.Filled.ZoomIn, "Zoom in setup diagram") }
      Text("Pinch to zoom · drag to pan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    SetupInstructions(guide, library)
  }
}

@Composable
private fun CoachingSection(title: String, icon: ImageVector, rows: List<String>, tint: Color) {
  AppCard {
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, Modifier.size(20.dp), tint = tint)
        Text(title, style = MaterialTheme.typography.titleSmall, color = tint)
      }
      rows.forEach { text ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("•", color = tint)
          Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        }
      }
    }
  }
}

@Composable
private fun SafetyProfileContent(profile: ExerciseSafetyProfile) {
  CoachingSection("Safety tips", Icons.Filled.VerifiedUser, profile.safetyTips, MaterialTheme.colorScheme.primary)
  CoachingSection("Injury prevention", Icons.Filled.HealthAndSafety, profile.injuryPrevention, GuideBlue)
  CoachingSection("Common mistakes & corrections", Icons.Filled.Rule, profile.mistakes, GuideGold)
}

@Composable
fun ExerciseSafetyGuide(e: Exercise, library: TrainingGuideLibrary) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    SectionTitle("Safety & common mistakes")
    if (e.guideKind.isBlank() && library.variants(e.name).isEmpty()) {
      Muted("General training checks. A movement-specific guide has not been authored for this custom exercise.")
    }
    val fallback = library.safetyFor(e.name)
    SafetyProfileContent(ExerciseSafetyProfile(e.safetyTips.ifEmpty { fallback.safetyTips },
      e.injuryPrevention.ifEmpty { fallback.injuryPrevention }, e.mistakes.ifEmpty { fallback.mistakes }))
    if (e.safety.isNotBlank() && e.safety != "Equipment availability varies by gym.") {
      Text(e.safety, style = MaterialTheme.typography.bodyMedium)
    }
    Text(library.stopRule, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    GuideReferences(library)
  }
}

/** Program-only lifts and stretches get the same coaching access as catalog exercises. */
@Composable
fun ExerciseSafetyButton(name: String) {
  val ctx = LocalContext.current
  val library = remember { TrainingGuideLibrary.load(ctx.applicationContext) }
  var open by rememberSaveable(name) { mutableStateOf(false) }
  TextButton({ open = true }, contentPadding = PaddingValues(horizontal = 8.dp),
    modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = "Safety guide for $name" }) {
    Icon(Icons.Filled.VerifiedUser, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
    Text("Safety & form", style = MaterialTheme.typography.labelMedium)
  }
  if (open) ExerciseSafetyDialog(name, library) { open = false }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExerciseSafetyDialog(name: String, library: TrainingGuideLibrary, dismiss: () -> Unit) {
  val variants = remember(name) { library.variants(name).ifEmpty { listOf(TrainingGuideVariant("General checks", "general", emptyList())) } }
  var selected by rememberSaveable(name) { mutableIntStateOf(0) }
  var equipment by remember { mutableStateOf<EquipmentSetupGuide?>(null) }
  val variant = variants[selected.coerceIn(variants.indices)]
  GuideDialog("Safety & common mistakes", dismiss) {
    Text(name, style = MaterialTheme.typography.titleMedium)
    if (variants.size > 1) FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      variants.forEachIndexed { i, v -> FilterChip(selected == i, { selected = i }, label = { Text(v.label) }) }
    }
    if (variant.profile == "general") Muted("General training checks. A movement-specific guide has not been authored for this custom exercise.")
    SafetyProfileContent(library.profiles.getValue(variant.profile))
    if (variant.equipmentIds.isNotEmpty()) {
      SectionTitle("Equipment setup guides")
      variant.equipmentIds.mapNotNull(library.equipment::get).forEach { guide ->
        AppOutlinedButton({ equipment = guide }, Modifier.fillMaxWidth()) {
          Icon(Icons.Filled.Tune, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(guide.name)
        }
      }
    }
    Text(library.stopRule, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    GuideReferences(library)
  }
  equipment?.let { EquipmentSetupGuideDialog(it, library) { equipment = null } }
}

@Composable
private fun GuideReferences(library: TrainingGuideLibrary, equipmentOnly: Boolean = false) {
  var expanded by remember { mutableStateOf(false) }
  val ctx = LocalContext.current
  TextButton({ expanded = !expanded }) { Text(if (expanded) "Hide references" else "References") }
  if (expanded) {
    library.sources.filter { !equipmentOnly || it.title.contains("manuals") || it.title.contains("alignment") }.forEach { source ->
      TextButton({ runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(source.url))) } }) { Text(source.title) }
    }
  }
}
