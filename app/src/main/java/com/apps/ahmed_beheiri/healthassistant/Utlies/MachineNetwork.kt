package com.apps.ahmed_beheiri.healthassistant.Utlies

import android.content.Context
import android.widget.Toast
import com.apps.ahmed_beheiri.healthassistant.Model.MachineData
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class MachineNetwork {
    public interface GetMachineData{
        @POST("seha")
    fun getmachinedata(@Body data:MachineData):Call<ResponseBody>
    }
    companion object {
        fun startTask(hearbeatvalue:String,temp:String,BaseURl:String,context:Context):Call<ResponseBody>?{
            if(BaseURl.equals("")){
                Toast.makeText(context,"Error connecting to machine",Toast.LENGTH_LONG).show()
                return null
            }else {
                var data: MachineData = MachineData(hearbeatvalue, temp)
                val retrofit: Retrofit = Retrofit.Builder().baseUrl(BaseURl).addConverterFactory(GsonConverterFactory.create()).build()
                return retrofit.create(GetMachineData::class.java).getmachinedata(data)
            }
        }
    }


}