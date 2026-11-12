package app.sandy.improv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.sandy.improv.ui.ImprovNav
import app.sandy.improv.ui.PaperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PaperTheme {
                ImprovNav()
            }
        }
    }
}
