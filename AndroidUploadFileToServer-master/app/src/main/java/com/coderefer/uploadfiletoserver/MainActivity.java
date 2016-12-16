package com.coderefer.uploadfiletoserver;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class MainActivity extends AppCompatActivity {

    public String currentIp = "147.8.203.213";

    //Post URLS

    //private String SERVER_URL = "http://"+currentIp+"/handle_upload.php";
    //private String SERVER_URL = "http://"+currentIp+":8000/uusapp/fileupload/";
    private String SERVER_URL = "http://"+ currentIp +":8000/uusapp/addimage/";
    //private String SERVER_URL = "http://requestb.in/16aqpcu1";


    // Request codes

    private String token="Token b85bd5705373081b09bcc59a20d97052cbc566fa";
    private static final int ACTION_TAKE_PHOTO_B = 1;
    private static int PICTURE_CAPTURE_PERMISSION = 222;
    private static final String TAG = MainActivity.class.getSimpleName();


    // Storage related

    private Uri mCurrentImageUri;
    private String mCurrentPhotoPath;
    private String mCurrentPhotoName;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;


    // GUI items

    ImageView iCaptured;
    Button bUpload;

    TextView tUploadStatus;
    TextView tvFileName;
    Button bCaptureImg;


    Button.OnClickListener mTakePicOnClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
                }
            };

    Button.OnClickListener mUploadPicOnClickListener =
            new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadImg();
                }
            };

    protected void onCreate(Bundle savedInstanceState) {
        // Set Up

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_image);
        iCaptured = (ImageView) findViewById(R.id.captured_image);
        bUpload = (Button) findViewById(R.id.upload_the_image);
        bCaptureImg = (Button) findViewById(R.id.do_image_capture);
        tvFileName = (TextView) findViewById(R.id.current_img_file_name);
        tUploadStatus = (TextView) findViewById(R.id.upload_status);
        bCaptureImg.setOnClickListener(mTakePicOnClickListener);
        bUpload.setOnClickListener(mUploadPicOnClickListener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }

        Intent intent = getIntent();
        this.token = intent.getStringExtra(LoginActivity.EXTRA_MESSAGE);
        // Start procedures
        askPermissions();
        //checkStorageDir();
        setUpTest();
    }

    public void checkStorageDir() {
        File mediaStorageDir = getAlbumDir();
        File[] temp = mediaStorageDir.listFiles();
        try {
            tUploadStatus.setText(temp[0].getPath());
        } catch (Exception ex) {
            tUploadStatus.setText("ikka null");
        }

        //tUploadStatus.setText(mediaStorageDir.getPath());
        //tvFileName.setText(mediaStorageDir.getAbsolutePath());
        //Picasso.with(getApplicationContext()).load().into(iCaptured);
    }


    /* Photo album for this application */
    private String getAlbumName() {
        return getString(R.string.album_name);
    }

    // Main Organizing method for getting shared Image Directory for this app
    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);

        return imageF;
    }


    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();
        mCurrentPhotoName = f.getName();
        mCurrentImageUri = Uri.fromFile(f);
        return f;
    }


    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        switch (actionCode) {
            case ACTION_TAKE_PHOTO_B:
                File f = null;

                try {
                    f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    mCurrentPhotoPath = null;
                }
                break;

            default:
                break;
        } // switch

        startActivityForResult(takePictureIntent, actionCode);
    }


    private void setPic() {
        File path = getAlbumDir();
        File f = new File(path, mCurrentPhotoName);
        Picasso.with(getApplicationContext()).load(f).into(iCaptured);
    }


    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void handleBigCameraPhoto() {

        if (mCurrentPhotoPath != null) {
            setPic();
            galleryAddPic();
            checkStorageDir();
            mCurrentPhotoPath = null;
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ACTION_TAKE_PHOTO_B == requestCode) {
            if (resultCode == RESULT_OK) {
                handleBigCameraPhoto();
            }
        }
    }


    private void setUpTest() {
        File albumDir = getAlbumDir();
        File[] albumList = albumDir.listFiles();
        File testFile = albumList[0];
        mCurrentImageUri = Uri.fromFile(testFile);
        mCurrentPhotoPath = testFile.getAbsolutePath();
        mCurrentPhotoName = testFile.getName();
        tvFileName.setText(mCurrentPhotoPath);
        Picasso.with(getApplicationContext()).load(testFile).into(iCaptured);
    }


    private void uploadImg() {
        final String selectedFilePath = FilePath.getPath(this, mCurrentImageUri);
        tvFileName.setText("Upload started");


        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new UploadImageTask().execute(selectedFilePath);
        } else {
            tUploadStatus.setText("No network connection available.");
        }

    }

    private class UploadImageTask extends AsyncTask<String, Void, String> {
        private final String U_TAG = UploadImageTask.class.getSimpleName();


        @Override
        protected String doInBackground(String... paths) {
            try {
                int resp = uploadFile(paths[0]);
                return "Server response " + resp;
            } catch (Exception e) {
                return "Unable to upload image";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            tUploadStatus.setText( result);

        }

        public int uploadFile(final String selectedFilePath) {


            int serverResponseCode = 0;

            HttpURLConnection connection;
            DataOutputStream dataOutputStream;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "921b1508a0b342f5bb06dfa40ae1f55d";


            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File selectedFile = new File(selectedFilePath);


            String[] parts = selectedFilePath.split("/");
            final String fileName = parts[parts.length - 1];

            if (!selectedFile.isFile()) {

                Log.e(U_TAG,"Source File Doesn't Exist: " + selectedFilePath);
                return 0;
            } else {
                try {
                    FileInputStream fileInputStream = new FileInputStream(selectedFile);
                    URL url = new URL(SERVER_URL);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);//Allow Inputs
                    connection.setDoOutput(true);//Allow Outputs
                    connection.setUseCaches(false);//Don't use a cached Copy
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    connection.setRequestProperty("uploaded_file", selectedFilePath);
                    connection.setRequestProperty("Authorization",token);
                    //creating new dataoutputstream
                    dataOutputStream = new DataOutputStream(connection.getOutputStream());
                    String dispName= "image";
                    //String dispName= "uploaded_file";
                    //writing bytes to data outputstream
                    dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\""+ dispName +"\";filename=\""
                            + fileName + "\"" + lineEnd);
                    dataOutputStream.writeBytes("Content-Type: image/jpeg" + lineEnd);

                    dataOutputStream.writeBytes(lineEnd);

                    //returns no. of bytes present in fileInputStream
                    bytesAvailable = fileInputStream.available();
                    //selecting the buffer size as minimum of available bytes or 1 MB
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    //setting the buffer as byte array of size of bufferSize
                    buffer = new byte[bufferSize];

                    //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);


                    //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                    while (bytesRead > 0) {

                        try {

                            //write the bytes read from inputstream
                            dataOutputStream.write(buffer, 0, bufferSize);
                        } catch (OutOfMemoryError e) {
                            Toast.makeText(MainActivity.this, "Insufficient Memory!", Toast.LENGTH_SHORT).show();
                        }
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    try {
                        serverResponseCode = connection.getResponseCode();
                    } catch (OutOfMemoryError e) {
                        Toast.makeText(MainActivity.this, "Memory Insufficient!", Toast.LENGTH_SHORT).show();
                    }
                    String serverResponseMessage = connection.getResponseMessage();

                    Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                    //response code of 200 indicates the server status OK
                    if (serverResponseCode == 200) {
                        Log.e(U_TAG,"File Upload completed.\n\n " + fileName);
                    }

                    //closing the input and output streams
                    fileInputStream.close();
                    dataOutputStream.flush();
                    dataOutputStream.close();


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.e(U_TAG,"File Not Found");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Log.e(U_TAG,"URL Error!");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(U_TAG,"Cannot Read/Write File");
                }
                //dialog.dismiss();
                return serverResponseCode;
            }

        }
    }



    private void askPermissions() {
        ArrayList<String> permissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.INTERNET);
        }

        if (permissions.size() > 0) {
            String[] permiss = permissions.toArray(new String[0]);

            ActivityCompat.requestPermissions(MainActivity.this, permiss,
                    PICTURE_CAPTURE_PERMISSION);
        } else {
            //StartVideoCapture();
        }
    }
}
