package me.kartikarora.transfersh.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

/**
 * Developer: chipset
 * Package : me.kartikarora.transfersh.services
 * Project : Transfer.sh
 * Date : 1/11/17
 */

public class ScheduledJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        startService(new Intent(getApplicationContext(), CheckAndNotifyOrDeleteService.class));
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
