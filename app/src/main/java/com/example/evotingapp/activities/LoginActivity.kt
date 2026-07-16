package com.example.evotingapp.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.evotingapp.activities.admin.AdminDashboardActivity
import com.example.evotingapp.activities.voter.VoterDashboardActivity
import com.example.evotingapp.databinding.ActivityLoginBinding
import com.example.evotingapp.repositories.AuthRepository
import com.example.evotingapp.utils.Constants
import com.example.evotingapp.utils.GoogleSignInHelper
import com.example.evotingapp.utils.hide
import com.example.evotingapp.utils.show
import com.example.evotingapp.utils.toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val authRepository = AuthRepository()
    private val auth = FirebaseAuth.getInstance()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account: GoogleSignInAccount =
                    task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    handleGoogleSignIn(token)
                } ?: toast("Google Sign-In failed")
            } catch (e: ApiException) {
                toast("Google Sign-In failed: ${e.message}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                toast("Please fill in all fields")
                return@setOnClickListener
            }

            binding.progressBar.show()
            binding.btnLogin.isEnabled = false

            CoroutineScope(Dispatchers.Main).launch {
                val result = authRepository.login(email, password)
                binding.progressBar.hide()
                binding.btnLogin.isEnabled = true

                result.fold(
                    onSuccess = { user ->
                        toast("Welcome, ${user.fullName}!")
                        navigateByRole(user.role)
                    },
                    onFailure = { e ->
                        toast(e.message ?: "Login failed")
                    }
                )
            }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            val signInIntent = GoogleSignInHelper.getSignInIntent(this)
            googleSignInLauncher.launch(signInIntent)
        }

        // Forgot Password
        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun showForgotPasswordDialog() {
        val input = android.widget.EditText(this).apply {
            hint = "Enter your email address"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)
            .setTitle("Reset Password")
            .setMessage("Enter your email to receive a password reset link")
            .setView(input)
            .setPositiveButton("Send Reset Link") { _, _ ->
                val email = input.text.toString().trim()
                if (email.isEmpty()) {
                    toast("Please enter your email")
                    return@setPositiveButton
                }
                sendPasswordReset(email)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                toast("Reset link sent to $email")
            }
            .addOnFailureListener { e ->
                toast(e.message ?: "Failed to send reset email")
            }
    }

    private fun handleGoogleSignIn(idToken: String) {
        binding.progressBar.show()
        CoroutineScope(Dispatchers.Main).launch {
            val result = GoogleSignInHelper.handleSignInResult(idToken)
            binding.progressBar.hide()

            result.fold(
                onSuccess = { user ->
                    toast("Welcome, ${user.fullName}!")
                    navigateByRole(user.role)
                },
                onFailure = { e ->
                    toast(e.message ?: "Google Sign-In failed")
                }
            )
        }
    }

    private fun navigateByRole(role: String) {
        if (role == Constants.ROLE_ADMIN) {
            startActivity(Intent(this, AdminDashboardActivity::class.java))
        } else {
            startActivity(Intent(this, VoterDashboardActivity::class.java))
        }
        finish()
    }
}
