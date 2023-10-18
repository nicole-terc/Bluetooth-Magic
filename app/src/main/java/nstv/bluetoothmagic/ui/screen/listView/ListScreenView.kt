package nstv.bluetoothmagic.ui.screen.listView

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nstv.bluetoothmagic.ui.theme.Grid

@Composable
fun ListScreenView(
    modifier: Modifier = Modifier,
    viewModel: ListScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ListScreenContent(uiState = uiState, modifier = modifier)
}

@Composable
fun ListScreenContent(
    uiState: ListScreenUiState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when (uiState) {
            is ListScreenUiState.Loading -> {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }

            is ListScreenUiState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.data) { item ->
                        Text(text = item.text, modifier = Modifier.padding(Grid.Half))
//                        AsyncImage(
//                            model = ImageRequest.Builder(LocalContext.current)
//                                .data(item.images.original.url)
//                                .crossfade(true)
//                                .build(),
//                            contentDescription = item.title,
//                            placeholder = ColorPainter(Color.LightGray),
//                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
//                            modifier = Modifier
//                                .padding(Grid.Half)
//                                .fillMaxSize()
//                                .aspectRatio(1f)
//                                .padding(Grid.Half)
//                        )
                    }
                }
            }

            is ListScreenUiState.Error -> {
                Text(text = uiState.message, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}