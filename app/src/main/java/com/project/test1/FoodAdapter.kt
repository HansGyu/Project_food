package com.project.test1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FoodAdapter(
    private var foodList: List<Food>,
    private val onItemClick: (Food) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        holder.bind(food, onItemClick, onDeleteClick)
    }

    override fun getItemCount(): Int = foodList.size

    fun updateFoodList(newList: List<Food>) {
        foodList = newList
        notifyDataSetChanged()
    }

    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFoodName: TextView = itemView.findViewById(R.id.tv_food_name)
        private val btnDelete: TextView = itemView.findViewById(R.id.btn_delete_food)

        fun bind(food: Food, onItemClick: (Food) -> Unit, onDeleteClick: (Int) -> Unit) {
            tvFoodName.text = food.식품명
            itemView.setOnClickListener { onItemClick(food) }
            btnDelete.setOnClickListener { onDeleteClick(adapterPosition) }
        }
    }
}
