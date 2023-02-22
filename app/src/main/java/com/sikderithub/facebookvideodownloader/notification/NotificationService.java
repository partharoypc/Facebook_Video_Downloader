package com.sikderithub.facebookvideodownloader.notification;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.sikderithub.facebookvideodownloader.activities.SplashActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class NotificationService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d("NotificationService", "onMessageReceived: received");

        if (message.getData().size() > 0) {
            Map<String, String> data = message.getData();
            Log.d("Notification", new Gson().toJson(data));

            handleData(data);

        } else if (message.getNotification() != null) {

            handleNotification(message.getNotification());
        }
    }

    private void handleNotification(RemoteMessage.Notification notification) {
        NotificationData notificationData = new NotificationData();

        notificationData.tittle = notification.getTitle();
        notificationData.description = notification.getBody();

        Intent resultIntent = new Intent(getApplicationContext(), SplashActivity.class);

        NotificationUtil notificationUtil = new NotificationUtil(getApplicationContext());

        notificationUtil.displayNotification(notificationData);
    }

    private void handleData(Map<String, String> data) {
        NotificationData notificationData = new NotificationData();
        Log.d("newMessage", "call");

        if (data.get("tittle") != null && data.get("description") != null && data.get("imgUrl") != null && data.get("notiClearAble") != null && data.get("action") != null && data.get("notiType") != null) {
            notificationData.tittle = data.get("tittle");
            notificationData.description = data.get("description");
            notificationData.imgUrl = data.get("imgUrl");
            notificationData.notiClearAble = Integer.parseInt(data.get("notiClearAble"));
            notificationData.action = Integer.parseInt(data.get("action"));
            notificationData.notiType = Integer.parseInt(data.get("notiType"));
            notificationData.actionUrl = data.get("actionUrl");
            notificationData.actionActivity = data.get("actionActivity");

            //Intent resultIntent  = new Intent(getApplicationContext(), MainActivity.class);

            if (notificationData.action == 2 && notificationData.actionActivity != null) {
                if (notificationData.actionActivity.equals("ResultListActivity") || notificationData.actionActivity.equals("game2.ResultListActivity")) {
                    if (!showResultNotification(data.get("time"), data.get("timezone"))) {
                        return;
                    }
                }
            }


            NotificationUtil notificationUtil = new NotificationUtil(getApplicationContext());

            notificationUtil.displayNotification(notificationData);
        } else {
            Log.d("Notification", "Null Data Found");
        }


    }

    public boolean showResultNotification(String serverTime, String timeZone) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.US);

        try {
            Date currTime = sdf.parse(sdf.format(new Date()));
            sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
            Date sendTime = sdf.parse(serverTime);
            sdf.setTimeZone(TimeZone.getTimeZone(TimeZone.getDefault().getID()));
            sendTime = sdf.parse(sdf.format(sendTime));

            long diffInMills = currTime.getTime() - sendTime.getTime();

            long diff = TimeUnit.MILLISECONDS.toSeconds(diffInMills);

            Log.d("Notification", String.valueOf(diff));
            long sec = 60 * 5;
            if (diff <= sec) {
                return true;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;

    }
}
