package io.shunters.surveilor.util;

import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by mykidong on 2017-02-24.
 */
public class ImageUtils {
    /**
     * Converts an image to byte buffer representing PNG (bytes as they would exist on disk)
     *
     * @param image
     * @param encoding the encoding to be used, one of: png, jpeg, bmp, wbmp, gif
     * @return byte[] representing the image
     * @throws IOException if the bytes[] could not be written
     */
    public static byte[] imageToBytes(BufferedImage image, String encoding) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, encoding, baos);
        return baos.toByteArray();
    }

    /**
     * Converts the provided byte buffer into an BufferedImage
     *
     * @param buf byte[] of an image as it would exist on disk
     * @return
     * @throws IOException
     */
    public static BufferedImage bytesToImage(byte[] buf) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);
        return ImageIO.read(bais);
    }


    /**
     * Converts a given image into grayscalse
     *
     * @param src
     * @return
     */
    public static BufferedImage convertToGray(BufferedImage src) {
        ColorConvertOp grayOp = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        return grayOp.filter(src, null);
    }

    public static BufferedImage mat2BufferedImage(Mat m) {
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

    public static void displayImage(Image image) {
        ImageIcon icon = new ImageIcon(image);
        JFrame frame = new JFrame();
        frame.setLayout(new FlowLayout());
        frame.setSize(image.getWidth(null) + 50, image.getHeight(null) + 50);
        JLabel lbl = new JLabel();
        lbl.setIcon(icon);
        frame.add(lbl);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
