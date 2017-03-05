package me.kartikarora.transfersh.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.kartikarora.transfersh.helpers.UtilsHelper;


/**
 * Developer: chipset
 * Package : me.kartikarora.transfersh.receivers
 * Project : Transfer.sh
 * Date : 11/1/17
 */

public class BootCompleteBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            UtilsHelper.getInstance().scheduleServiceJob(context);
        }
    }
}
