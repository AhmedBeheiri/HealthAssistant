package com.apps.ahmed_beheiri.healthassistant.UI

import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.apps.ahmed_beheiri.healthassistant.Model.User
import com.apps.ahmed_beheiri.healthassistant.R
import com.apps.ahmed_beheiri.healthassistant.UI.Adapter.FollowingAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_following.*
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import com.apps.ahmed_beheiri.healthassistant.Model.UserData


class FollowingActivity : AppCompatActivity() {
    lateinit var user:FirebaseUser
    lateinit var mAuth:FirebaseAuth
    lateinit var database: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var storage:FirebaseStorage
    lateinit var storageReference: StorageReference

    lateinit var following:LinkedHashMap<String,User>
    lateinit var followers:LinkedHashMap<String,String>
    lateinit var adapter:FollowingAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_following)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title="Following"
        mAuth=FirebaseAuth.getInstance()
        user=mAuth.currentUser!!
        database= FirebaseDatabase.getInstance()
        databaseReference=database.getReference("users").child(user?.uid)
        storage= FirebaseStorage.getInstance()
        storageReference=storage.getReference("users")
        following= LinkedHashMap()
        followers= LinkedHashMap()

        recyclerView.setHasFixedSize(true)
        var layoutManager:LinearLayoutManager= LinearLayoutManager(this)
        recyclerView.layoutManager=layoutManager
        recyclerView.addItemDecoration(VerticalSpaceItemDecorator(10))
         adapter= FollowingAdapter(following,this)
        recyclerView.adapter=adapter
            databaseReference.addValueEventListener(object:ValueEventListener{
                override fun onDataChange(value: DataSnapshot?) {
                    var followerssnapshot:DataSnapshot= value!!.child("following")
                    if(followerssnapshot.exists()){
                        recyclerView.visibility=View.VISIBLE
                        var followerschildren:Iterable<DataSnapshot> =followerssnapshot.children
                        for(follower:DataSnapshot in followerschildren){
                            var ref:DatabaseReference=database.getReference("users").child(follower.key).child("data")
                            ref.addListenerForSingleValueEvent(object :ValueEventListener{
                                override fun onDataChange(p0: DataSnapshot?) {
                                    databaseReference.child("following").child(follower.key).child("data").setValue(p0?.getValue(UserData::class.java))
                                }

                                override fun onCancelled(p0: DatabaseError?) {

                                }
                            })
                            var user:User=follower.getValue(User::class.java)!!
                            following.put(follower.key,user)
                            adapter.notifyDataSetChanged()
                        }
                        errortext.visibility=View.GONE

                    }else{
                        recyclerView.visibility=View.GONE
                        errortext.visibility=View.VISIBLE
                    }

                }

                override fun onCancelled(p0: DatabaseError?) {

                }
            })

        addfolowersbtn.setOnClickListener {
            addfollowers()
        }

    }



    fun addfollowers(){
        var m_Text:String=""
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter the User Code :")
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        builder.setView(input)

        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
            m_Text = input.text.toString()
            searchforuser(m_Text)
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

        builder.show()
    }



    fun searchforuser(code:String){
        var userref=database.getReference("users")
        userref.orderByChild("code").equalTo(code)


       userref.addListenerForSingleValueEvent(object :ValueEventListener {
           lateinit var followeruser: User
           override fun onDataChange(value: DataSnapshot?) {
               Log.d("what is coming", value.toString())

               for (datasnapShot: DataSnapshot in value?.children!!) {
                   var key: String = datasnapShot.key.toString()
                   followeruser = datasnapShot.getValue(User::class.java)!!
                   if (followeruser.code == code.toInt()) {
                       following.put(key, followeruser)
                       followers.put(user?.uid,user.email.toString())
                       database.getReference("users").child(key).child("followers").setValue(followers)
                       adapter.notifyDataSetChanged()
                   }
               }


               databaseReference.child("following").setValue(following)
               Log.d("followersadded","succecful")
               adapter.notifyDataSetChanged()
           }

           override fun onCancelled(error: DatabaseError?) {
               Log.d("gettingfollowerData",error?.message)
           }
       })

    }


    class VerticalSpaceItemDecorator(spacer: Int) : RecyclerView.ItemDecoration() {

        private val spacer: Int

        init {
            this.spacer = spacer
        }

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
            super.getItemOffsets(outRect, view, parent, state)

            outRect.bottom = spacer
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater:MenuInflater=menuInflater
        inflater.inflate(R.menu.followingmenu,menu)
        return true
    }

     override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.getcode->{


                val builder = AlertDialog.Builder(this)
                builder.setTitle("Your code is :")
                val input = TextView(this)
                input.setTextColor(resources.getColor(android.R.color.black))
                input.setTextSize(18f)
                input.gravity=Gravity.CENTER
                databaseReference.child("code").addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {
                        Log.d("Database Error",p0?.message)
                    }

                    override fun onDataChange(value: DataSnapshot?) {
                        input.setText(value?.getValue(Long::class.java).toString())
                    }
                })

                builder.setView(input)

                builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })

                builder.show()

            }
            android.R.id.home ->{
                onBackPressed()
            }
        }
         return true
    }



}
