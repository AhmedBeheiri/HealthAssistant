package com.apps.ahmed_beheiri.healthassistant.UI.Adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.apps.ahmed_beheiri.healthassistant.Model.User
import com.apps.ahmed_beheiri.healthassistant.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.card_following.view.*

/**
 * Created by ahmed_beheiri on 06/03/18.
 */
class FollowingAdapter(var followers:LinkedHashMap<String,User>,context:Context) : RecyclerView.Adapter<FollowingAdapter.MainHolder>() {
   lateinit var userData:LinkedHashMap<String,User>
    lateinit var context: Context
init {
    userData=followers
    this.context=context
}

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MainHolder {
        return MainHolder(LayoutInflater.from(parent?.context).inflate(R.layout.card_following,parent,false))

    }

    override fun getItemCount(): Int {
        return userData.size
    }

    override fun onBindViewHolder(holder:MainHolder?, position: Int) {

        var userslist:ArrayList<User> = ArrayList(userData.values)
        holder?.UpdateUI(userslist.get(position)!!,context)
    }


    class MainHolder(itemview:View):RecyclerView.ViewHolder(itemview){
        fun UpdateUI(userData: User,context: Context){
            itemView.heartratetext.setText("Heart rate : ")
            itemView.temptext.setText("temperature : ")
            itemView.followerusername.setText(userData.username)
            itemView.heartrate.setText(userData.data.heartrate.toString())
            if (userData.data.status==1){
            itemView.status.setText("Angry")
                itemView.status.setTextColor(itemView.resources.getColor(android.R.color.holo_red_dark))
            }else{
                itemView.status.setText("Happy")
                itemView.status.setTextColor(itemView.resources.getColor(android.R.color.holo_green_light))
            }
            itemView.temp.setText(userData.data.temp.toString())
            Picasso.with(context).load(userData.imageuri).placeholder(R.drawable.index).into(itemView.prof_pic)

        }
    }


}