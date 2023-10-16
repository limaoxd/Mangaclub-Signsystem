package com.example.mcss

import com.google.gson.Gson
import org.json.JSONObject
import java.io.IOException
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import android.Manifest
import android.os.Bundle
import android.os.Build
import android.os.Looper
import android.os.IBinder
import android.os.Handler
import android.app.Service
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.content.Context
import android.content.Intent
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity

data class User(val name: String)

var State = false
var Name = ""
val Time = 30
class BleService : Service() {
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()
    private val handler = Handler(Looper.getMainLooper())
    private val mainHandler = Handler(Looper.getMainLooper())

    //after several seconds will post leave
    private val checkInactivity = object : Runnable {
        override fun run() {
            if (State) {
                WebhookPost("leave")
                State = false
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            // Handle the ScanResult here, which contains the advertisement data.
            result?.let {
                val device = it.device
                val rssi = it.rssi
                val scanRecord = it.scanRecord
                // Access the advertisement data:
                val advertisedData = it.scanRecord?.bytes ?: ByteArray(0)

                //Log.i("device", String(advertisedData, Charsets.UTF_8))
                if(device.toString() == "28:CD:C1:05:14:9C") {
                    if(!State)
                        WebhookPost("enter")
                    State = true
                    Log.i("debug", "before counter.")
                    startInactivityTimer()
                }
            }
        }
    }

    // Method to start the inactivity timer
    fun startInactivityTimer() {
        // Remove any existing callbacks
        handler.removeCallbacks(checkInactivity)
        // Schedule the checkInactivity Runnable after 10 seconds
        val timer: Long = 1000 * Time.toLong()
        handler.postDelayed(checkInactivity, timer) // 10 seconds
    }

    fun Context.bluetoothAdapter(): BluetoothAdapter? =
        (this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    fun WebhookPost(event : String){
        Toast.makeText(applicationContext, event, Toast.LENGTH_SHORT).show()

        val aws_url = "https://n0jejlqs54.execute-api.ap-southeast-2.amazonaws.com/prod"
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()

        val json = JSONObject()
        json.put("name", Name)
        json.put("event", event)
        val requestBody = json.toString().toRequestBody(mediaType)
        val request = Request.Builder().url(aws_url).post(requestBody).build()

        client.newCall(request).enqueue(object  : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                //debug_terminal.text = "failed"
            }
            override fun onResponse(call: Call, response: Response) {
                //debug_terminal.text = "send"
            }
        })
    }

    override fun onCreate() {
        super.onCreate()
        //Start service
        startBleScan()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun startBleScan() {
        val bluetoothLeScanner = bluetoothAdapter()?.bluetoothLeScanner

        bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)

        // Stop scanning after a specified duration (e.g., 10 seconds).
        /*mainHandler.postDelayed({
            stopBleScan()
        }, 10000)*/
    }

    private fun stopBleScan() {
        val bluetoothLeScanner = bluetoothAdapter()?.bluetoothLeScanner
        bluetoothLeScanner?.stopScan(scanCallback)
        // Optionally, you can perform other cleanup tasks here.
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        stopBleScan()
        super.onDestroy()
    }
}

class MainActivity : ComponentActivity() {
    fun name_reader(file_name: String): String{
        val input = openFileInput(file_name)
        val reader = InputStreamReader(input)
        val stringBuilder = StringBuilder()
        val buffer = CharArray(1024)
        var bytesRead: Int

        while (reader.read(buffer).also { bytesRead = it } != -1) {
            stringBuilder.append(buffer, 0, bytesRead)
        }
        // Now, `jsonData` contains the JSON data read from the file
        reader.close()

        val json = stringBuilder.toString()
        val user = Gson().fromJson(json, User::class.java)
        return user.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bleServiceIntent = Intent(this, BleService::class.java)

        val button = findViewById<Button>(R.id.button)
        val debug_terminal = findViewById<TextView>(R.id.debug)

        //request permissions
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,  // For coarse location
            // OR
            Manifest.permission.ACCESS_FINE_LOCATION     // For fine location
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, 1)
            }
        }

        val file_name = "User.json"
        val file = File(filesDir, file_name)

        if(file.exists()) {
            Name = name_reader(file_name)
            //Start bluetooth service
            startService(bleServiceIntent)
        }

        //When register button has been press.
        button.setOnClickListener{
            //file.delete()
            if(file.exists()){
                try {
                    debug_terminal.text = name_reader(file_name) + " exist."
                } catch (e: Exception) {
                    debug_terminal.text = "Read file failed with unexpect situation."
                    e.printStackTrace()
                }
            }else{
                try{
                    //create a json, and output user name
                    val name = findViewById<EditText>(R.id.editText).text.toString()
                    //detect name is empty or not
                    if(name.isEmpty())
                        throw Exception("Name can't not be empty!")

                    Name = name
                    val json = JSONObject()
                    json.put("name", name)

                    //write file in .json
                    val output = openFileOutput(file_name, Context.MODE_PRIVATE)
                    val writer = OutputStreamWriter(output)
                    writer.write(json.toString())
                    writer.close()

                    startService(bleServiceIntent)
                    debug_terminal.text = name + " register success!"
                }catch(e: Exception){
                    debug_terminal.text = "Write file failed with unexpect situation."
                    e.printStackTrace()
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
    }
}