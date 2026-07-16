package com.example.evotingapp.activities.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.evotingapp.activities.LoginActivity
import com.example.evotingapp.databinding.ActivityAdminDashboardBinding
import com.example.evotingapp.repositories.AuthRepository
import com.example.evotingapp.repositories.ElectionRepository
import com.example.evotingapp.utils.Constants
import com.example.evotingapp.utils.toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    private val authRepository = AuthRepository()
    private val electionRepository = ElectionRepository()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadAdminInfo()
        loadStats()

        binding.cardManageElections.setOnClickListener {
            startActivity(Intent(this, ManageElectionsActivity::class.java))
        }

        binding.cardVerifyVoters.setOnClickListener {
            startActivity(Intent(this, VerifyVotersActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            authRepository.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun loadAdminInfo() {
        CoroutineScope(Dispatchers.Main).launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                binding.tvWelcome.text = "Welcome, ${user.fullName}"
            }
        }
    }

    private fun loadStats() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Election stats
                val electionsResult = electionRepository.getAllElections()
                electionsResult.fold(
                    onSuccess = { elections ->
                        val total = elections.size
                        val open = elections.count { it.status == Constants.STATUS_OPEN }
                        val closed = elections.count { it.status == Constants.STATUS_CLOSED }
                        binding.tvTotalElections.text = "Total Elections: $total"
                        binding.tvOpenElections.text = "Open: $open"
                        binding.tvClosedElections.text = "Closed: $closed"
                    },
                    onFailure = {}
                )

                // Voter stats
                val usersSnapshot = db.collection(Constants.USERS)
                    .whereEqualTo("role", Constants.ROLE_VOTER)
                    .get().await()

                val totalVoters = usersSnapshot.size()
                val verifiedVoters = usersSnapshot.documents.count {
                    it.getBoolean("verified") == true
                }
                val votedVoters = usersSnapshot.documents.count {
                    it.getBoolean("hasVoted") == true
                }
                val pendingVoters = totalVoters - verifiedVoters

                binding.tvTotalVoters.text = "Total Voters: $totalVoters"
                binding.tvVerifiedVoters.text = "Verified: $verifiedVoters"
                binding.tvVotedVoters.text = "Voted: $votedVoters"
                binding.tvPendingVoters.text = "Pending Verification: $pendingVoters"

            } catch (e: Exception) {
                toast(e.message ?: "Failed to load stats")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadStats()
    }
}