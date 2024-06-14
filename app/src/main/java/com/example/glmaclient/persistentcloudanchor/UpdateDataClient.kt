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
import com.bumptech.glide.Glide
import com.example.glmaclient.persistentcloudanchor.R
import com.example.glmaclient.persistentcloudanchor.databinding.ActivityUpdateDataClientBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar

class UpdateDataClient : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateDataClientBinding
    private var selectedImageUri: Uri? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog
    private var deceasedId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateDataClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Updating Data...")
        progressDialog.setCancelable(false)

        val backArrow: ImageView = findViewById(R.id.backarrow)
        backArrow.setOnClickListener {
            val intent = Intent(this, ManageDeceasedClient::class.java)
            startActivity(intent)
        }

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        storage = FirebaseStorage.getInstance()

        binding.editBirthDate.setOnClickListener {
            showDatePickerDialog(binding.editBirthDate)
        }

        binding.editDeathDate.setOnClickListener {
            showDatePickerDialog(binding.editDeathDate)
        }

        binding.selectImageButton.setOnClickListener {
            selectImage()
        }

        deceasedId = intent.getStringExtra("deceasedId")
        fetchApprovedData(deceasedId)

        binding.submitButtonClient.setOnClickListener {
            Log.d("UpdateDataClient", "Submit button clicked")
            val deceasedNameClient = binding.editDeceasedName.text.toString()
            val birthDateClient = binding.editBirthDate.text.toString()
            val deathDateClient = binding.editDeathDate.text.toString()
            val lotNumberClient = binding.editLotNumber.text.toString()

            if (lotNumberClient.isNotEmpty() && lotNumberClient.toIntOrNull() != null) {
                checkIfLotNumberExists(deceasedNameClient, birthDateClient, deathDateClient, lotNumberClient)
            } else {
                Toast.makeText(this, "Please enter a valid lot number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchApprovedData(deceasedId: String?) {
        if (deceasedId == null) return
        databaseReference.child("approved").child(deceasedId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue(Model::class.java)
                data?.let {
                    binding.editDeceasedName.setText(it.deceasedName)
                    binding.editBirthDate.text = it.birthDate
                    binding.editDeathDate.text = it.deathDate
                    binding.editLotNumber.setText(it.lotNumber)
                    it.lotPhoto?.let { photoUrl -> displayImage(photoUrl) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UpdateDataClient, "Failed to fetch data", Toast.LENGTH_SHORT).show()
            }
        })
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

    private fun displayImage(url: String) {
        Glide.with(this)
            .load(url)
            .placeholder(R.drawable.imagetest)
            .into(binding.selectedImageView)
    }

    private fun checkIfLotNumberExists(deceasedNameClient: String, birthDateClient: String, deathDateClient: String, lotNumberClient: String) {
        Log.d("UpdateDataClient", "Checking if lot number exists: $lotNumberClient")
        databaseReference.child("pending").orderByChild("lotNumber").equalTo(lotNumberClient)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.children.any { it.key != deceasedId }) {
                        Toast.makeText(this@UpdateDataClient, "Lot number already exists. Please enter a different lot number.", Toast.LENGTH_SHORT).show()
                    } else {
                        showConfirmationDialog(deceasedNameClient, birthDateClient, deathDateClient, lotNumberClient)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UpdateDataClient, "Failed to check lot number. Please try again.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showConfirmationDialog(deceasedNameClient: String, birthDateClient: String, deathDateClient: String, lotNumberClient: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Update")
            .setMessage("Are you sure you want to update the details?")
            .setPositiveButton("Proceed") { dialog, which ->
                if (selectedImageUri != null) {
                    progressDialog.show()
                    uploadImageToStorage(deceasedNameClient, birthDateClient, deathDateClient, lotNumberClient, selectedImageUri!!)
                } else {
                    saveDataToRealtimeDatabase(deceasedNameClient, birthDateClient, deathDateClient, lotNumberClient, null)
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun uploadImageToStorage(deceasedNameClient: String, birthDateClient: String, deathDateClient: String, lotNumberClient: String, imageUri: Uri) {
        val storageRef = storage.reference.child("GraveImages/${System.currentTimeMillis()}.jpg")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("UpdateDataClient", "Image uploaded successfully. URL: $uri")
                    saveDataToRealtimeDatabase(deceasedNameClient, birthDateClient, deathDateClient, lotNumberClient, uri.toString())
                }.addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveDataToRealtimeDatabase(deceasedNameClient: String, birthDateClient: String, deathDateClient: String, lotNumberClient: String, imageUrl: String?) {
        val deceasedMap = mutableMapOf<String, Any>(
            "deceasedName" to deceasedNameClient,
            "birthDate" to birthDateClient,
            "deathDate" to deathDateClient,
            "lotNumber" to lotNumberClient,
        )
        if (imageUrl != null) {
            deceasedMap["lotPhoto"] = imageUrl
        }

        Log.d("UpdateDataClient", "Saving data to Realtime Database: $deceasedMap")

        // Generate a unique key if deceasedId is null
        if (deceasedId == null) {
            deceasedId = databaseReference.child("updatepending").push().key
        }

        if (deceasedId != null) {
            databaseReference.child("updatepending").child(deceasedId!!).updateChildren(deceasedMap).addOnCompleteListener { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    Log.d("UpdateDataClient", "Data updated successfully")
                    Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, ManageDeceasedClient::class.java))
                    finish()
                } else {
                    Log.e("UpdateDataClient", "Data update failed", task.exception)
                    Toast.makeText(this, "Data update failed", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            progressDialog.dismiss()
            Log.e("UpdateDataClient", "Invalid deceased ID")
            Toast.makeText(this, "Invalid deceased ID", Toast.LENGTH_SHORT).show()
        }
    }
}
