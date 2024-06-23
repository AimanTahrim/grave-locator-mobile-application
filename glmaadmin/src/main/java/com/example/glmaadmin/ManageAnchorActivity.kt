package com.example.glmaadmin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ManageAnchorActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var anchorAdapter: AnchorAdapter
    private val anchorList = mutableListOf<Anchor>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_anchor)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("anchors")

        val backArrow: ImageView = findViewById(R.id.backarrow)
        backArrow.setOnClickListener {
            val intent = Intent(this, HomePageAdmin::class.java)
            startActivity(intent)
        }

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        anchorAdapter = AnchorAdapter(anchorList, this::updateAnchor, this::deleteAnchor)
        recyclerView.adapter = anchorAdapter

        // Load data from Firebase
        loadDataFromFirebase()
    }

    private fun loadDataFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                anchorList.clear()
                for (dataSnapshot in snapshot.children) {
                    val anchor = dataSnapshot.getValue(Anchor::class.java)
                    anchor?.let { anchorList.add(it) }
                }
                anchorAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("AdminActivity", "loadAnchors:onCancelled", error.toException())
            }
        })
    }

    @SuppressLint("MissingInflatedId")
    private fun updateAnchor(anchor: Anchor) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_anchor, null)
        val nicknameEditText: EditText = dialogView.findViewById(R.id.nicknameEditText)

        nicknameEditText.setText(anchor.anchorNickname)

        AlertDialog.Builder(this)
            .setTitle("Update Anchor")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updatedNickname = nicknameEditText.text.toString()
                val updatedAnchor = anchor.copy(anchorNickname = updatedNickname)
                database.child(anchor.anchorId).setValue(updatedAnchor)
                    .addOnSuccessListener {
                        Log.d("AdminActivity", "Anchor successfully updated!")
                    }
                    .addOnFailureListener { e ->
                        Log.w("AdminActivity", "Error updating anchor", e)
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAnchor(anchor: Anchor) {
        AlertDialog.Builder(this)
            .setTitle("Delete Anchor")
            .setMessage("Are you sure you want to delete this anchor?")
            .setPositiveButton("Delete") { _, _ ->
                database.child(anchor.anchorId).removeValue()
                    .addOnSuccessListener {
                        Log.d("AdminActivity", "Anchor successfully deleted!")
                    }
                    .addOnFailureListener { e ->
                        Log.w("AdminActivity", "Error deleting anchor", e)
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
