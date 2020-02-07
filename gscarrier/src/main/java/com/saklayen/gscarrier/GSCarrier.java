package com.saklayen.gscarrier;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GSCarrier {
    public ResponseReceiver responseReceiver;
    public Context context;
    ProgressDialog pDialog;
    Activity activity;

    public GSCarrier(Context context){
        this.context = context;
        this.activity = (Activity) context;

    }



    public void  setResponseReceiver(ResponseReceiver responseReceiver){
        this.responseReceiver = responseReceiver;
    }

    public  void savePreference(String key, String value)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public  String getPreference(String key)
    {
        String value="";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        value = prefs.getString(key, "null");

        return value;

    }


    public final boolean isInternetOn() {
        ConnectivityManager connec =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if ((connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED) ||
                (connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING) ||
                (connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING) ||
                (connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED)) {

            return true;
        } else if ( connec.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED ||  connec.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED  ) {

            return false;
        }
        return false;
    }
    public static String getCurrentDate()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String CurrentDate = ""+dateFormat.format(date);

        return CurrentDate;

    }

    public String getCurrentTime()
    {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date();
        String CurrentDate =dateFormat.format(date);

        return CurrentDate;

    }

    public String getCurrentDateTime()
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String CurrentDate =dateFormat.format(date);

        return CurrentDate;

    }

    public String getCurrentTimeAMPM()
    {
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        Date date = new Date();
        String CurrentDate =dateFormat.format(date);

        return CurrentDate;

    }

    public void hitApi(String URL, String request_json,int Request_Code,String requestMethod){

        try {
            if (isInternetOn()) {
                new AsyncTaskc(request_json, URL, Request_Code, requestMethod).execute();
                Log.e("ISInternet","true");

            }
            else {
                responseReceiver.OnConnetivityError();
                Log.e("ISInternet","false");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /*public void hitApi(String URL,int Request_Code,String requestMethod){

        try {
            if (isInternetOn()) {
                new AsyncTaskc(URL, Request_Code,requestMethod).execute();
                Log.e("ISInternet","true");
            }
            else {
                responseReceiver.OnConnetivityError();
                Log.e("ISInternet","false");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }*/




    class AsyncTaskc extends AsyncTask<String, String, String> {
        String json;
        // ProgressDialog pDialog;
        String url;
        String RType;
        int Rc;
        //  ProgressDialog pDialog;
        String Response;
        public AsyncTaskc(String json, String URL,int RC,String RType) throws JSONException {



            this.json = json;
            this.url = URL;
            this.Rc = RC;
            this.RType = RType;
            Response="";

        }


        public AsyncTaskc(String URL,int RC,String RType) throws JSONException {

            JSONObject jsonObject =new JSONObject();


            this.json = jsonObject.toString();
            this.url = URL;
            this.Rc = RC;
            this.RType = RType;
            Response="";

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();



            pDialog = new ProgressDialog(context);

            pDialog.setTitle(" Please wait...");
            pDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);

            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {


            try {
                // String url= Url.Registration;
                Response="";
                // Log.e("Network_Call_Info","Url= "+url+" request_json = " + req json+" responce = "+Responce);
                Response =  makeServiceCall(url,json,RType);
                Log.e("Service_Call_Details","Url= "+url+" request_json = " +json+" responce = "+Response);

                // Toast.makeText(context,Responce,Toast.LENGTH_LONG).show();



                pDialog.dismiss();




            } catch (Exception e) {
                Log.e("Exception",e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    Log.e("response onPostExec",Response+" postExecution");
                    if (!TextUtils.isEmpty(Response)) {
                        pDialog.dismiss();


                        responseReceiver.OnServerResponse(Response, Rc);
                        cancel(true);
                    }else {
                        if (pDialog.isShowing())
                            pDialog.dismiss();
                    }





                }
            });

        }



    }


    public static String makeServiceCall(String url1, String MyJson,String requestMethod) {

        try {

            URL url = new URL(url1);

            byte[] postDataBytes = MyJson.getBytes("UTF-8");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(1000000);
            conn.setConnectTimeout(1000000);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setRequestMethod(requestMethod);
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);
            Log.e("staus",conn.getResponseCode()+"");
            if (conn.getResponseCode()!=200){
                Reader in = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                for (int c; (c = in.read()) >= 0;)
                    sb.append((char)c);
                return sb.toString();
            }

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;)
                sb.append((char)c);
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            Log.e("error1", e.getMessage());
            e.printStackTrace();
        } catch (ProtocolException e) {
            Log.e("error1", e.getMessage());
            e.printStackTrace();
        } catch (MalformedURLException e) {
            Log.e("error1", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("error1", e.getMessage());
            e.printStackTrace();
        }

        return "";
    }





    public interface ResponseReceiver{
        void OnServerResponse(String responseString, int RequestCode);
        void OnConnetivityError();

    }
}
