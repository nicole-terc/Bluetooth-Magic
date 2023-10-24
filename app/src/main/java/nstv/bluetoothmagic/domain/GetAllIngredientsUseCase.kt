package nstv.bluetoothmagic.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import nstv.bluetoothmagic.data.local.Ingredient
import nstv.bluetoothmagic.data.local.IngredientCombinations
import nstv.bluetoothmagic.data.local.IngredientCountDao
import nstv.bluetoothmagic.data.local.toIngredient
import nstv.bluetoothmagic.di.IoDispatcher
import javax.inject.Inject

class GetAllIngredientsUseCase @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val ingredientCountDao: IngredientCountDao,
) {
    operator fun invoke(): Flow<List<Ingredient>> =
        ingredientCountDao.getAll()
            .map { list ->
                list.map { it.toIngredient() }
            }.flowOn(ioDispatcher)


//            .onStart {
//                getMainIngredientIdUseCase()
//            }
//            .map { ingredientCounts ->
//                IngredientCombinations.list.map { ingredientCombination ->
//                    val ingredientCountForCombination =
//                        ingredientCounts.find { it.id == ingredientCombination.id }
//                    ingredientCombination.copy(
//                        count = ingredientCountForCombination?.count ?: 0,
//                        isMainIngredient = ingredientCountForCombination?.isMainIngredient ?: false,
//                    )
//                }.sortedBy { it.isMainIngredient }
//            }.flowOn(ioDispatcher)
}