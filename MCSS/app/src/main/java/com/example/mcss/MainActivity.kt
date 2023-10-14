package com.example.mcss

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)
        val debug_terminal = findViewById<TextView>(R.id.debug)
        val aws_url = "https://n0jejlqs54.execute-api.ap-southeast-2.amazonaws.com/prod"

        button.setOnClickListener{
            val name = findViewById<EditText>(R.id.editText).text.toString()
            val json = JSONObject()
            json.put("name", name)

            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = json.toString().toRequestBody(mediaType)
            val request = Request.Builder().url(aws_url).post(requestBody).build()

            client.newCall(request).enqueue(object  : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    debug_terminal.text = "failed"
                }
                override fun onResponse(call: Call, response: Response) {
                    debug_terminal.text = "send"
                }
            })
        }
    }
}