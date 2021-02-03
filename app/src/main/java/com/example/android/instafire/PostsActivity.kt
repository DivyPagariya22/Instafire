package com.example.android.instafire


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.instafire.databinding.ActivityLoginBinding
import com.example.android.instafire.databinding.ActivityPostsBinding
import com.example.android.instafire.models.Post
import com.example.android.instafire.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


private lateinit var binding : ActivityPostsBinding
private var TAG = "Posts Activity"
val EXTRA_USERNAME = "EXTRA_USERNAME"

open class PostsActivity : AppCompatActivity() {
    private  var signedinUser : User? = null
    private lateinit var db : FirebaseFirestore
    private lateinit var posts : MutableList<Post>
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.recycle.layoutManager = LinearLayoutManager(this)

        posts = mutableListOf()
        adapter = PostAdapter(this,posts)
        binding.recycle.adapter = adapter
        db = FirebaseFirestore.getInstance()

        db.collection("users").
        document(FirebaseAuth.getInstance().
        currentUser?.uid as String)
                .get()
                .addOnSuccessListener { userSnapshot->
                    signedinUser = userSnapshot.toObject(User::class.java)
            Log.i(TAG,"user signed in $signedinUser")
        }
                .addOnFailureListener { execption ->
                    Log.i(TAG,"ERROR",execption)
                }


        var postRefrence = db.collection("posts").orderBy("creation_time",Query.Direction.DESCENDING)

        val username = intent.getStringExtra(EXTRA_USERNAME)
        if( username != null){
            supportActionBar ?.title = username
          postRefrence =  postRefrence.whereEqualTo("user.username",username)
        }
        postRefrence.addSnapshotListener { snapshot, exception ->
            if(exception != null || snapshot == null){
                Log.e(TAG,"Exception when query posted",exception)
                return@addSnapshotListener
            }

            val postlist = snapshot.toObjects(Post::class.java)
            posts.clear()
            posts.addAll(postlist)
            adapter.notifyDataSetChanged()

            for(post in postlist){
                    Log.i(TAG,"Post ${post}")
                }
        }

        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(this,CreateActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.profile){
            val intent = Intent(this,ProfileActivity::class.java)
            intent.putExtra(EXTRA_USERNAME, signedinUser?.username)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}