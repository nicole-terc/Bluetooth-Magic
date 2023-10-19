package nstv.bluetoothmagic.data.local.scannedDevices

data class ScannedDevice(
    val name: String,
    val signalStrength: Int,
    val lastConnected: Long,
)