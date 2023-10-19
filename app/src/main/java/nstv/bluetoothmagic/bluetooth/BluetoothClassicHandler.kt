package nstv.bluetoothmagic.bluetooth

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val REQUEST_ENABLE_BT: Int = 77777

@Singleton
class BluetoothClassicHandler @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter,
    @ApplicationContext private val context: Context,
) {


    fun isBluetoothEnabled(): Boolean = bluetoothAdapter.isEnabled

    @SuppressLint("MissingPermission")
    fun scan() {
        bluetoothAdapter.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothAdapter.cancelDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun startEnableBluetoothIntent(activity: Activity) {
        if (!isBluetoothEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }
}