package com.ilesson.ppim.receiver;

import android.content.Context;
import android.util.Log;

import io.rong.push.PushType;
import io.rong.push.notification.PushMessageReceiver;
import io.rong.push.notification.PushNotificationMessage;

/**
 * Created by potato on 2020/3/11.
 */

public class NotificationReceiver extends PushMessageReceiver {
    private static final String TAG = "NotificationReceiver";
    @Override
    public boolean onNotificationMessageArrived(Context context, PushType pushType, PushNotificationMessage pushNotificationMessage) {
        Log.d(TAG, "onNotificationMessageArrived: pushType="+pushType.getName());
        Log.d(TAG, "onNotificationMessageArrived: pushNotificationMessage="+pushNotificationMessage.getPushContent());
        return false;
    }

    @Override
    public boolean onNotificationMessageClicked(Context context, PushType pushType, PushNotificationMessage pushNotificationMessage) {
        return false;
    }
}