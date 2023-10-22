package nstv.bluetoothmagic.bluetooth.data

import android.bluetooth.BluetoothAdapter
import java.util.UUID

sealed interface BluetoothAdapterState {
    data object Enabled : BluetoothAdapterState
    data object Disabled : BluetoothAdapterState
    data class Connected(
        val characteristics: List<Pair<UUID, String>> = emptyList(),
    ) : BluetoothAdapterState

    data object Disconnected : BluetoothAdapterState
    data object Advertising : BluetoothAdapterState
    data class ServerStarted(
        val isAdvertising: Boolean,
        val connectedDevices: List<ScannedDevice> = emptyList()
    ) : BluetoothAdapterState

    data object Connecting : BluetoothAdapterState
    data class Scanning(val scannedDevices: List<ScannedDevice>) : BluetoothAdapterState
    data object Loading : BluetoothAdapterState
    data class Error(val message: String) : BluetoothAdapterState
}

fun Int.toBluetoothAdapterState(): BluetoothAdapterState {
    return when (this) {
        BluetoothAdapter.STATE_ON -> BluetoothAdapterState.Enabled
        BluetoothAdapter.STATE_OFF -> BluetoothAdapterState.Disabled
        else -> BluetoothAdapterState.Loading
    }
}

