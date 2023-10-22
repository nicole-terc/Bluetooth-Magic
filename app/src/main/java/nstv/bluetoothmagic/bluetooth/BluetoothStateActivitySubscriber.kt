package nstv.bluetoothmagic.bluetooth

import android.bluetooth.BluetoothAdapter
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
import javax.inject.Inject

@ActivityScoped
class BluetoothStateActivitySubscriber @Inject constructor(
    @ActivityContext private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    private val bluetoothStateRepository: BluetoothStateRepository,
) : DefaultLifecycleObserver {

    private val intentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
    private lateinit var bluetoothBroadcastReceiver: BroadcastReceiver

    init {
        (context as? MainActivity)?.lifecycle?.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        bluetoothBroadcastReceiver = createBroadcastReceiver()
        bluetoothStateRepository.updateBluetoothAdapterState(bluetoothAdapter.state)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        ContextCompat.registerReceiver(
            context,
            bluetoothBroadcastReceiver,
            intentFilter,
            RECEIVER_EXPORTED
        )
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        context.unregisterReceiver(bluetoothBroadcastReceiver)
    }

    private fun createBroadcastReceiver() = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                    Log.d(
                        "BluetoothStateActivitySubscriber",
                        "onReceive: ACTION_STATE_CHANGED state=$state"
                    )
                    bluetoothStateRepository.updateBluetoothAdapterState(state)
                }
            }
        }
    }
}