package com.example.mreminder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.mreminder.LoginActivity
import com.example.mreminder.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var username: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirm_password: EditText
    private lateinit var register_button: Button
    private lateinit var login: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth
        username = findViewById(R.id.username)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        confirm_password = findViewById(R.id.conf_password)
        register_button = findViewById(R.id.register_btn)
        login = findViewById(R.id.txt_login)

        login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        register_button.setOnClickListener {
            registration()
        }

    }

    private fun registration() {
        var Username = username.text.toString()
        var Email = email.text.toString()
        var Password = password.text.toString()
        var ConfPassword = confirm_password.text.toString()

        if (Username.isEmpty()) {
            Toast.makeText(this, "Username tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        if (Email.isEmpty()) {
            Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        else if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            Toast.makeText(this, "Email anda tidak valid", Toast.LENGTH_LONG).show()
            return
        }

        if (Password.isEmpty()) {
            Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        else if (Password.length <= 6){
            Toast.makeText(this, "Password tidak boleh kurang dari 6 karakter", Toast.LENGTH_SHORT).show()
            return
        }

        if (ConfPassword.isEmpty()) {
            Toast.makeText(this, "Konfirmasi password anda", Toast.LENGTH_SHORT).show()
            return
        }

        else if (ConfPassword != Password){
            Toast.makeText(this, "Password tidak sama", Toast.LENGTH_SHORT).show()
            return
        }

        regisUser(Email, Password, Username)
    }

    private fun regisUser(email: String, password: String, username: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){
                if (it.isSuccessful){
                    val userId = auth.currentUser?.uid.toString()
                    FirebaseDatabase.getInstance().getReference(userId)
                        .child("username").setValue(username)

                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                else{
                    Toast.makeText(this, "Gagal Buat Akun", Toast.LENGTH_SHORT).show()
                }
            }
    }

}