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
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import android.bluetooth.BluetoothDevice
import androidx.bluetooth.GattCharacteristic
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
import nstv.bluetoothmagic.domain.AddOneToIngredientCount
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothLeHandlerOldApi @Inject constructor(
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
    private val bluetoothLeScanner: BluetoothLeScanner,
    private val bluetoothStateRepository: BluetoothStateRepository,
    private val addOneToIngredientCount: AddOneToIngredientCount,
) {
    private var scope = CoroutineScope(SupervisorJob())

    private var scannedDevices = listOf<ScanResult>()
    private var connectedDevice: BluetoothDevice? = null
    private var gattServer: BluetoothGatt? = null

    private var characteristicMap = mutableMapOf<UUID, String>()
    private var characteristicsRefMap = mutableMapOf<UUID, BluetoothGattCharacteristic>()

    private var myService: BluetoothGattService? = null
    private var updateGarden: Boolean = false

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
    private fun getGattServer(context: Context): BluetoothGatt? {
        gattServer?.connect() ?: {
            connectedDevice?.let {
                gattServer = it.connectGatt(context, false, gattCallback)
            }
        }

        return gattServer
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
    fun readCharacteristic(context: Context, updateGarden: Boolean) {
        this.updateGarden = updateGarden
        scope.launch(coroutineDispatcher) {
            val characteristicRead =
                getGattServer(context)?.readCharacteristic(characteristicsRefMap[GardenService.mainIngredientUUID])
            Log.d("BluetoothLeHandler", "readCharacteristic: $characteristicRead")
        }
    }

    @SuppressLint("MissingPermission")
    fun writeCharacteristic(context: Context, value: String) {
        scope.launch(coroutineDispatcher) {
            Log.d("BluetoothLeHandler", "attempting to writeCharacteristic: $value")
            val data = value.toByteArray()
            characteristicsRefMap[GardenService.shareIngredientUUID]?.let { shareCharacteristic ->
                Log.d("BluetoothLeHandler", "writeCharacteristic: $shareCharacteristic")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    getGattServer(context)?.writeCharacteristic(
                        shareCharacteristic,
                        data,
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    )
                } else {
                    shareCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    shareCharacteristic.value = data
                    getGattServer(context)?.writeCharacteristic(shareCharacteristic)
                }
            }
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

            handleCharacteristicRead(
                characteristicUUID = characteristic.uuid,
                value = value
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
            if (characteristic == null) return

            handleCharacteristicRead(
                characteristicUUID = characteristic.uuid,
                value = characteristic.value
            )
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            Log.d(
                "BluetoothLeHandler",
                "onCharacteristicWrite: ${characteristic?.uuid}"
            )
        }
    }

    private fun handleCharacteristicRead(
        characteristicUUID: UUID,
        value: ByteArray,
    ) {
        val newValue = value.decodeToString()
        characteristicMap[characteristicUUID] = newValue

        bluetoothStateRepository.updateBluetoothAdapterState(
            BluetoothAdapterState.Connected(
                characteristics = characteristicMap.toList(),
                updatedCharacteristic = characteristicUUID to newValue
            )
        )
        if (updateGarden) {
            updateGarden = false
            newValue.toIntOrNull()?.let {
                scope.launch {
                    addOneToIngredientCount(it)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopEverything() {
        stopScan()
        gattServer?.disconnect()
        gattServer?.close()
        gattServer = null
        connectedDevice = null
        scannedDevices = emptyList()
        myService = null
        updateGarden = false
        characteristicMap = mutableMapOf()
        characteristicsRefMap = mutableMapOf()
        bluetoothStateRepository.updateBluetoothAdapterStateToCurrentState()
        scope.cancel()
        scope = CoroutineScope(SupervisorJob())
    }
}