package nstv.bluetoothmagic.domain

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import nstv.bluetoothmagic.data.local.IngredientCombinations
import nstv.bluetoothmagic.data.local.IngredientCountDao
import nstv.bluetoothmagic.data.local.toIngredientCount
import nstv.bluetoothmagic.di.IoDispatcher
import javax.inject.Inject

class AddOneToIngredientCount @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val ingredientCountDao: IngredientCountDao,
) {

    suspend operator fun invoke(id: Int) = withContext(ioDispatcher) {
        Log.d("AddOneToIngredientCount", "invoke: $id")
        var currentCount = 0
        ingredientCountDao.getIngredient(id)?.let { currentCount = it.count } ?: {
            Log.d("AddOneToIngredientCount", "invoke: $id not found, creating new")
            IngredientCombinations.getIngredientForId(id)?.let {
                Log.d("AddOneToIngredientCount", "addingIngredient $it")
                ingredientCountDao.insertIngredientCount(it.toIngredientCount())
            }
        }
        ingredientCountDao.updateIngredientCount(id, currentCount + 1)
    }
}