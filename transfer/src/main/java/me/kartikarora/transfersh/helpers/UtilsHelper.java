package me.kartikarora.transfersh.helpers;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.SimpleDateFormat;
import java.util.Locale;

import me.kartikarora.transfersh.BuildConfig;
import me.kartikarora.transfersh.services.ScheduledCheckAndNotifiyOrDeleteJobService;

/**
 * Developer: chipset
 * Package : me.kartikarora.transfersh.helpers
 * Project : Transfer.sh
 * Date : 1/12/17
 */

public class UtilsHelper {
    private static UtilsHelper utilsHelperObject;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private UtilsHelper() {
    }

    public static UtilsHelper getInstance() {
        if (utilsHelperObject == null) {
            utilsHelperObject = new UtilsHelper();
        }
        return utilsHelperObject;
    }

    public void scheduleServiceJob(Context context) {
        ComponentName componentName = new ComponentName(context, ScheduledCheckAndNotifiyOrDeleteJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(BuildConfig.VERSION_CODE / 10000, componentName)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setPeriodic(60 * 60 * 1000);
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler.schedule(builder.build()) == JobScheduler.RESULT_FAILURE) {
            Log.e("Transfer.sh", "Job Initiation Failed");
        } else {
            Log.i("Transfer.sh", "Job Initiated");
        }
    }

    public SimpleDateFormat getSdf() {
        return sdf;
    }

    public void trackEvent(FirebaseAnalytics firebaseAnalytics, String ... params) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_CATEGORY, params[0]);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, params[1]);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
}
