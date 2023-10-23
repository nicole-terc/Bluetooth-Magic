package nstv.bluetoothmagic.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import nstv.bluetoothmagic.data.local.IngredientCombinations
import nstv.bluetoothmagic.data.local.IngredientCountDao
import nstv.bluetoothmagic.data.local.toIngredientCount
import nstv.bluetoothmagic.di.IoDispatcher
import javax.inject.Inject

class GetMainIngredientIdUseCase @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val ingredientCountDao: IngredientCountDao,
) {

    suspend operator fun invoke(): Int = withContext(ioDispatcher) {
        var mainIngredient = ingredientCountDao.getMainIngredient()

        if (mainIngredient == null) {
            mainIngredient =
                IngredientCombinations.getRandom().copy(count = 1, isMainIngredient = true)
                    .toIngredientCount()
            ingredientCountDao.insertIngredientCount(mainIngredient)
        }

        return@withContext mainIngredient.id
    }
}