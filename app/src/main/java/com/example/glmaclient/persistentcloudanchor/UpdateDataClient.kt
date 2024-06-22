package com.example.glmaclient.persistentcloudanchor

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.glmaclient.persistentcloudanchor.databinding.ActivityUpdateDataClientBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide

class UpdateDataClient : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var binding: ActivityUpdateDataClientBinding
    private val calendar = Calendar.getInstance()
    private lateinit var pendingUpdatesRef: DatabaseReference
    private lateinit var deceasedRef: DatabaseReference

    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateDataClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val backArrow: ImageView = findViewById(R.id.backarrow)
        backArrow.setOnClickListener {
            finish()
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        pendingUpdatesRef = db.reference.child("updatepending")
        deceasedRef = db.reference.child("grave")

        val deceasedID = intent.getStringExtra("deceasedId")
        Log.d("UpdateDataClient", "Received deceasedID: $deceasedID") // Log the deceasedID

        if (deceasedID == null) {
            Toast.makeText(this, "Error: No Deceased ID provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        } else {
            fetchData(deceasedID)
        }

        binding.editBirthDate.setOnClickListener {
            showDatePickerDialog(binding.editBirthDate)
        }

        binding.editDeathDate.setOnClickListener {
            showDatePickerDialog(binding.editDeathDate)
        }

        binding.selectImageButton.setOnClickListener {
            openImagePicker()
        }

        binding.updateButtonClient.setOnClickListener {
            validateAndUpdateData(deceasedID)
        }
    }

    private fun fetchData(deceasedID: String) {
        deceasedRef.child(deceasedID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val deceasedName = snapshot.child("deceasedName").value as? String
                val birthDate = snapshot.child("birthDate").value as? String
                val deathDate = snapshot.child("deathDate").value as? String
                val lotNumber = snapshot.child("lotNumber").value as? String
                val lotPhoto = snapshot.child("lotPhoto").value as? String

                binding.editDeceasedName.setText(deceasedName)
                binding.editBirthDate.text = birthDate
                binding.editDeathDate.text = deathDate
                binding.editLotNumber.setText(lotNumber)

                lotPhoto?.let {
                    val imageView = binding.selectedImageView
                    Glide.with(this@UpdateDataClient).load(it).into(imageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UpdateDataClient, "Failed to fetch data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDatePickerDialog(dateTextView: TextView) {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            dateTextView.text = dateFormat.format(calendar.time)
        }
        DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun openImagePicker() {
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(pickImageIntent)
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            selectedImageUri = result.data!!.data
            if (selectedImageUri != null) {
                binding.selectedImageView.setImageURI(selectedImageUri)
            }
        }
    }

    private fun validateAndUpdateData(deceasedID: String?) {
        Log.d("UpdateDataClient", "validateAndUpdateData called with deceasedID: $deceasedID") // Log the deceasedID

        val deceasedName = binding.editDeceasedName.text.toString()
        val birthDate = binding.editBirthDate.text.toString()
        val deathDate = binding.editDeathDate.text.toString()
        val lotNumber = binding.editLotNumber.text.toString()

        if (deceasedName.isEmpty() || deathDate.isEmpty() || lotNumber.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Updating data...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        if (selectedImageUri != null) {
            uploadImageToFirebaseStorage(deceasedID, deceasedName, birthDate, deathDate, lotNumber, progressDialog)
        } else {
            submitUpdateRequest(deceasedID, deceasedName, birthDate, deathDate, lotNumber, null, progressDialog)
        }
    }


    private fun uploadImageToFirebaseStorage(deceasedID: String?, deceasedName: String, birthDate: String, deathDate: String, lotNumber: String, progressDialog: ProgressDialog) {
        val storageRef = FirebaseStorage.getInstance().reference.child("images/${UUID.randomUUID()}.jpg")
        val uploadTask = storageRef.putFile(selectedImageUri!!)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    Log.e("UpdateDataClient", "Image upload failed", it)
                    throw it
                }
            }
            storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                submitUpdateRequest(deceasedID, deceasedName, birthDate, deathDate, lotNumber, downloadUri.toString(), progressDialog)
            } else {
                progressDialog.dismiss()
                Toast.makeText(this, "Image upload failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitUpdateRequest(deceasedID: String?, deceasedName: String, birthDate: String, deathDate: String, lotNumber: String, imageUrl: String?, progressDialog: ProgressDialog) {
        Log.d("UpdateDataClient", "submitUpdateRequest called with deceasedID: $deceasedID") // Log the deceasedID

        val userID = auth.currentUser?.uid
        if (deceasedID == null) {
            progressDialog.dismiss()
            Toast.makeText(this, "Invalid Deceased ID", Toast.LENGTH_SHORT).show()
            return
        }

        val updateRequest = mapOf(
            "deceasedId" to deceasedID,
            "deceasedName" to deceasedName,
            "birthDate" to birthDate,
            "deathDate" to deathDate,
            "lotNumber" to lotNumber,
            "lotPhoto" to imageUrl,
            "requestedBy" to userID,
            "status" to "pending_update"
        )

        pendingUpdatesRef.child(deceasedID).setValue(updateRequest).addOnCompleteListener { task ->
            progressDialog.dismiss()
            if (task.isSuccessful) {
                Toast.makeText(this, "Update request submitted, pending admin approval.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ManageDeceasedClient::class.java)
                startActivity(intent)
                finish()
            } else {
                Log.e("UpdateDataClient", "Database update failed", task.exception)
                Toast.makeText(this, "Update request submission failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
