package nstv.composetemplate.data.remote

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.util.DebugLogger
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Named
import nstv.composetemplate.BuildConfig
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {

    @Provides
    @Singleton
    @Named("authInterceptor")
    fun providesAuthInterceptor(): Interceptor = Interceptor { chain ->
        val request = chain.request()
        val url = request.url.newBuilder().addQueryParameter("api_key", BuildConfig.API_KEY).build()
        val newRequest = request.newBuilder().url(url).build()
        chain.proceed(newRequest)
    }

    @Provides
    @Singleton
    fun okHttpCallFactory(@Named("authInterceptor") authInterceptor: Interceptor): Call.Factory =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor()
                    .apply {
                        if (BuildConfig.DEBUG) {
                            setLevel(HttpLoggingInterceptor.Level.BODY)
                        }
                    },
            ).addInterceptor(authInterceptor)
            .build()

    @Provides
    @Singleton
    fun providesNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun providesRetrofit(networkJson: Json, callFactory: Call.Factory): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
            .callFactory(callFactory)
            .build()

    @Provides
    @Singleton
    fun providesApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun providesImageLoader(
        @ApplicationContext application: Context,
        okhttpFactory: Call.Factory
    ): ImageLoader =
        ImageLoader.Builder(application)
            .callFactory(okhttpFactory)
            .components {
                add(SvgDecoder.Factory())
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }.respectCacheHeaders(false)
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }.build()
}