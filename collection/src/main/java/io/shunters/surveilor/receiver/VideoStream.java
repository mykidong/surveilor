package io.shunters.surveilor.receiver;

import java.io.Serializable;

/**
 * Created by mykidong on 2017-02-24.
 */
public class VideoStream implements Serializable {

    public static final String AVRO_SCHEMA = "/META-INF/avro/video-stream.avsc";

    private String channelId;
    private String location;

    private byte[] imageBytes;
    private int imageLength;
    private int imageWidth;
    private int imageHeight;
    private String imageType;

    private long frameTimestamp;
    private long sequenceNo;


    public VideoStream(String channelId, String location, byte[] imageBytes, int imageWidth, int imageHeight, String imageType, long frameTimestamp, long sequenceNo)
    {
        this.channelId = channelId;
        this.location = location;
        this.imageBytes = imageBytes;
        this.imageLength = this.imageBytes.length;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.imageType = imageType;
        this.frameTimestamp = frameTimestamp;
        this.sequenceNo = sequenceNo;
    }

    public int getImageLength() {
        return imageLength;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getLocation() {
        return location;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public String getImageType() {
        return imageType;
    }

    public long getFrameTimestamp() {
        return frameTimestamp;
    }

    public long getSequenceNo() {
        return sequenceNo;
    }
}
