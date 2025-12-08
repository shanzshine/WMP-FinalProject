package com.example.final_wmp.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.final_wmp.Adapter.CategoryAdapter
import com.example.final_wmp.Adapter.PopularAdapter
import com.example.final_wmp.R
import com.example.final_wmp.ViewModel.MainViewModel
import com.example.final_wmp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var realtimeDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        realtimeDatabase = FirebaseDatabase.getInstance()

        if (auth.currentUser == null) {
            Log.d("AuthCheck", "User not logged in. Redirecting to LoginActivity.")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        Log.d("AuthCheck", "User ${auth.currentUser?.uid} is logged in. Proceeding with setup.")

        setupWindowInsets()

        initBanner()
        initCategory()
        initPopular()
        initBottomMenu()
        initProfileInfo()
        initClickListeners()

        fetchDataFromRealtimeDatabase()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun fetchDataFromRealtimeDatabase() {
        val myRef = realtimeDatabase.getReference("greeting")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val value = snapshot.getValue(String::class.java)
                    Log.d("RealtimeDB", "Data from Realtime DB read successfully: $value")
                } else {
                    Log.d("RealtimeDB", "No data found at this path.")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("RealtimeDB", "Failed to read data. Check Security Rules.", error.toException())
            }
        })
    }

    private fun initProfileInfo() {
        val currentUser = auth.currentUser!!
        firestore.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name")
                    binding.textView2.text = name ?: "User"
                } else {
                    binding.textView2.text = "User"
                    Log.w("MainActivity", "User document does not exist in Firestore.")
                }
            }
            .addOnFailureListener { exception ->
                binding.textView2.text = "User"
                Log.e("MainActivity", "Failed to get user data from Firestore.", exception)
            }
    }

    private fun initBottomMenu() {
        binding.shoppingCartButton.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    private fun initClickListeners() {
        binding.imageView3.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.profileuserBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // --- PERBAIKAN: Tambahkan Listener untuk "See All" ---
        binding.textView4.setOnClickListener {
            val intent = Intent(this, ItemsListActivity::class.java)
            // Kita kirim ID "ALL" sebagai tanda untuk mengambil semua data
            intent.putExtra("id", "ALL")
            intent.putExtra("title", "All Menu")
            startActivity(intent)
        }
    }

    private fun initPopular() {
        binding.progressBarPopular.visibility = View.VISIBLE
        viewModel.loadPopular().observe(this, Observer { listPopular ->
            binding.recyclerViewPopular.layoutManager = GridLayoutManager(this, 2)
            binding.recyclerViewPopular.adapter = PopularAdapter(listPopular)
            binding.progressBarPopular.visibility = View.GONE
        })
    }

    private fun initCategory() {
        binding.progressBarCategory.visibility = View.VISIBLE
        viewModel.loadCategory().observe(this, Observer { listCategory ->
            binding.categoryView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.categoryView.adapter = CategoryAdapter(listCategory)
            binding.progressBarCategory.visibility = View.GONE
        })
    }

    private fun initBanner() {
        binding.progressBarBanner.visibility = View.VISIBLE
        viewModel.loadBanner().observe(this, Observer { listBanner ->
            if (listBanner.isNotEmpty()) {
                Glide.with(this)
                    .load(listBanner[0].url)
                    .into(binding.banner)
            }
            binding.progressBarBanner.visibility = View.GONE
        })
    }
}