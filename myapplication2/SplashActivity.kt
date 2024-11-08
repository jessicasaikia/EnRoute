package com.example.myapplication2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.ComponentActivity

class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_splash)


        Handler().postDelayed({

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}
