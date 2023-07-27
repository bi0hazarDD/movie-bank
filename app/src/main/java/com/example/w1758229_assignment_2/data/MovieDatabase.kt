package com.example.w1758229_assignment_2.data

import androidx.room.Database
import androidx.room.RoomDatabase

/* contains the database holder, main purpose is to serve as an access point for the connection between
   the app's data and the database */
@Database(entities = [Movie::class],version = 1)
abstract class MovieDatabase: RoomDatabase() {
    abstract fun getDao(): MovieDao
}