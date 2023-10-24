package nstv.bluetoothmagic.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import nstv.bluetoothmagic.data.local.Ingredient
import nstv.bluetoothmagic.data.local.IngredientCombinations
import nstv.bluetoothmagic.data.local.IngredientCountDao
import nstv.bluetoothmagic.data.local.toIngredientCount
import nstv.bluetoothmagic.di.IoDispatcher
import javax.inject.Inject

class InsertAllMushrooms @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val ingredientCountDao: IngredientCountDao,
) {

    suspend operator fun invoke(mainIngredient: Ingredient = IngredientCombinations.getRandom()) =
        withContext(ioDispatcher) {
            ingredientCountDao.insertIngredientCounts(IngredientCombinations.list.map { it.toIngredientCount() })
            ingredientCountDao.updateMainIngredient(mainIngredient.id)
            ingredientCountDao.updateIngredientCount(mainIngredient.id, 1)
        }
}