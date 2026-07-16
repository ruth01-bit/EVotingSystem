package com.example.evotingapp.activities.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.evotingapp.R
import com.example.evotingapp.adapters.ElectionAdapter
import com.example.evotingapp.databinding.ActivityManageElectionsBinding
import com.example.evotingapp.models.Election
import com.example.evotingapp.repositories.ElectionRepository
import com.example.evotingapp.utils.Constants
import com.example.evotingapp.utils.hide
import com.example.evotingapp.utils.show
import com.example.evotingapp.utils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManageElectionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManageElectionsBinding
    private val electionRepository = ElectionRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageElectionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Manage Elections"

        loadElections()

        binding.fabAddElection.setOnClickListener {
            showCreateElectionDialog()
        }
    }

    private fun loadElections() {
        binding.progressBar.show()
        CoroutineScope(Dispatchers.Main).launch {
            val result = electionRepository.getAllElections()
            binding.progressBar.hide()

            result.fold(
                onSuccess = { elections ->
                    if (elections.isEmpty()) {
                        binding.tvEmpty.show()
                        binding.rvElections.hide()
                    } else {
                        binding.tvEmpty.hide()
                        binding.rvElections.show()
                        binding.rvElections.layoutManager =
                            LinearLayoutManager(this@ManageElectionsActivity)
                        binding.rvElections.adapter = ElectionAdapter(elections) { election ->
                            showElectionOptionsDialog(election)
                        }
                    }
                },
                onFailure = { e ->
                    toast(e.message ?: "Failed to load elections")
                }
            )
        }
    }

    private fun showCreateElectionDialog() {
        val dialogView = LayoutInflater.from(this).inflate(
            R.layout.dialog_create_election, null
        )
        val etTitle = dialogView.findViewById<EditText>(R.id.etElectionTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etElectionDescription)
        val etConstituency = dialogView.findViewById<EditText>(R.id.etConstituency)
        val etWard = dialogView.findViewById<EditText>(R.id.etWard)
        val spinnerCategory = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerCategory)
        val spinnerCounty = dialogView.findViewById<android.widget.Spinner>(R.id.spinnerCounty)

        // Setup category spinner
        val categoryAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            Constants.ELECTION_CATEGORIES
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        // Setup county spinner
        val countyList = listOf("-- National (No County) --") + Constants.KENYA_COUNTIES
        val countyAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            countyList
        )
        countyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCounty.adapter = countyAdapter

        AlertDialog.Builder(this)
            .setTitle("Create Election")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val category = Constants.ELECTION_CATEGORIES[spinnerCategory.selectedItemPosition]
                val countySelection = spinnerCounty.selectedItemPosition
                val county = if (countySelection == 0) "" else Constants.KENYA_COUNTIES[countySelection - 1]
                val constituency = etConstituency.text.toString().trim()
                val ward = etWard.text.toString().trim()

                if (title.isEmpty()) {
                    toast("Title is required")
                    return@setPositiveButton
                }

                createElection(title, description, category, county, constituency, ward)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createElection(
        title: String,
        description: String,
        category: String,
        county: String,
        constituency: String,
        ward: String
    ) {
        val election = Election(
            title = title,
            description = description,
            category = category,
            county = county,
            constituency = constituency,
            ward = ward,
            status = Constants.STATUS_UPCOMING,
            startTime = System.currentTimeMillis(),
            endTime = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)
        )
        CoroutineScope(Dispatchers.Main).launch {
            val result = electionRepository.createElection(election)
            result.fold(
                onSuccess = {
                    toast("Election created!")
                    loadElections()
                },
                onFailure = { e ->
                    toast(e.message ?: "Failed to create election")
                }
            )
        }
    }

    private fun showElectionOptionsDialog(election: Election) {
        val options = arrayOf("Add Candidates", "Open Election", "Close Election")
        AlertDialog.Builder(this)
            .setTitle(election.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, AddCandidateActivity::class.java)
                        intent.putExtra(Constants.EXTRA_ELECTION_ID, election.id)
                        startActivity(intent)
                    }
                    1 -> updateStatus(election.id, Constants.STATUS_OPEN)
                    2 -> updateStatus(election.id, Constants.STATUS_CLOSED)
                }
            }
            .show()
    }

    private fun updateStatus(electionId: String, status: String) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = electionRepository.updateElectionStatus(electionId, status)
            result.fold(
                onSuccess = {
                    toast("Election status updated!")
                    loadElections()
                },
                onFailure = { e ->
                    toast(e.message ?: "Failed to update status")
                }
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
