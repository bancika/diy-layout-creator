package org.diylc.swing.plugins.statusbar;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.diylc.announcements.AnnouncementProvider;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.common.BadPositionException;
import org.diylc.common.ComponentType;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IView;
import org.diylc.images.IconLoader;
import org.diylc.presenter.Presenter;
import org.diylc.swing.ISwingUI;
import org.diylc.swingframework.MemoryBar;
import org.diylc.swingframework.miscutils.PercentageListCellRenderer;
import org.diylc.swingframework.update.UpdateLabel;

public class StatusBar extends JPanel implements IPlugIn {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(StatusBar.class);

  public static String UPDATE_URL = "http://www.diy-fever.com/update.xml";
  private static final Format sizeFormat = new DecimalFormat("0.##");

  private JComboBox zoomBox;
  private UpdateLabel updateLabel;
  private JLabel announcementLabel;
  private MemoryBar memoryPanel;
  private JLabel statusLabel;
  private JLabel sizeLabel;

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;

  // State variables
  private ComponentType componentSlot;
  private Point controlPointSlot;
  private List<String> componentNamesUnderCursor;
  private List<String> selectedComponentNames;
  private List<String> stuckComponentNames;
  private String statusMessage;

  private AnnouncementProvider announcementProvider;

  public StatusBar(ISwingUI swingUI) {
    super();
    this.swingUI = swingUI;

    this.announcementProvider = new AnnouncementProvider();

    setLayout(new GridBagLayout());

    try {
      swingUI.injectGUIComponent(this, SwingUtilities.BOTTOM);
    } catch (BadPositionException e) {
      LOG.error("Could not install status bar", e);
    }

    swingUI.executeBackgroundTask(new ITask<String>() {

      @Override
      public String doInBackground() throws Exception {
        Thread.sleep(1000);
        return announcementProvider.getCurrentAnnouncements(false);
      }

      @Override
      public void failed(Exception e) {
        LOG.error("Error while fetching announcements", e);
      }

      @Override
      public void complete(String result) {
        if (result != null && result.length() > 0) {
          StatusBar.this.swingUI.showMessage(result, "Public Announcement", IView.INFORMATION_MESSAGE);
          announcementProvider.dismissed();
        }
      }

    });
  }

  private JComboBox getZoomBox() {
    if (zoomBox == null) {
      zoomBox = new JComboBox(plugInPort.getAvailableZoomLevels());
      zoomBox.setSelectedItem(plugInPort.getZoomLevel());
      zoomBox.setFocusable(false);
      zoomBox.setRenderer(new PercentageListCellRenderer());
      zoomBox.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          plugInPort.setZoomLevel((Double) zoomBox.getSelectedItem());
        }
      });
    }
    return zoomBox;
  }

  private UpdateLabel getUpdateLabel() {
    if (updateLabel == null) {
      updateLabel = new UpdateLabel(plugInPort.getCurrentVersionNumber(), UPDATE_URL);
      // updateLabel.setBorder(BorderFactory.createCompoundBorder(
      // BorderFactory.createEtchedBorder(), BorderFactory
      // .createEmptyBorder(2, 4, 2, 4)));
      updateLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    }
    return updateLabel;
  }

  public JLabel getAnnouncementLabel() {
    if (announcementLabel == null) {
      announcementLabel = new JLabel(IconLoader.Megaphone.getIcon());
      announcementLabel.setToolTipText("Click to fetch the most recent public announcement");
      announcementLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      announcementLabel.addMouseListener(new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
          swingUI.executeBackgroundTask(new ITask<String>() {

            @Override
            public String doInBackground() throws Exception {
              return announcementProvider.getCurrentAnnouncements(true);
            }

            @Override
            public void failed(Exception e) {
              LOG.error("Error while fetching announcements", e);
              swingUI.showMessage("Could not fetch public announcements.", "Error", IView.ERROR_MESSAGE);
            }

            @Override
            public void complete(String result) {
              if (result != null && result.length() > 0) {
                swingUI.showMessage(result, "Public Announcement", IView.INFORMATION_MESSAGE);
                announcementProvider.dismissed();
              } else
                swingUI.showMessage("No new public announcements available.", "Public Announcement",
                    IView.INFORMATION_MESSAGE);
            }

          });
        }
      });
    }
    return announcementLabel;
  }

  private MemoryBar getMemoryPanel() {
    if (memoryPanel == null) {
      memoryPanel = new MemoryBar(false);
    }
    return memoryPanel;
  }

  private JLabel getStatusLabel() {
    if (statusLabel == null) {
      statusLabel = new JLabel();
      statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
    }
    return statusLabel;
  }

  public JLabel getSizeLabel() {
    if (sizeLabel == null) {
      sizeLabel = new JLabel(IconLoader.Size.getIcon());
      sizeLabel.setFocusable(true);
      sizeLabel.setToolTipText("Click to calculate selection size");
      sizeLabel.addMouseListener(new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
          Point2D size = plugInPort.calculateSelectionDimension();
          boolean metric = ConfigurationManager.getInstance().readBoolean(Presenter.METRIC_KEY, true);
          String text;
          if (size == null) {
            text = "Selection is empty.";
          } else {
            text =
                "Selection size: " + sizeFormat.format(size.getX()) + " x " + sizeFormat.format(size.getY())
                    + (metric ? " cm" : " in");
          }
          JOptionPane.showMessageDialog(SwingUtilities.getRootPane(StatusBar.this), text, "Information",
              JOptionPane.INFORMATION_MESSAGE);
        }
      });
    }
    return sizeLabel;
  }

  private void layoutComponents() {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1;
    add(getStatusLabel(), gbc);

    JPanel zoomPanel = new JPanel(new BorderLayout());
    zoomPanel.add(new JLabel("Zoom: "), BorderLayout.WEST);
    zoomPanel.add(getZoomBox(), BorderLayout.CENTER);
    // zoomPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
    // .createEtchedBorder(), BorderFactory.createEmptyBorder(2, 4, 2,
    // 4)));
    zoomPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

    gbc.gridx++;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 0;
    add(zoomPanel, gbc);

    gbc.gridx++;
    gbc.insets = new Insets(0, 2, 0, 2);
    add(getSizeLabel(), gbc);

    gbc.gridx++;
    add(getAnnouncementLabel(), gbc);

    gbc.gridx++;
    gbc.insets = new Insets(0, 0, 0, 0);
    add(getUpdateLabel(), gbc);

    gbc.gridx++;
    gbc.fill = GridBagConstraints.NONE;
    gbc.insets = new Insets(0, 0, 0, 4);
    add(getMemoryPanel(), gbc);

    gbc.gridx = 5;
    add(new JPanel(), gbc);
  }

  // IPlugIn

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;

    layoutComponents();
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.of(EventType.ZOOM_CHANGED, EventType.SLOT_CHANGED, EventType.AVAILABLE_CTRL_POINTS_CHANGED,
        EventType.SELECTION_CHANGED, EventType.STATUS_MESSAGE_CHANGED);
  }

  @SuppressWarnings({"unchecked", "incomplete-switch"})
  @Override
  public void processMessage(EventType eventType, Object... params) {
    switch (eventType) {
      case ZOOM_CHANGED:
        if (!params[0].equals(getZoomBox().getSelectedItem())) {
          final Double zoom = (Double) params[0];
          SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
              getZoomBox().setSelectedItem(zoom);
            }
          });
        }
        break;
      case SELECTION_CHANGED:
        List<IDIYComponent<?>> selection = (List<IDIYComponent<?>>) params[0];
        Collection<IDIYComponent<?>> stuckComponents = (Collection<IDIYComponent<?>>) params[1];
        Collection<String> componentNames = new HashSet<String>();
        for (IDIYComponent<?> component : selection) {
          componentNames.add("<font color='blue'>" + component.getName() + "</font>");
        }
        this.selectedComponentNames = new ArrayList<String>(componentNames);
        Collections.sort(this.selectedComponentNames);
        this.stuckComponentNames = new ArrayList<String>();
        for (IDIYComponent<?> component : stuckComponents) {
          this.stuckComponentNames.add("<font color='blue'>" + component.getName() + "</font>");
        }
        this.stuckComponentNames.removeAll(this.selectedComponentNames);
        Collections.sort(this.stuckComponentNames);
        refreshStatusText();
        break;
      case SLOT_CHANGED:
        componentSlot = (ComponentType) params[0];
        controlPointSlot = (Point) params[1];
        refreshStatusText();
        break;
      case AVAILABLE_CTRL_POINTS_CHANGED:
        componentNamesUnderCursor = new ArrayList<String>();
        for (IDIYComponent<?> component : ((Map<IDIYComponent<?>, Integer>) params[0]).keySet()) {
          componentNamesUnderCursor.add("<font color='blue'>" + component.getName() + "</font>");
        }
        Collections.sort(componentNamesUnderCursor);
        refreshStatusText();
        break;
      case STATUS_MESSAGE_CHANGED:
        statusMessage = (String) params[0];
    }
  }

  private void refreshStatusText() {
    String statusText = this.statusMessage;
    if (componentSlot == null) {
      if (componentNamesUnderCursor != null && !componentNamesUnderCursor.isEmpty()) {
        String formattedNames = Utils.toCommaString(componentNamesUnderCursor);
        statusText = "<html>Drag control point(s) of " + formattedNames + "</html>";
      } else if (selectedComponentNames != null && !selectedComponentNames.isEmpty()) {
        StringBuilder builder = new StringBuilder();
        builder.append(Utils.toCommaString(selectedComponentNames.subList(0,
            Math.min(20, selectedComponentNames.size()))));
        if (selectedComponentNames.size() > 15) {
          builder.append(" and " + (selectedComponentNames.size() - 15) + " more");
        }
        if (!stuckComponentNames.isEmpty()) {
          builder.append(" (hold <b>Ctrl</b> and drag to unstuck from ");
          builder.append(Utils.toCommaString(stuckComponentNames.subList(0, Math.min(5, stuckComponentNames.size()))));
          if (stuckComponentNames.size() > 5) {
            builder.append(" and " + (stuckComponentNames.size() - 5) + " more");
          }
          builder.append(")");
        }
        statusText = "<html>Selection: " + builder.toString() + "</html>";
      }
    } else {
      switch (componentSlot.getCreationMethod()) {
        case POINT_BY_POINT:
          String count;
          if (controlPointSlot == null) {
            count = "first";
          } else {
            count = "second";
          }
          statusText =
              "<html>Click on the canvas to set the " + count + " control point of a new <font color='blue'>"
                  + componentSlot.getName() + "</font> or press <b>Esc</b> to cancel</html>";
          break;
        case SINGLE_CLICK:
          statusText =
              "<html>Click on the canvas to create a new <font color='blue'>" + componentSlot.getName()
                  + "</font> or press <b>Esc</b> to cancel</html>";
          break;
      }
    }
    final String finalStatus = statusText;
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        getStatusLabel().setText(finalStatus);
      }
    });
  }
}
