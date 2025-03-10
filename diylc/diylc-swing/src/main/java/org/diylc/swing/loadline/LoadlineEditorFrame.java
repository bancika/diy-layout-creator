package org.diylc.swing.loadline;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.diylc.appframework.miscutils.IConfigurationManager;
import org.diylc.appframework.miscutils.InMemoryConfigurationManager;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.components.connectivity.Dot;
import org.diylc.components.connectivity.Line;
import org.diylc.components.misc.Image;
import org.diylc.components.misc.Image.ImageSizingMode;
import org.diylc.components.misc.Label;
import org.diylc.components.misc.LoadlineCurve;
import org.diylc.components.misc.LoadlineEntity;
import org.diylc.swingframework.ButtonDialog;
import org.diylc.swingframework.DoubleTextField;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.diylc.common.EventType;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.common.PropertyWrapper;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractCurvedComponent.PointCount;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IView;
import org.diylc.core.Project;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.core.measures.Voltage;
import org.diylc.core.measures.VoltageUnit;
import org.diylc.presenter.Presenter;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.gui.TranslatedPanel;
import org.diylc.swing.gui.editor.PropertyEditorDialog;
import org.diylc.utils.IconLoader;
import org.diylc.swing.plugins.canvas.CanvasPanel;
import org.diylc.swing.plugins.file.FileFilterEnum;

public class LoadlineEditorFrame extends JFrame implements IView {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(LoadlineEditorFrame.class);

  private static final String AXIS_LABEL = "AXIS_LABEL";
  private static final String CURVE = "CURVE";

  private LoadlineEntity loadline;
  private JPanel panel;
  private JPanel toolbar;
  private JPanel controlPanel;
  private CanvasPanel canvasPanel;
  private Presenter plugInPort;
  private IConfigurationManager<?> configManager;

  private JTextField nameEditor;
  private DoubleTextField vEditor;
  private DoubleTextField iEditor;
  private DoubleTextField dEditor;

  private static int[] INCREMENTS = new int[] {100, 50, 20, 10, 5, 1};
  private int xIncrement;
  private int yIncrement;

  public LoadlineEditorFrame() {
    super("Loadline Editor");
    setIconImage(IconLoader.Loadline.getImage());
    setResizable(false);

    if (loadline == null) {
      loadline = new LoadlineEntity();
      loadline.setMaxVoltage(300);
      loadline.setMaxCurrent(5);
      loadline.setLines(new ArrayList<LoadlineEntity.Line>());
    }

    configManager = new InMemoryConfigurationManager(new HashMap<String, Object>() {
      private static final long serialVersionUID = 1L;

      {
        put(IPlugInPort.LOCKED_ALPHA, false);
        put(IPlugInPort.EXTRA_SPACE_KEY, false);
        put(IPlugInPort.SHOW_GRID_KEY, false);
      }
    });


    this.plugInPort = new Presenter(this, configManager);
    this.plugInPort.installPlugin(() -> new IPlugIn() {

      @SuppressWarnings("incomplete-switch")
      @Override
      public void processMessage(EventType type, Object... params) {
        switch (type) {
          case REPAINT:
            applyToLoadline();
            getCanvasPanel().repaint();
            break;
        }
      }

      @Override
      public EnumSet<EventType> getSubscribedEventTypes() {
        return EnumSet.of(EventType.REPAINT);
      }

      @Override
      public void connect(IPlugInPort plugInPort) {}
    });

    loadProject();
    
    setContentPane(getMainComponent());
    pack();
    setLocationRelativeTo(null);
  }

  private void loadProject() {
    getvEditor().setValue(loadline.getMaxVoltage());
    getiEditor().setValue(loadline.getMaxCurrent());
    getdEditor().setValue(loadline.getMaxDissipation());
    getNameEditor().setText(loadline.getName());
    loadlineToProject();
    updateAxis();
  }

  protected JComponent getMainComponent() {
    if (panel == null) {
      panel = new JPanel();
      panel.setLayout(new BorderLayout());
      panel.add(getToolbar(), BorderLayout.NORTH);
      panel.add(getCanvasPanel(), BorderLayout.CENTER);
      panel.add(getControlPanel(), BorderLayout.SOUTH);
    }
    return panel;
  }

  public JPanel getToolbar() {
    if (toolbar == null) {
      toolbar = new TranslatedPanel();
      toolbar.add(new JButton(new ClearAction()));
      toolbar.add(new JButton(new LoadAction()));
      toolbar.add(new JButton(new SaveAction()));
      toolbar.add(new JButton(new LoadImageAction()));
      toolbar.add(new JButton(new AddCurveAction()));
      toolbar.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, UIManager.getColor("InternalFrame.borderShadow")));
    }
    return toolbar;
  }

  public CanvasPanel getCanvasPanel() {
    if (canvasPanel == null) {
      // create canvas panel that doesn't render locked components in lower alpha
      canvasPanel = new CanvasPanel(plugInPort, configManager);
      canvasPanel.setPreferredSize(plugInPort.getCanvasDimensions(true, false));

      canvasPanel.addMouseListener(new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
          if (e.getButton() == MouseEvent.BUTTON2)
            return;
          plugInPort.mouseClicked(e.getPoint(), e.getButton(),
              Utils.isMac() ? e.isMetaDown() : e.isControlDown(), e.isShiftDown(), e.isAltDown(),
              e.getClickCount());
        }

        @Override
        public void mousePressed(MouseEvent e) {
          canvasPanel.requestFocus();
        }
      });

      canvasPanel.addMouseMotionListener(new MouseAdapter() {

        @Override
        public void mouseMoved(MouseEvent e) {
          canvasPanel.setCursor(plugInPort.getCursorAt(e.getPoint(), Utils.isMac() ? e.isMetaDown() : e.isControlDown(),
              e.isShiftDown(), e.isAltDown()));
          plugInPort.mouseMoved(e.getPoint(), Utils.isMac() ? e.isMetaDown() : e.isControlDown(),
              e.isShiftDown(), e.isAltDown());
        }
      });
    }
    return canvasPanel;
  }
  
  public JTextField getNameEditor() {
    if (nameEditor == null) {
      nameEditor = new JTextField();
      nameEditor.setColumns(12);
    }
    return nameEditor;
  }

  public DoubleTextField getvEditor() {
    if (vEditor == null) {
      vEditor = new DoubleTextField();
      vEditor.setColumns(8);
    }
    return vEditor;
  }

  public DoubleTextField getiEditor() {
    if (iEditor == null) {
      iEditor = new DoubleTextField();
      iEditor.setColumns(8);
    }
    return iEditor;
  }

  public DoubleTextField getdEditor() {
    if (dEditor == null) {
      dEditor = new DoubleTextField();
      dEditor.setColumns(8);
    }
    return dEditor;
  }

  public JPanel getControlPanel() {
    if (controlPanel == null) {
      controlPanel = new TranslatedPanel();

      controlPanel.add(new JLabel("Name: "));
      controlPanel.add(getNameEditor());
      
      controlPanel.add(new JLabel("Max Voltage (V): "));
      controlPanel.add(getvEditor());

      controlPanel.add(new JLabel("Max Current (mA): "));
      controlPanel.add(getiEditor());

      controlPanel.add(new JLabel("Max Dissipation (W): "));
      controlPanel.add(getdEditor());

      JButton updateButton = new JButton(new UpdateValuesAction());
      controlPanel.add(updateButton);

      controlPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("InternalFrame.borderShadow")));
    }
    return controlPanel;
  }


  private void loadlineToProject() {
    Project project = new Project();
    project.setWidth(new Size(5d, SizeUnit.in));
    project.setHeight(new Size(3d, SizeUnit.in));
    project.setGridSpacing(new Size(0.05d, SizeUnit.in));

    double spacing = project.getGridSpacing().convertToPixels();

    int centerX = (int) (spacing * 3);
    int centerY = (int) (project.getHeight().convertToPixels() - spacing * 3);

    // setup coordinate system
    Line y = new Line();
    y.setControlPoint(new Point(centerX, (int) (spacing * 2)), 0);
    y.setControlPoint(new Point(centerX, centerY), 1);
    y.setArrowStart(true);
    y.setThickness(new Size(2d, SizeUnit.px));
    project.getComponents().add(y);
    project.getLockedComponents().add(y);

    Label yLabel = new Label();
    yLabel.setValue("I(ma)");
    yLabel.setControlPoint(new Point((int) (centerX + spacing), (int) (spacing * 5)), 0);
    yLabel.setHorizontalAlignment(HorizontalAlignment.LEFT);
    yLabel.setVerticalAlignment(VerticalAlignment.TOP);
    yLabel.setBold(true);
    project.getComponents().add(yLabel);
    project.getLockedComponents().add(yLabel);

    Line x = new Line();
    x.setControlPoint(new Point(centerX, centerY), 0);
    x.setControlPoint(
        new Point((int) (project.getWidth().convertToPixels() - spacing * 2), centerY), 1);
    x.setArrowEnd(true);
    x.setThickness(new Size(2d, SizeUnit.px));
    project.getComponents().add(x);
    project.getLockedComponents().add(x);

    Label xLabel = new Label();
    xLabel.setValue("U(V)");
    xLabel.setControlPoint(new Point((int) (project.getWidth().convertToPixels() - spacing * 4),
        (int) (centerY - spacing)), 0);
    xLabel.setHorizontalAlignment(HorizontalAlignment.RIGHT);
    xLabel.setVerticalAlignment(VerticalAlignment.TOP);
    xLabel.setBold(true);
    project.getComponents().add(xLabel);
    project.getLockedComponents().add(xLabel);

    Dot dot = new Dot();
    dot.setControlPoint(new Point(centerX, centerY), 0);
    project.getComponents().add(dot);
    project.getLockedComponents().add(dot);
    
    double v = loadline.getMaxVoltage();
    double i = loadline.getMaxCurrent();
    int maxX = (int) (project.getWidth().convertToPixels() - spacing * 4);
    int maxY = (int) (spacing * 5);
    
    for(LoadlineEntity.Line l : loadline.getLines()) {
      LoadlineCurve t = new LoadlineCurve();
      t.setName(CURVE);
      
      if (l.getControlPoints().length == 2)
        t.setPointCount(PointCount.TWO);
      else if (l.getControlPoints().length == 3)
        t.setPointCount(PointCount.THREE);
      else if (l.getControlPoints().length == 4)
        t.setPointCount(PointCount.FOUR);
      else if (l.getControlPoints().length == 5)
        t.setPointCount(PointCount.FIVE);
      else if (l.getControlPoints().length == 7)
        t.setPointCount(PointCount.SEVEN);
      
      for (int j = 0; j < l.getControlPoints().length; j++) {
        Point2D p = l.getControlPoints()[j];
//        points[j] = new Point2D.Double(1.0f * (p.x - centerX) / (maxX - centerX) * v,
//            1.0f * (p.y - centerY) / (maxY - centerY) * i);
        t.setControlPoint(new Point((int) (p.getX() / v * (maxX - centerX) + centerX), (int) (p.getY() / i * (maxY - centerY) + centerY)), j);
      }      
      t.setThickness(new Size(2d, SizeUnit.px));
      t.setLeadColor(Color.blue);
      t.setVoltage(new Voltage(l.getVoltage(), VoltageUnit.V));
      project.getComponents().add(t);
    }

    plugInPort.loadProject(project, true, "n/a");
  }

  private void updateAxis() {
    int v = (int) loadline.getMaxVoltage();
    int i = (int) loadline.getMaxCurrent();

    for (int j = 0; j < INCREMENTS.length; j++) {
      int increment = INCREMENTS[j];
      if (v % increment == 0 && v / increment >= 5) {
        xIncrement = increment;
        break;
      }
    }

    for (int j = 0; j < INCREMENTS.length; j++) {
      int increment = INCREMENTS[j];
      if (i % increment == 0 && i / increment >= 5) {
        yIncrement = increment;
        break;
      }
    }

    // delete old axis components
    plugInPort.selectMatching(AXIS_LABEL);
    plugInPort.deleteSelectedComponents();

    Project project = plugInPort.getCurrentProject();

    double spacing = project.getGridSpacing().convertToPixels();
    int centerX = (int) (spacing * 3);
    int centerY = (int) (project.getHeight().convertToPixels() - spacing * 3);
    int maxX = (int) (project.getWidth().convertToPixels() - spacing * 4);
    int maxY = (int) (spacing * 5);

    for (int j = xIncrement; j <= v; j += xIncrement) {
      Line tick = new Line();
      tick.setName(AXIS_LABEL);
      double offset = 1.0 * (maxX - centerX) * (1.0 * j / v);
      tick.setControlPoint(new Point((int) (centerX + offset), centerY), 0);
      tick.setControlPoint(new Point((int) (centerX + offset), (int) (centerY + spacing / 2)), 1);
      tick.setThickness(new Size(1d, SizeUnit.px));
      project.getComponents().add(tick);
      project.getLockedComponents().add(tick);

      Label xLabel = new Label();
      xLabel.setName(AXIS_LABEL);
      xLabel.setValue(String.valueOf(j));
      xLabel.setControlPoint(new Point((int) (centerX + offset), (int) (centerY + spacing / 2 + 1)),
          0);
      xLabel.setHorizontalAlignment(HorizontalAlignment.CENTER);
      xLabel.setVerticalAlignment(VerticalAlignment.BOTTOM);
      // xLabel.setFontSize((int) (xLabel.getFontSize() * 0.8));
      project.getComponents().add(xLabel);
      project.getLockedComponents().add(xLabel);
    }

    for (int j = yIncrement; j <= i; j += yIncrement) {
      Line tick = new Line();
      tick.setName(AXIS_LABEL);
      double offset = 1.0 * (maxY - centerY) * (1.0 * j / i);
      tick.setControlPoint(new Point(centerX, (int) (centerY + offset)), 0);
      tick.setControlPoint(new Point((int) (centerX - spacing / 2), (int) (centerY + offset)), 1);
      tick.setThickness(new Size(1d, SizeUnit.px));
      project.getComponents().add(tick);
      project.getLockedComponents().add(tick);

      Label yLabel = new Label();
      yLabel.setName(AXIS_LABEL);
      yLabel.setValue(String.valueOf(j));
      yLabel.setControlPoint(new Point((int) (centerX - spacing / 2 - 1), (int) (centerY + offset)),
          0);
      yLabel.setHorizontalAlignment(HorizontalAlignment.RIGHT);
      yLabel.setVerticalAlignment(VerticalAlignment.CENTER);
      // yLabel.setFontSize((int) (yLabel.getFontSize() * 0.8));
      project.getComponents().add(yLabel);
      project.getLockedComponents().add(yLabel);
    }
  }

  private void addCurve() {
    Project project = plugInPort.getCurrentProject();
    double spacing = project.getGridSpacing().convertToPixels();

    int centerX = (int) (spacing * 3);
    int centerY = (int) (project.getHeight().convertToPixels() - spacing * 3);

    LoadlineCurve t = new LoadlineCurve();
    t.setName(CURVE);
    t.setPointCount(PointCount.FOUR);
    t.setControlPoint(new Point(centerX, centerY), 0);
    t.setControlPoint(new Point((int) (centerX + 30 * spacing), (int) (centerY - 10 * spacing)), 1);
    t.setControlPoint(new Point((int) (centerX + 50 * spacing), (int) (centerY - 20 * spacing)), 2);
    t.setControlPoint(new Point((int) (centerX + 70 * spacing), (int) (centerY - 50 * spacing)), 3);
    t.setThickness(new Size(2d, SizeUnit.px));
    t.setLeadColor(Color.blue);
    project.getComponents().add(t);

    plugInPort.loadProject(plugInPort.getCurrentProject(), false, "n/a");
  }

  private void applyToLoadline() {
    Project project = plugInPort.getCurrentProject();
    double spacing = project.getGridSpacing().convertToPixels();

    loadline.setName(getNameEditor().getText());
    int centerX = (int) (spacing * 3);
    int centerY = (int) (project.getHeight().convertToPixels() - spacing * 3);
    double v = loadline.getMaxVoltage();
    double i = loadline.getMaxCurrent();
    int maxX = (int) (project.getWidth().convertToPixels() - spacing * 4);
    int maxY = (int) (spacing * 5);

    List<org.diylc.components.misc.LoadlineEntity.Line> lines =
        new ArrayList<org.diylc.components.misc.LoadlineEntity.Line>();
    for (IDIYComponent<?> c : project.getComponents()) {
      if (CURVE.equals(c.getName())) {
        Point2D[] points = new Point2D[c.getControlPointCount()];
        for (int j = 0; j < c.getControlPointCount(); j++) {
          Point2D p = c.getControlPoint(j);
          points[j] = new Point2D.Double(1.0f * (p.getX() - centerX) / (maxX - centerX) * v,
              1.0f * (p.getY() - centerY) / (maxY - centerY) * i);
        }
        LoadlineCurve curve = (LoadlineCurve)c;
        org.diylc.components.misc.LoadlineEntity.Line line =
            new org.diylc.components.misc.LoadlineEntity.Line();
        line.setControlPoints(points);
        line.setVoltage(curve.getVoltage() == null ? 0 : curve.getVoltage().getValue());
        lines.add(line);
      }
    }
    loadline.setLines(lines);
  }
  
  public static XStream loadlineXStream() {
    XStream xStream = new XStream(new DomDriver("UTF-8"));
    xStream.autodetectAnnotations(true);
    xStream.alias("point", Point2D.Double.class);
    xStream.alias("curve", org.diylc.components.misc.LoadlineEntity.Line.class);
    xStream.alias("loadline", org.diylc.components.misc.LoadlineEntity.class);
    return xStream;
  }

  @Override
  public void showMessage(String message, String title, int messageType) {
    // TODO Auto-generated method stub

  }

  @Override
  public int showConfirmDialog(String message, String title, int optionType, int messageType) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean editProperties(List<PropertyWrapper> properties,
      Set<PropertyWrapper> defaultedProperties, String title) {
    PropertyEditorDialog editor = DialogFactory.getInstance().createPropertyEditorDialog(LoadlineEditorFrame.this, properties,
        title, true);
    editor.setVisible(true);
    defaultedProperties.addAll(editor.getDefaultedProperties());
    return ButtonDialog.OK.equals(editor.getSelectedButtonCaption());
  }

  @Override
  public String showInputDialog(String message, String title) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public File promptFileSave() {
    // TODO Auto-generated method stub
    return null;
  }

  class SaveAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public SaveAction() {
      super();
      putValue(Action.NAME, "Save To File");
      putValue(Action.SMALL_ICON, IconLoader.DiskBlue.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      applyToLoadline();

      final File file = DialogFactory.getInstance().showSaveDialog(LoadlineEditorFrame.this,
          FileFilterEnum.CRV.getFilter(), null, FileFilterEnum.CRV.getExtensions()[0], null);
      if (file != null) {
        try {
          XStream xStream = loadlineXStream();

          FileOutputStream fos;
          fos = new FileOutputStream(file);
          Writer writer = new OutputStreamWriter(fos, "UTF-8");
          writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
          xStream.toXML(loadline, writer);
          fos.close();
        } catch (Exception ex) {
          LOG.error("Error saving loadline file", ex);
        }
      }
    }
  }

  class ClearAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public ClearAction() {
      super();
      putValue(Action.NAME, "Clear");
      putValue(Action.SMALL_ICON, IconLoader.Delete.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      loadline = new LoadlineEntity();
      loadline.setMaxVoltage(300);
      loadline.setMaxCurrent(5);
      loadline.setName("New");
      loadline.setLines(new ArrayList<LoadlineEntity.Line>());

      loadProject();
    }
  }

  class LoadAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public LoadAction() {
      super();
      putValue(Action.NAME, "Load From File");
      putValue(Action.SMALL_ICON, IconLoader.FolderOut.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      final File file = DialogFactory.getInstance().showOpenDialog(LoadlineEditorFrame.this, FileFilterEnum.CRV.getFilter(), null, 
          FileFilterEnum.CRV.getExtensions()[0], null);
      if (file != null) {
        try {
          XStream xStream = loadlineXStream();

          FileInputStream fis = new FileInputStream(file);
          Reader reader = new InputStreamReader(fis, "UTF-8");
          loadline = (LoadlineEntity) xStream.fromXML(reader);
          fis.close();
          loadProject();
        } catch (Exception ex) {
          LOG.error("Error loading loadline file", ex);
        }
      }          
    }
  }

  class UpdateValuesAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public UpdateValuesAction() {
      super();
      putValue(Action.NAME, "Update Axis");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      loadline.setMaxVoltage(getvEditor().getValue());
      loadline.setMaxCurrent(getiEditor().getValue());
      loadline.setMaxDissipation(getdEditor().getValue());
      updateAxis();
      applyToLoadline();
    }
  }

  class AddCurveAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public AddCurveAction() {
      super();
      putValue(Action.NAME, "Add a Curve");
      putValue(Action.SMALL_ICON, IconLoader.LoadlineAdd.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      addCurve();
    }
  }

  class LoadImageAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public LoadImageAction() {
      super();
      putValue(Action.NAME, "Insert Image");
      putValue(Action.SMALL_ICON, IconLoader.Image.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      File file = DialogFactory.getInstance().showOpenDialog(LoadlineEditorFrame.this, FileFilterEnum.IMAGES.getFilter(),
          null, FileFilterEnum.IMAGES.getExtensions()[0], null);
      if (file != null) {
        FileInputStream fis;
        try {
          fis = new FileInputStream(file);
          byte[] byteArray = IOUtils.toByteArray(fis);
          Image image = new Image();
          image.setData(byteArray);
          image.setSizingMode(ImageSizingMode.TwoPoints);
          Dimension canvasDimensions = plugInPort.getCanvasDimensions(true, false);
          Project project = plugInPort.getCurrentProject();

          double spacing = project.getGridSpacing().convertToPixels();
          // stretch the image across the canvas
          image.setControlPoint(new Point((int) spacing, (int) spacing), 0);
          image.setControlPoint(new Point((int) (canvasDimensions.width - spacing),
              (int) (canvasDimensions.height - spacing)), 1);
          image.setAlpha((byte) 50); // make it transparent
          fis.close();
          // add to the bottom
          project.getComponents().add(0, image);
          // reload the project
          plugInPort.loadProject(plugInPort.getCurrentProject(), false, "n/a");
        } catch (Exception e1) {
          LOG.error("Error loading image", e1);
        }
      }
    }
  }
  
  public static class LoadlineEditorAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public LoadlineEditorAction() {
      super();
      putValue(Action.NAME, "Loadline Editor");
      putValue(Action.SMALL_ICON, IconLoader.Loadline.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      new LoadlineEditorFrame().setVisible(true);
    }
  }
}
