package io.shunters.surveilor.receiver;

import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.receiver.Receiver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mykidong on 2017-02-24.
 */
public class VideoStreamReceiver extends Receiver<VideoStream> {

    private VideoConfiguration configuration;

    private List<String> locations;

    private Map<String, VideoStreamReader> streamReaderMap;

    public VideoStreamReceiver(VideoConfiguration configuration)
    {
        super(StorageLevel.MEMORY_ONLY_SER());

        this.configuration = configuration;

        this.locations = this.configuration.getLocations();
    }

    @Override
    public void onStart() {

        this.streamReaderMap = new HashMap<>();
        for(String location : locations)
        {
            VideoStreamReader reader = new VideoStreamReader(this, location, this.configuration.getStoreBatchSize(), this.configuration.getSamplingSize(), this.configuration.getImageType());
            this.streamReaderMap.put(location, reader);
            new Thread(reader).start();
        }
    }

    @Override
    public void onStop() {
        if(this.streamReaderMap != null)
        {
            for(String location : this.streamReaderMap.keySet())
            {
                this.streamReaderMap.get(location).stop();
            }
        }

        this.streamReaderMap = null;
    }
}
