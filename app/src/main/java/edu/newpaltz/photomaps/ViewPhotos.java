package edu.newpaltz.photomaps;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class ViewPhotos extends FragmentActivity {

    private static ViewPager mPager;
    private static String mNewLocation;
    private static String mNewComment;
    private static ScreenSlidePagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photos);
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        if (MyApplication.mPages == 0)
            Toast.makeText(getApplicationContext(), "No images to view...", Toast.LENGTH_SHORT).show();
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            PhotoFragment fragment = new PhotoFragment();
            Bundle args = new Bundle();
            args.putInt("page", i);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return MyApplication.mPages;
        }
    }

    public static class PhotoFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_photo, container, false);
            // get current page number
            Bundle args = getArguments();
            int page = args.getInt("page");
            // set page number text view
            TextView pageNum = (TextView)rootView.findViewById(R.id.page);
            pageNum.setText(pageNum.getText() + " " + String.valueOf(page + 1) + " of " + (MyApplication.mPages));
            // initialize views
            ImageView thumbnail = (ImageView)rootView.findViewById(R.id.thumbnail);
            final TextView location = (TextView)rootView.findViewById(R.id.location);
            TextView date = (TextView)rootView.findViewById(R.id.date);
            final TextView comment = (TextView)rootView.findViewById(R.id.comment);
            TextView upload = (TextView)rootView.findViewById(R.id.upload);
            Button delete = (Button)rootView.findViewById(R.id.delete);
            Button editLocation = (Button)rootView.findViewById(R.id.editLocation);
            Button editComment = (Button)rootView.findViewById(R.id.editComment);
            // get current photo
            final Photo photo = MyApplication.mListPhotos.get(page);
            // set image thumbnail
            Bitmap thumb = MyApplication.getThumbnailBitmap(MyApplication.mImagePath + photo.getPhoto(), 250);
            thumbnail.setImageBitmap(thumb);
            // set location
            location.setText(location.getText() + " " + photo.getLocation());
            // set date
            date.setText(date.getText() + " " + photo.getDate());
            // set comment
            comment.setText(comment.getText() + " " + photo.getComment());
            // set uploaded
            if (photo.getUpload() != null)
                upload.setText(upload.getText() + " Yes");
            else
                upload.setText(upload.getText() + " No");
            // delete button
            delete.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    try {
                        DBAdapter db = new DBAdapter(getActivity());
                        db.deletePhoto(photo.getId());
                        db.close();
                        // reload view
                        mPager.getAdapter().notifyDataSetChanged();
                        if (MyApplication.mPages == 0) {
                            // return to main menu if no images
                            Intent i = new Intent(getActivity(), MainMenu.class);
                            startActivity(i);
                        } else {
                            // or reinitialize view pager
                            mPager.setAdapter(mPagerAdapter);
                        }
                        //mPager.getAdapter().notifyDataSetChanged();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            // edit location button
            editLocation.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // prompt for new value
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Enter new location value");
                    // Set up the input
                    final EditText input = new EditText(getActivity());
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);
                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mNewLocation = input.getText().toString();
                            // store new value in database
                            try {
                                DBAdapter db = new DBAdapter(getActivity());
                                db.updateLocation(photo.getId(), mNewLocation);
                                db.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            // reload photo list from database
                            MyApplication.getPhotos();
                            // change text view
                            location.setText("Location: " + mNewLocation);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            });
            // edit location button
            editComment.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // prompt for new value
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Enter new description value");
                    // Set up the input
                    final EditText input = new EditText(getActivity());
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);
                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mNewComment = input.getText().toString();
                            // store new value in database
                            try {
                                DBAdapter db = new DBAdapter(getActivity());
                                db.updateComment(photo.getId(), mNewComment);
                                db.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            // reload photo list from database
                            MyApplication.getPhotos();
                            // change text view
                            comment.setText("Description: " + mNewComment);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            });
            return rootView;
        }
    }
}