package com.example.evotingapp.repositories

import com.example.evotingapp.models.Candidate
import com.example.evotingapp.models.Election
import com.example.evotingapp.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ElectionRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun createElection(election: Election): Result<String> {
        return try {
            val ref = db.collection(Constants.ELECTIONS).document()
            val withId = election.copy(id = ref.id)
            ref.set(withId).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllElections(): Result<List<Election>> {
        return try {
            val snapshot = db.collection(Constants.ELECTIONS).get().await()
            val elections = snapshot.toObjects(Election::class.java)
            Result.success(elections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOpenElections(): Result<List<Election>> {
        return try {
            val snapshot = db.collection(Constants.ELECTIONS)
                .get().await()
            val elections = snapshot.toObjects(Election::class.java)
                .filter { it.status == Constants.STATUS_OPEN }
            Result.success(elections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateElectionStatus(electionId: String, status: String): Result<Unit> {
        return try {
            db.collection(Constants.ELECTIONS)
                .document(electionId)
                .update("status", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCandidate(electionId: String, candidate: Candidate): Result<String> {
        return try {
            val ref = db.collection(Constants.ELECTIONS)
                .document(electionId)
                .collection(Constants.CANDIDATES)
                .document()
            val withId = candidate.copy(id = ref.id, electionId = electionId)
            ref.set(withId).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCandidates(electionId: String): Result<List<Candidate>> {
        return try {
            val snapshot = db.collection(Constants.ELECTIONS)
                .document(electionId)
                .collection(Constants.CANDIDATES)
                .get().await()
            val candidates = snapshot.toObjects(Candidate::class.java)
            Result.success(candidates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
