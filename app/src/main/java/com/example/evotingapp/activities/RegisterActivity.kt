package com.example.evotingapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.evotingapp.activities.voter.VoterDashboardActivity
import com.example.evotingapp.databinding.ActivityRegisterBinding
import com.example.evotingapp.repositories.AuthRepository
import com.example.evotingapp.utils.hide
import com.example.evotingapp.utils.show
import com.example.evotingapp.utils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            val nationalId = binding.etNationalId.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            // Validation
            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()
                || confirmPassword.isEmpty() || nationalId.isEmpty() || phone.isEmpty()
            ) {
                toast("Please fill in all fields")
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                toast("Passwords do not match")
                return@setOnClickListener
            }

            if (password.length < 6) {
                toast("Password must be at least 6 characters")
                return@setOnClickListener
            }

            binding.progressBar.show()
            binding.btnRegister.isEnabled = false

            CoroutineScope(Dispatchers.Main).launch {
                val result = authRepository.register(fullName, email, password, nationalId, phone)
                binding.progressBar.hide()
                binding.btnRegister.isEnabled = true

                result.fold(
                    onSuccess = {
                        toast("Registration successful!")
                        startActivity(Intent(this@RegisterActivity, VoterDashboardActivity::class.java))
                        finish()
                    },
                    onFailure = { e ->
                        toast(e.message ?: "Registration failed")
                    }
                )
            }
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }
}