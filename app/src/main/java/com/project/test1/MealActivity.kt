package com.project.test1

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.test1.databinding.ActivityMealBinding
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import kotlin.concurrent.thread

class MealActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMealBinding
    private lateinit var foodAdapter: FoodAdapter
    private var foodList = mutableListOf<FoodData>() // 음식 목록
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
                val connection = DatabaseConnection.getConnection()
                val statement = connection.createStatement()
                val resultSet = statement.executeQuery("SELECT `식품명` FROM fooddata")

                while (resultSet.next()) {
                    val foodName = resultSet.getString("식품명")
                    foodList.add(FoodData(foodName, 0, 0f, 0f, 0f)) // 기본값으로 초기화
                }
                connection.close()

                runOnUiThread {
                    foodAdapter.notifyDataSetChanged()
                }
            } catch (e: SQLException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    Log.e("MealActivity", "Error fetching food list: ${e.message}")
                }
            }
        }
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodAdapter(foodList, { selectedFood ->
            binding.etFoodName.setText(selectedFood.name)
            fetchFoodData(selectedFood.name)
        }, { position ->
            foodList.removeAt(position)
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
                foodAdapter.filter(newText ?: "")
                return true
            }
        })
    }

    private fun fetchFoodData(foodName: String) {
        thread {
            try {
                val connection = DatabaseConnection.getConnection()
                val statement = connection.createStatement()
                val resultSet = statement.executeQuery("SELECT * FROM fooddata WHERE `식품명` = '$foodName'")

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
            } catch (e: SQLException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "음식 데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    Log.e("MealActivity", "Error fetching food data: ${e.message}")
                }
            }
        }
    }

    private fun addFood(foodName: String) {
        thread {
            try {
                val connection = DatabaseConnection.getConnection()
                val statement = connection.createStatement()
                val resultSet = statement.executeQuery("SELECT * FROM fooddata WHERE `식품명` = '$foodName'")

                if (resultSet.next()) {
                    val kcal = resultSet.getInt("kcal")
                    val protein = resultSet.getFloat("protein")
                    val fat = resultSet.getFloat("fat")
                    val carbs = resultSet.getFloat("carbs")

                    val foodData = FoodData(foodName, kcal, protein, fat, carbs)
                    foodList.add(foodData)

                    runOnUiThread {
                        foodAdapter.notifyDataSetChanged()
                    }
                }
                connection.close()
            } catch (e: SQLException) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "음식 데이터를 추가하는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    Log.e("MealActivity", "Error adding food data: ${e.message}")
                }
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

        for (food in foodList) {
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

    object DatabaseConnection {
        private const val URL = "jdbc:mysql://localhost:3306/datasearch"
        private const val USER = "root"
        private const val PASSWORD = "1234"

        fun getConnection(): Connection {
            return DriverManager.getConnection(URL, USER, PASSWORD)
        }
    }

    data class FoodData(val name: String, val kcal: Int, val protein: Float, val fat: Float, val carbs: Float)
}
