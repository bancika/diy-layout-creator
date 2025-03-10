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
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Random;
import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;

import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.transform.ImageTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BinaryType;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ByteArrayProperty;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PercentEditor;

@ComponentDescriptor(name = "SVG Image", author = "Branislav Stojkovic", category = "Misc",
    description = "Scalable Vector Graphics", instanceNamePrefix = "Svg",
    zOrder = IDIYComponent.COMPONENT, flexibleZOrder = true, bomPolicy = BomPolicy.NEVER_SHOW,
    transformer = ImageTransformer.class)
public class SVGImage extends AbstractTransparentComponent<Void> {

  private static final Logger LOG = Logger.getLogger(SVGImage.class);

  private static final long serialVersionUID = 1L;
  public static String DEFAULT_TEXT = "Double click to edit text";
  private static byte DEFAULT_SCALE = 25;

  private Point2D.Double point = new Point2D.Double(0, 0);
  private Point2D.Double secondPoint = null;

  private Orientation orientation;
  
  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }

  private byte[] data;
  private Byte scale;
  private byte newScale = DEFAULT_SCALE;
  private ImageSizingMode sizingMode = ImageSizingMode.Scale;
  private transient SVGDiagram svgDiagram;

  public SVGImage() {
    try {
      data = IOUtils.toByteArray(SVGImage.class.getResourceAsStream("diylc.svg"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    double scaleX;
    double scaleY;
    SVGDiagram svgDiagram = getSvgDiagram();
    if (getSizingMode() == ImageSizingMode.Scale) {
      scaleX = scaleY = 1d * getScale() / DEFAULT_SCALE;
    } else {
      Point2D secondPoint = getControlPoint(1);
      scaleX = 1d * Math.abs(point.getX() - secondPoint.getX()) / svgDiagram.getWidth();
      scaleY = 1d * Math.abs(point.getY() - secondPoint.getY()) / svgDiagram.getHeight();
    }

    Shape clip = g2d.getClip().getBounds();
    if (!clip.intersects(new Rectangle2D.Double(point.getX(), point.getY(),
        svgDiagram.getWidth() * scaleX, svgDiagram.getHeight() * scaleY))) {
      return;
    }
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }

    double x;
    double y;
    if (getSizingMode() == ImageSizingMode.Scale) {
      x = point.getX();
      y = point.getY();
    } else {
      Point2D secondPoint = getControlPoint(1);
      x = Math.min(point.getX(), secondPoint.getX());
      y = Math.min(point.getY(), secondPoint.getY());
    }

    if (getOrientation() != Orientation.DEFAULT) {
      double theta = getOrientation().toRadians();
      g2d.rotate(theta, x, y);
    }

    g2d.scale(scaleX, scaleY);

    g2d.translate(x / scaleX, y / scaleY);

    try {
      svgDiagram.render(g2d);
    } catch (SVGException e) {
      LOG.error("Error rendering SVG", e);
    }

    if (componentState == ComponentState.SELECTED) {
      g2d.translate(-x / scaleX, -y / scaleY);
      g2d.setComposite(oldComposite);
      g2d.scale(1 / scaleX, 1 / scaleY);
      g2d.setColor(SELECTION_COLOR);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
      g2d.drawRect((int) x, (int) y, (int) (svgDiagram.getWidth() * scaleX),
          (int) (svgDiagram.getHeight() * scaleY));
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int factor = 32 / width;
    g2d.setColor(Color.decode("#91D4FF"));
    g2d.fillOval(2 / factor, 2 / factor, width / 2 + 2 / factor, height / 2 + 2 / factor);
    g2d.setColor(Color.decode("#91D4FF").darker());
    g2d.drawOval(2 / factor, 2 / factor, width / 2 + 2 / factor, height / 2 + 2 / factor);
    
    g2d.setColor(Color.decode("#A2D383"));
    g2d.fillRect(width / 2 - 2 / factor, height / 2 - 2 / factor, width / 2, height / 2);
    g2d.setColor(Color.decode("#A2D383").darker());
    g2d.drawRect(width / 2 - 2 / factor, height / 2 - 2 / factor, width / 2, height / 2);
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
      SVGDiagram svgDiagram = getSvgDiagram();
      secondPoint = new Point2D.Double(point.getX() + svgDiagram.getWidth(),
          point.getY() + svgDiagram.getHeight());
    }
    return secondPoint;
  }

  private SVGDiagram getSvgDiagram() {
    if (svgDiagram == null) {
      ByteArrayInputStream bis = new ByteArrayInputStream(data);
      SVGUniverse universe = new SVGUniverse();
      try {
        URI url = universe.loadSVG(bis, "SVG-" + Integer.toHexString(new Random(System.currentTimeMillis()).nextInt()));
        svgDiagram = universe.getDiagram(url);  
        svgDiagram.setIgnoringClipHeuristic(true);
      } catch (IOException e) {
        LOG.error("Error loading SVG", e);
      }
    }
    return svgDiagram;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return false;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return getSizingMode() == ImageSizingMode.Scale ? VisibilityPolicy.NEVER
        : VisibilityPolicy.WHEN_SELECTED;
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

  @ByteArrayProperty(binaryType = BinaryType.SVG)
  @EditableProperty(name = "Image")
  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
    this.svgDiagram = null;
  }

  @PercentEditor(_100PercentValue = 25)
  @EditableProperty(defaultable = false)
  public byte getScale() {
    if (scale != null) {
      newScale = (byte) (scale / 2);
      scale = null;
    }
    return newScale;
  }

  public void setScale(byte scale) {
    this.newScale = scale;
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
