package com.example.glmaclient.persistentcloudanchor

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
import com.example.glmaclient.persistentcloudanchor.R
import com.example.glmaclient.persistentcloudanchor.databinding.ActivityAddDataClientBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.UUID

class AddDataClient : AppCompatActivity() {

    private lateinit var binding: ActivityAddDataClientBinding
    private var selectedImageUri: Uri? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDataClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading Data...")
        progressDialog.setCancelable(false)

        //back button
        val backArrow: ImageView = findViewById(R.id.backarrow)
        backArrow.setOnClickListener {
            showBackAlertDialog()
        }

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()

        // Setup date pickers for birth date and death date
        binding.birthDateClient.setOnClickListener {
            showDatePickerDialog(binding.birthDateClient)
        }

        binding.deathDateClient.setOnClickListener {
            showDatePickerDialog(binding.deathDateClient)
        }

        // Setup image selection
        binding.selectImageButton.setOnClickListener {
            selectImage()
        }

        binding.submitButtonClient.setOnClickListener {
            Log.d("AddDataClient", "Submit button clicked")
            val deceasedNameClient = binding.deceasedNameClient.text.toString()
            val birthDateClient = binding.birthDateClient.text.toString()
            val deathDateClient = binding.deathDateClient.text.toString()
            val lotNumberClient = binding.lotNumberClient.text.toString()

            if (deceasedNameClient.isNotEmpty() && deathDateClient.isNotEmpty()) {
                // Check if lotNumberClient is a valid integer
                if (lotNumberClient.isNotEmpty() && lotNumberClient.toIntOrNull() != null) {
                    checkIfLotNumberExists(deceasedNameClient, birthDateClient, deathDateClient, lotNumberClient)
                } else {
                    // Show error message if lot number is not a valid integer
                    Toast.makeText(this, "Please enter a valid lot number", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Show error message if name or death date is null
                Toast.makeText(this, "Name and death date cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Calendar
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

    private val selectImageResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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

    private fun checkIfLotNumberExists(deceasedNameClient: String, birthDateClient: String, deathDateClient: String, lotNumberClient: String) {
        val approvedReference = FirebaseDatabase.getInstance().getReference("grave")
        approvedReference.orderByChild("lotNumber").equalTo(lotNumberClient)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Lot number already exists
                        Toast.makeText(this@AddDataClient, "Lot number already exists. Please enter a different lot number.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Lot number is unique
                        showConfirmationDialog(deceasedNameClient, birthDateClient, deathDateClient, lotNumberClient)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AddDataClient, "Failed to check lot number. Please try again.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showBackAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Navigation")
        builder.setMessage("Are you sure you want to navigate back? Any unsaved changes will be lost.")

        builder.setPositiveButton("Yes") { dialog, which ->
            Log.d("ManageDeceasedClient", "User confirmed navigation back")
            clearForm()
            val intent = Intent(this, ManageDeceasedClient::class.java)
            startActivity(intent)
        }

        builder.setNegativeButton("No") { dialog, which ->
            Log.d("ManageDeceasedClient", "User cancelled navigation back")
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showConfirmationDialog(deceasedNameClient: String, birthDateClient: String, deathDateClient: String, lotNumberClient: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Submission")
        builder.setMessage("Are you sure you want to submit the details?")

        builder.setPositiveButton("Proceed") { dialog, which ->
            Log.d("AddDataClient", "Proceed button clicked")
            if (selectedImageUri != null) {
                progressDialog.show()
                uploadImageToStorage(deceasedNameClient, birthDateClient, deathDateClient, lotNumberClient, selectedImageUri!!)
            } else {
                Toast.makeText(this, "Please select an image before proceeding", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            Log.d("AddDataClient", "Cancel button clicked")
            showClearConfirmationDialog()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showClearConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Clearing Form")
        builder.setMessage("Are you sure you want to clear the form?")

        builder.setPositiveButton("Yes") { dialog, which ->
            Log.d("AddDataClient", "Clearing form confirmed")
            clearForm()
        }

        builder.setNegativeButton("No") { dialog, which ->
            Log.d("AddDataClient", "Clearing form cancelled")
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun clearForm() {
        Log.d("AddDataClient", "Clearing form")
        binding.deceasedNameClient.text.clear()
        binding.birthDateClient.text = ""
        binding.deathDateClient.text = ""
        binding.lotNumberClient.text.clear()
        binding.selectedImageView.setImageBitmap(null)
        selectedImageUri = null
        Toast.makeText(this, "Form cleared", Toast.LENGTH_SHORT).show()
    }

    private fun uploadImageToStorage(deceasedNameClient: String, birthDateClient: String, deathDateClient: String, lotNumberClient: String, imageUri: Uri) {
        val storageRef = storage.reference
        val imagesRef = storageRef.child("grave_images")
        val imageFileName = "${System.currentTimeMillis()}.jpg"
        val imageFileRef = imagesRef.child(imageFileName)
        val uploadTask = imageFileRef.putFile(imageUri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Image uploaded successfully, get the download URL
            imageFileRef.downloadUrl.addOnSuccessListener { uri ->
                // Save the image URL to the Realtime Database
                saveDataToRealtimeDatabase(deceasedNameClient, birthDateClient, deathDateClient, lotNumberClient, uri.toString())
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            progressDialog.dismiss()
            Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveDataToRealtimeDatabase(deceasedNameClient: String, birthDateClient: String, deathDateClient: String, lotNumberClient: String, imageUrl: String?) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            progressDialog.dismiss()
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Generate a unique ID for the grave entry
        val uniqueId = databaseReference.child("add_pending").push().key ?: ""

        val data = mapOf(
            "deceasedId" to uniqueId,  // Use unique ID as deceasedId
            "deceasedName" to deceasedNameClient,
            "birthDate" to birthDateClient,
            "deathDate" to deathDateClient,
            "lotNumber" to lotNumberClient,
            "lotPhoto" to (imageUrl ?: ""),
            "submittedBy" to userId,
            "status" to "pending"
        )

        Log.d("AddDataClient", "Saving data to Realtime Database: userId=$userId, data=$data")

        databaseReference.child("add_pending").child(uniqueId)
            .setValue(data)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Data submitted for approval", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ManageDeceasedClient::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error submitting data: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
    }

}
