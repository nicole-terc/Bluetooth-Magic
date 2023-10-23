package nstv.bluetoothmagic.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import nstv.bluetoothmagic.bluetooth.data.BluetoothAdapterState
import nstv.bluetoothmagic.bluetooth.data.BluetoothStateRepository
import nstv.bluetoothmagic.bluetooth.data.toScannedDevice
import nstv.bluetoothmagic.di.IoDispatcher
import javax.inject.Inject

class BluetoothClassicHandler @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineScope,
    private val bluetoothAdapter: BluetoothAdapter,
    private val bluetoothAdapterStateRepository: BluetoothStateRepository,
) {

    val scope = CoroutineScope(SupervisorJob())

    @SuppressLint("MissingPermission")
    fun queryPairedDevices() {
        scope.launch {
            val pairedDevices = bluetoothAdapter.bondedDevices
            bluetoothAdapterStateRepository.updateBluetoothAdapterState(
                BluetoothAdapterState.ServerStarted(
                    isAdvertising = false,
                    pairedDevices = pairedDevices.map { it.toScannedDevice() }
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun queryConnectedDevices() {
        scope.launch {
            val connectedDevices = bluetoothAdapter.bondedDevices
            bluetoothAdapterStateRepository.updateBluetoothAdapterState(
                BluetoothAdapterState.ServerStarted(
                    isAdvertising = false,
                    connectedDevices = connectedDevices.map { it.toScannedDevice() }
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery(){
        scope.launch {
            if(bluetoothAdapter.startDiscovery()){
                bluetoothAdapterStateRepository.updateBluetoothAdapterState(
                    BluetoothAdapterState.Scanning(
                        scannedDevices = emptyList()
                    )
                )
            } else {
                bluetoothAdapterStateRepository.updateBluetoothAdapterState(
                    BluetoothAdapterState.Error(
                        message = "Failed to start discovery"
                    )
                )
            }
        }
    }


}