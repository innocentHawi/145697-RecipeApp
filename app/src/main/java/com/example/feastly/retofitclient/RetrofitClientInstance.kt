package com.example.feastly.retofitclient

import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClientInstance {
    companion object{
        private lateinit var retrofit: Retrofit
        private val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"
        val retrofitInstance:Retrofit
            get(){
                if (!::retrofit.isInitialized) {
                    retrofit = Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                }
                return retrofit
            }
    }

}