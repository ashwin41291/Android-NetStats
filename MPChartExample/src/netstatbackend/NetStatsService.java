package netstatbackend;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by sridh on 9/25/2016.
 */

public class NetStatsService extends IntentService {

    public  NetStatsService(){
        super("NetStatsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String url = intent.getDataString();
    }
}
