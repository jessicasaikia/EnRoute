package com.example.myapplication2

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity

class LoginActivity : ComponentActivity() {
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var hiveLoginButton: Button
    private lateinit var LoginButton: Button

    companion object {
        private const val LOGIN_REQUEST_CODE = 1
        private const val HIVE_KEYCHAIN_PACKAGE = "com.hive.keychain.mobile"
        private const val HIVE_LOGIN_ACTION = "hive.keychain.intent.LOGIN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)


        usernameInput = findViewById(R.id.editTextUsername)
        passwordInput = findViewById(R.id.editTextPassword)
        LoginButton = findViewById(R.id.buttonLogin)
        hiveLoginButton = findViewById(R.id.loginButton)

        LoginButton.setOnClickListener {
            performLocalLogin() // Call local login check here
        }

        hiveLoginButton.setOnClickListener {
            val accountName = usernameInput.text.toString().trim()
            if (accountName.isNotEmpty()) {
                if (isHiveKeychainInstalled()) {
                    initiateHiveKeychainLogin(accountName)
                } else {
                    Toast.makeText(this, "Hive Keychain app is not installed", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter your Hive account name", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun performLocalLogin() {
        val username = usernameInput.text.toString()
        val password = passwordInput.text.toString()

        // Check if both username and password are "debo"
        if (username == "debo" && password == "debo") {
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

            // Navigate to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close the LoginActivity
        } else {
            Toast.makeText(this, "Login failed. Try again.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun isHiveKeychainInstalled(): Boolean {
        return try {
            packageManager.getPackageInfo(HIVE_KEYCHAIN_PACKAGE, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("LoginActivity", "Hive Keychain app is not installed", e)
            false
        }
    }

    private fun initiateHiveKeychainLogin(accountName: String) {
        val loginIntent = Intent(HIVE_LOGIN_ACTION).apply {
            setPackage(HIVE_KEYCHAIN_PACKAGE)
            putExtra("account", accountName)
            putExtra("message", "Authorize login request")
        }

        try {
            startActivityForResult(loginIntent, LOGIN_REQUEST_CODE)
        } catch (e: ActivityNotFoundException) {
            Log.e("LoginActivity", "Hive Keychain login intent failed", e)
            Toast.makeText(this, "Failed to launch Hive Keychain app", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                val response = data.getStringExtra("response")
                if (response != null) {
                    Log.d("HiveKeychainLogin", "Login response: $response")
                    Toast.makeText(this, "Hive Keychain login successful", Toast.LENGTH_SHORT).show()

                    // Navigate to the main activity after successful login
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "No response from Hive Keychain", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Hive Keychain login canceled or failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
