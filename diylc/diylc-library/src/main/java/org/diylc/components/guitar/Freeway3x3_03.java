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
package org.diylc.components.guitar;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.diylc.appframework.miscutils.ConfigurationManager;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.ShapeElement;

import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.transform.Freeway3x3_03Transformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.ISwitch;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Freeway 3X3-03 Toggle", category = "Guitar",
    author = "Branislav Stojkovic", description = "Freeway 3X3-03 Toggle Switch",
    zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "SW",
    keywordPolicy = KeywordPolicy.SHOW_TAG, keywordTag = "Guitar Wiring Diagram",
    transformer = Freeway3x3_03Transformer.class, enableCache = true)
public class Freeway3x3_03 extends AbstractTransparentComponent<Void> implements ISwitch {

  private static final Logger LOG = Logger.getLogger(Freeway3x3_03.class);

  private static final long serialVersionUID = 1L;

  private static Color BASE_COLOR = FR4_COLOR;// Color.DARK_GRAY;
  private static Color PAD_COLOR = COPPER_COLOR;
  private static Color LABEL_COLOR = Color.WHITE;
  private static Color CASE_COLOR = METAL_COLOR;

  private static final double[] X_OFFSETS =
      new double[] {9, 15, 21, 21, 21, 21, 21, 21, 12, 3, 3, 3, 3, 3, 3};
  private static final double[] Y_OFFSETS =
      new double[] {3, 3, 5, 9.5, 14, 18.5, 23, 27.5, 30, 27.5, 23, 18.5, 14, 9.5, 5};

  private static final String[] PAD_NAMES = new String[] {"CA", "CB", "B-B1", "B-B2", "B-M1",
      "B-M2", "B-N2", "B-N1", "GND", "A-12", "A-N2", "A-M2", "A-M1", "A-B2", "A-B1"};

  private static final AffineTransform SCALE_TX = AffineTransform.getScaleInstance(0.615d, 0.615d);

  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};

  private Orientation orientation = Orientation.DEFAULT;

  private transient SVGDiagram svgDiagram;
  private transient List<Shape> pads;
  private transient List<Shape> labels;
  private transient Shape base;
  private transient Shape caseShape;
  private transient double[] xOffsetsPx;
  private transient double[] yOffsetsPx;

  public Freeway3x3_03() {
    super();
    updateControlPoints();
  }

  @Override
  public String getControlPointNodeName(int index) {
    return PAD_NAMES[index];
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {

    Shape base = getBase();
    Rectangle2D bounds2d = base.getBounds2D();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    Point2D point = getControlPoint(0);

    double x = point.getX();
    double y = point.getY();

    double[] xOffsets = getXOffsetsPx();
    double[] yOffsets = getYOffsetsPx();



    Rectangle2D rect = new Rectangle2D.Double(x - xOffsets[0], y - yOffsets[0], bounds2d.getWidth(),
        bounds2d.getHeight());

    if (getOrientation() != Orientation.DEFAULT) {
      double theta = orientation.toRadians();
      AffineTransform tx = AffineTransform.getRotateInstance(theta, x, y);
      rect = tx.createTransformedShape(rect).getBounds2D();
    }

    Shape clip = g2d.getClip().getBounds();
    if (!clip.intersects(rect)) {
      return;
    }

    List<Shape> pads = getPads();
    List<Shape> labels = getLabels();
    Shape caseShape = getCase();

    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }

    if (getOrientation() != Orientation.DEFAULT) {
      double theta = orientation.toRadians();
      g2d.rotate(theta, x, y);
    }

    g2d.translate(x - xOffsets[0], y - yOffsets[0]);

    Color finalBorderColor;
    if (outlineMode) {
      Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
          Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
              ? SELECTION_COLOR
              : BASE_COLOR.darker();
    }

    drawingObserver.startTracking();
    g2d.setColor(BASE_COLOR);
    g2d.fill(base);
    drawingObserver.stopTracking();
    g2d.setColor(finalBorderColor);
    g2d.draw(base);

    drawingObserver.startTrackingContinuityArea(true);
    g2d.setColor(PAD_COLOR);
    for (Shape pad : pads) {
      g2d.fill(pad);
    }
    drawingObserver.stopTrackingContinuityArea();

    g2d.setColor(PAD_COLOR.darker());
    for (Shape pad : pads) {
      g2d.draw(pad);
    }

    g2d.setColor(LABEL_COLOR);
    for (Shape l : labels) {
      g2d.fill(l);
    }

    g2d.setColor(CASE_COLOR);
    g2d.fill(caseShape);

    g2d.setColor(CASE_COLOR.darker());
    g2d.draw(caseShape);

    g2d.setComposite(oldComposite);
  }

  public double[] getXOffsetsPx() {
    if (xOffsetsPx == null) {
      xOffsetsPx = new double[X_OFFSETS.length];
      for (int i = 0; i < X_OFFSETS.length; i++) {
        xOffsetsPx[i] = new Size(X_OFFSETS[i], SizeUnit.mm).convertToPixels();
      }
    }
    return xOffsetsPx;
  }

  public double[] getYOffsetsPx() {
    if (yOffsetsPx == null) {
      yOffsetsPx = new double[Y_OFFSETS.length];
      for (int i = 0; i < Y_OFFSETS.length; i++) {
        yOffsetsPx[i] = new Size(Y_OFFSETS[i], SizeUnit.mm).convertToPixels();
      }
    }
    return yOffsetsPx;
  }

  private transient BufferedImage iconImage;

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    if (iconImage == null) {
      iconImage = new BufferedImage(width - 1, height - 1, BufferedImage.TYPE_INT_ARGB);
      Graphics2D iconG2d = iconImage.createGraphics();
      iconG2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      iconG2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      Shape base = getBase();
      List<Shape> pads = getPads();
      List<Shape> labels = getLabels();
      double scale = 1d * width / base.getBounds2D().getWidth() * 2.5;
      iconG2d.scale(scale, scale);
      iconG2d.setColor(BASE_COLOR);
      iconG2d.fill(base);
      iconG2d.setColor(BASE_COLOR.darker());
      iconG2d.draw(base);
      iconG2d.setColor(PAD_COLOR);
      pads.stream().forEach(p -> iconG2d.fill(p));
      iconG2d.setColor(LABEL_COLOR);
      labels.stream().forEach(l -> iconG2d.fill(l));
      iconG2d.setColor(CASE_COLOR);
      iconG2d.fill(getCase());
    }
    g2d.drawImage(iconImage, null, null);    
  }

  @Override
  public int getControlPointCount() {
    return X_OFFSETS.length;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return controlPoints[index];
  }

  private void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    controlPoints = new Point2D[X_OFFSETS.length];
    controlPoints[0] = firstPoint;
    double[] xOffsetsPx = getXOffsetsPx();
    double[] yOffsetsPx = getYOffsetsPx();
    // Update control points.
    double theta = orientation.toRadians();
    AffineTransform tx =
        AffineTransform.getRotateInstance(theta, firstPoint.getX(), firstPoint.getY());
    for (int i = 1; i < controlPoints.length; i++) {
      controlPoints[i] = new Point2D.Double(firstPoint.getX() + (xOffsetsPx[i] - xOffsetsPx[0]),
          firstPoint.getY() + (yOffsetsPx[i] - yOffsetsPx[0]));
      tx.transform(controlPoints[i], controlPoints[i]);
    }
  }

  public SVGDiagram getSvgDiagram() {
    if (svgDiagram == null) {
      try {
        byte[] data =
            IOUtils.toByteArray(Freeway3x3_03.class.getResourceAsStream("/freeway_3x3-03.svg"));
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        SVGUniverse universe = new SVGUniverse();
        URI url = universe.loadSVG(bis,
            "SVG-" + Integer.toHexString(new Random(System.currentTimeMillis()).nextInt()));
        svgDiagram = universe.getDiagram(url);
        svgDiagram.setIgnoringClipHeuristic(true);
      } catch (IOException e) {
        LOG.error("Error loading SVG", e);
      }
    }
    return svgDiagram;
  }

  public List<Shape> getPads() {
    if (pads == null) {
      SVGDiagram svg = getSvgDiagram();
      com.kitfox.svg.Group group = (com.kitfox.svg.Group) svg.getElement("pads");
      pads = new ArrayList<Shape>();
      for (int i = 0; i < group.getNumChildren(); i++) {
        SVGElement child = group.getChild(i);
        if (child instanceof ShapeElement) {
          ShapeElement shapeElement = (ShapeElement) child;
          pads.add(SCALE_TX.createTransformedShape(shapeElement.getShape()));
        }
      }
    }
    return pads;
  }

  public List<Shape> getLabels() {
    if (labels == null) {
      SVGDiagram svg = getSvgDiagram();
      com.kitfox.svg.Group group = (com.kitfox.svg.Group) svg.getElement("labels");
      labels = new ArrayList<Shape>();
      for (int i = 0; i < group.getNumChildren(); i++) {
        SVGElement child = group.getChild(i);
        if (child instanceof ShapeElement) {
          ShapeElement shapeElement = (ShapeElement) child;
          labels.add(SCALE_TX.createTransformedShape(shapeElement.getShape()));
        }
      }
    }
    return labels;
  }

  public Shape getCase() {
    if (caseShape == null) {
      SVGDiagram svg = getSvgDiagram();
      ShapeElement shapeElement = (ShapeElement) svg.getElement("case");
      caseShape = SCALE_TX.createTransformedShape(shapeElement.getShape());
    }
    return caseShape;
  }

  public Shape getBase() {
    if (base == null) {
      SVGDiagram svg = getSvgDiagram();
      ShapeElement shapeElement = (ShapeElement) svg.getElement("base");
      base = SCALE_TX.createTransformedShape(shapeElement.getShape());
    }
    return base;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.ALWAYS;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @EditableProperty
  public Orientation getOrientation() {
    if (orientation == null)
      orientation = Orientation.DEFAULT;
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
  }

  @Override
  public Rectangle2D getCachingBounds() {
    Shape base = getBase();
    Rectangle2D bounds2d = base.getBounds2D();
    Point2D point = getControlPoint(0);

    double x = point.getX();
    double y = point.getY();

    double theta = orientation.toRadians();
    AffineTransform tx = AffineTransform.getRotateInstance(theta, x, y);

    double[] xOffsets = getXOffsetsPx();
    double[] yOffsets = getYOffsetsPx();

    Rectangle2D rect = new Rectangle2D.Double(x - xOffsets[0] - 1, y - yOffsets[0] - 1,
        bounds2d.getWidth() + 2, bounds2d.getHeight() + 2);
    return tx.createTransformedShape(rect).getBounds2D();
  }

  @Override
  public int getPositionCount() {
    return 6;
  }

  @Override
  public String getPositionName(int position) {
    return Integer.toString(position + 1);
  }

  @Override
  public boolean arePointsConnected(int index1, int index2, int position) {
    if (position == 0 && ((index1 == 0 && index2 == 9) || (index1 == 1 && index2 == 7))) {
      return true;
    }
    if (position == 1 && ((index1 == 0 && (index2 == 9 || index2 == 14))
        || (index1 == 1 && (index2 == 2 || index2 == 7)))) {
      return true;
    }
    if (position == 2 && ((index1 == 0 && index2 == 14) || (index1 == 1 && index2 == 2))) {
      return true;
    }
    if (position == 3
        && ((index1 == 0 && (index2 == 9 || index2 == 10)) || (index1 == 1 && index2 == 6))) {
      return true;
    }
    if (position == 4 && ((index1 == 0 && (index2 == 11 || index2 == 12))
        || (index1 == 1 && (index2 == 4 || index2 == 5)))) {
      return true;
    }
    if (position == 5
        && ((index1 == 0 && (index2 == 13 || index2 == 14)) || (index1 == 1 && index2 == 3))) {
      return true;
    }
    return false;
  }
}
