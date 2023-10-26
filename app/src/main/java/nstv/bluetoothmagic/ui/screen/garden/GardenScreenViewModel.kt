package nstv.bluetoothmagic.ui.screen.garden

import android.content.Context
import androidx.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import nstv.bluetoothmagic.bluetooth.BluetoothLeHandler
import nstv.bluetoothmagic.bluetooth.BluetoothLeHandlerOldApi
import nstv.bluetoothmagic.bluetooth.GardenService
import nstv.bluetoothmagic.bluetooth.data.BluetoothAdapterState
import nstv.bluetoothmagic.bluetooth.data.ScannedDevice
import nstv.bluetoothmagic.data.local.Ingredient
import nstv.bluetoothmagic.data.local.toIngredient
import nstv.bluetoothmagic.domain.AddOneToIngredientCount
import nstv.bluetoothmagic.domain.GetAllIngredientsUseCase
import nstv.bluetoothmagic.domain.GetMainIngredientIdUseCase
import java.util.UUID
import javax.inject.Inject

data class GardenUiState(
    val bluetoothState: BluetoothAdapterState = BluetoothAdapterState.Loading,
)

@HiltViewModel
class GardenScreenViewModel @Inject constructor(
    private val bluetoothLeHandler: BluetoothLeHandler,
    private val bluetoothLeHandlerOldApi: BluetoothLeHandlerOldApi,
    private val getAllIngredientsUseCase: GetAllIngredientsUseCase,
    private val getMainIngredientIdUseCase: GetMainIngredientIdUseCase,
    private val addOneToIngredientCount: AddOneToIngredientCount,
) : ViewModel() {

    private var mainIngredientId: Int = -1
    val isInteractingWithBluetooth = MutableStateFlow(false)
    val ingredients: StateFlow<List<Ingredient>> = getAllIngredientsUseCase()
        .onStart { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList(),
        )
    val uiState: StateFlow<GardenUiState> =
        bluetoothLeHandler.bluetoothState().map { bluetoothState ->
            GardenUiState(
                bluetoothState = bluetoothState,
            )
        }.onStart {
            GardenUiState()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = GardenUiState(),
        )


    init {
        viewModelScope.launch {
            mainIngredientId = getMainIngredientIdUseCase()
        }
    }

    fun searchForIngredient() {
        startInteractingWithBluetooth()
        startScanning()
    }

    fun shareIngredient() {
        startInteractingWithBluetooth()
        startServer()
    }

    fun startAdvertising(fromServer: Boolean) {
        viewModelScope.launch {
            bluetoothLeHandler.startAdvertising(fromServer)
        }
    }

    fun startServer() {
        viewModelScope.launch {
            bluetoothLeHandler.startServer(
                startAdvertising = false,
                handleReadRequest = ::handleServerReadRequest,
                handleWriteRequest = ::handleServerWriteRequest,
            )
        }
    }

    private fun handleServerWriteRequest(
        device: BluetoothDevice,
        characteristicUUID: UUID,
        value: ByteArray
    ) {
        handleUpdatedCharacteristic(characteristicUUID to value.decodeToString())
    }

    private fun handleServerReadRequest(
        device: BluetoothDevice,
        characteristicUUID: UUID
    ): ByteArray? =
        when (characteristicUUID) {
            GardenService.mushroomToGetUUID -> mainIngredientId.toString().toByteArray()
            else -> null
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

    fun readCharacteristic(context: Context) {
        viewModelScope.launch {
            bluetoothLeHandlerOldApi.readCharacteristic(context, true)
        }
    }

    fun writeCharacteristic(context: Context) {
        viewModelScope.launch {
            bluetoothLeHandlerOldApi.writeCharacteristic(context, mainIngredientId.toString())
        }
    }

    fun onBluetoothEnabled() {
        bluetoothLeHandler.bluetoothEnabled()
    }

    fun stopAllBluetoothAction() {
        viewModelScope.launch {
            stopInteractingWithBluetooth()
            bluetoothLeHandler.stopEverything()
            bluetoothLeHandlerOldApi.stopEverything()
        }
    }

    fun stopAdvertising(fromServer: Boolean) {
        viewModelScope.launch {
            bluetoothLeHandler.stopAdvertising(fromServer)
        }
    }

    private fun startInteractingWithBluetooth() {
        isInteractingWithBluetooth.update { true }
    }

    private fun stopInteractingWithBluetooth() {
        isInteractingWithBluetooth.update { false }
    }

    private fun handleUpdatedCharacteristic(characteristic: Pair<UUID, String>) {
        characteristic.second.toIntOrNull()?.let { ingredientId ->
            viewModelScope.launch {
                addOneToIngredientCount(ingredientId)
            }
        }
    }

    fun onIngredientClick(ingredient: Ingredient) {
        viewModelScope.launch {
            addOneToIngredientCount(ingredient.id)
        }
    }
}