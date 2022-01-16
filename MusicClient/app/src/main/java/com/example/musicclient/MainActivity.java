package com.example.musicclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.CommonPackage.SongService;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "aMessage";
    private SongService mSongService;
    boolean mIsBound = false;

    SeekBar mSeekBar;
    Button startBtn;
    Button stopBtn;
    Button getSong;
    RecyclerView RV;
    SongAdapter mSongAdapter;
    TextView displayNum;
    TextView status;
    private Boolean mBitmapLock = false ;

    ArrayList<Bitmap> images;
    ArrayList<String> titles;
    ArrayList<String> authors;
    ArrayList<String> mp3s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSeekBar = (SeekBar) findViewById(R.id.setSong);
        startBtn = (Button) findViewById(R.id.startService);
        stopBtn = (Button) findViewById(R.id.stopService);
        getSong = (Button) findViewById(R.id.getSongs);
        displayNum = (TextView) findViewById(R.id.displayNumSong);
        status = (TextView) findViewById(R.id.status);
        RV = (RecyclerView) findViewById(R.id.recyclerView);
        mSongAdapter = new SongAdapter(titles, mp3s, authors, images);

        /*
            seekbar ranges from 0-5 inclusive
            case 0: returns all songs
            case 1-5: return song at an index
        */
        mSeekBar.setMax(5);
        RV.setAdapter(mSongAdapter);

        // click to start process
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // toast for when clicking startBtn and displaying instructions for seekbar
                Context context = getApplicationContext();
                CharSequence text = "Seekbar at 0 is for all songs\nSeekbar at 1-5 is for a particular song";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                // only bind service and create adapter when service is not yet bounded
                if (mIsBound == false){

                    // empty arraylist of song info to be kept in adapter
                    images = new ArrayList<>();
                    titles = new ArrayList<>();
                    authors = new ArrayList<>();
                    mp3s = new ArrayList<>();

                    // creation of adapter
                    mSongAdapter = new SongAdapter(titles, mp3s, authors, images);

                    // setting recycler view to the adapter
                    RV.setAdapter(mSongAdapter);

                    // defining the layout for recycler view items
                    RV.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                    // bind service
                    checkBindingAndBind();

                    // let user know the service is connected
                    status.setText("ALIVE");
                }
            }
        });

        // for unbinding the service and clearing the client side view of list items
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // only stop when service is actually binded
                if(mIsBound){

                    mIsBound = false;

                    // release and stop media so songs won't continue playing, if there is one
                    mSongAdapter.mPlayer.stop();
                    mSongAdapter.mPlayer.release();

                    // unbind service
                    unbindService(mConnection);

                    // let user know the service is connected
                    status.setText("DEAD");

                    // resetting the stored music info thus far
                    images = new ArrayList<>();
                    titles = new ArrayList<>();
                    authors = new ArrayList<>();
                    mp3s = new ArrayList<>();

                    mSongAdapter = new SongAdapter(titles, mp3s, authors, images);

                    // set the new initialized adapter to recycler view
                    RV.setAdapter(mSongAdapter);

                    // defining the layout for recycler view items
                    RV.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                }
            }
        });

        // get the song from the service
        getSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // only get song if service is binded
                if (mIsBound) {

                    //  process url images in a new thread to not block ui thread
                    Runnable aRunnable = new Runnable() {
                        public void run() {

                            synchronized (mBitmapLock) {
                                try {
                                    // our app only have 5 songs we so we will have 5 bitmap
                                    Bitmap[] mBitmaps = new Bitmap[5];

                                    // get the url images from the service
                                    String[] values = mSongService.getBitMap();

                                    // processing and store into the array
                                    for (int i = 0; i < mBitmaps.length; i++) {
                                        mBitmaps[i] = loadImageFromNetwork(values[i]);
                                    }

                                    // get value of the seeker that was set earlier into the textview
                                    Integer currNum = Integer.valueOf((String) displayNum.getText());

                                    // get all song info
                                    if (currNum != 0){
                                        images.add(mBitmaps[currNum-1]);
                                        titles.add(mSongService.getTitle()[currNum-1]);
                                        authors.add(mSongService.getAuthor()[currNum-1]);
                                        mp3s.add(mSongService.getMp3()[currNum-1]);
                                    }
                                    // get one song's info at a particular index
                                    else{
                                        images.addAll(Arrays.asList(mBitmaps));
                                        titles.addAll(Arrays.asList(mSongService.getTitle()));
                                        authors.addAll(Arrays.asList(mSongService.getAuthor()));
                                        mp3s.addAll(Arrays.asList(mSongService.getMp3()));
                                    }

                                    // only ui-thread can change the UI
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            mSongAdapter.mPlayer.stop();
                                            mSongAdapter.mPlayer.reset();
                                            mSongAdapter.mPlayer.release();

                                            // make a toast to let client know that new song/s are added
                                            Context context = getApplicationContext();
                                            CharSequence text = "New Songs Add!!!";
                                            int duration = Toast.LENGTH_LONG;
                                            Toast toast = Toast.makeText(context, text, duration);
                                            toast.show();

                                            mSongAdapter = new SongAdapter(titles, mp3s, authors, images);
                                            RV.setAdapter(mSongAdapter);
                                            RV.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                                        }
                                    });
                                }
                                catch (Exception e) {}
                            }
                        };
                    };
                    Thread t1 = new Thread(aRunnable);
                    t1.start();
                }
            }
        });

        // seekbar change listener for users to set the index of songs they want
        // case 0: returns all songs
        // case 1-5: return song at an index
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                // setting value of seekbar to textview so users can see
                displayNum.setText(String.valueOf(i));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    // function for binding to a service
    protected void checkBindingAndBind() {
        if (!mIsBound) {

            boolean b = false;
            Intent i = new Intent(SongService.class.getName());

            ResolveInfo info = getPackageManager().resolveService(i, 0);
            i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

            b = bindService(i, this.mConnection, Context.BIND_AUTO_CREATE);

            if (b) {
                Log.i(TAG, "bindService() succeeded!");
            } else {
                Log.i(TAG, "bindService() failed!");
            }
        }
    }


    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder iservice) {
            mSongService = SongService.Stub.asInterface(iservice);
            mIsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            mSongService = null;
            mIsBound = false;
        }
    };

    // returns a loaded bitmap given a url
    private Bitmap loadImageFromNetwork(String url)  {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
            return bitmap;
        } catch (Exception e) {
            System.out.println("Error Loading Image");
            return null ;
        }
    }
}