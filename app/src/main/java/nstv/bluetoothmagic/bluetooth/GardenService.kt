package nstv.bluetoothmagic.bluetooth

import androidx.bluetooth.GattCharacteristic
import androidx.bluetooth.GattService
import java.util.UUID

object GardenService {
    //No value
    val serviceUUID: UUID = UUID.fromString("879d9eeb-0b6e-4d57-8473-03ce06a62067")
    val giveMushroomUUID: UUID = UUID.fromString("4ebe810b-0225-4d51-8863-8f8ecc9f8546")
    val mushroomToGetUUID: UUID = UUID.fromString("28164b30-572a-477d-8cf8-2c8da1585f26")

    private fun getGiveMushroomCharacteristic() =
        GattCharacteristic(
            giveMushroomUUID,
            GattCharacteristic.PROPERTY_WRITE or GattCharacteristic.PROPERTY_READ,
        )

    private fun getMushroomToGetCharacteristic() =
        GattCharacteristic(
            mushroomToGetUUID,
            GattCharacteristic.PROPERTY_READ,
        )

    fun getGattService(): GattService {
        return GattService(
            serviceUUID,
            listOf(
                getMushroomToGetCharacteristic(),
                getGiveMushroomCharacteristic(),
            )
        )
    }
}