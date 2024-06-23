package com.example.glmaadmin

import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import android.content.Intent
import android.os.Bundle
import android.view.View

class HomePageAdmin : AppCompatActivity(), View.OnClickListener {

    private lateinit var manageDeceased: CardView
    private lateinit var manageAnchor: CardView
    private lateinit var manageAccount: CardView
    private lateinit var feedback: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page_admin)

        manageDeceased = findViewById(R.id.managedeceasedcard)
        manageAnchor = findViewById(R.id.manageanchorcard)
        manageAccount = findViewById(R.id.manageaccountcard)
        feedback = findViewById(R.id.feedbackcard)

        manageDeceased.setOnClickListener(this)
        manageAnchor.setOnClickListener(this)
        manageAccount.setOnClickListener(this)
        feedback.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val i: Intent = when (v.id) {
            R.id.managedeceasedcard -> Intent(this, ManageDeceasedAdmin::class.java)
            R.id.manageanchorcard -> Intent(this, ManageAnchorActivity::class.java)
            R.id.manageaccountcard -> Intent(this, ManageAccountAdmin::class.java)
            R.id.feedbackcard -> Intent(this, ViewFeedbackAdmin::class.java)
            else -> return
        }
        startActivity(i)
    }
}
