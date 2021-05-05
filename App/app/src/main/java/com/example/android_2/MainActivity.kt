package com.example.android_2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            val mapsFragment = MapsFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fc1, mapsFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
        } else {
            val loginFragment = LoginFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fc1, loginFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
        }
    }
}