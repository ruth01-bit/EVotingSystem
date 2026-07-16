package com.example.evotingapp.activities.voter

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.evotingapp.databinding.ActivityVoteReviewBinding
import com.example.evotingapp.repositories.VoteRepository
import com.example.evotingapp.utils.Constants
import com.example.evotingapp.utils.hide
import com.example.evotingapp.utils.show
import com.example.evotingapp.utils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VoteReviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVoteReviewBinding
    private val voteRepository = VoteRepository()
    private var electionId = ""
    private var electionTitle = ""
    private var candidateId = ""
    private var candidateName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoteReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        electionId = intent.getStringExtra(Constants.EXTRA_ELECTION_ID) ?: ""
        electionTitle = intent.getStringExtra(Constants.EXTRA_ELECTION_TITLE) ?: ""
        candidateId = intent.getStringExtra(Constants.EXTRA_CANDIDATE_ID) ?: ""
        candidateName = intent.getStringExtra(Constants.EXTRA_CANDIDATE_NAME) ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Review Your Vote"

        binding.tvElectionTitle.text = electionTitle
        binding.tvCandidateName.text = candidateName

        binding.btnConfirmVote.setOnClickListener {
            showConfirmDialog()
        }

        binding.btnChangeVote.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Vote")
            .setMessage("Are you sure you want to vote for $candidateName? This cannot be undone.")
            .setPositiveButton("Yes, Vote") { _, _ -> submitVote() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun submitVote() {
        binding.progressBar.show()
        binding.btnConfirmVote.isEnabled = false

        CoroutineScope(Dispatchers.Main).launch {
            val result = voteRepository.castVote(electionId, candidateId)
            binding.progressBar.hide()

            result.fold(
                onSuccess = {
                    val intent = Intent(this@VoteReviewActivity, VoteSuccessActivity::class.java)
                    intent.putExtra(Constants.EXTRA_CANDIDATE_NAME, candidateName)
                    intent.putExtra(Constants.EXTRA_ELECTION_TITLE, electionTitle)
                    startActivity(intent)
                    finishAffinity()
                },
                onFailure = { e ->
                    binding.btnConfirmVote.isEnabled = true
                    toast(e.message ?: "Failed to cast vote")
                }
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}