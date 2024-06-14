package com.example.glmaadmin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class AdminUpdateReviewActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var listView: ListView
    private lateinit var updatePendingList: MutableList<DataModel>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_update_review)

        val backArrow: ImageView = findViewById(R.id.backarrow)
        backArrow.setOnClickListener {
            val intent = Intent(this, HomePageAdmin::class.java)
            startActivity(intent)
        }

        listView = findViewById(R.id.updatePendingListView)
        databaseReference = FirebaseDatabase.getInstance().reference.child("updatepending")
        updatePendingList = mutableListOf()

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                updatePendingList.clear()
                for (dataSnapshot in snapshot.children) {
                    // Log the data being retrieved
                    Log.d("AdminUpdateReview", "DataSnapshot: ${dataSnapshot.value}")

                    if (dataSnapshot.value is Map<*, *>) {
                        val data = dataSnapshot.getValue(DataModel::class.java)
                        data?.let { updatePendingList.add(it) }
                    } else {
                        Log.e("AdminUpdateReview", "Invalid data format: ${dataSnapshot.value}")
                    }
                }
                Log.d("AdminUpdateReview", "Updated List: $updatePendingList")
                val adapter = UpdatePendingListAdapter(this@AdminUpdateReviewActivity, updatePendingList)
                listView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminUpdateReviewActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })

    }
}
