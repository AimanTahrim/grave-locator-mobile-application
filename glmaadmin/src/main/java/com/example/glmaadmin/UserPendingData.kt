package com.example.glmaadmin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView

class UserPendingData : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_pending_data)

        val backArrow: ImageView = findViewById(R.id.backarrow)
        backArrow.setOnClickListener {
            val intent = Intent(this, ManageDeceasedAdmin::class.java)
            startActivity(intent)
        }
    }

    fun onClick(v: View) {
        val i: Intent = when (v.id) {
            R.id.AddRequestCard -> Intent(this, AdminReviewActivity::class.java)
            R.id.UpdateRequestCard -> Intent(this, AdminUpdateReviewActivity::class.java)
            R.id.DeleteRequestCard -> Intent(this, AdminDeleteRequestsActivity::class.java)
            else -> return
        }
        startActivity(i)
    }
}