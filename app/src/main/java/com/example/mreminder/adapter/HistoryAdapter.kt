package com.example.mreminder.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mreminder.HistoryActivity
import com.example.mreminder.R
import com.example.mreminder.listener.IRecycleClickListener
import com.example.mreminder.model.HistoryItemModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class HistoryAdapter(
    private val context : Context,
    private val list : List<HistoryItemModel>,
    private val activity : Activity
): RecyclerView.Adapter<HistoryAdapter.HistoryHolder>(){
    inner class HistoryHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var NamaObat : TextView? = null
        var DateObat : TextView? = null
        var TimeObat : TextView? = null
        var DoseObat : TextView? = null
        var btnDel : ImageButton? = null

        init {
            NamaObat = itemView.findViewById(R.id.Nama_Obat) as TextView
            DateObat = itemView.findViewById(R.id.Date_Obat) as TextView
            TimeObat = itemView.findViewById(R.id.Time_obat) as TextView
            DoseObat = itemView.findViewById(R.id.Dose_Obat) as TextView
            btnDel = itemView.findViewById(R.id.deleteHistory) as ImageButton

//            mMenus?.setOnClickListener { popupMenus(it) }
        }

//        private fun popupMenus(it: View?) {
//            AlertDialog.Builder(context)
//                .setTitle("Delete")
//                .setMessage("Are you sure delete this history?")
//                .setPositiveButton("Yes"){
//                    dialog,_->
//                    list.remove
//
//                }
//        }

        override fun onClick(v: View?) {

        }
    }

    override fun onBindViewHolder(
        holder: HistoryHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        holder.NamaObat!!.text = StringBuilder().append(list[position].name)
        holder.DateObat!!.text = StringBuilder().append(list[position].date)
        holder.TimeObat!!.text = StringBuilder().append(list[position].time)
        holder.DoseObat!!.text = StringBuilder().append(list[position].dose)
        holder.btnDel!!.setOnClickListener { _->
            val UID = FirebaseAuth.getInstance().uid.toString()
            val dialog = AlertDialog.Builder(context)
                .setTitle("Delete History")
                .setMessage("Are you sure delete this history?")
                .setNegativeButton("Cancle") {dialog,_-> dialog.dismiss()}
                .setPositiveButton("Yes") {dialog,_->
                    notifyItemRemoved(position)
                    FirebaseDatabase.getInstance()
                        .getReference(UID)
                        .child("history")
                        .child(list[position].key!!)
                        .removeValue()
                        .addOnSuccessListener { Toast.makeText(context, "History deleted", Toast.LENGTH_SHORT).show()
                            activity.recreate()}
                }
                .create()
            dialog.show()
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        return HistoryHolder(LayoutInflater.from(context)
            .inflate(R.layout.item_history, parent, false))

    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        TODO("Not yet implemented")
    }
}