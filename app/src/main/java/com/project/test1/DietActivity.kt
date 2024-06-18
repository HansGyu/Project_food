package com.project.test1

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.project.test1.databinding.ActivityDietBinding

class DietActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDietBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDietBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBreakfast.setOnClickListener {
            val intent = Intent(this, MealActivity::class.java)
            intent.putExtra("mealType", "아침")
            startActivity(intent)
        }

        binding.btnLunch.setOnClickListener {
            val intent = Intent(this, MealActivity::class.java)
            intent.putExtra("mealType", "점심")
            startActivity(intent)
        }

        binding.btnDinner.setOnClickListener {
            val intent = Intent(this, MealActivity::class.java)
            intent.putExtra("mealType", "저녁")
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateTotals()
    }

    private fun updateTotals() {
        val sharedPreferences = getSharedPreferences("MealData", Context.MODE_PRIVATE)

        val breakfastKcal = sharedPreferences.getInt("아침_total_kcal", 0)
        val breakfastProtein = sharedPreferences.getFloat("아침_total_protein", 0.0f)
        val breakfastFat = sharedPreferences.getFloat("아침_total_fat", 0.0f)
        val breakfastCarbs = sharedPreferences.getFloat("아침_total_carbs", 0.0f)

        val lunchKcal = sharedPreferences.getInt("점심_total_kcal", 0)
        val lunchProtein = sharedPreferences.getFloat("점심_total_protein", 0.0f)
        val lunchFat = sharedPreferences.getFloat("점심_total_fat", 0.0f)
        val lunchCarbs = sharedPreferences.getFloat("점심_total_carbs", 0.0f)

        val dinnerKcal = sharedPreferences.getInt("저녁_total_kcal", 0)
        val dinnerProtein = sharedPreferences.getFloat("저녁_total_protein", 0.0f)
        val dinnerFat = sharedPreferences.getFloat("저녁_total_fat", 0.0f)
        val dinnerCarbs = sharedPreferences.getFloat("저녁_total_carbs", 0.0f)

        binding.tvBreakfastTotals.text = "아침 영양 성분 합계: 칼로리: $breakfastKcal, 탄수화물: $breakfastCarbs, 단백질: $breakfastProtein, 지방: $breakfastFat"
        binding.tvLunchTotals.text = "점심 영양 성분 합계: 칼로리: $lunchKcal, 탄수화물: $lunchCarbs, 단백질: $lunchProtein, 지방: $lunchFat"
        binding.tvDinnerTotals.text = "저녁 영양 성분 합계: 칼로리: $dinnerKcal, 탄수화물: $dinnerCarbs, 단백질: $dinnerProtein, 지방: $dinnerFat"
    }
}
