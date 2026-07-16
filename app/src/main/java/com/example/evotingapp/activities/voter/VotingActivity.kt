package com.example.evotingapp.activities.voter

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.evotingapp.adapters.CandidateAdapter
import com.example.evotingapp.databinding.ActivityVotingBinding
import com.example.evotingapp.repositories.ElectionRepository
import com.example.evotingapp.utils.Constants
import com.example.evotingapp.utils.hide
import com.example.evotingapp.utils.show
import com.example.evotingapp.utils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VotingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVotingBinding
    private val electionRepository = ElectionRepository()
    private var electionId = ""
    private var electionTitle = ""
    private var selectedCandidateId = ""
    private var selectedCandidateName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVotingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        electionId = intent.getStringExtra(Constants.EXTRA_ELECTION_ID) ?: ""
        electionTitle = intent.getStringExtra(Constants.EXTRA_ELECTION_TITLE) ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = electionTitle

        loadCandidates()

        binding.btnVote.setOnClickListener {
            if (selectedCandidateId.isEmpty()) {
                toast("Please select a candidate")
                return@setOnClickListener
            }
            val intent = Intent(this, VoteReviewActivity::class.java)
            intent.putExtra(Constants.EXTRA_ELECTION_ID, electionId)
            intent.putExtra(Constants.EXTRA_ELECTION_TITLE, electionTitle)
            intent.putExtra(Constants.EXTRA_CANDIDATE_ID, selectedCandidateId)
            intent.putExtra(Constants.EXTRA_CANDIDATE_NAME, selectedCandidateName)
            startActivity(intent)
        }
    }

    private fun loadCandidates() {
        binding.progressBar.show()
        CoroutineScope(Dispatchers.Main).launch {
            val result = electionRepository.getCandidates(electionId)
            binding.progressBar.hide()

            result.fold(
                onSuccess = { candidates ->
                    binding.rvCandidates.layoutManager =
                        LinearLayoutManager(this@VotingActivity)
                    binding.rvCandidates.adapter = CandidateAdapter(candidates) { candidate ->
                        selectedCandidateId = candidate.id
                        selectedCandidateName = candidate.name
                        binding.btnVote.isEnabled = true
                        binding.tvSelectedCandidate.text =
                            "Selected: ${candidate.name} — ${candidate.party}"
                        binding.tvSelectedCandidate.setTextColor(
                            android.graphics.Color.parseColor("#1A237E")
                        )
                    }
                },
                onFailure = { e ->
                    toast(e.message ?: "Failed to load candidates")
                }
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
