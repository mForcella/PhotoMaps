package edu.newpaltz.photomaps;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
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
                Intent i = new Intent(this, UploadPhotos.class);
                startActivity(i);
            }
        });
    }

    private File createImageFile() throws IOException {
        // generate file name
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        MyApplication.mJpg = timeStamp + ".jpg";
        //Log.i("jpg path", MyApplication.mImagePath + MyApplication.mJpg);
        return new File(MyApplication.mImagePath + MyApplication.mJpg); // storage directory
    }

    /**
     * Method called when returning from camera activity.
     * Returns to calling activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent mI = new Intent(this.getApplication(), SavePhoto.class);
        mI.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // add thumbnail extra
        if (requestCode == MyApplication.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mI.putExtra("thumb", imageBitmap);
        }
        // image saved successfully; return to previous screen
        startActivity(mI);
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
