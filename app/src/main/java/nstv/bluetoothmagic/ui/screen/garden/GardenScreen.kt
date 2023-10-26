package nstv.bluetoothmagic.ui.screen.garden

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nstv.bluetoothmagic.bluetooth.data.ScannedDevice
import nstv.bluetoothmagic.data.local.Ingredient
import nstv.bluetoothmagic.data.local.IngredientCombinations
import nstv.bluetoothmagic.sheep.LoadingSheep
import nstv.bluetoothmagic.ui.components.PermissionsWrapper
import nstv.bluetoothmagic.ui.screen.garden.Debug.IncreaseOnClick
import nstv.bluetoothmagic.ui.theme.BluetoothMagicTheme
import nstv.bluetoothmagic.ui.theme.Grid

private object Debug {
    const val IncreaseOnClick = true
}

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
                LoadingSheep(Modifier.align(Alignment.Center))
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
                onIngredientClick = {
                    if (IncreaseOnClick) {
                        viewModel.onIngredientClick(it)
                    }
                },
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
    onIngredientClick: (Ingredient) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        Garden(
            ingredients = ingredients,
            shareIngredient = shareIngredient,
            searchForIngredient = searchForIngredient,
            onIngredientClick = onIngredientClick,
            modifier = Modifier.fillMaxSize(),
        )

        AnimatedVisibility(
            visible = isInteractingWithBluetooth,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable {
//                    stopAllBluetoothAction()
                }
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
                        .fillMaxWidth(0.8f)
                        .fillMaxHeight(0.8f)
                        .padding(Grid.One)
                        .align(Center)
                        .background(MaterialTheme.colorScheme.surface)
                )
            }
        }
    }
}

@Composable
fun Garden(
    ingredients: List<Ingredient>,
    shareIngredient: () -> Unit,
    searchForIngredient: () -> Unit,
    onIngredientClick: (Ingredient) -> Unit,
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
                    ingredient = mainIngredient,
                    onIngredientClick = onIngredientClick,
                    modifier = Modifier
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
                IngredientItem(ingredient = ingredient, onIngredientClick = onIngredientClick)
            }
        }
    }
}

val blackAndWhite = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

@Composable
fun IngredientItem(
    ingredient: Ingredient,
    onIngredientClick: (Ingredient) -> Unit,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        label = "${ingredient.id}scale",
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    scale = if (scale == 1f) 0.5f else 1f
                    onIngredientClick(ingredient)
                }
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
                    .scale(animatedScale),
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
            IngredientCombinations.bayBolete,
            {}
        )
    }
}