package com.example.glmaclient.persistentcloudanchor

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.glmaclient.persistentcloudanchor.databinding.ActivitySignUpClientBinding

class SignUpClient : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpClientBinding
    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val progressDialog = ProgressDialog(this).apply {
            setMessage("Registering...")
            setCancelable(false)
        }

        viewModel.progress.observe(this) { isLoading ->
            if (isLoading) progressDialog.show() else progressDialog.dismiss()
        }

        viewModel.signupResult.observe(this) { result ->
            result?.let {
                if (it.success) {
                    Toast.makeText(this, "User Registered Successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginClient::class.java))
                    finish()
                } else {
                    Toast.makeText(this, it.error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.SignUpButton.setOnClickListener {
            val name = binding.SignUpName.text.toString()
            val email = binding.SignUpEmail.text.toString()
            val password = binding.SignUpPassword.text.toString()
            val confirmPassword = binding.SignUpConfirmPassword.text.toString()
            viewModel.signup(name, email, password, confirmPassword)
        }

        binding.LoginRedirectText.setOnClickListener {
            startActivity(Intent(this, LoginClient::class.java))
        }
    }
}