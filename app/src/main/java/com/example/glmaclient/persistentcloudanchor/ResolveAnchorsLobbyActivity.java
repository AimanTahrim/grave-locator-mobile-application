package com.example.glmaclient.persistentcloudanchor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.glmaclient.persistentcloudanchor.R;
import com.example.glmaclient.helpers.DisplayRotationHelper;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ResolveAnchorsLobbyActivity extends AppCompatActivity {
    private Spinner spinner;
    private SharedPreferences sharedPreferences;
    List<AnchorItem> selectedAnchors;

    public static List<AnchorItem> retrieveStoredAnchors(SharedPreferences anchorPreferences) {
        List<AnchorItem> anchors = new ArrayList<>();
        String hostedAnchorIds = anchorPreferences.getString(CloudAnchorActivity.HOSTED_ANCHOR_IDS, "");
        String hostedAnchorNames =
                anchorPreferences.getString(CloudAnchorActivity.HOSTED_ANCHOR_NAMES, "");
        String hostedAnchorMinutes =
                anchorPreferences.getString(CloudAnchorActivity.HOSTED_ANCHOR_MINUTES, "");
        if (!hostedAnchorIds.isEmpty()) {
            String[] anchorIds = hostedAnchorIds.split(";", -1);
            String[] anchorNames = hostedAnchorNames.split(";", -1);
            String[] anchorMinutes = hostedAnchorMinutes.split(";", -1);
            for (int i = 0; i < anchorIds.length - 1; i++) {
                long timeSinceCreation =
                        TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis())
                                - Long.parseLong(anchorMinutes[i]);
                if (timeSinceCreation < 24 * 60) {
                    anchors.add(new AnchorItem(anchorIds[i], anchorNames[i], timeSinceCreation));
                }
            }
        }
        return anchors;
    }

    private DisplayRotationHelper displayRotationHelper;
    /** Callback function invoked when the Host Button is pressed. */
    static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, ResolveAnchorsLobbyActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resolve_anchors_lobby);
        displayRotationHelper = new DisplayRotationHelper(this);
        View resolveButton = findViewById(R.id.resolve_button);
        resolveButton.setOnClickListener((view) -> onResolveButtonPress());
        sharedPreferences =
                getSharedPreferences(CloudAnchorActivity.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        selectedAnchors = retrieveStoredAnchors(sharedPreferences);
        spinner = (Spinner) findViewById(R.id.select_anchors_spinner);
        MultiSelectItem adapter = new MultiSelectItem(this, 0, selectedAnchors, spinner);
        spinner.setAdapter(adapter);
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

    /** Callback function invoked when the Resolve Button is pressed. */
    private void onResolveButtonPress() {
        ArrayList<String> anchorsToResolve = new ArrayList<>();
        for (AnchorItem anchorItem : selectedAnchors) {
            if (anchorItem.isSelected()) {
                anchorsToResolve.add(anchorItem.getAnchorId());
            }
        }
        EditText enteredAnchorIds = (EditText) findViewById(R.id.anchor_edit_text);
        String[] idsList = enteredAnchorIds.getText().toString().trim().split(",", -1);
        for (String anchorId : idsList) {
            if (anchorId.isEmpty()) {
                continue;
            }
            anchorsToResolve.add(anchorId);
        }
        Intent intent = CloudAnchorActivity.newResolvingIntent(this, anchorsToResolve);
        startActivity(intent);
    }
}
