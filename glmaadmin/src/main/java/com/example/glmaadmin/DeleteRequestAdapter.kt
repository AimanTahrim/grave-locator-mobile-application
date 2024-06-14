package com.example.glmaadmin

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import java.util.ArrayList

class DeleteRequestsAdapter(
    private val activity: AdminDeleteRequestsActivity,
    private val dataSource: ArrayList<DeleteRequest>
) : BaseAdapter() {

    private val inflater: LayoutInflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = convertView ?: inflater.inflate(R.layout.list_item_delete_request, parent, false)

        // Find the views
        val graveImageView = rowView.findViewById<ImageView>(R.id.graveImageList)
        val deceasedNameTextView = rowView.findViewById<TextView>(R.id.deceasedNameList)
        val lotNumberTextView = rowView.findViewById<TextView>(R.id.lotNumberList)
        val birthDateTextView = rowView.findViewById<TextView>(R.id.birthDateList)
        val deathDateTextView = rowView.findViewById<TextView>(R.id.deathDateList)
        val approveButton = rowView.findViewById<Button>(R.id.approveButton)
        val rejectButton = rowView.findViewById<Button>(R.id.rejectButton)

        // Get the data item for this position
        val deleteRequest = getItem(position) as DeleteRequest

        // Populate the data into the views
        Glide.with(activity).load(deleteRequest.lotPhoto).into(graveImageView)
        deceasedNameTextView.text = deleteRequest.deceasedName
        lotNumberTextView.text = deleteRequest.lotNumber
        birthDateTextView.text = deleteRequest.birthDate
        deathDateTextView.text = deleteRequest.deathDate

        // Logging to check data binding
        Log.d("AdapterBinding", "Binding data for position: $position, name: ${deleteRequest.deceasedName}")

        // Handle approve and reject button clicks
        approveButton.setOnClickListener {
            activity.handleDeleteRequest(position, true)
        }

        rejectButton.setOnClickListener {
            activity.handleDeleteRequest(position, false)
        }

        return rowView
    }
}

