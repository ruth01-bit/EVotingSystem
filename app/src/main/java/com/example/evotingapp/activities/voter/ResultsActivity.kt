package com.example.evotingapp.activities.voter

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.evotingapp.R
import com.example.evotingapp.adapters.ResultsAdapter
import com.example.evotingapp.databinding.ActivityResultsBinding
import com.example.evotingapp.models.Candidate
import com.example.evotingapp.models.Election
import com.example.evotingapp.repositories.ElectionRepository
import com.example.evotingapp.utils.PdfExportHelper
import com.example.evotingapp.utils.hide
import com.example.evotingapp.utils.show
import com.example.evotingapp.utils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResultsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultsBinding
    private val electionRepository = ElectionRepository()
    private var allElections = listOf<Election>()
    private var allCandidates = mutableMapOf<String, List<Candidate>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Election Results"

        loadResults()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_results, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export -> {
                exportAllResults()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportAllResults() {
        if (allElections.isEmpty()) {
            toast("No results to export")
            return
        }
        allElections.forEach { election ->
            val candidates = allCandidates[election.id] ?: emptyList()
            PdfExportHelper.exportResults(this, election, candidates)
        }
    }

    private fun loadResults() {
        binding.progressBar.show()
        CoroutineScope(Dispatchers.Main).launch {
            val electionsResult = electionRepository.getAllElections()
            binding.progressBar.hide()

            electionsResult.fold(
                onSuccess = { elections ->
                    allElections = elections
                    if (elections.isEmpty()) {
                        binding.tvEmpty.show()
                        binding.rvElections.hide()
                        return@fold
                    }

                    val allResults = mutableListOf<ResultItem>()

                    elections.forEach { election ->
                        allResults.add(
                            ResultItem.ElectionHeader(election.title, election.status)
                        )
                        val candidatesResult = electionRepository.getCandidates(election.id)
                        candidatesResult.fold(
                            onSuccess = { candidates ->
                                allCandidates[election.id] = candidates
                                val sorted = candidates.sortedByDescending { it.voteCount }
                                val totalVotes = candidates.sumOf { it.voteCount }
                                sorted.forEach { candidate ->
                                    allResults.add(
                                        ResultItem.CandidateResult(candidate, totalVotes)
                                    )
                                }
                            },
                            onFailure = {}
                        )
                    }

                    binding.tvEmpty.hide()
                    binding.rvElections.show()
                    binding.rvElections.layoutManager =
                        LinearLayoutManager(this@ResultsActivity)
                    binding.rvElections.adapter = ResultsAdapter(allResults)
                },
                onFailure = { e ->
                    toast(e.message ?: "Failed to load results")
                }
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

sealed class ResultItem {
    data class ElectionHeader(val title: String, val status: String) : ResultItem()
    data class CandidateResult(val candidate: Candidate, val totalVotes: Int) : ResultItem()
}
