package nstv.bluetoothmagic.ui.screen.listView

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nstv.bluetoothmagic.bluetooth.BluetoothAdapterState
import nstv.bluetoothmagic.ui.components.BluetoothDisabledOverlay
import nstv.bluetoothmagic.ui.components.PermissionsWrapper
import nstv.bluetoothmagic.ui.theme.Grid

@Composable
fun ListScreenView(
    modifier: Modifier = Modifier,
    viewModel: ListScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PermissionsWrapper(modifier.fillMaxSize()) {
        when (uiState) {
            ListScreenUiState.Loading -> Text("Loading here")
            is ListScreenUiState.Error -> Text(text = (uiState as ListScreenUiState.Error).message)
            is ListScreenUiState.Loaded -> {
                ListScreenContent(
                    uiState = uiState as ListScreenUiState.Loaded,
                    startScanning = viewModel::startScanning,
                    startAdvertising = viewModel::startAdvertising,
                    onBluetoothEnabled = viewModel::onBluetoothEnabled,
                    stopBluetoothAction = viewModel::stopBluetoothAction,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun ListScreenContent(
    uiState: ListScreenUiState.Loaded,
    startScanning: () -> Unit,
    startAdvertising: () -> Unit,
    onBluetoothEnabled: () -> Unit,
    stopBluetoothAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val btState = uiState.bluetoothState

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        when (btState) {
            BluetoothAdapterState.Disabled -> {
                BluetoothDisabledOverlay(
                    modifier = Modifier.fillMaxSize(),
                    onBluetoothEnabled = onBluetoothEnabled
                )
            }

            is BluetoothAdapterState.Scanning -> {
                Text(
                    text = "Scanning",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .padding(Grid.Two)
                        .align(Alignment.CenterHorizontally)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(btState.scannedDevices) { item ->
                        Column(Modifier.padding(Grid.Half)) {
                            Text(text = item.device.name.toString())
                            Text(text = item.device.bondState.toString())
                            Text(text = item.timestampNanos.toString())
                            HorizontalDivider(Modifier.height(Grid.Single))
                        }
                    }
                }
            }

            BluetoothAdapterState.Advertising -> Text(text = "Advertising")
            BluetoothAdapterState.Enabled -> {
                Column(
                    Modifier
                        .width(IntrinsicSize.Max)
                        .padding(Grid.Two),
                    verticalArrangement = Arrangement.spacedBy(Grid.Three),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(
                        onClick = startAdvertising,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Start Advertising",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    Button(
                        onClick = startScanning,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Start Scanning",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }

            is BluetoothAdapterState.Error -> Text(
                text = btState.message,
                style = MaterialTheme.typography.labelMedium
            )

            BluetoothAdapterState.Loading -> CircularProgressIndicator()
            BluetoothAdapterState.Disconnected -> Text(text = "Disconnected")
            is BluetoothAdapterState.Connected -> Text(text = "Connected")
        }

        if (btState != BluetoothAdapterState.Disabled
            && btState != BluetoothAdapterState.Loading
            && btState != BluetoothAdapterState.Enabled
        ) {
            Button(
                onClick = stopBluetoothAction,
                modifier = Modifier.padding(Grid.Two)
            ) {
                Text(text = "Stop")
            }
        }
    }
}