package com.example.evotingapp.activities.voter

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.evotingapp.activities.LoginActivity
import com.example.evotingapp.databinding.ActivityProfileBinding
import com.example.evotingapp.repositories.AuthRepository
import com.example.evotingapp.utils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Profile"

        loadProfile()

        binding.btnLogout.setOnClickListener {
            authRepository.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun loadProfile() {
        CoroutineScope(Dispatchers.Main).launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                // Set initials in avatar
                val initials = user.fullName
                    .split(" ")
                    .take(2)
                    .mapNotNull { it.firstOrNull()?.toString() }
                    .joinToString("")
                    .uppercase()
                binding.tvInitials.text = initials

                binding.tvFullName.text = user.fullName
                binding.tvEmail.text = user.email
                binding.tvPhone.text = user.phone.ifEmpty { "Not provided" }
                binding.tvNationalId.text = user.nationalId.ifEmpty { "Not provided" }
                binding.tvRole.text = user.role

                // Verification status
                if (user.verified) {
                    binding.tvVerified.text = "✓"
                    binding.tvVerified.setTextColor(
                        android.graphics.Color.parseColor("#4CAF50")
                    )
                } else {
                    binding.tvVerified.text = "✗"
                    binding.tvVerified.setTextColor(
                        android.graphics.Color.parseColor("#F44336")
                    )
                }

                // Voted status
                if (user.hasVoted) {
                    binding.tvHasVoted.text = "✓"
                    binding.tvHasVoted.setTextColor(
                        android.graphics.Color.parseColor("#4CAF50")
                    )
                } else {
                    binding.tvHasVoted.text = "✗"
                    binding.tvHasVoted.setTextColor(
                        android.graphics.Color.parseColor("#F44336")
                    )
                }
            } else {
                toast("Failed to load profile")
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
