package org.diylc.parsing;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Display;
import org.diylc.common.Orientation;
import org.diylc.components.boards.AbstractBoard;
import org.diylc.components.boards.BlankBoard;
import org.diylc.components.boards.PerfBoard;
import org.diylc.components.boards.VeroBoard;
import org.diylc.components.connectivity.AWG;
import org.diylc.components.connectivity.CopperTrace;
import org.diylc.components.connectivity.CurvedTrace;
import org.diylc.components.connectivity.Eyelet;
import org.diylc.components.connectivity.HookupWire;
import org.diylc.components.connectivity.Line;
import org.diylc.components.connectivity.SolderPad;
import org.diylc.components.connectivity.TraceCut;
import org.diylc.components.electromechanical.MiniToggleSwitch;
import org.diylc.components.electromechanical.ToggleSwitchType;
import org.diylc.components.misc.GroundSymbol;
import org.diylc.components.passive.AxialElectrolyticCapacitor;
import org.diylc.components.passive.AxialFilmCapacitor;
import org.diylc.components.passive.CapacitorSymbol;
import org.diylc.components.passive.PotentiometerPanel;
import org.diylc.components.passive.PotentiometerSymbol;
import org.diylc.components.passive.RadialCeramicDiskCapacitor;
import org.diylc.components.passive.RadialElectrolytic;
import org.diylc.components.passive.RadialFilmCapacitor;
import org.diylc.components.passive.Resistor;
import org.diylc.components.passive.ResistorSymbol;
import org.diylc.components.semiconductors.BJTSymbol;
import org.diylc.components.semiconductors.DIL_IC;
import org.diylc.components.semiconductors.DIL_IC.PinCount;
import org.diylc.components.semiconductors.DiodePlastic;
import org.diylc.components.semiconductors.DiodeSymbol;
import org.diylc.components.semiconductors.ICPointCount;
import org.diylc.components.semiconductors.ICSymbol;
import org.diylc.components.semiconductors.LEDSymbol;
import org.diylc.components.semiconductors.TransistorTO92;
import org.diylc.components.shapes.Ellipse;
import org.diylc.components.shapes.Rectangle;
import org.diylc.components.tube.PentodeSymbol;
import org.diylc.components.tube.TriodeSymbol;
import org.diylc.components.tube.TubeSocket;
import org.diylc.components.tube.TubeSocket.Base;
import org.diylc.core.Project;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.CapacitanceUnit;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.ResistanceUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.presenter.Presenter;
import org.diylc.utils.Constants;
import org.nfunk.jep.JEP;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class V2FileParser implements IOldFileParser {

  private static final Logger LOG = Logger.getLogger(V2FileParser.class);
  private static final Size V2_GRID_SPACING = new Size(0.1d, SizeUnit.in);

  @Override
  public boolean canParse(String version) {
    return version.equals("2.0");
  }

  public static Size parseString(String text) {
    JEP parser = new JEP();
    parser.addStandardConstants();
    parser.addStandardFunctions();
    parser.setImplicitMul(true);
    parser.addConstant("mm", Constants.PIXELS_PER_INCH / 25.4f);
    parser.addConstant("cm", Constants.PIXELS_PER_INCH / 2.54f);
    parser.addConstant("in", Constants.PIXELS_PER_INCH * 1f);
    parser.addConstant("grid", Constants.PIXELS_PER_INCH * 0.1f);
    parser.addConstant("degree", Double.valueOf(0.017453292519943295D));
    parser.addConstant("deg", Double.valueOf(0.017453292519943295D));
    parser.addConstant("px", 1f);
    parser.parseExpression(text);
    Double value = parser.getValue(); // in pixels
    if (ConfigurationManager.getInstance().readBoolean(Presenter.METRIC_KEY, true))
      return new Size(value / Constants.PIXELS_PER_INCH * 25.4f, SizeUnit.mm);
    else
      return new Size(value / Constants.PIXELS_PER_INCH, SizeUnit.in);

  }

  @Override
  public Project parseFile(Element root, List<String> warnings) {
    Project project = new Project();
    // warnings.add("V2 file parser is not yet implemented due to lack of interest and short life of DIYLC V2.");
    String projectName = root.getAttribute("projectName");
    String credits = root.getAttribute("credits");
    String width = root.getAttribute("width");
    String height = root.getAttribute("height");
    Size wp = parseString(width);
    Size hp = parseString(height);

    project.setTitle(projectName);
    project.setAuthor(credits);
    project.setGridSpacing(V2_GRID_SPACING);
    project.setDescription("V2FileParser");
    project.setWidth(wp);
    project.setHeight(hp);

    NodeList childNodes = root.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        if (node.getNodeName().equalsIgnoreCase("component")) {
          LOG.debug(node.getAttributes().getNamedItem("name").getNodeValue());
          NodeList unuci = node.getChildNodes();
          Node properties = unuci.item(1);
          Node points = unuci.item(3);

          NodeList propertyList = properties.getChildNodes();
          NodeList pointList = points.getChildNodes();
          Color cl = null;
          String com_name = "";
          String shape = "circle";
          String display = "";
          double angle = 0;
          double distance = 1;
          double value = -9999;
          String value_s = "";
          int transparency = 100;
          int pins = 6;
          Size sizePro = new Size(5.0, SizeUnit.mm);
          Size thicknessPro = new Size(5.0, SizeUnit.mm);
          Size diameterPro = new Size(5.0, SizeUnit.mm);
          Size lengthPro = new Size(0.0, SizeUnit.mm);
          Size bodyPro = new Size(5.0, SizeUnit.mm);
          Size spacingPro = new Size(0.0, SizeUnit.mm);
          Size radiusPro = new Size(1.0, SizeUnit.mm);
          CapacitanceUnit cp = CapacitanceUnit.nF;
          ResistanceUnit ru = ResistanceUnit.K;
          for (int j = 0; j < propertyList.getLength(); j++) {
            // propertyList.item(j).getNodeName() gives: property or "#text"
            if (propertyList.item(j).getNodeName().equalsIgnoreCase("property")) {
              // System.out.println(propertyList.item(j).getAttributes().getNamedItem("name").getNodeValue());
              // // gives Name Part# Thickness Color Group
              if (propertyList.item(j).getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("color")) {
                String color = propertyList.item(j).getAttributes().getNamedItem("value").getNodeValue();
                System.out.println(color);
                String hex_r = color.substring(0, 2);
                int value_r = Integer.parseInt(hex_r, 16);
                String hex_g = color.substring(2, 4);
                int value_g = Integer.parseInt(hex_g, 16);
                String hex_b = color.substring(4, 6);
                int value_b = Integer.parseInt(hex_b, 16);
                cl = new Color(value_r, value_g, value_b);
              } else if (propertyList.item(j).getAttributes().getNamedItem("name").getNodeValue()
                  .equalsIgnoreCase("name")) {
                com_name = propertyList.item(j).getAttributes().getNamedItem("value").getNodeValue();
              } else if (propertyList.item(j).getAttributes().getNamedItem("name").getNodeValue()
                  .equalsIgnoreCase("Thickness")) {
                String prom = propertyList.item(j).getAttributes().getNamedItem("value").getNodeValue();
                thicknessPro = parseString(prom);
              } else if (propertyList.item(j).getAttributes().getNamedItem("name").getNodeValue()
                  .equalsIgnoreCase("Size")) {
                String pr = propertyList.item(j).getAttributes().getNamedItem("value").getNodeValue();
                sizePro = parseString(pr);
              } else if (propertyList.item(j).getAttributes().getNamedItem("name").getNodeValue()
                  .equalsIgnoreCase("Shape")) {
                shape = propertyList.item(j).getAttributes().getNamedItem("value").getNodeValue();
              } else if (propertyList.item(j).getAttributes().getNamedItem("name").getNodeValue()
                  .equalsIgnoreCase("Diameter")) {
                String pr1 = propertyList.item(j).getAttributes().getNamedItem("value").getNodeValue();
                diameterPro = parseString(pr1);
              } else if (propertyList.item(j).getAttributes().getNamedItem("name").getNodeValue()
                  .equalsIgnoreCase("Length")) {
                String pr2 = propertyList.item(j).getAttributes().getNamedItem("value").getNodeValue();
                lengthPro = parseString(pr2);
              } else if (propertyList.item(j).getAttributes().getNamedItem("name").getNodeValue()
                  .equalsIgnoreCase("Display")) {
                display = propertyList.item(j).getAttributes().getNamedItem("value").getNodeValue();
              } else if (propertyList.item(j).getAttributes().getNamedItem("name").getNodeValue()
                  .equalsIgnoreCase("Body")) {
                String pr3 = propertyList.item(j).getAttributes().getNamedItem("value").getNodeValue();
                bodyPro = parseString(pr3);
              } else if (propertyList.item(j).getAttributes().getNamedItem("name").getNodeValue()
                  .equalsIgnoreCase("Spacing")) {
                String pr4 = propertyList.item(j).getAttributes().getNamedItem("value").getNodeValue();
                spacingPro = parseString(pr4);
              } else if (propertyList.item(j).getAttributes().getNamedItem("name").getNodeValue()
                  .equalsIgnoreCase("Angle")) {
                String pr5 = propertyList.item(j).getAttributes().getNamedItem("value").getNodeValue();
                Size anglePro = parseString(pr5);
                angle = anglePro.getValue() * Constants.PIXELS_PER_INCH / 25.4f / Double.valueOf(0.017453292519943295D); // (anglePro.getValue()*
                                                                                                                         // 25.4f/Double.valueOf(0.017453292519943295D)/Double.valueOf(3.2257990310669));
                angle = Math.floor(angle);
              } else if (propertyList.item(j).getAttributes().getNamedItem("name").getNodeValue()
                  .equalsIgnoreCase("Distance")) {
                String pr6 = propertyList.item(j).getAttributes().getNamedItem("value").getNodeValue();
                pr6 = pr6.replaceAll("[^0-9.]", "");
                distance = Double.parseDouble(pr6);
              } else if (propertyList.item(j).getAttributes().getNamedItem("name").getNodeValue()
                  .equalsIgnoreCase("Radius")) {
                String pr7 = propertyList.item(j).getAttributes().getNamedItem("value").getNodeValue();
                radiusPro = parseString(pr7);
              } else if (propertyList.item(j).getAttributes().getNamedItem("name").getNodeValue()
                  .equalsIgnoreCase("Value")) {
                String pr8 = propertyList.item(j).getAttributes().getNamedItem("value").getNodeValue();
                if (pr8 != "") {
                  value_s = pr8;
                  String pr9 = pr8.replaceAll("[^0-9.]", "");
                  String pr10 = pr8.replaceAll("[^a-zA-Z]", "");
                  if (pr10.equals("pF"))
                    cp = CapacitanceUnit.pF;
                  else if (pr10.equals("nF"))
                    cp = CapacitanceUnit.nF;
                  else if (pr10.equals("F"))
                    cp = CapacitanceUnit.F;
                  else if (pr10.equals("mF"))
                    cp = CapacitanceUnit.mF;
                  else if (pr10.equals("uF"))
                    cp = CapacitanceUnit.uF;
                  else if (pr10.equals("K"))
                    ru = ResistanceUnit.K;
                  else if (pr10.equals("M"))
                    ru = ResistanceUnit.M;
                  else if (pr10.equals("R"))
                    ru = ResistanceUnit.R;
                  value = Double.parseDouble(pr9);

                }
              } else if (propertyList.item(j).getAttributes().getNamedItem("name").getNodeValue()
                  .equalsIgnoreCase("Transparency")) {
                String p11 = propertyList.item(j).getAttributes().getNamedItem("value").getNodeValue();
                p11 = p11.replaceAll("[^0-9.]", "");
                transparency = (int) (Double.parseDouble(p11) * 100);
              } else if (propertyList.item(j).getAttributes().getNamedItem("name").getNodeValue()
                  .equalsIgnoreCase("Pins")) {
                String p12 = propertyList.item(j).getAttributes().getNamedItem("value").getNodeValue();
                p12 = p12.replaceAll("[^0-9.]", "");
                pins = Integer.parseInt(p12);
              }
            }
          }

          ArrayList<Point> tacke = new ArrayList<Point>();

          for (int k = 1; k < pointList.getLength(); k += 2) {
            int Iks = Integer.parseInt(pointList.item(k).getAttributes().getNamedItem("x").getNodeValue());
            int Ipsilon = Integer.parseInt(pointList.item(k).getAttributes().getNamedItem("y").getNodeValue());
            tacke.add(new Point(Iks, Ipsilon));
          }


          if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Blank Board")) {
            AbstractBoard board = new BlankBoard();
            if (cl != null) {
              board.setBoardColor(cl);
            }
            board.setBorderColor(Color.black);
            if (com_name != "") {
              board.setName(com_name);
            } else {
              board.setName("Main board");
            }
            board.setControlPoint(tacke.get(0), 0);
            board.setControlPoint(tacke.get(1), 1);
            project.getComponents().add(board);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Perfboard")) {
            PerfBoard board = new PerfBoard();
            board.setBoardColor(Color.white);
            board.setBorderColor(Color.black);
            if (com_name != "") {
              board.setName(com_name);
            } else {
              board.setName("Main board");
            }
            board.setSpacing(new Size(0.07, SizeUnit.in));
            board.setControlPoint(tacke.get(0), 0);
            board.setControlPoint(tacke.get(1), 1);
            project.getComponents().add(board);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Copper Trace")) {
            CopperTrace trace = new CopperTrace();
            if (com_name != "") {
              trace.setName(com_name);
            } else {
              trace.setName("t");
            }
            if (cl != null) {
              trace.setLeadColor(cl);
            }
            trace.setThickness(thicknessPro);
            trace.setControlPoint(tacke.get(0), 0);
            trace.setControlPoint(tacke.get(1), 1);
            project.getComponents().add(trace);

          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Copper Trace Curved")) {
            CurvedTrace trace = new CurvedTrace();
            if (com_name != "") {
              trace.setName(com_name);
            } else {
              trace.setName("t");
            }
            if (cl != null) {
              trace.setLeadColor(cl);
            }
            trace.setThickness(thicknessPro);
            trace.setControlPoint(tacke.get(0), 0);
            trace.setControlPoint(tacke.get(1), 1);
            trace.setControlPoint(tacke.get(2), 2);
            trace.setControlPoint(tacke.get(3), 3);
            project.getComponents().add(trace);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Eyelet")) {
            Eyelet eyelet = new Eyelet();
            if (com_name != "") {
              eyelet.setName(com_name);
            } else {
              eyelet.setName("eyelet");
            }
            eyelet.setSize(sizePro);
            eyelet.setControlPoint(tacke.get(0), 0);
            project.getComponents().add(eyelet);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Solder pad")) {
            SolderPad pad = new SolderPad();
            if (com_name != "") {
              pad.setName(com_name);
            } else
              pad.setName("pad");
            pad.setSize(sizePro);
            if (shape.equals("square")) {
              pad.setType(SolderPad.Type.SQUARE); // type is changed from static to public static
            } else
              pad.setType(SolderPad.Type.ROUND);
            pad.setControlPoint(tacke.get(0), 0);
            project.getComponents().add(pad);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Stripboard")) {
            VeroBoard board = new VeroBoard();
            if (com_name != "") {
              board.setName(com_name);
            } else {
              board.setName("Main board");
            }
            board.setControlPoint(tacke.get(0), 0);
            board.setControlPoint(tacke.get(1), 1);
            board.setSpacing(new Size(0.08, SizeUnit.in));
            project.getComponents().add(board);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Trace Cut")) {
            TraceCut cut = new TraceCut();
            if (com_name != "") {
              cut.setName(com_name);
            } else
              cut.setName("cut");
            cut.setControlPoint(tacke.get(0), 0);
            project.getComponents().add(cut);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Switch")) {
            MiniToggleSwitch sw = new MiniToggleSwitch();
            sw.setValue(ToggleSwitchType.DPDT);
            if (com_name != "") {
              sw.setName(com_name);
            } else
              sw.setName("sw");
            Point point;
            int x = (int) (tacke.get(0).getX() + 20);
            int y = (int) (tacke.get(0).getY() + 10);
            point = new Point(x, y);
            sw.setSpacing(new Size(0.1, SizeUnit.in));
            sw.setControlPoint(point, 0);
            x = (int) (tacke.get(0).getX() + 40);
            y = (int) (tacke.get(0).getY() + 10);
            point = new Point(x, y);
            sw.setControlPoint(point, 1);
            x = (int) (tacke.get(0).getX() + 20);
            y = (int) (tacke.get(0).getY() + 30);
            point = new Point(x, y);
            sw.setControlPoint(point, 2);
            x = (int) (tacke.get(0).getX() + 40);
            y = (int) (tacke.get(0).getY() + 30);
            point = new Point(x, y);
            sw.setControlPoint(point, 3);
            x = (int) (tacke.get(0).getX() + 20);
            y = (int) (tacke.get(0).getY() + 50);
            point = new Point(x, y);
            sw.setControlPoint(point, 4);
            x = (int) (tacke.get(0).getX() + 40);
            y = (int) (tacke.get(0).getY() + 50);
            point = new Point(x, y);
            sw.setControlPoint(point, 5);
            project.getComponents().add(sw);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Wire")) {
            HookupWire hw = new HookupWire();
            if (com_name != "") {
              hw.setName(com_name);
            } else
              hw.setName("hookup_wire");
            ArrayList<Double> awgt = new ArrayList<Double>();
            awgt.add(2 * 3.264);
            awgt.add(2 * 2.588);
            awgt.add(2 * 2.053);
            awgt.add(2 * 1.628);
            awgt.add(2 * 1.291);
            awgt.add(2 * 1.024);
            awgt.add(2 * 0.812);
            awgt.add(2 * 0.644);
            awgt.add(2 * 0.511);
            awgt.add(2 * 0.405);
            awgt.add(2 * 0.321);
            awgt.add(2 * 0.255);
            int num = 0;
            for (int q = 1; q < awgt.size(); q++) {
              if (thicknessPro.getValue() <= awgt.get(q - 1) && thicknessPro.getValue() > awgt.get(q)) {
                num = q - 1;
              }
            }
            num = num * 2 + 8;
            if (num == 8)
              hw.setGauge(AWG._8);
            else if (num == 10)
              hw.setGauge(AWG._10);
            else if (num == 12)
              hw.setGauge(AWG._12);
            else if (num == 14)
              hw.setGauge(AWG._14);
            else if (num == 16)
              hw.setGauge(AWG._16);
            else if (num == 18)
              hw.setGauge(AWG._18);
            else if (num == 20)
              hw.setGauge(AWG._20);
            else if (num == 22)
              hw.setGauge(AWG._22);
            else if (num == 24)
              hw.setGauge(AWG._24);
            else if (num == 26)
              hw.setGauge(AWG._26);
            else if (num == 28)
              hw.setGauge(AWG._28);
            else if (num == 30)
              hw.setGauge(AWG._30);
            hw.setLeadColor(cl);
            hw.setControlPoint(tacke.get(0), 0);
            hw.setControlPoint(tacke.get(1), 1);
            hw.setControlPoint(tacke.get(2), 2);
            hw.setControlPoint(tacke.get(3), 3);
            project.getComponents().add(hw);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Electrolytic (Axial)")) {
            AxialElectrolyticCapacitor capacitor = new AxialElectrolyticCapacitor();
            if (com_name != "") {
              capacitor.setName(com_name);
            } else
              capacitor.setName("A_E_C");
            capacitor.setAlpha((byte) transparency);
            if (value != -9999) {
              capacitor.setValue(new Capacitance(value, cp));
            }
            capacitor.setWidth(diameterPro);
            capacitor.setLength(lengthPro);
            if (display.equals("Name")) {
              capacitor.setDisplay(Display.NAME);
            } else {
              capacitor.setDisplay(Display.VALUE);
            }
            capacitor.setControlPoint(tacke.get(0), 0);
            capacitor.setControlPoint(tacke.get(1), 1);
            project.getComponents().add(capacitor);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Capacitor (Axial)")) {
            AxialFilmCapacitor capacitor = new AxialFilmCapacitor();
            if (com_name != "") {
              capacitor.setName(com_name);
            }
            capacitor.setAlpha((byte) transparency);
            if (value != -9999) {
              capacitor.setValue(new Capacitance(value, cp));
            } else
              capacitor.setName("A_F_C");
            if (display.equals("Name")) {
              capacitor.setDisplay(Display.NAME);
            } else {
              capacitor.setDisplay(Display.VALUE);
            }
            capacitor.setWidth(thicknessPro);
            capacitor.setLength(lengthPro);
            capacitor.setControlPoint(tacke.get(0), 0);
            capacitor.setControlPoint(tacke.get(1), 1);
            project.getComponents().add(capacitor);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Capacitor (Ceramic)")) {
            RadialCeramicDiskCapacitor capacitor = new RadialCeramicDiskCapacitor();
            if (com_name != "") {
              capacitor.setName(com_name);
            } else
              capacitor.setName("Radial_Ceramic_Capacitor");
            capacitor.setAlpha((byte) transparency);
            if (value != -9999) {
              capacitor.setValue(new Capacitance(value, cp));
            }
            if (display.equals("Name")) {
              capacitor.setDisplay(Display.NAME);
            } else {
              capacitor.setDisplay(Display.VALUE);
            }
            capacitor.setWidth(thicknessPro);
            capacitor.setLength(lengthPro);
            capacitor.setControlPoint(tacke.get(0), 0);
            capacitor.setControlPoint(tacke.get(1), 1);
            project.getComponents().add(capacitor);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Capacitor (Radial)")) {
            RadialFilmCapacitor capacitor = new RadialFilmCapacitor();
            if (com_name != "") {
              capacitor.setName(com_name);
            } else
              capacitor.setName("Radial_Film_Capacitor");
            capacitor.setAlpha((byte) transparency);
            if (value != -9999) {
              capacitor.setValue(new Capacitance(value, cp));
            }
            if (display.equals("Name")) {
              capacitor.setDisplay(Display.NAME);
            } else {
              capacitor.setDisplay(Display.VALUE);
            }
            capacitor.setWidth(thicknessPro);
            capacitor.setLength(lengthPro);
            capacitor.setControlPoint(tacke.get(0), 0);
            capacitor.setControlPoint(tacke.get(1), 1);
            project.getComponents().add(capacitor);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Electrolytic (Radial)")) {
            RadialElectrolytic capacitor = new RadialElectrolytic();
            if (com_name != "") {
              capacitor.setName(com_name);
            } else
              capacitor.setName("Radial_Electrolytic_Capacitor");
            if (value != -9999) {
              capacitor.setValue(new Capacitance(value, cp));
            }
            capacitor.setAlpha((byte) transparency);
            capacitor.setWidth(diameterPro);
            capacitor.setHeight(diameterPro);
            capacitor.setControlPoint(tacke.get(0), 0);
            capacitor.setControlPoint(tacke.get(1), 1);
            project.getComponents().add(capacitor);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Potentiometer Lug")) {
            PotentiometerPanel panel = new PotentiometerPanel();
            if (com_name != "") {
              panel.setName(com_name);
            } else
              panel.setName("potentiometer_panel");
            if (value != -9999) {
              panel.setValue(new Resistance(value, ru));
            }
            panel.setAlpha((byte) transparency);
            if (angle > 45 && angle <= 135)
              panel.setOrientation(Orientation._90);
            else if (angle > 135 && angle <= 225)
              panel.setOrientation(Orientation._180);
            else if (angle > 225 && angle <= 315)
              panel.setOrientation(Orientation._270);
            Point point;
            int x = (int) (tacke.get(0).getX() - 20);
            int y = (int) tacke.get(0).getY();
            point = new Point(x, y);
            panel.setControlPoint(point, 0);
            panel.setBodyDiameter(bodyPro);
            panel.setSpacing(spacingPro);
            panel.setLugDiameter(new Size(0.1, SizeUnit.in));
            project.getComponents().add(panel);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Resistor")
              || node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Resistor Standing")) {
            Resistor resistor = new Resistor();
            if (com_name != "") {
              resistor.setName(com_name);
            } else
              resistor.setName("resistor");
            if (value != -9999) {
              resistor.setValue(new Resistance(value, ru));
            }
            resistor.setAlpha((byte) transparency);
            resistor.setWidth(diameterPro);
            if (lengthPro.getValue() != 0) {
              resistor.setLength(lengthPro);
            }
            resistor.setControlPoint(tacke.get(0), 0);
            resistor.setControlPoint(tacke.get(1), 1);
            project.getComponents().add(resistor);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Capacitor Symbol")) {
            CapacitorSymbol cap = new CapacitorSymbol();
            if (com_name != "") {
              cap.setName(com_name);
            } else
              cap.setName("cap_simbol");
            if (value != -9999) {
              cap.setValue(new Capacitance(value, cp));
            }
            cap.setLength(new Size(distance, SizeUnit.mm));
            cap.setWidth(lengthPro);
            cap.setBodyColor(cl);
            cap.setBorderColor(cl);
            cap.setControlPoint(tacke.get(0), 0);
            cap.setControlPoint(tacke.get(1), 1);
            project.getComponents().add(cap);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Diode Symbol")) {
            DiodeSymbol dio = new DiodeSymbol();
            if (com_name != "") {
              dio.setName(com_name);
            } else
              dio.setName("dp");
            if (value_s != "") {
              dio.setValue(value_s);
            }
            dio.setBodyColor(cl);
            dio.setWidth(sizePro);
            dio.setControlPoint(tacke.get(0), 0);
            dio.setControlPoint(tacke.get(1), 1);
            project.getComponents().add(dio);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Ground Symbol")) {
            GroundSymbol gs = new GroundSymbol();
            if (com_name != "") {
              gs.setName(com_name);
            } else
              gs.setName("gs");
            gs.setSize(sizePro);
            gs.setControlPoint(tacke.get(0), 0);
            project.getComponents().add(gs);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("LED Symbol")) {
            LEDSymbol ls = new LEDSymbol();
            if (com_name != "") {
              ls.setName(com_name);
            } else
              ls.setName("ls");
            if (value_s != "") {
              ls.setValue(value_s);
            }
            ls.setBodyColor(cl);
            ls.setWidth(sizePro);
            ls.setControlPoint(tacke.get(0), 0);
            ls.setControlPoint(tacke.get(1), 1);
            project.getComponents().add(ls);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Pentode")) {
            PentodeSymbol ps = new PentodeSymbol();
            if (com_name != "") {
              ps.setName(com_name);
            } else
              ps.setName("ps");
            ps.setColor(cl);
            if (value_s != "")
              ps.setValue(value_s);

            Point point;
            int x = (int) (tacke.get(0).getX() - 70);
            int y = (int) tacke.get(0).getY();
            point = new Point(x, y);
            ps.setControlPoint(point, 0);
            project.getComponents().add(ps);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Electrolytic Symbol")) {
            CapacitorSymbol cs = new CapacitorSymbol();
            if (com_name != "") {
              cs.setName(com_name);
            } else
              cs.setName("cs");
            if (value != -9999) {
              cs.setValue(new Capacitance(value, cp));
            }
            cs.setLength(new Size(distance, SizeUnit.mm));
            cs.setWidth(lengthPro);
            cs.setPolarized(true);
            cs.setBorderColor(cl);
            cs.setControlPoint(tacke.get(0), 0);
            cs.setControlPoint(tacke.get(1), 1);
            project.getComponents().add(cs);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Opamp Symbol")) {
            ICSymbol ic = new ICSymbol();
            if (com_name != "") {
              ic.setName(com_name);
            } else
              ic.setName("ics");
            if (value_s != "")
              ic.setValue(value_s);
            ic.setAlpha((byte) transparency);
            Point point;
            int x = (int) (tacke.get(0).getX() - 70);
            int y = (int) tacke.get(0).getY() - 20;
            point = new Point(x, y);
            ic.setControlPoint(point, 0);
            ic.setIcPointCount(ICPointCount._3);
            project.getComponents().add(ic);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Potentiometer Symbol")) {
            PotentiometerSymbol ps = new PotentiometerSymbol();
            if (com_name != "") {
              ps.setName(com_name);
            } else
              ps.setName("ps");
            ps.setOrientation(Orientation._270);
            if (value_s != "")
              ps.setValue(value_s);
            ps.setColor(cl);
            Point point;
            int x = (int) (tacke.get(0).getX() + 40);
            int y = (int) tacke.get(0).getY() + 40;
            point = new Point(x, y);
            ps.setControlPoint(point, 0);
            x = (int) (tacke.get(1).getX() + 40);
            y = (int) tacke.get(1).getY() + 40;
            point = new Point(x, y);
            ps.setControlPoint(point, 1);
            project.getComponents().add(ps);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Resistor Symbol")) {
            ResistorSymbol rs = new ResistorSymbol();
            if (com_name != "") {
              rs.setName(com_name);
            } else
              rs.setName("rs");
            if (value != -9999) {
              rs.setValue(new Resistance(value, ru));
            }
            rs.setWidth(sizePro);
            rs.setLeadColor(cl);
            rs.setBorderColor(cl);
            rs.setBodyColor(cl);
            rs.setControlPoint(tacke.get(0), 0);
            rs.setControlPoint(tacke.get(1), 1);
            project.getComponents().add(rs);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Triode")) {
            TriodeSymbol ts = new TriodeSymbol();
            if (com_name != "") {
              ts.setName(com_name);
            } else
              ts.setName("ts");
            if (value_s != "") {
              ts.setValue(value_s);
            }
            ts.setColor(cl);
            Point point;
            int x = (int) (tacke.get(0).getX() - 30);
            int y = (int) tacke.get(0).getY();
            point = new Point(x, y);
            ts.setControlPoint(point, 0);
            project.getComponents().add(ts);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Diode")) {
            DiodePlastic dp = new DiodePlastic();
            if (com_name != "") {
              dp.setName(com_name);
            } else
              dp.setName("dp");
            if (value_s != "") {
              dp.setValue(value_s);
            }
            dp.setAlpha((byte) transparency);
            if (display.equals("Name"))
              dp.setDisplay(Display.NAME);
            else
              dp.setDisplay(Display.VALUE);
            dp.setLength(lengthPro);
            dp.setWidth(diameterPro);
            dp.setControlPoint(tacke.get(0), 0);
            dp.setControlPoint(tacke.get(1), 1);
            project.getComponents().add(dp);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("IC DIL")) {
            DIL_IC dil = new DIL_IC();
            if (com_name != "") {
              dil.setName(com_name);
            } else
              dil.setName("dil");
            if (pins == 4)
              dil.setPinCount(PinCount._4);
            else if (pins == 6)
              dil.setPinCount(PinCount._6);
            else if (pins == 8)
              dil.setPinCount(PinCount._8);
            else if (pins == 10)
              dil.setPinCount(PinCount._10);
            else if (pins == 12)
              dil.setPinCount(PinCount._12);
            else if (pins == 12)
              dil.setPinCount(PinCount._12);
            else if (pins == 14)
              dil.setPinCount(PinCount._14);
            else if (pins == 16)
              dil.setPinCount(PinCount._16);
            else if (pins == 18)
              dil.setPinCount(PinCount._18);
            else if (pins == 20)
              dil.setPinCount(PinCount._20);
            else if (pins == 22)
              dil.setPinCount(PinCount._22);
            else if (pins == 24)
              dil.setPinCount(PinCount._24);
            else if (pins == 26)
              dil.setPinCount(PinCount._26);
            else if (pins == 28)
              dil.setPinCount(PinCount._28);
            else if (pins == 30)
              dil.setPinCount(PinCount._30);
            else if (pins == 32)
              dil.setPinCount(PinCount._32);
            else if (pins == 34)
              dil.setPinCount(PinCount._34);
            else if (pins == 36)
              dil.setPinCount(PinCount._36);
            else if (pins == 38)
              dil.setPinCount(PinCount._38);
            else if (pins == 40)
              dil.setPinCount(PinCount._40);
            else if (pins == 42)
              dil.setPinCount(PinCount._42);
            else if (pins == 44)
              dil.setPinCount(PinCount._44);
            else if (pins == 46)
              dil.setPinCount(PinCount._46);
            else if (pins == 48)
              dil.setPinCount(PinCount._48);
            else if (pins == 50)
              dil.setPinCount(PinCount._50);
            dil.setAlpha((byte) transparency);
            if (value_s != "")
              dil.setValue(value_s);
            dil.setRowSpacing(spacingPro);
            dil.setControlPoint(tacke.get(0), 0);
            dil.setPinSpacing(new Size(0.1d, SizeUnit.in));
            project.getComponents().add(dil);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Transistor")) {
            TransistorTO92 trans = new TransistorTO92();
            if (com_name != "") {
              trans.setName(com_name);
            } else
              trans.setName("tr");
            trans.setAlpha((byte) transparency);
            if (value_s != "") {
              trans.setValue(value_s);
            }
            trans.setPinSpacing(new Size(0.1, SizeUnit.in));
            if (angle == 0) {
              int x = (int) (tacke.get(0).getX() - 20);
              int y = (int) (tacke.get(0).getY());
              Point point = new Point(x, y);
              trans.setControlPoint(point, 0);
              x = (int) (tacke.get(1).getX() - 20);
              y = (int) (tacke.get(1).getY());
              trans.setControlPoint(point, 1);
            } else if (angle == 90) {
              int x = (int) (tacke.get(0).getX());
              int y = (int) (tacke.get(0).getY() - 20);
              Point point = new Point(x, y);
              trans.setControlPoint(point, 0);
              x = (int) (tacke.get(1).getX());
              y = (int) (tacke.get(1).getY() - 20);
              trans.setControlPoint(point, 1);
            } else if (angle == 180) {
              int x = (int) (tacke.get(0).getX() + 20);
              int y = (int) (tacke.get(0).getY());
              Point point = new Point(x, y);
              trans.setControlPoint(point, 0);
              x = (int) (tacke.get(1).getX() + 20);
              y = (int) (tacke.get(1).getY());
              trans.setControlPoint(point, 1);
            } else if (angle == 270) {
              int x = (int) (tacke.get(0).getX());
              int y = (int) (tacke.get(0).getY() + 20);
              Point point = new Point(x, y);
              trans.setControlPoint(point, 0);
              x = (int) (tacke.get(1).getX());
              y = (int) (tacke.get(1).getY() + 20);
              trans.setControlPoint(point, 1);
            }
            Orientation orientation = Orientation._270;
            if (angle > 45 && angle <= 135)
              orientation = Orientation.DEFAULT;
            else if (angle > 135 && angle <= 225)
              orientation = Orientation._90;
            else if (angle > 225 && angle <= 315)
              orientation = Orientation._180;
            trans.setOrientation(orientation);
            project.getComponents().add(trans);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Ellipse")) {
            Ellipse el = new Ellipse();
            el.setName("elipse");
            el.setAlpha((byte) transparency);
            el.setColor(cl);
            el.setControlPoint(tacke.get(0), 0);
            el.setControlPoint(tacke.get(1), 1);

            project.getComponents().add(el);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Line")) {
            Line ln = new Line();
            ln.setName("ln");
            ln.setColor(cl);
            ln.setControlPoint(tacke.get(0), 0);
            ln.setControlPoint(tacke.get(1), 1);
            project.getComponents().add(ln);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Rectangle")) {
            Rectangle rec = new Rectangle();
            rec.setName("rec");
            rec.setColor(cl);
            rec.setAlpha((byte) transparency);
            rec.setEdgeRadius(radiusPro);
            rec.setControlPoint(tacke.get(0), 0);
            rec.setControlPoint(tacke.get(1), 1);
            project.getComponents().add(rec);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Noval Tube Socket")) {
            TubeSocket ts = new TubeSocket();
            if (com_name != "") {
              ts.setName(com_name);
            } else
              ts.setName("ts");
            ts.setAlpha((byte) transparency);
            if (value_s != "")
              ts.setValue(value_s);
            ts.setAngle((int) angle);
            ts.setControlPoint(tacke.get(0), 0);
            ts.setBase(Base.B9A);
            project.getComponents().add(ts);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("BJT Transistor")) {
            BJTSymbol bjt = new BJTSymbol();
            if (com_name != "") {
              bjt.setName(com_name);
            } else
              bjt.setName("bjt");
            bjt.setColor(cl);
            if (value_s != "")
              bjt.setValue(value_s);
            if (angle <= 45 || angle > 315)
              bjt.setOrientation(Orientation.DEFAULT);
            else if (angle > 45 && angle <= 135)
              bjt.setOrientation(Orientation._90);
            else if (angle > 135 && angle <= 225)
              bjt.setOrientation(Orientation._180);
            else if (angle > 225 && angle <= 315)
              bjt.setOrientation(Orientation._270);
            Point point;
            int x = (int) (tacke.get(0).getX() - 20);
            int y = (int) tacke.get(0).getY();
            point = new Point(x, y);
            bjt.setControlPoint(point, 0);
            project.getComponents().add(bjt);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("Octal Tube Socket")) {
            TubeSocket ts = new TubeSocket();
            if (com_name != "") {
              ts.setName(com_name);
            } else
              ts.setName("ts");
            ts.setAlpha((byte) transparency);
            if (value_s != "")
              ts.setValue(value_s);
            ts.setAngle((int) angle);
            ts.setControlPoint(tacke.get(0), 0);
            ts.setBase(Base.OCTAL);
            project.getComponents().add(ts);
          } else if (node.getAttributes().getNamedItem("name").getNodeValue().equalsIgnoreCase("7-pin Tube Socket")) {
            TubeSocket ts = new TubeSocket();
            if (com_name != "") {
              ts.setName(com_name);
            } else
              ts.setName("ts");
            ts.setAlpha((byte) transparency);
            if (value_s != "")
              ts.setValue(value_s);
            ts.setAngle((int) angle);

            ts.setControlPoint(tacke.get(0), 0);
            ts.setBase(Base.B7G);
            project.getComponents().add(ts);
          }
        } else {
          LOG.debug("Unrecognized node name found: " + node.getNodeName());
        }
      }
    }
    return project;
  }

}
