package com.project.test1

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.test1.databinding.ActivityMealBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import kotlin.concurrent.thread
import androidx.appcompat.widget.SearchView

class MealActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMealBinding
    private lateinit var foodAdapter: FoodAdapter
    private var foodList = mutableListOf<Food>() // 음식 목록
    private var mealType: String? = null // 아침, 점심, 저녁 구분

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mealType = intent.getStringExtra("MEAL_TYPE") ?: "default_meal"

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
        val foodApi = RetrofitClient.instance.create(FoodApi::class.java)
        foodApi.getFoods().enqueue(object : Callback<List<Food>> {
            override fun onResponse(call: Call<List<Food>>, response: Response<List<Food>>) {
                if (response.isSuccessful) {
                    foodList.clear()
                    response.body()?.let { foodList.addAll(it) }
                    foodAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@MealActivity, "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Food>>, t: Throwable) {
                Toast.makeText(this@MealActivity, "데이터를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodAdapter(foodList, { selectedFood ->
            binding.etFoodName.setText(selectedFood.식품명)
            fetchFoodData(selectedFood.식품명)
        }, { position ->
            foodList.removeAt(position)
            foodAdapter.notifyDataSetChanged()
        })
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = foodAdapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText ?: "")
                return true
            }
        })
    }

    private fun filter(text: String) {
        val filteredList = foodList.filter {
            it.식품명.contains(text, ignoreCase = true)
        }
        foodAdapter.updateFoodList(filteredList)
    }

    private fun fetchFoodData(foodName: String) {
        val foodApi = RetrofitClient.instance.create(FoodApi::class.java)
        foodApi.getFood(foodName).enqueue(object  : Callback<Food> {
            override fun onResponse(call: Call<Food>, response: Response<Food>) {
                if (response.isSuccessful) {
                    response.body()?.let { food ->
                        binding.tvFoodInfo.text =
                            "칼로리: ${food.에너지}, 단백질: ${food.단백질}, " +
                                    "지방: ${food.지방}, 탄수화물: ${food.탄수화물}"
                    }
                } else {
                    Toast.makeText(this@MealActivity, "음식을 찾을 수 없습니다.",
                        Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Food>, t: Throwable) {
                Toast.makeText(this@MealActivity, "음식 데이터를 가져오는 데 실패했습니다.",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addFood(foodName: String) {
        thread {
            try {
                val connection = DatabaseConnection.getConnection()
                val statement = connection.createStatement()
                val resultSet = statement.executeQuery("SELECT * FROM fooddata WHERE `식품명` = '$foodName'")

                if (resultSet.next()) {
                    val 식품코드 = resultSet.getString("식품코드")
                    val 식품명 = resultSet.getString("식품명")
                    val 에너지 = resultSet.getFloat("에너지(kcal)")
                    val 단백질 = resultSet.getFloat("단백질(g)")
                    val 지방 = resultSet.getFloat("지방(g)")
                    val 탄수화물 = resultSet.getFloat("탄수화물(g)")
                    val 당류 = resultSet.getFloat("당류(g)")
                    val 나트륨 = resultSet.getFloat("나트륨(mg)")

                    val foodData = Food(식품코드, 식품명, 에너지, 단백질, 지방, 탄수화물, 당류, 나트륨)
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
            val keyPrefix = "${mealType}_${food.식품명}"
            editor.putInt("${keyPrefix}_kcal", food.에너지.toInt())
            editor.putFloat("${keyPrefix}_protein", food.단백질)
            editor.putFloat("${keyPrefix}_fat", food.지방)
            editor.putFloat("${keyPrefix}_carbs", food.탄수화물)

            totalKcal += food.에너지.toInt()
            totalProtein += food.단백질
            totalFat += food.지방
            totalCarbs += food.탄수화물
        }

        editor.putInt("${mealType}_total_kcal", totalKcal)
        editor.putFloat("${mealType}_total_protein", totalProtein)
        editor.putFloat("${mealType}_total_fat", totalFat)
        editor.putFloat("${mealType}_total_carbs", totalCarbs)

        editor.apply()
    }

    object DatabaseConnection {
        private const val URL = "jdbc:mysql://localhost/foodsearch"
        private const val USER = "root"
        private const val PASSWORD = "1234"

        fun getConnection(): Connection {
            return DriverManager.getConnection(URL, USER, PASSWORD)
        }
    }
}
