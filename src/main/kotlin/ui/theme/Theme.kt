package ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val GreenPrimary = Color(0xFF2E7D32)
private val GreenDark = Color(0xFF1B5E20)
private val GreenContainer = Color(0xFFC8E6C9)
private val GreenSecondary = Color(0xFF558B2F)
private val GreenSecondaryContainer = Color(0xFFDCEDC8)
private val GreenBackground = Color(0xFFF5FAF6)
private val GreenSurfaceVariant = Color(0xFFE7F0E9)
private val GreenOutline = Color(0xFF8CA795)

private val TextPrimary = Color(0xFF1F2933)
private val TextSecondary = Color(0xFF52605A)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenContainer,
    onPrimaryContainer = GreenDark,
    secondary = GreenSecondary,
    onSecondary = Color.White,
    secondaryContainer = GreenSecondaryContainer,
    onSecondaryContainer = GreenDark,
    tertiary = Color(0xFF00695C),
    background = GreenBackground,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = GreenSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = GreenOutline,
    outlineVariant = Color(0xFFC8D8CD),
    error = Color(0xFFC62828),
    onError = Color.White,
    errorContainer = Color(0xFFFDECEA),
    onErrorContainer = Color(0xFF8E1B1B)
)

private val BaseTypography = Typography()

private val MemoraTypography = Typography(
    displaySmall = BaseTypography.displaySmall.copy(fontWeight = FontWeight.Bold),
    headlineSmall = BaseTypography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = BaseTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = BaseTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    labelLarge = BaseTypography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
)

private val MemoraShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

/** Cores de apoio para estados que não fazem parte do ColorScheme padrão. */
object MemoraColors {
    val Success = Color(0xFF2E7D32)
    val Warning = Color(0xFFB26A00)
    val Danger = Color(0xFFC62828)
    val Neutral = Color(0xFF6B7280)
}

@Composable
fun MemoraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = MemoraTypography,
        shapes = MemoraShapes,
        content = content
    )
}
