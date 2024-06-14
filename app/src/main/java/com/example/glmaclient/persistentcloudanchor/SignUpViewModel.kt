package com.example.glmaclient.persistentcloudanchor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().reference

    private val _progress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> get() = _progress

    private val _signupResult = MutableLiveData<SignupResult?>()
    val signupResult: LiveData<SignupResult?> get() = _signupResult

    fun signup(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _signupResult.value = SignupResult(error = "Fields cannot be empty")
            return
        }

        if (password != confirmPassword) {
            _signupResult.value = SignupResult(error = "Passwords do not match")
            return
        }

        _progress.value = true
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _progress.value = false
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid
                    if (userId != null) {
                        writeNewUser(userId, email, name)
                    }
                    _signupResult.value = SignupResult(success = true)
                } else {
                    _signupResult.value = SignupResult(error = task.exception?.message ?: "Sign up failed")
                }
            }
    }

    private fun writeNewUser(userId: String, email: String, name: String) {
        val userProfile = UserProfile(email, name)
        databaseReference.child("users").child(userId).child("profile").setValue(userProfile)
    }
}

data class SignupResult(val success: Boolean = false, val error: String? = null)