package ru.urfu.starwars.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {

    @Query("SELECT * FROM favorite_people ORDER BY added_at DESC")
    fun observeAll(): Flow<List<FavoritePersonEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_people WHERE id = :id)")
    fun observeIsFavorite(id: Int): Flow<Boolean>

    @Query("SELECT * FROM favorite_people WHERE id = :id")
    suspend fun getById(id: Int): FavoritePersonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoritePersonEntity)

    @Query("DELETE FROM favorite_people WHERE id = :id")
    suspend fun delete(id: Int)
}
