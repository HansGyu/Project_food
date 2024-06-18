package com.project.test1

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.test1.databinding.ActivityMealBinding
import java.sql.Connection
import java.sql.DriverManager
import kotlin.concurrent.thread

class MealActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMealBinding
    private lateinit var foodAdapter: FoodAdapter
    private var foodList = mutableListOf<String>() // 음식 목록
    private var addedFoodList = mutableListOf<FoodData>() // 추가된 음식 목록
    private var mealType: String? = null // 아침, 점심, 저녁 구분

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mealType = intent.getStringExtra("mealType")

        fetchFoodList() // 음식 목록 가져오기
        setupRecyclerView()
        setupSearchView()

        binding.btnAddFood.setOnClickListener {
            val selectedFood = binding.etFoodName.text.toString()
            if (selectedFood.isNotEmpty()) {
                addFood(selectedFood)
                binding.etFoodName.text.clear()
            } else {
                Toast.makeText(this, "음식 이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnComplete.setOnClickListener {
            saveMealData()
            finish()
        }
    }

    private fun fetchFoodList() {
        thread {
            try {
                val connection = connectToDatabase()
                val statement = connection.createStatement()
                val resultSet = statement.executeQuery("SELECT `식품명` FROM foodsearch")

                while (resultSet.next()) {
                    val foodName = resultSet.getString("식품명")
                    foodList.add(foodName)
                }
                connection.close()

                runOnUiThread {
                    foodAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodAdapter(addedFoodList, { selectedFood ->
            binding.etFoodName.setText(selectedFood)
            fetchFoodData(selectedFood)
        }, { position ->
            addedFoodList.removeAt(position)
            foodAdapter.notifyDataSetChanged()
        })
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = foodAdapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 여기서 검색된 음식 목록을 필터링하는 로직을 추가할 수 있습니다.
                return true
            }
        })
    }

    private fun fetchFoodData(foodName: String) {
        thread {
            try {
                val connection = connectToDatabase()
                val statement = connection.createStatement()
                val resultSet = statement.executeQuery("SELECT * FROM foodsearch WHERE `식품명` = '$foodName'")

                if (resultSet.next()) {
                    val kcal = resultSet.getInt("kcal")
                    val protein = resultSet.getFloat("protein")
                    val fat = resultSet.getFloat("fat")
                    val carbs = resultSet.getFloat("carbs")
                    val sugars = resultSet.getFloat("sugars")
                    val sodium = resultSet.getFloat("sodium")

                    runOnUiThread {
                        binding.tvFoodInfo.text = "칼로리: $kcal, 단백질: $protein, 지방: $fat, 탄수화물: $carbs, 당류: $sugars, 나트륨: $sodium"
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "음식을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                connection.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addFood(foodName: String) {
        thread {
            try {
                val connection = connectToDatabase()
                val statement = connection.createStatement()
                val resultSet = statement.executeQuery("SELECT * FROM foodsearch WHERE `식품명` = '$foodName'")

                if (resultSet.next()) {
                    val kcal = resultSet.getInt("kcal")
                    val protein = resultSet.getFloat("protein")
                    val fat = resultSet.getFloat("fat")
                    val carbs = resultSet.getFloat("carbs")

                    val foodData = FoodData(foodName, kcal, protein, fat, carbs)
                    addedFoodList.add(foodData)

                    runOnUiThread {
                        foodAdapter.notifyDataSetChanged()
                    }
                }
                connection.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveMealData() {
        val sharedPreferences = getSharedPreferences("MealData", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        var totalKcal = 0
        var totalProtein = 0.0f
        var totalFat = 0.0f
        var totalCarbs = 0.0f

        for (food in addedFoodList) {
            val keyPrefix = "${mealType}_${food.name}"
            editor.putInt("${keyPrefix}_kcal", food.kcal)
            editor.putFloat("${keyPrefix}_protein", food.protein)
            editor.putFloat("${keyPrefix}_fat", food.fat)
            editor.putFloat("${keyPrefix}_carbs", food.carbs)

            totalKcal += food.kcal
            totalProtein += food.protein
            totalFat += food.fat
            totalCarbs += food.carbs
        }

        editor.putInt("${mealType}_total_kcal", totalKcal)
        editor.putFloat("${mealType}_total_protein", totalProtein)
        editor.putFloat("${mealType}_total_fat", totalFat)
        editor.putFloat("${mealType}_total_carbs", totalCarbs)

        editor.apply()
    }

    private fun connectToDatabase(): Connection {
        val url = "jdbc:mysql://localhost:3306/foodsearch"
        val user = "root"
        val password = "password"
        return DriverManager.getConnection(url, user, password)
    }
}

data class FoodData(val name: String, val kcal: Int, val protein: Float, val fat: Float, val carbs: Float)
