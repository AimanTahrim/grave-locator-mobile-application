package com.example.glmaadmin

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UpdatePendingListAdapter(private val context: Context, private val dataList: List<DataModel>) : ArrayAdapter<DataModel>(context, 0, dataList) {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_update_request, parent, false)
        val data = dataList[position]

        val deceasedNameTextView: TextView = view.findViewById(R.id.deceasedNameTextView)
        val birthDateTextView: TextView = view.findViewById(R.id.birthDateTextView)
        val deathDateTextView: TextView = view.findViewById(R.id.deathDateTextView)
        val lotNumberTextView: TextView = view.findViewById(R.id.lotNumberTextView)
        val lotPhotoImageView: ImageView = view.findViewById(R.id.lotPhotoImageView)
        val approveButton: Button = view.findViewById(R.id.approveButton)
        val rejectButton: Button = view.findViewById(R.id.rejectButton)

        deceasedNameTextView.text = data.deceasedName
        birthDateTextView.text = data.birthDate
        deathDateTextView.text = data.deathDate
        lotNumberTextView.text = data.lotNumber

        Glide.with(context)
            .load(data.lotPhoto)
            .placeholder(R.mipmap.ic_launcher) // Optional placeholder image
            .into(lotPhotoImageView)

        approveButton.setOnClickListener {
            Log.d("UpdatePendingListAdapter", "Approve button clicked for: ${data.deceasedId}")
            approveUpdate(data)
        }

        rejectButton.setOnClickListener {
            Log.d("UpdatePendingListAdapter", "Reject button clicked for: ${data.deceasedId}")
            rejectUpdate(data)
        }

        return view
    }

    private fun approveUpdate(data: DataModel) {
        // Ensure data.id is not null
        val id = data.deceasedId ?: return

        // Create a map with only the fields expected by the client module
        val approvedData = mapOf(
            "deceasedName" to data.deceasedName,
            "birthDate" to data.birthDate,
            "deathDate" to data.deathDate,
            "lotNumber" to data.lotNumber,
            "lotPhoto" to data.lotPhoto
        )

        Log.d("UpdatePendingListAdapter", "Approving update for ID: $id with data: $approvedData")

        // Update the approved data and remove from updatepending
        databaseReference.child("approved").child(id).updateChildren(approvedData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                databaseReference.child("updatepending").child(id).removeValue().addOnCompleteListener { removeTask ->
                    if (removeTask.isSuccessful) {
                        Toast.makeText(context, "Update approved", Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, ManageDeceasedAdmin::class.java)
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Failed to remove data from updatepending", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Failed to approve update", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun rejectUpdate(data: DataModel) {
        // Ensure data.id is not null
        val id = data.deceasedId ?: return

        Log.d("UpdatePendingListAdapter", "Rejecting update for ID: $id")

        databaseReference.child("updatepending").child(id).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Update rejected", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, ManageDeceasedAdmin::class.java)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Failed to reject update", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
