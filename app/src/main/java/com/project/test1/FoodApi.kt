package com.project.test1

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface FoodApi {
    @GET("/api/fooddata")
    fun getFoods(): Call<List<MealActivity.FoodData>>

    @GET("/api/fooddata/{foodName}")
    fun getFood(@Path("foodName") foodName: String): Call<MealActivity.FoodData>
}