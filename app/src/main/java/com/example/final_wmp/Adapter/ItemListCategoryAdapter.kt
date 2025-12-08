package com.example.final_wmp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.final_wmp.Activity.DetailActivity
import com.example.final_wmp.Domain.ItemsModel
import com.example.final_wmp.databinding.ViewholderItemListBinding

class ItemListCategoryAdapter(private val items: MutableList<ItemsModel>) :
    RecyclerView.Adapter<ItemListCategoryAdapter.ViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val binding =
            ViewholderItemListBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.titleTxt.text = item.title
        holder.binding.priceTxt.text = "$" + item.price
        holder.binding.subtitleTxt.text = item.extra

        Glide.with(context)
            .load(item.picUrl.firstOrNull()) // Ambil gambar pertama dari list
            .into(holder.binding.pic)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtra("object", item) // Mengirim seluruh objek item ke DetailActivity
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ViewholderItemListBinding) :
        RecyclerView.ViewHolder(binding.root)
}
