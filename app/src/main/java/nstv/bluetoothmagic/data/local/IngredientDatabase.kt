package nstv.bluetoothmagic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [IngredientCount::class], version = 1, exportSchema = false)
abstract class IngredientDatabase : RoomDatabase() {
    abstract fun ingredientCountDao(): IngredientCountDao
}