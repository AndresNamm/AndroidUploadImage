package com.coderefer.uploadfiletoserver;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GalleryActivity extends BaseActivity implements AsyncResponse{



    public static String G_TAG = GalleryActivity.class.getSimpleName();
    private String token="Token b85bd5705373081b09bcc59a20d97052cbc566fa";

    // Connection related stuff
    private static final String AUTH_TOKEN_URL = "http://"+ currentIp +":8000/uusapp/userimages/";
    private static final String SUCCESS_MESSAGE =  "Successful result";
    private static final String FAILURES_MESSAGE =  "Something went wrong";
    public final static String EXTRA_MESSAGE = "com.coderefer.galleryactivity";
    private GetUserImgsTask mUserImgsTask = null;

    // Gui STUFF

    private TextView tvResponsetxt;
    private GridView gridView;
    private GridViewAdapter gridAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);


        gridView = (GridView) findViewById(R.id.gridView);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        tvResponsetxt = (TextView) findViewById(R.id.test_text);

        Intent intent = getIntent();
        String temp = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        if(temp!=null){
            this.token = temp;

        }

        getUserImages();


    }


    private void getUserImages(){
         // If user is not very patient
        if (mUserImgsTask != null) {
            return;
        }
         mUserImgsTask = new GetUserImgsTask(token, this);
         mUserImgsTask.execute((Void) null);

    }




    @Override
    public void processFinish(String response) {
        try{
            gridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, JSONfunctions.parseUserImages(response));
            gridView.setAdapter(gridAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    ImageItem item = (ImageItem) parent.getItemAtPosition(position);

                    //Create intent
                    Intent intent = new Intent(GalleryActivity.this, DetailsActivity.class);
                    intent.putExtra("title", item.getTitle());
                    intent.putExtra("image", item.getImage());

                    //Start details activity
                    startActivity(intent);
                }
            });
        }catch(Exception e){
            e.printStackTrace();
            tvResponsetxt.setText("No pictures to display/Some download problems");
        }


    }



    public class GetUserImgsTask extends AsyncTask<Void, Void, String> {

        private String token;
        private Boolean success = false;
        public AsyncResponse delegate = null;
        private final int BUFFER_SIZE = 500;


        GetUserImgsTask(String token, AsyncResponse delegate) {
            this.token = token;
            this.delegate = delegate;
        }


        @Override
        protected String doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                return getUsrImages();

            } catch (Exception e) {
                return "Caught some freaking exception";
            }
        }

        protected String getUsrImages() {
            String contentAsString = "not initialized";
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;
            try {
                URL url = new URL(AUTH_TOKEN_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                Log.d(G_TAG, "url.openConnection");
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                //conn.setDoOutput(true);
                Log.d(G_TAG, "Set up data unrelated headers");
                //header crap
                //conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                conn.setRequestProperty("Authorization",this.token);
                //connect
                conn.connect();
                Log.d(G_TAG, "Connection is established");
                // do something with response

                // Convert the InputStream into a string
                is = conn.getInputStream();
                contentAsString=readIt(is);
                //String contentAsString = readIt(is,500);
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
                if (is != null) {
                    is.close();
                }

                Log.d(G_TAG, conn.getResponseMessage() + " " + conn.getResponseCode());
                return contentAsString;

            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e(G_TAG,"Exeption");
                return "";
            }//TODO: IMPLEMENT CORRECT EXCEPTION HANDLING FOR EASIER DEBUGGING


        }


        public String readIt(InputStream  stream) throws IOException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            BufferedReader r = new BufferedReader(reader);
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            return total.toString();
        }

        public String readIt(InputStream  stream, int len) throws IOException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            return new String(buffer);
        }


        @Override
        protected void onPostExecute( String response )
        {
            mUserImgsTask = null;
            delegate.processFinish(response);

        }

        @Override
        protected void onCancelled() {
            mUserImgsTask = null;

        }
    }










}
