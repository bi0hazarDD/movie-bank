package com.example.w1758229_assignment_2

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.w1758229_assignment_2.data.Movie
import com.example.w1758229_assignment_2.data.MovieDao
import com.example.w1758229_assignment_2.data.MovieDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

// w1758229
// Jonathan Carrillo-Sanchez

/* References
* Help with BitMap: MODULE: (2021) 5COSC023W.2 Mobile Application Development Lecture8_Android_2022
*/

class SearchMovieActivity: AppCompatActivity() {
    var title_edt: EditText? = null
    lateinit var movieResults: TextView
    lateinit var moviePoster: ImageView

    lateinit var searchBtn: Button
    lateinit var addToDBBtn: Button

    var url_string: String? = null
    var MY_API_KEY: String? = null
    var counter: Int = 0
    var title: String? = ""
    var year: String? = null
    var rated: String? = null
    var released: String? = null
    var runtime: String? = null
    var genre: String? = null
    var director: String? = null
    var writer: String? = null
    var actors: String? = null
    var plot: String? = null
    var posterLink: String? = ""
    var bitMapImage: Bitmap? = null
    var response: String? = null
    var isPosterFound: Boolean = false // boolean which identifies whether the searched movie had a poster link
    var isMovieFound: Boolean = false // boolean which identifies whether the searched movie exists on the web service using the "Response" Object via parseJSON method
    var savedMovieResults: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_movie)
        title_edt = findViewById(R.id.textInputEditText)
        movieResults = findViewById(R.id.movie_details)
        moviePoster = findViewById(R.id.movie_poster)
        searchBtn = findViewById(R.id.searchMovie_btn)
        addToDBBtn = findViewById(R.id.addToDB_btn)

        MY_API_KEY = resources.getString(R.string.MY_API_KEY)

        if (savedInstanceState != null) {
            title = savedInstanceState.getString("title","")
            posterLink = savedInstanceState.getString("posterLink","N/A")
            savedMovieResults = savedInstanceState.getString("movieResults","No results")
            isMovieFound = savedInstanceState.getBoolean("isMovieFound",false)
            isPosterFound = savedInstanceState.getBoolean("isPosterFound",false)
            /* restore the bitMapImage. getBitMapPicture() makes use of the posterLink global variable
            *  therefore it is necessary to save this variable to the bundle as seen above.*/
            runBlocking {
                withContext(Dispatchers.IO) {
                    try {
                        bitMapImage = getBitMapPicture()
                    } catch (e: Exception) {
                        println("Exception caught: $e")
                    }

                }
                /* After changing the orientation, check to see if no movie details are being displayed
                * in the text view by checking if the savedMovieResults string is empty. If true,
                * remove the poster image as it will still be there as it is processed again
                * via the getBitMapPicture method above. */
                if (savedMovieResults!!.isEmpty()) moviePoster.setImageResource(0)
                else moviePoster.setImageBitmap(bitMapImage)
            }
            movieResults.text = savedMovieResults
        }

        //create the database
        val db = Room.databaseBuilder(this, MovieDatabase::class.java, "Movie_Bank_Database").build()
        // get movie data access object
        val movieDao = db.getDao()

        searchBtn.setOnClickListener {
            getMovie()
//            println("isPosterFound:" + isPosterFound)
//            println("isMovieFound:" + isMovieFound)
        }

        addToDBBtn.setOnClickListener {
            if (movieResults.text.trim().equals("")) {
                Toast.makeText(addToDBBtn.context
                    ,"Please search for a movie first before attempting to add a movie to the database."
                    ,Toast.LENGTH_LONG).show()
            } else {
                if (movieAlreadyExists(movieDao)) {
                    Toast.makeText(addToDBBtn.context
                        , "Cannot add this movie as it already exists in the database!"
                        ,Toast.LENGTH_LONG).show()
                } else {
                    addMovie(movieDao)
                    Toast.makeText(addToDBBtn.context
                        , "$title was added to the database!"
                        ,Toast.LENGTH_LONG).show()
                }
            }

        }
    }
    /* This method returns a boolean which is a result of a check that is made to search if a movie
    *  that the user is attempting to add to the local SQLite database via the Room Library
    *  exists already or not. Performs a search through all the current movies stored in the database
    *  and compares the titles of the movie being added against the next movie in the database.
    *
    *  This works well within the local database as it creates a consistent database with no duplicate
    *  movies that would otherwise affect other logic implemented in the overall application, e.g.,
    *  searching for an actor if the movie exists twice or three times will appear 2 or 3 times, as
    *  the movie the actor is starred in should only appear once. */
    private fun movieAlreadyExists(dao: MovieDao): Boolean {
        var result: Boolean = false
        runBlocking {
            launch {
                val allMovies = dao.getAllMovies()
                for (movie in allMovies) {
                    // if a movie with the same title as the one being added to the DB exists, return false
                    if (movie.title.equals(title)) {
                        result = true
                    }
                }
            }
        }
        return result
    }

    private fun addMovie(dao: MovieDao) {
        runBlocking {
            launch {
                /* if no movies in database upon launching app, set count to 1, else set count to
                 highest count + 1 */
                counter = if (dao.getAllMovies().isEmpty()) 1 else dao.getHighestId() + 1
                // insert current movie user has searched for into database
                dao.addMovie(Movie(
                    counter,
                    title,
                    year,
                    rated,
                    released,
                    runtime,
                    genre,
                    director,
                    writer,
                    actors,
                    plot))

            }
        }
    }
    /* This method does all the processing needed to retrieve the searched movie on the web service
    *  making use of the MY_API_KEY that references to a saved resource string which is my personal
    *  API key obtained from making an account on the web service site.
    *
    *  The name of the movie is searched, the poster of the movie is displayed via the ImageView
    *  and the details of the movie are processed and parsed from JSON to String format via the
    *  parseJSON method and then set to the movieResults TextView.
    *
    *  Checks are made to check whether any details are returned from the web service in regards to
    *  the user's search and whether if an existing movie actually has a poster image link available.
    *  If a movie is not found, a Toast appears informing the user to re-type their input.*/
    private fun getMovie() {
        // https://www.omdbapi.com/?t=matrix&apikey=80eda3fc
        val movieName = title_edt!!.text.toString().trim()
        url_string = "https://www.omdbapi.com/?t=" + movieName + "&apikey=" + MY_API_KEY
        // variable of type String named 'data' to set TextView with after JSON is parsing completed
        var data: String = ""

        // start fetch of data on background as cannot connect to network on main thread
        runBlocking {
            withContext(Dispatchers.IO) { // IO thread
                // Collect all of the data returned by the web service in JSON format and store as a string
                var movieData = StringBuilder("")
                //create URL object
                var url = URL(url_string)
                // make the connection
                var con = url.openConnection() as HttpURLConnection
                // ready to collect data and use the "reader" object's input stream line by line
                    try {
                        val reader = BufferedReader(InputStreamReader(con.inputStream))
                        var line = reader.readLine()
                        while (line != null) {
                            movieData.append(line)
                            line = reader.readLine()
                        }
                        // string builder object contains all of the data/ chars in the JSON file from OMDb API
                        data = parseJSON(movieData)
                        bitMapImage = getBitMapPicture()

                    } catch (e: Exception) {
                        println("Exception caught: " + e)
                    }


            }
            //display the search results to the user of the movie
            movieResults.text = data
            /* If the movie name the user typed is cannot be retrieved from the web service,
            * do not */
            if (isMovieFound) {
                if (isPosterFound){
                    moviePoster.setImageBitmap(bitMapImage)
                    isMovieFound = false
                    isPosterFound = false
                } else {
                    moviePoster.setImageResource(R.drawable.not_available)
                }
            } else {
                moviePoster.setImageResource(0)
                Toast.makeText(searchBtn.context
                    , "Error: Movie that was searched cannot be retrieved as it does not exist." +
                            "\nPlease try entering the title of the movie in full."
                    ,Toast.LENGTH_LONG).show()
            }


        }
    }

    private fun getBitMapPicture(): Bitmap {
        var bitmap: Bitmap? = null
        var url = URL(posterLink)
        val con = url.openConnection() as HttpURLConnection
        val bf = BufferedInputStream(con.inputStream)
        bitmap = BitmapFactory.decodeStream(bf)
        return bitmap
    }

    /* Extracts relevant information retrieved from the web service api OMDb API and returns as
        a String
        The argument data is a JSON string, obtained from the web service
     */
    private fun parseJSON(data: StringBuilder): String {
//        moviePoster.setImageResource(android.R.color.transparent)
        // JSON Access Object
        val JAO = JSONObject(data.toString())

        title = JAO["Title"] as String
        year = JAO["Year"] as String
        rated = JAO["Rated"] as String
        released = JAO["Released"] as String
        runtime = JAO["Runtime"] as String
        genre = JAO["Genre"] as String
        director = JAO["Director"] as String
        writer = JAO["Writer"] as String
        actors = JAO["Actors"] as String
        plot = JAO["Plot"] as String
        response = JAO["Response"] as String
        posterLink = JAO["Poster"] as String

        isPosterFound = !(posterLink.equals("N/A"))
        isMovieFound = response.equals("True")

        return "Title: " + title + "\n" + "Year: " + year + "\n" + "Rated: " + rated +
                "\n" + "Released: " + released + "\n" + "Runtime: " + runtime + "\n" + "Genre: " +
                genre + "\n" + "Director: " + director + "\n" + "Writer: " + writer + "\n" + "Actors: " +
                actors + "\n" + "Plot: " + plot
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("movieResults",movieResults.text.toString())
        outState.putString("posterLink",posterLink)
        outState.putString("title",title)
        outState.putBoolean("isMovieFound",isMovieFound)
        outState.putBoolean("isPosterFound",isPosterFound)
    }
}