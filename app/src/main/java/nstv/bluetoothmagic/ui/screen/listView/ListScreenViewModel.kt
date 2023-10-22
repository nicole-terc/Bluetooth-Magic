package nstv.bluetoothmagic.ui.screen.listView

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nstv.bluetoothmagic.bluetooth.data.BluetoothAdapterState
import nstv.bluetoothmagic.bluetooth.BluetoothLeHandler
import nstv.bluetoothmagic.bluetooth.BluetoothLeHandlerOldApi
import nstv.bluetoothmagic.bluetooth.data.ScannedDevice
import javax.inject.Inject

sealed interface ListScreenUiState {
    data object Loading : ListScreenUiState
    data class Loaded(
        val bluetoothState: BluetoothAdapterState,
    ) : ListScreenUiState

    data class Error(val message: String) : ListScreenUiState
}

@HiltViewModel
class ListScreenViewModel @Inject constructor(
    private val bluetoothLeHandler: BluetoothLeHandler,
    private val bluetoothLeHandlerOldApi: BluetoothLeHandlerOldApi,
) : ViewModel() {

    val uiState: StateFlow<ListScreenUiState> = bluetoothLeHandler.bluetoothState()
        .map<BluetoothAdapterState, ListScreenUiState>(ListScreenUiState::Loaded)
        .onStart { emit(ListScreenUiState.Loading) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ListScreenUiState.Loading,
        )

    fun startAdvertising(fromServer: Boolean) {
        viewModelScope.launch {
            bluetoothLeHandler.startAdvertising(fromServer)
        }
    }

    fun startServer() {
        viewModelScope.launch {
            bluetoothLeHandler.startServer()
        }
    }

    fun startScanning() {
        viewModelScope.launch {
//            bluetoothLeHandler.scan()
            bluetoothLeHandlerOldApi.scan()
        }
    }

    fun connectToServer(context: Context, scanResult: ScannedDevice) {
        viewModelScope.launch {
//            bluetoothLeHandler.connectToServer(scanResult)
            bluetoothLeHandlerOldApi.connectToServer(context, scanResult)
        }
    }

    fun readCharacteristic() {
        viewModelScope.launch {
//            bluetoothLeHandler.readCharacteristic()
            bluetoothLeHandlerOldApi.readCharacteristic()
        }
    }

    fun onBluetoothEnabled() {
        bluetoothLeHandler.bluetoothEnabled()
    }

    fun stopAllBluetoothAction() {
        bluetoothLeHandler.stopEverything()
        bluetoothLeHandlerOldApi.stopEverything()
    }

    fun stopAdvertising(fromServer: Boolean) {
        bluetoothLeHandler.stopAdvertising(fromServer)
    }
}