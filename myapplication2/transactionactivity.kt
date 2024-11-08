package com.example.myapplication2

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class TransactionActivity : ComponentActivity() {
    private val client = OkHttpClient()
    private lateinit var transactionTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transactionactivity)  // Use the new layout for this activity

        transactionTextView = findViewById(R.id.transaction_text_view)

        val accountName = "nanamikent0"  // Replace with the Hive account name
        fetchHiveTransaction(accountName)
    }

    private fun fetchHiveTransaction(accountName: String) {
        val url = "https://api.hive.blog"  // Hive API endpoint
        val requestBody = """
        {
            "jsonrpc": "2.0",
            "method": "account_history_api.get_account_history",
            "params": {
                "account": "$accountName",
                "start": -1,
                "limit": 10
            },
            "id": 1
        }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .post(RequestBody.create("application/json".toMediaTypeOrNull(), requestBody))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@TransactionActivity, "Failed to fetch transaction", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    response.use {
                        if (!response.isSuccessful) {
                            runOnUiThread {
                                Toast.makeText(this@TransactionActivity, "Error: ${response.message}", Toast.LENGTH_SHORT).show()
                            }
                            return
                        }

                        val responseData = response.body?.string()

                        if (responseData != null) {
                            try {
                                val jsonObject = JSONObject(responseData)
                                val resultObject = jsonObject.optJSONObject("result")

                                if (resultObject != null && resultObject.has("history")) {
                                    val resultArray = resultObject.getJSONArray("history")
                                    val transactionDetails = StringBuilder()

                                    for (i in 0 until resultArray.length()) {
                                        val transaction = resultArray.getJSONArray(i).getJSONObject(1).getJSONObject("op")
                                        val type = transaction.getString("type")

                                        // Check if the 'timestamp' exists
                                        val timestamp = if (transaction.has("timestamp")) {
                                            transaction.getString("timestamp")
                                        } else {
                                            "Timestamp not available"
                                        }

                                        transactionDetails.append("Transaction $i: $type\nTimestamp: $timestamp\n\n")
                                    }

                                    runOnUiThread {
                                        transactionTextView.text = transactionDetails.toString()
                                        Toast.makeText(this@TransactionActivity, "Transactions fetched successfully", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    runOnUiThread {
                                        Toast.makeText(this@TransactionActivity, "No transaction data found", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                runOnUiThread {
                                    Toast.makeText(this@TransactionActivity, "Error parsing transaction data: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@TransactionActivity, "No data received from API", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@TransactionActivity, "Failed to fetch transaction", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
