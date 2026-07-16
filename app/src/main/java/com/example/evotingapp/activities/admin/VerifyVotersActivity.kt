package com.example.evotingapp.activities.admin

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.evotingapp.adapters.VoterVerifyAdapter
import com.example.evotingapp.databinding.ActivityVerifyVotersBinding
import com.example.evotingapp.models.User
import com.example.evotingapp.utils.Constants
import com.example.evotingapp.utils.hide
import com.example.evotingapp.utils.show
import com.example.evotingapp.utils.toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class VerifyVotersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerifyVotersBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyVotersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Verify Voters"

        loadUnverifiedVoters()
    }

    private fun loadUnverifiedVoters() {
        binding.progressBar.show()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val snapshot = db.collection(Constants.USERS)
                    .whereEqualTo("verified", false)
                    .whereEqualTo("role", Constants.ROLE_VOTER)
                    .get().await()

                val voters = snapshot.toObjects(User::class.java)
                binding.progressBar.hide()

                if (voters.isEmpty()) {
                    binding.tvEmpty.show()
                    binding.rvVoters.hide()
                } else {
                    binding.tvEmpty.hide()
                    binding.rvVoters.show()
                    binding.rvVoters.layoutManager = LinearLayoutManager(this@VerifyVotersActivity)
                    binding.rvVoters.adapter = VoterVerifyAdapter(voters) { user ->
                        showVerifyDialog(user)
                    }
                }
            } catch (e: Exception) {
                binding.progressBar.hide()
                toast(e.message ?: "Failed to load voters")
            }
        }
    }

    private fun showVerifyDialog(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Verify Voter")
            .setMessage("Verify ${user.fullName}?\nNational ID: ${user.nationalId}\nPhone: ${user.phone}")
            .setPositiveButton("Verify") { _, _ ->
                verifyVoter(user.uid)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun verifyVoter(uid: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                db.collection(Constants.USERS)
                    .document(uid)
                    .update("verified", true)
                    .await()
                toast("Voter verified successfully!")
                loadUnverifiedVoters()
            } catch (e: Exception) {
                toast(e.message ?: "Failed to verify voter")
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}