package nstv.bluetoothmagic.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientCountDao {

    @Query("SELECT * FROM $INGREDIENT_TABLE_NAME")
    fun getAll(): Flow<List<IngredientCount>>

    @Query("SELECT * FROM $INGREDIENT_TABLE_NAME WHERE isMainIngredient = 1 LIMIT 1")
    fun getMainIngredient(): IngredientCount?

    @Query("UPDATE $INGREDIENT_TABLE_NAME SET isMainIngredient = 1 WHERE id = :id")
    fun updateMainIngredient(id: Int)

    @Query("SELECT count FROM $INGREDIENT_TABLE_NAME WHERE id = :id")
    fun getIngredientCount(id: Int): Int?

    @Query("UPDATE $INGREDIENT_TABLE_NAME SET count = :count WHERE id = :id")
    fun updateIngredientCount(id: Int, count: Int)

    @Insert
    fun insertIngredientCount(ingredientCount: IngredientCount)

    @Insert
    fun insertIngredientCounts(ingredientCounts: List<IngredientCount>)

    @Delete
    fun deleteIngredientCount(ingredientCount: IngredientCount)
}