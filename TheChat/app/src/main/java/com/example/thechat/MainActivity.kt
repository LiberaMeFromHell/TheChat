package com.example.thechat

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    val ANONYMOUS = "anonymous"
    val DEFAULT_MSG_LENGTH_LIMIT = 1000


    var listView: ListView? = null
    var messageAdapter: MessageAdapter? = null
    var progressBar: ProgressBar? = null
    var imageButton: ImageButton? = null
    var editText: EditText? = null
    var sendButton: ImageButton? = null
    var firebaseAuth: FirebaseAuth? = null
    var authStateListener: FirebaseAuth.AuthStateListener? = null
    var messagesDatabase: DatabaseReference? = null
    var firebaseUserName: String? = null
    var childEventListener: ChildEventListener? = null
    var firebaseStorage: FirebaseStorage? = null
    var storageReference: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listView)
        progressBar = findViewById(R.id.progressBar)
        imageButton = findViewById(R.id.imageButton)
        editText = findViewById(R.id.editText)
        sendButton = findViewById(R.id.sendButton)

        //Init Firebase Storage
        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage!!.getReference().child("chat_photos")

        //Init Firebase Authentication components
        firebaseAuth = FirebaseAuth.getInstance()
        authStateListener = object : FirebaseAuth.AuthStateListener {

            override fun onAuthStateChanged(p0: FirebaseAuth) {

                var firebaseUser = firebaseAuth?.currentUser
                if (firebaseUser != null) {
                    // the user is signed in
                    Toast.makeText(this@MainActivity, "Authentication's successful", Toast.LENGTH_SHORT).show()

                    onSignedInInitialize(firebaseUser.displayName)

                } else {
                    //the user is signed out

                    onSignedOutCleanup()

                    startActivityForResult(
                        AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(
                                Arrays.asList(
                                    AuthUI.IdpConfig.GoogleBuilder().build(),
                                    AuthUI.IdpConfig.EmailBuilder().build()
                                )

                            ).build(), 1
                    )
                }
            }
        }
        //Init listView
        val messages: ArrayList<Message> = ArrayList()
        messageAdapter = MessageAdapter(this, R.layout.message_item, messages)
        listView?.adapter = messageAdapter

        //Hide progress bar
        progressBar?.visibility = ProgressBar.INVISIBLE

        //Show library to upload an image
        imageButton?.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT);
            intent.type = "image/jpeg"
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), 2)
        })

        //TODO("?????")
        editText?.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var s: CharSequence? = s
                sendButton?.isEnabled = s.toString().isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        //init firebase realtime database
        messagesDatabase = FirebaseDatabase.getInstance().reference.child("messages")

        //Send message and clear editText
        sendButton?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                messagesDatabase!!.push().setValue(Message(editText?.text.toString(), firebaseUserName!!, null))
                editText?.setText("")
            }
        })
    }

    private fun onSignedInInitialize(displayName: String?) {
        firebaseUserName = displayName
        attachDatabaseReadListener()
    }

    private fun onSignedOutCleanup() {
        firebaseUserName = ANONYMOUS
        messageAdapter?.clear()
        detachDatabaseReadListener()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {

            R.id.logoff -> AuthUI.getInstance().signOut(this)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        firebaseAuth?.addAuthStateListener(authStateListener!!)
    }

    override fun onPause() {
        super.onPause()
        firebaseAuth?.removeAuthStateListener(authStateListener!!)
        detachDatabaseReadListener()
        messageAdapter?.clear()
    }

    private fun attachDatabaseReadListener() {

        childEventListener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java)
                messageAdapter?.add(message)
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        }
        messagesDatabase?.addChildEventListener(childEventListener!!)
    }

    private fun detachDatabaseReadListener() {
        if (messagesDatabase != null && childEventListener != null) {
            messagesDatabase?.removeEventListener(childEventListener!!)
            messagesDatabase = null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this@MainActivity, "Cancelled", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            var selectedImageURL = data?.data
            var photoReference = storageReference?.child(selectedImageURL!!.lastPathSegment)

            photoReference!!.putFile(selectedImageURL!!).addOnSuccessListener(this, object : OnSuccessListener<UploadTask.TaskSnapshot> {
                    override fun onSuccess(taskSnapshot: UploadTask.TaskSnapshot?) {
                        var downloadUrl = taskSnapshot?.uploadSessionUri
                        var message = Message("", firebaseUserName!!, downloadUrl!!.toString())
                    }
                })
        }
    }
}
