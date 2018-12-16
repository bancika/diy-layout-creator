///*
//
//    DIY Layout Creator (DIYLC).
//    Copyright (c) 2009-2018 held jointly by the individual authors.
//
//    This file is part of DIYLC.
//
//    DIYLC is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    DIYLC is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
//
//*/
//package org.diylc.components.passive;
//
//import java.awt.AlphaComposite;
//import java.awt.Color;
//import java.awt.Composite;
//import java.awt.FontMetrics;
//import java.awt.Graphics2D;
//import java.awt.Point;
//import java.awt.Rectangle;
//import java.awt.geom.Area;
//import java.awt.geom.Ellipse2D;
//import java.awt.geom.Rectangle2D;
//
//import org.diylc.appframework.miscutils.ConfigurationManager;
//import org.diylc.common.Display;
//import org.diylc.common.IPlugInPort;
//import org.diylc.common.ObjectCache;
//import org.diylc.common.Orientation;
//import org.diylc.components.AbstractTransparentComponent;
//import org.diylc.core.ComponentState;
//import org.diylc.core.IDIYComponent;
//import org.diylc.core.IDrawingObserver;
//import org.diylc.core.Project;
//import org.diylc.core.Theme;
//import org.diylc.core.VisibilityPolicy;
//import org.diylc.core.annotations.ComponentDescriptor;
//import org.diylc.core.annotations.EditableProperty;
//import org.diylc.core.annotations.KeywordPolicy;
//import org.diylc.core.measures.Capacitance;
//import org.diylc.core.measures.Size;
//import org.diylc.core.measures.SizeUnit;
//import org.diylc.utils.Constants;
//
////@ComponentDescriptor(name = "Electrolytic Capacitor (can)", author = "Branislav Stojkovic", category = "Passive",
////    instanceNamePrefix = "C", description = "Vertical chassis-mount electrolytic capacitor similar to JJ",
////    stretchable = false, zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE)
//public class ElectrolyticCanCapacitor extends AbstractTransparentComponent<Capacitance[]> {
//
//  private static final long serialVersionUID = 1L;
//
//  public static Color BODY_COLOR = Color.lightGray;
//  public static Color BASE_COLOR = Color.gray;
//  public static Color BORDER_COLOR = BODY_COLOR.darker();
//  public static Color PIN_COLOR = Color.decode("#00B2EE");
//  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
//  public static Color LABEL_COLOR = Color.black;
//  public static Size PIN_SIZE = new Size(0.08d, SizeUnit.in);
////  public static Size PIN_SPACING = new Size(0.05d, SizeUnit.in);
//  public static Size BODY_DIAMETER = new Size(1d, SizeUnit.in);
//
//  private Capacitance[] value = new Capacitance[4];
//  private org.diylc.core.measures.Voltage voltage = null;
//
//  private Orientation orientation = Orientation.DEFAULT;
//  private Point[] controlPoints = new Point[] {new Point(0, 0), new Point(0, 0), new Point(0, 0)};
//  transient private Area body;
//  private Color bodyColor = BODY_COLOR;
//  private Color baseColor = BASE_COLOR;
//  private Color borderColor = BORDER_COLOR;
//  private Color labelColor = LABEL_COLOR;
//  protected Display display = Display.NAME;
////  private Size pinSpacing = PIN_SPACING;
//  private CanSections sections = CanSections._1;
//  private Size diameter = BODY_DIAMETER;
//
//  public ElectrolyticCanCapacitor() {
//    super();
//    updateControlPoints();
//  }
//
//  @EditableProperty
//  public Capacitance[] getValue() {
//    return value;
//  }
//
//  public void setValue(Capacitance[] value) {
//    this.value = value;
//  }
//
//  @EditableProperty
//  public org.diylc.core.measures.Voltage getVoltage() {
//    return voltage;
//  }
//
//  public void setVoltage(org.diylc.core.measures.Voltage voltage) {
//    this.voltage = voltage;
//  }
////
////  @EditableProperty
////  public CanSections getSections() {
////    return sections;
////  }
////
////  public void setSections(CanSections sections) {
////    this.sections = sections;
////  }
//  
//  @EditableProperty
//  public Size getDiameter() {
//    return diameter;
//  }
//  
//  public void setDiameter(Size diameter) {
//    this.diameter = diameter;
//  }
//
//  @EditableProperty
//  public Orientation getOrientation() {
//    return orientation;
//  }
//
//  public void setOrientation(Orientation orientation) {
//    this.orientation = orientation;
//    updateControlPoints();
//    // Reset body shape;
//    body = null;
//  }
//
//  @Override
//  public int getControlPointCount() {
//    return controlPoints.length;
//  }
//
//  @Override
//  public Point getControlPoint(int index) {
//    return controlPoints[index];
//  }
//
//  @Override
//  public boolean isControlPointSticky(int index) {
//    return true;
//  }
//
//  @Override
//  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
//    return VisibilityPolicy.NEVER;
//  }
//
//  @Override
//  public void setControlPoint(Point point, int index) {
//    controlPoints[index].setLocation(point);
//    body = null;
//  }
//
//  private void updateControlPoints() {
////    int pinSpacing = (int) getPinSpacing().convertToPixels();
//    int pinSpacing = (int) getDiameter().convertToPixels();
////    switch (getS)
//    
//    // Update control points.
//    int x = controlPoints[0].x;
//    int y = controlPoints[0].y;
//    switch (orientation) {
//      case DEFAULT:
//        controlPoints[1].setLocation(x - pinSpacing, y + pinSpacing);
//        controlPoints[2].setLocation(x, y + 2 * pinSpacing);
//        break;
//      case _90:
//        controlPoints[1].setLocation(x - pinSpacing, y - pinSpacing);
//        controlPoints[2].setLocation(x - 2 * pinSpacing, y);
//        break;
//      case _180:
//        controlPoints[1].setLocation(x + pinSpacing, y - pinSpacing);
//        controlPoints[2].setLocation(x, y - 2 * pinSpacing);
//        break;
//      case _270:
//        controlPoints[1].setLocation(x + pinSpacing, y + pinSpacing);
//        controlPoints[2].setLocation(x + 2 * pinSpacing, y);
//        break;
//      default:
//        throw new RuntimeException("Unexpected orientation: " + orientation);
//    }
//  }
//
//  public Area getBody() {
//    if (body == null) {
//      int x = (controlPoints[0].x + controlPoints[1].x + controlPoints[2].x) / 3;
//      int y = (controlPoints[0].y + controlPoints[1].y + controlPoints[2].y) / 3;
//      int bodyDiameter = getClosestOdd(getDiameter().convertToPixels());
//
//      body = new Area(new Ellipse2D.Double(x - bodyDiameter / 2, y - bodyDiameter / 2, bodyDiameter, bodyDiameter));
//
//    }
//    return body;
//  }
//
//  @Override
//  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
//      IDrawingObserver drawingObserver) {
//    if (checkPointsClipped(g2d.getClip())) {
//      return;
//    }
//    int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
//    Area mainArea = getBody();
//    Composite oldComposite = g2d.getComposite();
//    if (alpha < MAX_ALPHA) {
//      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
//    }
//    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
//    g2d.fill(mainArea);
//    g2d.setComposite(oldComposite);
//    Color finalBorderColor;
//    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
//    if (outlineMode) {
//      finalBorderColor =
//          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
//              : theme.getOutlineColor();
//    } else {
//      finalBorderColor =
//          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
//              : borderColor;
//    }
//    g2d.setColor(finalBorderColor);
//    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
//    g2d.draw(mainArea);
//
//    for (Point point : controlPoints) {
//      if (!outlineMode) {
//        g2d.setColor(PIN_COLOR);
//        g2d.fillOval(point.x - pinSize / 2, point.y - pinSize / 2, pinSize, pinSize);
//      }
//      g2d.setColor(outlineMode ? theme.getOutlineColor() : PIN_BORDER_COLOR);
//      g2d.drawOval(point.x - pinSize / 2, point.y - pinSize / 2, pinSize, pinSize);
//    }
//
//    // Draw label.
//    g2d.setFont(project.getFont());
//    Color finalLabelColor;
//    if (outlineMode) {
//      finalLabelColor =
//          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
//              : theme.getOutlineColor();
//    } else {
//      finalLabelColor =
//          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
//              : getLabelColor();
//    }
//    g2d.setColor(finalLabelColor);
//    String label = "";
//    label = (getDisplay() == Display.NAME) ? getName() : getValue().toString();
//    if (getDisplay() == Display.NONE) {
//      label = "";
//    }
//    if (getDisplay() == Display.BOTH) {
//      label = getName() + "  " + (getValue() == null ? "" : getValue().toString());
//    }
//    FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
//    Rectangle2D rect = fontMetrics.getStringBounds(label, g2d);
//    int textHeight = (int) (rect.getHeight());
//    int textWidth = (int) (rect.getWidth());
//    // Center text horizontally and vertically
//    Rectangle bounds = mainArea.getBounds();
//    int x = bounds.x + (bounds.width - textWidth) / 2;
//    int y = bounds.y + (bounds.height - textHeight) / 2 + fontMetrics.getAscent();
//    g2d.drawString(label, x, y);
//  }
//
//  @Override
//  public void drawIcon(Graphics2D g2d, int width, int height) {
//    int margin = 2 * width / 32;
//    Area area = new Area(new Ellipse2D.Double(margin / 2, margin, width - 2 * margin, width - 2 * margin));
//    g2d.setColor(BODY_COLOR);
//    g2d.fill(area);
//    g2d.setColor(BORDER_COLOR);
//    g2d.draw(area);
//    g2d.setColor(PIN_COLOR);
//    int pinSize = 2 * width / 32;
//    for (int i = 0; i < 3; i++) {
//      g2d.fillOval((i == 1 ? width * 3 / 8 : width / 2) - pinSize / 2, (height / 4) * (i + 1), pinSize, pinSize);
//    }
//  }
//
//  @EditableProperty(name = "Body")
//  public Color getBodyColor() {
//    return bodyColor;
//  }
//
//  public void setBodyColor(Color bodyColor) {
//    this.bodyColor = bodyColor;
//  }
//  
//  @EditableProperty(name = "Base")
//  public Color getBaseColor() {
//    return baseColor;
//  }
//  
//  public void setBaseColor(Color baseColor) {
//    this.baseColor = baseColor;
//  }
//
////  @EditableProperty(name = "Pin spacing")
////  public Size getPinSpacing() {
////    return pinSpacing;
////  }
////
////  public void setPinSpacing(Size pinSpacing) {
////    this.pinSpacing = pinSpacing;
////    updateControlPoints();
////    // Reset body shape;
////    body = null;
////  }
//
//  @EditableProperty(name = "Border")
//  public Color getBorderColor() {
//    return borderColor;
//  }
//
//  public void setBorderColor(Color borderColor) {
//    this.borderColor = borderColor;
//  }
//
//  @EditableProperty(name = "Label")
//  public Color getLabelColor() {
//    return labelColor;
//  }
//
//  public void setLabelColor(Color labelColor) {
//    this.labelColor = labelColor;
//  }
//
//  @EditableProperty
//  public Display getDisplay() {
//    if (display == null) {
//      display = Display.NAME;
//    }
//    return display;
//  }
//
//  public void setDisplay(Display display) {
//    this.display = display;
//  }
//
//  public enum CanSections {
//    _1, _2, _3, _4, _5;
//
//    @Override
//    public String toString() {
//      return name().substring(1);
//    }
//  }
//}
