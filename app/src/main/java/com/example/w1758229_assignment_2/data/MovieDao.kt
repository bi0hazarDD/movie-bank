package com.example.w1758229_assignment_2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// contains the methods used for accessing the database
@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMovie(movie: Movie) // use suspend because we will use coroutines

    @Query("SELECT * FROM movies_table ORDER BY id ASC")
    suspend fun getAllMovies(): List<Movie>

    @Query("DELETE FROM movies_table")
    suspend fun deleteAll()

    @Query("SELECT MAX(id) FROM movies_table")
    suspend fun getHighestId(): Int

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg movie: Movie)
}