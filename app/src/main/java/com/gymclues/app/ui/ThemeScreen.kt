package com.gymclues.app.ui

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable fun ThemeScreen() {
  val selected = LocalAppTheme.current
  val prefs = LocalContext.current.getSharedPreferences("ui", Context.MODE_PRIVATE)
  BoxWithConstraints(Modifier.fillMaxSize()) {
    val columns = if (maxWidth >= 650.dp) 2 else 1
    LazyVerticalGrid(GridCells.Fixed(columns), Modifier.padding(horizontal = 20.dp),
      contentPadding = PaddingValues(vertical = 20.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      item(span = { GridItemSpan(maxLineSpan) }) {
        ScreenTitle("Theme", "Current: ${selected.title}. Changes apply instantly and are saved on this device.")
      }
      items(AppTheme.entries, key = { it.id }, span = { GridItemSpan(if (it == AppTheme.DEFAULT) columns else 1) }) { theme ->
        MaterialTheme(colorScheme = theme.colors) {
          val cs = MaterialTheme.colorScheme
          val active = theme == selected
          Surface(color = cs.surfaceContainer, shape = MaterialTheme.shapes.medium,
            border = BorderStroke(if (active) 2.dp else 1.dp, if (active || theme == AppTheme.DEFAULT) cs.primary else cs.outlineVariant)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(theme.title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                if (active) Icon(Icons.Filled.CheckCircle, "Selected theme", Modifier.size(20.dp), tint = cs.primary)
              }
              Muted(theme.description)
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeSwatch("Canvas", cs.background, cs.onBackground, Modifier.weight(1f))
                ThemeSwatch("Card", cs.primaryContainer, cs.onPrimaryContainer, Modifier.weight(1f))
                ThemeSwatch("Accent", cs.primary, cs.onPrimary, Modifier.weight(1f))
              }
              AppButton({ prefs.edit().putString(THEME_KEY, theme.id).apply() },
                Modifier.fillMaxWidth().semantics { contentDescription = if (active) "${theme.title}, selected" else "Use ${theme.title} theme" },
                enabled = !active) { Text(if (active) "Selected" else "Use theme") }
            }
          }
        }
      }
    }
  }
}

@Composable private fun ThemeSwatch(label: String, color: Color, text: Color, modifier: Modifier) {
  Surface(modifier, color = color, shape = MaterialTheme.shapes.small) {
    Box(Modifier.heightIn(min = 36.dp).padding(6.dp), contentAlignment = Alignment.Center) {
      Text(label, color = text, style = MaterialTheme.typography.labelMedium)
    }
  }
}
