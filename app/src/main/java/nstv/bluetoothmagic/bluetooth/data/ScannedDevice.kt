package nstv.bluetoothmagic.bluetooth.data

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import androidx.bluetooth.BluetoothDevice

data class ScannedDevice(
    val deviceName: String,
    val deviceId: String,
    val deviceAddress: String,
)

@SuppressLint("MissingPermission")
fun ScanResult.toScannedDevice(): ScannedDevice {
    return ScannedDevice(
        deviceName = device.name ?: "Unknown",
        deviceId = device.uuids?.firstOrNull()?.uuid?.toString() ?: "Unknown",
        deviceAddress = device.address,
    )
}

fun androidx.bluetooth.ScanResult.toScannedDevice(): ScannedDevice {
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