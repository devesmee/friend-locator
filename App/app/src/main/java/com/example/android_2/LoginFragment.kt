package com.example.android_2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    private lateinit var transaction: FragmentTransaction
    private lateinit var auth: FirebaseAuth

    private lateinit var currentView: View
    private lateinit var emailTextField: EditText
    private lateinit var passwordTextField: EditText
    private lateinit var errorLoginText: TextView
    private lateinit var loginButton: Button
    private lateinit var noAccountText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        transaction = activity?.supportFragmentManager?.beginTransaction()!!
        // Inflate the layout for this fragment
        currentView = inflater.inflate(R.layout.fragment_login, container, false)

        emailTextField = currentView.findViewById(R.id.emailTextField)
        passwordTextField = currentView.findViewById(R.id.passwordTextField)
        errorLoginText = currentView.findViewById(R.id.loginErrorText)
        loginButton = currentView.findViewById(R.id.loginButton)
        noAccountText = currentView.findViewById(R.id.noAccountText)

        setNavigationActions()

        return currentView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment LoginFragment.
         */
        fun newInstance() =
                LoginFragment()
    }

    private fun setNavigationActions() {
        loginButton.setOnClickListener {
            if(emailTextField.text.isEmpty() || passwordTextField.text.isEmpty())
            {
                errorLoginText.text = getString(R.string.error_empty_text_fields)
                errorLoginText.visibility = View.VISIBLE
            } else {
                auth.signInWithEmailAndPassword(
                    emailTextField.text.toString(),
                    passwordTextField.text.toString()
                )
                        .addOnCompleteListener(OnCompleteListener<AuthResult> { task ->
                            if (task.isSuccessful) {
                                Log.d("LOGIN SUCCESFUL ", "signInWithEmail:success")
                                transaction.replace(R.id.fc1, MapsFragment())
                                transaction.disallowAddToBackStack()
                                transaction.commit()
                            } else {
                                val errorCode = (task.exception as FirebaseAuthException?)!!.errorCode
                                Log.e("LOGIN FAILED ", "signInWithEmail:failure", task.exception)
                                setErrorLabel(errorCode)
                            }
                        })
            }
        }

        noAccountText.setOnClickListener {
            transaction.replace(R.id.fc1, BasicRegisterFragment())
            transaction.disallowAddToBackStack()
            transaction.commit()
        }
    }

    private fun setErrorLabel(error: String)
    {
        when(error) {
            "ERROR_INVALID_EMAIL" -> errorLoginText.text = getString(R.string.error_invalid_email)
            "ERROR_USER_NOT_FOUND" -> errorLoginText.text = getString(R.string.error_invalid_user)
            "ERROR_WRONG_PASSWORD" -> errorLoginText.text = getString(R.string.error_wrong_password)
            else -> errorLoginText.text = getString(R.string.error_general)
        }
        errorLoginText.visibility = View.VISIBLE
    }


}