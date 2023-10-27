package nstv.bluetoothmagic.bluetooth

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.bluetooth.AdvertiseParams
import androidx.bluetooth.BluetoothDevice
import androidx.bluetooth.BluetoothLe
import androidx.bluetooth.GattServerRequest
import androidx.bluetooth.ScanFilter
import androidx.bluetooth.ScanResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nstv.bluetoothmagic.bluetooth.data.BluetoothAdapterState
import nstv.bluetoothmagic.bluetooth.data.BluetoothStateRepository
import nstv.bluetoothmagic.bluetooth.data.ScannedDevice
import nstv.bluetoothmagic.bluetooth.data.toScannedDevice
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
    private var scanningScope = CoroutineScope(SupervisorJob())

    private var isAdvertising: Boolean = false

    private var scannedDevices: List<ScanResult> = emptyList()
    private var connectedDevices: MutableList<BluetoothDevice> = mutableListOf()

    private var currentServerDevice: BluetoothDevice? = null

    fun bluetoothState() = bluetoothStateRepository.bluetoothAdapterState.asStateFlow()

    fun bluetoothEnabled() {
        bluetoothStateRepository.updateBluetoothAdapterState(BluetoothAdapterState.Enabled)
    }

    fun updateServerState(updatedCharacteristic: Pair<UUID, String>) {
        val currentState = bluetoothStateRepository.getCurrentState()
        if (currentState is BluetoothAdapterState.ServerStarted) {
            bluetoothStateRepository.updateBluetoothAdapterState(
                currentState.copy(
                    updatedCharacteristic = updatedCharacteristic
                )
            )
        }
    }

    fun getAdvertiseParams() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            AdvertiseParams(
                shouldIncludeDeviceName = true,
                isConnectable = true,
                isDiscoverable = true,
                serviceUuids = listOf(GardenService.serviceUUID),
                durationMillis = AdvertiseTimeout,
            )
        } else {
            // Devices running Android 12 and below throw ADVERTISE_FAILED_DATA_TOO_LARGE error if more parameters are set
            AdvertiseParams(
                shouldIncludeDeviceName = false,
                isConnectable = true,
                serviceUuids = listOf(GardenService.serviceUUID),
                durationMillis = AdvertiseTimeout,
            )
        }


    @SuppressLint("MissingPermission")
    suspend fun startAdvertising(fromServer: Boolean) {
        Log.d("BluetoothLeHandler", "StartAdvertising: $fromServer")
        isAdvertising = true
        val timeout: Int = if (fromServer) 0 else AdvertiseTimeout
        scope.launch {
            advertiseScope.launch(coroutineDispatcher) {
                bluetoothLe.advertise(getAdvertiseParams()) { advertiseResult ->
                    when (advertiseResult) {
                        BluetoothLe.ADVERTISE_STARTED -> {
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

    @SuppressLint("MissingPermission")
    suspend fun scan(scanFilters: List<ScanFilter> = emptyList()) {
        scannedDevices = emptyList()
        bluetoothStateRepository.updateBluetoothAdapterState(
            BluetoothAdapterState.Scanning(emptyList())
        )
        scope.launch(coroutineDispatcher) {
            scanningScope.launch {
                bluetoothLe.scan(scanFilters + ScanFilter(serviceUuid = GardenService.serviceUUID))
                    .collect { scanResult ->
                        scannedDevices =
                            (scannedDevices + scanResult).distinctBy { it.deviceAddress }
                        bluetoothStateRepository.updateBluetoothAdapterState(
                            BluetoothAdapterState.Scanning(
                                scannedDevices.map { it.toScannedDevice() }
                            )
                        )
                    }
            }
        }
    }

    suspend fun startServer(
        startAdvertising: Boolean = false,
        handleReadRequest: ((BluetoothDevice, UUID) -> ByteArray?)? = null,
        handleWriteRequest: ((BluetoothDevice, UUID, ByteArray) -> Unit)? = null,
    ) {
        connectedDevices = mutableListOf()
        bluetoothStateRepository.updateBluetoothAdapterState(BluetoothAdapterState.Loading)
        scope.launch(coroutineDispatcher) {
            val server = bluetoothLe.openGattServer(
                services = listOf(GardenService.getGattService())
            ) {
                // Update State
                bluetoothStateRepository.updateBluetoothAdapterState(
                    BluetoothAdapterState.ServerStarted(
                        false
                    )
                )

                // Start Advertising
                if (startAdvertising) {
                    startAdvertising(true)
                }

                // Handle Requests
                this.connectRequests.collect { request ->
                    request.accept {
                        connectedDevices.add(request.device)
                        bluetoothStateRepository.updateBluetoothAdapterState(
                            BluetoothAdapterState.ServerStarted(
                                isAdvertising = isAdvertising,
                                connectedDevices = connectedDevices.map { it.toScannedDevice() }
                            )
                        )
                        this.requests.collect { serverRequest ->
                            Log.d("BluetoothLeHandler", "Request: $serverRequest")
                            when (serverRequest) {
                                is GattServerRequest.ReadCharacteristic -> {
                                    val readValue = handleReadRequest?.invoke(
                                        request.device,
                                        serverRequest.characteristic.uuid
                                    ) ?: "Hello from server".toByteArray()

                                    serverRequest.sendResponse(readValue)
                                }

                                is GattServerRequest.WriteCharacteristics -> {
                                    Log.d(
                                        "BluetoothLeHandler",
                                        "WriteCharacteristics: $serverRequest"
                                    )
                                    serverRequest.parts.find { it.characteristic.uuid == GardenService.giveMushroomUUID }
                                        ?.let { part ->
                                            handleWriteRequest?.invoke(
                                                request.device,
                                                part.characteristic.uuid,
                                                part.value
                                            )
                                        }
                                    serverRequest.sendResponse()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Not Working for some reason :(
    // Check BluetoothLeHandlerOldApi.kt for working example
    @SuppressLint("MissingPermission")
    suspend fun connectToServer(scanResult: ScannedDevice) {
        Log.d("BluetoothLeHandler", "ConnectToServer: $scanResult")
        stopScanning()
        bluetoothStateRepository.updateBluetoothAdapterState(BluetoothAdapterState.Connecting)
        scope.launch(coroutineDispatcher) {
            val device =
                scannedDevices.first { it.deviceAddress.address == scanResult.deviceAddress }.device
            bluetoothLe.connectGatt(device) {
                currentServerDevice = device
                val characteristics: List<Pair<UUID, String>> =
                    getService(GardenService.serviceUUID)?.characteristics?.map {
                        it.uuid to it.properties.toString()
                    } ?: emptyList()

                bluetoothStateRepository.updateBluetoothAdapterState(
                    BluetoothAdapterState.Connected(
                        characteristics = characteristics,
                    )
                )

//                val gardenCharacteristic = getService(serviceUUID)?.getCharacteristic(
//                    GardenService.characteristicUUID
//                )!!
//
//                Log.d("BluetoothLeHandler", "ReadCharacteristic: $gardenCharacteristic")
//                val data = readCharacteristic(gardenCharacteristic)
//                val stringData = data.getOrNull()?.toString(Charsets.UTF_8) ?: ""
//                Log.d("BluetoothLeHandler", "DATA: $stringData")
//
//                bluetoothStateRepository.updateBluetoothAdapterState(
//                    BluetoothAdapterState.Connected(
//                        characteristics = listOf(
//                            gardenCharacteristic.uuid to stringData
//                        ),
//                    )
//                )

//                Log.d("BluetoothLeHandler", "WriteCharacteristic: $characteristics")
//                writeCharacteristic(
//                    gardenCharacteristic,
//                    "Hello from client".toByteArray()
//                )


            }
        }
    }

    // Not Working for some reason :(
    // Check BluetoothLeHandlerOldApi.kt for working example
    @SuppressLint("MissingPermission")
    suspend fun readCharacteristic() {
        Log.d("BluetoothLeHandler", "ReadCharacteristic HERE")
        scope.launch(coroutineDispatcher) {
            scope.launch(coroutineDispatcher) {
                currentServerDevice?.let { serverDevice ->
                    bluetoothLe.connectGatt(serverDevice) {
                        val characteristic = async {
                            readCharacteristic(
                                getService(GardenService.serviceUUID)?.getCharacteristic(
                                    GardenService.mushroomToGetUUID
                                )!!
                            )
                        }
                        characteristic.await().let { result ->
                            Log.d("BluetoothLeHandler", "ReadCharacteristic result: $result")

                            if (result.isSuccess) {
                                bluetoothStateRepository.updateBluetoothAdapterState(
                                    BluetoothAdapterState.Connected(
                                        characteristics = listOf(
                                            GardenService.mushroomToGetUUID to
                                                    (result.getOrNull()?.toString(Charsets.UTF_8)
                                                        ?: "")
                                        ),
                                    )

                                )
                            } else {
                                bluetoothStateRepository.updateBluetoothAdapterState(
                                    BluetoothAdapterState.Error(
                                        "Error reading characteristic: ${result.exceptionOrNull()?.message}"
                                    )
                                )
                            }
                        }
                    }
                }
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

    fun stopScanning() {
        scanningScope.cancel()
        scanningScope = CoroutineScope(SupervisorJob())
    }

    fun stopEverything() {
        scope.cancel()
        scope = CoroutineScope(SupervisorJob())
        scannedDevices = emptyList()
        connectedDevices = mutableListOf()
        currentServerDevice = null
        bluetoothStateRepository.updateBluetoothAdapterStateToCurrentState()
    }
}