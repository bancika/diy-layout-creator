/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.components.misc;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.diylc.appframework.miscutils.IconImageConverter;

import com.thoughtworks.xstream.annotations.XStreamConverter;

import org.diylc.awt.ImageUtils;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.Percentage;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.transform.ImageTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.*;

@ComponentDescriptor(name = "Image", author = "Branislav Stojkovic", category = "Misc",
    description = "User defined image", instanceNamePrefix = "Img", zOrder = IDIYComponent.COMPONENT,
    flexibleZOrder = true, bomPolicy = BomPolicy.NEVER_SHOW, transformer = ImageTransformer.class)
public class Image extends AbstractTransparentComponent<Void> {

  private static final Logger LOG = Logger.getLogger(Image.class);

  private static final long serialVersionUID = 1L;
  public static String DEFAULT_TEXT = "Double click to edit text";
  private static ImageIcon ICON;
  
  private Point2D.Double point = new Point2D.Double(0, 0);
  private Point2D.Double secondPoint = null;
  
  private Orientation orientation;

  static {
    String name = "/diylc-library-images/image.png";
      try (InputStream inputStream = Image.class.getResourceAsStream(name)) {
          if (inputStream != null) {
            ICON = new ImageIcon(inputStream.readAllBytes(), name);
          }
      } catch (IOException e) {
        LOG.error("Error loading image " + name, e);
      }
  }

  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }  

  @XStreamConverter(IconImageConverter.class)
  @Deprecated
  private ImageIcon image;
  private byte[] data;
  @Deprecated
  private Byte scale;
  @Deprecated
  private Byte newScale;
  private Percentage scalePercent = new Percentage(100);
  private ImageSizingMode sizingMode = ImageSizingMode.Scale;

  private transient ImageIcon imageIcon;

  public Image() {
    try {
      data = IOUtils.toByteArray(Image.class.getResourceAsStream("/diylc-library-images/image.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    ImageIcon imageIcon = getImage();
    if (imageIcon == null || imageIcon.getImage() == null || imageIcon.getIconWidth() <= 0
        || imageIcon.getIconHeight() <= 0) {
      return;
    }

    Shape clip = g2d.getClip();
    if (clip != null && !clip.intersects(getImageRectangle())) {
      return;
    }

    double scaleX;
    double scaleY;
    double x;
    double y;
    if (getSizingMode() == ImageSizingMode.Scale) {
      scaleX = scaleY = 1d * getScale().getValue() / 100d;
      x = point.getX();
      y = point.getY();
    } else {
      Point2D secondPoint = getControlPoint(1);
      scaleX = 1d * Math.abs(point.getX() - secondPoint.getX()) / imageIcon.getIconWidth();
      scaleY = 1d * Math.abs(point.getY() - secondPoint.getY()) / imageIcon.getIconHeight();
      x = Math.min(point.getX(), secondPoint.getX());
      y = Math.min(point.getY(), secondPoint.getY());
    }

    Composite oldComposite = applyAlpha(g2d, componentState);
    AffineTransform oldTx = g2d.getTransform();

    if (getOrientation() != Orientation.DEFAULT) {
      double theta = getOrientation().toRadians();
      g2d.rotate(theta, x, y);
    }

    g2d.scale(scaleX, scaleY);

    g2d.drawImage(imageIcon.getImage(), (int) Math.round(x / scaleX), (int) Math.round(y / scaleY), null);
    if (componentState == ComponentState.SELECTED) {
      g2d.setComposite(oldComposite);
      g2d.scale(1 / scaleX, 1 / scaleY);
      g2d.setColor(SELECTION_COLOR);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
      g2d.drawRect((int) Math.round(x), (int) Math.round(y),
          (int) Math.round(imageIcon.getIconWidth() * scaleX),
          (int) Math.round(imageIcon.getIconHeight() * scaleY));
    }

    g2d.setTransform(oldTx);
    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.drawImage(ICON.getImage(), (int) Math.round(point.getX()), (int) Math.round(point.getY()), null);
  }

  @Override
  public int getControlPointCount() {
    return getSizingMode() == ImageSizingMode.Scale ? 1 : 2;
  }

  @Override
  public Point2D getControlPoint(int index) {
    if (index == 0)
      return point;
    if (secondPoint == null) {
      ImageIcon imageIcon = getImage();
      secondPoint = new Point2D.Double(point.getX() + imageIcon.getIconWidth(), point.getY() + imageIcon.getIconHeight());
    }
    return secondPoint;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return false;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return getSizingMode() == ImageSizingMode.Scale ? VisibilityPolicy.NEVER : VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    if (index == 0) {
      this.point.setLocation(point.getX(), point.getY());
    } else {
      if (secondPoint == null)
        secondPoint = new Point2D.Double(point.getX(), point.getY());
      else
        secondPoint.setLocation(point.getX(), point.getY());
    }
  }

  public ImageIcon getImage() {
    if (imageIcon == null) {
      if (image != null) {
        // when loading old files, convert the stored image to byte array and then then discard it, we won't be needing it anymore
        BufferedImage bi = ImageUtils.ToBufferedImage(image.getImage());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
          ImageIO.write(bi, "png", baos);
          // make it official
          data = baos.toByteArray();
        } catch (IOException e) {
        }
        // don't save back to the file
        image = null;
      }

      imageIcon = new ImageIcon(data);
    }
    return imageIcon;
  }

  @ByteArrayProperty(binaryType = BinaryType.IMAGE)
  @EditableProperty(name = "Image")
  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
    this.imageIcon = null;
    this.image = null;
    if (getSizingMode() == ImageSizingMode.TwoPoints) {
      ImageIcon imageIcon = getImage();
      setControlPoint(new Point2D.Double(point.getX() + imageIcon.getIconWidth(), point.getY() + imageIcon.getIconHeight()), 1);
    }
  }

  @PercentEditor(minValue = 1, maxValue = 1000)
  @EditableProperty(name = "Scale", defaultable = false)
  public Percentage getScale() {
    if (scalePercent == null) {
      if (newScale != null) {
        scalePercent = new Percentage(newScale * 4);
        newScale = null;
      } else if (scale != null) {
        scalePercent = new Percentage(scale * 2);
        scale = null;
      } else {
        scalePercent = new Percentage(100);
      }
    }
    return scalePercent;
  }

  public void setScale(Percentage scale) {
    this.scalePercent = scale;
  }

  @Override
  public String getName() {
    return super.getName();
  }

  @Override
  public Void getValue() {
    return null;
  }
  
  @EditableProperty(name = "Sizing Mode")
  public ImageSizingMode getSizingMode() {
    if (sizingMode == null)
      sizingMode = ImageSizingMode.Scale;
    return sizingMode;
  }
  
  public void setSizingMode(ImageSizingMode sizingMode) {
    this.sizingMode = sizingMode;
  }

  @Override
  public void setValue(Void value) {}
  
  @EditableProperty
  public Orientation getOrientation() {
    if (orientation == null)
      orientation = Orientation.DEFAULT;
    return orientation;
  }
  
  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
  }

  protected Point2D getFinalSecondPoint() {
    Point2D finalSecondPoint;
    if (getSizingMode() == ImageSizingMode.TwoPoints) {
      finalSecondPoint = getControlPoint(1);
    } else {
      double scale = 1d * getScale().getValue() / 100d;
      ImageIcon iconImage = getImage();
      double w = (iconImage != null && iconImage.getIconWidth() > 0) ? iconImage.getIconWidth() * scale : 0;
      double h = (iconImage != null && iconImage.getIconHeight() > 0) ? iconImage.getIconHeight() * scale : 0;
      finalSecondPoint = new Point2D.Double(point.getX() + w, point.getY() + h);
    }
    return finalSecondPoint;
  }

  protected Rectangle2D getImageRectangle() {
    Point2D finalSecondPoint = getFinalSecondPoint();
    double x = Math.min(point.getX(), finalSecondPoint.getX());
    double y = Math.min(point.getY(), finalSecondPoint.getY());
    double w = Math.abs(point.getX() - finalSecondPoint.getX());
    double h = Math.abs(point.getY() - finalSecondPoint.getY());
    Rectangle2D rect = new Rectangle2D.Double(x, y, w, h);
    if (getOrientation() != null && getOrientation() != Orientation.DEFAULT) {
      AffineTransform tx = AffineTransform.getRotateInstance(getOrientation().toRadians(), x, y);
      return tx.createTransformedShape(rect).getBounds2D();
    }
    return rect;
  }

  @Override
  public Rectangle2D getCachingBounds() {
    return getImageRectangle();
  }

  public static enum ImageSizingMode {
    TwoPoints("Opposing Points"), Scale("Scale");
    
    private String label;

    private ImageSizingMode(String label) {
      this.label = label;
    }
    
    @Override
    public String toString() {
      return label;
    }
  }
}
