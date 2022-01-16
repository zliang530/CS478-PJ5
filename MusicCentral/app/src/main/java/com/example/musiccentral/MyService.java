package com.example.musiccentral;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import androidx.core.app.NotificationCompat;
import com.example.CommonPackage.SongService;


public class MyService extends Service {

    private Notification notification ;
    private static final int NOTIFICATION_ID = 1;
    private static String CHANNEL_ID = "Music player style" ;

    @Override
    public void onCreate() {
        super.onCreate();
        this.createNotificationChannel();

        notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true).setContentTitle("Music Service")
                .setContentText("Service is now live!")
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {

        CharSequence name = "Music player notification";
        String description = "The channel for music player notifications";

        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, name, importance);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel.setDescription(description);
        }

        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    private final SongService.Stub mSongService = new SongService.Stub() {
        @Override
        public String[] getBitMap() throws RemoteException {
            String[] values = getResources().getStringArray(R.array.picture);
            return values;
        }

        @Override
        public String[] getTitle() throws RemoteException {
            String[] values = getResources().getStringArray(R.array.title);
            return values;
        }

        @Override
        public String[] getMp3() throws RemoteException {
            String[] values = getResources().getStringArray(R.array.mp3);
            return values;
        }

        @Override
        public String[] getAuthor() throws RemoteException {
            String[] values = getResources().getStringArray(R.array.author);
            return values;
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mSongService;
    }
}