package com.example.feastly.interfaces

import retrofit2.Call
import com.example.feastly.entities.Category
import com.example.feastly.entities.Meal
import com.example.feastly.entities.MealResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GetDataService {
    @GET("categories.php")
    fun getCategoryList(): Call<Category>

    @GET("filter.php")
    fun getMealList(@Query("c")category: String): Call<Meal>

    @GET("lookup.php")
    fun getSpecificItem(@Query("i") id: String): Call<MealResponse>


}