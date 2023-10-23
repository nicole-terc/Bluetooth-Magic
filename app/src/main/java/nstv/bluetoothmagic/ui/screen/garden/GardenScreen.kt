package nstv.bluetoothmagic.ui.screen.garden

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    PermissionsWrapper(modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            GardenScreenContent(uiState = uiState, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun GardenScreenContent(
    uiState: GardenUiState,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Garden(
            ingredients = uiState.ingredients,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}


@Composable
fun Garden(
    ingredients: List<Ingredient>,
    modifier: Modifier = Modifier,
) {
    val mainIngredient = ingredients.find { it.isMainIngredient }
    val otherIngredients = ingredients.filter { !it.isMainIngredient }

    Column(modifier) {
        mainIngredient?.let {
            IngredientItem(
                ingredient = mainIngredient, modifier = Modifier
                    .weight(1.3f)
                    .aspectRatio(0.6f)
                    .align(Alignment.CenterHorizontally)
            )
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