package com.sikderithub.facebookvideodownloader.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.sikderithub.facebookvideodownloader.R;
import com.sikderithub.facebookvideodownloader.activities.SplashActivity;
import com.sikderithub.facebookvideodownloader.utils.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class NotificationUtil {


    private static final String CHANNEL_ID = "fbd_ll_Notification_Chanel";
    private static final String CHANNEL_NAME = "fbd_ll_Notification";
    private static int NOTIFICATION_ID = 22021;
    private final Context context;
    private Notification notification;

    public NotificationUtil(Context context) {
        this.context = context;
    }

    @SuppressLint("WrongConstant")
    public void displayNotification(NotificationData notificationData) {

        String tittle = notificationData.tittle;
        String message = notificationData.description;
        String iconUrl = notificationData.imgUrl;
        Bitmap iconBitmap = null;

        PendingIntent resultPendingIntent;

        Log.d("intentSelect", new Gson().toJson(notificationData));

        if (notificationData.action == 1) {
            //open url
            Log.d("intentSelect", "Url");
            Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
            notificationIntent.setData(Uri.parse(notificationData.actionUrl));


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                resultPendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_MUTABLE);
            } else {
                resultPendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
            }
        } else if (notificationData.action == 2) {
            //open activity
            Log.d("intentSelect", "Activity");


            String activity = notificationData.actionActivity;
            Intent resultIntent = null;
            if (activity != null) {
                resultIntent = new Intent(context, SplashActivity.class);
                resultIntent.putExtra(Constants.ACTIVITY, activity);
                resultIntent.putExtra(Constants.ACTIVITY_CREATED_BY_NOTI, true);

            }


            //resultIntent.putExtra("id", notificationData.id);
            // Create the TaskStackBuilder and add the intent, which inflates the back stack
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            // Get the PendingIntent containing the entire back stack
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                resultPendingIntent =
                        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE);
            } else {
                resultPendingIntent =
                        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            }

        } else {
            Log.d("intentSelect", "Splash Activity");

            Intent resultIntent = new Intent(context, SplashActivity.class);
            // Create the TaskStackBuilder and add the intent, which inflates the back stack
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntentWithParentStack(resultIntent);
            // Get the PendingIntent containing the entire back stack
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                resultPendingIntent =
                        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE);
            } else {
                resultPendingIntent =
                        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }


        if (notificationData.notiType == 2) {
            iconBitmap = getBitmapFromURL(iconUrl);
        }
        int icon = R.mipmap.ic_launcher;

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context, CHANNEL_ID);

        mBuilder.setContentIntent(resultPendingIntent).setOngoing(notificationData.notiClearAble == 0);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        mBuilder.setSmallIcon(getNotificationIcon(mBuilder));

        if (iconBitmap == null) {

            Log.d("NotificationLog", "iconBitmap null");

            //show without image
            notification = mBuilder
                    .setContentTitle(tittle)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build();
          /* if (notificationData.notiClearAble==0){
               notification.flags |= Notification.FLAG_AUTO_CANCEL;
           }*/

        } else {

            Log.d("NotificationLog", "iconBitmap not null");


            notification = mBuilder
                    .setTicker(tittle)
                    .setWhen(0)
                    .setContentTitle(tittle)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .setBigContentTitle(tittle)
                            .bigPicture(iconBitmap))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setLargeIcon(iconBitmap)
                    .setAutoCancel(true)
                    .build();
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_NAME;
            String description = "Notice";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setSound(soundUri, audioAttributes);


            notificationManager.createNotificationChannel(channel);
        } else {
            mBuilder.setSound(soundUri);
        }
        int m = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        NOTIFICATION_ID = m;
        notificationManager.notify(NOTIFICATION_ID, notification);

    }

    private Bitmap getBitmapFromURL(String strURL) {
        try {
            URL url = new URL(strURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int getNotificationIcon(NotificationCompat.Builder notificationBuilder) {

        //notificationBuilder.setColor(context.getResources().getColor(R.color.OrangeRed));
        return R.mipmap.ic_launcher;

    }
}
