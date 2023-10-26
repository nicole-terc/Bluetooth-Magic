package nstv.bluetoothmagic.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import nstv.bluetoothmagic.data.local.Ingredient
import nstv.bluetoothmagic.data.local.IngredientCountDao
import nstv.bluetoothmagic.data.local.toIngredient
import nstv.bluetoothmagic.di.IoDispatcher
import javax.inject.Inject

class GetAllIngredientsUseCase @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val ingredientCountDao: IngredientCountDao,
) {
    operator fun invoke(): Flow<List<Ingredient>> {
        return ingredientCountDao.getAll()
            .map { list ->
                list.map { it.toIngredient() }
            }.flowOn(ioDispatcher)
    }
}