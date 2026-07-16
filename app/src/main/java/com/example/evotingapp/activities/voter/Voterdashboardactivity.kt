package com.example.evotingapp.activities.voter

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.evotingapp.activities.LoginActivity
import com.example.evotingapp.databinding.ActivityVoterDashboardBinding
import com.example.evotingapp.repositories.AuthRepository
import com.example.evotingapp.repositories.VoteRepository
import com.example.evotingapp.utils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VoterDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVoterDashboardBinding
    private val authRepository = AuthRepository()
    private val voteRepository = VoteRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoterDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserInfo()

        binding.cardVote.setOnClickListener {
            startActivity(Intent(this, ElectionsActivity::class.java))
        }

        binding.cardResults.setOnClickListener {
            startActivity(Intent(this, ResultsActivity::class.java))
        }

        binding.cardProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            authRepository.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun loadUserInfo() {
        CoroutineScope(Dispatchers.Main).launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                binding.tvWelcome.text = "Welcome, ${user.fullName}"
                binding.tvVerificationStatus.text = if (user.verified)
                    "✓ Verified Voter" else "⚠ Pending Verification"

                // Show per-election voting status
                val votedIds = voteRepository.getVotedElectionIds()
                binding.tvVotingStatus.text = if (votedIds.isEmpty())
                    "You have not voted in any election yet"
                else
                    "✓ Voted in ${votedIds.size} election(s)"
            } else {
                toast("Failed to load user info")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()
    }
}
