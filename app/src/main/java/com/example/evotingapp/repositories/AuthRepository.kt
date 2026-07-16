package com.example.evotingapp.repositories

import com.example.evotingapp.models.User
import com.example.evotingapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val uid = auth.currentUser!!.uid
            val doc = db.collection(Constants.USERS).document(uid).get().await()
            val user = doc.toObject(User::class.java) ?: return Result.failure(Exception("User not found"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        nationalId: String,
        phone: String
    ): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user!!.uid
            val user = User(
                uid = uid,
                fullName = fullName,
                email = email,
                nationalId = nationalId,
                phone = phone,
                role = Constants.ROLE_VOTER,
                hasVoted = false,
                verified = false
            )
            db.collection(Constants.USERS).document(uid).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): User? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            val doc = db.collection(Constants.USERS).document(uid).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null
}
