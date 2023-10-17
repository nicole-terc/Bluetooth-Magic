package nstv.composetemplate.ui.screen.listView

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale.Companion
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import nstv.composetemplate.ui.theme.Grid

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
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.images.original.url)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.title,
                            placeholder = ColorPainter(Color.LightGray),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier
                                .padding(Grid.Half)
                                .fillMaxSize()
                                .aspectRatio(1f)
                                .padding(Grid.Half)
                        )
                    }
                }
            }

            is ListScreenUiState.Error -> {
                Text(text = uiState.message, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}