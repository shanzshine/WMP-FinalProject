package com.example.final_wmp.Helper

import android.content.Context
import android.widget.Toast
import com.example.final_wmp.Adapter.ChangeNumberItemsListener
import com.example.final_wmp.Domain.ItemsModel
import com.google.firebase.auth.FirebaseAuth // <-- TAMBAHKAN IMPORT INI

class ManagmentCart(val context: Context) {

    private val tinyDB = TinyDB(context)

    // --- PERUBAHAN UTAMA ADA DI SINI ---
    // 1. Buat sebuah properti privat untuk mendapatkan kunci keranjang yang unik per pengguna
    private val cartKey: String
        get() {
            val currentUser = FirebaseAuth.getInstance().currentUser
            // Jika ada pengguna yang login, gunakan UID-nya. Jika tidak, gunakan "GUEST".
            val userId = currentUser?.uid ?: "GUEST"
            return "CartList_$userId"
        }
    // --- AKHIR PERUBAHAN UTAMA ---


    fun insertItems(item: ItemsModel) {
        val listItem = getListCart()
        val existAlready = listItem.any { it.title == item.title }
        val index = listItem.indexOfFirst { it.title == item.title }

        if (existAlready) {
            listItem[index].numberInCart = item.numberInCart
        } else {
            listItem.add(item)
        }
        // Gunakan kunci dinamis (cartKey)
        tinyDB.putListObject(cartKey, listItem)
        Toast.makeText(context, "Added to your Cart", Toast.LENGTH_SHORT).show()
    }

    fun getListCart(): ArrayList<ItemsModel> {
        // Gunakan kunci dinamis (cartKey)
        return tinyDB.getListObject(cartKey) ?: arrayListOf()
    }

    fun minusItem(listItems: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
        if (listItems[position].numberInCart == 1) {
            listItems.removeAt(position)
        } else {
            listItems[position].numberInCart--
        }
        // Gunakan kunci dinamis (cartKey)
        tinyDB.putListObject(cartKey, listItems)
        listener.onChanged()
    }

    fun removeItem(listItems: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
        listItems.removeAt(position)
        // Gunakan kunci dinamis (cartKey)
        tinyDB.putListObject(cartKey, listItems)
        listener.onChanged()
    }

    fun plusItem(listItems: ArrayList<ItemsModel>, position: Int, listener: ChangeNumberItemsListener) {
        listItems[position].numberInCart++
        // Gunakan kunci dinamis (cartKey)
        tinyDB.putListObject(cartKey, listItems)
        listener.onChanged()
    }

    fun getTotalFee(): Double {
        val listItem = getListCart()
        var fee = 0.0
        for (item in listItem) {
            fee += item.price * item.numberInCart
        }
        return fee
    }

    fun clearCart() {
        val emptyList = arrayListOf<ItemsModel>()
        // Gunakan kunci dinamis (cartKey) untuk membersihkan keranjang pengguna saat ini
        tinyDB.putListObject(cartKey, emptyList)
    }
}
