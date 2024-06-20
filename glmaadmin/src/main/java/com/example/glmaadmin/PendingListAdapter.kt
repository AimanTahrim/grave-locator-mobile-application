package com.example.glmaadmin

import android.content.Context
import android.content.Intent
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

class PendingListAdapter(private val context: Context, private val dataList: List<DataModel>) : ArrayAdapter<DataModel>(context, 0, dataList) {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.pending_item, parent, false)
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
            approveData(data)
        }

        rejectButton.setOnClickListener {
            rejectData(data)
        }

        return view
    }

    private fun approveData(data: DataModel) {
        // Ensure data.id is not null
        val id = data.deceasedId ?: return

        // Create a map with only the fields expected by the client module
        val approvedData = mapOf(
            "deceasedId" to id,  // Ensure deceasedId is included
            "deceasedName" to data.deceasedName,
            "birthDate" to data.birthDate,
            "deathDate" to data.deathDate,
            "lotNumber" to data.lotNumber,
            "lotPhoto" to data.lotPhoto,
            "submittedBy" to data.submittedBy,
            "status" to "approved"
        )

        val approvedRef = databaseReference.child("grave").child(id)
        approvedRef.setValue(approvedData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                databaseReference.child("add_pending").child(id).removeValue().addOnCompleteListener { removeTask ->
                    if (removeTask.isSuccessful) {
                        Toast.makeText(context, "Data approved", Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, ManageDeceasedAdmin::class.java)
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Failed to remove data from pending", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Failed to approve data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun rejectData(data: DataModel) {
        // Ensure data.id is not null
        val id = data.deceasedId ?: return

        databaseReference.child("add_pending").child(id).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Data rejected", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, ManageDeceasedAdmin::class.java)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "Failed to reject data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
