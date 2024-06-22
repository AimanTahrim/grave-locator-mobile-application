package com.example.glmaadmin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AdminLoginViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    val adminEmail = "glma00@gmail.com"

    private val _progress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> get() = _progress

    private val _loginResult = MutableLiveData<LoginResult?>()
    val loginResult: LiveData<LoginResult?> get() = _loginResult

    fun login(email: String, password: String) {
        _progress.value = true
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            _progress.value = false
            if (task.isSuccessful) {
                _loginResult.value = LoginResult(success = true)
            } else {
                _loginResult.value = LoginResult(error = task.exception?.message ?: "Login failed")
            }
        }
    }

    fun resetPassword(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _loginResult.value = LoginResult(success = true)
            } else {
                _loginResult.value = LoginResult(error = task.exception?.message ?: "Reset password failed")
            }
        }
    }
}

data class LoginResult(val success: Boolean = false, val error: String? = null)