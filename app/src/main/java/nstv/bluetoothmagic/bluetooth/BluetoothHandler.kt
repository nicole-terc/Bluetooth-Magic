package nstv.bluetoothmagic.bluetooth

import androidx.bluetooth.ScanFilter

interface BluetoothHandler {
    fun isBluetoothEnabled(): Boolean
    fun scan(scanFilters: List<ScanFilter> = emptyList())
    fun stopScan()
}