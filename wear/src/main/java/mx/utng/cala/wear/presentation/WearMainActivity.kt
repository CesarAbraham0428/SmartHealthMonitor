package mx.utng.cala.wear.presentation

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales

import kotlinx.coroutines.launch
import mx.utng.cala.wear.R
import mx.utng.cala.wear.presentation.data.models.HealthDataService
import mx.utng.cala.wear.presentation.theme.SmartHealthWearTheme

class MainActivity : ComponentActivity() {

    private val permisosLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permisos ->
            val permisoSensores = permisos[Manifest.permission.BODY_SENSORS] == true
            val permisoActividad = permisos[Manifest.permission.ACTIVITY_RECOGNITION] == true

            if (permisoSensores && permisoActividad) {
                registrarHealthServices()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permisosLauncher.launch(
            arrayOf(
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        )

        setContent {
            SmartHealthWearTheme {
                WearApp("Android")
            }
        }
    }

    private fun registrarHealthServices() {
        lifecycleScope.launch {
            HealthDataService.registrar(applicationContext)
        }
    }
}

@Composable
fun WearApp(greetingName: String) {
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            item {
                ListHeader {
                    Text(text = stringResource(R.string.hello_world, greetingName))
                }
            }

            item {
                Chip(
                    onClick = { /* TODO */ },
                    label = { Text("Button A") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ChipDefaults.primaryChipColors()
                )
            }

            item {
                Chip(
                    onClick = { /* TODO */ },
                    label = { Text("Button B") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ChipDefaults.primaryChipColors()
                )
            }

            item {
                Chip(
                    onClick = { /* TODO */ },
                    label = { Text("Button C") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ChipDefaults.primaryChipColors()
                )
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun DefaultPreview() {
    SmartHealthWearTheme {
        WearApp("Preview Android")
    }
}
