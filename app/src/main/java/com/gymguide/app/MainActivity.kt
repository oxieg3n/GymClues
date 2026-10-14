package com.gymguide.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gymguide.app.data.AppContainer
import com.gymguide.app.ui.App
import com.gymguide.app.ui.GymCueTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(b: Bundle?) { super.onCreate(b)
    val app = AppContainer(applicationContext)
    setContent { GymCueTheme { App(app) } } }
}
