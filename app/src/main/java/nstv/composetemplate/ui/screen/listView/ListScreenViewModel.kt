package nstv.composetemplate.ui.screen.listView

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import nstv.composetemplate.data.gif.Gif
import nstv.composetemplate.data.gif.GifRepository
import javax.inject.Inject

sealed interface ListScreenUiState {
    object Loading : ListScreenUiState
    data class Success(val data: List<Gif>) : ListScreenUiState
    data class Error(val message: String) : ListScreenUiState
}

@HiltViewModel
class ListScreenViewModel @Inject constructor(
    gifRepository: GifRepository
) : ViewModel() {

    val uiState: StateFlow<ListScreenUiState> = gifRepository.getTrendingGifs()
        .map<List<Gif>, ListScreenUiState>(ListScreenUiState::Success)
        .onStart { emit(ListScreenUiState.Loading) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ListScreenUiState.Loading,
        )
}