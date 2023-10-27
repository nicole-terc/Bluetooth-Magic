package nstv.bluetoothmagic.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nstv.bluetoothmagic.ui.theme.Grid
import java.util.UUID

@Composable
fun BluetoothCharacteristic(
    item: Pair<UUID, String>,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(text = "id:  ${item.first}")
        Text(text = "value:  ${item.second}")
        HorizontalDivider(Modifier.height(Grid.Single))
    }
}