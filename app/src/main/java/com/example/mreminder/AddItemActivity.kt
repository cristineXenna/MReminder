package com.example.mreminder

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import com.example.mreminder.model.Detail
import com.example.mreminder.model.MedicineModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale


class AddItemActivity : AppCompatActivity() {

    lateinit var notifBtn : ImageButton
    lateinit var cancle_Btn : Button
    lateinit var oke_btn : Button
    lateinit var radioGroup: RadioGroup
    lateinit var unit : RadioGroup
    lateinit var DateRange: EditText
    lateinit var DateRangeBtn: Button
    lateinit var Name : EditText
    lateinit var Dose : EditText
    lateinit var TotalOfMedicine : EditText
    var IDArray = arrayOf<Int>()
    var ids = 0
    var image = ""
    var units = ""
    var timeArray = arrayOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        Name = findViewById(R.id.add_name)
        Dose = findViewById(R.id.add_dose)
        TotalOfMedicine = findViewById(R.id.total_of_medicine)

        if (Detail.editable === true){
//            get data dari firebase pake detail.key
//            tampilin data di fieldnya
                getData()
            getTime()
        }

        notifBtn = findViewById(R.id.notifBtn)
        notifBtn.setOnClickListener {
            createNewButton("00:00")
        }

        cancle_Btn = findViewById(R.id.cancleBtn)
        cancle_Btn.setOnClickListener {
            Detail.editable = false
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        oke_btn = findViewById(R.id.okBtn)
        oke_btn.setOnClickListener {
            createNewItem()
            Detail.editable = false
        }

        radioGroup = findViewById(R.id.radio_group)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            radioProcess(checkedId)
        }

        DateRange = findViewById(R.id.add_date)
        DateRangeBtn = findViewById(R.id.add_date_btn)
        DateRangeBtn.setOnClickListener{
            datePickerDialog()
        }

        unit = findViewById(R.id.unit)
        unit.setOnCheckedChangeListener { group, checkedId ->
            unitProcess(checkedId)
        }
    }

    private fun getTime() {
        val UID = FirebaseAuth.getInstance().uid.toString()
        val child = Detail.key
        FirebaseDatabase.getInstance()
            .getReference(UID)
            .child("medicines")
            .child(child)
            .child("time")
            .addValueEventListener(object  : ValueEventListener{
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (data in snapshot.children){
                            createNewButton(data.value.toString())
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun getData() {
        val uid = FirebaseAuth.getInstance().uid.toString()
        val child = Detail.key
        val medicineModels:MutableList<MedicineModel> = ArrayList()

        FirebaseDatabase.getInstance()
            .getReference(uid)
            .child("medicines")
            .child(child)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (data in snapshot.children){
                            var datas = snapshot.getValue(MedicineModel::class.java)
                            datas!!.key = data.key
                            medicineModels.add(datas)
                        }
                        var data = medicineModels[0]

                        Name.setText("${data.name}")
                        Dose.setText("${data.dose}")
                        DateRange.setText("${data.dateRange}")
                        TotalOfMedicine.setText("${data.totalOfMedicine}")

                        if (data.unit === "ml"){
                            unit.check(R.id.ml)
                        }
                        else{
                            unit.check(R.id.capsule)
                        }

                        when (data.image?.get(36).toString()){
                            "1" -> radioGroup.check(R.id.radio1)
                            "2" -> radioGroup.check(R.id.radio2)
                            "3" -> radioGroup.check(R.id.radio3)
                            "4" -> radioGroup.check(R.id.radio4)
                            else -> {
                                radioGroup.check(R.id.radio5)
                            }
                        }
//                            println("Medicine = ${data.image?.get(36)}")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun unitProcess(checkedId: Int) {
        val unit = findViewById<RadioButton>(checkedId).text
        units = unit.toString()
//        Toast.makeText(this, "$unit", Toast.LENGTH_SHORT).show()
    }

    private fun radioProcess(checkedId: Int) {
        val radio = findViewById<RadioButton>(checkedId)
        if (radio == findViewById(R.id.radio1)){
            image = "https://r2.easyimg.io/e2wshqbun/img_1.png"
        }

        if (radio == findViewById(R.id.radio2)){
            image = "https://r2.easyimg.io/e2wshqbun/img_2.png"
        }

        if (radio == findViewById(R.id.radio3)){
            image = "https://r2.easyimg.io/e2wshqbun/img_3.png"
        }

        if (radio == findViewById(R.id.radio4)){
            image = "https://r2.easyimg.io/e2wshqbun/img_4.png"
        }

        if (radio == findViewById(R.id.radio5)){
            image = "https://r2.easyimg.io/e2wshqbun/img_5.png"
        }
    }

    private fun datePickerDialog() {
        val builder: MaterialDatePicker.Builder<Pair<Long, Long>> =
            MaterialDatePicker.Builder.dateRangePicker()
        builder.setTitleText("Select a date range")

        val datePicker = builder.build()
        datePicker.addOnPositiveButtonClickListener { selection: Pair<Long, Long> ->

            val startDate = selection.first
            val endDate = selection.second

            val sdf =
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val startDateString: String = sdf.format(Date(startDate))
            val endDateString: String = sdf.format(Date(endDate))

            val selectedDateRange = "$startDateString - $endDateString"

            DateRange.setText(selectedDateRange)
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun createNewItem() {
        val uid = FirebaseAuth.getInstance().uid
        val name = findViewById<EditText>(R.id.add_name).text.toString()
        val dose = findViewById<EditText>(R.id.add_dose).text.toString()
        val dateRange = findViewById<EditText>(R.id.add_date).text.toString()
        val totalOfMedicine = findViewById<EditText>(R.id.total_of_medicine).text.toString().toInt()

        val data = hashMapOf(
            "name" to name,
            "dose" to dose,
            "dateRange" to dateRange,
            "image" to image,
            "unit" to units,
            "totalOfMedicine" to totalOfMedicine,
            "ussedMedicine" to 0
        )

        FirebaseDatabase.getInstance().getReference(uid.toString())
            .child("medicines")
            .child(name).setValue(data)
            .addOnSuccessListener {
                for (i in 0 until IDArray.size){
                    val Data = findViewById<Button>(IDArray[i]).text.toString()
                    FirebaseDatabase.getInstance().getReference(uid.toString())
                        .child("medicines")
                        .child(name).child("time")
                        .child(i.toString())
                        .setValue(Data)
                }

                Toast.makeText(applicationContext,"New Medicine Is Added",
                Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

    }

    private fun createNewButton(text: String) {
        val layout = findViewById(R.id.layoutNotif) as LinearLayout
        val layout2 = findViewById<LinearLayout>(R.id.layoutNotif2)
        val form = Button(this)
        val id = View.generateViewId()

        form.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        form.text = text
        form.id = id
        form.setOnClickListener{
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR)
            val minute = calendar.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                form.setText("$hourOfDay:$minute")
            }, hour, minute, true)

            timePickerDialog.show()
        }
        println(id)
        IDArray += arrayOf(id)

        if (ids < 4){
            layout.addView(form)
        }
        else{
            layout2.addView(form)
        }

        ids += 1
//        println("ID = ${IDArray.contentToString()}")
    }
}