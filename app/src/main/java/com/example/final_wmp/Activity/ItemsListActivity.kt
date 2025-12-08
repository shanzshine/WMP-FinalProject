package com.example.final_wmp.Activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.final_wmp.Adapter.ItemListCategoryAdapter
import com.example.final_wmp.Domain.ItemsModel
import com.example.final_wmp.databinding.ActivityItemsListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query // Pastikan import Query ada
import com.google.firebase.database.ValueEventListener

class ItemsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemsListBinding
    private lateinit var database: FirebaseDatabase

    private var categoryIdStr: String = ""
    private var categoryTitle: String? = null
    private var isLoadAll: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        getIntentExtra()
        fetchItemsFromFirebase()

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun getIntentExtra() {
        // Ambil ID sebagai String dulu
        categoryIdStr = intent.getStringExtra("id") ?: ""
        categoryTitle = intent.getStringExtra("title")

        binding.categoryTxt.text = categoryTitle

        // Cek apakah ID-nya "ALL"
        if (categoryIdStr == "ALL") {
            isLoadAll = true
            Log.d("ItemsList-Check", "Mode: Load ALL Items")
        } else {
            isLoadAll = false
            Log.d("ItemsList-Check", "Mode: Filter by Category ID: $categoryIdStr")
        }
    }

    private fun fetchItemsFromFirebase() {
        // Validasi: Jika bukan Load All dan ID kosong/tidak valid, stop.
        if (!isLoadAll && (categoryIdStr.isEmpty() || categoryIdStr == "-1")) {
            Log.e("ItemsList-Check", "ID Kategori tidak valid. Batal mengambil data.")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.listView.visibility = View.GONE

        val itemsRef = database.getReference("Items")

        // --- LOGIC BARU ---
        // Tentukan Query: Apakah ambil semua atau filter by categoryId
        val query: Query = if (isLoadAll) {
            // Jika "See All", ambil referensi langsung (semua data di node Items)
            itemsRef
        } else {
            // Jika Kategori, filter berdasarkan field categoryId
            itemsRef.orderByChild("categoryId").equalTo(categoryIdStr)
        }

        Log.i("ItemsList-Check", "MEMULAI QUERY ke Firebase...")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.progressBar.visibility = View.GONE

                if (snapshot.exists()) {
                    val itemsList = mutableListOf<ItemsModel>()
                    for (itemSnapshot in snapshot.children) {
                        val item = itemSnapshot.getValue(ItemsModel::class.java)
                        if (item != null) {
                            itemsList.add(item)
                        }
                    }

                    if (itemsList.isNotEmpty()) {
                        Log.i("ItemsList-Check", "SUKSES: Ditemukan ${itemsList.size} item.")
                        binding.listView.visibility = View.VISIBLE
                        setupRecyclerView(itemsList)
                    } else {
                        Log.w("ItemsList-Check", "Snapshot ada, tapi list kosong.")
                    }

                } else {
                    Log.e("ItemsList-Check", "GAGAL: Data tidak ditemukan.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBar.visibility = View.GONE
                Log.e("ItemsList-Check", "Error: ${error.message}")
            }
        })
    }

    private fun setupRecyclerView(items: MutableList<ItemsModel>) {
        binding.listView.layoutManager = GridLayoutManager(this, 2)
        val adapter = ItemListCategoryAdapter(items)
        binding.listView.adapter = adapter
    }
}