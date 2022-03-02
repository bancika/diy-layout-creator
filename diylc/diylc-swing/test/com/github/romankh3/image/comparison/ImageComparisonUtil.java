package com.github.romankh3.image.comparison;

import com.github.romankh3.image.comparison.exception.ImageComparisonException;
import com.github.romankh3.image.comparison.exception.ImageNotFoundException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Tools for the {@link ImageComparison} object.
 */
public final class ImageComparisonUtil {

    /**
     * Make a copy of the {@link BufferedImage} object.
     *
     * @param image the provided image.
     * @return copy of the provided image.
     */
    static BufferedImage deepCopy(BufferedImage image) {
        ColorModel cm = image.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(image.getRaster().createCompatibleWritableRaster());
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    /**
     * Read image from the provided path.
     *
     * @param path the path where contains image.
     * @return the {@link BufferedImage} object of this specific image.
     * @throws ImageComparisonException due to read the image from resources.
     */
    public static BufferedImage readImageFromResources(String path) throws ImageComparisonException {
        File imageFile = new File(path);
        if (imageFile.exists()) {
            try {
                return ImageIO.read(imageFile);
            } catch (IOException e) {
                throw new ImageComparisonException(String.format("Cannot read image from the file, path=%s", path), e);
            }
        } else {
            InputStream inputStream = ImageComparisonUtil.class.getClassLoader().getResourceAsStream(path);
            if (inputStream != null) {
                try {
                    return ImageIO.read(inputStream);
                } catch (IOException e) {
                    throw new ImageComparisonException(String.format("Cannot read image from the file, path=%s", path), e);
                }
            } else {
                throw new ImageNotFoundException(String.format("Image with path = %s not found", path));
            }
        }
    }

    /**
     * Save image to the provided path.
     *
     * @param pathFile the path to the saving image.
     * @param image the {@link BufferedImage} object of this specific image.
     * @throws ImageComparisonException due to save image.
     */
    public static void saveImage(File pathFile, BufferedImage image) throws ImageComparisonException {
        File dir = pathFile.getParentFile();
        // make dir if it's not using from Gradle.
        boolean dirExists = dir == null || dir.isDirectory() || dir.mkdirs();
        if (!dirExists) {
            throw new ImageComparisonException("Unable to create directory " + dir);
        }
        try {
            ImageIO.write(image, "png", pathFile);
        } catch (IOException e) {
            throw new ImageComparisonException(
                    String.format("Cannot save image to path=%s", pathFile.getAbsolutePath()), e);
        }
    }

    /**
     * Resize image to new dimensions and return new image.
     *
     * @param img the object of the image to be resized.
     * @param newW the new width.
     * @param newH the new height.
     * @return resized {@link BufferedImage} object.
     */
    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        return toBufferedImage(img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH));
    }

    /**
     * Convert image to buffered image.
     *
     * @param img the object of the image to be converted to buffered image.
     * @return the converted buffered image.
     */
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        float softenFactor = 0.05f;
        final Image temp = new ImageIcon(img).getImage();
        final BufferedImage bufferedImage = new BufferedImage(
                temp.getWidth(null),
                temp.getHeight(null),
                BufferedImage.TYPE_INT_RGB);
        final Graphics g = bufferedImage.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, temp.getWidth(null), temp.getHeight(null));
        g.drawImage(temp, 0, 0, null);
        g.dispose();

        final float[] softenArray = {0, softenFactor, 0, softenFactor, 1 - (softenFactor * 4), softenFactor, 0,
                softenFactor, 0};
        final Kernel kernel = new Kernel(3, 3, softenArray);
        final ConvolveOp cOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);

        return cOp.filter(bufferedImage, null);
    }

    /**
     * Return the difference in percent between two buffered images.
     *
     * @param img1 the first image.
     * @param img2 the second image.
     * @return difference percent.
     */
    public static float getDifferencePercent(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img1.getHeight();

        long diff = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                diff += pixelDiff(img1.getRGB(x, y), img2.getRGB(x, y));
            }
        }
        long maxDiff = 3L * 255 * width * height;

        return (float) (100.0 * diff / maxDiff);
    }

    /**
     * Compare two pixels
     *
     * @param rgb1 the first rgb
     * @param rgb2 the second rgn
     * @return the difference.
     */
    public static int pixelDiff(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xff;
        int g1 = (rgb1 >> 8) & 0xff;
        int b1 = rgb1 & 0xff;
        int r2 = (rgb2 >> 16) & 0xff;
        int g2 = (rgb2 >> 8) & 0xff;
        int b2 = rgb2 & 0xff;
        return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
    }
}
