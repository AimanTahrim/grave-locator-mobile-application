package com.example.glmaclient.persistentcloudanchor;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.glmaclient.persistentcloudanchor.R;
import com.example.glmaclient.helpers.DisplayRotationHelper;
import com.google.android.material.button.MaterialButton;

public class NavigationAr extends AppCompatActivity {

    private DisplayRotationHelper displayRotationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_ar);
        displayRotationHelper = new DisplayRotationHelper(this);
        View hostButton = findViewById(R.id.host_button);
        hostButton.setOnClickListener((view)-> onHostButtonPress());
        View resolveButton = findViewById(R.id.begin_resolve_button);
        resolveButton.setOnClickListener((view)-> onResolveButtonPress());

    }

    @Override
    protected void onResume() {
        super.onResume();
        displayRotationHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        displayRotationHelper.onPause();
    }

    private void onHostButtonPress() {
        Intent intent = CloudAnchorActivity.newHostingIntent(this);
        startActivity(intent);
    }

    private void onResolveButtonPress() {
        Intent intent = ResolveAnchorsLobbyActivity.newIntent(this);
        startActivity(intent);
    }
}