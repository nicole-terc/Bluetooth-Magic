package nstv.bluetoothmagic.bluetooth.data

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.bluetooth.BluetoothDevice

data class ScannedDevice(
    val deviceName: String,
    val deviceId: String,
    val deviceAddress: String,
)

@SuppressLint("MissingPermission")
fun ScanResult.toScannedDevice(): ScannedDevice = this.device.toScannedDevice()

@SuppressLint("MissingPermission")
fun BluetoothDevice.toScannedDevice(): ScannedDevice {
    return ScannedDevice(
        deviceName = name ?: "Unknown",
        deviceId = uuids?.firstOrNull()?.uuid?.toString() ?: "Unknown",
        deviceAddress = address,
    )
}

fun androidx.bluetooth.ScanResult.toScannedDevice(): ScannedDevice {
    return ScannedDevice(
        deviceName = device.name ?: "Unknown",
        deviceId = device.id.toString(),
        deviceAddress = deviceAddress.address,
    )
}

fun androidx.bluetooth.BluetoothDevice.toScannedDevice(): ScannedDevice {
    return ScannedDevice(
        deviceName = name ?: "Unknown",
        deviceId = id.toString(),
        deviceAddress = "Unknown",
    )
}