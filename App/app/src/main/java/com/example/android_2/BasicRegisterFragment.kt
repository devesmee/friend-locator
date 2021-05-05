package com.example.android_2

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * A simple [Fragment] subclass.
 * Use the [BasicRegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BasicRegisterFragment : Fragment() {

    private lateinit var transaction: FragmentTransaction
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var currentview: View
    private lateinit var usernameTextField: EditText
    private lateinit var emailTextField: EditText
    private lateinit var passwordTextField: EditText
    private lateinit var errorBasicRegisterText: TextView
    private lateinit var nextStepButton: Button
    private lateinit var alreadyAccountText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        database = Firebase.database.reference
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        transaction = activity?.supportFragmentManager?.beginTransaction()!!
        // Inflate the layout for this fragment
        currentview = inflater.inflate(R.layout.fragment_basic_register, container, false)

        usernameTextField = currentview.findViewById(R.id.usernameRegisterTextField)
        emailTextField = currentview.findViewById(R.id.emailRegisterTextField)
        passwordTextField = currentview.findViewById(R.id.passwordRegisterTextField)
        errorBasicRegisterText = currentview.findViewById(R.id.basicRegisterErrorText)
        nextStepButton = currentview.findViewById(R.id.nextRegisterButton)
        alreadyAccountText = currentview.findViewById(R.id.alreadyAccountText)

        setNavigationActions()

        return currentview
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment BasicRegisterFragment.
         */
        fun newInstance() =
                BasicRegisterFragment()
    }

    private fun createNewUser(username: String, email: String) {
        val user = User(username, email)
        database.child("Users").child(auth.currentUser!!.uid).setValue(user)
    }

    private fun setNavigationActions() {
        nextStepButton.setOnClickListener {
            if(emailTextField.text.isEmpty() || passwordTextField.text.isEmpty() || usernameTextField.text.isEmpty())
            {
                errorBasicRegisterText.text = getString(R.string.error_empty_text_fields)
                errorBasicRegisterText.visibility = View.VISIBLE
            } else {
                auth.createUserWithEmailAndPassword(emailTextField.text.toString(), passwordTextField.text.toString())
                    .addOnCompleteListener(OnCompleteListener<AuthResult> {
                            task ->
                        if(task.isSuccessful)
                        {
                            Log.d("REGISTER SUCCESSFUL ", "createUserWithEmail:success")
                            createNewUser(usernameTextField.text.toString(), emailTextField.text.toString())
                            transaction.replace(R.id.fc1, ProfileRegisterFragment())
                            transaction.disallowAddToBackStack()
                            transaction.commit()
                        } else {
                            val errorCode = (task.exception as FirebaseAuthException?)!!.errorCode
                            Log.e("ERROR CODE: ", errorCode);
                            Log.e("REGISTER FAILED ", "createUserWithEmail:failure:", task.exception)
                            setErrorLabel(errorCode)
                        }

                    })
            }
        }

        alreadyAccountText.setOnClickListener {
            transaction.replace(R.id.fc1, LoginFragment())
            transaction.disallowAddToBackStack()
            transaction.commit()
        }
    }

    private fun setErrorLabel(error: String)
    {
        when(error) {
            "ERROR_INVALID_EMAIL" -> errorBasicRegisterText.text = getString(R.string.error_invalid_email)
            "ERROR_WEAK_PASSWORD" -> errorBasicRegisterText.text = getString(R.string.error_password_instructions)
            else -> errorBasicRegisterText.text = getString(R.string.error_general)
        }
        errorBasicRegisterText.visibility = View.VISIBLE
    }
}