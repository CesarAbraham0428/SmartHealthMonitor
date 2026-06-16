package mx.utng.cala.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

private val WearDarkColors = Colors(
    primary          = WearPrimary,
    onPrimary        = WearOnPrimary,
    secondary        = WearSecondary,
    onSecondary      = WearOnSecondary,
    background       = WearBackground,
    onBackground     = WearOnBackground,
    surface          = WearSurface,
    onSurface        = WearOnSurface,
    error            = WearError,
    onError          = WearOnError
)

@Composable
fun SmartHealthWearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = WearDarkColors,
        content = content
    )
}
