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
import com.bumptech.glide.Glide
import com.example.glmaadmin.databinding.ActivityUpdateDeceasedAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.UUID

class UpdateDeceasedAdmin : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateDeceasedAdminBinding
    private var selectedImageUri: Uri? = null
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateDeceasedAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Updating Data...")
        progressDialog.setCancelable(false)

        val backArrow: ImageView = findViewById(R.id.backarrow)
        backArrow.setOnClickListener {
            finish()
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

        binding.submitButtonClient.setOnClickListener {
            Log.d("UpdateDeceasedAdmin", "Submit button clicked")
            val deceasedId = intent.getStringExtra("deceasedId")
            val deceasedName = binding.editDeceasedName.text.toString()
            val birthDate = binding.editBirthDate.text.toString() // birthDate is now optional
            val deathDate = binding.editDeathDate.text.toString()
            val lotNumber = binding.editLotNumber.text.toString()

            if (deceasedName.isEmpty() || deathDate.isEmpty() || lotNumber.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            } else if (deceasedId == null) {
                Log.e("UpdateDeceasedAdmin", "Deceased ID is missing")
                Toast.makeText(this, "Deceased ID is missing", Toast.LENGTH_SHORT).show()
            } else {
                // Show confirmation dialog
                AlertDialog.Builder(this).apply {
                    setTitle("Confirm Update")
                    setMessage("Are you sure you want to update this data?")
                    setPositiveButton("Yes") { _, _ ->
                        Log.d("UpdateDeceasedAdmin", "Updating data with deceasedId: $deceasedId")
                        progressDialog.show()
                        if (selectedImageUri != null) {
                            uploadImageAndSaveData(deceasedId, deceasedName, birthDate, deathDate, lotNumber)
                        } else {
                            saveDataToFirebase(deceasedId, deceasedName, birthDate, deathDate, lotNumber, null)
                        }
                    }
                    setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    create()
                    show()
                }
            }
        }

        populateFieldsForUpdate()
    }

    private fun showDatePickerDialog(dateTextView: TextView) {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val date = "$dayOfMonth/${month + 1}/$year"
            dateTextView.text = date
        }
        DatePickerDialog(this, dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            selectedImageUri = result.data!!.data
            selectedImageUri?.let {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, it)
                }
                binding.selectedImageView.setImageBitmap(bitmap)
            }
        }
    }

    private fun populateFieldsForUpdate() {
        val deceasedName = intent.getStringExtra("deceasedName")
        val birthDate = intent.getStringExtra("birthDate")
        val deathDate = intent.getStringExtra("deathDate")
        val lotNumber = intent.getStringExtra("lotNumber")
        val lotPhoto = intent.getStringExtra("lotPhoto")
        val deceasedId = intent.getStringExtra("deceasedId")

        if (deceasedId == null) {
            Log.e("UpdateDeceasedAdmin", "Deceased ID is missing in populateFieldsForUpdate")
        } else {
            Log.d("UpdateDeceasedAdmin", "Deceased ID received: $deceasedId")
        }

        binding.editDeceasedName.setText(deceasedName)
        binding.editBirthDate.text = birthDate
        binding.editDeathDate.text = deathDate
        binding.editLotNumber.setText(lotNumber)

        lotPhoto?.let {
            val imageView = binding.selectedImageView
            Glide.with(this).load(it).into(imageView)
        }
    }

    private fun uploadImageAndSaveData(deceasedId: String, deceasedName: String, birthDate: String, deathDate: String, lotNumber: String) {
        val storageRef = storage.reference.child("grave_images/${UUID.randomUUID()}.jpg")
        val uploadTask = storageRef.putFile(selectedImageUri!!)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                saveDataToFirebase(deceasedId, deceasedName, birthDate, deathDate, lotNumber, downloadUri.toString())
            } else {
                progressDialog.dismiss()
                Toast.makeText(this, "Image upload failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveDataToFirebase(deceasedId: String, deceasedName: String, birthDate: String, deathDate: String, lotNumber: String, imageUrl: String?) {
        val updates = hashMapOf<String, Any>(
            "deceasedName" to deceasedName,
            "birthDate" to birthDate,
            "deathDate" to deathDate,
            "lotNumber" to lotNumber
        )
        imageUrl?.let {
            updates["lotPhoto"] = it
        }

        databaseReference.child("grave").child(deceasedId).updateChildren(updates).addOnCompleteListener { task ->
            progressDialog.dismiss()
            if (task.isSuccessful) {
                Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show()
                // Start intent to ManageDeceasedAdmin
                val intent = Intent(this, ManageDeceasedAdmin::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Failed to update data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
