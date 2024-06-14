package com.example.glmaclient.persistentcloudanchor

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.glmaclient.persistentcloudanchor.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FeedbackClient : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextMessage: EditText
    private lateinit var submitButton: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback_client)

        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextMessage = findViewById(R.id.editTextMessage)
        submitButton = findViewById(R.id.submitButton)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        // Initialize ProgressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Submitting feedback...")
        progressDialog.setCancelable(false)

        val backArrow: ImageView = findViewById(R.id.backarrow)
        backArrow.setOnClickListener {
            val intent = Intent(this, HomePageClient::class.java)
            startActivity(intent)
        }

        // Set email field with user's email from Firebase Authentication
        val currentUser = firebaseAuth.currentUser
        editTextEmail.setText(currentUser?.email)

        submitButton.setOnClickListener {
            submitFeedback()
        }
    }

    private fun submitFeedback() {
        val name = editTextName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val message = editTextMessage.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog.show() // Show progress dialog

        val feedback = Feedback(name, email, message)

        // Save the feedback under a common feedback node in Firebase Realtime Database
        val feedbackReference = databaseReference.child("feedback").push()
        feedbackReference.setValue(feedback)
            .addOnSuccessListener {
                progressDialog.dismiss() // Dismiss progress dialog
                Toast.makeText(this, "Feedback submitted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                progressDialog.dismiss() // Dismiss progress dialog
                Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show()
            }
    }
}
