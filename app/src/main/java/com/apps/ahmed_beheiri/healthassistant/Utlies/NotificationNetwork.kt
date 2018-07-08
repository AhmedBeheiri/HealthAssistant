package com.apps.ahmed_beheiri.healthassistant.Utlies

import android.location.Location
import android.os.AsyncTask
import android.os.StrictMode
import com.google.firebase.auth.FirebaseUser
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.util.*

class NotificationNetwork {
    companion object {


         fun sendNotification(emails: ArrayList<String>, location: String) {
            AsyncTask.execute(object : Runnable {
                override fun run() {
                    val SDK_INT = android.os.Build.VERSION.SDK_INT
                    if (SDK_INT > 8) {
                        var policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder()
                                .permitAll().build()
                        StrictMode.setThreadPolicy(policy)
                        var send_email: String = ""

                        //This is a Simple Logic to Send Notification different Device Programmatically....
                        for (email in emails) {
                            send_email = email


                            try {
                                var jsonResponse: String

                                var url = URL("https://onesignal.com/api/v1/notifications");
                                val con: HttpURLConnection = url.openConnection() as HttpURLConnection
                                con.setUseCaches(false);
                                con.setDoOutput(true);
                                con.setDoInput(true);

                                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                                con.setRequestProperty("Authorization", "Basic ZjhhNzI4YWItYzNhZC00ZjEyLTlhODAtNDc4MDUzZjgwODBi")
                                con.setRequestMethod("POST");

                                val strJsonBody: String = "{" +
                                        "\"app_id\": \"ad5ccda3-4b48-44aa-857c-01c4cde7e93d\"," +
                                        "\"filters\": [{\"field\": \"tag\", \"key\": \"User_Id\", \"relation\": \"=\", \"value\": \"" +
                                        send_email + "\"}]," + "\"data\": {\"foo\": \"bar\"}," +
                                        "\"contents\": {\"en\": \"This person you follow needs help his location is "+location+"\"}" +
                                        "}"


                                System.out.println("strJsonBody:\n" + strJsonBody)


                                var sendBytes: ByteArray = strJsonBody.toByteArray(Charsets.UTF_8)
                                con.setFixedLengthStreamingMode(sendBytes.size)

                                var outputStream: OutputStream = con.getOutputStream()
                                outputStream.write(sendBytes)

                                var httpResponse: Int = con.getResponseCode();
                                System.out.println("httpResponse: " + httpResponse)

                                if (httpResponse >= HttpURLConnection.HTTP_OK
                                        && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST) {
                                    var scanner: Scanner = Scanner(con.getInputStream(), "UTF-8");
                                    if (scanner.useDelimiter("\\A").hasNext()) {
                                        jsonResponse = scanner.next()
                                    } else {
                                        jsonResponse = ""
                                    }
                                    scanner.close()
                                } else {
                                    var scanner: Scanner = Scanner(con.getErrorStream(), "UTF-8")
                                    if (scanner.useDelimiter("\\A").hasNext()) {
                                        jsonResponse = scanner.next()
                                    } else {
                                        jsonResponse = ""
                                    }
                                    scanner.close()
                                }
                                System.out.println("jsonResponse:\n" + jsonResponse)

                            } catch (t: Throwable) {
                                t.printStackTrace()
                            }
                        }
                    }
                }
            })
        }
    }
}
