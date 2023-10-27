package nstv.bluetoothmagic.ui.screen.garden

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nstv.bluetoothmagic.bluetooth.data.BluetoothAdapterState
import nstv.bluetoothmagic.bluetooth.data.BondState
import nstv.bluetoothmagic.bluetooth.data.ScannedDevice
import nstv.bluetoothmagic.data.local.Ingredient
import nstv.bluetoothmagic.data.local.IngredientCombinations
import nstv.bluetoothmagic.sheep.LoadingSheep
import nstv.bluetoothmagic.ui.components.BluetoothCharacteristic
import nstv.bluetoothmagic.ui.components.BluetoothDeviceItem
import nstv.bluetoothmagic.ui.components.BluetoothDisabledOverlay
import nstv.bluetoothmagic.ui.theme.Grid
import java.util.UUID

@Composable
fun BluetoothActionsOverlay(
    bluetoothState: BluetoothAdapterState,
    ingredients: List<Ingredient>,
    onBluetoothEnabled: () -> Unit,
    startAdvertising: (fromServer: Boolean) -> Unit,
    startServer: () -> Unit,
    startScanning: () -> Unit,
    connectToServer: (Context, ScannedDevice) -> Unit,
    stopAdvertising: (fromServer: Boolean) -> Unit,
    stopAllBluetoothAction: () -> Unit,
    readCharacteristic: (Context) -> Unit,
    writeCharacteristic: (Context) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler {
        stopAllBluetoothAction()
    }

    Column(
        modifier = modifier.padding(Grid.One),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (bluetoothState) {
            BluetoothAdapterState.Disabled -> {
                BluetoothDisabledOverlay(
                    modifier = Modifier.fillMaxSize(),
                    onBluetoothEnabled = onBluetoothEnabled
                )
            }

            BluetoothAdapterState.Advertising -> LoadingWithText(text = "Advertising")
            BluetoothAdapterState.Connecting -> LoadingWithText(text = "Connecting")
            BluetoothAdapterState.Disconnected -> Text(text = "Disconnected")
            is BluetoothAdapterState.Error -> Text(text = bluetoothState.message)
            BluetoothAdapterState.Loading -> LoadingSheep()
            BluetoothAdapterState.Enabled -> Enabled(
                startAdvertising = startAdvertising,
                startServer = startServer,
                startScanning = startScanning,
                modifier = Modifier.fillMaxSize(),
            )

            is BluetoothAdapterState.ServerStarted -> ServerStarted(
                serverStarted = bluetoothState,
                ingredients = ingredients,
                startAdvertising = startAdvertising,
                stopAdvertising = stopAdvertising,
            )

            is BluetoothAdapterState.Scanning -> Scanning(
                scanning = bluetoothState,
                connectToServer = connectToServer,
            )

            is BluetoothAdapterState.Connected -> Connected(
                connected = bluetoothState,
                ingredients = ingredients,
                readCharacteristic = readCharacteristic,
                writeCharacteristic = writeCharacteristic,
            )
        }

        if (bluetoothState != BluetoothAdapterState.Disabled
            && bluetoothState != BluetoothAdapterState.Loading
            && bluetoothState != BluetoothAdapterState.Enabled
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
fun Enabled(
    startAdvertising: (fromServer: Boolean) -> Unit,
    startServer: () -> Unit,
    startScanning: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
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


@Composable
fun ColumnScope.ServerStarted(
    serverStarted: BluetoothAdapterState.ServerStarted,
    ingredients: List<Ingredient>,
    startAdvertising: (fromServer: Boolean) -> Unit,
    stopAdvertising: (fromServer: Boolean) -> Unit,
) {
    val context = LocalContext.current
    val writeIngredient =
        ingredients.find { it.id == serverStarted.updatedCharacteristic?.second?.toIntOrNull() }
            ?: IngredientCombinations.unknown

    Text(
        text = "Server Started",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier
            .padding(Grid.Two)
            .align(Alignment.CenterHorizontally)
    )
    Button(
        onClick = {
            if (serverStarted.isAdvertising) {
                stopAdvertising(true)
            } else {
                startAdvertising(true)
            }
        },
        modifier = Modifier.padding(Grid.Two)
    ) {
        Text(text = if (serverStarted.isAdvertising) "Stop Advertising" else "Start Advertising")
    }
    Spacer(modifier = Modifier.height(Grid.Three))
    if (serverStarted.connectedDevices.isEmpty()) {
        LoadingSheep(
            modifier = Modifier
                .size(200.dp)
                .padding(Grid.One),
            spinning = serverStarted.isAdvertising,
        )
    } else {
        Row(
            Modifier
                .width(150.dp)
                .padding(Grid.One),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "⬇ Get!",
                    Modifier.align(CenterHorizontally),
                    style = MaterialTheme.typography.headlineSmall
                )
                IngredientItem(
                    ingredient = writeIngredient,
                    onIngredientClick = {
                        Toast.makeText(context, "WOOP!", Toast.LENGTH_SHORT).show()
                    })

            }
        }
    }
    Text(text = "Connected Devices")
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f)
    ) {
        items(serverStarted.connectedDevices) { item ->
            BluetoothDeviceItem(item)
        }
    }

}


@Composable
fun ColumnScope.Scanning(
    scanning: BluetoothAdapterState.Scanning,
    connectToServer: (Context, ScannedDevice) -> Unit,
) {
    val context = LocalContext.current
    Text(
        text = "Scanning",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier
            .padding(Grid.Two)
            .align(Alignment.CenterHorizontally)
    )
    if (scanning.scannedDevices.isEmpty()) {
        LoadingSheep(
            modifier = Modifier
                .size(200.dp)
                .padding(Grid.One)
        )
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f)
    ) {
        items(scanning.scannedDevices) { item ->
            BluetoothDeviceItem(
                item = item,
                onClick = { connectToServer(context, item) })
        }
    }
}

@Composable
fun ColumnScope.Connected(
    connected: BluetoothAdapterState.Connected,
    ingredients: List<Ingredient>,
    readCharacteristic: (Context) -> Unit,
    writeCharacteristic: (Context) -> Unit,
) {
    val context = LocalContext.current
    val writeIngredient =
        ingredients.find { it.isMainIngredient } ?: IngredientCombinations.unknown
    val readIngredient =
        ingredients.find { it.id == connected.updatedCharacteristic?.second?.toIntOrNull() }
            ?: IngredientCombinations.unknown

    Text(
        text = "Connected",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier
            .padding(Grid.Two)
            .align(Alignment.CenterHorizontally)
    )
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Grid.Three, vertical = Grid.Two),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "⬇ Get!",
                Modifier.align(CenterHorizontally),
                style = MaterialTheme.typography.headlineSmall
            )
            IngredientItem(
                ingredient = readIngredient,
                onIngredientClick = { readCharacteristic(context) })

        }
        Spacer(modifier = Modifier.width(Grid.Three))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "⬆ Send!",
                Modifier.align(CenterHorizontally),
                style = MaterialTheme.typography.headlineSmall
            )
            IngredientItem(
                ingredient = writeIngredient,
                onIngredientClick = { writeCharacteristic(context) })
        }
    }
    Spacer(modifier = Modifier.height(Grid.Three))
    Text(text = "Characteristics")
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .weight(1f)
    ) {
        items(connected.characteristics) { item ->
            BluetoothCharacteristic(item)
        }
    }
}

@Composable
fun ColumnScope.LoadingWithText(
    text: String,
) {
    Text(text = text, style = MaterialTheme.typography.headlineMedium)
    LoadingSheep(
        modifier = Modifier
            .size(200.dp)
            .padding(Grid.One)
    )

}


/**
 * PREVIEWS ------------------------
 */

@Composable
private fun PreviewBluetoothActionsOverlay(state: BluetoothAdapterState) {
    BluetoothActionsOverlay(
        bluetoothState = state,
        onBluetoothEnabled = {},
        startAdvertising = { },
        startServer = { },
        startScanning = { },
        connectToServer = { _, _ -> },
        stopAdvertising = { },
        stopAllBluetoothAction = { },
        readCharacteristic = { },
        writeCharacteristic = { },
        ingredients = IngredientCombinations.list
    )
}

@Preview
@Composable
private fun PreviewEnabled() {
    PreviewBluetoothActionsOverlay(BluetoothAdapterState.Enabled)
}

@Preview
@Composable
private fun PreviewServerStarted() {
    PreviewBluetoothActionsOverlay(
        BluetoothAdapterState.ServerStarted(
            isAdvertising = true,
            connectedDevices = listOf(
                ScannedDevice(
                    deviceName = "Test",
                    deviceAddress = "Test",
                    deviceId = "Test",
                    bondState = BondState.BONDED,
                )
            )
        )
    )
}

@Preview
@Composable
private fun PreviewScanning() {
    PreviewBluetoothActionsOverlay(
        BluetoothAdapterState.Scanning(
            scannedDevices = listOf(
                ScannedDevice(
                    deviceName = "Test",
                    deviceAddress = "Test",
                    deviceId = "Test",
                    bondState = BondState.BONDED,
                )
            )
        )
    )
}

@Preview
@Composable
private fun PreviewConnected() {
    PreviewBluetoothActionsOverlay(
        BluetoothAdapterState.Connected(
            characteristics = listOf(
                UUID.randomUUID() to "TEST",
            )
        )
    )
}
