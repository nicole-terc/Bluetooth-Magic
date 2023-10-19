package nstv.bluetoothmagic.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nstv.bluetoothmagic.ui.screen.garden.GardenScreen
import nstv.bluetoothmagic.ui.screen.listView.ListScreenView
import nstv.bluetoothmagic.ui.theme.Grid
import nstv.bluetoothmagic.ui.theme.components.DropDownWithArrows

private enum class Screen {
    SCANNER_LIST,
    GARDEN,
}

@Composable
fun MainContent(modifier: Modifier = Modifier) {

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .padding(Grid.Two),
    ) { paddingValues ->
        var selectedScreen by remember { mutableStateOf(Screen.SCANNER_LIST) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            DropDownWithArrows(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopStart),
                options = Screen.values().map { it.name }.toList(),
                selectedIndex = Screen.values().indexOf(selectedScreen),
                onSelectionChanged = { selectedScreen = Screen.values()[it] },
                textStyle = MaterialTheme.typography.headlineSmall,
                loopSelection = true,
            )
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Grid.One)
            )
            Crossfade(
                targetState = selectedScreen,
                animationSpec = tween(durationMillis = 500), label = "Main crossfade"
            ) { screen ->
                when (screen) {
                    Screen.SCANNER_LIST -> ListScreenView()
                    Screen.GARDEN -> GardenScreen()
                }
            }
        }
    }
}