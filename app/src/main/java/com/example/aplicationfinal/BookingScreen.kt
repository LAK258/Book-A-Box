package com.example.aplicationfinal

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.media.Image
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.example.aplicationfinal.databinding.ActivityBookingScreenBinding
import java.io.IOException
import java.util.*

class BookingScreen : AppCompatActivity() {

    private lateinit var booking : ActivityBookingScreenBinding // creates a variable to be initialized later

    var m_bluetoothAdapter: BluetoothAdapter? = null
    val REQUEST_ENABLE_BLUETOOTH = 1 // used to check if a device is capable of connecting to bluetooth

    companion object{ // variables you wanna access from other classes // used to move items from one class to another class
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // "universal unique identifier" used to give the bluetooth connection a unique name
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {  // creates the layout
        super.onCreate(savedInstanceState)
        booking = ActivityBookingScreenBinding.inflate(layoutInflater) // connects the binding variable to the layout file
        setContentView(booking.root) // set which layout file to view

        val currentUser  = intent.getStringExtra(USERNAME)  // gets the information passed from the login class

        val muteBox1 = MuteBoxMaker(booking.MuteBox1Text, false, booking.mB1BookText, booking.MuteBox1, booking.mB1Book, currentUser!!, booking.mB1EndBook, booking.backArrow1) // creates an object of the MuteBoxMaker class
        val muteBox2 = MuteBoxMaker(booking.MuteBox2Text, false, booking.mB2BookText, booking.MuteBox2, booking.mB2Book, currentUser!!, booking.mB2EndBook, booking.backArrow2)
        val muteBox3 = MuteBoxMaker(booking.MuteBox3Text, false, booking.mB3BookText, booking.MuteBox3, booking.mB3Book, currentUser!!, booking.mB3EndBook, booking.backArrow3)
        val muteBox4 = MuteBoxMaker(booking.MuteBox4Text, false, booking.mB4BookText, booking.MuteBox4, booking.mB4Book, currentUser!!, booking.mB4EndBook, booking.backArrow4)

        var muteBoxList = arrayOf(muteBox1, muteBox2, muteBox3, muteBox4) // creates an array of objects

        roomListeners(muteBoxList) // calls a method passing the MuteBoxList as a parameter
        bookListener(muteBoxList)

        m_address = "3C:71:BF:9D:97:62" // MAC-Address of the bluetooth module.
        ConnectToDevice(this).execute() // connects the Bluetooth module to this application

        booking.Background.setOnClickListener { // determines what happens when you click on the background
            buttonSizeReset(muteBoxList)
        }

        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() // gets the build-in bluetooth adapter of the phone

        if(m_bluetoothAdapter == null) {// checks to see if device supports bluetooth
            Toast.makeText(this, "this device does not support bluetooth", Toast.LENGTH_SHORT).show() // a pop-up message
            return
        }
        if (!m_bluetoothAdapter!!.isEnabled){ // checks to see if bt is enabled and enables it if allowed
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH) // runs on activity result for the Bluetooth connectivity
        }

    }

 // Functions
    private fun roomListeners(muteBoxList: Array<MuteBoxMaker>){   // decides what happens when a MuteBox button has been clicked on
        for (i in muteBoxList.indices){ // goes through each item in the Object array and checks if any of them has been clicked
            muteBoxList[i].muteBox.setOnClickListener {
                buttonSizeReset(muteBoxList)
                muteBoxList[i].expandButton()
            }
            muteBoxList[i].endButton.setOnClickListener {
                muteBoxList[i].endBooking()
                Toast.makeText(applicationContext, "${muteBoxList[i].name.hint} is no longer booked", Toast.LENGTH_SHORT).show()
                sendCommand("G")
            }
            muteBoxList[i].backButton.setOnClickListener {
                muteBoxList[i].resetButton()
            }
        }
    }

    private fun buttonSizeReset(muteBoxList: Array<MuteBoxMaker>){ // resets the size of the MuteBox Buttons
        for (i in muteBoxList.indices){
            muteBoxList[i].resetButton()
        }
    }

    private fun bookListener(muteBoxList: Array<MuteBoxMaker>){ // checks if the Book button has been pressed for a MuteBox
        for (i in muteBoxList.indices){
            muteBoxList[i].book.setOnClickListener {
                bookButton(muteBoxList, i)
            }
        }

    }
    private fun bookButton(muteBoxList: Array<MuteBoxMaker>, iD: Int) { // reserves the desired MuteBox
        if (muteBoxList[iD].isBooked) {
            Toast.makeText(applicationContext, "MuteBox has already been booked", Toast.LENGTH_SHORT).show()
        } else {
            muteBoxList[iD].boothBooker()
            sendCommand("R")
            Toast.makeText(applicationContext, "You have booked ${muteBoxList[iD].name.hint}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {    //checks if bluetooth is enabled
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH){
            if (resultCode == Activity.RESULT_OK){
                if (m_bluetoothAdapter!!.isEnabled){ // checks if bluetooth has been disabled or enabled
                    Toast.makeText(this, "Bluetooth has been enabled", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Bluetooth has been disabled", Toast.LENGTH_SHORT).show()
                }
            } else if (resultCode == Activity.RESULT_CANCELED){ // bluetooth enabling has been cancelled
                Toast.makeText(this, "Bluetooth has been cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendCommand(input: String) { // sends a command to the Connected Bluetooth device
        if (m_bluetoothSocket != null){
            try{
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e: IOException){
                e.printStackTrace()
            }
        }
    }

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() { // The class in charge of connecting the Phone and the Arduino
        private var connectSuccess: Boolean = true
        private val context: Context


        init {
            this.context = c
        }

        override fun onPreExecute() { // what the user sees while connecting
            super.onPreExecute()
            m_progress= ProgressDialog.show(context, "Connecting...", "please wait")
        }
        override fun doInBackground(vararg p0: Void?) : String? { // what happens in the background while the user waits for the connection to happen
            try {
                if (m_bluetoothSocket  == null || !m_isConnected){ // checks if there is a connection already and if not tries to make the connection based on the given MAC-address and UUID
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery() //stops from searching for additional devices
                    m_bluetoothSocket!!.connect()
                }
            }catch (e: IOException){
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }
        override fun onPostExecute(result: String?) { // what happens after the connection is either successful or unsuccessful
            super.onPostExecute(result)
            if(!connectSuccess){
                Log.i("data", "couldn't connect")
            } else{
                m_isConnected = true
            }
            m_progress.dismiss()
        }
    }
}
