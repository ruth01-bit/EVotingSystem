package com.example.evotingapp.repositories

import com.example.evotingapp.models.Vote
import com.example.evotingapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class VoteRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun castVote(electionId: String, candidateId: String): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(Exception("Not logged in"))

        return try {
            db.runTransaction { transaction ->
                // References
                val userRef = db.collection(Constants.USERS).document(uid)
                val ballotRef = db.collection(Constants.VOTES)
                    .document(electionId)
                    .collection(Constants.BALLOTS)
                    .document(uid)
                val candidateRef = db.collection(Constants.ELECTIONS)
                    .document(electionId)
                    .collection(Constants.CANDIDATES)
                    .document(candidateId)

                val userSnap = transaction.get(userRef)
                val ballotSnap = transaction.get(ballotRef)
                val candidateSnap = transaction.get(candidateRef)

                // Check if already voted in THIS specific election
                if (ballotSnap.exists()) {
                    throw Exception("You have already voted in this election")
                }

                // Check verified
                if (userSnap.getBoolean("verified") != true) {
                    throw Exception("Your account is not verified yet")
                }

                val currentVotes = candidateSnap.getLong("voteCount") ?: 0

                // Write ballot
                val vote = Vote(
                    uid = uid,
                    electionId = electionId,
                    candidateId = candidateId,
                    timestamp = System.currentTimeMillis()
                )
                transaction.set(ballotRef, vote)

                // Increment candidate vote count
                transaction.update(candidateRef, "voteCount", currentVotes + 1)

                // Update voted elections list on user
                // We no longer set hasVoted=true globally
                // Instead we track per election in the votes collection

            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasUserVotedInElection(electionId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val doc = db.collection(Constants.VOTES)
                .document(electionId)
                .collection(Constants.BALLOTS)
                .document(uid)
                .get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getVotedElectionIds(): List<String> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return try {
            val elections = db.collection(Constants.ELECTIONS).get().await()
            val votedIds = mutableListOf<String>()
            for (election in elections.documents) {
                val ballot = db.collection(Constants.VOTES)
                    .document(election.id)
                    .collection(Constants.BALLOTS)
                    .document(uid)
                    .get().await()
                if (ballot.exists()) {
                    votedIds.add(election.id)
                }
            }
            votedIds
        } catch (e: Exception) {
            emptyList()
        }
    }
}
