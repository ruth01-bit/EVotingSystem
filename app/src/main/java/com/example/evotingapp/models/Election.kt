package com.example.evotingapp.models

data class Election(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "PRESIDENTIAL",
    val county: String = "",
    val constituency: String = "",
    val ward: String = "",
    val status: String = "UPCOMING",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val createdBy: String = ""
)