package nstv.bluetoothmagic.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import nstv.bluetoothmagic.data.local.IngredientCountDao
import nstv.bluetoothmagic.di.IoDispatcher
import javax.inject.Inject

class AddOneToIngredientCount @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val ingredientCountDao: IngredientCountDao,
) {

    suspend operator fun invoke(id: Int) = withContext(ioDispatcher) {
        val currentCount = ingredientCountDao.getIngredientCount(id) ?: 0
        ingredientCountDao.updateIngredientCount(id, currentCount + 1)
    }
}