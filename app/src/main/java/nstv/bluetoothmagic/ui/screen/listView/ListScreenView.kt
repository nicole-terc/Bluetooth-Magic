package nstv.bluetoothmagic.ui.screen.listView

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nstv.bluetoothmagic.bluetooth.data.BluetoothAdapterState
import nstv.bluetoothmagic.bluetooth.data.ScannedDevice
import nstv.bluetoothmagic.ui.components.BluetoothDisabledOverlay
import nstv.bluetoothmagic.ui.components.PermissionsWrapper
import nstv.bluetoothmagic.ui.theme.Grid
import java.util.UUID

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
                    onBluetoothEnabled = viewModel::onBluetoothEnabled,
                    startAdvertising = viewModel::startAdvertising,
                    startServer = viewModel::startServer,
                    startScanning = viewModel::startScanning,
                    connectToServer = viewModel::connectToServer,
                    readCharacteristic = viewModel::readCharacteristic,
                    stopAdvertising = viewModel::stopAdvertising,
                    stopAllBluetoothAction = viewModel::stopAllBluetoothAction,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.stopAllBluetoothAction()
        }
    }
}

@Composable
fun ListScreenContent(
    uiState: ListScreenUiState.Loaded,
    onBluetoothEnabled: () -> Unit,
    startAdvertising: (fromServer: Boolean) -> Unit,
    startServer: () -> Unit,
    startScanning: () -> Unit,
    connectToServer: (Context, ScannedDevice) -> Unit,
    readCharacteristic: () -> Unit,
    stopAdvertising: (fromServer: Boolean) -> Unit,
    stopAllBluetoothAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val btState = uiState.bluetoothState
    val context = LocalContext.current

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
                        BluetoothDeviceItem(
                            item = item,
                            onClick = { connectToServer(context, item) })
                    }
                }
            }

            BluetoothAdapterState.Advertising -> Text(text = "Advertising")
            BluetoothAdapterState.Connecting -> Text(text = "Connecting")
            is BluetoothAdapterState.ServerStarted -> {
                Text(text = "Server Started")
                Spacer(modifier = Modifier.height(Grid.Three))
                Button(
                    onClick = {
                        if (btState.isAdvertising) {
                            stopAdvertising(true)
                        } else {
                            startAdvertising(true)
                        }
                    },
                    modifier = Modifier.padding(Grid.Two)
                ) {
                    Text(text = if (btState.isAdvertising) "Stop Advertising" else "Start Advertising")
                }
                Spacer(modifier = Modifier.height(Grid.Three))
                Text(text = "Connected Devices")
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(btState.connectedDevices) { item ->
                        BluetoothDeviceItem(item)
                    }
                }
            }

            BluetoothAdapterState.Enabled -> {
                Column(
                    Modifier
                        .width(IntrinsicSize.Max)
                        .padding(Grid.Two),
                    verticalArrangement = Arrangement.spacedBy(Grid.Three),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(
                        onClick = { startAdvertising(false) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Start Advertising",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    Button(
                        onClick = startServer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Start Server",
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
            is BluetoothAdapterState.Connected -> {
                Text(text = "Connected")
                Spacer(modifier = Modifier.height(Grid.Three))
                Button(onClick = readCharacteristic, modifier = Modifier.padding(Grid.Two)) {
                    Text(text = "Read Characteristic")
                }
                Spacer(modifier = Modifier.height(Grid.Three))
                Text(text = "Characteristics")
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(btState.characteristics) { item ->
                        BluetoothCharacteristic(item)
                    }
                }
            }
        }

        if (btState != BluetoothAdapterState.Disabled
            && btState != BluetoothAdapterState.Loading
            && btState != BluetoothAdapterState.Enabled
        ) {
            Button(
                onClick = stopAllBluetoothAction,
                modifier = Modifier.padding(Grid.Two)
            ) {
                Text(text = "Stop Everything")
            }
        }
    }
}

@Composable
fun BluetoothDeviceItem(
    item: ScannedDevice,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier
            .padding(Grid.Half)
            .clickable { onClick() }) {
        Text(text = item.deviceName)
        Text(text = item.deviceAddress)
        Text(text = item.deviceId)
        HorizontalDivider(Modifier.height(Grid.Single))
    }
}

@Composable
fun BluetoothCharacteristic(
    item: Pair<UUID, String>,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(text = "id:  ${item.first}")
        Text(text = "value:  ${item.second}")
        HorizontalDivider(Modifier.height(Grid.Single))
    }
}