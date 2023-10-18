package nstv.bluetoothmagic.ui.screen.listView

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import nstv.bluetoothmagic.data.local.SharedData
import javax.inject.Inject

sealed interface ListScreenUiState {
    object Loading : ListScreenUiState
    data class Success(val data: List<SharedData>) : ListScreenUiState
    data class Error(val message: String) : ListScreenUiState
}

@HiltViewModel
class ListScreenViewModel @Inject constructor() : ViewModel() {

    val uiState: StateFlow<ListScreenUiState> =
        MutableStateFlow(ListScreenUiState.Success(listOf(SharedData("Hello World"))))

//    val uiState: StateFlow<ListScreenUiState> = gifRepository.getTrendingGifs()
//        .map<List<Gif>, ListScreenUiState>(ListScreenUiState::Success)
//        .onStart { emit(ListScreenUiState.Loading) }
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5_000),
//            initialValue = ListScreenUiState.Loading,
//        )
}