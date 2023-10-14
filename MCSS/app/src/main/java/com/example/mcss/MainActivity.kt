package com.example.mcss

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import okhttp3.*
import org.json.JSONObject
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import com.google.gson.Gson

data class User(val name: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)
        val debug_terminal = findViewById<TextView>(R.id.debug)

        //When register button has been press.
        button.setOnClickListener{
            val file_name = "User.json"
            val file = File(filesDir, file_name)
            //file.delete()

            if(file.exists()){
                try {
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
                    debug_terminal.text = user.name + " exist."
                } catch (e: Exception) {
                    debug_terminal.text = "Read failed with unexpect situation."
                    e.printStackTrace()
                }
            }else{
                try{
                    //create a json, and output user name
                    val name = findViewById<EditText>(R.id.editText).text.toString()
                    //detect name is empty or not
                    if(name.isEmpty())
                        throw Exception("Name can't not be empty!")

                    val json = JSONObject()
                    json.put("name", name)

                    //write file in .json
                    val output = openFileOutput(file_name, Context.MODE_PRIVATE)
                    val writer = OutputStreamWriter(output)
                    writer.write(json.toString())
                    writer.close()

                    debug_terminal.text = name + " register success!"
                }catch(e: Exception){
                    debug_terminal.text = "Write failed with unexpect situation."
                    e.printStackTrace()
                }
            }
        }
        /*
        val aws_url = "https://n0jejlqs54.execute-api.ap-southeast-2.amazonaws.com/prod"
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
        })*/
    }
}