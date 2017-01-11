package me.kartikarora.transfersh.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.kartikarora.transfersh.services.CheckAndNotifyOrDeleteService;


/**
 * Developer: chipset
 * Package : me.kartikarora.transfersh.receivers
 * Project : Transfer.sh
 * Date : 1/11/17
 */

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, CheckAndNotifyOrDeleteService.class));
    }
}
