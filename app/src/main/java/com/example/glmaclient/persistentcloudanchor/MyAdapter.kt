package com.example.glmaclient.persistentcloudanchor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.glmaclient.persistentcloudanchor.R

class MyAdapter(private var userList: ArrayList<Model>, private val context: Context) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private var filteredList: ArrayList<Model> = ArrayList(userList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.single_list, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    fun searchDataList(searchList: ArrayList<Model>) {
        filteredList = searchList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = filteredList[position]

        holder.bind(currentItem)

        holder.itemView.setOnClickListener{
            val intent = Intent(context, DeceasedInfo::class.java).apply {
                putExtra("deceasedName", currentItem.deceasedName)
                putExtra("birthDate", currentItem.birthDate)
                putExtra("deathDate", currentItem.deathDate)
                putExtra("lotNumber", currentItem.lotNumber)
                putExtra("lotPhoto", currentItem.lotPhoto)
            }
            context.startActivity(intent)
        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDeceaseName: TextView = itemView.findViewById(R.id.deceasedNameList)
        val txtLotAddress: TextView = itemView.findViewById(R.id.lotNumberList)
        val txtDeathDate: TextView = itemView.findViewById(R.id.deathDateList)
        val txtBirthDate: TextView = itemView.findViewById(R.id.birthDateList)
        val imageView: ImageView = itemView.findViewById(R.id.graveImageList)

        fun bind(model: Model) {
            txtDeceaseName.text = model.deceasedName
            txtLotAddress.text = model.lotNumber
            txtDeathDate.text = model.deathDate
            txtBirthDate.text = model.birthDate

            Glide.with(itemView.context)
                .load(model.lotPhoto)
                .placeholder(R.drawable.imagetest)
                .into(imageView)
        }
    }
}
