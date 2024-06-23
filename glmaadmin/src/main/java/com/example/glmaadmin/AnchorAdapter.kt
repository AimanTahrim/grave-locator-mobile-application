package com.example.glmaadmin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AnchorAdapter(private val anchors: List<Anchor>, private val onUpdateClick: (Anchor) -> Unit, private val onDeleteClick: (Anchor) -> Unit) : RecyclerView.Adapter<AnchorAdapter.AnchorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnchorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_anchor, parent, false)
        return AnchorViewHolder(view, onUpdateClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: AnchorViewHolder, position: Int) {
        val anchor = anchors[position]
        holder.bind(anchor)
    }

    override fun getItemCount() = anchors.size

    class AnchorViewHolder(itemView: View, private val onUpdateClick: (Anchor) -> Unit, private val onDeleteClick: (Anchor) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val anchorIdTextView: TextView = itemView.findViewById(R.id.anchorIdTextView)
        private val anchorNicknameTextView: TextView = itemView.findViewById(R.id.anchorNicknameTextView)
        private val anchorTimestampTextView: TextView = itemView.findViewById(R.id.anchorTimestampTextView)
        private val buttonUpdate: Button = itemView.findViewById(R.id.buttonUpdate)
        private val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)

        fun bind(anchor: Anchor) {
            anchorIdTextView.text = anchor.anchorId
            anchorNicknameTextView.text = anchor.anchorNickname
            anchorTimestampTextView.text = convertTimestampToTime(anchor.timestamp)

            buttonUpdate.setOnClickListener { onUpdateClick(anchor) }
            buttonDelete.setOnClickListener { onDeleteClick(anchor) }
        }

        private fun convertTimestampToTime(timestamp: Long): String {
            val date = Date(timestamp)
            val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return format.format(date)
        }
    }
}
