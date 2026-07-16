package com.example.evotingapp.models

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val nationalId: String = "",
    val phone: String = "",
    val role: String = "VOTER",
    val hasVoted: Boolean = false,
    val verified: Boolean = false
)
