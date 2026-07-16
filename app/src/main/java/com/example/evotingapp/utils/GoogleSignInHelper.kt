package com.example.evotingapp.utils

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.example.evotingapp.models.User
import kotlinx.coroutines.tasks.await

object GoogleSignInHelper {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getSignInIntent(activity: Activity): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("6802830852-29pbr06b6rfn9h6bqdebnv3n3mfnkirb.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
        return googleSignInClient.signInIntent
    }

    suspend fun handleSignInResult(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user!!

            // Check if user exists in Firestore
            val doc = db.collection(Constants.USERS)
                .document(firebaseUser.uid)
                .get().await()

            val user = if (doc.exists()) {
                doc.toObject(User::class.java)!!
            } else {
                // Create new user
                val newUser = User(
                    uid = firebaseUser.uid,
                    fullName = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    nationalId = "",
                    phone = firebaseUser.phoneNumber ?: "",
                    role = Constants.ROLE_VOTER,
                    hasVoted = false,
                    verified = false
                )
                db.collection(Constants.USERS)
                    .document(firebaseUser.uid)
                    .set(newUser).await()
                newUser
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut(activity: Activity) {
        if (::googleSignInClient.isInitialized) {
            googleSignInClient.signOut()
        }
        auth.signOut()
    }
}