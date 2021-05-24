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

    private lateinit var booking : ActivityBookingScreenBinding

    var m_bluetoothAdapter: BluetoothAdapter? = null
    lateinit var m_pairedDevices: Set<BluetoothDevice>
    val REQUEST_ENABLE_BLUETOOTH = 1

    companion object{ // variables you wanna access from other classes // used to move items from one class to another class
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        booking = ActivityBookingScreenBinding.inflate(layoutInflater)
        setContentView(booking.root)

        val currentUser  = intent.getStringExtra(USERNAME)

        val muteBox1 = MuteBoxMaker(booking.MuteBox1Text, false, booking.mB1BookText, booking.MuteBox1, booking.mB1Book, currentUser!!, booking.mB1EndBook, booking.backArrow1)
        val muteBox2 = MuteBoxMaker(booking.MuteBox2Text, false, booking.mB2BookText, booking.MuteBox2, booking.mB2Book, currentUser!!, booking.mB2EndBook, booking.backArrow2)
        val muteBox3 = MuteBoxMaker(booking.MuteBox3Text, false, booking.mB3BookText, booking.MuteBox3, booking.mB3Book, currentUser!!, booking.mB3EndBook, booking.backArrow3)
        val muteBox4 = MuteBoxMaker(booking.MuteBox4Text, false, booking.mB4BookText, booking.MuteBox4, booking.mB4Book, currentUser!!, booking.mB4EndBook, booking.backArrow4)

        var muteBoxList = arrayOf(muteBox1, muteBox2, muteBox3, muteBox4)

        roomListeners(muteBoxList)
        bookListener(muteBoxList)
        m_address = "3C:71:BF:9D:97:62" // ændre til and mac address hvis nødvendigt
        ConnectToDevice(this).execute()

        booking.Background.setOnClickListener {
            buttonSizeReset(muteBoxList)
        }
        // bt tester
            m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

            if(m_bluetoothAdapter == null) {// checks to see if device supports bluetooth
                Toast.makeText(this, "this device does not support bluetooth", Toast.LENGTH_SHORT).show()
                return
            }
            if (!m_bluetoothAdapter!!.isEnabled){ // checks to see if bt is enabled
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
            }

        // bt tester end


    }

    // decides what happens when booth has been clicked on
    private fun roomListeners(muteBoxList: Array<MuteBoxMaker>){
        for (i in muteBoxList.indices){
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

    private fun buttonSizeReset(muteBoxList: Array<MuteBoxMaker>){
        for (i in muteBoxList.indices){
            muteBoxList[i].resetButton()
        }
    }

    private fun bookListener(muteBoxList: Array<MuteBoxMaker>){
        for (i in muteBoxList.indices){
            muteBoxList[i].book.setOnClickListener {
                bookButton(muteBoxList, i)
            }
        }

    }
    private fun bookButton(muteBoxList: Array<MuteBoxMaker>, iD: Int) {
        if (muteBoxList[iD].isBooked) {
            Toast.makeText(applicationContext, "MuteBox has already been booked", Toast.LENGTH_SHORT).show()
        } else {
            muteBoxList[iD].boothBooker()
            sendCommand("R")
            Toast.makeText(applicationContext, "You have booked ${muteBoxList[iD].name.hint}", Toast.LENGTH_SHORT).show()
        }
       //test



        //test end
    }

    //BT TEST FUNCTIONS
    //checks if bluetooth is enabled

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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

    private fun sendCommand(input: String) {
        if (m_bluetoothSocket != null){
            try{
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch (e: IOException){
                e.printStackTrace()
            }
        }
    }

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private val context: Context


        init {
            this.context = c
        }

        override fun onPreExecute() { // what the user sees while connecting
            super.onPreExecute()
            m_progress= ProgressDialog.show(context, "Connecting...", "please wait")
        }
        override fun doInBackground(vararg p0: Void?) : String? {
            try {
                if (m_bluetoothSocket  == null || !m_isConnected){
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery() //stops from serachin for additional devices
                    m_bluetoothSocket!!.connect()
                }
            }catch (e: IOException){
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(!connectSuccess){
                Log.i("data", "couldn't connect")
            } else{
                m_isConnected = true
            }
            m_progress.dismiss()
        }
    }
    // BT test Ends
}
