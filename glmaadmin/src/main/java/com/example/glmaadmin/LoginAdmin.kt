package com.example.glmaadmin

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.glmaadmin.databinding.ActivityLoginAdminBinding
import com.google.firebase.auth.FirebaseAuth

class LoginAdmin : AppCompatActivity() {

    private lateinit var binding: ActivityLoginAdminBinding
    private val viewModel: AdminLoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val progressDialog = ProgressDialog(this).apply {
            setMessage("Signing in...")
            setCancelable(false)
        }

        viewModel.progress.observe(this) { isLoading ->
            if (isLoading) progressDialog.show() else progressDialog.dismiss()
        }

        viewModel.loginResult.observe(this) { result ->
            result?.let {
                if (it.success) {
                    startActivity(Intent(this, HomePageAdmin::class.java))
                    finish()
                } else {
                    Toast.makeText(this, it.error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.SignInButton.setOnClickListener {
            val email = binding.SignInEmail.text.toString().trim()
            val password = binding.SignInPassword.text.toString().trim()
            if (validateInput(email, password)) {
                if (email == viewModel.adminEmail) {
                    viewModel.login(email, password)
                } else {
                    Toast.makeText(this, "Access denied: Unauthorized email", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.ForgotPasswordRedirectLabel.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                Toast.makeText(this, "Email field cannot be empty", Toast.LENGTH_SHORT).show()
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                false
            }
            password.isEmpty() -> {
                Toast.makeText(this, "Password field cannot be empty", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_forgot, null)
        val userEmail = view.findViewById<EditText>(R.id.editBox)

        builder.setView(view)
        val dialog = builder.create()

        view.findViewById<Button>(R.id.btnReset).setOnClickListener {
            val email = userEmail.text.toString().trim()
            if (validateEmail(email)) {
                viewModel.resetPassword(email)
                dialog.dismiss()
            }
        }
        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
        }
        dialog.show()
    }

    private fun validateEmail(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                Toast.makeText(this, "Email field cannot be empty", Toast.LENGTH_SHORT).show()
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }
}