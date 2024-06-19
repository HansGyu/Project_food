package com.project.test1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FoodAdapter(
    private val foodList: MutableList<MealActivity.FoodData>,
    private val clickListener: (MealActivity.FoodData) -> Unit,
    private val deleteListener: (Int) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    private var filteredFoodList: MutableList<MealActivity.FoodData> = foodList.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(filteredFoodList[position], clickListener, deleteListener, position)
    }

    override fun getItemCount(): Int {
        return filteredFoodList.size
    }

    fun filter(query: String) {
        filteredFoodList = if (query.isEmpty()) {
            foodList.toMutableList()
        } else {
            foodList.filter { it.name.contains(query, true) }.toMutableList()
        }
        notifyDataSetChanged()
    }

    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodNameTextView: TextView = itemView.findViewById(R.id.tv_food_name)
        private val deleteButton: Button = itemView.findViewById(R.id.btn_delete_food)

        fun bind(
            food: MealActivity.FoodData,
            clickListener: (MealActivity.FoodData) -> Unit,
            deleteListener: (Int) -> Unit,
            position: Int
        ) {
            foodNameTextView.text = food.name
            itemView.setOnClickListener { clickListener(food) }
            deleteButton.setOnClickListener { deleteListener(position) }
        }
    }
}
