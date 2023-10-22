package nstv.bluetoothmagic.ui.screen.listView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nstv.bluetoothmagic.bluetooth.BluetoothAdapterState
import nstv.bluetoothmagic.bluetooth.BluetoothLeHandler
import nstv.bluetoothmagic.bluetooth.BluetoothStateRepository
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
) : ViewModel() {

    val uiState: StateFlow<ListScreenUiState> = bluetoothLeHandler.bluetoothState()
        .map<BluetoothAdapterState, ListScreenUiState>(ListScreenUiState::Loaded)
        .onStart { emit(ListScreenUiState.Loading) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = ListScreenUiState.Loading,
        )

    fun startScanning() {
        viewModelScope.launch {
            bluetoothLeHandler.scan()
        }
    }

    fun startAdvertising() {
        viewModelScope.launch {
            bluetoothLeHandler.startAdvertising()
        }
    }

    fun onBluetoothEnabled() {
        bluetoothLeHandler.bluetoothEnabled()
    }

    fun stopBluetoothAction() {
        bluetoothLeHandler.stopAction()
    }
}