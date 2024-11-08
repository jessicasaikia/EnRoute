package com.example.myapplication2

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


val DB_NAME = "assam_bus_stands.db"
class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, 1) {

    private val DB_PATH = context.getDatabasePath(DB_NAME).absolutePath

    init {
        copyDatabase()
    }

    private fun copyDatabase() {
        // Copy the database from assets if it doesn't exist
        val input: InputStream = context.assets.open(DB_NAME)
        val output: OutputStream = FileOutputStream(DB_PATH)
        input.copyTo(output)
        output.flush()
        output.close()
        input.close()
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // No need to implement since we're copying the DB
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades if necessary
    }

    // Function to get bus stands
    fun getBusStands(): List<BusStand> {
        val busStands = mutableListOf<BusStand>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT name,latitude, longitude FROM bus_stands", null)
        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                val lat = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"))
                val lon = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"))
                busStands.add(BusStand(lat, lon, name))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return busStands
    }
}

// Data class for BusStand
data class BusStand(val latitude: Double, val longitude: Double, val name: String)
