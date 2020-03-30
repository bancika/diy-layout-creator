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
package org.diylc.components.boards;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

@ComponentDescriptor(name = "P-0+o (Proto) Board", category = "Boards", author = "Branislav Stojkovic",
    description = "A prototyping board for modular synths, central bus with V+ GND and V-", instanceNamePrefix = "BB",
    zOrder = IDIYComponent.BOARD, bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, autoEdit = false,
    keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME)
public class ProtoBoard extends AbstractComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static Color FILL_COLOR = Color.decode("#0B3B0B");
  public static Color BORDER_COLOR = FILL_COLOR.darker();
  public static Size SPACING = new Size(0.1d, SizeUnit.in);
  public static Color SELECTION_COLOR = Color.red;
  public static Color PAD_COLOR = Color.decode("#D2D2D2");
  public static Color UNDERSIDE_PAD_COLOR = Color.decode("#A8A8DA");
  public static Color UNDERSIDE_LINE_COLOR = Color.decode("#CDCDFF");
  public static Color HOLE_COLOR = Color.black;

  public static float COORDINATE_FONT_SIZE = 9f;
  public static Color COORDINATE_COLOR = Color.gray.brighter(); 
  
  public static Color SEPARATOR_COLOR = Color.white;
  public static Color SCREEN_COLOR = Color.decode("#CAD4CA");  

  public static Size PAD_SIZE = new Size(2d, SizeUnit.mm);  
  public static int ROW_COUNT = 33;
  
  public static Size WIDTH_SIZE = new Size(2d, SizeUnit.in);  
  public static Size LENGTH_SIZE = new Size(4d, SizeUnit.in);  
  
  private static int[] SEGMENTS = new int[] { 2, 3, 3, 3, 3, 2 };
  private static int[] SEGMENTS_SUM = new int[] { 0, 2, 5, 8, 11, 14 };
  private static int[] GAPS = new int[] { 1, 0, 2, 0, 0, 0 };
  private static int[] SEGMENTS_STARTS = new int[] { 0, 3, 6, 11, 14, 17 };  

  protected Point point = new Point(0, 0);

  protected PowerStripPosition powerStripPosition;
  protected Orientation orientation;
  protected Boolean markUndersidePads = true;

  @SuppressWarnings("incomplete-switch")
  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }

    // adjust the angle
    double theta = 0;
    switch (getOrientation()) {
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
    if (theta != 0) {
      g2d.rotate(theta, point.x, point.y);
    }

    double spacing = SPACING.convertToPixels();            

    // draw body
    g2d.setColor(FILL_COLOR);
    int width = (int) WIDTH_SIZE.convertToPixels();
    int length = (int) LENGTH_SIZE.convertToPixels();
    
    g2d.fillRect(point.x, point.y, width, length);
    g2d.setColor(componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
        : BORDER_COLOR);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.drawRect(point.x, point.y, width, length);

    drawingObserver.stopTracking();

    int padSize = getClosestOdd(PAD_SIZE.convertToPixels());
    int holeSize = getClosestOdd(padSize / 2);

    g2d.setFont(LABEL_FONT.deriveFont(COORDINATE_FONT_SIZE));       
    
    int xOffset = 1;
    int yOffset = 5;
    
    g2d.setColor(PAD_COLOR.brighter());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(padSize / 4));
    
    drawingObserver.startTrackingContinuityArea(true);
    
    // draw segment connectors
    for (int y = 0; y < ROW_COUNT; y++) {
      for (int segment = 0; segment < SEGMENTS.length; segment++) {      
        drawingObserver.setContinuityMarker("SEGMENT=" + segment + ";ROW=" + y);
        g2d.drawLine((int) (point.x + (xOffset + SEGMENTS_STARTS[segment]) * spacing),
            (int) (point.y + (yOffset + y) * spacing),
            (int) (point.x + (xOffset + SEGMENTS_STARTS[segment] + SEGMENTS[segment] - 1) * spacing),
            (int) (point.y + (yOffset + y) * spacing));
      }
    }
    drawingObserver.setContinuityMarker(null);
    
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(padSize / 4));
    
    // draw central mini pad connectors, top row
    for (int y = 0; y < ROW_COUNT; y++) { 
      // top row
      g2d.drawLine((int) (point.x + (1 + SEGMENTS_SUM[3]) * spacing), (int) (point.y + (yOffset + y - 0.25d) * spacing + 1), 
          (int) (point.x + (1 + SEGMENTS_SUM[3] + 0.5) * spacing), (int) (point.y + (yOffset + y - 0.25d) * spacing + 1));
      g2d.drawLine((int) (point.x + (1 + SEGMENTS_SUM[3] + 3) * spacing), (int) (point.y + (yOffset + y - 0.25d) * spacing + 1), 
          (int) (point.x + (1 + SEGMENTS_SUM[3] + 2.5) * spacing), (int) (point.y + (yOffset + y - 0.25d) * spacing + 1));
    }
    
    g2d.setColor(getMarkUndersidePads() ? UNDERSIDE_LINE_COLOR : PAD_COLOR.brighter());
    
    // bottom row.
    for (int y = 0; y < ROW_COUNT; y++) {       
      g2d.drawLine((int) (point.x + (1 + SEGMENTS_SUM[3]) * spacing), (int) (point.y + (yOffset + y + 0.25d) * spacing + 1), 
          (int) (point.x + (1 + SEGMENTS_SUM[3] + 1) * spacing), (int) (point.y + (yOffset + y + 0.25d) * spacing + 1));          
      g2d.drawLine((int) (point.x + (1 + SEGMENTS_SUM[3] + 3) * spacing), (int) (point.y + (yOffset + y + 0.25d) * spacing + 1), 
          (int) (point.x + (1 + SEGMENTS_SUM[3] + 2) * spacing), (int) (point.y + (yOffset + y + 0.25d) * spacing + 1));
    }    
    
    // vertical ground line
    g2d.drawLine((int) (point.x + (1 + SEGMENTS_SUM[3] + 1.5) * spacing), (int) (point.y + (yOffset - 1.5) * spacing),
      (int) (point.x + (1 + SEGMENTS_SUM[3] + 1.5) * spacing), (int) (point.y + (yOffset + ROW_COUNT + 0.5) * spacing));
    
    g2d.setColor(PAD_COLOR.brighter());   
    
    // traces to top and bottom 3 pads
    g2d.drawPolyline(new int[] { 
        (int) (point.x + (1 + SEGMENTS_SUM[3] + 0.5) * spacing),
        (int) (point.x + (1 + SEGMENTS_SUM[3] + 1.2) * spacing), 
        (int) (point.x + (1 + SEGMENTS_SUM[3] + 1.2) * spacing)}, 
        new int[] { 
        (int) (point.y + (yOffset - 1.5) * spacing),
        (int) (point.y + (yOffset - 1) * spacing),
        (int) (point.y + (yOffset - 0.3) * spacing)}, 3);
    g2d.drawPolyline(new int[] { 
        (int) (point.x + (1 + SEGMENTS_SUM[3] + 2.5) * spacing),
        (int) (point.x + (1 + SEGMENTS_SUM[3] + 1.8) * spacing),
        (int) (point.x + (1 + SEGMENTS_SUM[3] + 1.8) * spacing)}, 
        new int[] {
        (int) (point.y + (yOffset - 1.5) * spacing),
        (int) (point.y + (yOffset - 1) * spacing),   
        (int) (point.y + (yOffset - 0.3) * spacing)}, 3);
    
    g2d.drawPolyline(new int[] { 
        (int) (point.x + (1 + SEGMENTS_SUM[3] + 0.5) * spacing),
        (int) (point.x + (1 + SEGMENTS_SUM[3] + 1.2) * spacing), 
        (int) (point.x + (1 + SEGMENTS_SUM[3] + 1.2) * spacing)}, 
        new int[] { 
        (int) (point.y + (yOffset + ROW_COUNT + 0.5) * spacing),
        (int) (point.y + (yOffset + ROW_COUNT) * spacing),
        (int) (point.y + (yOffset + ROW_COUNT - 0.35) * spacing)}, 3);
    g2d.drawPolyline(new int[] { 
        (int) (point.x + (1 + SEGMENTS_SUM[3] + 2.5) * spacing),
        (int) (point.x + (1 + SEGMENTS_SUM[3] + 1.8) * spacing),
        (int) (point.x + (1 + SEGMENTS_SUM[3] + 1.8) * spacing)}, 
        new int[] {
        (int) (point.y + (yOffset + ROW_COUNT + 0.5) * spacing),
        (int) (point.y + (yOffset + ROW_COUNT) * spacing),   
        (int) (point.y + (yOffset + ROW_COUNT - 0.35) * spacing)}, 3);
    
    drawingObserver.stopTrackingContinuityArea();
    
    int miniPadSize = getClosestOdd((2d * spacing - 6) / 5);
    
    // draw top mini pad traces
    g2d.setColor(PAD_COLOR.brighter());      
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(padSize / 4));
    drawingObserver.startTrackingContinuityArea(true);
    g2d.drawLine((int) (point.x + (1 + SEGMENTS_SUM[3] + 0.5) * spacing), (int) (point.y + 1 * spacing), 
        (int) (point.x + (1 + SEGMENTS_SUM[3] + 0.5) * spacing), (int) (point.y + 3.5 * spacing));
    g2d.drawLine((int) (point.x + (1 + SEGMENTS_SUM[3] + 1.5) * spacing + miniPadSize / 2), (int) (point.y + 1 * spacing), 
        (int) (point.x + (1 + SEGMENTS_SUM[3] + 1.5) * spacing + miniPadSize / 2), (int) (point.y + 3.5 * spacing));
    g2d.drawLine((int) (point.x + (1 + SEGMENTS_SUM[3] + 2.5) * spacing), (int) (point.y + 1 * spacing), 
        (int) (point.x + (1 + SEGMENTS_SUM[3] + 2.5) * spacing), (int) (point.y + 3.5 * spacing));
    drawingObserver.stopTrackingContinuityArea();
    
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    
    // vertical separators
    g2d.setColor(SEPARATOR_COLOR);
    
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
    
    g2d.drawLine((int) (point.x + (1 + SEGMENTS_STARTS[2] - 0.5) * spacing), (int) (point.y + (yOffset - 1) * spacing),
        (int) (point.x + (1 + SEGMENTS_STARTS[2] - 0.5) * spacing), (int) (point.y + (yOffset + ROW_COUNT -0.5) * spacing));
    g2d.drawLine((int) (point.x + (1 + SEGMENTS_STARTS[4] - 0.5) * spacing), (int) (point.y + (yOffset - 1) * spacing),
        (int) (point.x + (1 + SEGMENTS_STARTS[4] - 0.5) * spacing), (int) (point.y + (yOffset + ROW_COUNT -0.5) * spacing));
    g2d.fillRect((int) (point.x + (1 + SEGMENTS_STARTS[1] - 1.25) * spacing), (int) (point.y + (yOffset - 1) * spacing),
        (int)(0.5 * spacing), (int) ((ROW_COUNT + 0.5) * spacing));
    
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));        
    
    // draw main pads
    for (int segment = 0; segment < SEGMENTS.length; segment++) {
      for (int y = 0; y < ROW_COUNT; y++) {            
        for (int x = 0; x < SEGMENTS[segment]; x++) {
          int padX = (int) (point.x + (xOffset + x) * spacing);
          int padY = (int) (point.y + (yOffset + y) * spacing);
          
          drawingObserver.startTrackingContinuityArea(true);
          drawingObserver.setContinuityMarker("SEGMENT=" + segment + ";ROW=" + y);
          
          g2d.setColor(PAD_COLOR);
          if ((segment == 2 && x == 2) || (segment == 3 && x == 0))
            g2d.fillRect(padX - padSize / 2, padY - padSize / 2, padSize, padSize);
          else
            g2d.fillOval(padX - padSize / 2, padY - padSize / 2, padSize, padSize);
          
          drawingObserver.setContinuityMarker(null);
          
          drawingObserver.stopTrackingContinuityArea();
          
          g2d.setColor(PAD_COLOR.darker());          
          if ((segment == 2 && x == 2) || (segment == 3 && x == 0))
            g2d.drawRect(padX - padSize / 2, padY - padSize / 2, padSize, padSize);
          else
            g2d.drawOval(padX - padSize / 2, padY - padSize / 2, padSize, padSize);
          
          g2d.setColor(HOLE_COLOR);
          g2d.fillOval(padX - holeSize / 2, padY - holeSize / 2, holeSize, holeSize);   
          
          // Draw horizontal labels
          if (y == 0) {
            g2d.setColor(COORDINATE_COLOR);
            StringUtils.drawCenteredText(g2d, Integer.toString(SEGMENTS_SUM[segment] + x + 1), padX, (int) (point.y + (yOffset - 1) * spacing) - 1,
                HorizontalAlignment.CENTER, VerticalAlignment.TOP);
          }
        }
      }
      
      xOffset += SEGMENTS[segment] + GAPS[segment];
    }    
    
    // Draw top and bottom 3 pads
    for (int y = 0; y < 2; y++) {
      g2d.setColor(SEPARATOR_COLOR);
      g2d.drawRect((int) (point.x + (1 + SEGMENTS_SUM[3]) * spacing), (int) (point.y + (yOffset - 2 + y * (ROW_COUNT + 2)) * spacing), 
          (int)(3 * spacing), (int)spacing);
      
      drawingObserver.startTrackingContinuityArea(true);
      
      for (int x = 0; x < 3; x++) {      
        int padX = (int) (point.x + (1 + SEGMENTS_SUM[3] + 0.5 + x) * spacing);
        int padY = (int) (point.y + (yOffset - 1.5 + y * (ROW_COUNT + 2)) * spacing);        
        
        g2d.setColor(PAD_COLOR);
        
        if (x == 0)
          drawingObserver.setContinuityMarker("columnA");
        else if (x == 2)
          drawingObserver.setContinuityMarker("columnC");
        
        if (x == 2)
          g2d.fillRect(padX - padSize / 2, padY - padSize / 2, padSize, padSize);
        else
          g2d.fillOval(padX - padSize / 2, padY - padSize / 2, padSize, padSize);
        
        drawingObserver.setContinuityMarker(null);
        
        g2d.setColor(PAD_COLOR.darker());
        if (x == 2)
          g2d.drawRect(padX - padSize / 2, padY - padSize / 2, padSize, padSize);
        else
          g2d.drawOval(padX - padSize / 2, padY - padSize / 2, padSize, padSize);
        g2d.setColor(HOLE_COLOR);
        g2d.fillOval(padX - holeSize / 2, padY - holeSize / 2, holeSize, holeSize);
      }
      
      drawingObserver.stopTrackingContinuityArea();
    }    
                
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
    
    g2d.setColor(SCREEN_COLOR);
    g2d.fillRect((int) (point.x + width - 1.5 * spacing), point.y + 2, (int) (spacing), (int) (2 * spacing));
    g2d.setColor(SEPARATOR_COLOR);
    g2d.drawRect((int) (point.x + width - 5.5 * spacing), point.y + 2, (int) (5 * spacing), (int) (2 * spacing));
    
    g2d.drawLine((int) (point.x + width - 5.5 * spacing), (int) (point.y + 2 * spacing) + 8, 
        (int) (point.x + width - 5.5 * spacing), (int) (point.y + 3.5 * spacing));
    g2d.drawLine((int) (point.x + width - 4.5 * spacing), (int) (point.y + 2 * spacing + 8), 
        (int) (point.x + width - 4.5 * spacing), (int) (point.y + 3.5 * spacing));
    g2d.drawLine((int) (point.x + width - 1.5 * spacing), (int) (point.y + 2 * spacing + 8), 
        (int) (point.x + width - 1.5 * spacing), (int) (point.y + 3.5 * spacing));
    
    g2d.setColor(PAD_COLOR.brighter());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(padSize / 4));
    
    drawingObserver.startTrackingContinuityArea(true);     
    
    // Draw two horizontal connections in the top-right section
    g2d.drawLine((int) (point.x + width - 4 * spacing), (int) (point.y + 0.5 * spacing), (int) (point.x + width - (2) * spacing), (int) (point.y + 0.5 * spacing));
    g2d.drawLine((int) (point.x + width - 4 * spacing), (int) (point.y + 1.5 * spacing), (int) (point.x + width - (2) * spacing), (int) (point.y + 1.5 * spacing));        
    
    // Draw top-right pads
    for (int x = 0; x < 6; x++) { 
      int padX = (int) (point.x + width - (x + 1) * spacing);

      g2d.setColor(PAD_COLOR.brighter());      
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(padSize / 4));
      g2d.drawLine(padX, (int) (point.y + 0.5 * spacing), padX, (int) (point.y + 3 * spacing));
      
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      
      for (int y = 0; y < 3; y++) {      
        
        int padY = (int) (point.y + (y + (y == 2 ? 0.5 : 0) + 0.5) * spacing);
        
        if (x == 0)
          drawingObserver.setContinuityMarker("-12V");
        else if (x == 4)
            drawingObserver.setContinuityMarker("+12V");
        else if (x == 5)
          drawingObserver.setContinuityMarker("+5V");
        else
          drawingObserver.setContinuityMarker("GND");
          
        g2d.setColor(PAD_COLOR);
        if (x >= 4)
          g2d.fillRect(padX - padSize / 2, padY - padSize / 2, padSize, padSize);
        else
          g2d.fillOval(padX - padSize / 2, padY - padSize / 2, padSize, padSize);
        
        drawingObserver.setContinuityMarker(null);
        
        g2d.setColor(PAD_COLOR.darker());
        if (x >= 4)
          g2d.drawRect(padX - padSize / 2, padY - padSize / 2, padSize, padSize);
        else
          g2d.drawOval(padX - padSize / 2, padY - padSize / 2, padSize, padSize);
        g2d.setColor(HOLE_COLOR);
        g2d.fillOval(padX - holeSize / 2, padY - holeSize / 2, holeSize, holeSize);
      }
    }
    
    drawingObserver.stopTrackingContinuityArea();   
    
    // draw top mini pads
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    for (int y = 0; y < 4; y++) {
      int padY = (int) (point.y + (0.5 * y + 1) * spacing);      
      int[] padXs = new int[] {(int) (point.x + (1 + SEGMENTS_SUM[3] + 0.5) * spacing - miniPadSize - 2), (int) (point.x + (1 + SEGMENTS_SUM[3] + 0.5) * spacing),
          (int) (point.x + (1 + SEGMENTS_SUM[3] + 1.5) * spacing) - miniPadSize / 2 - 2, (int) (point.x + (1 + SEGMENTS_SUM[3] + 1.5) * spacing) + miniPadSize / 2 + 1,
          (int) (point.x + (1 + SEGMENTS_SUM[3] + 2.5) * spacing), (int) (point.x + (1 + SEGMENTS_SUM[3] + 2.5) * spacing + miniPadSize + 2)};
      
      for (int i = 0; i < padXs.length; i++) {          
        int padX = padXs[i];
        drawingObserver.startTrackingContinuityArea(true);
        
        if (i == 0 || i == 2 || i == 5) {
          switch (y) {
            case 0: drawingObserver.setContinuityMarker("+5V");
                    break;
            case 1: drawingObserver.setContinuityMarker("+12V");
                    break;
            case 2: drawingObserver.setContinuityMarker("GND");
                    break;
            case 3: drawingObserver.setContinuityMarker("-12V");
                    break;
          }
        }
      
        g2d.setColor(PAD_COLOR);        
        g2d.fillRect(padX - miniPadSize / 2, padY - miniPadSize / 2, miniPadSize, miniPadSize);
        
        drawingObserver.setContinuityMarker(null);
      
        drawingObserver.stopTrackingContinuityArea();
        g2d.setColor(PAD_COLOR.darker());
        g2d.drawRect(padX - miniPadSize / 2, padY - miniPadSize / 2, miniPadSize, miniPadSize);
      }
    }        
    
    g2d.setColor(COORDINATE_COLOR);
    
    // Draw top labels
    StringUtils.drawCenteredText(g2d, "+5V", (int) (point.x + (1 + SEGMENTS_SUM[3] - 0.3) * spacing), (int) (point.y + 1 * spacing) + 1, 
        HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
    StringUtils.drawCenteredText(g2d, "+12V", (int) (point.x + (1 + SEGMENTS_SUM[3] - 0.3) * spacing), (int) (point.y + 1.5 * spacing) + 1, 
        HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
    StringUtils.drawCenteredText(g2d, "GND", (int) (point.x + (1 + SEGMENTS_SUM[3] - 0.3) * spacing), (int) (point.y + 2 * spacing) + 1, 
        HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
    StringUtils.drawCenteredText(g2d, "-12V", (int) (point.x + (1 + SEGMENTS_SUM[3] - 0.3) * spacing), (int) (point.y + 2.5 * spacing) + 1, 
        HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
    
    // Draw top-right labels
    int labelY = (int) (point.y + 3.3 * spacing);
    StringUtils.drawCenteredText(g2d, "+5V", (int) (point.x + width - 6 * spacing), labelY, HorizontalAlignment.CENTER, VerticalAlignment.TOP);
    StringUtils.drawCenteredText(g2d, "+", (int) (point.x + width - 5 * spacing) - 6, labelY, HorizontalAlignment.CENTER, VerticalAlignment.TOP);
    StringUtils.drawCenteredText(g2d, " 12V", (int) (point.x + width - 5 * spacing) + 2, labelY, HorizontalAlignment.CENTER, VerticalAlignment.TOP);
    StringUtils.drawCenteredText(g2d, "GND", (int) (point.x + width - 3 * spacing), labelY, HorizontalAlignment.CENTER, VerticalAlignment.TOP);
    StringUtils.drawCenteredText(g2d, "-12V", (int) (point.x + width - 1 * spacing), labelY, HorizontalAlignment.CENTER, VerticalAlignment.TOP);

    // Draw vertical labels
    byte A = "A".getBytes()[0];
    byte a = "a".getBytes()[0];
    for (int y = 0; y < ROW_COUNT; y++) { 
      int padY = (int) (point.y + (yOffset + y) * spacing);
      String label;
      if (y >= 26)
        label = new String(new byte[] {(byte) (a + y - 26)});
      else
        label = new String(new byte[] {(byte) (A + y)});
      StringUtils.drawCenteredText(g2d, label, (int) (point.x + spacing * 0.35), padY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      StringUtils.drawCenteredText(g2d, label, (int) (point.x + width - spacing * 0.35), padY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    }
    
    g2d.setColor(PAD_COLOR.brighter());
    g2d.setFont(LABEL_FONT.deriveFont(COORDINATE_FONT_SIZE + 2));
    
    StringUtils.drawCenteredText(g2d, "P-0+o by:\nKristian Blåsol 2019", (int) (point.x + width * 0.70d), (int) (point.y + (yOffset + ROW_COUNT + 0.4) * spacing), 
        HorizontalAlignment.LEFT, VerticalAlignment.TOP);        
    
    // draw central mini pads
    for (int y = 0; y < ROW_COUNT; y++) { 
      g2d.setColor(PAD_COLOR);
      
      drawingObserver.startTrackingContinuityArea(true);
      
      // top row      
      g2d.fillRect((int) (point.x + (1 + SEGMENTS_SUM[3] + 0.5) * spacing) + 1, (int) (point.y + (yOffset + y - 0.5d) * spacing + 2), miniPadSize, miniPadSize);
      drawingObserver.setContinuityMarker("columnA");
      g2d.fillRect((int) (point.x + (1 + SEGMENTS_SUM[3] + 0.5) * spacing) + miniPadSize + 3, (int) (point.y + (yOffset + y - 0.5d) * spacing + 2), miniPadSize, miniPadSize);      
      drawingObserver.setContinuityMarker("columnC");
      g2d.fillRect((int) (point.x + (1 + SEGMENTS_SUM[3] + 1.5) * spacing) + 3, (int) (point.y + (yOffset + y - 0.5d) * spacing + 2), miniPadSize, miniPadSize);
      drawingObserver.setContinuityMarker(null);
      g2d.fillRect((int) (point.x + (1 + SEGMENTS_SUM[3] + 1.5) * spacing) + miniPadSize + 5, (int) (point.y + (yOffset + y - 0.5d) * spacing + 2), miniPadSize, miniPadSize);
      
      drawingObserver.stopTrackingContinuityArea();
            
      g2d.setColor(PAD_COLOR.darker());
      g2d.drawRect((int) (point.x + (1 + SEGMENTS_SUM[3] + 0.5) * spacing) + 1, (int) (point.y + (yOffset + y - 0.5d) * spacing + 2), miniPadSize, miniPadSize);
      g2d.drawRect((int) (point.x + (1 + SEGMENTS_SUM[3] + 0.5) * spacing) + miniPadSize + 3, (int) (point.y + (yOffset + y - 0.5d) * spacing + 2), miniPadSize, miniPadSize);
      
      g2d.setColor(getMarkUndersidePads() ? UNDERSIDE_PAD_COLOR : PAD_COLOR);
      
      drawingObserver.startTrackingContinuityArea(true);
      
      // bottom row      
      
      g2d.fillRect((int) (point.x + (1 + SEGMENTS_SUM[3] + 1.5) * spacing) - miniPadSize / 2, (int) (point.y + (yOffset + y + 0.5d) * spacing - 1 - miniPadSize), miniPadSize, miniPadSize);
//      drawingObserver.setContinuityMarker("columnB");      
      g2d.fillRect((int) (point.x + (1 + SEGMENTS_SUM[3] + 1.5) * spacing) - miniPadSize * 3 / 2 - 2, (int) (point.y + (yOffset + y + 0.5d) * spacing - 1 - miniPadSize), miniPadSize, miniPadSize);
//      drawingObserver.setContinuityMarker("null");
      g2d.fillRect((int) (point.x + (1 + SEGMENTS_SUM[3] + 1.5) * spacing) + miniPadSize / 2 + 3, (int) (point.y + (yOffset + y + 0.5d) * spacing - 1 - miniPadSize), miniPadSize, miniPadSize);
      
      drawingObserver.stopTrackingContinuityArea();
      
      g2d.setColor((getMarkUndersidePads() ? UNDERSIDE_PAD_COLOR : PAD_COLOR).darker());
      
      g2d.drawRect((int) (point.x + (1 + SEGMENTS_SUM[3] + 1.5) * spacing) + 3, (int) (point.y + (yOffset + y - 0.5d) * spacing + 2), miniPadSize, miniPadSize);
      g2d.drawRect((int) (point.x + (1 + SEGMENTS_SUM[3] + 1.5) * spacing) + miniPadSize + 5, (int) (point.y + (yOffset + y - 0.5d) * spacing + 2), miniPadSize, miniPadSize);
      
      g2d.drawRect((int) (point.x + (1 + SEGMENTS_SUM[3] + 1.5) * spacing) - miniPadSize / 2, (int) (point.y + (yOffset + y + 0.5d) * spacing - 1 - miniPadSize), miniPadSize, miniPadSize);
      g2d.drawRect((int) (point.x + (1 + SEGMENTS_SUM[3] + 1.5) * spacing) - miniPadSize * 3 / 2 - 2, (int) (point.y + (yOffset + y + 0.5d) * spacing - 1 - miniPadSize), miniPadSize, miniPadSize);
      g2d.drawRect((int) (point.x + (1 + SEGMENTS_SUM[3] + 1.5) * spacing) + miniPadSize / 2 + 3, (int) (point.y + (yOffset + y + 0.5d) * spacing - 1 - miniPadSize), miniPadSize, miniPadSize);
    }        
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int factor = 32 / width;
    g2d.setColor(FILL_COLOR);
    g2d.fillRect(2 / factor, 2 / factor, width - 4 / factor, height - 4 / factor);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect(2 / factor, 2 / factor, width - 4 / factor, height - 4 / factor);
    
    g2d.setColor(PAD_COLOR.brighter());
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2 / factor));
    
    int size = (int) (8.0 / factor);
    
    g2d.drawLine(width / 3 - 2 / factor + size / 2, 6 / factor + size / 2, width, 6 / factor + size / 2);
    g2d.drawLine(width / 3 - 2 / factor + size / 2, 2 * width / 3 - 2 / factor + size / 2, width, 2 * width / 3 - 2 / factor + size / 2);
    
    
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));

    g2d.setColor(PAD_COLOR);
    g2d.fillRect(width / 3 - 2 / factor, 6 / factor, size, size);
    g2d.setColor(PAD_COLOR.darker());
    g2d.drawRect(width / 3 - 2 / factor, 6 / factor, size, size);

    g2d.setColor(PAD_COLOR);
    g2d.fillOval(2 * width / 3 - 2 / factor, 6 / factor, size, size);
    g2d.setColor(PAD_COLOR.darker());
    g2d.drawOval(2 * width / 3 - 2 / factor, 6 / factor, size, size);

    g2d.setColor(PAD_COLOR);
    g2d.fillRect(width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, size, size);
    g2d.setColor(PAD_COLOR.darker());
    g2d.drawRect(width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, size, size);

    g2d.setColor(PAD_COLOR);
    g2d.fillOval(2 * width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, size, size);
    g2d.setColor(PAD_COLOR.darker());
    g2d.drawOval(2 * width / 3 - 2 / factor, 2 * width / 3 - 2 / factor, size, size);
    
    g2d.setColor(Color.darkGray);
    g2d.fillOval(width / 3 - 2 / factor + size / 2 - 1, 6 / factor + size / 2 - 1, 3, 3);
    g2d.fillOval(width / 3 - 2 / factor + size / 2 - 1, 2 * width / 3 - 2 / factor + size / 2 - 1, 3, 3);
    g2d.fillOval(2 * width / 3 - 2 / factor + size / 2 - 1, 6 / factor + size / 2 - 1, 3, 3);
    g2d.fillOval(2 * width / 3 - 2 / factor + size / 2 - 1, 2 * width / 3 - 2 / factor + size / 2 - 1, 3, 3);
  }

  @EditableProperty
  public Orientation getOrientation() {
    if (orientation == null)
      orientation = Orientation.DEFAULT;
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
  }

  @EditableProperty(name = "Power Strip")
  public PowerStripPosition getPowerStripPosition() {
    if (powerStripPosition == null)
      powerStripPosition = PowerStripPosition.Inline;
    return powerStripPosition;
  }

  public void setPowerStripPosition(PowerStripPosition powerStripPosition) {
    this.powerStripPosition = powerStripPosition;
  }
  
  @EditableProperty(name = "Mark Underside Pads")
  public Boolean getMarkUndersidePads() {
    return markUndersidePads;
  }
  
  public void setMarkUndersidePads(Boolean markUndersidePads) {
    this.markUndersidePads = markUndersidePads;
  }

  @Override
  public int getControlPointCount() {
    return 2;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public Point getControlPoint(int index) {
    if (index == 0)
      return point;
    
    // create a synthetic second control point for clipping check and to make sure that we cannot drag the right side of the board off the screen

    // adjust the angle
    double theta = 0;
    switch (getOrientation()) {
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
    int width = (int) WIDTH_SIZE.convertToPixels();
    int length = (int) LENGTH_SIZE.convertToPixels();
    Point secondPoint = new Point((int) (point.x + width), (int) (point.y + length));
    if (theta != 0) {
      AffineTransform tx = AffineTransform.getRotateInstance(theta, point.x, point.y);
      tx.transform(secondPoint, secondPoint);
    }
    return secondPoint;
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
    if (index == 0)
      this.point.setLocation(point);
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}
  
  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }

  public enum PowerStripPosition {
    Inline, Offset
  }
}
