package com.example.glmaadmin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FeedbackAdapter(private val feedbackList: ArrayList<Feedback>) :
    RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.feedback_item, parent, false)
        return FeedbackViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbackList[position]
        holder.nameTextView.text = feedback.name
        holder.emailTextView.text = feedback.email
        holder.messageTextView.text = feedback.message
    }

    override fun getItemCount() = feedbackList.size

    class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
    }
}

