package nstv.bluetoothmagic.bluetooth


import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.bluetooth.AdvertiseParams
import androidx.bluetooth.BluetoothDevice
import androidx.bluetooth.BluetoothLe
import androidx.bluetooth.BluetoothLe.Companion.ADVERTISE_STARTED
import androidx.bluetooth.ScanFilter
import androidx.bluetooth.ScanResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nstv.bluetoothmagic.bluetooth.GardenService.serviceUUID
import nstv.bluetoothmagic.di.IoDispatcher
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val AdvertiseTimeout = 60000

@Singleton
class BluetoothLeHandler @Inject constructor(
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
    private val bluetoothLe: BluetoothLe,
    private val bluetoothStateRepository: BluetoothStateRepository,
) {

    // Added here for quickness, don't do this :)
    private var scope = CoroutineScope(SupervisorJob())
    private var advertiseScope = CoroutineScope(SupervisorJob())
    private var isAdvertising: Boolean = false
    private var scannedDevices: List<ScanResult> = emptyList()
    private var connectedDevices: MutableList<BluetoothDevice> = mutableListOf()

    fun bluetoothState() = bluetoothStateRepository.bluetoothAdapterState

    fun bluetoothEnabled() {
        bluetoothStateRepository.updateBluetoothAdapterState(BluetoothAdapterState.Enabled)
    }

    fun getAdvertiseParams() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AdvertiseParams(
                shouldIncludeDeviceName = true,
                isConnectable = true,
                isDiscoverable = true,
                serviceUuids = listOf(serviceUUID),
                durationMillis = AdvertiseTimeout,
            )
        } else {
            // Devices running Android 12 and below throw ADVERTISE_FAILED_DATA_TOO_LARGE error if more parameters are set
            AdvertiseParams(
                shouldIncludeDeviceName = false,
                isConnectable = true,
                serviceUuids = listOf(serviceUUID),
                durationMillis = AdvertiseTimeout,
            )
        }


    @SuppressLint("MissingPermission")
    suspend fun startAdvertising(fromServer: Boolean) {
        isAdvertising = true
        val timeout: Int = if (fromServer) 0 else AdvertiseTimeout
        scope.launch {
            advertiseScope.launch(coroutineDispatcher) {
                bluetoothLe.advertise(getAdvertiseParams()) { advertiseResult ->
                    when (advertiseResult) {
                        ADVERTISE_STARTED -> {
                            bluetoothStateRepository.updateBluetoothAdapterState(
                                if (fromServer) {
                                    BluetoothAdapterState.ServerStarted(true)
                                } else {
                                    BluetoothAdapterState.Advertising
                                }
                            )
                            if (timeout != 0) {
                                launch {
                                    delay(timeout.toLong())
                                    stopAdvertising(fromServer)
                                }
                            }
                        }

                        else -> {
                            isAdvertising = false
                            Log.e("BluetoothLeHandler", "Error advertising: $advertiseResult")
                            bluetoothStateRepository.updateBluetoothAdapterState(
                                BluetoothAdapterState.Error(
                                    "$advertiseResult: Error advertising"
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    suspend fun startServer() {
        bluetoothStateRepository.updateBluetoothAdapterState(BluetoothAdapterState.Loading)
        scope.launch(coroutineDispatcher) {
            val server = bluetoothLe.openGattServer(
                services = listOf(GardenService.getGattService())
            ) {
                bluetoothStateRepository.updateBluetoothAdapterState(
                    BluetoothAdapterState.ServerStarted(
                        false
                    )
                )
                this.connectRequests.collect { request ->
                    request.accept {
                        connectedDevices.add(request.device)
                        bluetoothStateRepository.updateBluetoothAdapterState(
                            BluetoothAdapterState.ServerStarted(
                                isAdvertising = isAdvertising,
                                connectedDevices = connectedDevices
                            )
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun scan(scanFilters: List<ScanFilter> = emptyList()) {
        scannedDevices = emptyList()
        bluetoothStateRepository.updateBluetoothAdapterState(
            BluetoothAdapterState.Scanning(scannedDevices)
        )
        scope.launch(coroutineDispatcher) {
            bluetoothLe.scan(scanFilters + ScanFilter(serviceUuid = serviceUUID))
                .collect { scanResult ->
                    scannedDevices = (scannedDevices + scanResult).distinctBy { it.device.id }
                    bluetoothStateRepository.updateBluetoothAdapterState(
                        BluetoothAdapterState.Scanning(
                            scannedDevices
                        )
                    )
                }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun connectToServer(scanResult: ScanResult) {
        bluetoothStateRepository.updateBluetoothAdapterState(BluetoothAdapterState.Connecting)
        scope.launch(coroutineDispatcher) {
            bluetoothLe.connectGatt(scanResult.device) {
                val characteristics: List<Pair<UUID, String>> =
                    getService(serviceUUID)?.characteristics?.map {
                        it.uuid to it.properties.toString()
                    } ?: emptyList()


                bluetoothStateRepository.updateBluetoothAdapterState(
                    BluetoothAdapterState.Connected(
                        characteristics = characteristics,
                    )
                )
            }
        }
    }

    fun stopAdvertising(fromServer: Boolean) {
        isAdvertising = false
        advertiseScope.cancel()
        advertiseScope = CoroutineScope(SupervisorJob())
        if (fromServer) {
            bluetoothStateRepository.updateBluetoothAdapterState(
                BluetoothAdapterState.ServerStarted(
                    false
                )
            )
        } else {
            bluetoothStateRepository.updateBluetoothAdapterStateToCurrentState()
        }
    }

    fun stopEverything() {
        scope.cancel()
        scope = CoroutineScope(SupervisorJob())
        bluetoothStateRepository.updateBluetoothAdapterStateToCurrentState()
    }
}


