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
package org.diylc.serialization;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.diylc.common.Display;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.Orientation;
import org.diylc.common.OrientationHV;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.components.boards.AbstractBoard;
import org.diylc.components.boards.BlankBoard;
import org.diylc.components.boards.PerfBoard;
import org.diylc.components.boards.VeroBoard;
import org.diylc.components.connectivity.CopperTrace;
import org.diylc.components.connectivity.HookupWire;
import org.diylc.components.connectivity.Jumper;
import org.diylc.components.connectivity.SolderPad;
import org.diylc.components.connectivity.TraceCut;
import org.diylc.components.electromechanical.MiniToggleSwitch;
import org.diylc.components.electromechanical.ToggleSwitchType;
import org.diylc.components.misc.BOM;
import org.diylc.components.misc.Label;
import org.diylc.components.passive.PotentiometerPanel;
import org.diylc.components.passive.RadialElectrolytic;
import org.diylc.components.passive.RadialFilmCapacitor;
import org.diylc.components.passive.Resistor;
import org.diylc.components.passive.Taper;
import org.diylc.components.passive.TrimmerPotentiometer;
import org.diylc.components.passive.TrimmerPotentiometer.TrimmerType;
import org.diylc.components.semiconductors.DIL_IC;
import org.diylc.components.semiconductors.DiodePlastic;
import org.diylc.components.semiconductors.LED;
import org.diylc.components.semiconductors.SIL_IC;
import org.diylc.components.semiconductors.TransistorTO92;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.presenter.CalcUtils;
import org.diylc.presenter.ComparatorFactory;
import org.diylc.serialization.IOldFileParser;
import org.diylc.utils.Constants;

public class V1FileParser implements IOldFileParser {

  private static final Logger LOG = Logger.getLogger(V1FileParser.class);

  private static final Size V1_GRID_SPACING = new Size(0.1d, SizeUnit.in);
  private static final Map<String, Color> V1_COLOR_MAP = new HashMap<String, Color>();
  static {
    V1_COLOR_MAP.put("red", Color.red);
    V1_COLOR_MAP.put("blue", Color.blue);
    V1_COLOR_MAP.put("white", Color.white);
    V1_COLOR_MAP.put("green", Color.green.darker());
    V1_COLOR_MAP.put("black", Color.black);
    V1_COLOR_MAP.put("yellow", Color.yellow);
  }

  @Override
  public boolean canParse(String version) {
    return version == null || version.trim().isEmpty();
  }

  public Project parseFile(Element root, List<String> warnings) {
    Project project = new Project();
    project.setTitle(root.getAttribute("Project"));
    project.setAuthor(root.getAttribute("Credits"));
    project.setGridSpacing(V1_GRID_SPACING);
    project.setDescription("Automatically converted from V1 format.");
    String type = root.getAttribute("Type");

    // Create the board.
    int width = Integer.parseInt(root.getAttribute("Width")) + 1;
    int height = Integer.parseInt(root.getAttribute("Height")) + 1;
    int boardWidth = (int) (width * Constants.PIXELS_PER_INCH * V1_GRID_SPACING.getValue());
    int boardHeight = (int) (height * Constants.PIXELS_PER_INCH * V1_GRID_SPACING.getValue());
    int projectWidth = (int) project.getWidth().convertToPixels();
    int projectHeight = (int) project.getHeight().convertToPixels();
    int x = (projectWidth - boardWidth) / 2;
    int y = (projectHeight - boardHeight) / 2;
    AbstractBoard board;
    if (type.equalsIgnoreCase("pcb")) {
      board = new BlankBoard();
      board.setBoardColor(Color.white);
    } else if (type.equalsIgnoreCase("perfboard")) {
      board = new PerfBoard();
    } else if (type.equalsIgnoreCase("stripboard")) {
      board = new VeroBoard();
    } else {
      throw new IllegalArgumentException("Unrecognized board type: " + type);
    }
    board.setName("Main board");
    Point2D referencePoint =
        new Point2D.Double(CalcUtils.roundToGrid(x, V1_GRID_SPACING), CalcUtils.roundToGrid(y, V1_GRID_SPACING));
    board.setControlPoint(referencePoint, 0);
    board.setControlPoint(
        new Point2D.Double(CalcUtils.roundToGrid(x + boardWidth, V1_GRID_SPACING), CalcUtils.roundToGrid(y + boardHeight,
            V1_GRID_SPACING)), 1);
    project.getComponents().add(board);

    NodeList childNodes = root.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        String nodeName = node.getNodeName();
        String nameAttr = node.getAttributes().getNamedItem("Name").getNodeValue();
        Node valueNode = node.getAttributes().getNamedItem("Value");
        String valueAttr = valueNode == null ? null : valueNode.getNodeValue();
        int x1Attr = Integer.parseInt(node.getAttributes().getNamedItem("X1").getNodeValue());
        int y1Attr = Integer.parseInt(node.getAttributes().getNamedItem("Y1").getNodeValue());
        Point2D point1 = convertV1CoordinatesToV3Point(referencePoint, x1Attr, y1Attr);
        Point2D point2 = null;
        Integer x2Attr = null;
        Integer y2Attr = null;
        Color color = null;
        if (node.getAttributes().getNamedItem("Color") != null) {
          String colorAttr = node.getAttributes().getNamedItem("Color").getNodeValue();
          color = V1_COLOR_MAP.get(colorAttr.toLowerCase());
        }
        if (node.getAttributes().getNamedItem("X2") != null && node.getAttributes().getNamedItem("Y2") != null) {
          x2Attr = Integer.parseInt(node.getAttributes().getNamedItem("X2").getNodeValue());
          y2Attr = Integer.parseInt(node.getAttributes().getNamedItem("Y2").getNodeValue());
          point2 = convertV1CoordinatesToV3Point(referencePoint, x2Attr, y2Attr);
        }
        IDIYComponent<?> component = null;
        if (nodeName.equalsIgnoreCase("text")) {
          LOG.debug("Recognized " + nodeName);
          Label label = new Label();
          label.setName(nameAttr);
          if (color != null) {
            label.setColor(color);
          }
          label.setValue(valueAttr);
          label.setHorizontalAlignment(HorizontalAlignment.LEFT);
          label.setVerticalAlignment(VerticalAlignment.CENTER);
          label.setControlPoint(convertV1CoordinatesToV3Point(referencePoint, x1Attr, y1Attr), 0);
          component = label;
        } else if (nodeName.equalsIgnoreCase("pad")) {
          LOG.debug("Recognized " + nodeName);
          SolderPad pad = new SolderPad();
          pad.setName(nameAttr);
          if (color != null) {
            pad.setLeadColor(color);
          }
          pad.setControlPoint(convertV1CoordinatesToV3Point(referencePoint, x1Attr, y1Attr), 0);
          component = pad;
        } else if (nodeName.equalsIgnoreCase("cut")) {
          LOG.debug("Recognized " + nodeName);
          TraceCut cut = new TraceCut();
          cut.setCutBetweenHoles(false);
          cut.setName(nameAttr);
          cut.setControlPoint(convertV1CoordinatesToV3Point(referencePoint, x1Attr, y1Attr), 0);
          component = cut;
        } else if (nodeName.equalsIgnoreCase("trace")) {
          LOG.debug("Recognized " + nodeName);
          CopperTrace trace = new CopperTrace();
          trace.setName(nameAttr);
          if (color != null) {
            trace.setLeadColor(color);
          }
          trace.setControlPoint(point1, 0);
          trace.setControlPoint(point2, 1);
          component = trace;
        } else if (nodeName.equalsIgnoreCase("jumper")) {
          LOG.debug("Recognized " + nodeName);
          Jumper jumper = new Jumper();
          jumper.setName(nameAttr);
          jumper.setControlPoint(point1, 0);
          jumper.setControlPoint(point2, 1);
          component = jumper;
        } else if (nodeName.equalsIgnoreCase("wire")) {
          LOG.debug("Recognized " + nodeName);
          HookupWire wire = new HookupWire();
          long seed = Long.parseLong(node.getAttributes().getNamedItem("Seed").getNodeValue());
          Random r = new Random(seed);
          randSeed = seed;
          int d = (int) Math.round(Math.sqrt(Math.pow(point1.getX() - point2.getX(), 2) + Math.pow(point1.getY() - point2.getY(), 2)) / 2);
          int x2 = (int) (point1.getX() + Math.round((point2.getX() - point1.getX()) * 0.40) + myRandom(d, r));
          int y2 = (int) (point1.getY() + Math.round((point2.getY() - point1.getY()) * 0.40) + myRandom(d, r));
          int x3 = (int) (point1.getX() + Math.round((point2.getX() - point1.getX()) * 0.60) + myRandom(d, r));
          int y3 = (int) (point1.getY() + Math.round((point2.getY() - point1.getY()) * 0.60) + myRandom(d, r));

          wire.setName(nameAttr);
          String colorAttr = node.getAttributes().getNamedItem("Color").getNodeValue();
          wire.setLeadColor(parseV1Color(colorAttr));
          wire.setControlPoint(point1, 0);
          wire.setControlPoint(new Point(x2, y2), 1);
          wire.setControlPoint(new Point(x3, y3), 2);
          wire.setControlPoint(point2, 3);
          component = wire;
        } else if (nodeName.equalsIgnoreCase("resistor")) {
          LOG.debug("Recognized " + nodeName);
          Resistor resistor = new Resistor();
          resistor.setName(nameAttr);
          try {
            resistor.setValue(Resistance.parseResistance(valueAttr));
          } catch (Exception e) {
            LOG.debug("Could not set value of " + nameAttr);
          }
          resistor.setLength(new Size(6.35d, SizeUnit.mm));
          resistor.setWidth(new Size(2.2d, SizeUnit.mm));
          resistor.setControlPoint(point1, 0);
          resistor.setControlPoint(point2, 1);
          component = resistor;
        } else if (nodeName.equalsIgnoreCase("capacitor")) {
          LOG.debug("Recognized " + nodeName);
          RadialFilmCapacitor capacitor = new RadialFilmCapacitor();
          capacitor.setName(nameAttr);
          try {
            capacitor.setValue(Capacitance.parseCapacitance(valueAttr));
          } catch (Exception e) {
            LOG.debug("Could not set value of " + nameAttr);
          }
          capacitor.setLength(new Size(6d, SizeUnit.mm));
          capacitor.setWidth(new Size(2d, SizeUnit.mm));
          capacitor.setControlPoint(point1, 0);
          capacitor.setControlPoint(point2, 1);
          component = capacitor;
        } else if (nodeName.equalsIgnoreCase("electrolyte")) {
          LOG.debug("Recognized " + nodeName);
          RadialElectrolytic capacitor = new RadialElectrolytic();
          capacitor.setName(nameAttr);
          try {
            capacitor.setValue(Capacitance.parseCapacitance(valueAttr));
          } catch (Exception e) {
            LOG.debug("Could not set value of " + nameAttr);
          }
          try {
            String sizeAttr = node.getAttributes().getNamedItem("Size").getNodeValue();
            if (sizeAttr.equalsIgnoreCase("small")) {
              capacitor.setLength(new Size(3.5d, SizeUnit.mm));
            } else if (sizeAttr.equalsIgnoreCase("medium")) {
              capacitor.setLength(new Size(5d, SizeUnit.mm));
            } else if (sizeAttr.equalsIgnoreCase("large")) {
              capacitor.setLength(new Size(7d, SizeUnit.mm));
            } else {
              capacitor.setLength(new Size(4d, SizeUnit.mm));
            }
          } catch (Exception e) {
            capacitor.setLength(new Size(5d, SizeUnit.mm));
            LOG.debug("Could not set size of " + nameAttr);
          }
          capacitor.setControlPoint(point1, 0);
          capacitor.setControlPoint(point2, 1);
          component = capacitor;
        } else if (nodeName.equalsIgnoreCase("diode")) {
          LOG.debug("Recognized " + nodeName);
          DiodePlastic capacitor = new DiodePlastic();
          capacitor.setName(nameAttr);
          try {
            capacitor.setValue(valueAttr);
          } catch (Exception e) {
            LOG.debug("Could not set value of " + nameAttr);
          }
          capacitor.setLength(new Size(6d, SizeUnit.mm));
          capacitor.setWidth(new Size(2d, SizeUnit.mm));
          capacitor.setControlPoint(point1, 0);
          capacitor.setControlPoint(point2, 1);
          component = capacitor;
        } else if (nodeName.equalsIgnoreCase("led")) {
          LOG.debug("Recognized " + nodeName);
          LED led = new LED();
          led.setName(nameAttr);
          led.setValue(valueAttr);
          led.setBodyColor(Color.red);
          led.setBorderColor(Color.red.darker());
          led.setLength(new Size(3d, SizeUnit.mm));
          led.setControlPoint(point1, 0);
          led.setControlPoint(point2, 1);
          component = led;
        } else if (nodeName.equalsIgnoreCase("transistor")) {
          LOG.debug("Recognized " + nodeName);
          TransistorTO92 transistor = new TransistorTO92();
          transistor.setName(nameAttr);
          try {
            transistor.setValue(valueAttr);
          } catch (Exception e) {
            LOG.debug("Could not set value of " + nameAttr);
          }
          transistor.setControlPoint(point1, 0);
          if (point1.getY() > point2.getY()) {
            transistor.setOrientation(Orientation._180);
          } else if (point1.getY() < point2.getY()) {
            transistor.setOrientation(Orientation.DEFAULT);
          } else if (point1.getX() < point2.getX()) {
            transistor.setOrientation(Orientation._270);
          } else if (point1.getX() > point2.getX()) {
            transistor.setOrientation(Orientation._90);
          }
          // capacitor.setControlPoint(point2, 1);
          component = transistor;
        } else if (nodeName.equalsIgnoreCase("ic")) {
          LOG.debug("Recognized " + nodeName);
          DIL_IC ic = new DIL_IC();
          int pinCount = 8;
          int rowSpace = 3;
          if (x1Attr < x2Attr && y1Attr < y2Attr) {
            pinCount = (y2Attr - y1Attr + 1) * 2;
            rowSpace = x2Attr - x1Attr;
            ic.setOrientation(Orientation.DEFAULT);
          } else if (x1Attr > x2Attr && y1Attr < y2Attr) {
            pinCount = (x1Attr - x2Attr + 1) * 2;
            rowSpace = y2Attr - y1Attr;
            ic.setOrientation(Orientation._90);
          } else if (x1Attr > x2Attr && y1Attr > y2Attr) {
            rowSpace = x1Attr - x2Attr;
            pinCount = (y1Attr - y2Attr + 1) * 2;
            ic.setOrientation(Orientation._180);
          } else if (x1Attr < x2Attr && y1Attr > y2Attr) {
            rowSpace = y1Attr - y2Attr;
            pinCount = (x2Attr - x1Attr + 1) * 2;
            ic.setOrientation(Orientation._270);
          }
          ic.setRowSpacing(new Size(0.1 * rowSpace, SizeUnit.in));
          ic.setPinCount(DIL_IC.PinCount.valueOf("_" + pinCount));
          ic.setName(nameAttr);
          // Translate control points.
          for (int j = 0; j < ic.getControlPointCount(); j++) {
            Point2D oldP = ic.getControlPoint(j);
            Point2D p = new Point2D.Double(oldP.getX() + point1.getX(), oldP.getY() + point1.getY());            
            ic.setControlPoint(p, j);
          }
          ic.setValue(valueAttr);
          component = ic;
        } else if (nodeName.equalsIgnoreCase("switch")) {
          LOG.debug("Recognized " + nodeName);
          MiniToggleSwitch sw = new MiniToggleSwitch();
          int sizeX = Math.abs(x1Attr - x2Attr);
          int sizeY = Math.abs(y1Attr - y2Attr);
          ToggleSwitchType switchType = null;
          OrientationHV orientation = null;
          if (Math.min(sizeX, sizeY) == 0 && Math.max(sizeX, sizeY) == 1) {
            switchType = ToggleSwitchType.SPST;
            orientation = sizeX < sizeY ? OrientationHV.VERTICAL : OrientationHV.HORIZONTAL;
          }
          if (Math.min(sizeX, sizeY) == 0 && Math.max(sizeX, sizeY) == 2) {
            switchType = ToggleSwitchType.SPDT;
            orientation = sizeX < sizeY ? OrientationHV.VERTICAL : OrientationHV.HORIZONTAL;
          }
          if (Math.min(sizeX, sizeY) == 1 && Math.max(sizeX, sizeY) == 2) {
            switchType = ToggleSwitchType.DPDT;
            orientation = sizeX < sizeY ? OrientationHV.VERTICAL : OrientationHV.HORIZONTAL;
          }
          if (Math.min(sizeX, sizeY) == 2 && Math.max(sizeX, sizeY) == 2) {
            switchType = ToggleSwitchType._3PDT;
            orientation = OrientationHV.VERTICAL;
          }
          if (Math.min(sizeX, sizeY) == 2 && Math.max(sizeX, sizeY) == 3) {
            switchType = ToggleSwitchType._4PDT;
            orientation = sizeX < sizeY ? OrientationHV.HORIZONTAL : OrientationHV.VERTICAL;
          }
          if (Math.min(sizeX, sizeY) == 2 && Math.max(sizeX, sizeY) == 4) {
            switchType = ToggleSwitchType._5PDT;
            orientation = sizeX < sizeY ? OrientationHV.HORIZONTAL : OrientationHV.HORIZONTAL;
          }

          if (switchType == null || orientation == null) {
            String message = "Unsupported toggle switch dimensions";
            LOG.debug(message);
            if (!warnings.contains(message)) {
              warnings.add(message);
            }
          } else {
            sw.setName(nameAttr);
            sw.setOrientation(orientation);
            sw.setValue(switchType);
            sw.setSpacing(new Size(0.1, SizeUnit.in));
            // compensate for potential negative coordinates after the type and orientation have
            // been set. Make sure that the top left corner is at (0, 0)
            double dx = 0;
            double dy = 0;
            for (int j = 0; j < sw.getControlPointCount(); j++) {
              Point2D oldP = sw.getControlPoint(j);
              if (oldP.getX() < 0 && oldP.getX() < dx)
                dx = oldP.getX();
              if (oldP.getY() < 0 && oldP.getY() < dy)
                dy = oldP.getY();
            }
            // Translate control points.
            for (int j = 0; j < sw.getControlPointCount(); j++) {
              Point2D oldP = sw.getControlPoint(j);
              Point2D p = new Point2D.Double(oldP.getX() + Math.min(point1.getX(), point2.getX()) - dx, oldP.getY() + Math.min(point1.getY(), point2.getY()) - dy);              
              sw.setControlPoint(p, j);
            }
            component = sw;
          }
        } else if (nodeName.equalsIgnoreCase("lineic")) {
          LOG.debug("Recognized " + nodeName);
          SIL_IC ic = new SIL_IC();
          int pinCount = 8;
          if (x1Attr == x2Attr && y1Attr < y2Attr) {
            pinCount = (y2Attr - y1Attr + 1);
            ic.setOrientation(Orientation.DEFAULT);
          } else if (x1Attr > x2Attr && y1Attr == y2Attr) {
            pinCount = (x1Attr - x2Attr + 1);
            ic.setOrientation(Orientation._90);
          } else if (x1Attr == x2Attr && y1Attr > y2Attr) {
            pinCount = (y1Attr - y2Attr + 1);
            ic.setOrientation(Orientation._180);
          } else if (x1Attr < x2Attr && y1Attr == y2Attr) {
            pinCount = (x2Attr - x1Attr + 1);
            ic.setOrientation(Orientation._270);
          }
          ic.setPinCount(SIL_IC.PinCount.valueOf("_" + pinCount));
          ic.setName(nameAttr);
          // Translate control points.
          for (int j = 0; j < ic.getControlPointCount(); j++) {
            Point2D oldP = ic.getControlPoint(j);
            Point2D p = new Point2D.Double(oldP.getX() + point1.getX(), oldP.getY() + point1.getY());            
            ic.setControlPoint(p, j);
          }
          ic.setValue(valueAttr);
          component = ic;
        } else if (nodeName.equalsIgnoreCase("pot")) {
          LOG.debug("Recognized " + nodeName);
          PotentiometerPanel pot = new PotentiometerPanel();
          pot.setBodyDiameter(new Size(14d, SizeUnit.mm));
          pot.setSpacing(new Size(0.2, SizeUnit.in));
          pot.setName(nameAttr);
          try {
            pot.setValue(Resistance.parseResistance(valueAttr));
          } catch (Exception e) {
            LOG.debug("Could not set value of " + nameAttr);
          }
          String taperAttr = node.getAttributes().getNamedItem("Taper").getNodeValue();
          if ("Linear".equals(taperAttr)) {
            pot.setTaper(Taper.LIN);
          } else if ("Audio".equals(taperAttr)) {
            pot.setTaper(Taper.LOG);
          } else if ("Reverse Audio".equals(taperAttr)) {
            pot.setTaper(Taper.REV_LOG);
          }
          // Pin spacing, we'll need to move pot around a bit.
          int delta = Constants.PIXELS_PER_INCH / 5;
          if (x1Attr < x2Attr) {
            pot.setOrientation(Orientation.DEFAULT);
            for (int j = 0; j < pot.getControlPointCount(); j++) {
              Point2D oldP = pot.getControlPoint(j);
              Point2D p = new Point2D.Double(oldP.getX() + point1.getX() - delta, oldP.getY() + point1.getY());              
              pot.setControlPoint(p, j);
            }
          } else if (x1Attr > x2Attr) {
            pot.setOrientation(Orientation._180);
            for (int j = 0; j < pot.getControlPointCount(); j++) {
              Point2D oldP = pot.getControlPoint(j);
              Point2D p = new Point2D.Double(oldP.getX() + point1.getX() + delta, oldP.getY() + point1.getY());              
              pot.setControlPoint(p, j);
            }
          } else if (y1Attr < y2Attr) {
            pot.setOrientation(Orientation._90);
            for (int j = 0; j < pot.getControlPointCount(); j++) {
              Point2D oldP = pot.getControlPoint(j);
              Point2D p = new Point2D.Double(oldP.getX() + point1.getX(), oldP.getY() + point1.getY() - delta);              
              pot.setControlPoint(p, j);
            }
          } else if (y1Attr > y2Attr) {
            pot.setOrientation(Orientation._270);
            for (int j = 0; j < pot.getControlPointCount(); j++) {
              Point2D oldP = pot.getControlPoint(j);
              Point2D p = new Point2D.Double(oldP.getX() + point1.getX(), oldP.getY() + point1.getY() + delta);
              pot.setControlPoint(p, j);
            };
          }
          component = pot;
        } else if (nodeName.equalsIgnoreCase("trimmer")) {
          LOG.debug("Recognized " + nodeName);
          TrimmerPotentiometer trimmer = new TrimmerPotentiometer();
          trimmer.setName(nameAttr);
          int sizeX = Math.abs(x1Attr - x2Attr);
          int sizeY = Math.abs(y1Attr - y2Attr);
          TrimmerType trimmerType = null;
          Orientation orientation = null;
          int dx = 0;
          int dy = 0;
          // determine type by size
          if (Math.min(sizeX, sizeY) == 0 && Math.max(sizeX, sizeY) == 1) {
            trimmerType = TrimmerType.VERTICAL_INLINE;

            if (y1Attr > y2Attr) {
              orientation = Orientation.DEFAULT;
              dy = -1;
            } else if (x1Attr > x2Attr) {
              orientation = Orientation._90;
              dx = 1;
            } else if (y1Attr < y2Attr) {
              orientation = Orientation._180;
              dy = 1;
            } else if (x1Attr < x2Attr) {
              orientation = Orientation._270;
              dx = -1;
            }
          } else if (Math.min(sizeX, sizeY) == 1 && Math.max(sizeX, sizeY) == 1) {
            trimmerType = TrimmerType.VERTICAL_OFFSET;

            if (x1Attr > x2Attr && y1Attr > y2Attr) {
              orientation = Orientation.DEFAULT;
              dx = -1;
              dy = -1;
            } else if (x1Attr < x2Attr && y1Attr > y2Attr) {
              orientation = Orientation._90;
              dx = 1;
              dy = -1;
            } else if (x1Attr < x2Attr && y1Attr < y2Attr) {
              orientation = Orientation._180;
              dx = 1;
              dy = 1;
            } else if (x1Attr > x2Attr && y1Attr < y2Attr) {
              orientation = Orientation._270;
              dx = -1;
              dy = 1;
            }
          } else if (Math.min(sizeX, sizeY) == 1 && Math.max(sizeX, sizeY) == 2) {
            trimmerType = TrimmerType.VERTICAL_OFFSET_BIG_GAP;

            if (x1Attr > x2Attr && y1Attr > y2Attr) {
              if (sizeX == 2) {
                orientation = Orientation.DEFAULT;
                dx = -2;
                dy = -1;
              } else {
                orientation = Orientation._90;
                dx = 1;
                dy = -2;
              }
            } else if (x1Attr < x2Attr && y1Attr > y2Attr) {
              if (sizeX == 2) {
                orientation = Orientation._180;
                dx = 2;
                dy = 1;
              } else {
                orientation = Orientation._90;
                dx = 1;
                dy = -2;
              }
            } else if (x1Attr < x2Attr && y1Attr < y2Attr) {
              if (sizeX == 2) {
                orientation = Orientation._180;
                dx = 2;
                dy = 1;
              } else {
                orientation = Orientation._270;
                dx = -1;
                dy = 2;
              }
            } else if (x1Attr > x2Attr && y1Attr < y2Attr) {
              if (sizeX == 2) {
                orientation = Orientation.DEFAULT;
                dx = -2;
                dy = -1;
              } else {
                orientation = Orientation._270;
                dx = -1;
                dy = 2;
              }
            }
          } else if (Math.min(sizeX, sizeY) == 1 && Math.max(sizeX, sizeY) == 4) {
            trimmerType = TrimmerType.FLAT_LARGE;

            if (x1Attr > x2Attr && y1Attr > y2Attr) {
              if (sizeX == 4) {
                orientation = Orientation.DEFAULT;
                dx = -4;
                dy = -1;
              } else {
                orientation = Orientation._90;
                dx = 1;
                dy = -4;
              }
            } else if (x1Attr < x2Attr && y1Attr > y2Attr) {
              if (sizeX == 4) {
                orientation = Orientation._180;
                dx = 4;
                dy = 1;
              } else {
                orientation = Orientation._90;
                dx = 1;
                dy = -4;
              }
            } else if (x1Attr < x2Attr && y1Attr < y2Attr) {
              if (sizeX == 4) {
                orientation = Orientation._180;
                dx = 4;
                dy = 1;
              } else {
                orientation = Orientation._270;
                dx = -1;
                dy = 4;
              }
            } else if (x1Attr > x2Attr && y1Attr < y2Attr) {
              if (sizeX == 4) {
                orientation = Orientation.DEFAULT;
                dx = -4;
                dy = -1;
              } else {
                orientation = Orientation._270;
                dx = -1;
                dy = 4;
              }
            }
          }

          if (trimmerType == null || orientation == null) {
            String message = "Unsupported trimmer dimensions";
            LOG.debug(message);
            if (!warnings.contains(message)) {
              warnings.add(message);
            }
          } else {
            try {
              trimmer.setValue(Resistance.parseResistance(valueAttr));
            } catch (Exception e) {
              LOG.debug("Could not set value of " + nameAttr);
            }
            trimmer.setType(trimmerType);
            trimmer.setOrientation(orientation);
            // scale nudges
            dx *= V1_GRID_SPACING.convertToPixels();
            dy *= V1_GRID_SPACING.convertToPixels();
            // Translate control points.
            for (int j = 0; j < trimmer.getControlPointCount(); j++) {
              Point2D oldP = trimmer.getControlPoint(j);
              Point2D p = new Point2D.Double(oldP.getX() + point1.getX() + dx, oldP.getY() + point1.getY() + dy);              
              trimmer.setControlPoint(p, j);
            }
            component = trimmer;
          }
        } else {
          String message = "Could not recognize component type " + nodeName;
          LOG.debug(message);
          if (!warnings.contains(message)) {
            warnings.add(message);
          }
        }
        if (component != null) {
          if (component instanceof AbstractLeadedComponent<?>) {
            ((AbstractLeadedComponent<?>) component).setDisplay(Display.NAME);
          }
          if (component instanceof AbstractTransparentComponent<?>) {
            ((AbstractTransparentComponent<?>) component).setAlpha((byte) 100);
          }
          project.getComponents().add(component);
        }
      }
    }

    double minY = y;
    for (IDIYComponent<?> c : project.getComponents()) {
      for (int i = 0; i < c.getControlPointCount(); i++) {
        Point2D p = c.getControlPoint(i);
        if (p.getY() < minY)
          minY = p.getY();
      }
    }

    // Add title and credits
    Label titleLabel = new Label();
    titleLabel.setColor(Color.blue);
    titleLabel.setFontSize(24);
    titleLabel.setValue(project.getTitle());
    titleLabel.setHorizontalAlignment(HorizontalAlignment.CENTER);
    titleLabel.setControlPoint(
        new Point2D.Double(CalcUtils.roundToGrid(x + boardWidth / 2, V1_GRID_SPACING), CalcUtils.roundToGrid(
            (int) (minY - Constants.PIXELS_PER_INCH * V1_GRID_SPACING.getValue() * 5), V1_GRID_SPACING)), 0);
    project.getComponents().add(titleLabel);

    Label creditsLabel = new Label();
    creditsLabel.setFontSize(16);
    creditsLabel.setValue(project.getAuthor());
    creditsLabel.setHorizontalAlignment(HorizontalAlignment.CENTER);
    creditsLabel.setControlPoint(
        new Point2D.Double(CalcUtils.roundToGrid(x + boardWidth / 2, V1_GRID_SPACING), CalcUtils.roundToGrid(
            (int) (minY - Constants.PIXELS_PER_INCH * V1_GRID_SPACING.getValue() * 4), V1_GRID_SPACING)), 0);
    project.getComponents().add(creditsLabel);

    // Add BOM at the bottom
    BOM bom = new BOM();
    int bomSize = (int) bom.getSize().convertToPixels();
    bom.setControlPoint(
        new Point2D.Double(CalcUtils.roundToGrid(x + (boardWidth - bomSize) / 2, V1_GRID_SPACING), CalcUtils.roundToGrid(
            (int) (y + boardHeight + 2 * V1_GRID_SPACING.convertToPixels()), V1_GRID_SPACING)), 0);
    project.getComponents().add(bom);

    // Sort by z-order
    Collections.sort(project.getComponents(), ComparatorFactory.getInstance().getComponentZOrderComparator());
    return project;
  }

  private Point2D convertV1CoordinatesToV3Point(Point2D reference, int x, int y) {
    Point2D point = new Point2D.Double(reference.getX() + x * Constants.PIXELS_PER_INCH * V1_GRID_SPACING.getValue(), reference.getY() + y
        * Constants.PIXELS_PER_INCH * V1_GRID_SPACING.getValue());    
    return point;
  }

  private Color parseV1Color(String color) {
    if ("brown".equals(color.toLowerCase()))
      return new Color(139, 69, 19);
    try {
      Field field = Color.class.getDeclaredField(color.toLowerCase());
      return (Color) field.get(null);
    } catch (Exception e) {
      LOG.error("Could not parse color \"" + color + "\"", e);
      return Color.black;
    }
  }

  private int myRandom(int range, Random r) {
    range = (range * 2) / 3;
    int rand = r.nextInt(range) - range / 2;
    if (Math.abs(rand) < range / 3)
      rand = myRandom(range, r);
    return rand;
  }

  private long randSeed = 0;

  @SuppressWarnings("unused")
  private int randInt(int range) {
    long newSeed = randSeed * 0x08088405 + 1;
    randSeed = newSeed;
    return (int) ((long)newSeed * range >> 32);
  }

}
