package com.example.final_wmp.ViewModel

import android.R
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.final_wmp.Domain.BannerModel
import com.example.final_wmp.Domain.CategoryModel
import com.example.final_wmp.Domain.ItemsModel
import com.example.final_wmp.Repository.MainRepository

class MainViewModel: ViewModel() {
    private val repository= MainRepository()

    fun loadBanner(): LiveData<MutableList<BannerModel>>{
        return repository.loadBanner()
    }

    fun loadCategory(): LiveData<MutableList<CategoryModel>>{
        return repository.loadCategory()
    }

    fun loadPopular(): LiveData<MutableList<ItemsModel>>{
        return repository.loadPopular()
    }

    fun loadItems(categoryId: String): LiveData<MutableList<ItemsModel>> {
        return repository.loadItemCategory(categoryId)
    }

}