package com.example.glmaclient.persistentcloudanchor;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MultiSelectItem extends ArrayAdapter<AnchorItem> {
    private LayoutInflater layoutInflater;
    private final List<AnchorItem> anchorsList;
    public Spinner spinner = null;
    private static final String TAG = MultiSelectItem.class.getSimpleName();
    private static final String CHECKED_BOX = "\u2611";
    private static final String UNCHECKED_BOX = "\u2610";
    private static final String SPACE = "    ";

    public MultiSelectItem(Context context, int resource, List<AnchorItem> objects, Spinner spinner) {
        super(context, resource, objects);
        this.anchorsList = objects;
        this.spinner = spinner;
        layoutInflater = LayoutInflater.from(context);
        fetchAnchorsFromFirebase();  // Fetch anchors from Firebase when the adapter is created
    }

    // Adjust for the blank initial selection item.
    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        return getCustomView(position, view, parent);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        return getCustomView(position, view, parent);
    }

    static class ViewHolder {
        TextView anchorName;
        TextView creationTime;
    }

    // Creates a view if convertView is null, otherwise reuse cached view.
    public View getCustomView(final int position, @Nullable View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.anchor_item, null, false);
            viewHolder.anchorName = convertView.findViewById(R.id.anchor_name);
            viewHolder.creationTime = convertView.findViewById(R.id.creation_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String text;
        int anchorPosition = position - 1;
        if (position == 0) {
            viewHolder.anchorName.setText("    Select Lot Number");
            viewHolder.creationTime.setText("");
        } else {
            if (anchorsList.get(anchorPosition).isSelected()) {
                text = SPACE + CHECKED_BOX + SPACE + anchorsList.get(anchorPosition).getAnchorName();
            } else {
                text = SPACE + UNCHECKED_BOX + SPACE + anchorsList.get(anchorPosition).getAnchorName();
            }
            viewHolder.anchorName.setText(text);
            viewHolder.anchorName.setTag(anchorPosition);
            viewHolder.creationTime.setText(anchorsList.get(anchorPosition).getMinutesSinceCreation());
            viewHolder.anchorName.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            spinner.performClick();
                            int getPosition = (Integer) v.getTag();
                            anchorsList.get(getPosition).setSelected(!anchorsList.get(getPosition).isSelected());
                            notifyDataSetChanged();
                        }
                    });
        }
        return convertView;
    }

    // Method to fetch anchors from Firebase Realtime Database
    private void fetchAnchorsFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("anchors");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                anchorsList.clear();  // Clear current list
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String anchorId = snapshot.child("anchorId").getValue(String.class);
                    String anchorNickname = snapshot.child("anchorNickname").getValue(String.class);
                    long timestamp = snapshot.child("timestamp").getValue(Long.class);
                    long minutesSinceCreation = (System.currentTimeMillis() - timestamp) / (1000 * 60);

                    AnchorItem anchorItem = new AnchorItem(anchorId, anchorNickname, minutesSinceCreation);
                    anchorsList.add(anchorItem);
                }
                notifyDataSetChanged();  // Notify adapter that the data has changed
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }
}
