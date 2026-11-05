package ru.urfu.starwars.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoritePersonEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
}
