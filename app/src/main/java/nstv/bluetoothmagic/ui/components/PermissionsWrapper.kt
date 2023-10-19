package nstv.bluetoothmagic.ui.components

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState


val defaultBluetoothPermissions = listOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.BLUETOOTH_SCAN,
    Manifest.permission.BLUETOOTH_ADVERTISE,
    Manifest.permission.BLUETOOTH_CONNECT,
)


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsWrapper(
    modifier: Modifier = Modifier,
    permissions: List<String> = defaultBluetoothPermissions,
    content: @Composable () -> Unit,
) {
    var errorText by remember {
        mutableStateOf("")
    }

    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    if (permissionState.allPermissionsGranted) {
        content()
    } else {
        Column(modifier.fillMaxSize()) {
            Text(
                text = "The following permissions are required for this app:",
                style = MaterialTheme.typography.headlineMedium
            )
            permissions.forEach {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                Text("Grant permission", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}