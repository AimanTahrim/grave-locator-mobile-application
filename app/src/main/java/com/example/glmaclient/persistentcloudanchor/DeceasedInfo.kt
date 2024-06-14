package com.example.glmaclient.persistentcloudanchor

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.glmaclient.persistentcloudanchor.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DeceasedInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deceased_info)

        val backArrow: ImageView = findViewById(R.id.backarrow)
        backArrow.setOnClickListener {
            val intent = Intent(this, ManageDeceasedClient::class.java)
            startActivity(intent)
        }

        val deceasedId = intent.getStringExtra("deceasedId") ?: ""
        val deceasedName = intent.getStringExtra("deceasedName") ?: ""
        val birthDate = intent.getStringExtra("birthDate") ?: ""
        val deathDate = intent.getStringExtra("deathDate") ?: ""
        val lotNumber = intent.getStringExtra("lotNumber") ?: ""
        val lotPhoto = intent.getStringExtra("lotPhoto") ?: ""

        findViewById<TextView>(R.id.deceasedNameList).text = deceasedName
        findViewById<TextView>(R.id.birthDateList).text = birthDate
        findViewById<TextView>(R.id.deathDateList).text = deathDate
        findViewById<TextView>(R.id.lotNumberList).text = lotNumber

        val imageView = findViewById<ImageView>(R.id.graveImageList)
        Glide.with(this)
            .load(lotPhoto)
            .placeholder(R.drawable.imagetest)
            .into(imageView)

        val navigateButton: Button = findViewById(R.id.navigateButton)
        navigateButton.setOnClickListener {
            val intent = Intent(this, NavigationAr::class.java)
            startActivity(intent)
        }

        val btndelete: ImageView = findViewById(R.id.btndelete)
        btndelete.setOnClickListener {
            showDeleteConfirmationDialog(deceasedId)
        }
    }

    private fun showDeleteConfirmationDialog(deceasedId: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Delete")
        builder.setMessage("Are you sure you want to submit a delete request for this record?")

        builder.setPositiveButton("Yes") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
            requestDelete(deceasedId)
        }

        builder.setNegativeButton("No") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun requestDelete(deceasedId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val deleteRequestRef = FirebaseDatabase.getInstance().getReference("deleteRequests")
            val requestId = deleteRequestRef.push().key ?: return
            val deleteRequest = DeleteRequest(
                id = requestId,
                deceasedId = deceasedId,
                deceasedName = findViewById<TextView>(R.id.deceasedNameList).text.toString(),
                birthDate = findViewById<TextView>(R.id.birthDateList).text.toString(),
                deathDate = findViewById<TextView>(R.id.deathDateList).text.toString(),
                lotNumber = findViewById<TextView>(R.id.lotNumberList).text.toString(),
                lotPhoto = intent.getStringExtra("lotPhoto") ?: "", // Ensure lotPhoto is passed correctly
                requestedBy = currentUser.uid,
                status = "pending"
            )
            deleteRequestRef.child(requestId).setValue(deleteRequest)
                .addOnSuccessListener {
                    Toast.makeText(this, "Delete request submitted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to submit delete request", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
