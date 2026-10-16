package com.gymclues.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gymclues.app.data.AppContainer
import com.gymclues.app.ui.App
import com.gymclues.app.ui.GymCluesTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(b: Bundle?) { super.onCreate(b)
    val app = AppContainer(applicationContext)
    setContent { GymCluesTheme { App(app) } } }
}
