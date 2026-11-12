package app.sandy.improv.ui

import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.sandy.improv.R

// Kevin Sans, public-domain Hershey, from
// https://kevintngo.github.io/looseleaf/fonts/ KevinSans-Regular.woff2 and Bold.

object Ink {
    val desk = Color(0xFF9DB8AE)
    val paper = Color(0xFFF4EAD4)
    val felt = Color(0xFF2A241C)
    val you = Color(0xFF2A5F9E)
    val marker = Color(0xFFC45C3E)
    val pencil = Color(0xFF6B5E4E)
    val danger = Color(0xFFB42318)
    val umber = Color(0x385A371E)
}

val KevinSans = FontFamily(
    Font(R.font.kevin_sans_regular, FontWeight.Normal),
    Font(R.font.kevin_sans_bold, FontWeight.Bold),
)

private val PaperScheme = lightColorScheme(
    primary = Ink.marker,
    onPrimary = Ink.paper,
    primaryContainer = Ink.paper,
    onPrimaryContainer = Ink.felt,
    inversePrimary = Ink.marker,
    secondary = Ink.you,
    onSecondary = Ink.paper,
    secondaryContainer = Ink.paper,
    onSecondaryContainer = Ink.felt,
    tertiary = Ink.pencil,
    onTertiary = Ink.paper,
    tertiaryContainer = Ink.paper,
    onTertiaryContainer = Ink.felt,
    background = Ink.desk,
    onBackground = Ink.felt,
    surface = Ink.paper,
    onSurface = Ink.felt,
    surfaceVariant = Ink.paper,
    onSurfaceVariant = Ink.pencil,
    surfaceTint = Ink.marker,
    error = Ink.danger,
    onError = Ink.paper,
    errorContainer = Ink.paper,
    onErrorContainer = Ink.danger,
    outline = Ink.felt,
    outlineVariant = Ink.pencil,
    scrim = Ink.felt,
)

private fun TextStyle.inked(weight: FontWeight = FontWeight.Normal) =
    copy(fontFamily = KevinSans, fontWeight = weight, color = Ink.felt)

private val PaperType = Typography().run {
    copy(
        displayLarge = displayLarge.inked(),
        displayMedium = displayMedium.inked(),
        displaySmall = displaySmall.inked(),
        headlineLarge = headlineLarge.inked(),
        headlineMedium = headlineMedium.inked(),
        headlineSmall = headlineSmall.inked(),
        titleLarge = titleLarge.inked(FontWeight.Bold),
        titleMedium = titleMedium.inked(FontWeight.Bold),
        titleSmall = titleSmall.inked(FontWeight.Bold),
        bodyLarge = bodyLarge.inked(),
        bodyMedium = bodyMedium.inked(),
        bodySmall = bodySmall.inked(),
        labelLarge = labelLarge.inked(FontWeight.Bold),
        labelMedium = labelMedium.inked(),
        labelSmall = labelSmall.inked(),
    )
}

@Composable
fun PaperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PaperScheme,
        typography = PaperType,
        content = content,
    )
}

@Composable
fun reduceMotion(): Boolean {
    val scale = Settings.Global.getFloat(
        LocalContext.current.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    )
    return scale == 0f
}

@Composable
fun PaperSheet(
    modifier: Modifier = Modifier,
    tilt: Boolean = true,
    content: @Composable () -> Unit,
) {
    val degrees = if (tilt && !reduceMotion()) 1f else 0f
    Box(
        modifier
            .graphicsLayer { rotationZ = degrees }
            .shadow(2.dp, ambientColor = Ink.umber, spotColor = Ink.umber)
            .background(Ink.paper),
    ) {
        content()
    }
}

/** Terracotta fill, ink outline. Pair with [stampBorder]. */
@Composable
fun stampButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
    containerColor = Ink.marker,
    contentColor = Ink.felt,
)

fun stampBorder(): BorderStroke = BorderStroke(1.dp, Ink.felt)
