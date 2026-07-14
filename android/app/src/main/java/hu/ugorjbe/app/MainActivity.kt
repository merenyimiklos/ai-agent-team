package hu.ugorjbe.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import hu.ugorjbe.app.ui.UgorjBeApp
import hu.ugorjbe.app.ui.theme.UgorjBeTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UgorjBeTheme { UgorjBeApp() }
        }
    }
}
