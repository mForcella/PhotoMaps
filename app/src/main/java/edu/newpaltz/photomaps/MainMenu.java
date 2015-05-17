package edu.newpaltz.photomaps;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainMenu extends Activity {

    ImageView mImageView;

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
                // go to main screen with newly created outing
                Intent i = new Intent(this, ViewPhotos.class);
                startActivity(i);
            }
        });
        uploadPhotos.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // go to main screen with newly created outing
                //Intent i = new Intent(this, UploadPhotos.class);
                //startActivity(i);
            }
        });
    }

    /**
     * Method called when returning from camera activity.
     * Returns to calling activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent mI = new Intent(this.getApplication(), SavePhoto.class);
        mI.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // create thumbnail and pass to intent
        Bitmap imageBitmap = getThumbnailBitmap(MyApplication.mPhotoPath, 300);
        mI.putExtra("thumb", imageBitmap);
        // image saved successfully; return to previous screen
        startActivity(mI);
    }

    // creates image file on disk
    private File createImageFile() throws IOException {
        // generate file name
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        MyApplication.mJpg = timeStamp + ".jpg";
        MyApplication.mPhotoPath = MyApplication.mImagePath + MyApplication.mJpg;
        return new File(MyApplication.mPhotoPath); // storage directory
    }

    // creates a thumbnail from an image path
    private Bitmap getThumbnailBitmap(String path, int thumbnailSize) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);
        if ((bounds.outWidth == -1) || (bounds.outHeight == -1)) {
            return null;
        }
        int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight
                : bounds.outWidth;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = originalSize / thumbnailSize;
        return BitmapFactory.decodeFile(path, opts);
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
        getMenuInflater().inflate(R.menu.main_menu, menu);
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
