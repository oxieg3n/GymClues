package com.gymcue.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gymcue.app.data.AppContainer
import com.gymcue.app.ui.App
import com.gymcue.app.ui.GymCueTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(b: Bundle?) { super.onCreate(b)
    val app = AppContainer(applicationContext)
    setContent { GymCueTheme { App(app) } } }
}
