package nstv.composetemplate.data.gif

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import nstv.composetemplate.data.remote.ApiService
import nstv.composetemplate.di.IoDispatcher
import javax.inject.Inject

class GifRepository @Inject constructor(
    private val apiService: ApiService,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    fun getTrendingGifs(): Flow<List<Gif>> = flow {
        emit(apiService.getTrendingGifs().data)
    }.flowOn(dispatcher)
}