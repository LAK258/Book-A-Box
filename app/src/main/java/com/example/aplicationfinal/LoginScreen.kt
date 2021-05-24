package com.example.aplicationfinal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.example.aplicationfinal.databinding.ActivityLoginBinding
const val USERNAME = "com.example.PhoneLinear.USERNAME" // a constant created to pass a variable into another class

class LoginScreen : AppCompatActivity() {

    lateinit var binding : ActivityLoginBinding // creates a variable to be initialized later

    override fun onCreate(savedInstanceState: Bundle?) { // creates the layout
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater) // connects the binding variable to the layout file
        setContentView(binding.root) // set which layout file to view

        var userName : String? = null // variable for the username initialized as a null and made nullable with the "?" at the end

        binding.login.setOnClickListener {  // determines what happens when you click on the login button
            userName = binding.username.text.toString() // converts the CharSequence received input to a String

            if (!userName.isNullOrBlank()){ // checks if there is a valid username
                Toast.makeText(applicationContext, "Welcome $userName", Toast.LENGTH_SHORT).show() // a pop-up notification
                val loginIntent = Intent(this, BookingScreen::class.java).apply {  // creates an intent that creates a new class
                    putExtra(USERNAME, userName) //additional parameters passed into a new class
                }
                this.startActivity(loginIntent) // starts the create class intent
            }else {
                Toast.makeText(applicationContext, "Please input a valid username", Toast.LENGTH_SHORT).show()
            }
        }
    }
}