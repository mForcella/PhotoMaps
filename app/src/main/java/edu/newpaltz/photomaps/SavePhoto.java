package edu.newpaltz.photomaps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class SavePhoto extends Activity {

    ImageView mImageView;
    Activity myActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_photo);
        // set image thumbnail
        mImageView = (ImageView)findViewById(R.id.thumbnail);
        Bitmap thumb = getIntent().getParcelableExtra("thumb");
        mImageView.setImageBitmap(thumb);
        // save button
        Button saveButton = (Button)findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // get gps coordinates
                double[] latLng = getLatLng();
                // get comments
                String comment = ((EditText)findViewById(R.id.photo_comment)).getText().toString();
                // add entry to sqlite database

                // return to main menu
                Intent i = new Intent(myActivity, MainMenu.class);
                startActivity(i);
            }
        });
        // cancel button
        Button cancelButton = (Button)findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // return to main menu
                Intent i = new Intent(myActivity, MainMenu.class);
                startActivity(i);
            }
        });
    }

    /**
     * Method to set the latitude and longitude values.
     */
    private double[] getLatLng() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        /* Loop over the array backwards, and if you get an accurate location, then break out the loop*/
        Location l = null;
        for (int i = providers.size()-1; i >= 0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }
        double[] gps = new double[2];
        if (l != null) {
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
        }
        return gps;
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
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return serverResponseCode;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.save_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
