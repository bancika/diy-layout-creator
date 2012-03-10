package org.diylc.presenter;

import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.common.Display;
import org.diylc.common.EventType;
import org.diylc.common.Orientation;
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
import org.diylc.components.misc.Label;
import org.diylc.components.passive.PotentiometerPanel;
import org.diylc.components.passive.RadialElectrolytic;
import org.diylc.components.passive.RadialFilmCapacitor;
import org.diylc.components.passive.Resistor;
import org.diylc.components.passive.Taper;
import org.diylc.components.semiconductors.DIL_IC;
import org.diylc.components.semiconductors.DiodePlastic;
import org.diylc.components.semiconductors.LED;
import org.diylc.components.semiconductors.TransistorTO92;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ProjectFileManager {

	private static final Logger LOG = Logger.getLogger(ProjectFileManager.class);

	// private static final int V1_PIXELS_PER_INCH = 200;
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

	private XStream xStream;
	// Legacy deserializer for 3.0.1 through 3.0.7, loads Points referenced in
	// pixels.
	private XStream xStreamOld;

	private String currentFileName = null;
	private boolean modified = false;

	private MessageDispatcher<EventType> messageDispatcher;

	public ProjectFileManager(MessageDispatcher<EventType> messageDispatcher) {
		super();
		this.xStream = new XStream(new DomDriver());
		xStream.autodetectAnnotations(true);
		xStream.registerConverter(new PointConverter());
		this.xStreamOld = new XStream(new DomDriver());
		xStreamOld.autodetectAnnotations(true);
		this.messageDispatcher = messageDispatcher;
	}

	public void startNewFile() {
		currentFileName = null;
		modified = false;
		fireFileStatusChanged();
	}

	public void serializeProjectToFile(Project project, String fileName, boolean isBackup)
			throws IOException {
		if (!isBackup) {
			LOG.info(String.format("saveProjectToFile(%s)", fileName));
		}
		FileOutputStream fos;
		fos = new FileOutputStream(fileName);
		Writer writer = new OutputStreamWriter(fos, "UTF-8");
		writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
		xStream.toXML(project, writer);
		fos.close();
		if (!isBackup) {
			this.currentFileName = fileName;
			this.modified = false;
			fireFileStatusChanged();
		}
	}

	public Project deserializeProjectFromFile(String fileName, List<String> warnings)
			throws SAXException, IOException, ParserConfigurationException {
		LOG.info(String.format("loadProjectFromFile(%s)", fileName));
		Project project;
		File file = new File(fileName);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		doc.getDocumentElement().normalize();
		if (doc.getDocumentElement().getNodeName().equalsIgnoreCase(Project.class.getName())) {
			project = parseV3File(fileName);
		} else {
			if (!doc.getDocumentElement().getNodeName().equalsIgnoreCase("layout")) {
				throw new IllegalArgumentException(
						"Could not open DIY file. Root node is not named 'Layout'.");
			}
			String formatVersion = doc.getDocumentElement().getAttribute("formatVersion");
			if (formatVersion == null || formatVersion.trim().isEmpty()) {
				LOG.debug("Detected v1 file.");
				project = parseV1File(doc.getDocumentElement(), warnings);
			} else if (formatVersion.equals("2.0")) {
				LOG.debug("Detected v2 file.");
				project = parseV2File(doc.getDocumentElement());
			} else {
				throw new IllegalArgumentException("Unknown file format version: " + formatVersion);
			}
		}
		Collections.sort(warnings);
		this.currentFileName = fileName;
		this.modified = false;
		return project;
	}

	public void notifyFileChange() {
		this.modified = true;
		fireFileStatusChanged();
	}

	public String getCurrentFileName() {
		return currentFileName;
	}

	public boolean isModified() {
		return modified;
	}

	public void fireFileStatusChanged() {
		messageDispatcher.dispatchMessage(EventType.FILE_STATUS_CHANGED, getCurrentFileName(),
				isModified());
	}

	private Project parseV1File(Element root, List<String> warnings) {
		Project project = new Project();
		project.setTitle(root.getAttribute("Project"));
		project.setAuthor(root.getAttribute("Credits"));
		project.setGridSpacing(V1_GRID_SPACING);
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
			board.setBorderColor(Color.black);
		} else if (type.equalsIgnoreCase("perfboard")) {
			board = new PerfBoard();
		} else if (type.equalsIgnoreCase("stripboard")) {
			board = new VeroBoard();
		} else {
			throw new IllegalArgumentException("Unrecognized board type: " + type);
		}
		board.setName("Main board");
		Point referencePoint = new Point(CalcUtils.roundToGrid(x, V1_GRID_SPACING), CalcUtils
				.roundToGrid(y, V1_GRID_SPACING));
		board.setControlPoint(referencePoint, 0);
		board.setControlPoint(new Point(CalcUtils.roundToGrid(x + boardWidth, V1_GRID_SPACING),
				CalcUtils.roundToGrid(y + boardHeight, V1_GRID_SPACING)), 1);
		project.getComponents().add(board);

		NodeList childNodes = root.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String nodeName = node.getNodeName();
				String nameAttr = node.getAttributes().getNamedItem("Name").getNodeValue();
				Node valueNode = node.getAttributes().getNamedItem("Value");
				String valueAttr = valueNode == null ? null : valueNode.getNodeValue();
				int x1Attr = Integer.parseInt(node.getAttributes().getNamedItem("X1")
						.getNodeValue());
				int y1Attr = Integer.parseInt(node.getAttributes().getNamedItem("Y1")
						.getNodeValue());
				Point point1 = convertV1CoordinatesToV3Point(referencePoint, x1Attr, y1Attr);
				Point point2 = null;
				Integer x2Attr = null;
				Integer y2Attr = null;
				Color color = null;
				if (node.getAttributes().getNamedItem("Color") != null) {
					String colorAttr = node.getAttributes().getNamedItem("Color").getNodeValue();
					color = V1_COLOR_MAP.get(colorAttr.toLowerCase());
				}
				if (node.getAttributes().getNamedItem("X2") != null
						&& node.getAttributes().getNamedItem("Y2") != null) {
					x2Attr = Integer.parseInt(node.getAttributes().getNamedItem("X2")
							.getNodeValue());
					y2Attr = Integer.parseInt(node.getAttributes().getNamedItem("Y2")
							.getNodeValue());
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
					label.setText(valueAttr);
					label.setCenter(false);
					label.setControlPoint(convertV1CoordinatesToV3Point(referencePoint, x1Attr,
							y1Attr + 1), 0);
					component = label;
				} else if (nodeName.equalsIgnoreCase("pad")) {
					LOG.debug("Recognized " + nodeName);
					SolderPad pad = new SolderPad();
					pad.setName(nameAttr);
					if (color != null) {
						pad.setColor(color);
					}
					pad.setControlPoint(convertV1CoordinatesToV3Point(referencePoint, x1Attr,
							y1Attr), 0);
					component = pad;
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
					Point midPoint = new Point((point1.x + point2.x) / 2, (point1.y + point2.y) / 2);
					wire.setName(nameAttr);
					String colorAttr = node.getAttributes().getNamedItem("Color").getNodeValue();
					wire.setColor(parseV1Color(colorAttr));
					wire.setControlPoint(point1, 0);
					wire.setControlPoint(midPoint, 1);
					wire.setControlPoint(midPoint, 2);
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
					if (point1.y > point2.y) {
						transistor.setOrientation(Orientation._180);
					} else if (point1.y < point2.y) {
						transistor.setOrientation(Orientation.DEFAULT);
					} else if (point1.x < point2.x) {
						transistor.setOrientation(Orientation._270);
					} else if (point1.x > point2.x) {
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
						Point p = new Point(ic.getControlPoint(j));
						p.translate(point1.x, point1.y);
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
						pot.setResistance(Resistance.parseResistance(valueAttr));
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
							Point p = new Point(pot.getControlPoint(j));
							p.translate(point1.x - delta, point1.y);
							pot.setControlPoint(p, j);
						}
					} else if (x1Attr > x2Attr) {
						pot.setOrientation(Orientation._180);
						for (int j = 0; j < pot.getControlPointCount(); j++) {
							Point p = new Point(pot.getControlPoint(j));
							p.translate(point1.x + delta, point1.y);
							pot.setControlPoint(p, j);
						}
					} else if (y1Attr < y2Attr) {
						pot.setOrientation(Orientation._90);
						for (int j = 0; j < pot.getControlPointCount(); j++) {
							Point p = new Point(pot.getControlPoint(j));
							p.translate(point1.x, point1.y - delta);
							pot.setControlPoint(p, j);
						}
					} else if (y1Attr > y2Attr) {
						pot.setOrientation(Orientation._270);
						for (int j = 0; j < pot.getControlPointCount(); j++) {
							Point p = new Point(pot.getControlPoint(j));
							p.translate(point1.x, point1.y + delta);
							pot.setControlPoint(p, j);
						}
						;
					}
					component = pot;
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
		Collections.sort(project.getComponents(), ComparatorFactory.getInstance()
				.getComponentZOrderComparator());
		return project;
	}

	private Point convertV1CoordinatesToV3Point(Point reference, int x, int y) {
		Point point = new Point(reference);
		point.translate((int) (x * Constants.PIXELS_PER_INCH * V1_GRID_SPACING.getValue()),
				(int) (y * Constants.PIXELS_PER_INCH * V1_GRID_SPACING.getValue()));
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

	private Project parseV2File(Element root) {
		Project project = new Project();
		String projectName = root.getAttribute("projectName");
		String credits = root.getAttribute("credits");
		String width = root.getAttribute("width");
		String height = root.getAttribute("height");
		NodeList childNodes = root.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				if (node.getNodeName().equalsIgnoreCase("component")) {
					LOG.debug(node.getAttributes().getNamedItem("name").getNodeValue());
				} else {
					LOG.debug("Unrecognized node name found: " + node.getNodeName());
				}
			}
		}
		return project;
	}

	private Project parseV3File(String fileName) throws IOException {
		Project project;
		FileInputStream fis = new FileInputStream(fileName);
		try {
			Reader reader = new InputStreamReader(fis, "UTF-8");  
			project = (Project) xStream.fromXML(reader);
		} catch (Exception e) {
			LOG.warn("Could not open with the new xStream, trying the old one");
			fis.close();
			fis = new FileInputStream(fileName);
			project = (Project) xStreamOld.fromXML(fis);
		}
		fis.close();
		return project;
	}
}