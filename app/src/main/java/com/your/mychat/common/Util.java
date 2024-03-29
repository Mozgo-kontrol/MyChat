package com.your.mychat.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.your.mychat.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Handler;

/**
 * The Util class was made to help check
 * the internet connection.
 *
 *
 * */
public class Util {
    private static final String TAG = "Util";
    public static boolean internetConnectionAvailable(Context context){

        ConnectivityManager connectivityManager =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager !=null&&connectivityManager.getActiveNetworkInfo()!=null)
        {

                  return connectivityManager.getActiveNetworkInfo().isAvailable();
        }
        else return false;
    }

    /**updateDeviceToken
     * update the notification token for the current device
     *
     * @param context
     * @param token
     * */
    public static  void  updateDeviceToken(final Context context, String token)
    {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser =firebaseAuth.getCurrentUser();

        if(currentUser!=null) {

            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference databaseReference = rootRef.child(NodeNames.TOKENS).child(currentUser.getUid());
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(NodeNames.DEVICE_TOKEN, token);

            databaseReference.setValue(hashMap).addOnCompleteListener(task -> {
               if(!task.isSuccessful())
               {
                   Toast.makeText(context, R.string.failed_to_save_device_token, Toast.LENGTH_SHORT).show();

               }
            });
        }
    }

    public  static void sendNotification(final Context context, String title,String message, String userId, String whoSentId)
    {
       DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();
       DatabaseReference databaseReference = rootRef.child(NodeNames.TOKENS).child(userId);
       databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               if(snapshot.child(NodeNames.DEVICE_TOKEN).getValue()!=null) {

                   String deviceToken = snapshot.child(NodeNames.DEVICE_TOKEN).getValue().toString();

                   JSONObject notification = new JSONObject();
                   JSONObject notificationData = new JSONObject();

                   try {
                       notificationData.put(Constants.NOTIFICATION_TITLE, title);
                       notificationData.put(Constants.NOTIFICATION_MESSAGE, message);

                       notificationData.put(Constants.NOTIFICATION_WHO_SENT, whoSentId);

                       notification.put(Constants.NOTIFICATION_TO, deviceToken);
                       notification.put(Constants.NOTIFICATION_DATA, notificationData);
                       String fcmApiUrl = "https://fcm.googleapis.com/fcm/send";
                       String contentType = "application/json";


                       Response.Listener successListener = response -> {
                           Toast.makeText(context, R.string.notification_sent, Toast.LENGTH_SHORT).show();
                           Log.d(TAG, "Notification title" + title+"; Notification Data"+message);
                       };
                       Response.ErrorListener failureListener = error -> {
                           Toast.makeText(context, context.getString(R.string.failed_to_sent_notification, error.getMessage()), Toast.LENGTH_SHORT).show();
                       };

                       JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(fcmApiUrl, notification, successListener, failureListener) {
                           //protect request, authorisation request
                           @Override
                           public Map<String, String> getHeaders() throws AuthFailureError {
                               Map<String, String> params = new HashMap<>();
                               params.put("Authorization", "key=" + Constants.FIREBASE_KEY);
                               params.put("Sender", "id=" + Constants.SENDER_ID);
                               params.put("Content-Type", contentType);
                               return params;
                           }
                       };

                       RequestQueue requestQueue = Volley.newRequestQueue(context);
                       requestQueue.add(jsonObjectRequest);


                   }
                   catch (JSONException e) {
                       Toast.makeText(context, context.getString(R.string.failed_to_sent_notification, e.getMessage()), Toast.LENGTH_SHORT).show();
                       e.printStackTrace();
                   }
               }
               }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {
               Toast.makeText(context, context.getString(R.string.failed_to_sent_notification,error.getMessage()), Toast.LENGTH_SHORT).show();
           }
       });

    }

    public static  void updateChatDetails(Context context, String currentUserId, String chatUserId, String lastMessage)
    {
      DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
      DatabaseReference chatRef = rootRef.child(NodeNames.CHATS).child(chatUserId).child(currentUserId);

      chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
              String currentCount="0";
              if(snapshot.child(NodeNames.UNREAD_COUNT).getValue()!=null){
                  currentCount = Objects.requireNonNull(snapshot.child(NodeNames.UNREAD_COUNT).getValue()).toString();
              }
              Map<String, Object> chatMap =new HashMap();
              chatMap.put(NodeNames.TIME_STAMP, ServerValue.TIMESTAMP);
              chatMap.put(NodeNames.UNREAD_COUNT,Integer.valueOf(currentCount)+1);
              chatMap.put(NodeNames.LAST_MESSAGE, lastMessage);
              chatMap.put(NodeNames.LAST_MESSAGE_TIME, ServerValue.TIMESTAMP);


               //------------chechen---------------------------------------------------------------
              //TODO chechen its cann be Failur
              HashMap chatUserMap = new HashMap();

              chatUserMap.put(NodeNames.CHATS+"/"+chatUserId+"/"+currentUserId, chatMap);
              //---------------------------------------------------------------------------
              rootRef.updateChildren(chatUserMap, (error, ref) -> {
                  if(error!=null){
                      Toast.makeText(context, context.getString(R.string.something_went_wrong,error.getMessage()), Toast.LENGTH_SHORT).show();
                  }
              });
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {
              Toast.makeText(context, context.getString(R.string.something_went_wrong,error.getMessage()), Toast.LENGTH_SHORT).show();
          }
      });

    }

    public static String getTimeAgo(long time)
    {
        final int SECOND_MILLS = 1000;
        final int MINUTE_MILLIS = 60 * SECOND_MILLS;
        final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        final int DAY_MILLIS = 24 * HOUR_MILLIS;
       // time *= 1000;

        long now = System.currentTimeMillis();
        if(time <= 0)
        {

          return "";
        }

        final long diff = now - time;
        Log.i(TAG, "Diff :"+ diff);

        if(diff < MINUTE_MILLIS)
        {
            return "just now";
        }
        else if(diff < 2 * MINUTE_MILLIS)
        {
            return "one minute ago";
        }
        else if(diff < 59*MINUTE_MILLIS)
        {
            return diff/MINUTE_MILLIS+ " minutes ago";
        }
        else if (diff > 90 * MINUTE_MILLIS)
        {
           return "an hour ago";
        }
        else if (diff < 24 * HOUR_MILLIS)
        {
            return diff/HOUR_MILLIS +  " hours ago";
        }
        else if (diff < 48 * HOUR_MILLIS)
        {
            return "yesterday";
        }
        else {

            return diff/DAY_MILLIS+ " days ago";
        }
    }


}
