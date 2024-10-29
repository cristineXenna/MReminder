package com.example.mreminder

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Patterns
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var email:EditText
    private lateinit var password: EditText
    private lateinit var forgetPass:TextView
    private lateinit var signUp:TextView
    private lateinit var loginButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_login)

        auth = Firebase.auth
        email = findViewById(R.id.login_email)
        password = findViewById(R.id.login_pass)
        forgetPass = findViewById(R.id.forget_pass)
        signUp = findViewById(R.id.txt_register)
        loginButton = findViewById(R.id.login_btn)

        signUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
        loginButton.setOnClickListener {
            login()
        }
        forgetPass.setOnClickListener {
            forgetPassword()
        }
    }

    private fun forgetPassword() {
        val lp : WindowManager.LayoutParams =WindowManager.LayoutParams()
        val dialog = Dialog (this)

        lp.copyFrom(dialog.window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.forget_password)
        val Email = dialog.findViewById<EditText>(R.id.reset_email)
        val yes = dialog.findViewById<Button>(R.id.confirm_reset_button)
        val no = dialog.findViewById<Button>(R.id.batal_reset_button)

        yes.setOnClickListener {
            if (Email.text.toString().isEmpty()) {
                Toast.makeText(this,"Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
            else if (!Patterns.EMAIL_ADDRESS.matcher(Email.text.toString()).matches()){
                Toast.makeText(this, "Email Anda tidak valid", Toast.LENGTH_SHORT).show()
            }
            else{
                auth.sendPasswordResetEmail(Email.text.toString())
                Toast.makeText(this, "Email Reset Password Telah Dikirim Silahkan Cek Email Anda", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        no.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
        dialog.window?.attributes = lp
    }

    private fun login() {
        var Email = email.text.toString()
        var Password = password.text.toString()
        if (Email.isEmpty()){
            Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }
        if (Password.isEmpty()){
            Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
            Toast.makeText(this, "Email tidak valid", Toast.LENGTH_SHORT).show()
        }
        loginUser(Email, Password)
    }

    private fun loginUser(logEmail: String, logPassword: String) {
        auth.signInWithEmailAndPassword(logEmail, logPassword)
            .addOnCompleteListener (this){
                if (it.isSuccessful){
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else {
                    Toast.makeText(this, "Email atau password anda salah", Toast.LENGTH_SHORT).show()
                }
            }

    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null){
            Intent(this, MainActivity::class.java).also { intent ->
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }
}