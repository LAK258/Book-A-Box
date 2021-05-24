package com.example.aplicationfinal

import android.bluetooth.BluetoothSocket
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.aplicationfinal.databinding.ActivityBookingScreenBinding
import java.io.IOException
import kotlin.math.exp

class MuteBoxMaker(val name: TextView, var isBooked: Boolean, val bookingInfo: TextView, val muteBox: ImageView,
                   val book: Button, private val userName: String, val endButton: Button, val backButton: ImageView) { // constructor

    // member fields
    private val preBook = "You have 5 minutes to acquire the MuteBox when booked" // predetermined text
    private val postBookPreCode  = "MuteBox has been reserved\n By: $userName \n" +
            "For: "
    private val acquired = false


    // member functions
    fun expandButton(){ // expands a button and gives it a color based on availability
        if (isBooked){
            muteBox.setImageResource(R.drawable.ic_bigredbox) // sets the color of the button in the form of a image
        }else {
            muteBox.setImageResource(R.drawable.ic_biggreenbox)
            bookingInfo.text = preBook
        }
        revealBookButtons()
    }

    fun resetButton(){ // resets button and gives it a color based on availability
        if (isBooked){
            muteBox.setImageResource(R.drawable.ic_smallredbox)
        }else{
            muteBox.setImageResource(R.drawable.ic_smallgreenbox)
        }
        hideButtons()
    }

    fun endBooking(){ // ends the reservation/booking of a MuteBox manually settings its color to green and removing the "end booking" button
        isBooked = false
        bookingInfo.text = preBook
        muteBox.setImageResource(R.drawable.ic_biggreenbox)
        endButton.visibility = View.GONE
    }

    private fun revealBookButtons(){ // reveals new interactable buttons, like "end booking" and "book", when buttons are expanded
        backButton.visibility = View.VISIBLE
        book.visibility = View.VISIBLE
        bookingInfo.visibility=View.VISIBLE
        if (isBooked) {
            endButton.visibility = View.VISIBLE
        } else {
            endButton.visibility = View.GONE
        }
    }

    private fun sendCommand(input: String) { // sends a command to the connected bluetooth device in the form of a string
        if (BookingScreen.m_bluetoothSocket != null){ // checks if there is a bluetooth connection present
            try{
                BookingScreen.m_bluetoothSocket!!.outputStream.write(input.toByteArray()) //sends string
            } catch (e: IOException){
                e.printStackTrace() // shows errors
            }
        }
    }
    private fun hideButtons(){ // hide the interactable buttons,"end booking" and "book", in the expandable buttons
        book.visibility = View.GONE
        bookingInfo.visibility=View.GONE
        endButton.visibility = View.GONE
        backButton.visibility = View.GONE
    }

    fun boothBooker(){ // books a booth by changing its availability and color
        isBooked = true
        endButton.visibility = View.VISIBLE
        muteBox.setImageResource(R.drawable.ic_bigredbox)
        timer()
    }
    private fun timer(){ // Starts a reservation timer after a MuteBox has been booked
        object : CountDownTimer(30000, 1) { // total countdown time and how often it ticks down
            var postBook = postBookPreCode
            override fun onTick(millisUntilFinished: Long) { // what happens when the timer decrements
                if (isBooked){
                    postBook = postBookPreCode + millisUntilFinished/1000 + " seconds"
                    bookingInfo.text = postBook
                } else {
                  cancel()
                }
            }

            override fun onFinish() { // checks to see if the MuteBox has been acquired and sets the appropriate color of the MuteBox
                isBooked = acquired
                muteBox.setImageResource(R.drawable.ic_smallgreenbox)
                hideButtons()
                sendCommand("G")
            }
        }.start()
    }
}