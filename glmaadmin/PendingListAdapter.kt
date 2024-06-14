package com.example.glmaadmin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PendingListAdapter(
    context: Context,
    private val dataList: List<DataModel>
) : ArrayAdapter<DataModel>(context, 0, dataList) {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.pending_item, parent, false)
        val data = dataList[position]

        val deceasedNameTextView: TextView = view.findViewById(R.id.deceasedNameTextView)
        val birthDateTextView: TextView = view.findViewById(R.id.birthDateTextView)
        val deathDateTextView: TextView = view.findViewById(R.id.deathDateTextView)
        val lotNumberTextView: TextView = view.findViewById(R.id.lotNumberTextView)
        val approveButton: Button = view.findViewById(R.id.approveButton)
        val rejectButton: Button = view.findViewById(R.id.rejectButton)

        deceasedNameTextView.text = data.deceasedName
        birthDateTextView.text = data.birthDate
        deathDateTextView.text = data.deathDate
        lotNumberTextView.text = data.lotNumber

        approveButton.setOnClickListener {
            approveData(data)
        }

        rejectButton.setOnClickListener {
            rejectData(data)
        }

        return view
    }

    private fun approveData(data: DataModel) {
        val approvedRef = databaseReference.child("approved").push()
        approvedRef.setValue(data).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                databaseReference.child("pending").child(data.id).removeValue()
                Toast.makeText(context, "Data approved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to approve data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun rejectData(data: DataModel) {
        databaseReference.child("pending").child(data.id).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(context, "Data rejected", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to reject data", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
