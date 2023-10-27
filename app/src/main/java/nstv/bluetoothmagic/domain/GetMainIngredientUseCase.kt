package nstv.bluetoothmagic.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import nstv.bluetoothmagic.data.local.Ingredient
import nstv.bluetoothmagic.data.local.IngredientCombinations
import nstv.bluetoothmagic.data.local.IngredientCountDao
import nstv.bluetoothmagic.data.local.toIngredient
import nstv.bluetoothmagic.data.local.toIngredientCount
import nstv.bluetoothmagic.di.IoDispatcher
import javax.inject.Inject

class GetMainIngredientUseCase @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val insertAllMushrooms: InsertAllMushrooms,
    private val ingredientCountDao: IngredientCountDao,
) {
    suspend operator fun invoke(): Ingredient = withContext(ioDispatcher) {
        var mainIngredient = ingredientCountDao.getMainIngredient()

        if (mainIngredient == null) {
            val tempMainIngredient = IngredientCombinations.getRandom()
            insertAllMushrooms(tempMainIngredient)
            mainIngredient = tempMainIngredient.toIngredientCount()
        }

        return@withContext mainIngredient.toIngredient()
    }
}