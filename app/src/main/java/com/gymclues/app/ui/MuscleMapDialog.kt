package com.gymclues.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gymclues.app.data.MuscleMapLibrary
import com.gymclues.app.data.MuscleMapVariant

private val TargetColor = Color(0xFFFF7765)
private val AssistingColor = Color(0xFFE9BF59)
private val StabilizerColor = Color(0xFF67B7F7)

/** Works independently of catalog IDs, including program-only lifts and warm-ups. */
@Composable
fun MuscleMapButton(exerciseName: String) {
  val ctx = LocalContext.current
  val library = remember { MuscleMapLibrary.load(ctx.applicationContext) }
  val variants = remember(exerciseName, library) { library.find(exerciseName) }
  if (variants.isEmpty()) return
  var open by rememberSaveable(exerciseName) { mutableStateOf(false) }
  TextButton(
    onClick = { open = true },
    contentPadding = PaddingValues(horizontal = 8.dp),
    modifier = Modifier.heightIn(min = 48.dp)
      .semantics { contentDescription = "Muscle map for $exerciseName" }
  ) {
    Icon(Icons.Filled.AccessibilityNew, contentDescription = null, modifier = Modifier.size(20.dp))
    Spacer(Modifier.width(4.dp))
    Text("Muscles", style = MaterialTheme.typography.labelMedium)
  }
  if (open) MuscleMapDialog(exerciseName, library, variants) { open = false }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MuscleMapDialog(
  name: String,
  library: MuscleMapLibrary,
  variants: List<MuscleMapVariant>,
  dismiss: () -> Unit
) {
  val ctx = LocalContext.current
  var selected by rememberSaveable(name) { mutableIntStateOf(0) }
  val variant = variants[selected.coerceIn(variants.indices)]
  val profile = library.profiles.getValue(variant.profile)
  val scroll = rememberScrollState()
  LaunchedEffect(selected) { scroll.scrollTo(0) }
  val image = remember(variant.image) {
    // These names are generated and coverage-tested against all program rows.
    ctx.resources.getIdentifier(variant.image, "drawable", ctx.packageName)
  }
  val maxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.9f
  Dialog(onDismissRequest = dismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
    Surface(
      modifier = Modifier.padding(16.dp).widthIn(max = 600.dp).fillMaxWidth().heightIn(max = maxHeight),
      shape = MaterialTheme.shapes.large,
      color = MaterialTheme.colorScheme.surfaceContainer, border = informationBorder()
    ) {
      Column(Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("Muscle mapping", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
          IconButton(onClick = dismiss) { Icon(Icons.Filled.Close, "Close muscle map") }
        }
        Column(
          Modifier.weight(1f, fill = false).verticalScroll(scroll),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(name, style = MaterialTheme.typography.titleMedium)
          if (variants.size > 1) {
            Text("Choose the movement you are doing", style = MaterialTheme.typography.bodySmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              variants.forEachIndexed { index, option ->
                FilterChip(selected == index, { selected = index }, label = { Text(option.label) })
              }
            }
          }
          FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            MuscleMapKey(when (profile.kind) { "mobility" -> "Mobility"; "recovery" -> "Recovery"; else -> "Target" }, TargetColor)
            MuscleMapKey("Assist", AssistingColor)
            MuscleMapKey("Stabilize", StabilizerColor)
          }
          Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            Text("FRONT", style = MaterialTheme.typography.labelSmall)
            Text("BACK", style = MaterialTheme.typography.labelSmall)
          }
          Image(
            painter = painterResource(image),
            contentDescription = "Front and back muscle map for $name" +
              variant.label.takeIf { it.isNotBlank() }?.let { ": $it" }.orEmpty() + ". Muscle roles listed below.",
            modifier = Modifier.fillMaxWidth().aspectRatio(520f / 580f)
          )
          MuscleRoleList(profile.targetLabel, profile.primary.map(library.muscles::getValue), TargetColor)
          MuscleRoleList("Assisting movers", profile.assisting.map(library.muscles::getValue), AssistingColor)
          MuscleRoleList("Stabilizers", profile.stabilizers.map(library.muscles::getValue), StabilizerColor)
          if (profile.note.isNotBlank()) Text(profile.note, style = MaterialTheme.typography.bodyMedium)
          Text(
            "Typical muscle roles, not measured activation. Technique and support change involvement. " +
              "Surface regions are simplified; deep muscles are projected schematically. " +
              "Both sides are shown for alternating exercises. Some contributing muscles are omitted.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          TextButton(onClick = {
            runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(profile.source))) }
          }) { Text("Anatomy reference") }
        }
        TextButton(onClick = dismiss, modifier = Modifier.align(Alignment.End)) { Text("Done") }
      }
    }
  }
}

@Composable
private fun MuscleMapKey(label: String, color: Color) {
  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
    Box(Modifier.size(8.dp).background(color, CircleShape))
    Text(label, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall)
  }
}

@Composable
private fun MuscleRoleList(title: String, muscles: List<String>, color: Color) {
  Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerHigh, border = informationBorder()) {
    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(10.dp).background(color, CircleShape))
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
      }
      Text(muscles.joinToString("\n").ifEmpty { "None separately mapped" }, style = MaterialTheme.typography.bodyMedium)
    }
  }
}
