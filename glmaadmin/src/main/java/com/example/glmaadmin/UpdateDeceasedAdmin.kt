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
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.glmaadmin.databinding.ActivityUpdateDeceasedAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar

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
            val deceasedName = binding.editDeceasedName.text.toString()
            val birthDate = binding.editBirthDate.text.toString()
            val deathDate = binding.editDeathDate.text.toString()
            val lotNumber = binding.editLotNumber.text.toString()

            if (deceasedName.isNotEmpty() && birthDate.isNotEmpty() && deathDate.isNotEmpty() && lotNumber.isNotEmpty()) {
                updateDeceasedData(deceasedName, birthDate, deathDate, lotNumber)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        populateFieldsForUpdate()
    }

    private fun populateFieldsForUpdate() {
        val deceasedName = intent.getStringExtra("deceasedName")
        val birthDate = intent.getStringExtra("birthDate")
        val deathDate = intent.getStringExtra("deathDate")
        val lotNumber = intent.getStringExtra("lotNumber")
        val lotPhoto = intent.getStringExtra("lotPhoto")
        val dataKey = intent.getStringExtra("dataKey")

        if (dataKey == null) {
            Log.e("UpdateDeceasedAdmin", "Data key is missing in populateFieldsForUpdate")
        } else {
            Log.d("UpdateDeceasedAdmin", "Data key received: $dataKey")
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

    private fun updateDeceasedData(deceasedName: String, birthDate: String, deathDate: String, lotNumber: String) {
        val dataKey: String? = intent.getStringExtra("dataKey")
        if (dataKey == null) {
            Toast.makeText(this, "Error: Data key is missing", Toast.LENGTH_SHORT).show()
            return
        }

        val updateData = hashMapOf<String, Any>(
            "deceasedName" to deceasedName,
            "birthDate" to  birthDate,
            "deathDate" to deathDate,
            "lotNumber" to lotNumber
        )

        progressDialog.show()

        if (selectedImageUri != null) {
            uploadImageToStorage(deceasedName, birthDate, deathDate, lotNumber, dataKey)
        } else {
            updateDataInFirebase(dataKey, updateData)
        }
    }

    private fun uploadImageToStorage(deceasedName: String, birthDate: String, deathDate: String, lotNumber: String, dataKey: String) {
        val storageRef = storage.reference
        val imagesRef = storageRef.child("images")
        val imageFileName = "${System.currentTimeMillis()}.jpg"
        val imageFileRef = imagesRef.child(imageFileName)
        val uploadTask = imageFileRef.putFile(selectedImageUri!!)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            imageFileRef.downloadUrl.addOnSuccessListener { uri ->
                val updateData = hashMapOf<String, Any>(
                    "deceasedName" to deceasedName,
                    "birthDate" to birthDate,
                    "deathDate" to deathDate,
                    "lotNumber" to lotNumber,
                    "lotPhoto" to uri.toString()
                )
                updateDataInFirebase(dataKey, updateData)
            }
        }.addOnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateDataInFirebase(dataKey: String, updateData: Map<String, Any>) {
        val ref = FirebaseDatabase.getInstance().reference.child("Deceased").child(dataKey)
        ref.updateChildren(updateData).addOnCompleteListener { task ->
            progressDialog.dismiss()
            if (task.isSuccessful) {
                Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to update data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

