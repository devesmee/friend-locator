package com.example.android_2

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileRegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileRegisterFragment : Fragment() {

    private lateinit var transaction: FragmentTransaction
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private lateinit var currentview: View
    private lateinit var usernameText: TextView
    private lateinit var profilePictureTextField: TextView
    private lateinit var profilePicturePreview: ImageView
    private lateinit var ageTextField: EditText
    private lateinit var genderTextField: EditText
    private lateinit var professionTextField: EditText
    private lateinit var errorProfileRegisterText: TextView
    private lateinit var registerButton: Button

    private val PICKIMAGEREQUEST = 22
    private lateinit var filePath: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        database = Firebase.database.reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.reference;
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        transaction = activity?.supportFragmentManager?.beginTransaction()!!
        // Inflate the layout for this fragment
        currentview = inflater.inflate(R.layout.fragment_profile_register, container, false)

        usernameText = currentview.findViewById(R.id.registerProfileUsername)
        getCurrentUsername()
        profilePictureTextField = currentview.findViewById(R.id.profilePictureRegisterTextField)
        profilePictureTextField.setOnClickListener { chooseImage() }
        profilePicturePreview = currentview.findViewById(R.id.profilePicturePreview)
        ageTextField = currentview.findViewById(R.id.ageRegisterTextField)
        genderTextField = currentview.findViewById(R.id.genderRegisterTextField)
        professionTextField = currentview.findViewById(R.id.professionRegisterTextField)
        errorProfileRegisterText = currentview.findViewById(R.id.profileRegisterErrorText)
        registerButton = currentview.findViewById(R.id.registerButton)

        setNavigationActions()

        return currentview
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment ProfileRegisterFragment.
         */
        fun newInstance() =
            ProfileRegisterFragment()
    }

    private fun getCurrentUsername() {
        database.child("Users").child(auth.currentUser!!.uid).child("username").get().addOnSuccessListener {
            usernameText.text = it.value.toString()
        }
    }

    private fun updateUserInfo(profilePicture: String, age: Int, gender: String, profession: String) {
        database.child("Users").child(auth.currentUser!!.uid).child("age").setValue(age)
        database.child("Users").child(auth.currentUser!!.uid).child("gender").setValue(gender)
        database.child("Users").child(auth.currentUser!!.uid).child("profession").setValue(profession)
        uploadImage()
        database.child("Users").child(auth.currentUser!!.uid).child("profilePicture").setValue("images/" + usernameText.text)
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*";
        intent.action = Intent.ACTION_GET_CONTENT;
        startActivityForResult(
            Intent.createChooser(intent, "Select your profile picture"),
            PICKIMAGEREQUEST
        );
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICKIMAGEREQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data!!
            try {
                //profilePictureUpload.visibility = View.INVISIBLE
                profilePictureTextField.visibility = View.INVISIBLE
                val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, filePath)
                profilePicturePreview.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage() {
        if (filePath != null) {
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Uploading...")
            progressDialog.show()
            val ref = storageReference.child("images/" + usernameText.text)
            ref.putFile(filePath)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    e.message?.let { Log.e("upload failed: ", it) }
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot
                        .totalByteCount
                    progressDialog.setMessage("Uploaded " + progress.toInt() + "%")
                }
        }
    }

    private fun setNavigationActions() {
        registerButton.setOnClickListener {
            if(profilePictureTextField.text.isEmpty() || ageTextField.text.isEmpty() || genderTextField.text.isEmpty() || professionTextField.text.isEmpty())
            {
                errorProfileRegisterText.text = getString(R.string.error_empty_text_fields)
                errorProfileRegisterText.visibility = View.VISIBLE
            } else {
                updateUserInfo(
                    profilePictureTextField.text.toString(),
                    ageTextField.text.toString().toInt(),
                    genderTextField.text.toString(),
                    professionTextField.text.toString()
                )
                transaction.replace(R.id.fc1, MapsFragment())
                transaction.disallowAddToBackStack()
                transaction.commit()
            }
        }
    }
}