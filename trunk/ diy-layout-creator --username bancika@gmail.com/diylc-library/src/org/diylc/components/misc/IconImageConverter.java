package org.diylc.components.misc;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class IconImageConverter implements Converter {

	private static final Logger LOG = Logger.getLogger(IconImageConverter.class);

	@Override
	public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {
		ImageIcon image = (ImageIcon) object;
		int width = image.getIconWidth();
		int height = image.getIconHeight();
		int[] pixels = new int[width * height];

		if (image != null) {
			try {
				PixelGrabber pg = new PixelGrabber(image.getImage(), 0, 0, width, height, pixels,
						0, width);
				pg.grabPixels();
				if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
					LOG.error("Failed to load image contents");
				}
			} catch (InterruptedException e) {
				LOG.error("Image load interrupted");
			}
		}
		LOG.debug("Writing image to file: " + width + "x" + height);
		writer.addAttribute("width", Integer.toString(width));
		writer.addAttribute("height", Integer.toString(height));
		StringBuilder dataBuilder = new StringBuilder();
		int n = 0;
		for (int pixel : pixels) {
			dataBuilder.append(Integer.toString(pixel, 16));
			dataBuilder.append(",");
			n++;
			if (n % 16 == 0) {
				dataBuilder.append("\n");
			}
		}
		writer.addAttribute("data", dataBuilder.toString());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		int width = Integer.parseInt(reader.getAttribute("width"));
		int height = Integer.parseInt(reader.getAttribute("height"));
		LOG.debug("Reading image from file: " + width + "x" + height);
		int[] pixels = new int[width * height];
		String data = reader.getAttribute("data").replace("\n", "");
		String[] splitData = data.split(",");
		for (int i = 0; i < Math.min(splitData.length, pixels.length); i++) {
			if (!splitData[i].trim().isEmpty()) {
				try {
					pixels[i] = Integer.parseInt(splitData[i].trim(), 16);
				} catch (Exception e) {
					pixels[i] = 0;
				}
			}
		}

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		ColorModel colorModel = ColorModel.getRGBdefault();
		Image image = toolkit.createImage(new MemoryImageSource(width, height, colorModel, pixels,
				0, width));

//		BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//		bimg.setRGB(0, 0, width, height, pixels, 0, width);
//		File outputfile = new File("c:\\saved.png");
//		try {
//			ImageIO.write(bimg, "png", outputfile);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return new ImageIcon(image);
	}

	@Override
	public boolean canConvert(Class clazz) {
		return ImageIcon.class.isAssignableFrom(clazz);
	}
}
