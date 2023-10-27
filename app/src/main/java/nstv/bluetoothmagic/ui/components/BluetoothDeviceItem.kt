package nstv.bluetoothmagic.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import nstv.bluetoothmagic.bluetooth.data.ScannedDevice
import nstv.bluetoothmagic.ui.theme.Grid

@Composable
fun BluetoothDeviceItem(
    item: ScannedDevice,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier
            .padding(Grid.Half)
            .clickable { onClick() }) {
        Text(text = "name ${item.deviceName}")
//        Text(text = item.deviceAddress)
//        Text(text = "deviceId: ${item.deviceId}", maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(text = "BondState ${item.bondState.name}")
        HorizontalDivider(Modifier.height(Grid.Single))
    }
}
