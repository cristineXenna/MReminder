package com.example.mreminder

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mreminder.adapter.HistoryAdapter
import com.example.mreminder.listener.IHistory
import com.example.mreminder.model.HistoryItemModel
import com.example.mreminder.utils.SpaceItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryActivity : AppCompatActivity(), IHistory {
    lateinit var historyListener: IHistory
    lateinit var recycleItem2: RecyclerView
    lateinit var btnBack2 : ImageButton
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        recycleItem2= findViewById(R.id.rcycl_history)
        init()
        loadItemFromFirebase()
        btnBack2 = findViewById(R.id.btn_backhistory)
        btnBack2.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun loadItemFromFirebase() {
        val historyModels : MutableList<HistoryItemModel> = ArrayList()
        val UID = FirebaseAuth.getInstance().uid.toString()
        FirebaseDatabase.getInstance()
            .getReference(UID)
            .child("history")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (historySnapshot in snapshot.children){
                            val historyModell = historySnapshot.getValue(HistoryItemModel::class.java)
                            historyModell!!.key = historySnapshot.key
                            historyModels.add(historyModell)
                        }
                        historyListener.onLoadSucces(historyModels)
                    }
//                    else historyListener.onLoadFail("History not found")
                    else println("HIStory not found")
                }

                override fun onCancelled(error: DatabaseError) {
//                    historyListener.onLoadFail(error.message)
                    println(error.message)
                }
            })
    }
    private fun init(){
        historyListener=this
        val gridLayoutManager=GridLayoutManager(this, 1)
        recycleItem2.layoutManager=gridLayoutManager
//        recycleItem2.addItemDecoration(SpaceItemDecoration())
    }
    override fun onLoadSucces(HistoryModelList: List<HistoryItemModel>?) {
        val adapter = HistoryAdapter(this, HistoryModelList!!, this)
        recycleItem2.adapter = adapter
    }

    override fun onLoadFail(message: String?) {
        Toast.makeText(this, message!!, Toast.LENGTH_LONG).show()
    }
}