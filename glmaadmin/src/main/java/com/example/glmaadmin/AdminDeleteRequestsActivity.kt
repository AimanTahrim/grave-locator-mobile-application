package com.example.glmaadmin

import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class AdminDeleteRequestsActivity : AppCompatActivity() {

    private lateinit var deleteRequestsListView: ListView
    private lateinit var deleteRequests: ArrayList<DeleteRequest>
    private lateinit var adapter: DeleteRequestsAdapter
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_delete_requests)

        deleteRequestsListView = findViewById(R.id.deleteRequestsListView)
        deleteRequests = ArrayList()
        adapter = DeleteRequestsAdapter(this, deleteRequests)
        deleteRequestsListView.adapter = adapter

        database = FirebaseDatabase.getInstance().getReference("delete_pending")

        fetchPendingDeleteRequests()
    }

    private fun fetchPendingDeleteRequests() {
        database.orderByChild("status").equalTo("pending_delete")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    deleteRequests.clear()
                    for (requestSnapshot in snapshot.children) {
                        val request = requestSnapshot.getValue(DeleteRequest::class.java)
                        if (request != null) {
                            deleteRequests.add(request)
                        } else {
                            Log.e("DataError", "Request is null")
                        }
                    }
                    Log.d("DataFetch", "Fetched ${deleteRequests.size} requests")
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AdminDeleteRequestsActivity, "Failed to fetch delete requests", Toast.LENGTH_SHORT).show()
                    Log.e("DataError", "Database error: $error")
                }
            })
    }

    fun handleDeleteRequest(position: Int, isApproved: Boolean) {
        if (position >= 0) {
            val selectedRequest = deleteRequests[position]
            val requestRef = database.child(selectedRequest.deceasedId ?: return)
            requestRef.child("status").setValue(if (isApproved) "approved" else "rejected")
                .addOnSuccessListener {
                    if (isApproved) {
                        // Perform the actual delete operation
                        val deceasedRef = FirebaseDatabase.getInstance().getReference("grave").child(selectedRequest.deceasedId ?: return@addOnSuccessListener)
                        deceasedRef.removeValue()
                            .addOnSuccessListener {
                                requestRef.removeValue()
                                Toast.makeText(this, "Deceased record deleted", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to delete deceased record", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        requestRef.removeValue()
                        Toast.makeText(this, "Delete request rejected", Toast.LENGTH_SHORT).show()
                    }
                    fetchPendingDeleteRequests()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update delete request status", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No request selected", Toast.LENGTH_SHORT).show()
        }
    }
}
