package com.example.android.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.android.instafire.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

private lateinit var binding : ActivityLoginBinding
private var TAG = "Login Activity"

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        val mAuth = FirebaseAuth.getInstance()
        if(mAuth.currentUser != null){
            goPostActivity()
        }
        binding.button.setOnClickListener {
            binding.button.isEnabled = false
            val email = binding.Username.text.toString()
            val password = binding.Password.text.toString()
            if(email.isBlank() || password.isBlank()){
                Toast.makeText(this,"Not valid email or password",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            //FireBase Authencation check
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                binding.button.isEnabled = true
                if(task.isSuccessful){
                    Toast.makeText(this,"Success",Toast.LENGTH_SHORT).show()
                    goPostActivity()
                }
                else{
                    Log.i(TAG,"signEmailfailed",task.exception)
                    Toast.makeText(this,"Authentication failed",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun goPostActivity() {
        Log.i(TAG,"goPostActivity()")
        val intent = Intent(this,PostsActivity::class.java)
        startActivity(intent)
        finish()
    }
}