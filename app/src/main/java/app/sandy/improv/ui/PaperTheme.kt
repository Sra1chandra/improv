package app.sandy.improv.ui

import android.graphics.BitmapShader
import android.graphics.Shader
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.Role
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
    val paperFace = Color(0xFFFBF6EA)
    val rule = Color(0xFFD7C4A4)
    val felt = Color(0xFF2A241C)
    val you = Color(0xFF2A5F9E)
    val marker = Color(0xFFC45C3E)
    val pencil = Color(0xFF6B5E4E)
    val danger = Color(0xFFB42318)
    val umber = Color(0x385A371E)
    val highlight = Color(0x66F0DC6B)
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
fun PaperSheet(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // ponytail: tilt retired. Crooked sheet made the desk look broken. Square pad.
    val tooth = rememberTooth()
    Box(
        modifier
            .shadow(3.dp, ambientColor = Ink.umber, spotColor = Ink.umber)
            .clip(RectangleShape)
            .drawBehind {
                drawRect(Ink.paper)
                drawRect(
                    brush = Brush.linearGradient(
                        0f to Color(0x66FFF8EC),
                        1f to Color.Transparent,
                        start = Offset.Zero,
                        end = Offset(size.width * 0.35f, size.height * 0.45f),
                    ),
                )
                drawRect(tooth)
                val step = 28.dp.toPx()
                var y = 56.dp.toPx()
                while (y < size.height) {
                    drawLine(Ink.rule.copy(alpha = 0.45f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    y += step
                }
            },
    ) {
        content()
    }
}

/** Terracotta sits 2dp inside the ink outline. Not a Material pill. */
@Composable
fun Stamp(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val outline = RoundedCornerShape(4.dp)
    Box(
        modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .border(1.dp, Ink.felt, outline)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(2.dp)
            .background(
                if (enabled) Ink.marker else Ink.marker.copy(alpha = 0.4f),
                RoundedCornerShape(2.dp),
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Ink.felt, style = MaterialTheme.typography.labelLarge)
    }
}

// ponytail: 16px procedural tooth, not a scanned sheet. Upgrade = vendor a real tile.
@Composable
private fun rememberTooth(): Brush = remember {
    val tile = ImageBitmap(16, 16)
    val canvas = Canvas(tile)
    val paint = Paint()
    paint.color = Color(0x1A5A371E)
    canvas.drawCircle(Offset(3f, 5f), 0.7f, paint)
    canvas.drawCircle(Offset(12f, 11f), 0.6f, paint)
    paint.color = Color(0x22FFFFFF)
    canvas.drawCircle(Offset(8f, 2f), 0.5f, paint)
    canvas.drawCircle(Offset(1f, 14f), 0.45f, paint)
    ShaderBrush(
        BitmapShader(
            tile.asAndroidBitmap(),
            Shader.TileMode.REPEAT,
            Shader.TileMode.REPEAT,
        ),
    )
}
