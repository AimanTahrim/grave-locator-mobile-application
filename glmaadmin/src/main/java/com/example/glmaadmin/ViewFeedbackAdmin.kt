package com.example.glmaadmin

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class ViewFeedbackAdmin : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var feedbackList: ArrayList<Feedback>
    private lateinit var feedbackAdapter: FeedbackAdapter
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_feedback_admin)

        val backArrow: ImageView = findViewById(R.id.backarrow)
        backArrow.setOnClickListener{
            val intent = Intent(this, HomePageAdmin::class.java)
            startActivity(intent)
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        feedbackList = arrayListOf()
        feedbackAdapter = FeedbackAdapter(feedbackList)
        recyclerView.adapter = feedbackAdapter

        databaseReference = FirebaseDatabase.getInstance().getReference("feedback")

        loadFeedback()
    }

    private fun loadFeedback() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                feedbackList.clear()
                if (snapshot.exists()) {
                    for (feedbackSnapshot in snapshot.children) {
                        val feedback = feedbackSnapshot.getValue(Feedback::class.java)
                        feedback?.let { feedbackList.add(it) }
                    }
                    feedbackAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ViewFeedbackAdmin, "Failed to load feedback", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
