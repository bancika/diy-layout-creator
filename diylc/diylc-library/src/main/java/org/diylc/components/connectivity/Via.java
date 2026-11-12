/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

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
package org.diylc.components.connectivity;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.bancika.gerberwriter.GerberFunctions;

import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.IConnective;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.ILayeredComponent;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.AiNote;
import org.diylc.core.annotations.AiNote.Scope;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.gerber.GerberLayer;
import org.diylc.core.gerber.IGerberComponentCustom;
import org.diylc.core.gerber.IGerberDrawingObserver;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@AiNote(value = "Use on Blank Board and PCB layouts to take a trace from one copper layer to the "
    + "other", scope = Scope.SELECTION)
@AiNote("Place the control point where a Top trace and a Bottom trace meet; the via joins the "
    + "copper of both layers there. It is not a component pad: leads go into a Solder Pad")
@ComponentDescriptor(name = "Via", category = "Connectivity", author = "Branislav Stojkovic",
    description = "Plated hole joining the copper on the top and bottom of a PCB",
    instanceNamePrefix = "Via", zOrder = IDIYComponent.TRACE + 0.1,
    bomPolicy = BomPolicy.NEVER_SHOW, keywordPolicy = KeywordPolicy.SHOW_TAG, keywordTag = "PCB",
    transformer = SimpleComponentTransformer.class, enableCache = true)
public class Via extends AbstractTransparentComponent<Void>
    implements IConnective, ILayeredComponent, IGerberComponentCustom {

  private static final long serialVersionUID = 1L;

  // the defaults KiCad gives a new board
  public static Size SIZE = new Size(0.8d, SizeUnit.mm);
  public static Size HOLE_SIZE = new Size(0.4d, SizeUnit.mm);
  public static Color COLOR = Color.black;

  private Size size = SIZE;
  private Size holeSize = HOLE_SIZE;
  private Color color = COLOR;
  private Point2D.Double point = new Point2D.Double(0, 0);

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver,
      IGerberDrawingObserver gerberDrawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    double diameter = getSize().convertToPixels();
    double holeDiameter = getHoleSize().convertToPixels();
    g2d.setColor(
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING
            ? SELECTION_COLOR
            : color);
    // the via is tented: the solder mask covers it, so no mask opening is written
    if (gerberDrawingObserver != null) {
      gerberDrawingObserver.startGerberOutput(GerberLayer.CopperTop, GerberFunctions.VIA_PAD,
          false);
      gerberDrawingObserver.startGerberOutput(GerberLayer.CopperBot, GerberFunctions.VIA_PAD,
          false);
    }
    Composite oldComposite = applyAlpha(g2d, componentState);
    // the copper is not on a layer of its own, so it connects to the copper of every layer
    drawingObserver.startTrackingContinuityArea(true);
    drawingObserver.startDrawingBothSides();
    g2d.fill(new Ellipse2D.Double(point.getX() - diameter / 2, point.getY() - diameter / 2,
        diameter, diameter));
    drawingObserver.stopTracking();
    drawingObserver.stopTrackingContinuityArea();
    // the hole goes to the drill file only; cutting it out of the copper as well is advised against
    // by the Gerber specification
    if (gerberDrawingObserver != null) {
      gerberDrawingObserver.stopGerberOutput(GerberLayer.CopperTop);
      gerberDrawingObserver.stopGerberOutput(GerberLayer.CopperBot);
      gerberDrawingObserver.startGerberOutput(GerberLayer.DrillPlated, GerberFunctions.VIA_DRILL,
          false);
    }
    g2d.setColor(Constants.CANVAS_COLOR);
    g2d.fill(new Ellipse2D.Double(point.getX() - holeDiameter / 2,
        point.getY() - holeDiameter / 2, holeDiameter, holeDiameter));
    drawingObserver.stopDrawingSide();
    g2d.setComposite(oldComposite);
    if (gerberDrawingObserver != null) {
      gerberDrawingObserver.stopGerberOutput();
    }
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    this.draw(g2d, componentState, outlineMode, project, drawingObserver, null);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int diameter = getClosestOdd(width / 3);
    int holeDiameter = getClosestOdd(width / 7);
    g2d.setColor(COLOR);
    g2d.fillOval((width - diameter) / 2, (height - diameter) / 2, diameter, diameter);
    g2d.setColor(Constants.CANVAS_COLOR);
    g2d.fillOval((width - holeDiameter) / 2, (height - holeDiameter) / 2, holeDiameter,
        holeDiameter);
  }

  @EditableProperty(validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getSize() {
    if (size == null) {
      size = SIZE;
    }
    return size;
  }

  public void setSize(Size size) {
    this.size = size;
  }

  @EditableProperty(name = "Hole", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getHoleSize() {
    if (holeSize == null) {
      holeSize = HOLE_SIZE;
    }
    return holeSize;
  }

  public void setHoleSize(Size holeSize) {
    this.holeSize = holeSize;
  }

  @EditableProperty
  public Color getColor() {
    if (color == null) {
      color = COLOR;
    }
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  // goes through the board, so it is on every copper layer
  @Override
  public int getLayerId() {
    return 0;
  }

  @Override
  public int getControlPointCount() {
    return 1;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return point;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    this.point.setLocation(point);
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}

  @Override
  public Rectangle2D getCachingBounds() {
    double size = getSize().convertToPixels();
    return new Rectangle2D.Double(point.getX() - size, point.getY() - size, size * 2, size * 2);
  }
}
