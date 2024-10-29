package com.example.mreminder

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.mreminder.model.Detail
import com.example.mreminder.model.MedicineModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
class DetailActivity : AppCompatActivity() {

    lateinit var btn_back : ImageButton
    lateinit var image : ImageView
    lateinit var tv_name : TextView
    lateinit var tv_dose : TextView
    lateinit var tv_next_dose : TextView
    lateinit var tv_detail_dose : TextView
    lateinit var tv_program : TextView
    lateinit var tv_quanitty : TextView
    lateinit var change_btn : Button
    lateinit var delete_btn : Button
    var timeArray = arrayOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        btn_back = findViewById(R.id.back_button)
        image = findViewById(R.id.detail_image)
        tv_name = findViewById(R.id.detail_name)
        tv_dose = findViewById(R.id.detail_dose)
        tv_next_dose = findViewById(R.id.detail_next_dose)
        tv_detail_dose = findViewById(R.id.detail_dose_time)
        tv_program = findViewById(R.id.detail_program)
        tv_quanitty = findViewById(R.id.detail_quantity)
        change_btn = findViewById(R.id.change_btn)
        delete_btn = findViewById(R.id.delete_btn)

        btn_back.setOnClickListener {
            var inten = Intent(this, MainActivity::class.java)
            startActivity(inten)
        }

        change_btn.setOnClickListener {
            Detail.editable = true
            val intent = Intent(this, AddItemActivity::class.java)
            println("Editable = ${Detail.editable}")
            startActivity(intent)
            finish()
//            Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show()
        }

        delete_btn.setOnClickListener {
            deleteProcess()
        }

        getDataFromDatabase()
        getTimeFromDatabase()


    }

    private fun deleteProcess() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Do You Want To Delete This Medicine ?")

        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            val uid = FirebaseAuth.getInstance().uid.toString()
            val child = Detail.key
            FirebaseDatabase.getInstance().getReference(uid)
                .child("medicines")
                .child(child)
                .removeValue()
            Toast.makeText(applicationContext,
                "Medicine Deleted", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        builder.setNegativeButton(android.R.string.no) { dialog, which ->
            Toast.makeText(applicationContext,
                android.R.string.no, Toast.LENGTH_SHORT).show()
        }

        builder.show()
    }

    private fun getTimeFromDatabase() {
        val UID = FirebaseAuth.getInstance().uid.toString()
        val child = Detail.key
        var detailTime = ""
        FirebaseDatabase.getInstance()
            .getReference(UID)
            .child("medicines")
            .child(child)
            .child("time")
            .addValueEventListener(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (data in snapshot.children){
                            timeArray += data.value.toString()

                        }
                        detailTime = timeArray.contentToString().replace("[","")

                        tv_detail_dose.text = "${timeArray.size} times | ${detailTime.replace("]","")} "

                        for (i in 0 until timeArray.size){
                            val now = LocalTime.now()
                            val time = LocalTime.parse(timeArray[i], DateTimeFormatter.ofPattern("H:m"))

                            if (now.hour < time.hour){
                                tv_next_dose.text = timeArray[i]
                                return
                            }

                            if (now.hour == time.hour && now.minute <= time.minute){
                                tv_next_dose.text = timeArray[i]
                                return
                            }

                            else{
                                tv_next_dose.text = "Tomorow"
                            }

//                            if (now.hour == time.hour || now.minute <= time.minute){
//                                tv_next_dose.text = timeArray[i]
//                                return
//                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun getDataFromDatabase() {
        val UID = FirebaseAuth.getInstance().uid.toString()
        val child = Detail.key
        val medicineModels:MutableList<MedicineModel> = ArrayList()

        FirebaseDatabase.getInstance()
            .getReference(UID)
            .child("medicines")
            .child(child)
            .addValueEventListener(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (data in snapshot.children){
                            var datas = snapshot.getValue(MedicineModel::class.java)
                            datas!!.key = data.key
                            medicineModels.add(datas)
                        }
                        val remainMedicine = medicineModels[0].totalOfMedicine.toString().toInt() - medicineModels[0].ussedMedicine.toString().toInt()

                        Glide.with(this@DetailActivity)
                            .load(medicineModels[0].image)
                            .into(image)

                        tv_name.text = medicineModels[0].name.toString()
                        tv_dose.text = "${medicineModels[0].dose.toString()} ${medicineModels[0].unit}"
                        tv_quanitty.text = "Total ${medicineModels[0].totalOfMedicine.toString()} ${medicineModels[0].unit} | ${remainMedicine} ${medicineModels[0].unit} left"
                        var dateRange = medicineModels[0].dateRange?.replace(" ", "")
                        if (dateRange != null) {
                            var startDate = LocalDate.parse(dateRange.substring(0, dateRange.indexOf("-")).replace("/", "-"), DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                            var endDate = LocalDate.parse(dateRange.substring(dateRange.indexOf("-")+1, dateRange.length).replace("/", "-"), DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                            var totalWeek = ChronoUnit.WEEKS.between(startDate, endDate)
                            var reminderWeek = ChronoUnit.WEEKS.between(LocalDate.now(), endDate)

                            tv_program.text = "Total $totalWeek weeks | $reminderWeek weeks left"
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })


    }
}