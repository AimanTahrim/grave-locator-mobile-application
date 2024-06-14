package com.example.glmaclient.persistentcloudanchor

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.glmaclient.persistentcloudanchor.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ManageDeceasedClient : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userArrayList: ArrayList<Model>
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mAdapter: MyAdapter

    private lateinit var mSearchText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_deceased_client)

        //back button
        val backArrow: ImageView = findViewById(R.id.backarrow)
        backArrow.setOnClickListener{
            val intent = Intent(this, HomePageClient::class.java)
            startActivity(intent)
        }

        mSearchText = findViewById(R.id.inputSearch)
        firebaseAuth = FirebaseAuth.getInstance()
        userRecyclerView = findViewById(R.id.recyclerView)
        userRecyclerView.layoutManager = LinearLayoutManager(this)
        userRecyclerView.setHasFixedSize(true)

        userArrayList = arrayListOf()
        mAdapter = MyAdapter(userArrayList, this)
        userRecyclerView.adapter = mAdapter

        getUserData()

        //Search Deceased Data
        mSearchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString().trim()
                Log.d("ManageDeceasedClient", "Search text: $searchText")
                if (searchText.isEmpty()) {
                    mAdapter.searchDataList(userArrayList)
                } else {
                    searchInDatabase(searchText)
                }
            }
        })

    }

    //Fetch data in grave child realtime database
    private fun getUserData() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Deceased")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userArrayList.clear()
                for (dataSnapshot in snapshot.children) {
                    try {
                        val model = dataSnapshot.getValue(Model::class.java)
                        if (model != null) {
                            userArrayList.add(model)
                        }
                    } catch (e: DatabaseException) {
                        Log.e("ManageDeceasedClient", "Error converting data snapshot to Model: ${e.message}")
                    }
                }
                Log.d("ManageDeceasedClient", "Approved data fetched: ${userArrayList.size} items")
                mAdapter.searchDataList(userArrayList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ManageDeceasedClient, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    //function for Search Data in Realtime Database
    private fun searchInDatabase(searchText: String) {
        val firebaseSearchQuery = databaseReference.orderByChild("deceasedName").startAt(searchText).endAt(searchText + "\uf8ff")

        firebaseSearchQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val searchList: ArrayList<Model> = ArrayList()
                for (dataSnapshot in snapshot.children) {
                    val model = dataSnapshot.getValue(Model::class.java)
                    if (model != null) {
                        searchList.add(model)
                    }
                }
                Log.d("ManageDeceasedClient", "Search result: ${searchList.size} items")
                mAdapter.searchDataList(searchList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ManageDeceasedClient", "Error: ${error.message}")
                Toast.makeText(this@ManageDeceasedClient, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun onClick(v: View) {
        val i: Intent = when (v.id) {
            R.id.addDeceasedCard -> Intent(this, AddDataClient::class.java)
            else -> return
        }
        startActivity(i)
    }
}
