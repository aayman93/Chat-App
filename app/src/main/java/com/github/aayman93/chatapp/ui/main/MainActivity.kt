package com.github.aayman93.chatapp.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.PopupMenu
import com.github.aayman93.chatapp.R
import com.github.aayman93.chatapp.databinding.ActivityMainBinding
import com.github.aayman93.chatapp.ui.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivMore.setOnClickListener { v ->
            PopupMenu(this, v).apply {
                setOnMenuItemClickListener(this@MainActivity)
                inflate(R.menu.menu_popup)
                show()
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when(item?.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                Intent(this, AuthActivity::class.java).also {
                    startActivity(it)
                    finish()
                }
                true
            }
            else -> false
        }
    }


}