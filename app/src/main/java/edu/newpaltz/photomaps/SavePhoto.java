package edu.newpaltz.photomaps;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
                // get date
                String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
                // create photo object
                Photo photo = new Photo(String.valueOf(latLng[0]), String.valueOf(latLng[1]),
                        comment, MyApplication.mJpg, date);
                // add entry to sqlite database
                try {
                    DBAdapter db = new DBAdapter(myActivity);
                    db.addPhoto(photo);
                    db.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
