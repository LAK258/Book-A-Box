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

class MuteBoxMaker(val name: TextView, var isBooked: Boolean, val bookingInfo: TextView, val muteBox: ImageView, val book: Button, private val userName: String, val endButton: Button, val backButton: ImageView) {

    private val preBook = "You have 5 minutes to acquire the MuteBox when booked"
    private val postBookPreCode  = "MuteBox has been reserved\n By: $userName \n" +
            "For: "
    private val acquired = false
    // member functions
    fun expandButton(){
        if (isBooked){
            muteBox.setImageResource(R.drawable.ic_bigredbox)
        }else {
            muteBox.setImageResource(R.drawable.ic_biggreenbox)
            bookingInfo.text = preBook
        }
        revealBookButtons()
    }

    fun resetButton(){
        if (isBooked){
            muteBox.setImageResource(R.drawable.ic_smallredbox)
        }else{
            muteBox.setImageResource(R.drawable.ic_smallgreenbox)
        }
        hideButtons()
    }

    fun endBooking(){
        isBooked = false
        bookingInfo.text = preBook
        muteBox.setImageResource(R.drawable.ic_biggreenbox)
        endButton.visibility = View.GONE
    }

    private fun revealBookButtons(){
        backButton.visibility = View.VISIBLE
        book.visibility = View.VISIBLE
        bookingInfo.visibility=View.VISIBLE
        if (isBooked) {
            endButton.visibility = View.VISIBLE
        } else {
            endButton.visibility = View.GONE
        }
    }

    private fun sendCommand(input: String) {
        if (BookingScreen.m_bluetoothSocket != null){
            try{
                BookingScreen.m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e: IOException){
                e.printStackTrace()
            }
        }
    }
    private fun hideButtons(){
        book.visibility = View.GONE
        bookingInfo.visibility=View.GONE
        endButton.visibility = View.GONE
        backButton.visibility = View.GONE
    }

    fun boothBooker(){
        isBooked = true
        endButton.visibility = View.VISIBLE
        muteBox.setImageResource(R.drawable.ic_bigredbox)
        timer()
    }
    private fun timer(){
        object : CountDownTimer(30000, 1) {
            var postBook = postBookPreCode
            override fun onTick(millisUntilFinished: Long) {
                if (isBooked){
                    postBook = postBookPreCode + millisUntilFinished/1000 + " seconds"
                    bookingInfo.text = postBook
                } else {
                  cancel()
                }
            }

            override fun onFinish() {
                isBooked = acquired
                muteBox.setImageResource(R.drawable.ic_smallgreenbox)
                hideButtons()
                sendCommand("G")
            }
        }.start()
    }
}