package nstv.bluetoothmagic.ui.screen.garden

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nstv.bluetoothmagic.bluetooth.BluetoothLeHandler
import nstv.bluetoothmagic.bluetooth.BluetoothLeHandlerOldApi
import nstv.bluetoothmagic.bluetooth.data.BluetoothAdapterState
import nstv.bluetoothmagic.bluetooth.data.ScannedDevice
import nstv.bluetoothmagic.data.local.Ingredient
import nstv.bluetoothmagic.domain.GetAllIngredientsUseCase
import javax.inject.Inject

data class GardenUiState(
    val ingredients: List<Ingredient> = emptyList(),
    val bluetoothState: BluetoothAdapterState = BluetoothAdapterState.Loading,
    val isLoading: Boolean = true,
)

@HiltViewModel
class GardenScreenViewModel @Inject constructor(
    private val bluetoothLeHandler: BluetoothLeHandler,
    private val bluetoothLeHandlerOldApi: BluetoothLeHandlerOldApi,
    private val getAllIngredientsUseCase: GetAllIngredientsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<GardenUiState>(GardenUiState())
    val uiState: StateFlow<GardenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAllIngredientsUseCase().collect {
                onIngredientsUpdated(it)
            }

            bluetoothLeHandler.bluetoothState().collect {
                onBluetoothStateUpdated(it)
            }
        }
    }

    private fun onIngredientsUpdated(ingredients: List<Ingredient>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(ingredients = ingredients, isLoading = false)
        }
    }

    private fun onBluetoothStateUpdated(bluetoothState: BluetoothAdapterState) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(bluetoothState = bluetoothState, isLoading = false)
        }
    }

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
            bluetoothLeHandlerOldApi.scan()
        }
    }

    fun connectToServer(context: Context, scanResult: ScannedDevice) {
        viewModelScope.launch {
            bluetoothLeHandlerOldApi.connectToServer(context, scanResult)
        }
    }

    fun readCharacteristic() {
        viewModelScope.launch {
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