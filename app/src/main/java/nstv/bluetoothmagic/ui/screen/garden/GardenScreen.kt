package nstv.bluetoothmagic.ui.screen.garden

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nstv.bluetoothmagic.bluetooth.data.ScannedDevice
import nstv.bluetoothmagic.data.local.Ingredient
import nstv.bluetoothmagic.data.local.IngredientCombinations
import nstv.bluetoothmagic.ui.components.PermissionsWrapper
import nstv.bluetoothmagic.ui.theme.BluetoothMagicTheme
import nstv.bluetoothmagic.ui.theme.Grid

@Composable
fun GardenScreen(
    modifier: Modifier = Modifier,
    viewModel: GardenScreenViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ingredients by viewModel.ingredients.collectAsStateWithLifecycle()
    val isInteractingWithBluetooth by viewModel.isInteractingWithBluetooth.collectAsStateWithLifecycle()

    PermissionsWrapper(modifier.fillMaxSize()) {
        if (ingredients.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        } else {
            GardenScreenContent(
                uiState = uiState,
                ingredients = ingredients,
                isInteractingWithBluetooth = isInteractingWithBluetooth,
                modifier = Modifier.fillMaxSize(),
                onBluetoothEnabled = viewModel::onBluetoothEnabled,
                shareIngredient = viewModel::shareIngredient,
                searchForIngredient = viewModel::searchForIngredient,
                startAdvertising = viewModel::startAdvertising,
                startServer = viewModel::startServer,
                startScanning = viewModel::startScanning,
                connectToServer = viewModel::connectToServer,
                stopAdvertising = viewModel::stopAdvertising,
                stopAllBluetoothAction = viewModel::stopAllBluetoothAction,
                readCharacteristic = viewModel::readCharacteristic,
                writeCharacteristic = viewModel::writeCharacteristic,
            )
        }
    }

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.stopAllBluetoothAction()
        }
    }
}

@Composable
fun GardenScreenContent(
    uiState: GardenUiState,
    ingredients: List<Ingredient>,
    isInteractingWithBluetooth: Boolean,
    onBluetoothEnabled: () -> Unit,
    startAdvertising: (fromServer: Boolean) -> Unit,
    startServer: () -> Unit,
    startScanning: () -> Unit,
    connectToServer: (Context, ScannedDevice) -> Unit,
    stopAdvertising: (fromServer: Boolean) -> Unit,
    stopAllBluetoothAction: () -> Unit,
    readCharacteristic: (Context) -> Unit,
    writeCharacteristic: (Context) -> Unit,
    shareIngredient: () -> Unit,
    searchForIngredient: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        Garden(
            ingredients = ingredients,
            shareIngredient = shareIngredient,
            searchForIngredient = searchForIngredient,
            modifier = Modifier.fillMaxSize(),
        )

        AnimatedVisibility(
            visible = isInteractingWithBluetooth,
            modifier = Modifier.fillMaxSize(),
        ) {
            BluetoothActionsOverlay(
                bluetoothState = uiState.bluetoothState,
                onBluetoothEnabled = onBluetoothEnabled,
                startAdvertising = startAdvertising,
                startServer = startServer,
                startScanning = startScanning,
                connectToServer = connectToServer,
                stopAdvertising = stopAdvertising,
                stopAllBluetoothAction = stopAllBluetoothAction,
                readCharacteristic = readCharacteristic,
                writeCharacteristic = writeCharacteristic,
                modifier = Modifier
                    .fillMaxSize(0.8f)
                    .background(MaterialTheme.colorScheme.surface)
            )
        }
    }
}

@Composable
fun Garden(
    ingredients: List<Ingredient>,
    shareIngredient: () -> Unit,
    searchForIngredient: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mainIngredient = ingredients.find { it.isMainIngredient }
    val otherIngredients = ingredients.filter { !it.isMainIngredient }

    Column(modifier) {
        mainIngredient?.let {
            Row(
                Modifier
                    .fillMaxSize()
                    .weight(1.3f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {

                Button(
                    onClick = shareIngredient,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(Grid.Two)
                ) {
                    Icon(
                        Icons.Filled.Share,
                        "Share",
                        Modifier
                            .align(CenterVertically)
                            .fillMaxSize()
                    )
                }
                IngredientItem(
                    ingredient = mainIngredient, modifier = Modifier
                        .aspectRatio(0.6f)
                        .weight(1f)
                )
                Button(
                    onClick = searchForIngredient,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(Grid.Two)
                ) {
                    Icon(
                        Icons.Filled.Search,
                        "Get",
                        Modifier
                            .align(CenterVertically)
                            .fillMaxSize()
                    )
                }
            }

        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .weight(3f)
        ) {
            items(otherIngredients, key = { it.id }) { ingredient ->
                IngredientItem(ingredient = ingredient)
            }
        }
    }
}

@Composable
fun IngredientItem(
    ingredient: Ingredient,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(Grid.Half),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(color = ingredient.color.copy(alpha = 0.8f), shape = CircleShape)
                .padding(Grid.Half)
        ) {

            Image(
                painter = painterResource(id = ingredient.resource),
                contentDescription = "Image of ${ingredient.name}",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Grid.One)
                    .align(Alignment.Center)
            )
        }
        Text(
            text = ingredient.name,
            modifier = Modifier.padding(top = Grid.One),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Count: ${ingredient.count}",
            modifier = Modifier.padding(top = Grid.Half),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Preview
@Composable
private fun IngredientPreview() {
    BluetoothMagicTheme {
        IngredientItem(
            IngredientCombinations.bayBolete
        )
    }
}