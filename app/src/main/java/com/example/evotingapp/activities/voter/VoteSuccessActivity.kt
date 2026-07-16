package com.example.evotingapp.activities.voter

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.evotingapp.activities.LoginActivity
import com.example.evotingapp.databinding.ActivityVoteSuccessBinding
import com.example.evotingapp.utils.Constants

class VoteSuccessActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVoteSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVoteSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val candidateName = intent.getStringExtra(Constants.EXTRA_CANDIDATE_NAME) ?: ""
        val electionTitle = intent.getStringExtra(Constants.EXTRA_ELECTION_TITLE) ?: ""

        binding.tvSuccessMessage.text = "Your vote for $candidateName in $electionTitle has been recorded!"

        binding.btnBackHome.setOnClickListener {
            startActivity(Intent(this, VoterDashboardActivity::class.java))
            finishAffinity()
        }

        binding.btnViewResults.setOnClickListener {
            startActivity(Intent(this, ResultsActivity::class.java))
            finish()
        }
    }
}