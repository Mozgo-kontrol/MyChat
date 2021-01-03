package com.your.mychat.common;

import android.content.Context;
import android.net.ConnectivityManager;
/**
 * The Util class was made to help check
 * the internet connection.
 *
 *
 * */
public class Util {

    public static boolean internetConnectionAvailable(Context context){

        ConnectivityManager connectivityManager =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager !=null&&connectivityManager.getActiveNetworkInfo()!=null)
        {

                  return connectivityManager.getActiveNetworkInfo().isAvailable();
        }
        else return false;


    }
}
