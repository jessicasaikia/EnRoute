package com.example.myapplication2

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class MainActivity : ComponentActivity() {
    private lateinit var mapView: MapView
    private lateinit var databaseHelper: DatabaseHelper
    private val client = OkHttpClient()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sourceInput: EditText
    private lateinit var destinationInput: EditText
    private lateinit var transactionTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val transactionLayout = layoutInflater.inflate(R.layout.transactionactivity, null)
        transactionTextView = transactionLayout.findViewById(R.id.transaction_text_view)
        val accountName = "nanamikent0"  // Replace with your Hive account name
        val fetchTransactionButton: Button = findViewById(R.id.button_transaction)
        fetchTransactionButton.setOnClickListener {
            val intent = Intent(this, TransactionActivity::class.java)
            startActivity(intent)
        }

        sourceInput = findViewById(R.id.source_input)
        destinationInput = findViewById(R.id.destination_input)

        Configuration.getInstance().userAgentValue = packageName

        mapView = findViewById(R.id.map)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(7.0)

        val assamCenter = GeoPoint(26.2006, 92.9376)  // Coordinates of Assam
        mapView.controller.setCenter(assamCenter)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        databaseHelper = DatabaseHelper(this)

        val railwayButton: Button = findViewById(R.id.button_railway)
        val busButton: Button = findViewById(R.id.button_bus)
        val magictracker: Button = findViewById(R.id.button_magic_tracker)


        railwayButton.setOnClickListener {
            fetchRailwayStations()
        }
        busButton.setOnClickListener {
            fetchBusStops()
        }
        magictracker.setOnClickListener {
            fetchBusStops()
        }
    }

    private fun fetchRailwayStations() {
        val url = "https://overpass-api.de/api/interpreter?data=[out:json];area[name=\"Assam\"];node[railway=station](area);out;"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (responseData != null) {
                    parseAndDisplayStations(responseData)
                }
            }
        })
    }

    private fun fetchBusStops() {
        val url = "https://overpass-api.de/api/interpreter?data=[out:json];area[name=\"Assam\"];node[highway=bus_stop](area);out;"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (responseData != null) {
                    parseAndDisplayStations(responseData)
                }
            }
        })
    }

    private fun parseAndDisplayStations(jsonData: String) {
        val jsonObject = JSONObject(jsonData)
        val elements = jsonObject.getJSONArray("elements")

        for (i in 0 until elements.length()) {
            val station = elements.getJSONObject(i)
            val lat = station.getDouble("lat")
            val lon = station.getDouble("lon")
            val name = station.optJSONObject("tags")?.optString("name", "Unnamed Station") ?: "Unnamed Station"

            runOnUiThread {
                val stationMarker = Marker(mapView)
                stationMarker.position = GeoPoint(lat, lon)
                stationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                stationMarker.title = name
                mapView.overlays.add(stationMarker)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
