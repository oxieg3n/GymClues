package com.gymclues.app.ui
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.gymclues.app.data.CatalogRepository
import com.gymclues.app.data.Exercise
import com.gymclues.app.media.MediaRemote
import com.gymclues.app.media.MusicApp
import com.gymclues.app.media.NowPlaying

/** A quiet, consistently sized control shared by workout set steppers. */
@Composable fun SetStepButton(add: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
  val green = MaterialTheme.colorScheme.primary
  OutlinedIconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp),
    colors = IconButtonDefaults.outlinedIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    border = BorderStroke(1.dp, if (enabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant)) {
    Icon(if (add) Icons.Filled.Add else Icons.Filled.Remove, contentDescription = if (add) "Add set" else "Remove set",
      tint = if (enabled) green else green.copy(alpha = 0.3f))
  }
}

/** A restrained removal control with a full touch target. */
@Composable fun RemoveCircleButton(label: String, onClick: () -> Unit) {
  val red = MaterialTheme.colorScheme.error
  Surface(onClick = onClick, shape = CircleShape, color = red.copy(alpha = 0.08f),
    modifier = Modifier.size(48.dp)) {
    Box(contentAlignment = Alignment.Center) { Icon(Icons.Filled.Close, contentDescription = label, tint = red, modifier = Modifier.size(18.dp)) }
  }
}

/** Links to every piece of equipment an exercise needs (machine, dumbbells, bench...). */
@OptIn(ExperimentalLayoutApi::class)
@Composable fun EquipmentLinks(catalog: CatalogRepository, e: Exercise, open: (equipmentId: String) -> Unit) {
  val items = e.equipmentIds.mapNotNull(catalog::equipment)
  if (items.isEmpty()) return
  FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
    items.forEach { q ->
      AssistChip(onClick = { open(q.id) }, label = { Text(q.name) },
        shape = RoundedCornerShape(10.dp),
        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
          labelColor = MaterialTheme.colorScheme.onSurfaceVariant, leadingIconContentColor = MaterialTheme.colorScheme.primary),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        leadingIcon = { Icon(Icons.Filled.FitnessCenter, null, Modifier.size(16.dp)) },
        trailingIcon = { Icon(Icons.Filled.ChevronRight, null, Modifier.size(16.dp)) })
    }
  }
}

/** Compact "notification-style" player for the user's chosen music app. */
@Composable fun NowPlayingBar(modifier: Modifier = Modifier) {
  val ctx = LocalContext.current
  val remote = remember { MediaRemote(ctx.applicationContext) }
  var state by remember { mutableStateOf<NowPlaying?>(null) }
  var access by remember { mutableStateOf(remote.hasAccess()) }
  var app by remember { mutableStateOf(remote.selected) }
  var menu by remember { mutableStateOf(false) }
  val lifecycle = LocalLifecycleOwner.current.lifecycle
  DisposableEffect(lifecycle) {
    val obs = LifecycleEventObserver { _, ev ->
      if (ev == Lifecycle.Event.ON_RESUME) { access = remote.hasAccess(); remote.stop(); remote.start { state = it } }
      if (ev == Lifecycle.Event.ON_PAUSE) remote.stop()
    }
    lifecycle.addObserver(obs); remote.start { state = it }
    onDispose { lifecycle.removeObserver(obs); remote.stop() }
  }
  Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
    color = MaterialTheme.colorScheme.surfaceContainerLow, border = informationBorder()) {
    Row(Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
      Box {
        IconButton({ menu = true }, Modifier.size(44.dp)) { Icon(Icons.Filled.MusicNote, "Choose music app: ${app.label}", Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) }
        DropdownMenu(menu, { menu = false }) {
          MusicApp.values().forEach { m -> val ok = remote.installed(m)
            DropdownMenuItem(text = { Text(m.label + if (ok) "" else " (not installed)") }, enabled = ok,
              trailingIcon = { if (m == app) Icon(Icons.Filled.Check, null) },
              onClick = { remote.select(m); app = m; menu = false }) }
        }
      }
      Column(Modifier.weight(1f).clickable { remote.openPlaying() }) {
        Text(state?.title?.ifBlank { null } ?: if (access) "Nothing playing" else "Music controls", style = MaterialTheme.typography.bodyMedium,
          maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(state?.artist?.ifBlank { null } ?: app.label, style = MaterialTheme.typography.bodySmall,
          maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      if (!access) TextButton({ remote.openAccessSettings() }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text("Connect") }
      else {
        IconButton({ remote.previous() }, Modifier.size(44.dp)) { Icon(Icons.Filled.SkipPrevious, "Previous", Modifier.size(20.dp)) }
        FilledTonalIconButton({ remote.playPause() }, Modifier.size(44.dp),
          colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary)) {
          Icon(if (state?.playing == true) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            if (state?.playing == true) "Pause" else "Play", Modifier.size(22.dp)) }
        IconButton({ remote.next() }, Modifier.size(44.dp)) { Icon(Icons.Filled.SkipNext, "Next", Modifier.size(20.dp)) }
      }
    }
  }
}

/** Exercise picker sheet: suggestions that fit the workout, or the entire database. Supports multi-select. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable fun ExercisePickerSheet(catalog: CatalogRepository, suggested: List<Exercise>?, exclude: Set<String>,
                                    onDismiss: () -> Unit, onCreate: (() -> Unit)? = null, onPick: (List<Exercise>) -> Unit) {
  var showAll by remember { mutableStateOf(suggested == null) }
  var cardioOnly by remember { mutableStateOf(false) }
  var query by remember { mutableStateOf("") }
  var muscle by remember { mutableStateOf<String?>(null) }
  val picked = remember { mutableStateListOf<String>() }
  val base = if (cardioOnly || showAll || suggested == null) catalog.exercises else suggested
  val list = base.filter { (!cardioOnly || it.movement == "Cardio") && it.id !in exclude && (muscle == null || muscle in it.primaryMuscles) &&
    (query.isBlank() || it.name.contains(query, true) || it.primaryMuscles.any { m -> m.contains(query, true) }) }
  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
    Column(Modifier.padding(horizontal = 20.dp).fillMaxHeight(0.85f)) {
      Text("Add exercises", style = MaterialTheme.typography.titleLarge)
      if (suggested != null) Row(verticalAlignment = Alignment.CenterVertically) {
        FilterChip(!showAll, { showAll = false }, { Text("Fits this workout") }); Spacer(Modifier.width(8.dp))
        FilterChip(showAll, { showAll = true }, { Text("All exercises") }) }
      FilterChip(cardioOnly, { cardioOnly = !cardioOnly; muscle = null }, { Text("Cardio") })
      AppTextField(query, { query = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text("Search") },
        leadingIcon = { Icon(Icons.Filled.Search, null) })
      LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        item { FilterChip(muscle == null, { muscle = null }, { Text("Any muscle") }) }
        items(catalog.muscles) { m -> FilterChip(muscle == m, { muscle = if (muscle == m) null else m }, { Text(m) }) }
      }
      LazyColumn(Modifier.weight(1f)) {
        if (list.isEmpty()) item { Text("No matching exercises.", Modifier.padding(vertical = 12.dp)) }
        if (onCreate != null) item { TextButton(onCreate) { Icon(Icons.Filled.Add, null); Spacer(Modifier.width(4.dp)); Text("Not listed? Add your own exercise") } }
        items(list, key = { it.id }) { e -> val on = e.id in picked
          AppListItem(headlineContent = { Text(e.name) },
            supportingContent = { Text("${e.primaryMuscles.joinToString()} · ${e.movement} · ${e.difficulty}") },
            trailingContent = { Checkbox(on, { if (it) picked += e.id else picked -= e.id }) },
            modifier = Modifier.clickable { if (on) picked -= e.id else picked += e.id })
        }
      }
      AppButton({ onPick(picked.mapNotNull(catalog::exercise)) }, Modifier.fillMaxWidth().padding(vertical = 12.dp).heightIn(min = 52.dp),
        enabled = picked.isNotEmpty()) { Text(if (picked.isEmpty()) "Select exercises" else "Add ${picked.size} exercise(s)") }
    }
  }
}

/** Embeds a YouTube link or a direct video file; anything else becomes an "Open" button. */
@SuppressLint("SetJavaScriptEnabled")
@Composable fun VideoEmbed(url: String) {
  val ctx = LocalContext.current
  val yt = youtubeId(url)
  val direct = url.substringBefore('?').lowercase().let { it.endsWith(".mp4") || it.endsWith(".webm") || it.endsWith(".m3u8") }
  if (yt != null || direct) {
    val html = if (yt != null)
      """<html><body style="margin:0;background:#000"><iframe width="100%" height="100%" style="position:absolute;inset:0;border:0"
         src="https://www.youtube-nocookie.com/embed/$yt?playsinline=1&rel=0" allow="autoplay; encrypted-media; picture-in-picture" allowfullscreen></iframe></body></html>"""
    else """<html><body style="margin:0;background:#000"><video src="$url" controls playsinline style="width:100%;height:100%"></video></body></html>"""
    AndroidView(factory = { c -> WebView(c).apply {
        settings.javaScriptEnabled = true; settings.mediaPlaybackRequiresUserGesture = true; settings.domStorageEnabled = true
        webChromeClient = WebChromeClient()
        loadDataWithBaseURL("https://app.gymclues.local", html, "text/html", "utf-8", null) } },
      modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(MaterialTheme.shapes.medium))
  }
  TextButton({ runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } }) {
    Icon(Icons.Filled.OpenInNew, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text(if (yt != null || direct) "Open video in another app" else "Open video") }
}
fun youtubeId(url: String): String? {
  val u = runCatching { Uri.parse(url) }.getOrNull() ?: return null
  val host = u.host?.lowercase() ?: return null
  return when {
    host.endsWith("youtu.be") -> u.lastPathSegment
    host.contains("youtube") && u.path?.startsWith("/watch") == true -> u.getQueryParameter("v")
    host.contains("youtube") && (u.path?.startsWith("/shorts/") == true || u.path?.startsWith("/embed/") == true) -> u.pathSegments.getOrNull(1)
    else -> null
  }?.takeIf { it.matches(Regex("[A-Za-z0-9_-]{6,20}")) }
}

@Composable fun PhotoRow(uris: List<String>, onRemove: ((String) -> Unit)? = null) {
  if (uris.isEmpty()) return
  LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    items(uris) { u -> Box {
      AsyncImage(model = if (u.startsWith("/")) java.io.File(u) else u, contentDescription = "Equipment photo",
        modifier = Modifier.size(96.dp).clip(MaterialTheme.shapes.small), contentScale = ContentScale.Crop)
      if (onRemove != null) Box(Modifier.align(Alignment.TopEnd).padding(4.dp)) { RemoveCircleButton("Remove photo") { onRemove(u) } }
    } }
  }
}

@Composable fun SectionTitle(t: String) {
  Text(t, Modifier.padding(top = 16.dp, bottom = 8.dp), style = MaterialTheme.typography.titleMedium,
    color = MaterialTheme.colorScheme.secondary)
}
@Composable fun Muted(t: String) = Text(t, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
