package com.example.w1758229_assignment_2

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.room.Room
import com.example.w1758229_assignment_2.data.Movie
import com.example.w1758229_assignment_2.data.MovieDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
// w1758229
// Jonathan Carrillo-Sanchez
/* References
* Animated Background: https://www.youtube.com/watch?v=4lEnLTqsnaw&ab_channel=PracticalCoding */

class MainActivity : AppCompatActivity() {

    lateinit var addBtn: Button
    lateinit var searchMovieBtn: Button
    lateinit var searchActorBtn: Button
    lateinit var searchWebServiceBtn: Button

    var counter: Int = 0// unique value for primary key
    var addedToDatabase: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addBtn = findViewById(R.id.bt1)
        searchMovieBtn = findViewById(R.id.bt2)
        searchActorBtn = findViewById(R.id.bt3)
        searchWebServiceBtn = findViewById(R.id.bt4)

        var constraintLayout: ConstraintLayout = findViewById(R.id.mainLayout)
        /* Creating animation for MainActivity background effect,
        * created different gradient resource files and entered them into a list */
        var animatedBackground: AnimationDrawable = constraintLayout.background as AnimationDrawable
        animatedBackground.setEnterFadeDuration(2500)
        animatedBackground.setExitFadeDuration(5000)
        animatedBackground.start()

        if (savedInstanceState != null) {
            addedToDatabase = savedInstanceState.getBoolean("addedToDatabase",false)
            if (addedToDatabase) {
                addBtn.isEnabled = false
            }
        }

        addBtn.setOnClickListener {
            insertSomeMovies()
        }
        searchMovieBtn.setOnClickListener {
            Intent(this, SearchMovieActivity::class.java).also {
                startActivity(it)
            }
        }
        searchActorBtn.setOnClickListener {
            Intent(this, SearchActorActivity::class.java).also {
                startActivity(it)
            }
        }
        searchWebServiceBtn.setOnClickListener {
            Intent(this, SearchWebServiceActivity::class.java).also {
                startActivity(it)
            }
        }
    }
    /* this method inserts 5 movies into the local database via the Room library following from
    * the specification provided. A counter is used to increment and enter the unique identifiers,
    * in this case the id. */
    private fun insertSomeMovies() {
        //create the database
        val db = Room.databaseBuilder(this, MovieDatabase::class.java, "Movie_Bank_Database").build()
        // get movie data access object
        val movieDao = db.getDao()

        runBlocking {
            launch {
                // create movie entry and insert it into database
                ++counter
                val movie1 = Movie(
                    counter,
                    "The Shawshank Redemption",
                    "1994",
                    "R",
                    "14 Oct 1994",
                    "142 min",
                    "Drama",
                    "Frank Darabont",
                    "Stephen King, Frank Darabont",
                    "Tim Robbins, Morgan Freeman, Bob Gunton",
                    "Two imprisoned men bond over a number of years, finding solace\n" +
                            "and eventual redemption through acts of common decency.")
                ++counter
                val movie2 = Movie(
                    counter,
                    "Batman: The Dark Knight Returns, Part 1",
                    "2012",
                    "PG-13",
                    "25 Sep 2012",
                    "76 min",
                    "Animation, Action, Crime, Drama, Thriller",
                    "Jay Oliva",
                    "Bob Kane (character created by: Batman), Frank Miller (comic book), Klaus Janson (comic book), Bob\n" +
                            "Goodman",
                    "Peter Weller, Ariel Winter, David Selby, Wade Williams",
                    "Batman has not been seen for ten years. A new breed\n" +
                            "of criminal ravages Gotham City, forcing 55-year-old Bruce Wayne back\n" +
                            "into the cape and cowl. But, does he still have what it takes to fight\n" +
                            "crime in a new era?")
                ++counter
                val movie3 = Movie(
                    counter,
                    "The Lord of the Rings: The Return of the King",
                    "2003",
                    "PG-13",
                    "17 Dec 2003",
                    "201 min",
                    "Action, Adventure, Drama",
                    "Peter Jackson",
                    "J.R.R. Tolkien, Fran Walsh, Philippa Boyens",
                    "Elijah Wood, Viggo Mortensen, Ian McKellen",
                    "Gandalf and Aragorn lead the World of Men against Sauron's\n" +
                            "army to draw his gaze from Frodo and Sam as they approach Mount Doom\n" +
                            "with the One Ring.")
                ++counter
                val movie4 = Movie(
                    counter,
                    "Inception",
                    "2010",
                    "PG-13",
                    "16 Jul 2010",
                    "148 min",
                    "Action, Adventure, Sci-Fi",
                    "Christopher Nolan",
                    "Christopher Nolan",
                    "Leonardo DiCaprio, Joseph Gordon-Levitt, Elliot Page",
                    "A thief who steals corporate secrets through the use of\n" +
                            "dream-sharing technology is given the inverse task of planting an idea\n" +
                            "into the mind of a C.E.O., but his tragic past may doom the project\n" +
                            "and his team to disaster.")
                ++counter
                val movie5 = Movie(
                    counter,
                    "The Matrix",
                    "1999",
                    "R",
                    "31 Mar 1999",
                    "136 min",
                    "Action, Sci-Fi",
                    "Lana Wachowski, Lilly Wachowski",
                    "Lilly Wachowski, Lana Wachowski",
                    "Keanu Reeves, Laurence Fishburne, Carrie-Anne Moss",
                    "When a beautiful stranger leads computer hacker Neo to a\n" +
                            "forbidding underworld, he discovers the shocking truth--the life he\n" +
                            "knows is the elaborate deception of an evil cyber-intelligence.")
                // insert movie objects into database
                movieDao.insertAll(movie1, movie2, movie3, movie4, movie5)
                addBtn.isEnabled = false
                addedToDatabase = true
                Toast.makeText(
                    addBtn.context,
                    "Successfully added movies to Movie_Bank_Database",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("addedToDatabase",addedToDatabase)

    }
}