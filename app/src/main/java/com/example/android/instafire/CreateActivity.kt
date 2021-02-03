package com.example.android.instafire

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import com.example.android.instafire.databinding.ActivityCreateBinding
import com.example.android.instafire.databinding.ActivityPostsBinding
import com.example.android.instafire.models.Post
import com.example.android.instafire.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

private lateinit var binding: ActivityCreateBinding
private const val TAG = "Create Activity"
private const val PICK_PHOTO_CODE = 1234
class CreateActivity : AppCompatActivity() {
    private var signedinUser: User? = null
    private var photoUri: Uri? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var storageReference: StorageReference

//    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        storageReference = FirebaseStorage.getInstance().reference
        db = FirebaseFirestore.getInstance()
        db.collection("users").document(FirebaseAuth.getInstance().currentUser?.uid as String)
                .get()
                .addOnSuccessListener { userSnapshot ->
                    signedinUser = userSnapshot.toObject(User::class.java)
                    Log.i(TAG, "user signed in $signedinUser")
                }
                .addOnFailureListener { execption ->
                    Log.i(TAG, "ERROR", execption)
                }

        binding.btnPickImage.setOnClickListener {
            Log.i(TAG, "Open the image picker on device")
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"
            if (imagePickerIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(imagePickerIntent, PICK_PHOTO_CODE)
            }
        }

        binding.btnSubmit.setOnClickListener {
            handleSubmitButtonClick()
        }
    }

    private fun handleSubmitButtonClick() {
        if (photoUri == null) {
            Toast.makeText(this, "No photo selected", Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.etDescription.text.isBlank()) {
            Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (signedinUser == null) {
            Toast.makeText(this, "No signed in user, please wait", Toast.LENGTH_SHORT).show()
            return
        }
        binding.btnSubmit.isEnabled = false
        val photouploadUri = photoUri as Uri
        val photoReference = storageReference.child("images/${System.currentTimeMillis()}-photo.jpg")
        photoReference.putFile(photouploadUri)
                .continueWithTask { photoUploadTask ->
                    Log.i(TAG, "uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                    //Retrive image url of uploaded photo
                    photoReference.putFile(photouploadUri)
                            .continueWithTask { photoUploadTask ->
                                Log.i(TAG, "uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                                // Retrieve image url of the uploaded image
                                photoReference.downloadUrl
                            }.continueWithTask { downloadUrlTask ->
                                // Create a post object with the image URL and add that to the posts collection
                                val post = Post(
                                        binding.etDescription.text.toString(),
                                        downloadUrlTask.result.toString(),
                                        System.currentTimeMillis(),
                                        signedinUser)
                                db.collection("posts").add(post)
                            }.addOnCompleteListener { postCreationTask ->
                                binding.btnSubmit.isEnabled = true
                                if (!postCreationTask.isSuccessful) {
                                    Log.e(TAG, "Exception during Firebase operations", postCreationTask.exception)
                                    Toast.makeText(this, "Failed to save post", Toast.LENGTH_SHORT).show()
                                }
                                binding.etDescription.text.clear()
                                binding.imageViewCreate.setImageResource(0)
                                Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
                                val profileIntent = Intent(this, ProfileActivity::class.java)
                                profileIntent.putExtra(EXTRA_USERNAME, signedinUser?.username)
                                startActivity(profileIntent)
                                finish()
                            }
                }
    }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == PICK_PHOTO_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    photoUri = data?.data
                    Log.i(TAG, " photoUri $photoUri")
                    binding.imageViewCreate.setImageURI(photoUri)
                } else {
                    Toast.makeText(this, "image icking cancelled", Toast.LENGTH_SHORT)
                }
            }

        }
    }


