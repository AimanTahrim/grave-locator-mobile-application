package com.example.glmaclient.persistentcloudanchor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.glmaclient.persistentcloudanchor.R;
import com.example.glmaclient.helpers.DisplayRotationHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ResolveAnchorsLobbyActivity extends AppCompatActivity {
    private Spinner spinner;
    private List<AnchorItem> selectedAnchors;
    private DisplayRotationHelper displayRotationHelper;

    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, ResolveAnchorsLobbyActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resolve_anchors_lobby);
        displayRotationHelper = new DisplayRotationHelper(this);
        View resolveButton = findViewById(R.id.resolve_button);
        resolveButton.setOnClickListener((view) -> onResolveButtonPress());

        selectedAnchors = new ArrayList<>();
        spinner = findViewById(R.id.select_anchors_spinner);
        MultiSelectItem adapter = new MultiSelectItem(this, 0, selectedAnchors, spinner);
        spinner.setAdapter(adapter);

        fetchAnchorsFromFirebase(adapter);
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
        EditText enteredAnchorIds = findViewById(R.id.anchor_edit_text);
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

    // Method to fetch anchors from Firebase Realtime Database
    private void fetchAnchorsFromFirebase(MultiSelectItem adapter) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("anchors");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                selectedAnchors.clear();  // Clear current list
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String anchorId = snapshot.child("anchorId").getValue(String.class);
                    String anchorNickname = snapshot.child("anchorNickname").getValue(String.class);
                    long timestamp = snapshot.child("timestamp").getValue(Long.class);
                    long minutesSinceCreation = (System.currentTimeMillis() - timestamp) / (1000 * 60);

                    AnchorItem anchorItem = new AnchorItem(anchorId, anchorNickname, minutesSinceCreation);
                    selectedAnchors.add(anchorItem);
                }
                adapter.notifyDataSetChanged();  // Notify adapter that the data has changed
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("ResolveAnchorsLobbyActivity", "Failed to read value.", error.toException());
            }
        });
    }
}
