package com.example.final_wmp.Activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.final_wmp.R
import com.example.final_wmp.Domain.ItemsModel
import com.example.final_wmp.Helper.ManagmentCart
import com.example.final_wmp.databinding.ActivityDetailBinding
import kotlin.compareTo
import kotlin.dec
import kotlin.inc
import kotlin.toString

class DetailActivity : AppCompatActivity() {
    lateinit var binding: ActivityDetailBinding
    private lateinit var item: ItemsModel
    private lateinit var managmentCart: ManagmentCart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managmentCart= ManagmentCart(this)

        bundle()
    }

    private fun bundle() {
        binding.apply {
            item=intent.getSerializableExtra("object") as ItemsModel

            Glide.with(this@DetailActivity)
                .load(item.picUrl[0])
                .into(binding.picMain)

            titleTxt.text=item.title
            descriptionTxt.text=item.description
            priceTxt.text="$"+item.price
            ratingTxt.text=item.rating.toString()

            addToCartBtn.setOnClickListener {
                item.numberInCart=Integer.valueOf(
                    numberInCartTxt.text.toString()
                )
                managmentCart.insertItems(item)
            }

            backBtn.setOnClickListener { finish() }

            plusBtn.setOnClickListener {
                item.numberInCart++
                numberInCartTxt.text = item.numberInCart.toString()
            }

            minusBtn.setOnClickListener {
                if (item.numberInCart > 1) {
                    item.numberInCart--
                    numberInCartTxt.text = item.numberInCart.toString()
                }
            }

        }
    }
}

