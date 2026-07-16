package com.example.evotingapp.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.evotingapp.activities.admin.AdminDashboardActivity
import com.example.evotingapp.activities.voter.VoterDashboardActivity
import com.example.evotingapp.databinding.ActivityMainBinding
import com.example.evotingapp.repositories.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginState()
        }, 3000)
    }

    private fun checkLoginState() {
        if (authRepository.isLoggedIn()) {
            CoroutineScope(Dispatchers.Main).launch {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    if (user.role == "ADMIN") {
                        startActivity(Intent(this@MainActivity, AdminDashboardActivity::class.java))
                    } else {
                        startActivity(Intent(this@MainActivity, VoterDashboardActivity::class.java))
                    }
                } else {
                    goToLogin()
                }
                finish()
            }
        } else {
            goToLogin()
        }
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}