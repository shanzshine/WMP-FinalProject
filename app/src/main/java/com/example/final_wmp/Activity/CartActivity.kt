package com.example.final_wmp.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.final_wmp.Adapter.CartAdapter
import com.example.final_wmp.Adapter.ChangeNumberItemsListener
import com.example.final_wmp.Helper.ManagmentCart
import com.example.final_wmp.databinding.ActivityCartBinding

class CartActivity : AppCompatActivity() {

    lateinit var binding: ActivityCartBinding
    lateinit var managmentCart: ManagmentCart
    private var tax: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managmentCart = ManagmentCart(this)

        calculateCart()
        initCartList()
        setVariable()
        initCheckoutButton()
    }

    private fun initCheckoutButton() {
        binding.checkoutBtn.setOnClickListener {
            // Optional: Check if cart is empty before proceeding
            if (managmentCart.getListCart().isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Checkout Successfully! Thank You.", Toast.LENGTH_SHORT).show()
                managmentCart.clearCart()

                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
                finish()
            }
        }
    }

    private fun initCartList() {
        binding.apply {
            listView.layoutManager =
                LinearLayoutManager(this@CartActivity, LinearLayoutManager.VERTICAL, false)
            listView.adapter = CartAdapter(
                managmentCart.getListCart(),
                this@CartActivity,
                object : ChangeNumberItemsListener {
                    override fun onChanged() {
                        calculateCart()
                    }
                }
            )
        }
    }

    private fun setVariable() {
        binding.backBtn.setOnClickListener { finish() }
    }

    private fun calculateCart() {
        val percentTax = 0.02
        val delivery = 15.0

        val totalFee = managmentCart.getTotalFee()

        tax = totalFee * percentTax
        val itemTotal = totalFee

        val total = totalFee + tax + delivery

        // FIX: Using String.format to limit decimals to 2 digits
        binding.apply {
            totalFeeTxt.text = String.format("$%.2f", itemTotal)
            totalTaxTxt.text = String.format("$%.2f", tax)
            deliveryTxt.text = String.format("$%.2f", delivery)
            totalTxt.text = String.format("$%.2f", total)
        }
    }
}