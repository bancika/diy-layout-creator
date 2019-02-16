/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.components.guitar;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.ISwitch;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "LP Toggle Switch", category = "Guitar", author = "Branislav Stojkovic",
    description = "Les Paul style 3 position toggle switch", stretchable = false, zOrder = IDIYComponent.COMPONENT,
    instanceNamePrefix = "SW")
public class LPSwitch extends AbstractTransparentComponent<String> implements ISwitch {

  private static final long serialVersionUID = 1L;

  private static Color BASE_COLOR = Color.lightGray;
  private static Color WAFER_COLOR = Color.decode("#CD8500");

  private static Size LENGTH = new Size(1.3d, SizeUnit.in);
  private static Size BASE_LENGTH = new Size(18d, SizeUnit.mm);
  private static Size WAFER_THICKNESS = new Size(0.05d, SizeUnit.in);
  private static Size TERMINAL_SPACING = new Size(0.2d, SizeUnit.in);

  private String value = "";
  private Point[] controlPoints = new Point[] {new Point(0, 0), new Point(0, 0), new Point(0, 0), new Point(0, 0)};
  transient Shape[] body;
  private Orientation orientation = Orientation.DEFAULT;

  public LPSwitch() {
    super();
    updateControlPoints();
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    Shape[] body = getBody();

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    if (componentState != ComponentState.DRAGGING) {
      Composite oldComposite = g2d.getComposite();
      if (alpha < MAX_ALPHA) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
      }
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : WAFER_COLOR);
      g2d.fill(body[1]);
      g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : BASE_COLOR);
      g2d.fill(body[0]);
      g2d.setComposite(oldComposite);
    }

    Color finalBorderColor;

    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : WAFER_COLOR.darker();
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[0]);

    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : BASE_COLOR.darker();
    }

    g2d.setColor(finalBorderColor);
    g2d.draw(body[1]);

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
    g2d.setColor(METAL_COLOR);
    g2d.draw(body[2]);
  }

  @SuppressWarnings("incomplete-switch")
  public Shape[] getBody() {
    if (body == null) {
      body = new Shape[3];

      int x = controlPoints[0].x;
      int y = controlPoints[0].y;
      int baseLength = (int) BASE_LENGTH.convertToPixels();
      int length = (int) LENGTH.convertToPixels();
      int waferThickness = getClosestOdd(WAFER_THICKNESS.convertToPixels());

      Rectangle2D ground =
          new Rectangle2D.Double(x - waferThickness / 2, y, waferThickness - 1, baseLength + (length - baseLength) / 2);
      body[0] = new Area(ground);

      int bodyY = y + (length - baseLength) / 2;

      Area waferArea = new Area();//new Area(new Rectangle2D.Double(x - waferThickness * 3 / 2, bodyY, waferThickness, baseLength));
      waferArea.add(new Area(new Rectangle2D.Double(x + waferThickness / 2, bodyY, waferThickness  *3, baseLength)));

      waferArea.add(new Area(new Rectangle2D.Double(x - waferThickness * 5 / 2, bodyY, waferThickness, baseLength)));
      waferArea.add(new Area(new Rectangle2D.Double(x + waferThickness * 3 / 2, bodyY, waferThickness, baseLength)));

      waferArea.add(new Area(new Rectangle2D.Double(x - waferThickness * 7 / 2, bodyY, waferThickness * 3, baseLength)));
//      waferArea.add(new Area(new Rectangle2D.Double(x + waferThickness * 5 / 2, bodyY, waferThickness, baseLength)));

      body[1] = waferArea;

      double theta = 0;
      // Rotate if needed
      if (orientation != Orientation.DEFAULT) {
        switch (orientation) {
          case _90:
            theta = Math.PI / 2;
            break;
          case _180:
            theta = Math.PI;
            break;
          case _270:
            theta = Math.PI * 3 / 2;
            break;
        }
      }

      int terminalSpacing = (int) TERMINAL_SPACING.convertToPixels();

      GeneralPath terminalPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
      terminalPath.moveTo(x - waferThickness * 3 / 2, bodyY + 1);
      terminalPath.lineTo(x - waferThickness * 3 / 2, bodyY + baseLength);
      terminalPath.lineTo(x, y + length);

      terminalPath.moveTo(x + waferThickness * 3 / 2, bodyY + 1);
      terminalPath.lineTo(x + waferThickness * 3 / 2, bodyY + baseLength);
      terminalPath.lineTo(x, y + length);

      terminalPath.moveTo(x - waferThickness * 5 / 2, bodyY + 1);
      terminalPath.lineTo(x - waferThickness * 5 / 2, bodyY + baseLength);
      terminalPath.lineTo(x - terminalSpacing, y + length);

      terminalPath.moveTo(x + waferThickness * 5 / 2, bodyY + 1);
      terminalPath.lineTo(x + waferThickness * 5 / 2, bodyY + baseLength);
      terminalPath.lineTo(x + terminalSpacing, y + length);
      body[2] = terminalPath;

      // Rotate if needed
      if (theta != 0) {
        AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
        // Skip the last one because it's already rotated
        for (int i = 0; i < body.length; i++) {
          Shape shape = body[i];
          if (shape instanceof Area) {
            Area area = (Area) shape;
            area.transform(rotation);
          } else if (shape instanceof GeneralPath) {
            GeneralPath path = (GeneralPath) shape;
            path.transform(rotation);
          }
        }
      }
    }
    return body;
  }

  @SuppressWarnings("incomplete-switch")
  private void updateControlPoints() {
    int x = controlPoints[0].x;
    int y = controlPoints[0].y;
    int terminalSpacing = (int) TERMINAL_SPACING.convertToPixels();
    int length = (int) LENGTH.convertToPixels();

    controlPoints[1].setLocation(x - terminalSpacing, y + length);
    controlPoints[2].setLocation(x, y + length);
    controlPoints[3].setLocation(x + terminalSpacing, y + length);

    // Rotate if needed
    if (orientation != Orientation.DEFAULT) {
      double theta = 0;
      switch (orientation) {
        case _90:
          theta = Math.PI / 2;
          break;
        case _180:
          theta = Math.PI;
          break;
        case _270:
          theta = Math.PI * 3 / 2;
          break;
      }
      AffineTransform rotation = AffineTransform.getRotateInstance(theta, x, y);
      for (Point point : controlPoints) {
        rotation.transform(point, point);
      }
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(Math.PI / 4, width / 2, height / 2);
    int baseLength = 20 * width / 32;

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(3f));
    g2d.setColor(BASE_COLOR);
    g2d.drawLine(width / 2, 0, width / 2, baseLength + (width - baseLength) / 2);
    g2d.setColor(WAFER_COLOR);
    g2d.fillRect(8 * width / 32, (width - baseLength) / 2, 16 * width / 32, baseLength);
    g2d.setColor(METAL_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    g2d.drawLine(width / 2 - 2, (width - baseLength) / 2, width / 2 - 2, baseLength + (width - baseLength) / 2);
    g2d.drawLine(width / 2 + 2, (width - baseLength) / 2, width / 2 + 2, baseLength + (width - baseLength) / 2);
    g2d.drawLine(width / 2 - 5, (width - baseLength) / 2, width / 2 - 5, baseLength + (width - baseLength) / 2);
    g2d.drawLine(width / 2 + 5, (width - baseLength) / 2, width / 2 + 5, baseLength + (width - baseLength) / 2);
    int dx = 2 * width / 32;
    int dy = 4 * width / 32;
    g2d.drawLine(width / 2 - 2, baseLength + (width - baseLength) / 2, width / 2 - 2 + dx, baseLength
        + (width - baseLength) / 2 + dy);
    g2d.drawLine(width / 2 + 2, baseLength + (width - baseLength) / 2, width / 2 + 2 - dx, baseLength
        + (width - baseLength) / 2 + dy);

    g2d.drawLine(width / 2 - 5, baseLength + (width - baseLength) / 2, width / 2 - 5 - dx, baseLength
        + (width - baseLength) / 2 + dy);
    g2d.drawLine(width / 2 + 5, baseLength + (width - baseLength) / 2, width / 2 + 5 + dx, baseLength
        + (width - baseLength) / 2 + dy);
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public Point getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public void setControlPoint(Point point, int index) {
    this.controlPoints[index].setLocation(point);
    // Invalidate the body
    body = null;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    // Invalidate the body
    body = null;
  }
  
  @Override
  public String getControlPointNodeName(int index) {
    // we don't want the switch to produce any nodes, it just makes connections
    return null;
  }
  
  // switch stuff

  @Override
  public int getPositionCount() {    
    return 3;
  }

  @Override
  public String getPositionName(int position) {
    switch (position) {
      case 0:
        return "Treble";
      case 1:
        return "Middle";
      case 2:
        return "Rhythm";
    }
    return null;
  }

  @Override
  public boolean arePointsConnected(int index1, int index2, int position) {
    switch (position) {
      case 0:
        return index1 == 1 && index2 == 2;
      case 1:
        return index1 > 0;
      case 2:
        return index1 == 2 && index2 == 3;
    }
    return false;
  }
}
