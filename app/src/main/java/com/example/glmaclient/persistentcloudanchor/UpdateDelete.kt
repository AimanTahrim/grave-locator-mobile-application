package com.example.glmaclient.persistentcloudanchor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UpdateDelete : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userArrayList: ArrayList<Model>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var UdAdapter: UpDelAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_delete)

        val backArrow: ImageView = findViewById(R.id.backarrow)
        backArrow.setOnClickListener {
            val intent = Intent(this, ManageDeceasedClient::class.java)
            startActivity(intent)
        }

        firebaseAuth = FirebaseAuth.getInstance()
        userRecyclerView = findViewById(R.id.recyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.setHasFixedSize(true)

        userArrayList = arrayListOf()
        UdAdapter = UpDelAdapter(userArrayList, this)
        userRecyclerView.adapter = UdAdapter

        getUserData()
    }

    private fun getUserData() {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("grave")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userArrayList.clear()
                for (dataSnapshot in snapshot.children) {
                    try {
                        val model = dataSnapshot.getValue(Model::class.java)
                        if (model != null && model.submittedBy == currentUserId) {
                            userArrayList.add(model)
                        }
                    } catch (e: DatabaseException) {
                        Log.e("UpdateDelete", "Error converting data snapshot to Model: ${e.message}")
                    }
                }
                Log.d("UpdateDelete", "Approved data fetched: ${userArrayList.size} items")
                UdAdapter.searchDataList(userArrayList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UpdateDelete, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
