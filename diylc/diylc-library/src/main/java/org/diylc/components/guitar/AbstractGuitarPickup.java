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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.Orientation;
import org.diylc.common.OrientationHV;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractLabeledComponent;
import org.diylc.components.guitar.pickup.CoilDefinition;
import org.diylc.components.guitar.pickup.LeadSpec;
import org.diylc.components.guitar.pickup.MagneticPolarity;
import org.diylc.components.guitar.pickup.PickupDefinition;
import org.diylc.components.guitar.pickup.PickupDefinitionSnapshot;
import org.diylc.components.guitar.pickup.TerminalDefinition;
import org.diylc.components.guitar.pickup.TerminalRole;
import org.diylc.core.*;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

public abstract class AbstractGuitarPickup extends AbstractLabeledComponent<String> implements IDefaultLeadStyleProvider {

  private static final long serialVersionUID = 1L;
  
  protected static Size POINT_SPACING = new Size(0.1d, SizeUnit.in);
  private static Size POINT_SIZE = new Size(1.5d, SizeUnit.mm);
  
  protected static final int TERMINAL_FONT_SIZE = 11;  
  
  protected String value = "";
  protected Orientation orientation = Orientation.DEFAULT;
  protected transient Shape[] body;  
  
  protected Point2D controlPoint = new Point2D.Double(0, 0);
  protected Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0), new Point2D.Double(0, 0)};
  
  protected Polarity polarity = Polarity.North;

  protected Color labelColor;

  /**
   * Id of the {@link PickupDefinition} this component was last set from, kept only for display
   * and possible future "refresh from library" functionality. The library is never consulted
   * automatically to keep this in sync - {@link #pickupDefinitionSnapshot} is authoritative.
   */
  private String pickupDefinitionId;

  /**
   * Frozen copy of the pickup definition applied to this component (if any), embedded directly so
   * the project remains portable even without the original library file. Absent
   * (<code>null</code>) for generic pickups and for projects created before this feature existed;
   * all existing behaviour (naming, analysis) is unaffected either way.
   */
  private PickupDefinitionSnapshot pickupDefinitionSnapshot;

  public AbstractGuitarPickup() {
    updateControlPoints();
  }
  
  protected void drawTerminalLabels(Graphics2D g2d, Color color, Project project) {
    Point2D[] points = getControlPoints();    
    g2d.setColor(color);
      
    g2d.setFont(project.getFont().deriveFont(TERMINAL_FONT_SIZE * 1f));
    int dx = 0;
    int dy = 0;
    switch (orientation) {
      case DEFAULT:        
        dx = getControlPointDirection() == OrientationHV.HORIZONTAL ? 0 : (int) (TERMINAL_FONT_SIZE * 0.8);
        dy = getControlPointDirection() == OrientationHV.HORIZONTAL ? -TERMINAL_FONT_SIZE : 0;  
        break;
      case _90:
        dx = getControlPointDirection() == OrientationHV.HORIZONTAL ? TERMINAL_FONT_SIZE : 0;
        dy = getControlPointDirection() == OrientationHV.HORIZONTAL ? 0 : (int) (TERMINAL_FONT_SIZE * 0.8);
        break;
      case _180:
        dx = getControlPointDirection() == OrientationHV.HORIZONTAL ? 0 : -(int) (TERMINAL_FONT_SIZE * 0.8);
        dy = getControlPointDirection() == OrientationHV.HORIZONTAL ? TERMINAL_FONT_SIZE  : 0;       
        break;
      case _270:
        dx = getControlPointDirection() == OrientationHV.HORIZONTAL ? -TERMINAL_FONT_SIZE : 0;
        dy = getControlPointDirection() == OrientationHV.HORIZONTAL ? 0 : -(int) (TERMINAL_FONT_SIZE * 0.8);
        break;     
    }   

    StringUtils.drawCenteredText(g2d, "N", (points[0].getX() + points[1].getX()) / 2 + dx, (points[0].getY() + points[1].getY()) / 2 + dy, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    StringUtils.drawCenteredText(g2d, "S", (points[2].getX() + points[3].getX()) / 2 + dx, (points[2].getY() + points[3].getY()) / 2 + dy, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);   
  }
  
  protected void drawMainLabel(Graphics2D g2d, Project project, boolean outlineMode, ComponentState componentState) {
    Color finalLabelColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : theme.getOutlineColor();
    } else {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : getLabelColor();
    }
    g2d.setColor(finalLabelColor);
    g2d.setFont(project.getFont().deriveFont(Font.BOLD));
    
    // Override font size
    if (getFontSizeOverride() != null)
      g2d.setFont(g2d.getFont().deriveFont(1f * getFontSizeOverride()));
    
    Rectangle bounds = getBody()[0].getBounds();
    
    AffineTransform originalTx = g2d.getTransform();
    g2d.translate(bounds.getX() + bounds.width / 2, bounds.getY() + bounds.height / 2);
    if (orientation == Orientation._90)
      g2d.rotate(Math.PI / 2);
    else if (orientation == Orientation._270){
      g2d.rotate(-Math.PI / 2);
    }
    g2d.translate(0, getMainLabelYOffset());
    
    StringUtils.drawCenteredText(g2d, getName(), 0, 0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
   
    g2d.setTransform(originalTx);
  }

  protected void markContactPoints(Graphics2D g2d, IDrawingObserver drawingObserver) {
    g2d.setColor(Constants.TRANSPARENT_COLOR);
    int pointSize = getClosestOdd(POINT_SIZE.convertToPixels());
    drawingObserver.startTrackingContinuityArea(true);
    for (int i = 0; i < getControlPointCount(); i++) {
      if (isControlPointSticky(i)) {
        Point2D p = getControlPoint(i);
        g2d.fill(
            new Ellipse2D.Double(p.getX() - pointSize / 2, p.getY() - pointSize / 2, pointSize,
                pointSize));
      }
    }
    drawingObserver.stopTrackingContinuityArea();
  }
  
  protected abstract Shape[] getBody();
  
  public abstract boolean isHumbucker();
  
  protected int getMainLabelYOffset() {
    return 0;
  }
  
  @EditableProperty(name = "Model")
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
    // Invalidate the body
    body = null;
    updateControlPoints();
  }
  
  protected Point2D[] getControlPoints() {
    if (controlPoints == null) {
      controlPoints =
          new Point2D[] {controlPoint, new Point2D.Double(controlPoint.getX(), controlPoint.getY()),
              new Point2D.Double(controlPoint.getX(), controlPoint.getY()), new Point2D.Double(controlPoint.getX(), controlPoint.getY())};
      updateControlPoints();
    }
    return controlPoints;
  }
  
  @Override
  public int getControlPointCount() {
    return getControlPoints().length;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.ALWAYS;
  }


  @Override
  public Point2D getControlPoint(int index) {
    return getControlPoints()[index];
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    getControlPoints()[index].setLocation(point);
    // Invalidate the body
    body = null;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }
  
  @EditableProperty(name = "Coil(s)")
  public Polarity getPolarity() {
    if (polarity == null)
      polarity = Polarity.North;
    return polarity;
  }
  
  public void setPolarity(Polarity polarity) {
    this.polarity = polarity;
    // Invalidate the body
    body = null;
  }
  
  @EditableProperty(name = "Label")
  public Color getLabelColor() {
    if (labelColor == null)
      labelColor = LABEL_COLOR;
    return labelColor;
  }
  
  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  /**
   * @return the id of the pickup library definition currently applied to this component, or
   *         {@code null} for a generic pickup. Kept only for display/possible future refresh -
   *         {@link #getPickupDefinitionSnapshot()} is authoritative for this component's data.
   */
  public String getPickupDefinitionId() {
    return pickupDefinitionId;
  }

  public void setPickupDefinitionId(String pickupDefinitionId) {
    this.pickupDefinitionId = pickupDefinitionId;
  }

  /**
   * @return the frozen pickup definition data applied to this component, or {@code null} for a
   *         generic pickup (including every pickup placed before this feature existed).
   */
  public PickupDefinitionSnapshot getPickupDefinitionSnapshot() {
    return pickupDefinitionSnapshot;
  }

  public void setPickupDefinitionSnapshot(PickupDefinitionSnapshot pickupDefinitionSnapshot) {
    this.pickupDefinitionSnapshot = pickupDefinitionSnapshot;
    this.pickupDefinitionId = pickupDefinitionSnapshot == null ? null : pickupDefinitionSnapshot.getDefinitionId();
  }

  /** @return the definition data itself (a shortcut for {@code getPickupDefinitionSnapshot().getDefinition()}), or {@code null}. */
  public PickupDefinition getAppliedDefinition() {
    return pickupDefinitionSnapshot == null ? null : pickupDefinitionSnapshot.getDefinition();
  }

  /**
   * Resolves which terminal of the currently-applied pickup definition (if any) physically sits
   * at the given control point, using the same coil magnetic-polarity / start-finish semantics
   * as the existing guitar netlist analyser (control point 0 = North Start, 1 = North Finish, 2
   * = South Finish, 3 = South Start for a 4-terminal humbucking pickup; the coil's own start/
   * finish for a 2-terminal single-coil pickup) - never assuming a particular pole-piece
   * construction (screw/slug/rod/etc.) implies a particular polarity.
   *
   * <p>Returns {@code null} whenever no definition is applied, the point is not an active/sticky
   * terminal, or the definition's coil/terminal data is missing or ambiguous - callers must fall
   * back to existing generic behaviour in that case. This method never changes what
   * {@link #getControlPointNodeName(int)} or {@link #getInternalLinkName(int, int)} report, so
   * applying a definition can never affect circuit topology or analysis output.
   */
  public TerminalDefinition getTerminalForControlPoint(int index) {
    PickupDefinition definition = getAppliedDefinition();
    if (definition == null || index < 0 || index >= getControlPointCount() || !isControlPointSticky(index)) {
      return null;
    }
    if (isHumbucker()) {
      return resolveHumbuckingTerminal(definition, index);
    }
    return resolveSingleCoilTerminal(definition, index);
  }

  /** Convenience for {@link #getTerminalForControlPoint(int)}{@code .lead()}, or {@code null}. */
  public LeadSpec getLeadSpecForControlPoint(int index) {
    TerminalDefinition terminal = getTerminalForControlPoint(index);
    return terminal == null ? null : terminal.lead();
  }

  /**
   * Supports "Add Flexible Leads" (and any other caller wanting a suggested lead colour) without
   * that caller needing any knowledge of pickup definitions/terminals: delegates to the applied
   * definition's lead colour for this control point, or {@code null} when there is none (no
   * definition applied, ambiguous/incomplete metadata, or the terminal has no colour set) - in
   * which case the caller keeps using its own current default, unchanged.
   */
  @Override
  public Color getDefaultLeadColor(int controlPointIndex) {
    LeadSpec lead = getLeadSpecForControlPoint(controlPointIndex);
    return lead == null ? null : lead.toAwtColor();
  }

  private TerminalDefinition resolveHumbuckingTerminal(PickupDefinition definition, int index) {
    CoilDefinition northCoil = definition.findUniqueCoilByPolarity(MagneticPolarity.NORTH);
    CoilDefinition southCoil = definition.findUniqueCoilByPolarity(MagneticPolarity.SOUTH);
    if (northCoil == null || southCoil == null) {
      return null;
    }
    CoilDefinition coil;
    TerminalRole role;
    switch (index) {
      case 0:
        coil = northCoil;
        role = TerminalRole.START;
        break;
      case 1:
        coil = northCoil;
        role = TerminalRole.FINISH;
        break;
      case 2:
        coil = southCoil;
        role = TerminalRole.FINISH;
        break;
      case 3:
        coil = southCoil;
        role = TerminalRole.START;
        break;
      default:
        return null;
    }
    return findTerminalForCoilAndRole(definition, coil, role);
  }

  private TerminalDefinition resolveSingleCoilTerminal(PickupDefinition definition, int index) {
    // A 2-terminal single-coil-format pickup only makes an unambiguous "start"/"finish" pairing
    // when the definition describes exactly one coil; with zero or several coils there is no
    // reliable way to know which one is wired to these two control points.
    if (definition.coils().size() != 1) {
      return null;
    }
    CoilDefinition coil = definition.coils().get(0);
    TerminalRole role;
    if (index == 1) {
      role = TerminalRole.START;
    } else if (index == 2) {
      role = TerminalRole.FINISH;
    } else {
      return null;
    }
    return findTerminalForCoilAndRole(definition, coil, role);
  }

  private static TerminalDefinition findTerminalForCoilAndRole(PickupDefinition definition, CoilDefinition coil,
      TerminalRole role) {
    if (coil == null) {
      return null;
    }
    TerminalDefinition match = null;
    for (TerminalDefinition terminal : definition.terminals()) {
      if (coil.id().equals(terminal.coilId()) && terminal.role() == role) {
        if (match != null) {
          // ambiguous - more than one terminal claims this coil+role combination.
          return null;
        }
        match = terminal;
      }
    }
    return match;
  }

  protected abstract OrientationHV getControlPointDirection();

  /**
   * The (dx, dy) unit vector control point 1 sits at (relative to control point 0) for the given
   * native axis/orientation combination - a pure, static extraction of
   * {@link #updateControlPoints()}'s dx/dy computation, kept only so that computation has a
   * single source of truth.
   */
  @SuppressWarnings("incomplete-switch")
  private static int[] controlPointDirectionVector(OrientationHV direction, Orientation orientation) {
    boolean horizontal = direction == OrientationHV.HORIZONTAL;
    int dx = horizontal ? 1 : 0;
    int dy = horizontal ? 0 : 1;
    switch (orientation) {
      case _90:
        dx = horizontal ? 0 : -1;
        dy = horizontal ? -1 : 0;
        break;
      case _180:
        dx = horizontal ? -1 : 0;
        dy = horizontal ? 0 : -1;
        break;
      case _270:
        dx = horizontal ? 0 : 1;
        dy = horizontal ? 1 : 0;
        break;
    }
    return new int[] {dx, dy};
  }

  @SuppressWarnings("incomplete-switch")
  protected void updateControlPoints() {
    Point2D[] points = getControlPoints();
    int pointSpacing = (int) POINT_SPACING.convertToPixels();
    int[] vector = controlPointDirectionVector(getControlPointDirection(), orientation);
    int dx = vector[0];
    int dy = vector[1];
    points[1].setLocation(points[0].getX() + dx * pointSpacing, points[0].getY() + dy * pointSpacing);
    points[2]
        .setLocation(points[0].getX() + 2 * dx * pointSpacing, points[0].getY() + 2 * dy * pointSpacing);
    points[3]
        .setLocation(points[0].getX() + 3 * dx * pointSpacing, points[0].getY() + 3 * dy * pointSpacing);
  }
  
  @Override
  public Rectangle2D getCachingBounds() {
    Shape[] body = getBody();
    int margin = 20;
    double minX = 0;
    double minY = 0;
    double maxX = 0;
    double maxY = 0;
    for (Shape a : body) {
      if (a != null) {
        Rectangle2D bounds2d = a.getBounds2D();
        if (bounds2d.getMinX() < minX)
          minX = bounds2d.getMinX();
        if (bounds2d.getMinY() < minY)
          minY = bounds2d.getMinY();
        if (bounds2d.getMaxX() > maxX)
          maxX = bounds2d.getMaxX();
        if (bounds2d.getMaxY() > maxY)
          maxY = bounds2d.getMaxY();
      }
    }
    return new Rectangle2D.Double(minX - margin, minY - margin, maxX - minX + 2 * margin, maxY - minY + 2 * margin);
  }
  
  public enum Polarity {
    North("Single - North"), South("Single - South"), Humbucking("Humbucking - 4 leads");
    
    private String label;

    private Polarity(String label) {
      this.label = label;
    }
    
    @Override
    public String toString() {
      return label;
    }
  }
}
