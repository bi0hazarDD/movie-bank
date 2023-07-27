package com.example.w1758229_assignment_2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

// w1758229
// Jonathan Carrillo-Sanchez

class SearchWebServiceActivity : AppCompatActivity() {

    lateinit var search_movie_et: EditText
    lateinit var search_Movie_Web_btn: Button
    lateinit var search_Movie_Web_tv: TextView
    var url_string: String? = null
    var MY_API_KEY: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_webservice)

        search_movie_et = findViewById(R.id.search_movie_et)
        search_Movie_Web_btn = findViewById(R.id.search_Movie_Web_btn)
        search_Movie_Web_tv = findViewById(R.id.search_Movie_Web_tv)
        if (savedInstanceState != null) {
            var movieResultsString = savedInstanceState.getString("movieResults")
            search_Movie_Web_tv.text = movieResultsString
        }

        MY_API_KEY = resources.getString(R.string.MY_API_KEY)

        search_Movie_Web_btn.setOnClickListener {
            getWebMovie()
        }


    }

    private fun getWebMovie() {
        // https://www.omdbapi.com/?s=mat&apikey=80eda3fc
        val movieName = search_movie_et!!.text.toString().trim()
        url_string = "https://www.omdbapi.com/?s=" + movieName + "&apikey=" + MY_API_KEY
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

                } catch (e: Exception) {
                    println("Exception caught: " + e)
                }


            }
            //display the search results to the user of the movie
            search_Movie_Web_tv.text = data
            print("data: " + data)
            /* If the movie name the user typed is cannot be retrieved from the web service,
            * do not */

        }

    }

    private fun parseJSON(dataJSON: StringBuilder): String {
        var movieResultsFromJSON = StringBuilder("")
        try {
            val obj = JSONObject(dataJSON.toString())
            val moviesArray = obj.getJSONArray("Search")
            for (i in 0 until moviesArray.length()) {
                var objectJSON = moviesArray.getJSONObject(i)
                movieResultsFromJSON.append("Title: " + objectJSON.getString("Title") + "\n")
                movieResultsFromJSON.append("Year: " + objectJSON.getString("Year") + "\n\n")
            }
        } catch (e: Exception) {
            println("Exception: $e")
        }


        return movieResultsFromJSON.toString().trim()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("movieResults",search_Movie_Web_tv.text.toString())

    }
}