package nstv.bluetoothmagic.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothLeHandlerOldApi @Inject constructor(
    private val bluetoothLeScanner: BluetoothLeScanner,
    private val bluetoothStateRepository: BluetoothStateRepository,
) {

    private var scannedDevices = listOf<ScanResult>()
    private var gattServer: BluetoothGatt? = null

    private var characteristicMap = mutableMapOf<UUID, String>()

    val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.d("BluetoothLeHandler", "onScanResult: $result")
            result?.let { scanResult ->
                scannedDevices =
                    (scannedDevices + scanResult).distinctBy { it.device.address }
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
        val device =
            scannedDevices.first { it.device.address == scannedDevice.deviceAddress }.device
        gattServer = device.connectGatt(context, false, gattCallback)

    }

    val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d("BluetoothLeHandler", "onConnectionStateChange: $status -> $newState")
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> {
                    bluetoothStateRepository.updateBluetoothAdapterState(
                        BluetoothAdapterState.Connected(
                            gatt?.services?.flatMap { service ->
                                service.characteristics.map { characteristic ->
                                    characteristic.uuid to ""
                                }
                            } ?: emptyList()
                        )
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
            Log.d("BluetoothLeHandler", "onServicesDiscovered: $status")
            super.onServicesDiscovered(gatt, status)
            gatt?.services?.forEach { service ->
                Log.d("BluetoothLeHandler", "onServicesDiscovered: ${service.uuid}")
                service.characteristics.forEach { characteristic ->
                    Log.d("BluetoothLeHandler", "onServicesDiscovered: ${characteristic.uuid}")
                    gatt.readCharacteristic(characteristic)
                }
            }
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
                "onCharacteristicRead: ${characteristic.uuid} -> ${value.toString(Charsets.UTF_8)}"
            )
            characteristicMap[characteristic.uuid] = value.toString(Charsets.UTF_8)

            bluetoothStateRepository.updateBluetoothAdapterState(
                BluetoothAdapterState.Connected(characteristicMap.toList())
            )
        }
    }
}