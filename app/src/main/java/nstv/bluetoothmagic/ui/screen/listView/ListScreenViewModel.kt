package nstv.bluetoothmagic.ui.screen.listView

import androidx.bluetooth.ScanResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import nstv.bluetoothmagic.bluetooth.BluetoothLeHandler
import javax.inject.Inject

enum class BluetoothState {
    ENABLED,
    DISABLED,
    ADVERTISING,
    SCANNING,
}

sealed interface ListScreenUiState {
    data object Loading : ListScreenUiState
    data object Ready : ListScreenUiState
    data object Advertising : ListScreenUiState
    data class Scanning(
        val data: List<ScanResult>,
    ) : ListScreenUiState

    data class Error(val message: String) : ListScreenUiState
}

@HiltViewModel
class ListScreenViewModel @Inject constructor(
    private val bluetoothLeHandler: BluetoothLeHandler,
) : ViewModel() {

    // TODO: Only make it ready when bluetooth is enabled
    val uiState = MutableStateFlow<ListScreenUiState>(ListScreenUiState.Ready)

    fun startScanning() {
        viewModelScope.launch {
            bluetoothLeHandler.scan().collect { scanResult ->
                uiState.value =
                    (uiState.value as? ListScreenUiState.Scanning)?.let { currentState ->
                        ListScreenUiState.Scanning(data = currentState.data + scanResult)
                    } ?: ListScreenUiState.Scanning(data = listOf(scanResult))
            }
        }
    }

    fun startAdvertising() {
        viewModelScope.launch {
            bluetoothLeHandler.startAdvertising()
            uiState.value = ListScreenUiState.Advertising
        }
    }
}