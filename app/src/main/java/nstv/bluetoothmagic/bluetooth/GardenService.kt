package nstv.bluetoothmagic.bluetooth

import android.bluetooth.BluetoothGattCharacteristic
import androidx.bluetooth.GattCharacteristic
import androidx.bluetooth.GattService
import java.util.UUID

object GardenService {
    val serviceUUID: UUID = UUID.fromString("879d9eeb-0b6e-4d57-8473-03ce06a62067")
    val headerCharacteristicUUID: UUID = UUID.fromString("4ebe810b-0225-4d51-8863-8f8ecc9f8546")
    val ingredientCharacteristicUUID: UUID = UUID.fromString("28164b30-572a-477d-8cf8-2c8da1585f26")

    private fun serviceHeaderProperties() =
        BluetoothGattCharacteristic(
            headerCharacteristicUUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_WRITE or BluetoothGattCharacteristic.PERMISSION_READ,
        )

    private fun getInformationCharacteristic() =
        BluetoothGattCharacteristic(
            ingredientCharacteristicUUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_WRITE or BluetoothGattCharacteristic.PERMISSION_READ,
        )

    fun getGattService(): GattService {
        val headerCharacteristic = serviceHeaderProperties()
        val dataCharacteristic = getInformationCharacteristic()
        return GattService(
            serviceUUID,
            listOf(
                GattCharacteristic(headerCharacteristic.uuid, headerCharacteristic.properties),
                GattCharacteristic(dataCharacteristic.uuid, dataCharacteristic.properties)
            )
        )
    }
}