package org.diylc.swing.plugins.toolbox.openide;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageObserver;
import java.awt.image.RGBImageFilter;
import java.awt.image.WritableRaster;
import java.net.URL;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public final class ImageUtilities {
  static final String TOOLTIP_SEPAR = "<br>";

  private static final Set<String> extraInitialSlashes = new HashSet<String>();
  private static volatile Object currentLoader;
  private static boolean noLoaderWarned;
  private static final Component component;
  private static final MediaTracker tracker;
  private static int mediaTrackerID;
  private static ImageReader PNG_READER;
  private static final Logger ERR;

  private ImageUtilities() {}

  public static final Icon image2Icon(Image image) {
    if (image instanceof ToolTipImage) {
      return ((ToolTipImage) image).getIcon();
    }
    return new ImageIcon(image);
  }

  public static final Image icon2Image(Icon icon) {
    if (icon instanceof ImageIcon) {
      return ((ImageIcon) icon).getImage();
    }
    ToolTipImage image = new ToolTipImage("", icon.getIconWidth(), icon.getIconHeight(), 2);
    Graphics g = image.getGraphics();
    icon.paintIcon(new JLabel(), g, 0, 0);
    g.dispose();
    return image;
  }

  public static final String getImageToolTip(Image image) {
    if (image instanceof ToolTipImage) {
      return ((ToolTipImage) image).toolTipText;
    }
    return "";
  }

  public static Icon createDisabledIcon(Icon icon) {
    Parameters.notNull("icon", icon);
    return new LazyDisabledIcon(ImageUtilities.icon2Image(icon));
  }

  public static Image createDisabledImage(Image image) {
    Parameters.notNull("image", image);
    return LazyDisabledIcon.createDisabledImage(image);
  }

  static final Image toBufferedImage(Image img) {
    new ImageIcon(img, "");
    if (img.getHeight(null) * img.getWidth(null) > 576) {
      return img;
    }
    BufferedImage rep = ImageUtilities.createBufferedImage(img.getWidth(null), img.getHeight(null));
    Graphics2D g = rep.createGraphics();
    g.drawImage(img, 0, 0, null);
    g.dispose();
    img.flush();
    return rep;
  }

  /*
   * WARNING - Removed try catching itself - possible behaviour change.
   */
  private static void ensureLoaded(Image image) {
    if ((Toolkit.getDefaultToolkit().checkImage(image, -1, -1, null) & 48) != 0) {
      return;
    }
    MediaTracker mediaTracker = tracker;
    synchronized (mediaTracker) {
      int id = ++mediaTrackerID;
      tracker.addImage(image, id);
      try {
        tracker.waitForID(id, 0);
      } catch (InterruptedException e) {
        System.out.println("INTERRUPTED while loading Image");
      }
      assert (tracker.statusID(id, false) == 8);
      tracker.removeImage(image, id);
    }
  }

  private static final Image doMergeImages(Image image1, Image image2, int x, int y) {
    ImageUtilities.ensureLoaded(image1);
    ImageUtilities.ensureLoaded(image2);
    int w = Math.max(image1.getWidth(null), x + image2.getWidth(null));
    int h = Math.max(image1.getHeight(null), y + image2.getHeight(null));
    boolean bitmask =
        image1 instanceof Transparency && ((Transparency) ((Object) image1)).getTransparency() != 3
            && image2 instanceof Transparency && ((Transparency) ((Object) image2)).getTransparency() != 3;
    StringBuilder str = new StringBuilder(image1 instanceof ToolTipImage ? ((ToolTipImage) image1).toolTipText : "");
    if (image2 instanceof ToolTipImage) {
      String toolTip = ((ToolTipImage) image2).toolTipText;
      if (str.length() > 0 && toolTip.length() > 0) {
        str.append("<br>");
      }
      str.append(toolTip);
    }
    ColorModel model = ImageUtilities.colorModel(bitmask ? 2 : 3);
    ToolTipImage buffImage =
        new ToolTipImage(str.toString(), model, model.createCompatibleWritableRaster(w, h),
            model.isAlphaPremultiplied(), null, null);
    Graphics2D g = buffImage.createGraphics();
    g.drawImage(image1, 0, 0, null);
    g.drawImage(image2, x, y, null);
    g.dispose();
    return buffImage;
  }

  static final BufferedImage createBufferedImage(int width, int height) {
    if (Utilities.isMac()) {
      return new BufferedImage(width, height, 3);
    }
    ColorModel model = ImageUtilities.colorModel(3);
    BufferedImage buffImage =
        new BufferedImage(model, model.createCompatibleWritableRaster(width, height), model.isAlphaPremultiplied(),
            null);
    return buffImage;
  }

  private static ColorModel colorModel(int transparency) {
    ColorModel model;
    try {
      model =
          GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
              .getColorModel(transparency);
    } catch (HeadlessException he) {
      model = ColorModel.getRGBdefault();
    }
    return model;
  }

  static {
    noLoaderWarned = false;
    component = new Component() {};
    tracker = new MediaTracker(component);
    ERR = Logger.getLogger(ImageUtilities.class.getName());
    ImageIO.setUseCache(false);
    PNG_READER = ImageIO.getImageReadersByMIMEType("image/png").next();
  }

  private static class CompositeImageKey {
    Image baseImage;
    Image overlayImage;
    int x;
    int y;

    CompositeImageKey(Image base, Image overlay, int x, int y) {
      this.x = x;
      this.y = y;
      this.baseImage = base;
      this.overlayImage = overlay;
    }

    public boolean equals(Object other) {
      if (!(other instanceof CompositeImageKey)) {
        return false;
      }
      CompositeImageKey k = (CompositeImageKey) other;
      return this.x == k.x && this.y == k.y && this.baseImage == k.baseImage && this.overlayImage == k.overlayImage;
    }

    public int hashCode() {
      int hash = (this.x << 3 ^ this.y) << 4;
      hash = hash ^ this.baseImage.hashCode() ^ this.overlayImage.hashCode();
      return hash;
    }

    public String toString() {
      return "Composite key for " + this.baseImage + " + " + this.overlayImage + " at [" + this.x + ", " + this.y + "]";
    }
  }

  private static class DisabledButtonFilter extends RGBImageFilter {
    DisabledButtonFilter() {
      this.canFilterIndexColorModel = true;
    }

    @Override
    public int filterRGB(int x, int y, int rgb) {
      return (rgb & -16777216) + 8947848 + ((rgb >> 16 & 255) >> 2 << 16) + ((rgb >> 8 & 255) >> 2 << 8)
          + ((rgb & 255) >> 2);
    }

    public void setProperties(Hashtable props) {
      props = (Hashtable) props.clone();
      this.consumer.setProperties(props);
    }
  }

  private static class LazyDisabledIcon implements Icon {
    private static final RGBImageFilter DISABLED_BUTTON_FILTER = new DisabledButtonFilter();
    private Image img;
    private Icon disabledIcon;

    public LazyDisabledIcon(Image img) {
      assert (null != img);
      this.img = img;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      this.getDisabledIcon().paintIcon(c, g, x, y);
    }

    @Override
    public int getIconWidth() {
      return this.getDisabledIcon().getIconWidth();
    }

    @Override
    public int getIconHeight() {
      return this.getDisabledIcon().getIconHeight();
    }

    private synchronized Icon getDisabledIcon() {
      if (null == this.disabledIcon) {
        this.disabledIcon = new ImageIcon(LazyDisabledIcon.createDisabledImage(this.img));
      }
      return this.disabledIcon;
    }

    static Image createDisabledImage(Image img) {
      FilteredImageSource prod = new FilteredImageSource(img.getSource(), DISABLED_BUTTON_FILTER);
      return Toolkit.getDefaultToolkit().createImage(prod);
    }
  }

  private static class ToolTipImage extends BufferedImage implements Icon {
    final String toolTipText;
    ImageIcon imageIcon;
    final URL url;

    public static ToolTipImage createNew(String toolTipText, Image image, URL url) {
      ImageUtilities.ensureLoaded(image);
      boolean bitmask = image instanceof Transparency && ((Transparency) ((Object) image)).getTransparency() != 3;
      ColorModel model = ImageUtilities.colorModel(bitmask ? 2 : 3);
      int w = image.getWidth(null);
      int h = image.getHeight(null);
      ToolTipImage newImage =
          new ToolTipImage(toolTipText, model, model.createCompatibleWritableRaster(w, h),
              model.isAlphaPremultiplied(), null, url);
      Graphics2D g = newImage.createGraphics();
      g.drawImage(image, 0, 0, null);
      g.dispose();
      return newImage;
    }

    public ToolTipImage(String toolTipText, ColorModel cm, WritableRaster raster, boolean isRasterPremultiplied,
        Hashtable<?, ?> properties, URL url) {
      super(cm, raster, isRasterPremultiplied, properties);
      this.toolTipText = toolTipText;
      this.url = url;
    }

    public ToolTipImage(String toolTipText, int width, int height, int imageType) {
      super(width, height, imageType);
      this.toolTipText = toolTipText;
      this.url = null;
    }

    synchronized ImageIcon getIcon() {
      if (this.imageIcon == null) {
        this.imageIcon = new ImageIcon(this);
      }
      return this.imageIcon;
    }

    @Override
    public int getIconHeight() {
      return super.getHeight();
    }

    @Override
    public int getIconWidth() {
      return super.getWidth();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.drawImage(this, x, y, null);
    }

    @Override
    public Object getProperty(String name, ImageObserver observer) {
      if ("url".equals(name)) {
        if (this.url != null) {
          return this.url;
        }
        return this.imageIcon.getImage().getProperty("url", observer);
      }
      return super.getProperty(name, observer);
    }
  }

  private static class ToolTipImageKey {
    Image image;
    String str;

    ToolTipImageKey(Image image, String str) {
      this.image = image;
      this.str = str;
    }

    public boolean equals(Object other) {
      if (!(other instanceof ToolTipImageKey)) {
        return false;
      }
      ToolTipImageKey k = (ToolTipImageKey) other;
      return this.str.equals(k.str) && this.image == k.image;
    }

    public int hashCode() {
      int hash = this.image.hashCode() ^ this.str.hashCode();
      return hash;
    }

    public String toString() {
      return "ImageStringKey for " + this.image + " + " + this.str;
    }
  }

}
