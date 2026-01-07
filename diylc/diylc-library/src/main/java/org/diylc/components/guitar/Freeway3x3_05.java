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
package org.diylc.components.guitar;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.ShapeElement;
import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.awt.StringUtils;
import org.diylc.common.*;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.transform.Freeway3x3_03Transformer;
import org.diylc.core.*;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.DynamicEditableProperty;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.diylc.core.measures.SizeUnit.px;

@ComponentDescriptor(name = "Freeway 3X3-05 (Ultra) Switch", category = "Guitar",
    author = "Martin Morrell", description = "Freeway 3X3-05 (Ultra) Toggle Switch",
    zOrder = IDIYComponent.COMPONENT, instanceNamePrefix = "SW",
    keywordPolicy = KeywordPolicy.SHOW_TAG, keywordTag = "Guitar Wiring Diagram",
    transformer = Freeway3x3_03Transformer.class, enableCache = false)
public class Freeway3x3_05 extends AbstractTransparentComponent<Void> implements ISwitch {

  private static final Logger LOG = Logger.getLogger(Freeway3x3_05.class);

  private static final long serialVersionUID = 1L;

  private static Color BASE_COLOR = FR4_COLOR;// Color.DARK_GRAY;
  private static Color PAD_COLOR = COPPER_COLOR;
  private static Color LABEL_COLOR = Color.WHITE;
  private static Color CASE_COLOR = METAL_COLOR;
  private static Color MARKER_COLOR = Color.lightGray;

  private static final double DIAMETER_SWITCH = 34.0;
  private static final double PAD_RADIUS = 15.3;
  private static final double PAD_SIZE = 1.5;
  private static final int NUM_CONTACTS = 28;
  private static final double CONTACT_SPACING_RAD = Math.PI * (360.0 / NUM_CONTACTS) / 180.0;
  private static final double CONTACT_SPACING_INITIAL = (CONTACT_SPACING_RAD * 14) + CONTACT_SPACING_RAD / 2;
  private static final double GROUND_DIAMETER = 20;
  private static final double CENTRE_LEN = 17;
  private static final double CENTRE_WID = 10;
  private static final double MARKER_OFFSET = 3.1;

  private static final String[] PAD_NAMES = new String[] {
          "1B", "2B", "3B", "6B", "5B", "4B", "CB", "TB", "6B", "5B", "4B", "1B", "2B", "3B",
          "6A", "5A", "4A", "1A", "2A", "3A", "TA", "CA", "1A", "2A", "3A", "6A", "5A", "4A"
  };

  private static final AffineTransform SCALE_TX = AffineTransform.getScaleInstance(0.615d, 0.615d);

  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};

  private Orientation orientation = Orientation.DEFAULT;

  private Integer selectedPosition;
  private Boolean showMarkers;

  private transient SVGDiagram svgDiagram;
  private transient List<Shape> pads;
  private transient List<Shape> labels;
  private transient Shape base;
  private transient Shape caseShape;
  private transient double[] xOffsetsPx;
  private transient double[] yOffsetsPx;

  public Freeway3x3_05() {
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

    double x = controlPoints[0].getX();
    double y = controlPoints[0].getY();

    if (getOrientation() != Orientation.DEFAULT) {
      double theta = orientation.toRadians();
      g2d.rotate(theta, x, y);
    }

    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }

    double offsetX = getXOffsetsPx()[0];
    double offsetY = getYOffsetsPx()[0];
    //double origin = DIAMETER_SWITCH / 2;

    int diameter = (int)(new Size(DIAMETER_SWITCH, SizeUnit.mm)).convertToPixels();

    double originX = x - offsetX + diameter / 2.0;
    double originY = y - offsetY + diameter / 2.0;

    //Main circular shape
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
    Shape base = new Ellipse2D.Double( originX, originY, diameter, diameter);
    drawingObserver.startTracking();
    g2d.setColor(BASE_COLOR);
    g2d.fill(base);
    drawingObserver.stopTracking();
    g2d.setStroke(new BasicStroke(1f));
    g2d.setColor(finalBorderColor);
    g2d.draw(base);

    //Ground circular shape
    int gndDiameter = (int)(new Size(GROUND_DIAMETER, SizeUnit.mm)).convertToPixels();
    int gndShift = (int)(new Size( (DIAMETER_SWITCH - GROUND_DIAMETER) / 2, SizeUnit.mm).convertToPixels());
    Shape gnd = new Ellipse2D.Double( originX + gndShift, originY + gndShift, gndDiameter, gndDiameter);
    g2d.setPaint(PAD_COLOR);
    g2d.fill(gnd);
    g2d.setColor(PAD_COLOR.darker());
    g2d.draw(gnd);

    //Centre rectangle casing block
    int rectLen = (int)(new Size(CENTRE_LEN, SizeUnit.mm)).convertToPixels();
    int rectWid = (int)(new Size(CENTRE_WID, SizeUnit.mm)).convertToPixels();

    int rectShiftX = (int)(new Size( (DIAMETER_SWITCH - CENTRE_WID) / 2, SizeUnit.mm).convertToPixels());
    int rectShiftY = (int)(new Size( (DIAMETER_SWITCH - CENTRE_LEN) / 2, SizeUnit.mm).convertToPixels());

    int rectangleArc = (int)(new Size(1.0, SizeUnit.mm)).convertToPixels();

    Shape casing = new RoundRectangle2D.Double(originX + rectShiftX, originY + rectShiftY, rectWid, rectLen, rectangleArc, rectangleArc);

    g2d.setPaint(CASE_COLOR);
    g2d.fill(casing);
    g2d.setColor(CASE_COLOR.darker());
    g2d.draw(casing);

    //Pads
    double circleShift = (int)(new Size( DIAMETER_SWITCH / 2, SizeUnit.mm).convertToPixels());
    double circleCentreX = originX + circleShift;
    double circleCentreY = originY + circleShift;
    double radChange = CONTACT_SPACING_RAD * 0.45;
    double extendedRadius = (int)new Size(PAD_RADIUS + PAD_SIZE, SizeUnit.mm).convertToPixels();
    double reducedRadius = (int)new Size(PAD_RADIUS - PAD_SIZE, SizeUnit.mm).convertToPixels();
    for (int i = 0; i < NUM_CONTACTS; i++) {
      double rad = ((i + 0.5) * CONTACT_SPACING_RAD);

      double rotExX = Math.cos(rad) * (extendedRadius * 1.01) + circleCentreX;
      double rotExY = Math.sin(rad) * (extendedRadius * 1.01) + circleCentreY;
      double rotExPosX = Math.cos(rad + radChange) * extendedRadius + circleCentreX;
      double rotExPosY = Math.sin(rad + radChange) * extendedRadius + circleCentreY;
      double rotExNegX = Math.cos(rad - radChange) * extendedRadius + circleCentreX;
      double rotExNegY = Math.sin(rad - radChange) * extendedRadius + circleCentreY;
      double rotReX = Math.cos(rad) * (reducedRadius * 1.01) + circleCentreX;
      double rotReY = Math.sin(rad) * (reducedRadius * 1.01) + circleCentreY;
      double rotRePosX = Math.cos(rad + radChange) * reducedRadius + circleCentreX;
      double rotRePosY = Math.sin(rad + radChange) * reducedRadius + circleCentreY;
      double rotReNegX = Math.cos(rad - radChange) * reducedRadius + circleCentreX;
      double rotReNegY = Math.sin(rad - radChange) * reducedRadius + circleCentreY;

      Path2D pad = new Path2D.Double();
      pad.moveTo(rotExNegX, rotExNegY);
      pad.quadTo(rotExX, rotExY, rotExPosX, rotExPosY);
      pad.lineTo(rotRePosX, rotRePosY);
      pad.quadTo(rotReX, rotReY,rotReNegX, rotReNegY);
      pad.lineTo(rotExNegX, rotExNegY);
      pad.closePath();

      g2d.setPaint(PAD_COLOR);
      g2d.fill(pad);
      g2d.setColor(PAD_COLOR.darker());
      g2d.draw(pad);
    }

    //Labels
    double offset = (int) new Size(MARKER_OFFSET, SizeUnit.mm).convertToPixels();
    for (int i = 0; i < NUM_CONTACTS; i++) {
      Point2D p = getControlPoint(i);

      double px = Math.sin(CONTACT_SPACING_INITIAL + i * CONTACT_SPACING_RAD) * (PAD_RADIUS - MARKER_OFFSET);
      double py = Math.cos(CONTACT_SPACING_INITIAL + i * CONTACT_SPACING_RAD) * (PAD_RADIUS - MARKER_OFFSET);

      g2d.setPaint(LABEL_COLOR);
      StringUtils.drawCenteredText(g2d, PAD_NAMES[i],
              (new Size(px + DIAMETER_SWITCH / 2.0, SizeUnit.mm).convertToPixels()) + originX,
              (new Size(py + DIAMETER_SWITCH / 2.0, SizeUnit.mm).convertToPixels()) + originY,
              HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }

    //Add other text
    int makeLen = (int)(new Size(12.0, SizeUnit.mm)).convertToPixels();
    int modelLen = (int)(new Size(8.0, SizeUnit.mm)).convertToPixels();
    double mWidth = 1.8;
    int modelWid = (int)(new Size(mWidth, SizeUnit.mm)).convertToPixels();

    int makeShiftX = (int)(new Size( (DIAMETER_SWITCH / 2) - 7.25, SizeUnit.mm).convertToPixels());
    int makeShiftY = (int)(new Size( (DIAMETER_SWITCH / 2) - 6.0, SizeUnit.mm).convertToPixels());
    int modelShiftX = (int)(new Size( (DIAMETER_SWITCH / 2) + 5.25, SizeUnit.mm).convertToPixels());
    int modelShiftY = (int)(new Size( (DIAMETER_SWITCH / 2) - 4.0, SizeUnit.mm).convertToPixels());

    int modelArc = (int)(new Size(mWidth, SizeUnit.mm)).convertToPixels();

    Shape make = new RoundRectangle2D.Double(originX + makeShiftX, originY + makeShiftY, modelWid, makeLen, modelArc, modelArc);
    Shape model = new RoundRectangle2D.Double(originX + modelShiftX, originY + modelShiftY, modelWid, modelLen, modelArc, modelArc);

    g2d.setPaint(BASE_COLOR);
    g2d.fill(make);
    g2d.fill(model);
    g2d.setColor(PAD_COLOR.darker());
    g2d.draw(make);
    g2d.draw(model);

    /*String makeTxt = "FREE-WAY·ULTRA";
    String modelTxt = "3X3-05";
    g2d.setPaint(LABEL_COLOR);
    StringUtils.drawCenteredText(g2d, makeTxt,
            (new Size(0.0, SizeUnit.mm).convertToPixels()) + originX + makeShiftX,
            (new Size(0.0, SizeUnit.mm).convertToPixels()) + originY + makeShiftY,
            HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    StringUtils.drawCenteredText(g2d, modelTxt,
            (new Size(0.0, SizeUnit.mm).convertToPixels()) + originX + modelShiftX,
            (new Size(0.0, SizeUnit.mm).convertToPixels()) + originY + modelShiftY,
            HorizontalAlignment.CENTER, VerticalAlignment.CENTER);*/

    if (getShowMarkers()) {
      g2d.setColor(MARKER_COLOR);

      for (int i = 0; i < getControlPointCount(); i++) {
        Point2D p = getControlPoint(i);
        Point2D labelPoint = new Point2D.Double(offset, 0);

        double px = Math.sin(CONTACT_SPACING_INITIAL + i * CONTACT_SPACING_RAD) * (PAD_RADIUS + MARKER_OFFSET);
        double py = Math.cos(CONTACT_SPACING_INITIAL + i * CONTACT_SPACING_RAD) * (PAD_RADIUS + MARKER_OFFSET);

        String marker = PAD_NAMES[i];
        if(marker.equals("CA")) {
          marker = "A";
        } else if (marker.equals("CB")) {
          marker = "B";
        } else if (marker.startsWith("T")) {
          continue;
        } else {
          marker = marker.substring(0, 1);
        }

        StringUtils.drawCenteredText(g2d, marker,
                (new Size(px + DIAMETER_SWITCH / 2.0, SizeUnit.mm).convertToPixels()) + originX,
                (new Size(py + DIAMETER_SWITCH / 2.0, SizeUnit.mm).convertToPixels()) + originY,
                HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      }
    }

    g2d.setComposite(oldComposite);
  }

  public double[] getXOffsetsPx() {
    if (xOffsetsPx == null) {
      xOffsetsPx = new double[NUM_CONTACTS];
      for (int i = 0; i < NUM_CONTACTS; i++) {
        double x = Math.sin(CONTACT_SPACING_INITIAL + i * CONTACT_SPACING_RAD) * PAD_RADIUS;
        xOffsetsPx[i] = new Size(x + DIAMETER_SWITCH, SizeUnit.mm).convertToPixels();
      }
    }
    return xOffsetsPx;
  }

  public double[] getYOffsetsPx() {
    if (yOffsetsPx == null) {
      yOffsetsPx = new double[NUM_CONTACTS];
      for (int i = 0; i < NUM_CONTACTS; i++) {
        double y = Math.cos(CONTACT_SPACING_INITIAL + i * CONTACT_SPACING_RAD) * PAD_RADIUS;
        yOffsetsPx[i] = new Size(y + DIAMETER_SWITCH , SizeUnit.mm).convertToPixels();
      }
    }
    return yOffsetsPx;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    Shape base = new Ellipse2D.Double( 0, 0, width, height);
    g2d.setColor(BASE_COLOR);
    g2d.fill(base);

    double contactOffset = 1;
    Shape contacts = new Ellipse2D.Double( contactOffset, contactOffset, width - contactOffset * 2, height - contactOffset * 2);
    g2d.setColor(PAD_COLOR);
    g2d.fill(contacts);

    double centreOffset = 4;
    Shape centre = new Ellipse2D.Double( centreOffset, centreOffset, width - centreOffset * 2, height - centreOffset * 2);
    g2d.setColor(BASE_COLOR);
    g2d.fill(centre);

    double gndOffset = 9;
    Shape gnd = new Ellipse2D.Double( gndOffset, gndOffset, width - gndOffset * 2, height - gndOffset * 2);
    g2d.setColor(PAD_COLOR);
    g2d.fill(gnd);

    double arc = 2.0;
    Shape casing = new RoundRectangle2D.Double(12, 10, 8, 12, arc, arc);
    g2d.setColor(CASE_COLOR);
    g2d.fill(casing);
  }

  @Override
  public int getControlPointCount() {
    return NUM_CONTACTS;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return controlPoints[index];
  }

  private void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    controlPoints = new Point2D[NUM_CONTACTS];
    controlPoints[0] = firstPoint;
    double[] xOffsetsPx = getXOffsetsPx();
    double[] yOffsetsPx = getYOffsetsPx();
    double theta = orientation.toRadians();
    AffineTransform tx =
        AffineTransform.getRotateInstance(theta, firstPoint.getX(), firstPoint.getY());
    for (int i = 1; i < controlPoints.length; i++) {
      controlPoints[i] = new Point2D.Double(firstPoint.getX() + (xOffsetsPx[i] - xOffsetsPx[0]),
          firstPoint.getY() + (yOffsetsPx[i] - yOffsetsPx[0]));
      tx.transform(controlPoints[i], controlPoints[i]);
    }
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
  public int getPositionCount() {
    return 6;
  }

  @Override
  public String getPositionName(int position) {
    return Integer.toString(position + 1);
  }

  @DynamicEditableProperty(source = Freeway3x4_03SwitchPositionPropertyValueSource.class)
  @EditableProperty(name = "Selected Position")
  @Override
  public Integer getSelectedPosition() {
    return selectedPosition;
  }

  public void setSelectedPosition(Integer selectedPosition) {
    this.selectedPosition = selectedPosition;
  }

  @EditableProperty(name = "Markers")
  public Boolean getShowMarkers() {
    if (showMarkers == null) {
      showMarkers = false;
    }
    return showMarkers;
  }

  public void setShowMarkers(Boolean showMarkers) {
    this.showMarkers = showMarkers;
  }

  @Override
  public boolean arePointsConnected(int index1, int index2, int position) {
    if (position == 0 &&
            ((index1 == 0 && index2 == 6) || (index1 == 6 && index2 == 11) || (index1 == 0 && index2 == 11) ||
                    (index1 == 17 && index2 == 21) || (index1 == 21 && index2 == 22) || (index1 == 17 && index2 == 22))
    ) {
      return true;
    }
    if (position == 1 &&
            ((index1 == 1 && index2 == 6) || (index1 == 6 && index2 == 12) || (index1 == 1 && index2 == 12) ||
                    (index1 == 18 && index2 == 21) || (index1 == 21 && index2 == 23) || (index1 == 18 && index2 == 23))
    ) {
      return true;
    }
    if (position == 2 &&
            ((index1 == 2 && index2 == 6) || (index1 == 6 && index2 == 13) || (index1 == 2 && index2 == 13) ||
                    (index1 == 19 && index2 == 21) || (index1 == 21 && index2 == 24) || (index1 == 19 && index2 == 24))
    ) {
      return true;
    }
    if (position == 3 &&
            ((index1 == 5 && index2 == 6) || (index1 == 6 && index2 == 10) || (index1 == 5 && index2 == 10) ||
                    (index1 == 16 && index2 == 21) || (index1 == 21 && index2 == 27) || (index1 == 16 && index2 == 27))
    ) {
      return true;
    }
    if (position == 4 &&
            ((index1 == 4 && index2 == 6) || (index1 == 6 && index2 == 9) || (index1 == 4 && index2 == 9) ||
                    (index1 == 15 && index2 == 21) || (index1 == 21 && index2 == 26) || (index1 == 15 && index2 == 26))
    ) {
      return true;
    }
    if (position == 5 &&
            ((index1 == 3 && index2 == 6) || (index1 == 6 && index2 == 8) || (index1 == 3 && index2 == 8) ||
                    (index1 == 14 && index2 == 21) || (index1 == 21 && index2 == 25) || (index1 == 14 && index2 == 25))
    ) {
      return true;
    }
    return false;
  }
}
