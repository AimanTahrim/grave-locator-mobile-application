package com.example.glmaclient.persistentcloudanchor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.glmaclient.persistentcloudanchor.R
import com.example.glmaclient.persistentcloudanchor.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MapsClient())
                .commit()
        }
    }
}
