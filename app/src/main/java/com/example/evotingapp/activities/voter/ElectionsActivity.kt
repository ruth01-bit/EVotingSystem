package com.example.evotingapp.activities.voter

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.evotingapp.adapters.ElectionAdapter
import com.example.evotingapp.databinding.ActivityElectionsBinding
import com.example.evotingapp.models.Election
import com.example.evotingapp.repositories.ElectionRepository
import com.example.evotingapp.utils.Constants
import com.example.evotingapp.utils.hide
import com.example.evotingapp.utils.show
import com.example.evotingapp.utils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ElectionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityElectionsBinding
    private val electionRepository = ElectionRepository()
    private var allElections = listOf<Election>()
    private var selectedCategory = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityElectionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Active Elections"

        setupCategoryFilter()
        loadElections()
        setupSearch()
    }

    private fun setupCategoryFilter() {
        val categories = listOf("ALL") + Constants.ELECTION_CATEGORIES
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategoryFilter.adapter = adapter

        binding.spinnerCategoryFilter.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedCategory = categories[position]
                    Log.d("ELECTIONS", "Category selected: $selectedCategory")
                    filterElections(binding.etSearch.text.toString())
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterElections(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterElections(query: String) {
        Log.d("ELECTIONS", "Filtering with query='$query' category='$selectedCategory'")
        Log.d("ELECTIONS", "Total elections before filter: ${allElections.size}")

        var filtered = allElections

        if (selectedCategory != "ALL") {
            filtered = filtered.filter { it.category == selectedCategory }
            Log.d("ELECTIONS", "After category filter: ${filtered.size}")
        }

        if (query.isNotEmpty()) {
            filtered = filtered.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true) ||
                        it.county.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
            }
            Log.d("ELECTIONS", "After search filter: ${filtered.size}")
        }

        if (filtered.isEmpty()) {
            binding.tvEmpty.show()
            binding.rvElections.hide()
            Log.d("ELECTIONS", "Showing empty state")
        } else {
            binding.tvEmpty.hide()
            binding.rvElections.show()
            binding.rvElections.layoutManager = LinearLayoutManager(this)
            binding.rvElections.adapter = ElectionAdapter(filtered) { election ->
                val intent = Intent(this, VotingActivity::class.java)
                intent.putExtra(Constants.EXTRA_ELECTION_ID, election.id)
                intent.putExtra(Constants.EXTRA_ELECTION_TITLE, election.title)
                startActivity(intent)
            }
            Log.d("ELECTIONS", "Showing ${filtered.size} elections")
        }
    }

    private fun loadElections() {
        binding.progressBar.show()
        Log.d("ELECTIONS", "Starting to load elections...")

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = electionRepository.getOpenElections()
                binding.progressBar.hide()

                result.fold(
                    onSuccess = { elections ->
                        Log.d("ELECTIONS", "Loaded ${elections.size} open elections")
                        elections.forEach { election ->
                            Log.d("ELECTIONS", "Election: ${election.title} | status=${election.status} | category=${election.category} | id=${election.id}")
                        }

                        allElections = elections

                        if (elections.isEmpty()) {
                            binding.tvEmpty.show()
                            binding.rvElections.hide()
                            toast("No open elections found in database")
                        } else {
                            filterElections(binding.etSearch.text.toString())
                        }
                    },
                    onFailure = { e ->
                        Log.e("ELECTIONS", "Failed to load: ${e.message}", e)
                        toast("Error: ${e.message}")
                        binding.tvEmpty.show()
                    }
                )
            } catch (e: Exception) {
                binding.progressBar.hide()
                Log.e("ELECTIONS", "Exception: ${e.message}", e)
                toast("Exception: ${e.message}")
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}