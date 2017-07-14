package me.kartikarora.transfersh.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import me.kartikarora.potato.Potato;
import me.kartikarora.transfersh.R;
import me.kartikarora.transfersh.actions.IntentAction;
import me.kartikarora.transfersh.activities.TransferActivity;
import me.kartikarora.transfersh.contracts.FilesContract;
import me.kartikarora.transfersh.helpers.UtilsHelper;

/**
 * Developer: chipset
 * Package : me.kartikarora.transfersh.services
 * Project : Transfer.sh
 * Date : 1/11/17
 */

public class ScheduledCheckAndNotifiyOrDeleteJobService extends JobService {
    private static final String TAG = ScheduledCheckAndNotifiyOrDeleteJobService.class.getName();

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(this.getClass().getName(), "Job started");
        boolean success = false;
//        startService(new Intent(getApplicationContext(), CheckAndNotifyOrDeleteService.class));
        Cursor cursor = getContentResolver().query(FilesContract.BASE_CONTENT_URI, new String[]{
                FilesContract.FilesEntry._ID, FilesContract.FilesEntry.COLUMN_NAME,
                FilesContract.FilesEntry.COLUMN_DATE_DELETE}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String dateDelete = cursor.getString(cursor.getColumnIndex(FilesContract.FilesEntry.COLUMN_DATE_DELETE));
                String name = cursor.getString(cursor.getColumnIndex(FilesContract.FilesEntry.COLUMN_NAME));
                long id = cursor.getLong(cursor.getColumnIndex(FilesContract.FilesEntry._ID));
                SimpleDateFormat sdf = UtilsHelper.getInstance().getSdf();
                Calendar today = Calendar.getInstance();
                Calendar delDate = Calendar.getInstance();
                try {
                    delDate.setTime(sdf.parse(dateDelete));
                    long todayMillis = today.getTimeInMillis();
                    long delDateMillis = delDate.getTimeInMillis();
                    long days = TimeUnit.MILLISECONDS.toDays(Math.abs(delDateMillis - todayMillis));
                    Log.i(TAG, days + "");
                    if (days == 3) {
                        success = true;
                        Potato.potate(getApplicationContext()).Notifications().showNotificationDefaultSound("Transfer.sh",
                                "Your uploaded file " + name + " is scheduled for deletion in 3 days",
                                R.drawable.ic_notification, new Intent(getApplicationContext(), TransferActivity.class));
                    } else if (days == 1) {
                        success = true;
                        Intent reuploadIntent = new Intent(this, TransferActivity.class);
                        reuploadIntent.setAction(IntentAction.ACTION_REUPLOAD);
                        reuploadIntent.putExtra("file_id", id);
                        PendingIntent reuploadPendingIntent = PendingIntent.getActivity(this, 0, reuploadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        Notification notification = new NotificationCompat.Builder(getApplicationContext(), "default")
                                .setContentTitle("Transfer.sh")
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentText("Your uploaded file " + name + " is due for deletion tomorrow")
                                .addAction(R.drawable.ic_upload, "RE UPLOAD", reuploadPendingIntent)
                                .build();
                        NotificationManagerCompat mNotifyMgr = NotificationManagerCompat.from(getApplicationContext());
                        mNotifyMgr.cancelAll();
                        mNotifyMgr.notify(0, notification);
                    } else if (days == 0) {
                        success = true;
                        getContentResolver().delete(FilesContract.BASE_CONTENT_URI, FilesContract.FilesEntry._ID + "=?",
                                new String[]{String.valueOf(id)});
                    } else {
                        Log.i(TAG, "onStartJob: No job to perform, exiting");
                        success = true;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
            jobFinished(params, !success);
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
