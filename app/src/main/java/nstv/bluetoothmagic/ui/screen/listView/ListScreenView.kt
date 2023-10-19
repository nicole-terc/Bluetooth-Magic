package nstv.bluetoothmagic.ui.screen.listView

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nstv.bluetoothmagic.ui.components.PermissionsWrapper
import nstv.bluetoothmagic.ui.theme.Grid

@Composable
fun ListScreenView(
    modifier: Modifier = Modifier,
    viewModel: ListScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PermissionsWrapper(modifier.fillMaxSize()) {
        ListScreenContent(
            uiState = uiState,
            startScanning = viewModel::startScanning,
            startAdvertising = viewModel::startAdvertising,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ListScreenContent(
    uiState: ListScreenUiState,
    startScanning: () -> Unit,
    startAdvertising: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (uiState) {
            is ListScreenUiState.Loading -> {
                CircularProgressIndicator()
            }

            is ListScreenUiState.Scanning -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.data) { item ->
                        Text(
                            text = item.device.name.toString(),
                            modifier = Modifier.padding(Grid.Half)
                        )
                        HorizontalDivider(Modifier.height(Grid.Single))
                    }
                }
            }

            is ListScreenUiState.Error -> {
                Text(text = uiState.message)
            }

            ListScreenUiState.Advertising -> {
                Text(text = "Advertising")
            }

            ListScreenUiState.Ready -> {
                Row(Modifier.fillMaxWidth()) {
                    Button(
                        onClick = startScanning,
//                        enabled = uiState.bluetoothState == BluetoothState.ENABLED
                    ) {
                        Text(text = "Start Scanning")
                    }
                    Button(
                        onClick = startAdvertising,
//                        enabled = uiState.bluetoothState == BluetoothState.ENABLED
                    ) {
                        Text(text = "Start Advertising")
                    }
                }
            }
        }
    }
}