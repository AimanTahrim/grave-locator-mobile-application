package com.example.glmaadmin

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.glmaadmin.databinding.ActivityManageAccountAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ManageAccountAdmin : AppCompatActivity() {

    private lateinit var binding: ActivityManageAccountAdminBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageAccountAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("admins").child(firebaseAuth.currentUser!!.uid).child("profile")
        storageReference = FirebaseStorage.getInstance()

        // Initialize ProgressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Updating profile...")
        progressDialog.setCancelable(false)

        loadUserProfile()

        val backArrow: ImageView = findViewById(R.id.backarrow)
        backArrow.setOnClickListener {
            val intent = Intent(this, HomePageAdmin::class.java)
            startActivity(intent)
        }

        binding.changeProfileImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.saveProfileButton.setOnClickListener {
            progressDialog.show() // Show progress dialog
            saveUserProfile()
        }

        binding.changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun loadUserProfile() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userProfile = snapshot.getValue(UserProfile::class.java)
                if (userProfile != null) {
                    binding.profileName.setText(userProfile.name)
                    binding.profileEmail.setText(userProfile.email)
                    if (userProfile.profileImageUrl.isNotEmpty()) {
                        Glide.with(this@ManageAccountAdmin).load(userProfile.profileImageUrl).into(binding.profileImage)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ManageAccountAdmin, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveUserProfile() {
        val name = binding.profileName.text.toString()
        val email = binding.profileEmail.text.toString()

        if (name.isNotEmpty()) {
            val userProfile = UserProfile(email, name)
            databaseReference.setValue(userProfile).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (imageUri != null) {
                        uploadImageToFirebase()
                    } else {
                        progressDialog.dismiss() // Dismiss progress dialog
                        Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    progressDialog.dismiss() // Dismiss progress dialog
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            progressDialog.dismiss() // Dismiss progress dialog
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebase() {
        val storageRef = storageReference.reference.child("profile_images").child(firebaseAuth.currentUser!!.uid + ".jpg")
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = storageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val profileImageUrl = uri.toString()
                databaseReference.child("profileImageUrl").setValue(profileImageUrl).addOnCompleteListener { task ->
                    progressDialog.dismiss() // Dismiss progress dialog
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to update profile image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.addOnFailureListener {
            progressDialog.dismiss() // Dismiss progress dialog
            Toast.makeText(this, "Failed to upload profile image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showChangePasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Change Password")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(input)

        builder.setPositiveButton("Change") { dialog, _ ->
            val newPassword = input.text.toString()
            if (newPassword.isNotEmpty()) {
                changeUserPassword(newPassword)
            } else {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun changeUserPassword(newPassword: String) {
        val user = firebaseAuth.currentUser

        user?.updatePassword(newPassword)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to change password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logoutUser() {
        firebaseAuth.signOut()
        val intent = Intent(this, LoginAdmin::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            binding.profileImage.setImageURI(imageUri)
        }
    }
}
