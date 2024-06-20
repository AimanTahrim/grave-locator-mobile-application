package com.example.glmaclient.persistentcloudanchor

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
import com.example.glmaclient.persistentcloudanchor.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DeceasedInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deceased_info)

        val backArrow: ImageView = findViewById(R.id.backarrow)
        backArrow.setOnClickListener {
            val intent = Intent(this, ManageDeceasedClient::class.java)
            startActivity(intent)
        }

        val deceasedId = intent.getStringExtra("deceasedId") ?: ""
        val deceasedName = intent.getStringExtra("deceasedName") ?: ""
        val birthDate = intent.getStringExtra("birthDate") ?: ""
        val deathDate = intent.getStringExtra("deathDate") ?: ""
        val lotNumber = intent.getStringExtra("lotNumber") ?: ""
        val lotPhoto = intent.getStringExtra("lotPhoto") ?: ""

        findViewById<TextView>(R.id.deceasedNameList).text = deceasedName
        findViewById<TextView>(R.id.birthDateList).text = birthDate
        findViewById<TextView>(R.id.deathDateList).text = deathDate
        findViewById<TextView>(R.id.lotNumberList).text = lotNumber

        val imageView = findViewById<ImageView>(R.id.graveImageList)
        Glide.with(this)
            .load(lotPhoto)
            .placeholder(R.drawable.imagetest)
            .into(imageView)

        val navigateButton: Button = findViewById(R.id.navigateButton)
        navigateButton.setOnClickListener {
            val intent = Intent(this, NavigationAr::class.java)
            startActivity(intent)
        }
    }

}
