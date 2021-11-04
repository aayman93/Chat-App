package com.github.aayman93.chatapp.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.aayman93.chatapp.R
import com.github.aayman93.chatapp.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        if (auth.currentUser != null) {
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }
}