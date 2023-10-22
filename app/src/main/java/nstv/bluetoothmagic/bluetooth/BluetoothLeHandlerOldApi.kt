package nstv.bluetoothmagic.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import nstv.bluetoothmagic.bluetooth.data.BluetoothAdapterState
import nstv.bluetoothmagic.bluetooth.data.BluetoothStateRepository
import nstv.bluetoothmagic.bluetooth.data.ScannedDevice
import nstv.bluetoothmagic.bluetooth.data.toScannedDevice
import nstv.bluetoothmagic.di.IoDispatcher
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothLeHandlerOldApi @Inject constructor(
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
    private val bluetoothLeScanner: BluetoothLeScanner,
    private val bluetoothStateRepository: BluetoothStateRepository,
) {
    private var scope = CoroutineScope(SupervisorJob())

    private var scannedDevices = listOf<ScanResult>()
    private var gattServer: BluetoothGatt? = null

    private var characteristicMap = mutableMapOf<UUID, String>()
    private var characteristicsRefMap = mutableMapOf<UUID, BluetoothGattCharacteristic>()

    private var myService: BluetoothGattService? = null

    val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.d("BluetoothLeHandler", "onScanResult: $result")
            result?.let { scanResult ->
                scannedDevices =
                    (scannedDevices + scanResult).distinctBy { it.device.address }
                bluetoothStateRepository.updateBluetoothAdapterState(
                    BluetoothAdapterState.Scanning(
                        scannedDevices.map { it.toScannedDevice() }
                    )
                )
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("BluetoothLeHandler", "onScanFailed: $errorCode")
            bluetoothStateRepository.updateBluetoothAdapterState(
                BluetoothAdapterState.Error(
                    "Error Scanning $errorCode"
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun scan() {
        scannedDevices = emptyList()
        bluetoothStateRepository.updateBluetoothAdapterState(
            BluetoothAdapterState.Scanning(
                emptyList()
            )
        )
        bluetoothLeScanner.startScan(
            listOf(
                ScanFilter.Builder().setServiceUuid(ParcelUuid(GardenService.serviceUUID))
                    .build()
            ),
            ScanSettings.Builder().build(),
            scanCallback
        )
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothLeScanner.stopScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun connectToServer(context: Context, scannedDevice: ScannedDevice) {
        bluetoothStateRepository.updateBluetoothAdapterState(BluetoothAdapterState.Connecting)
        stopScan()
        val device =
            scannedDevices.first { it.device.address == scannedDevice.deviceAddress }.device
        gattServer = device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun readCharacteristic() {
        scope.launch(coroutineDispatcher) {
            val characteristicRead =
                gattServer?.readCharacteristic(characteristicsRefMap[GardenService.ingredientCharacteristicUUID])
            Log.d("BluetoothLeHandler", "readCharacteristic: $characteristicRead")
        }
    }

    val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d("BluetoothLeHandler", "onConnectionStateChange: $status -> $newState")
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> {
                    bluetoothStateRepository.updateBluetoothAdapterState(
                        BluetoothAdapterState.Connected(emptyList())
                    )
                    gatt?.discoverServices()
                }

                BluetoothGatt.STATE_DISCONNECTED -> {
                    bluetoothStateRepository.updateBluetoothAdapterState(BluetoothAdapterState.Disconnected)
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            gatt?.services?.forEach { service ->
                Log.d("BluetoothLeHandler", "onServicesDiscovered: ${service.uuid}")
                if (service.uuid == GardenService.serviceUUID) {
                    myService = service
                    service.characteristics.forEach { characteristic ->
                        Log.d(
                            "BluetoothLeHandler",
                            "onCharacteristicDiscovered: ${characteristic.uuid}"
                        )
                        characteristicsRefMap[characteristic.uuid] = characteristic
                        characteristicMap[characteristic.uuid] = ""
                    }
                }
            }
            bluetoothStateRepository.updateBluetoothAdapterState(
                BluetoothAdapterState.Connected(characteristicMap.toList())
            )
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            Log.d(
                "BluetoothLeHandler",
                "onCharacteristicRead: ${characteristic.uuid}"
            )

            characteristicMap[characteristic.uuid] = value.decodeToString()

            bluetoothStateRepository.updateBluetoothAdapterState(
                BluetoothAdapterState.Connected(characteristicMap.toList())
            )
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            Log.d(
                "BluetoothLeHandler",
                "onCharacteristicRead OLD: ${characteristic?.uuid}"
            )
            characteristic?.let {
                characteristicMap[characteristic.uuid] = characteristic.value.decodeToString()
            }
            bluetoothStateRepository.updateBluetoothAdapterState(
                BluetoothAdapterState.Connected(characteristicMap.toList())
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun stopEverything() {
        stopScan()
        gattServer?.disconnect()
        gattServer?.close()
        gattServer = null
        scannedDevices = emptyList()
        myService = null
        characteristicMap = mutableMapOf()
        characteristicsRefMap = mutableMapOf()
        bluetoothStateRepository.updateBluetoothAdapterStateToCurrentState()
        scope.cancel()
        scope = CoroutineScope(SupervisorJob())
    }
}