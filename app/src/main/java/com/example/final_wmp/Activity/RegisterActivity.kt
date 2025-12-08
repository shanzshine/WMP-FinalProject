package com.example.final_wmp.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.final_wmp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val genderList = listOf("Select your gender", "Male", "Female")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, genderList) {
            override fun isEnabled(position: Int): Boolean = position != 0
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.roleSpinner.adapter = adapter

        binding.registerBtn.setOnClickListener { performRegister() }
        binding.loginText.setOnClickListener { finish() }
    }

    private fun performRegister() {
        val name = binding.nameEditTextReg.text.toString().trim()
        val email = binding.emailEditTextReg.text.toString().trim()
        val password = binding.passwordEditTextReg.text.toString().trim()
        val gender = binding.roleSpinner.selectedItem.toString()
        val agree = binding.agreeCheckBox.isChecked

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            binding.statusTextViewReg.text = "All fields are required"
            return
        }
        if (gender == "Select your gender") {
            binding.statusTextViewReg.text = "Please select your gender"
            return
        }
        if (!agree) {
            binding.statusTextViewReg.text = "You must agree to continue"
            return
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser ?: return@addOnCompleteListener

                // Simpan langsung ke Firestore tanpa menunggu updateProfile
                val userMap = hashMapOf(
                    "name" to name,
                    "email" to email,
                    "gender" to gender
                )
                db.collection("users").document(user.uid).set(userMap)

                Toast.makeText(this, "Register successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                binding.statusTextViewReg.text = "Registration failed: ${task.exception?.message}"
            }
        }
    }
}
