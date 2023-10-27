package nstv.bluetoothmagic.bluetooth.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice.BOND_BONDED
import android.bluetooth.BluetoothDevice.BOND_BONDING
import android.bluetooth.BluetoothDevice.BOND_NONE
import android.bluetooth.le.ScanResult
import androidx.bluetooth.BluetoothDevice

data class ScannedDevice(
    val deviceName: String,
    val deviceId: String,
    val deviceAddress: String,
    val bondState: BondState,
)

enum class BondState {
    BONDED, BONDING, NONE, UNKNOWN
}

@SuppressLint("MissingPermission")
fun ScanResult.toScannedDevice(): ScannedDevice {
    return ScannedDevice(
        deviceName = device.name ?: "Unknown",
        deviceId = device.uuids?.firstOrNull()?.uuid?.toString() ?: "Unknown",
        deviceAddress = device.address,
        bondState = device.bondState.toBondState()
    )
}

fun androidx.bluetooth.ScanResult.toScannedDevice(): ScannedDevice {
    return ScannedDevice(
        deviceName = device.name ?: "Unknown",
        deviceId = device.id.toString(),
        deviceAddress = deviceAddress.address,
        bondState = device.bondState.toBondState()
    )
}

fun BluetoothDevice.toScannedDevice(): ScannedDevice {
    return ScannedDevice(
        deviceName = name ?: "Unknown",
        deviceId = id.toString(),
        deviceAddress = "Unknown",
        bondState = this.bondState.toBondState()
    )
}

fun Int.toBondState(): BondState = when (this) {
    BOND_BONDED -> BondState.BONDED
    BOND_BONDING -> BondState.BONDING
    BOND_NONE -> BondState.NONE
    else -> BondState.UNKNOWN
}