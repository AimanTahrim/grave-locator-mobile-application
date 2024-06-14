package com.example.glmaadmin

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.glmaadmin.databinding.ActivityAddDataAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar

class AddDataAdmin : AppCompatActivity() {

    private lateinit var binding: ActivityAddDataAdminBinding
    private var selectedImageUri: Uri? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDataAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading Data...")
        progressDialog.setCancelable(false)

        val backArrow: ImageView = findViewById(R.id.backarrow)
        backArrow.setOnClickListener {
            val intent = Intent(this, ManageDeceasedAdmin::class.java)
            startActivity(intent)
        }

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()

        // Setup date pickers for birth date and death date
        binding.birthDateAdmin.setOnClickListener {
            showDatePickerDialog(binding.birthDateAdmin)
        }

        binding.deathDateAdmin.setOnClickListener {
            showDatePickerDialog(binding.deathDateAdmin)
        }

        // Setup image selection
        binding.selectImageButton.setOnClickListener {
            selectImage()
        }

        binding.submitButtonAdmin.setOnClickListener {
            Log.d("AddDataAdmin", "Submit button clicked")
            val deceasedNameAdmin = binding.deceasedNameAdmin.text.toString()
            val birthDateAdmin = binding.birthDateAdmin.text.toString()
            val deathDateAdmin = binding.deathDateAdmin.text.toString()
            val lotNumberAdmin = binding.lotNumberAdmin.text.toString()

            // Check if lotNumberAdmin is a valid integer
            if (lotNumberAdmin.isNotEmpty() && lotNumberAdmin.toIntOrNull() != null) {
                checkIfLotNumberExists(deceasedNameAdmin, birthDateAdmin, deathDateAdmin, lotNumberAdmin)
            } else {
                // Show error message if lot number is not a valid integer
                Toast.makeText(this, "Please enter a valid lot number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePickerDialog(textView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "${selectedDay}/${selectedMonth + 1}/${selectedYear}"
                textView.text = selectedDate
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private val selectImageResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                displayImage(uri)
            }
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageResultLauncher.launch(intent)
    }

    private fun displayImage(uri: Uri) {
        val bitmap: Bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(this.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
        binding.selectedImageView.setImageBitmap(bitmap)
    }

    private fun checkIfLotNumberExists(deceasedNameAdmin: String, birthDateAdmin: String, deathDateAdmin: String, lotNumberAdmin: String) {
        databaseReference.child("approved").orderByChild("lotNumber").equalTo(lotNumberAdmin)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Lot number already exists
                        Toast.makeText(this@AddDataAdmin, "Lot number already exists. Please enter a different lot number.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Lot number is unique
                        showConfirmationDialog(deceasedNameAdmin, birthDateAdmin, deathDateAdmin, lotNumberAdmin)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AddDataAdmin, "Failed to check lot number. Please try again.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showConfirmationDialog(deceasedNameAdmin: String, birthDateAdmin: String, deathDateAdmin: String, lotNumberAdmin: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Submission")
        builder.setMessage("Are you sure you want to submit the details?")

        builder.setPositiveButton("Proceed") { dialog, which ->
            Log.d("AddDataAdmin", "Proceed button clicked")
            if (selectedImageUri != null) {
                progressDialog.show()
                uploadImageToStorage(deceasedNameAdmin, birthDateAdmin, deathDateAdmin, lotNumberAdmin, selectedImageUri!!)
            } else {
                Toast.makeText(this, "Please select an image before proceeding", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            Log.d("AddDataAdmin", "Cancel button clicked")
            clearForm()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun clearForm() {
        Log.d("AddDataAdmin", "Clearing form")
        binding.deceasedNameAdmin.text.clear()
        binding.birthDateAdmin.text = ""
        binding.deathDateAdmin.text = ""
        binding.lotNumberAdmin.text.clear()
        binding.selectedImageView.setImageBitmap(null)
        selectedImageUri = null
        Toast.makeText(this, "Form cleared", Toast.LENGTH_SHORT).show()
    }

    private fun uploadImageToStorage(deceasedNameAdmin: String, birthDateAdmin: String, deathDateAdmin: String, lotNumberAdmin: String, imageUri: Uri) {
        val storageRef = storage.reference
        val imagesRef = storageRef.child("images")
        val imageFileName = "${System.currentTimeMillis()}.jpg"
        val imageFileRef = imagesRef.child(imageFileName)
        val uploadTask = imageFileRef.putFile(imageUri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Image uploaded successfully, get the download URL
            imageFileRef.downloadUrl.addOnSuccessListener { uri ->
                // Save the image URL to the Realtime Database
                saveDataToRealtimeDatabase(deceasedNameAdmin, birthDateAdmin, deathDateAdmin, lotNumberAdmin, uri.toString())
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDataToRealtimeDatabase(deceasedNameAdmin: String, birthDateAdmin: String, deathDateAdmin: String, lotNumberAdmin: String, imageUrl: String?) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            progressDialog.dismiss()
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val data = mapOf(
            "deceasedName" to deceasedNameAdmin,
            "birthDate" to birthDateAdmin,
            "deathDate" to deathDateAdmin,
            "lotNumber" to lotNumberAdmin,
            "lotPhoto" to (imageUrl ?: ""),
            "submittedBy" to userId,
            "status" to "approved"
        )

        Log.d("AddDataAdmin", "Saving data to Realtime Database: userId=$userId, data=$data")

        databaseReference.child("approved").push()
            .setValue(data)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Data submitted successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ManageDeceasedAdmin::class.java)
                startActivity(intent)
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to submit data", Toast.LENGTH_SHORT).show()
            }
    }
}
