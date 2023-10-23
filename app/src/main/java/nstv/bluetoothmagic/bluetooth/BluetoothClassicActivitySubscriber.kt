package nstv.bluetoothmagic.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.RECEIVER_EXPORTED
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import nstv.bluetoothmagic.MainActivity
import nstv.bluetoothmagic.bluetooth.data.BluetoothAdapterState
import nstv.bluetoothmagic.bluetooth.data.BluetoothStateRepository
import nstv.bluetoothmagic.bluetooth.data.toScannedDevice
import javax.inject.Inject

@ActivityScoped
class BluetoothClassicActivitySubscriber @Inject constructor(
    @ActivityContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    private val bluetoothStateRepository: BluetoothStateRepository,
) : DefaultLifecycleObserver {

    private val intentFilterActionFound = IntentFilter(BluetoothDevice.ACTION_FOUND)
    private lateinit var bluetoothClassicBroadcastReceiver: BroadcastReceiver

    init {
        (context as? MainActivity)?.lifecycle?.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        bluetoothClassicBroadcastReceiver = createBroadcastReceiver()
        bluetoothStateRepository.updateBluetoothAdapterState(bluetoothAdapter.state)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        ContextCompat.registerReceiver(
            context,
            bluetoothClassicBroadcastReceiver,
            intentFilterActionFound,
            RECEIVER_EXPORTED
        )
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        context.unregisterReceiver(bluetoothClassicBroadcastReceiver)
    }

    private fun createBroadcastReceiver() = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    (intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) as? BluetoothDevice)?.let { device ->
                        Log.d(
                            "BluetoothClassicActivitySubscriber",
                            "Found device: ${device.name} - ${device.address}"
                        )
                        val state =
                            bluetoothStateRepository.getCurrentState() as? BluetoothAdapterState.Discovering
                                ?: BluetoothAdapterState.Discovering(
                                    scannedDevices = emptyList()
                                )

                        bluetoothStateRepository.updateBluetoothAdapterState(
                            state.copy(
                                scannedDevices = state.scannedDevices.plus(device.toScannedDevice())
                            )
                        )
                    }
                }
            }
        }
    }
}