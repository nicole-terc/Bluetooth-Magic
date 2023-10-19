package nstv.bluetoothmagic.bluetooth


import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import androidx.bluetooth.AdvertiseParams
import androidx.bluetooth.BluetoothLe
import androidx.bluetooth.ScanFilter
import kotlinx.coroutines.Job
import java.util.UUID
import javax.inject.Inject

class BluetoothLeHandler @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    private val bluetoothLe: BluetoothLe,
) {
    private val serviceUuid: UUID = UUID.fromString("879d9eeb-0b6e-4d57-8473-03ce06a62067")
    private val advertisingJob = Job()
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter.isEnabled

//    val scannedDevices = mutableListOf<ScanResult>()

    @SuppressLint("MissingPermission")
    fun scan(scanFilters: List<ScanFilter> = emptyList()) =
        bluetoothLe.scan(scanFilters + ScanFilter(serviceUuid = serviceUuid))

    @SuppressLint("MissingPermission")
    suspend fun startAdvertising() {
        bluetoothLe.advertise(
            AdvertiseParams(
                shouldIncludeDeviceName = true,
                isConnectable = true,
                isDiscoverable = true,
                serviceUuids = listOf(serviceUuid)
            ),
        )
    }
}