package org.diylc.swing.gui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.diylc.appframework.miscutils.IConfigurationManager;
import org.diylc.appframework.miscutils.InMemoryConfigurationManager;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.common.EventType;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.common.PropertyWrapper;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractCurvedComponent.PointCount;
import org.diylc.components.connectivity.CurvedTrace;
import org.diylc.components.connectivity.Dot;
import org.diylc.components.connectivity.Line;
import org.diylc.components.misc.Label;
import org.diylc.components.misc.LoadlineEntity;
import org.diylc.core.IView;
import org.diylc.core.Project;
import org.diylc.core.measures.Current;
import org.diylc.core.measures.CurrentUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.core.measures.Voltage;
import org.diylc.core.measures.VoltageUnit;
import org.diylc.images.IconLoader;
import org.diylc.presenter.Presenter;
import org.diylc.swing.plugins.canvas.CanvasPanel;
import org.diylc.swingframework.ButtonDialog;
import org.diylc.swingframework.DoubleTextField;

public class LoadlineEditorDialog extends ButtonDialog implements IView {

  private static final long serialVersionUID = 1L;
  private static final String AXIS_LABEL = "AXIS_LABEL";
  private LoadlineEntity loadline;
  private JPanel panel;
  private JPanel toolbar;
  private JPanel controlPanel;
  private CanvasPanel canvasPanel;
  private Presenter plugInPort;
  private IConfigurationManager configManager;

  private static int[] INCREMENTS = new int[] {100, 50, 20, 10, 5, 1};
  private int xIncrement;
  private int yIncrement;

  public LoadlineEditorDialog(JFrame owner, String title, LoadlineEntity loadline) {
    super(owner, title, new String[] {ButtonDialog.OK, ButtonDialog.CANCEL});

    if (loadline == null) {
      loadline = new LoadlineEntity();
      loadline.setMaxVoltage(new Voltage(300d, VoltageUnit.V));
      loadline.setMaxCurrent(new Current(5d, CurrentUnit.mA));
    }
    
    configManager = new InMemoryConfigurationManager(new HashMap<String, Object>() {
      private static final long serialVersionUID = 1L;

      {
        put(IPlugInPort.LOCKED_ALPHA, false);
        put(IPlugInPort.EXTRA_SPACE_KEY, false);
      }
    });
    

    this.loadline = loadline;
    this.plugInPort = new Presenter(this, configManager);
    this.plugInPort.installPlugin(new IPlugIn() {

      @Override
      public void processMessage(EventType type, Object... params) {
        switch (type) {
          case REPAINT:
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
    initializeProject();
    updateAxis();

    layoutGui();
  }

  @Override
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
      toolbar = new JPanel();
      toolbar.add(new JButton(new LoadAction()));
      toolbar.add(new JButton(new SaveAction()));
      toolbar.add(new JButton(new LoadImageAction()));
      toolbar.add(new JButton(new AddCurveAction()));
    }
    return toolbar;
  }

  public CanvasPanel getCanvasPanel() {
    if (canvasPanel == null) {
      // create canvas panel that doesn't render locked components in lower alpha
      canvasPanel = new CanvasPanel(plugInPort, configManager);
      canvasPanel.setPreferredSize(plugInPort.getCanvasDimensions(true, false));

      canvasPanel.addMouseMotionListener(new MouseAdapter() {

        @Override
        public void mouseMoved(MouseEvent e) {
          canvasPanel.setCursor(plugInPort.getCursorAt(e.getPoint()));
          plugInPort.mouseMoved(e.getPoint(), Utils.isMac() ? e.isMetaDown() : e.isControlDown(),
              e.isShiftDown(), e.isAltDown());
        }
      });
    }
    return canvasPanel;
  }

  public JPanel getControlPanel() {
    if (controlPanel == null) {
      controlPanel = new JPanel();


      controlPanel.add(new JLabel("Max Voltage (V): "));
      JTextField vEditor = new DoubleTextField();
      vEditor.setColumns(8);
      controlPanel.add(vEditor);

      controlPanel.add(new JLabel("Max Current (mA): "));
      JTextField iEditor = new DoubleTextField();
      iEditor.setColumns(8);
      controlPanel.add(iEditor);

      controlPanel.add(new JLabel("Max Dissipation (W): "));
      JTextField dEditor = new DoubleTextField();
      dEditor.setColumns(8);
      controlPanel.add(dEditor);

      JButton updateButton = new JButton("Update");
      controlPanel.add(updateButton);

    }
    return controlPanel;
  }


  private void initializeProject() {
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

    plugInPort.loadProject(project, true, "n/a");
  }

  private void updateAxis() {
    int v = loadline.getMaxVoltage().getValue().intValue();
    int i = loadline.getMaxCurrent().getValue().intValue();

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

    CurvedTrace t = new CurvedTrace();
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
      Set<PropertyWrapper> defaultedProperties) {
    // TODO Auto-generated method stub
    return false;
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
    public void actionPerformed(ActionEvent e) {}
  }

  class LoadAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public LoadAction() {
      super();
      putValue(Action.NAME, "Load From File");
      putValue(Action.SMALL_ICON, IconLoader.FolderOut.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {}
  }

  class AddCurveAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public AddCurveAction() {
      super();
      putValue(Action.NAME, "Add a Curve");
      // putValue(Action.SMALL_ICON, IconLoader.DiskBlue.getIcon());
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
    public void actionPerformed(ActionEvent e) {}
  }
}
