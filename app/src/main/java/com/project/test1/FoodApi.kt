package com.project.test1

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FoodApi {
    @GET("foods")
    fun getFoods(): Call<List<Food>>

    @GET("food/{name}")
    fun getFood(@Path("name") name: String): Call<Food>
}
