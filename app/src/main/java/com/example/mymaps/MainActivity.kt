package com.example.mymaps

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymaps.databinding.ActivityMainBinding
import com.example.mymaps.models.Place
import com.example.mymaps.models.UserMap
import java.io.*

const val EXTRA_USER_MAP = "EXTRA_USER_MAP"
const val EXTRA_MAP_TITLE = "EXTRA_MAP_TITLE"
private const val FILENAME = "UserMaps.data"
private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userMaps : MutableList<UserMap>
    private lateinit var mapAdaptor : MapsAdaptor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userMaps = deserializeUserMaps(this).toMutableList()
//        userMaps.addAll(generateSampleData())
        //set layout manager on RV
        binding.rvMaps.layoutManager = LinearLayoutManager(this)
        //set adapter on RV
        mapAdaptor = MapsAdaptor(this,userMaps, object: MapsAdaptor.OnClickListener {
            override fun onItemClick(position: Int) {
                Log.i(TAG,"onItemClick $position")
                // when clicked on view in RV,navigate to new Activity
                val intent = Intent(this@MainActivity,MapsActivity::class.java)
                intent.putExtra(EXTRA_USER_MAP,userMaps[position])
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left)
            }

        })
        binding.rvMaps.adapter =mapAdaptor

        binding.fabCreateMap.setOnClickListener {
            Log.i(TAG,"tap on FAB")
            showAlertDialog()
        }
    }
    private var resultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // parse result and perform action
            val userMap = result.data?.getSerializableExtra(EXTRA_USER_MAP) as UserMap
            Log.i(TAG,"onActivityResult with new map title ${userMap.title}")
            userMaps.add(userMap)
            mapAdaptor.notifyItemInserted(userMaps.size-1)
            serializeUserMaps(this,userMaps)
        }
    }
    private fun showAlertDialog() {

        val mapFormView = LayoutInflater.from(this).inflate(R.layout.dialog_create_map,null)
        val dialog =
            AlertDialog.Builder(this)
                .setTitle("Map Title")
                .setView(mapFormView)
                .setNegativeButton("Cancel",null)
                .setPositiveButton("OK",null)
                .show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            val title = mapFormView.findViewById<EditText>(R.id.etTitleMap).text.toString()
            if(title.trim().isEmpty() ){
                Toast.makeText(this,"Map must have Non-empty Title", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            //Navigate to create map activity
            val intent = Intent(this@MainActivity, CreateMapActivity::class.java)
            intent.putExtra(EXTRA_MAP_TITLE,title)
            resultLauncher.launch(intent)
            dialog.dismiss()
        }

    }
    private fun serializeUserMaps(context: Context,userMaps: List<UserMap>){
        Log.i(TAG,"serializeUserMaps")
        ObjectOutputStream(FileOutputStream(getDataFile(context))).use { it.writeObject(userMaps) }
    }
    private fun deserializeUserMaps(context: Context):List<UserMap>{
        Log.i(TAG,"deserializeUserMaps")
        val dataFile = getDataFile(context)
        if(!dataFile.exists()){
            Log.i(TAG,"Data file does not exist yet")
            return emptyList()
        }
        ObjectInputStream(FileInputStream(dataFile)).use { return it.readObject() as List<UserMap> }
    }
    private fun getDataFile(context: Context): File {
        Log.i(TAG,"Getting file from drectory ${context.filesDir}")
        return File(context.filesDir,FILENAME)
    }

    private fun generateSampleData(): List<UserMap> {
        return listOf(
            UserMap(
                "Memories from University",
                listOf(
                    Place("Branner Hall", "Best dorm at Stanford", 37.426, -122.163),
                    Place("Gates CS building", "Many long nights in this basement", 37.430, -122.173),
                    Place("Pinkberry", "First date with my wife", 37.444, -122.170)
                )
            ),
            UserMap("January vacation planning!",
                listOf(
                    Place("Tokyo", "Overnight layover", 35.67, 139.65),
                    Place("Ranchi", "Family visit + wedding!", 23.34, 85.31),
                    Place("Singapore", "Inspired by \"Crazy Rich Asians\"", 1.35, 103.82)
                )),
            UserMap("Singapore travel itinerary",
                listOf(
                    Place("Gardens by the Bay", "Amazing urban nature park", 1.282, 103.864),
                    Place("Jurong Bird Park", "Family-friendly park with many varieties of birds", 1.319, 103.706),
                    Place("Sentosa", "Island resort with panoramic views", 1.249, 103.830),
                    Place("Botanic Gardens", "One of the world's greatest tropical gardens", 1.3138, 103.8159)
                )
            ),
            UserMap("My favorite places in the Midwest",
                listOf(
                    Place("Chicago", "Urban center of the midwest, the \"Windy City\"", 41.878, -87.630),
                    Place("Rochester, Michigan", "The best of Detroit suburbia", 42.681, -83.134),
                    Place("Mackinaw City", "The entrance into the Upper Peninsula", 45.777, -84.727),
                    Place("Michigan State University", "Home to the Spartans", 42.701, -84.482),
                    Place("University of Michigan", "Home to the Wolverines", 42.278, -83.738)
                )
            ),
            UserMap("Restaurants to try",
                listOf(
                    Place("Champ's Diner", "Retro diner in Brooklyn", 40.709, -73.941),
                    Place("Althea", "Chicago upscale dining with an amazing view", 41.895, -87.625),
                    Place("Shizen", "Elegant sushi in San Francisco", 37.768, -122.422),
                    Place("Citizen Eatery", "Bright cafe in Austin with a pink rabbit", 30.322, -97.739),
                    Place("Kati Thai", "Authentic Portland Thai food, served with love", 45.505, -122.635)
                )
            )
        )
    }
}