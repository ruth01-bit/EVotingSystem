package com.example.evotingapp.activities.admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.evotingapp.databinding.ActivityAddCandidateBinding
import com.example.evotingapp.models.Candidate
import com.example.evotingapp.repositories.ElectionRepository
import com.example.evotingapp.utils.Constants
import com.example.evotingapp.utils.hide
import com.example.evotingapp.utils.show
import com.example.evotingapp.utils.toast
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddCandidateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddCandidateBinding
    private val electionRepository = ElectionRepository()
    private var electionId = ""
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCandidateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        electionId = intent.getStringExtra(Constants.EXTRA_ELECTION_ID) ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Candidate"

        loadCandidates()

        binding.ivCandidatePhoto.setOnClickListener {
            openImagePicker()
        }

        binding.btnSelectPhoto.setOnClickListener {
            openImagePicker()
        }

        binding.btnAddCandidate.setOnClickListener {
            val name = binding.etCandidateName.text.toString().trim()
            val party = binding.etCandidateParty.text.toString().trim()

            if (name.isEmpty() || party.isEmpty()) {
                toast("Please fill in all fields")
                return@setOnClickListener
            }

            if (selectedImageUri != null) {
                uploadPhotoAndAddCandidate(name, party)
            } else {
                addCandidate(name, party, "")
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            Glide.with(this)
                .load(selectedImageUri)
                .circleCrop()
                .into(binding.ivCandidatePhoto)
            binding.btnSelectPhoto.text = "Change Photo"
        }
    }

    private fun uploadPhotoAndAddCandidate(name: String, party: String) {
        binding.progressBar.show()
        binding.btnAddCandidate.isEnabled = false

        val storageRef = FirebaseStorage.getInstance().reference
            .child("candidates/${System.currentTimeMillis()}.jpg")

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val downloadUrl = storageRef.putFile(selectedImageUri!!)
                    .await()
                    .storage
                    .downloadUrl
                    .await()
                    .toString()
                addCandidate(name, party, downloadUrl)
            } catch (e: Exception) {
                binding.progressBar.hide()
                binding.btnAddCandidate.isEnabled = true
                toast("Failed to upload photo: ${e.message}")
            }
        }
    }

    private fun addCandidate(name: String, party: String, photoUrl: String) {
        binding.progressBar.show()
        binding.btnAddCandidate.isEnabled = false

        val candidate = Candidate(
            name = name,
            party = party,
            photoUrl = photoUrl,
            voteCount = 0,
            electionId = electionId
        )

        CoroutineScope(Dispatchers.Main).launch {
            val result = electionRepository.addCandidate(electionId, candidate)
            binding.progressBar.hide()
            binding.btnAddCandidate.isEnabled = true

            result.fold(
                onSuccess = {
                    toast("Candidate added!")
                    binding.etCandidateName.text?.clear()
                    binding.etCandidateParty.text?.clear()
                    selectedImageUri = null
                    binding.ivCandidatePhoto.setImageResource(
                        android.R.drawable.ic_menu_gallery
                    )
                    binding.btnSelectPhoto.text = "Select Photo"
                    loadCandidates()
                },
                onFailure = { e ->
                    toast(e.message ?: "Failed to add candidate")
                }
            )
        }
    }

    private fun loadCandidates() {
        CoroutineScope(Dispatchers.Main).launch {
            val result = electionRepository.getCandidates(electionId)
            result.fold(
                onSuccess = { candidates ->
                    binding.tvCandidateCount.text = "${candidates.size} candidates added"
                    val names = candidates.joinToString("\n") {
                        "• ${it.name} (${it.party})"
                    }
                    binding.tvCandidateList.text =
                        if (names.isEmpty()) "No candidates yet" else names
                },
                onFailure = {}
            )
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
