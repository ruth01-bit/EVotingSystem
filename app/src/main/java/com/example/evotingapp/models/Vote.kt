package com.example.evotingapp.models

data class Vote(
    val uid: String = "",
    val electionId: String = "",
    val candidateId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
