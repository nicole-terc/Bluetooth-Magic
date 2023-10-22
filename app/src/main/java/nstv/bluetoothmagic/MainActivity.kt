package nstv.bluetoothmagic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import nstv.bluetoothmagic.bluetooth.BluetoothStateActivitySubscriber
import nstv.bluetoothmagic.ui.screen.MainContent
import nstv.bluetoothmagic.ui.theme.BluetoothMagicTheme
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var bluetoothStateActivitySubscriber: BluetoothStateActivitySubscriber

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.lifecycle.addObserver(bluetoothStateActivitySubscriber)
        setContent {
            BluetoothMagicTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent()
                }
            }
        }
    }
}