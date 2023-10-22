package nstv.bluetoothmagic.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import androidx.bluetooth.BluetoothDevice
import androidx.bluetooth.ScanResult
import android.bluetooth.le.ScanResult as OldResult
import java.util.UUID

data class ScannedDevice(
    val deviceName: String,
    val deviceId: String,
    val deviceAddress: String,
)

@SuppressLint("MissingPermission")
fun OldResult.toScannedDevice(): ScannedDevice {
    return ScannedDevice(
        deviceName = device.name ?: "Unknown",
        deviceId = device.uuids?.firstOrNull()?.uuid?.toString() ?: "Unknown",
        deviceAddress = device.address,
    )
}

fun ScanResult.toScannedDevice(): ScannedDevice {
    return ScannedDevice(
        deviceName = device.name ?: "Unknown",
        deviceId = device.id.toString(),
        deviceAddress = deviceAddress.address,
    )
}

fun BluetoothDevice.toScannedDevice(): ScannedDevice {
    return ScannedDevice(
        deviceName = name ?: "Unknown",
        deviceId = id.toString(),
        deviceAddress = "Unknown",
    )
}

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

