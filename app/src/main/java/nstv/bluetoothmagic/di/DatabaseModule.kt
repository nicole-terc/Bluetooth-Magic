package nstv.bluetoothmagic.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import nstv.bluetoothmagic.data.local.IngredientDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): IngredientDatabase =
        Room.databaseBuilder(
            context,
            IngredientDatabase::class.java,
            "ingredient_database"
        ).build()

    @Singleton
    @Provides
    fun provideIngredientDao(database: IngredientDatabase) = database.ingredientCountDao()
}