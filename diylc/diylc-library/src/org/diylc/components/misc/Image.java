/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
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
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.poi.util.IOUtils;
import org.diylc.appframework.miscutils.IconImageConverter;
import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PercentEditor;

import sun.awt.image.ToolkitImage;

import com.thoughtworks.xstream.annotations.XStreamConverter;


@ComponentDescriptor(name = "Image", author = "Branislav Stojkovic", category = "Misc",
    description = "User defined image", instanceNamePrefix = "Img", zOrder = IDIYComponent.COMPONENT,
    flexibleZOrder = true, stretchable = false, bomPolicy = BomPolicy.NEVER_SHOW)
public class Image extends AbstractTransparentComponent<Void> {

  private static final long serialVersionUID = 1L;
  public static String DEFAULT_TEXT = "Double click to edit text";
  private static ImageIcon ICON;
  private static byte DEFAULT_SCALE = 50;

  static {
    String name = "image.png";
    java.net.URL imgURL = Image.class.getResource(name);
    if (imgURL != null) {
      ICON = new ImageIcon(imgURL, name);
    }
  }

  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }

  private Point point = new Point(0, 0);

  @XStreamConverter(IconImageConverter.class)
  @Deprecated
  private ImageIcon image;
  private byte[] data;
  private byte scale = DEFAULT_SCALE;

  public Image() {
    try {
      data = IOUtils.toByteArray(Image.class.getResourceAsStream("image.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    double s = 1d * scale / DEFAULT_SCALE;
    Shape clip = g2d.getClip().getBounds();
    if (!clip.intersects(new Rectangle2D.Double(point.getX(), point.getY(), getImage().getIconWidth() * s, getImage()
        .getIconHeight() * s))) {
      return;
    }
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }

    g2d.scale(s, s);
    g2d.drawImage(getImage().getImage(), (int) (point.x / s), (int) (point.y / s), null);
    if (componentState == ComponentState.SELECTED) {
      g2d.setComposite(oldComposite);
      g2d.scale(1 / s, 1 / s);
      g2d.setColor(SELECTION_COLOR);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
      g2d.drawRect(point.x, point.y, (int) (getImage().getIconWidth() * s), (int) (getImage().getIconHeight() * s));
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.drawImage(ICON.getImage(), point.x, point.y, null);
  }

  @Override
  public int getControlPointCount() {
    return 1;
  }

  @Override
  public Point getControlPoint(int index) {
    return point;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return false;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public void setControlPoint(Point point, int index) {
    this.point.setLocation(point);
  }

  public ImageIcon getImage() {
    if (image != null) {
      // when loading old files, convert the stored image to byte array and then then discard it, we won't be needing it anymore
      BufferedImage bi = ((ToolkitImage) image.getImage()).getBufferedImage();
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

    return new ImageIcon(data);
  }

  @EditableProperty(name = "Image")
  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
    this.image = null;
  }

  @PercentEditor(_100PercentValue = 50)
  @EditableProperty(defaultable = false)
  public byte getScale() {
    return scale;
  }

  public void setScale(byte scale) {
    this.scale = scale;
  }

  @Override
  public String getName() {
    return super.getName();
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}
}
