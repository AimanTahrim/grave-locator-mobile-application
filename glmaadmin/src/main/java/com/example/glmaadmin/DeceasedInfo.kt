package com.example.glmaadmin

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
import com.google.firebase.database.FirebaseDatabase

class DeceasedInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deceased_info)

        val backArrow: ImageView = findViewById(R.id.backarrow)
        backArrow.setOnClickListener {
            val intent = Intent(this, ManageDeceasedAdmin::class.java)
            startActivity(intent)
        }

        val deceasedId = intent.getStringExtra("deceasedId")
        val deceasedName = intent.getStringExtra("deceasedName")
        val birthDate = intent.getStringExtra("birthDate")
        val deathDate = intent.getStringExtra("deathDate")
        val lotNumber = intent.getStringExtra("lotNumber")
        val lotPhoto = intent.getStringExtra("lotPhoto")

        findViewById<TextView>(R.id.deceasedNameList).text = deceasedName
        findViewById<TextView>(R.id.birthDateList).text = birthDate
        findViewById<TextView>(R.id.deathDateList).text = deathDate
        findViewById<TextView>(R.id.lotNumberList).text = lotNumber

        val imageView = findViewById<ImageView>(R.id.graveImageList)
        Glide.with(this)
            .load(lotPhoto)
            .placeholder(R.drawable.imagetest)
            .into(imageView)

        val updateButton: Button = findViewById(R.id.updateButton)
        updateButton.setOnClickListener {
            val intent = Intent(this, UpdateDeceasedAdmin::class.java).apply {
                putExtra("deceasedName", deceasedName)
                putExtra("birthDate", birthDate)
                putExtra("deathDate", deathDate)
                putExtra("lotNumber", lotNumber)
                putExtra("lotPhoto", lotPhoto)
                putExtra("deceasedId", deceasedId)  // Pass the deceasedId to the Update activity
            }
            startActivity(intent)
        }

        val deleteButton: Button = findViewById(R.id.deleteButton)
        deleteButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton("Yes") { dialog, which ->
                    if (deceasedId != null) {
                        deleteDeceased(deceasedId)
                    } else {
                        Toast.makeText(this, "Deceased ID is missing", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun deleteDeceased(deceasedId: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("grave").child(deceasedId)
        databaseReference.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Entry deleted successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to delete entry: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
