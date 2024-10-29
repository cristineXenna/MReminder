package com.example.mreminder.fragment

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.mreminder.LoginActivity
import com.example.mreminder.MainActivity
import com.example.mreminder.R
import com.example.mreminder.databinding.FragmentUserBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File

class UserFragment : Fragment() {
    private var _binding : FragmentUserBinding? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var imgUri: Uri
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =FragmentUserBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val user =auth.currentUser
        binding.delAkun.setOnClickListener { deleteAccount() }
        binding.logout.setOnClickListener { btnLogout() }
        binding.chngPsswrd.setOnClickListener { changePassword() }
        binding.btnBack1.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        binding.cviUser.setOnClickListener {
            goToCamera()
        }


        if (user != null){
            binding.chngName.setText(user.displayName)
            //${FirebaseAuth.getInstance().currentUser?.email}

            // Inisialisasi FirebaseStorage
            val storage = Firebase.storage
            val storageRef = storage.reference
            val uid = FirebaseAuth.getInstance().uid.toString()
            val imageRef = storageRef.child("images/${uid}_profile.jpg")
            imageRef.downloadUrl.addOnSuccessListener {
                Log.d("UserFragment","downloadUrl success")
                Picasso.get().load(it).into(binding.cviUser)
            }
        }
        binding.btnSave.setOnClickListener {
            val image =when{
                :: imgUri.isInitialized -> imgUri
                user?.photoUrl == null -> Uri.parse("https://picsum.photos/id/316/200")
                else->user.photoUrl
            }
            val name = binding.chngName.text.toString().trim()
            if (name.isEmpty()){
                binding.chngName.error ="Nama harus di isi"
                binding.chngName.requestFocus()
                return@setOnClickListener
            }
            UserProfileChangeRequest.Builder()
                .setDisplayName(name)
//                .setPhotoUri(image)

                .build().also {
                    user?.updateProfile(it)?.addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(activity, "Profile update", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(activity, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }

    private fun deleteAccount() {
        val cUser = FirebaseAuth.getInstance().currentUser
        if (cUser != null){
            binding.delAkun.setOnClickListener {
                cUser.delete()
                    .addOnSuccessListener {
                        Toast.makeText(context,"Delete Account Successful", Toast.LENGTH_SHORT).show()
                        val mainIntent = Intent(context, LoginActivity::class.java)
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(mainIntent)
                        activity?.finish()
                    }
            }
        }
    }

    private fun btnLogout() {
        auth = FirebaseAuth.getInstance()
        auth.signOut()
        val intent=Intent(context, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun changePassword() {
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        binding.cardVerifyPass.visibility = View.VISIBLE
        binding.btnBatal.setOnClickListener { binding.cardVerifyPass.visibility=View.GONE }

        binding.btnKonfirm1.setOnClickListener btnConfirm@{
            val pass = binding.edtPass.text.toString()
            if (pass.isEmpty()){
                binding.edtPass.error="Password tidak boleh kosong"
                binding.edtPass.requestFocus()
                return@btnConfirm
            }
            user.let {
                val userCredential = EmailAuthProvider.getCredential(it?.email!!,pass)
                it.reauthenticate(userCredential).addOnCompleteListener { task->
                    when{
                        task.isSuccessful ->{
                            binding.cardUpdatePass.visibility=View.VISIBLE
                            binding.cardVerifyPass.visibility=View.GONE
                        }
                        task.exception is FirebaseAuthInvalidCredentialsException ->{
                            binding.edtPass.error="Password salah"
                            binding.edtPass.requestFocus()
                        }
                        else->{
                            Toast.makeText(context,"\${task.exception?.message", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            binding.btnCancel2.setOnClickListener {
                binding.cardVerifyPass.visibility=View.GONE
                binding.cardUpdatePass.visibility=View.VISIBLE
            }
            binding.btnUpdate.setOnClickListener newChangePass@{
                val newPass = binding.edtNewPass.text.toString()
                val passConfirm = binding.edtConfirmPass.text.toString()

                if (newPass.isEmpty()){
                    binding.edtPass.error="Password tidak boleh kosong"
                    binding.edtPass.requestFocus()
                    return@newChangePass
                }
                if (passConfirm.isEmpty()){
                    binding.edtPass.error="Ulangi password baru"
                    binding.edtPass.requestFocus()
                    return@newChangePass
                }
                if (newPass.length <6){
                    binding.edtPass.error="Password harus lebih dari 6 karakter"
                    binding.edtPass.requestFocus()
                    return@newChangePass
                }
                if (passConfirm.length <6){
                    binding.edtNewPass.error="Password tidak sama"
                    binding.edtPass.requestFocus()
                    return@newChangePass
                }
                if (newPass != passConfirm){
                    binding.edtPass.error="Password tidak sama"
                    binding.edtPass.requestFocus()
                    return@newChangePass
                }
                user?.let {
                    user.updatePassword(newPass).addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(context,"Paassword berhasil di update", Toast.LENGTH_SHORT).show()
                            successLogout()
                        }else{
                            Toast.makeText(context,"${it.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun successLogout() {
        auth=FirebaseAuth.getInstance()
        auth.signOut()

        val intent = Intent(context, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
        Toast.makeText(context,"Silahkan login kembali", Toast.LENGTH_SHORT).show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
            val bytes = ByteArrayOutputStream()
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
            val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
            return Uri.parse(path)
        }

        if (requestCode == REQ_CAM && resultCode == RESULT_OK && data!= null){
            val bitmap = data?.extras?.get("data") as Bitmap
            binding.cviUser.setImageBitmap(bitmap)
            uploadImageToFirebase(bitmap)
        }
    }

    private fun uploadImageToFirebase(bitmap: Bitmap) {
        binding.cviUser.isDrawingCacheEnabled =true
        binding.cviUser.buildDrawingCache()
        val bitmap = (binding.cviUser.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        val storageRef = Firebase.storage.reference
        val uid = FirebaseAuth.getInstance().uid.toString()
        val imgRef = storageRef.child("images/${uid}_profile.jpg")
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos)
        val data = baos.toByteArray()

        val uploadTask = imgRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            imgRef.downloadUrl.addOnSuccessListener { uri->
                imgUri = uri
                binding.cviUser.setImageBitmap(bitmap)
            }
        }.addOnFailureListener { exception->

        }
    }

    private fun goToCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            activity?.packageManager?.let {
                intent?.resolveActivity(it).also {
                    startActivityForResult(intent, REQ_CAM)
                }
            }
        }
    }

    companion object {
        const val REQ_CAM = 100
    }
}