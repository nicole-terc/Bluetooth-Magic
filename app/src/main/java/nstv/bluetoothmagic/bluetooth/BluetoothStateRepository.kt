package nstv.bluetoothmagic.bluetooth

import android.bluetooth.BluetoothAdapter
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class BluetoothStateRepository @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
) {
    val bluetoothAdapterState =
        MutableStateFlow<BluetoothAdapterState>(bluetoothAdapter.state.toBluetoothAdapterState())

    fun updateBluetoothAdapterState(state: Int) {
        val newState = state.toBluetoothAdapterState()
        Log.i("BluetoothStateRepository", "updateBluetoothAdapterState: $state -> $newState")
        bluetoothAdapterState.value = newState
    }

    fun updateBluetoothAdapterState(state: BluetoothAdapterState) {
        Log.i("BluetoothStateRepository", "updateBluetoothAdapterState: $state")
        bluetoothAdapterState.value = state
    }

    fun updateBluetoothAdapterStateToCurrentState() {
        updateBluetoothAdapterState(bluetoothAdapter.state)
    }

    fun getCurrentState()  = bluetoothAdapterState.value

    fun isBluetoothEnabled() = bluetoothAdapter.isEnabled
}