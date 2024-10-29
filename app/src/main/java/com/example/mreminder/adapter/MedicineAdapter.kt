package com.example.mreminder.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mreminder.DetailActivity
import com.example.mreminder.R
import com.example.mreminder.listener.IRecycleClickListener
import com.example.mreminder.model.Detail
import com.example.mreminder.model.MedicineModel
import java.lang.StringBuilder

class MedicineAdapter (
    private val context : Context,
    private val list: List<MedicineModel>,
    private val activity: Activity
) : RecyclerView.Adapter<MedicineAdapter.MedicineHolder>() {
    class MedicineHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var imageView : ImageView?=null
        var txtName : TextView?=null
        var txtDose : TextView?=null

        private var clickListener: IRecycleClickListener?=null
        fun setOnClickListener(clickListener: IRecycleClickListener){
            this.clickListener=clickListener
        }
        init {
            imageView = itemView.findViewById(R.id.imageView) as ImageView
            txtName = itemView.findViewById(R.id.txtName) as TextView
            txtDose = itemView.findViewById(R.id.txtDose) as TextView

            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
        clickListener!!.onItemClickListener(v, adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineHolder {
        return MedicineHolder(LayoutInflater.from(context)
            .inflate(R.layout.layout_madecine_item, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(
        holder: MedicineHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        Glide.with(context)
            .load(list[position].image)
            .into(holder.imageView!!)
        holder.txtName!!.text = StringBuilder().append(list[position].name)
        holder.txtDose!!.text = StringBuilder().append("${list[position].dose} ${list[position].unit}")

        holder.setOnClickListener(object : IRecycleClickListener{
            override fun onItemClickListener(view: View?, position: Int) {
                println(list[position].key)
                Detail.key = list[position].key.toString()
                var intent = Intent(activity, DetailActivity::class.java)
                context.startActivity(intent)
            }
        })
    }


    override fun onBindViewHolder(holder: MedicineHolder, position: Int) {
        TODO("Not yet implemented")
    }

}