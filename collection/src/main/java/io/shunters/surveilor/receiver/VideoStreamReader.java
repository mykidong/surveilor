package io.shunters.surveilor.receiver;

import io.shunters.surveilor.util.ImageUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mykidong on 2017-02-24.
 */
public class VideoStreamReader implements Runnable {

    private static Logger log = LoggerFactory.getLogger(VideoStreamReader.class);

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private boolean running = false;
    private String location;
    private VideoStreamReceiver receiver;
    private int storeBatchSize;
    private int samplingSize;
    private String imageType;
    private VideoCapture videoCapture;
    private long sequenceNo = 0;

    public VideoStreamReader(VideoStreamReceiver receiver, String location, int storeBatchSize, int samplingSize, String imageType) {
        this.receiver = receiver;
        this.location = location;
        this.storeBatchSize = storeBatchSize;
        this.samplingSize = samplingSize;
        this.imageType = imageType;
    }


    @Override
    public void run() {
        running = true;

        videoCapture = new VideoCapture(this.location);
        log.info("location [" + this.location + "] is opened: [" + videoCapture.isOpened() + "]");

        final Size frameSize = new Size((int) videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH), (int) videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT));
        log.info("resolution: {}", frameSize);

        log.info("fps: [" + videoCapture.get(Videoio.CAP_PROP_FPS) + "]");

        final Mat mat = new Mat();

        List<VideoStream> streams = new ArrayList<>();
        int count = 0;
        while (running) {
            try {
                // sampling size 는 skip 함.
                for (int i = 0; i < this.samplingSize; i++) {
                    videoCapture.read(mat);

                    sequenceNo++;
                }

                // sampling size 의 frame 들을 skip 한 후 frame 얻음.
                videoCapture.read(mat);
                sequenceNo++;

                BufferedImage image = ImageUtils.mat2BufferedImage(mat);
                byte[] imageBytes = ImageUtils.imageToBytes(image, imageType);
                long frameTimestamp = (long) videoCapture.get(Videoio.CAP_PROP_POS_MSEC);

                // TODO: stream id.
                String streamId = null;

                VideoStream stream = new VideoStream();
                stream.setStreamId(streamId);
                stream.setImageBytes(imageBytes);
                stream.setFrameTimestamp(frameTimestamp);
                stream.setSequenceNo(sequenceNo);
                stream.setImageWidth((int) frameSize.width);
                stream.setImageHeight((int) frameSize.height);

                streams.add(stream);

                count++;

                if ((count > storeBatchSize) && (streams.size() > 0)) {
                    this.receiver.store(streams.iterator());

                    streams = new ArrayList<>();
                    count = 0;
                }
            } catch (IOException e) {
                log.error("ERROR: " + e.getMessage());

                continue;
            }
        }

        this.videoCapture.release();
        mat.release();
    }

    public void stop() {
        this.videoCapture.release();

        this.running = false;
    }
}
