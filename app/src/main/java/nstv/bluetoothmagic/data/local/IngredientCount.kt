package nstv.bluetoothmagic.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


const val INGREDIENT_TABLE_NAME = "ingredientcount"

@Entity(tableName = INGREDIENT_TABLE_NAME)
data class IngredientCount(
    @PrimaryKey val id: Int,
    val count: Int,
    val isMainIngredient: Boolean,
)