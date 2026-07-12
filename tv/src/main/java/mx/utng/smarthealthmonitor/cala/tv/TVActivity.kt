package mx.utng.smarthealthmonitor.cala.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.tv.material3.MaterialTheme
import mx.utng.smarthealthmonitor.cala.tv.presentation.TvDetailScreen
import mx.utng.smarthealthmonitor.cala.tv.presentation.TvPlaybackScreen

/**
 * Tema de Compose para TV.
 */
@Composable
fun SmartHealthTvTheme(contenido: @Composable () -> Unit) {
    MaterialTheme {
        contenido()
    }
}

/**
 * Wrapper de compatibilidad para adaptar PantallaCatalogoTv con el nombre esperado en la guía.
 */
@Composable
fun TvCatalogScreen(onCardClick: (Int) -> Unit) {
    PantallaCatalogoTv(alSeleccionarLectura = onCardClick)
}

/**
 * Actividad principal de Android TV usando Jetpack Compose.
 */
class TVActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartHealthTvTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "catalog") {
                    composable("catalog") {
                        TvCatalogScreen(onCardClick = { lecturaId ->
                            navController.navigate("detail/$lecturaId")
                        })
                    }
                    composable(
                        route = "detail/{lecturaId}",
                        arguments = listOf(navArgument("lecturaId") { type = NavType.IntType })
                    ) { backStack ->
                        val id = backStack.arguments?.getInt("lecturaId") ?: return@composable
                        TvDetailScreen(lecturaId = id, navController = navController)
                    }
                    composable("playback") {
                        TvPlaybackScreen(navController = navController)
                    }
                }
            }
        }
    }
}
