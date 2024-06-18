package com.project.test1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FoodAdapter(
    private val foodList: MutableList<FoodData>,
    private val clickListener: (String) -> Unit,
    private val deleteListener: (Int) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(foodList[position], clickListener, deleteListener, position)
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodNameTextView: TextView = itemView.findViewById(R.id.tv_food_name)
        private val deleteButton: Button = itemView.findViewById(R.id.btn_delete_food)

        fun bind(
            food: FoodData,
            clickListener: (String) -> Unit,
            deleteListener: (Int) -> Unit,
            position: Int
        ) {
            foodNameTextView.text = food.name
            itemView.setOnClickListener { clickListener(food.name) }
            deleteButton.setOnClickListener { deleteListener(position) }
        }
    }
}
