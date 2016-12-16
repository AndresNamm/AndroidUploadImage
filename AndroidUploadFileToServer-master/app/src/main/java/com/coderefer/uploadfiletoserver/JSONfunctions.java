package com.coderefer.uploadfiletoserver;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;



public class JSONfunctions {
    private static final String ERROR_TAG = "E__JSONGfunctions";
    private static final String DEBUG_TAG = "D__JSONGfunctions";

    public static JSONObject getLoginObject(String username, String password){
        try{
            JSONObject temp = new JSONObject();
            temp.put("username",username);
            temp.put("password",password);
            Log.d(DEBUG_TAG, "json login object created");
            return temp;
        }catch (JSONException ex){
            Log.e(ERROR_TAG, "Something went wrong with JSON Username Password creation");
        }
        return null;
    }



    public static String parseAuthToken( String response){
        try{
            JSONObject  temp = new JSONObject(response );
            String token = "Token " + temp.getString("token");
            return token;
        }catch (JSONException ex){
            ex.printStackTrace();
            return "";
        }
    }


}
