package io.shunters.surveilor.receiver;

import java.io.Serializable;

/**
 * Created by mykidong on 2017-02-24.
 */
public class VideoStream implements Serializable {

    private String streamId;
    private byte[] imageBytes;
    private int imageWidth;
    private int imageHeight;
    private String imageType;

    private long frameTimestamp;
    private long sequenceNo;

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public long getFrameTimestamp() {
        return frameTimestamp;
    }

    public void setFrameTimestamp(long frameTimestamp) {
        this.frameTimestamp = frameTimestamp;
    }

    public long getSequenceNo() {
        return sequenceNo;
    }

    public void setSequenceNo(long sequenceNo) {
        this.sequenceNo = sequenceNo;
    }
}
