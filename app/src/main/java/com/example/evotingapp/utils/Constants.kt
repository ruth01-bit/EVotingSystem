package com.example.evotingapp.utils

object Constants {
    // Firestore Collections
    const val USERS = "users"
    const val ELECTIONS = "elections"
    const val CANDIDATES = "candidates"
    const val VOTES = "votes"
    const val BALLOTS = "ballots"

    // User Roles
    const val ROLE_VOTER = "VOTER"
    const val ROLE_ADMIN = "ADMIN"

    // Election Status
    const val STATUS_UPCOMING = "UPCOMING"
    const val STATUS_OPEN = "OPEN"
    const val STATUS_CLOSED = "CLOSED"

    // Election Categories
    const val CAT_PRESIDENTIAL = "PRESIDENTIAL"
    const val CAT_GOVERNOR = "GOVERNOR"
    const val CAT_SENATOR = "SENATOR"
    const val CAT_MP = "MP"
    const val CAT_WOMEN_REP = "WOMEN REP"
    const val CAT_MCA = "MCA"

    val ELECTION_CATEGORIES = listOf(
        "PRESIDENTIAL",
        "GOVERNOR",
        "SENATOR",
        "MP",
        "WOMEN REP",
        "MCA"
    )

    // Kenya Counties
    val KENYA_COUNTIES = listOf(
        "Mombasa", "Kwale", "Kilifi", "Tana River", "Lamu", "Taita Taveta",
        "Garissa", "Wajir", "Mandera", "Marsabit", "Isiolo", "Meru",
        "Tharaka Nithi", "Embu", "Kitui", "Machakos", "Makueni", "Nyandarua",
        "Nyeri", "Kirinyaga", "Murang'a", "Kiambu", "Turkana", "West Pokot",
        "Samburu", "Trans Nzoia", "Uasin Gishu", "Elgeyo Marakwet", "Nandi",
        "Baringo", "Laikipia", "Nakuru", "Narok", "Kajiado", "Kericho",
        "Bomet", "Kakamega", "Vihiga", "Bungoma", "Busia", "Siaya",
        "Kisumu", "Homa Bay", "Migori", "Kisii", "Nyamira", "Nairobi"
    )

    // Intent Keys
    const val EXTRA_ELECTION_ID = "election_id"
    const val EXTRA_ELECTION_TITLE = "election_title"
    const val EXTRA_CANDIDATE_ID = "candidate_id"
    const val EXTRA_CANDIDATE_NAME = "candidate_name"
    const val EXTRA_CATEGORY = "category"
}