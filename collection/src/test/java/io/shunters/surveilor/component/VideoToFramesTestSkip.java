package io.shunters.surveilor.component;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Properties;

/**
 * Created by mykidong on 2017-02-23.
 */
public class VideoToFramesTestSkip {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    String basedir;


    @Before
    public void init() throws Exception
    {
        String libPath = System.getProperty("java.library.path");
        System.out.println("libPath: [" + libPath + "]");

        PropertiesFactoryBean propBean = new PropertiesFactoryBean();
        propBean.setLocation(new ClassPathResource("project.properties"));
        propBean.afterPropertiesSet();

        Properties prop = propBean.getObject();

        basedir = (String) prop.get("project.basedir");
        System.out.println("basedir: [" + basedir + "]");
    }

    @Test
    public void video2Frames() throws Exception{
        // ============ RTSP Stream Online Video Streaming. ===============
        // String url = "rtsp://192.168.0.108:554/mpeg4";
        // String url = "rtsp://admin:4321@192.168.0.106:554/profile2/media.smp";

        // ============ File Video Streaming. ===============
        String url = basedir + "/src/test/resources/data/paul-gilbert-technical-difficulties.mp4";
        System.out.println("url: [" + url + "]");


        System.out.printf("OpenCV %s\n", Core.VERSION);
        System.out.printf("Input file: %s\n", url);

        VideoCapture videoCapture = new VideoCapture(url);

        Assert.assertTrue(videoCapture.isOpened());


        System.out.println("isOpened: [" + videoCapture.isOpened() + "]");

        final Size frameSize = new Size((int) videoCapture.get(Videoio.CAP_PROP_FRAME_WIDTH), (int) videoCapture.get(Videoio.CAP_PROP_FRAME_HEIGHT));
        System.out.printf("Resolution: %s\n", frameSize);

        System.out.println("fps: [" + videoCapture.get(Videoio.CAP_PROP_FPS) + "]");

        final Mat mat = new Mat();

        int frames = 0;
        final long startTime = System.currentTimeMillis();

        while (videoCapture.read(mat)) {
            frames++;

            if (frames == 1000) {
                // display image.
                displayImage(mat2BufferedImage(mat));

                Thread.sleep(10000);


                break;
            }
        }
        final long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.printf("%d frames\n", frames);
        System.out.printf("Elapsed time: %4.2f seconds\n", (double) estimatedTime / 1000);

        videoCapture.release();
        mat.release();
    }

    private static void displayImage(Image img2) {
        ImageIcon icon = new ImageIcon(img2);
        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(img2.getWidth(null) + 50, img2.getHeight(null) + 50);
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


    private static BufferedImage mat2BufferedImage(Mat m) {
        // Fastest code
        // output can be assigned either to a BufferedImage or to an Image

        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels() * m.cols() * m.rows();

        byte[] b = new byte[bufferSize];
        m.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);

        return image;
    }
}
