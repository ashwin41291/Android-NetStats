package netstatbackend;

/**
 * Created by sridh on 10/28/2016.
 */

public class NetworkStatistic implements Comparable<NetworkStatistic>{
    public String appName;
    public int uid;
    public long usageInBytes;
    public long startDate;
    public long endDate;
    public String packageName;

    @Override
    public int compareTo(NetworkStatistic networkStatistic) {
        if(this.usageInBytes>networkStatistic.usageInBytes)
            return 1;
        else return 0;
    }
}
