package me.kartikarora.transfersh.receivers;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import me.kartikarora.transfersh.BuildConfig;
import me.kartikarora.transfersh.services.ScheduledJobService;


/**
 * Developer: chipset
 * Package : me.kartikarora.transfersh.receivers
 * Project : Transfer.sh
 * Date : 11/1/17
 */

public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName componentName = new ComponentName(context, ScheduledJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(BuildConfig.VERSION_CODE / 10000, componentName)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setPeriodic(24 * 60 * 60 * 1000);
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler.schedule(builder.build()) == JobScheduler.RESULT_FAILURE) {
            Log.e("Transfer.sh", "Job Initiation Failed");
        } else {
            Log.i("Transfer.sh", "Job Initiated");
        }
    }
}
