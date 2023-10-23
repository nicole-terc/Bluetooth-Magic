package nstv.bluetoothmagic.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import nstv.bluetoothmagic.data.local.Ingredient
import nstv.bluetoothmagic.data.local.IngredientCombinations
import nstv.bluetoothmagic.data.local.IngredientCountDao
import nstv.bluetoothmagic.di.IoDispatcher
import javax.inject.Inject

class GetAllIngredientsUseCase @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val ingredientCountDao: IngredientCountDao,
    private val getMainIngredientIdUseCase: GetMainIngredientIdUseCase,
) {
    operator fun invoke(): Flow<List<Ingredient>> = ingredientCountDao.getAll()
        .onStart {
            val mainIngredientId = getMainIngredientIdUseCase()
        }
        .map { ingredientCounts ->
            IngredientCombinations.list.map { ingredientCombination ->
                val ingredientCountForCombination =
                    ingredientCounts.find { it.id == ingredientCombination.id }
                ingredientCombination.copy(
                    count = ingredientCountForCombination?.count ?: 0,
                    isMainIngredient = ingredientCountForCombination?.isMainIngredient ?: false,
                )
            }.sortedBy { it.isMainIngredient }
        }
}