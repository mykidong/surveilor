package io.shunters.surveilor.util;

/**
 * Created by mykidong on 2017-02-24.
 */
public class VideoStreamUtils {

    public static String getChannelId(String location)
    {
        String channelId = "" + location.hashCode();
        if (location.contains("/")) {
            channelId = location.substring(location.lastIndexOf('/') + 1) + "_" + channelId;
        }

        return channelId;
    }
}
