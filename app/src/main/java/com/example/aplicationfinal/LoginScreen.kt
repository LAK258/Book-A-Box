package com.example.aplicationfinal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.example.aplicationfinal.databinding.ActivityLoginBinding
const val USERNAME = "com.example.PhoneLinear.USERNAME"

class LoginScreen : AppCompatActivity() {

    lateinit var binding : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var userName : String? = null

        binding.login.setOnClickListener {
            userName = binding.username.text.toString()

            if (!userName.isNullOrBlank()){
                Toast.makeText(applicationContext, "Welcome $userName", Toast.LENGTH_SHORT).show()
                val loginIntent = Intent(this, BookingScreen::class.java).apply {
                    putExtra(USERNAME, userName) //Optional parameters
                }
                this.startActivity(loginIntent)
            }else {
                Toast.makeText(applicationContext, "Please input a valid username", Toast.LENGTH_SHORT).show()
            }
        }
    }
}