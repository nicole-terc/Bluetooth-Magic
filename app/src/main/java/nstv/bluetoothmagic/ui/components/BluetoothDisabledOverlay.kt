package nstv.bluetoothmagic.ui.components

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nstv.bluetoothmagic.ui.theme.Grid

@Composable
fun BluetoothDisabledOverlay(
    modifier: Modifier = Modifier,
    onBluetoothEnabled: () -> Unit,
) {
    val result =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) onBluetoothEnabled()
        }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Oh no, Bluetooth is disabled!",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(Grid.Two))
        Button(
            onClick = {
                result.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            },
        ) {
            Text(text = "Click to enable :D", style = MaterialTheme.typography.headlineSmall)
        }
    }
}