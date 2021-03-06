package edu.newpaltz.photomaps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainMenu extends Activity {

    ImageView mImageView;
    Activity myActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Button takePhoto = (Button)findViewById(R.id.take_photo);
        Button viewPhotos = (Button)findViewById(R.id.view_photos);
        Button uploadPhotos = (Button)findViewById(R.id.upload_photos);
        mImageView = (ImageView)findViewById(R.id.thumbnail);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // start camera activity
                Intent cI = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                if (cI.resolveActivity(getPackageManager()) != null) {
                    // create new file
                    File image = null;
                    try {
                        image = createImageFile();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                    // call camera activity
                    if (image != null) {
                        cI.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                        startActivityForResult(cI, MyApplication.REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });
        viewPhotos.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(myActivity, ViewPhotos.class);
                startActivity(i);
            }
        });
        uploadPhotos.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // check internet connection
                if (isOnline()) {
                    // get upload count
                    int count = getUploadCount();
                    if (count == 0) {
                        Toast.makeText(getApplicationContext(), "No images to upload...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Now uploading " + count + " images...", Toast.LENGTH_SHORT).show();
                        // upload photos
                        new UploadPhotos().execute();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "No internet connection available...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Method called when returning from camera activity.
     * Returns to calling activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check for image result
        if(resultCode == RESULT_OK){
            Intent mI = new Intent(this.getApplication(), SavePhoto.class);
            mI.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // create thumbnail and pass to intent
            Bitmap thumb = MyApplication.getThumbnailBitmap(MyApplication.mPhotoPath, 300);
            mI.putExtra("thumb", thumb);
            // image saved successfully; return to previous screen
            startActivity(mI);
        } else {
            Intent i = new Intent(this.getApplication(), MainMenu.class);
            startActivity(i);
        }
    }

    // creates image file on disk
    private File createImageFile() throws IOException {
        // generate file name
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        MyApplication.mJpg = "scaled_" + timeStamp + ".jpg";
        MyApplication.mPhotoPath = MyApplication.mImagePath + MyApplication.mJpg;
        return new File(MyApplication.mPhotoPath); // storage directory
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    /**
     * Background task to post a new survey instance to online database.
     */
    private class UploadPhotos extends AsyncTask<String,String,String> {

        /**
         * This method performs a POST http operation.
         * It will post a new survey instance to the online database.
         *
         * @param     args    dummy variable, not used
         * @return            dummy return value
         */
        protected String doInBackground(String... args) {
            int fail = 0;
            for (Photo photo: MyApplication.mListPhotos) {
                // check if photo is already uploaded
                if (photo.getUpload() == null) {
                    // scale image
                    File image = new File(MyApplication.mImagePath, photo.getPhoto());
                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
                    Bitmap scaled = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth()*0.4), (int)(bitmap.getHeight()*0.4), true);
                    // store scaled image
                    FileOutputStream out = null;
                    String path = MyApplication.mImagePath+photo.getPhoto();
                    try {
                        out = new FileOutputStream(path);
                        scaled.compress(Bitmap.CompressFormat.JPEG, 85, out);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    // upload scaled image
                    int response = uploadFile(path);
                    if (response == 200) {
                        // add photo object to sql database
                        ArrayList<BasicNameValuePair> photos = new ArrayList<>();
                        photos.add(new BasicNameValuePair("id", photo.getId()));
                        photos.add(new BasicNameValuePair("lat", photo.getLat()));
                        photos.add(new BasicNameValuePair("lng", photo.getLng()));
                        photos.add(new BasicNameValuePair("comment", photo.getComment()));
                        photos.add(new BasicNameValuePair("photo", photo.getPhoto()));
                        photos.add(new BasicNameValuePair("date", photo.getDate()));
                        photos.add(new BasicNameValuePair("location", photo.getLocation()));
                        photos.add(new BasicNameValuePair("upload", photo.getUpload()));
                        // post to http
                        DefaultHttpClient httpClient = new DefaultHttpClient();
                        HttpPost httpPost = new HttpPost(MyApplication.postPhotoUrl);
                        try {
                            httpPost.setEntity(new UrlEncodedFormEntity(photos));
                            httpClient.execute(httpPost);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // update photo entry in sqlite database
                        try {
                            DBAdapter db = new DBAdapter(myActivity);
                            db.updateUpload(photo.getId());
                            db.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        fail++;
                    }
                    if (fail > 0) {
                        final String FAIL = String.valueOf(fail);
                        myActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(myActivity.getBaseContext(), ("Failed to upload " + FAIL + " image files..."), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        myActivity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(myActivity.getBaseContext(), "All image files successfully uploaded!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
            // reload photo list
            MyApplication.getPhotos();
            return null;
        }

        /**
         * Method to upload an image attachment to the server.
         *
         * @param     sourceFileUri   the source file path
         * @return                    the http response code
         */
        public int uploadFile(String sourceFileUri) {
            HttpURLConnection conn;
            DataOutputStream dos;
            String lineEnd = "\r\n";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(sourceFileUri);
            int serverResponseCode = 0;
            if (!sourceFile.isFile()) {
                return 0;
            } else {
                try {
                    // open a URL connection to the Servlet
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL(MyApplication.attachUrl);
                    // Open a HTTP  connection to  the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + "*****");
                    conn.setRequestProperty("uploaded_file", sourceFileUri);
                    dos = new DataOutputStream(conn.getOutputStream());
                    dos.writeBytes("--" + "*****" + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + sourceFileUri + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    // create a buffer of  maximum size
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];
                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }
                    // send multipart form data necessary after file data...
                    dos.writeBytes(lineEnd);
                    dos.writeBytes("--" + "*****" + "--" + lineEnd);
                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();
                    Log.i("uploadFile", "HTTP Response is : "
                            + serverResponseMessage + ": " + serverResponseCode);
                    //close the streams //
                    fileInputStream.close();
                    dos.flush();
                    dos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return serverResponseCode;
            }
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public int getUploadCount() {
        int count = 0;
        for (Photo photo: MyApplication.mListPhotos)
            if (photo.getUpload() == null)
                count++;
        return count;
    }
}
