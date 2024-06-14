package com.example.glmaclient.persistentcloudanchor

import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.example.glmaclient.persistentcloudanchor.R

class HomePageClient : AppCompatActivity(), View.OnClickListener {

    private lateinit var browseGrave: CardView
    private lateinit var manageDeceased: CardView
    private lateinit var manageAccount: CardView
    private lateinit var feedback: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page_client)

        browseGrave = findViewById(R.id.browsecard)
        manageDeceased = findViewById(R.id.managedeceasedcard)
        manageAccount = findViewById(R.id.manageaccountcard)
        feedback = findViewById(R.id.feedbackcard)

        browseGrave.setOnClickListener(this)
        manageDeceased.setOnClickListener(this)
        manageAccount.setOnClickListener(this)
        feedback.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val i: Intent = when (v.id) {
            R.id.browsecard -> Intent(this, MapsActivity::class.java)
            R.id.managedeceasedcard -> Intent(this, ManageDeceasedClient::class.java)
            R.id.manageaccountcard -> Intent(this, ManageAccountClient::class.java)
            R.id.feedbackcard -> Intent(this, FeedbackClient::class.java)
            else -> return
        }
        startActivity(i)
    }
}
