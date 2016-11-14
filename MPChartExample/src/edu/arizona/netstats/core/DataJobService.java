package edu.arizona.netstats.core;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;

import netstatbackend.Persistence;

/**
 * Created by sridh on 11/13/2016.
 */

public class DataJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(this.getApplicationContext());

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private class AsyncTaskRunner extends AsyncTask<Context,Void,Void>{

        @Override
        protected Void doInBackground(Context... params) {
            DataSyncService service = new DataSyncService(params[0]);
            service.storeData();
            return null;
        }
    }
}
