package com.example.glmaclient.persistentcloudanchor

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.glmaclient.persistentcloudanchor.databinding.ActivityUpDelDeceasedInfoBinding

class UpDelDeceasedInfo : AppCompatActivity() {

    private lateinit var binding: ActivityUpDelDeceasedInfoBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private var userId: String? = null
    private var deceasedId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpDelDeceasedInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase components
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        // Get current user ID
        userId = auth.currentUser?.uid

        // Retrieve data passed from previous activity
        val intent = intent
        deceasedId = intent.getStringExtra("deceasedId")
        binding.deceasedNameList.text = intent.getStringExtra("deceasedName")
        binding.birthDateList.text = intent.getStringExtra("birthDate")
        binding.deathDateList.text = intent.getStringExtra("deathDate")
        binding.lotNumberList.text = intent.getStringExtra("lotNumber")

        Glide.with(this)
            .load(intent.getStringExtra("lotPhoto"))
            .placeholder(R.drawable.imagetest)
            .into(binding.graveImageList)

        binding.backarrow.setOnClickListener {
            finish()
        }

        binding.updateButtonClient.setOnClickListener {
            Log.d("UpDelDeceasedInfo", "Starting UpdateDataClient with deceasedId: $deceasedId") // Log the deceasedId
            val updateIntent = Intent(this, UpdateDataClient::class.java)
            updateIntent.putExtra("deceasedId", deceasedId)
            startActivity(updateIntent)
        }

        binding.deleteButtonClient.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Delete") { dialog, which ->
                requestDeleteApproval()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun requestDeleteApproval() {
        if (userId != null && deceasedId != null) {
            // Verify if the current user is the one who inserted the data
            database.child("grave").child(deceasedId!!).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val submittedBy = snapshot.child("submittedBy").getValue(String::class.java)

                    if (submittedBy == userId) {
                        // User is authorized to delete
                        val deletePendingData = mapOf(
                            "deceasedId" to deceasedId,
                            "deceasedName" to snapshot.child("deceasedName").getValue(String::class.java),
                            "birthDate" to snapshot.child("birthDate").getValue(String::class.java),
                            "deathDate" to snapshot.child("deathDate").getValue(String::class.java),
                            "lotNumber" to snapshot.child("lotNumber").getValue(String::class.java),
                            "lotPhoto" to snapshot.child("lotPhoto").getValue(String::class.java),
                            "submittedBy" to userId,
                            "status" to "pending_delete"
                        )

                        database.child("delete_pending").child(deceasedId!!).setValue(deletePendingData)
                            .addOnSuccessListener {
                                Toast.makeText(this@UpDelDeceasedInfo, "Delete request sent for approval", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this@UpDelDeceasedInfo, "Failed to send delete request", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // User is not authorized to delete
                        Toast.makeText(this@UpDelDeceasedInfo, "You are not authorized to delete this data", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UpDelDeceasedInfo, "Database error occurred", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Unable to delete data", Toast.LENGTH_SHORT).show()
        }
    }
}
