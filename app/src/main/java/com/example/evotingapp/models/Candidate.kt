package com.example.evotingapp.models

data class Candidate(
    val id: String = "",
    val name: String = "",
    val party: String = "",
    val photoUrl: String = "",
    val voteCount: Int = 0,
    val electionId: String = ""
)
