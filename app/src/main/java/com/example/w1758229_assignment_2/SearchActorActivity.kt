package com.example.w1758229_assignment_2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.w1758229_assignment_2.data.MovieDao
import com.example.w1758229_assignment_2.data.MovieDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

// w1758229
// Jonathan Carrillo-Sanchez

class SearchActorActivity: AppCompatActivity() {
    lateinit var search_actor_et: EditText
    lateinit var search_btn: Button
    lateinit var search_actor_tv: TextView

    var MY_API_KEY: String? = null
    var savedActorResults: String? = "" // string to restore textview after orientation change

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_actor)
        search_actor_et = findViewById(R.id.search_actor_et)
        search_btn = findViewById(R.id.searchActor_btn)
        search_actor_tv = findViewById(R.id.search_actor_tv)

        MY_API_KEY = resources.getString(R.string.MY_API_KEY)

        //create the database
        val db = Room.databaseBuilder(this, MovieDatabase::class.java, "Movie_Bank_Database").build()
        // get movie data access object
        val movieDao = db.getDao()

        if (savedInstanceState != null){
            savedActorResults = savedInstanceState.getString("savedActorResults","None")
            search_actor_tv.setText(savedActorResults)

        }

        search_btn.setOnClickListener {
            if (searchForActor(movieDao) && !search_actor_et.text.equals("")) Toast.makeText(search_btn.context,"Actors found",Toast.LENGTH_LONG).show()
            else Toast.makeText(search_btn.context,"Couldn't find any actors",Toast.LENGTH_LONG).show()

        }
    }
    /* This method returns a boolean value that signifies whether the application was able to find
    * an actor or not, depending on what the user entered in the EditText. The user input and database
    * strings are converted to lowercase for easier and more accurate searching to provide the user
    * with more precise results.*/
    private fun searchForActor(dao: MovieDao): Boolean {
        search_actor_tv.text = ""
        var foundActor = false
        var userInput = search_actor_et.text.toString().lowercase().trim()
        runBlocking {
            launch {
                val allMovies = dao.getAllMovies()
                for (movie in allMovies) {
                    // if a movie with the same title as the one being added to the DB exists, return false
                    if (movie.actors?.lowercase()?.contains(userInput) == true) {
                        search_actor_tv.append("Title: " + movie.title + "\nYear: " + movie.year +
                        "\nActors: " + movie.actors)
                        search_actor_tv.append("\n")
                        search_actor_tv.append("\n")
                        foundActor = true
                    }
                }
            }
        }
        return foundActor
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("savedActorResults",search_actor_tv.text.toString())
    }
}