package ru.lim1x.places.back.http

import android.util.Log
import okhttp3.*
import java.io.IOException

class PClient() {
    var code = "123456"
    private val tryings = 0
    var phone = "empty"
    private val apikey = "2DUWCL38JE0Z7PT7DNC0BC582EQ33XUBR0RPHCTDY940FQO2OTKG3T213SULQ5HG"


    val client = OkHttpClient()



    fun send(){
        if (phone.equals("empty"))
            return
        val request = Request.Builder()
            .url("https://smspilot.ru/api.php?send=$code&to=$phone&from=INFORM&apikey=$apikey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        throw IOException("Запрос к серверу не был успешен:" +
                                " ${response.code} ${response.message}")
                    }
                    for ((name, value) in response.headers) {
                        Log.i("Header", "$name, $value")
                    }
                    Log.i("Body", response.body!!.string())
                }
            }
        })
    }







}