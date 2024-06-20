package com.example.glmaadmin

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class AdminReviewActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var listView: ListView
    private lateinit var pendingList: MutableList<DataModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_review)

        val backArrow: ImageView = findViewById(R.id.backarrow)
        backArrow.setOnClickListener {
            val intent = Intent(this, ManageDeceasedAdmin::class.java)
            startActivity(intent)
        }

        listView = findViewById(R.id.pendingListView)
        databaseReference = FirebaseDatabase.getInstance().reference.child("add_pending")
        pendingList = mutableListOf()

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pendingList.clear()
                for (dataSnapshot in snapshot.children) {
                    val data = dataSnapshot.getValue(DataModel::class.java)
                    data?.let {
                        it.deceasedId = dataSnapshot.key // Set the unique key (id) for each entry
                        pendingList.add(it)
                    }
                }
                val adapter = PendingListAdapter(this@AdminReviewActivity, pendingList)
                listView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminReviewActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
