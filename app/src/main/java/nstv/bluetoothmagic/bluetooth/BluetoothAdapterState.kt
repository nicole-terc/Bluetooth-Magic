package nstv.bluetoothmagic.bluetooth

import android.bluetooth.BluetoothAdapter
import androidx.bluetooth.BluetoothDevice
import androidx.bluetooth.ScanResult
import java.util.UUID

sealed interface BluetoothAdapterState {
    data object Enabled : BluetoothAdapterState
    data object Disabled : BluetoothAdapterState
    data class Connected(
        val characteristics: List<Pair<UUID, String>> = emptyList(),
        val connectedDevices: List<BluetoothDevice> = emptyList(),
    ) : BluetoothAdapterState

    data object Disconnected : BluetoothAdapterState
    data object Advertising : BluetoothAdapterState
    data class Scanning(val scannedDevices: List<ScanResult>) : BluetoothAdapterState
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
