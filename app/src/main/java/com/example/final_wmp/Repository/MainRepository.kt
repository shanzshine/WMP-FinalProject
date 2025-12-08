package com.example.final_wmp.Repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.final_wmp.Domain.BannerModel
import com.example.final_wmp.Domain.CategoryModel
import com.example.final_wmp.Domain.ItemsModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // <-- Digunakan untuk pengambilan data asinkron
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

// Class MainRepository yang sudah diperbaiki menggunakan Coroutines
class MainRepository {

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    // Gunakan Scope untuk menjalankan Coroutines di background (I/O)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    // =======================================================
    // 1. loadBanner - Menggunakan Coroutine
    // =======================================================
    fun loadBanner(): LiveData<MutableList<BannerModel>> {
        val listData = MutableLiveData<MutableList<BannerModel>>()

        ioScope.launch { // Memulai di background thread
            try {
                // Mengambil data dengan await()
                val snapshot = firebaseDatabase.getReference("Banner").get().await()
                val list = mutableListOf<BannerModel>()

                // Parsing di background thread (aman)
                for (childSnapshot in snapshot.children) {
                    childSnapshot.getValue(BannerModel::class.java)?.let { list.add(it) }
                }

                // Mengirim hasilnya kembali ke LiveData
                listData.postValue(list)
            } catch (e: Exception) {
                Log.e("Repo", "Error loading Banner: ${e.message}")
            }
        }
        return listData
    }

    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        val listData = MutableLiveData<MutableList<CategoryModel>>()

        ioScope.launch {
            try {
                val snapshot = firebaseDatabase.getReference("Category").get().await()
                val finalCategoryList = mutableListOf<CategoryModel>()

                // Iterasi langsung melalui children (paling kuat untuk melewati null/gap)
                for (childSnapshot in snapshot.children) {

                    // 💡 Coba ambil sebagai Map<String, Any?> untuk fleksibilitas maksimum
                    val mapData = childSnapshot.value as? Map<String, Any?>

                    if (mapData != null) {
                        // Dapatkan title dan id dari Map
                        val title = mapData["title"] as? String
                        val id = (mapData["id"] as? Number)?.toInt() // Cast dari Number (Long/Double) ke Int

                        // Hanya masukkan kategori jika title-nya valid (tidak null, tidak kosong)
                        if (!title.isNullOrBlank()) {
                            val category = CategoryModel(id = id, title = title)
                            finalCategoryList.add(category)
                        } else {
                            Log.w("REPO_DEBUG", "Item Kategori dilewati: Title kosong/null.")
                        }
                    } else {
                        Log.w("REPO_DEBUG", "Item Kategori dilewati: Null atau tidak berbentuk Map (data rusak).")
                    }
                }

                Log.d("REPO_DEBUG", "Jumlah Kategori Valid Akhir: ${finalCategoryList.size}")

                listData.postValue(finalCategoryList.toMutableList())

            } catch (e: Exception) {
                Log.e("REPO_DEBUG", "KESALAHAN FATAL KATEGORI (Parsing Manual): ${e.message}", e)
                listData.postValue(mutableListOf())
            }
        }
        return listData
    }

    // =======================================================
    // 3. loadPopular - Menggunakan Coroutine (Target utama perbaikan freeze)
    // =======================================================
    fun loadPopular(): LiveData<MutableList<ItemsModel>> {
        val listData = MutableLiveData<MutableList<ItemsModel>>()

        // Kita ambil data dari node "Items" karena data populer biasanya diambil dari semua item
        // Jika Anda bersikeras mengambil dari node "Popular" di DB, ganti "Items" menjadi "Popular"
        ioScope.launch {
            try {
                val snapshot = firebaseDatabase.getReference("Items").get().await()
                val list = mutableListOf<ItemsModel>()

                // Parsing seluruh data Items di background thread
                for (childSnapshot in snapshot.children) {
                    childSnapshot.getValue(ItemsModel::class.java)?.let { item ->
                        list.add(item)
                    }
                }

                // Lakukan filtering dan sorting (jika perlu) di background thread
                // Contoh: ambil 4 item dengan rating tertinggi
                val topItems = list.sortedByDescending { it.rating }.take(4).toMutableList()

                listData.postValue(topItems)
                Log.d("Repo", "Popular items loaded: ${topItems.size}")

            } catch (e: Exception) {
                Log.e("Repo", "Error loading Popular items: ${e.message}")
            }
        }
        return listData
    }

    // =======================================================
    // 4. loadItemCategory - Menggunakan Coroutine (Single value event)
    // =======================================================
    fun loadItemCategory(categoryId: String): LiveData<MutableList<ItemsModel>> {
        val itemsLiveData = MutableLiveData<MutableList<ItemsModel>>()

        ioScope.launch {
            try {
                val ref = firebaseDatabase.getReference("Items")
                // Mengambil Query data dari Firebase
                val snapshot = ref.orderByChild("categoryId").equalTo(categoryId).get().await()

                val list = mutableListOf<ItemsModel>()

                // Parsing di background thread
                for (childSnapshot in snapshot.children) {
                    childSnapshot.getValue(ItemsModel::class.java)?.let { list.add(it) }
                }

                itemsLiveData.postValue(list)
                Log.d("Repo", "Items for category $categoryId loaded: ${list.size}")

            } catch (e: Exception) {
                Log.e("Repo", "Error loading Items by Category: ${e.message}")
            }
        }
        return itemsLiveData
    }
}