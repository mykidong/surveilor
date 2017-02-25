package io.shunters.surveilor.receiver;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mykidong on 2017-02-24.
 */
public class VideoConfiguration implements Serializable {

    private List<String> locations;
    private int storeBatchSize;
    private int samplingRate;
    String imageType;

    public int getStoreBatchSize() {
        return storeBatchSize;
    }

    public void setStoreBatchSize(int storeBatchSize) {
        this.storeBatchSize = storeBatchSize;
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
        this.locations = locations;
    }
}
