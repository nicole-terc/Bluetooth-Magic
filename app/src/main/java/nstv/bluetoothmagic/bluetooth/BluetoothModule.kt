package nstv.bluetoothmagic.bluetooth

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import androidx.bluetooth.BluetoothLe
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BluetoothModule {

    @Provides
    @Singleton
    fun providesBluetoothManager(application: Application): BluetoothManager =
        application.getSystemService(BluetoothManager::class.java)

    @Provides
    @Singleton
    fun providesBluetoothAdapter(bluetoothManager: BluetoothManager): BluetoothAdapter =
        bluetoothManager.adapter

    @Provides
    @Singleton
    fun providesBluetoothScanner(bluetoothAdapter: BluetoothAdapter): BluetoothLeScanner =
        bluetoothAdapter.bluetoothLeScanner

    @Provides
    @Singleton
    fun providesBluetoothLe(@ApplicationContext context: Context) = BluetoothLe(context)
}