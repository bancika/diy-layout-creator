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
import javax.swing.UIManager;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.appframework.update.UpdateChecker;
import org.diylc.appframework.update.Version;
import org.diylc.common.*;
import org.diylc.swingframework.MemoryBar;
import org.diylc.swingframework.miscutils.PercentageListCellRenderer;
import org.diylc.swingframework.update.UpdateDialog;
import org.diylc.swingframework.update.UpdateLabel;
import net.java.balloontip.BalloonTip;
import net.java.balloontip.styles.EdgedBalloonStyle;
import org.diylc.announcements.AnnouncementProvider;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IView;
import org.diylc.lang.LangUtil;
import org.diylc.presenter.Presenter;
import org.diylc.swing.ISwingUI;
import org.diylc.utils.IconLoader;

public class StatusBar extends JPanel implements IPlugIn {

  private static final String CLICK_TO_CREATE = LangUtil.translate("Click on the canvas to create a new %s or press %s to cancel");
  private static final String CLICK_TO_SET_POINT =
      LangUtil.translate("Click on the canvas to set the %s control point of a new %s or press %s to cancel");
  private static final String HOLD_TO_UNSTUCK = LangUtil.translate("hold %s and drag to unstick from");
  private static final String SELECTION = LangUtil.translate("Selection");
  private static final String DRAG_POINTS_OF = LangUtil.translate("Drag control point(s) of");
  private static final String DRAG_TO_PLACE_A_NEW = LangUtil.translate("Drag the mouse over the canvas to place a new");
  private static final String ZOOM = LangUtil.translate("Zoom");
  private static final String SELECTION_SIZE = LangUtil.translate("Selection Size");
  private static final String SELECTION_IS_EMPTY = LangUtil.translate("Selection is empty.");
  private static final String INFORMATION = LangUtil.translate("Information");
  private static final String NO_HISTORY = LangUtil.translate("Version history is not available.");
  private static final String CLICK_UPDATES = LangUtil.translate("Click to show recent changes and updates");
  private static final String NO_ANNOUNCEMENTS = LangUtil.translate("No new public announcements available.");
  private static final String ERROR_TITLE = LangUtil.translate("Error");
  private static final String PUBLIC_ANNOUNCEMENT = LangUtil.translate("Public Announcement");
  private static final String COULD_NOT_FETCH_ANNOUNCEMENTS = LangUtil.translate("Could not fetch public announcements.");
  private static final String CLICK_TO_FETCH_ANNOUNCEMENT = LangUtil.translate("Click to fetch the most recent public announcement");
  private static final String CLICK_THE_LIGHTBULB = LangUtil.translate("Click the lightbulb icon in the bottom-right corner of the window for more info.");
  private static final String NEW_VERSION_AVAILABLE = LangUtil.translate("New version available");
  private static String HIGHLIGHT_CONNECTED_TIP = LangUtil.translate("Click on a component to highlight area directly or indirectly connected to the area below the cursor. To continue editing, this mode needs to be switched OFF");

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(StatusBar.class);

  public static String UPDATE_URL = "http://diy-fever.com/diylc/api/v1/update.xml";  
  private static final Format sizeFormat = new DecimalFormat("0.##");
  private static final int MAX_SELECTED_COMPONENTS = 15;

  private JComboBox<Double> zoomBox;
  private UpdateLabel updateLabel;
  private JLabel announcementLabel;
  private JLabel recentChangesLabel;
  private MemoryBar memoryPanel;
  private JLabel statusLabel;
  private JLabel positionLabel;
  private JLabel sizeLabel;  

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;

  // State variables
  private ComponentType componentSlot;
  private Point2D controlPointSlot;
  private Boolean forceInstatiate;
  private List<String> componentNamesUnderCursor;
  private List<String> selectedComponentNames;
  private List<String> stuckComponentNames;
  private String statusMessage;  

  private AnnouncementProvider announcementProvider;

  private Point2D mousePositionIn;

  private Point2D mousePositionMm;

  public StatusBar(ISwingUI swingUI) {
    super();
    this.swingUI = swingUI;

    this.announcementProvider = new AnnouncementProvider();

    setLayout(new GridBagLayout());

    try {
      swingUI.injectGUIComponent(this, SwingUtilities.BOTTOM, false, null);
    } catch (BadPositionException e) {
      LOG.error("Could not install status bar", e);
    }

    swingUI.executeBackgroundTask(new ITask<String>() {

      @Override
      public String doInBackground() throws Exception {
        Thread.sleep(1000);
        String announcements = announcementProvider.getCurrentAnnouncements(false);

        String update = getUpdateLabel().getUpdateChecker().findNewVersionShort();

        if (update != null) {          
          String updateHtml =
              "<font size='4'><b>" + NEW_VERSION_AVAILABLE + ":</b> " + update
                  + "</font><br>"
                      + CLICK_THE_LIGHTBULB;
          if (announcements == null || announcements.length() == 0)
            return "<html>" + updateHtml + "</html>";
          announcements = announcements.replace("<html>", "<html>" + updateHtml + "<br>");
        }

        return announcements;
      }

      @Override
      public void failed(Exception e) {
        LOG.error("Error while fetching announcements", e);
      }

      @Override
      public void complete(String result) {
        if (result != null && result.length() > 0) {
          new BalloonTip(getUpdateLabel(), result, new EdgedBalloonStyle(UIManager.getColor("ToolTip.background"),
              UIManager.getColor("ToolTip.foreground")), true);
          announcementProvider.dismissed();
        }
      }

    }, false);
    
    ConfigurationManager.getInstance().addConfigListener(IPlugInPort.METRIC_KEY, new IConfigListener() {
      
      @Override
      public void valueChanged(String key, Object value) {
        refreshPosition((Boolean)value);
      }
    });
  }

  private JComboBox<Double> getZoomBox() {
    if (zoomBox == null) {
      zoomBox = new JComboBox<Double>(plugInPort.getAvailableZoomLevels());
      zoomBox.setSelectedItem(plugInPort.getZoomLevel());
      zoomBox.setFocusable(false);
      zoomBox.setRenderer(new PercentageListCellRenderer());
      zoomBox.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          Double newZoom = (Double) zoomBox.getSelectedItem();
          if (!newZoom.equals(plugInPort.getZoomLevel()))
            plugInPort.setZoomLevel((Double) zoomBox.getSelectedItem());
        }
      });
    }
    return zoomBox;
  }

  private UpdateLabel getUpdateLabel() {
    if (updateLabel == null) {
      updateLabel = new UpdateLabel(plugInPort.getCurrentVersionNumber(), UPDATE_URL) {

        private static final long serialVersionUID = 1L;

        @Override
        public Point getToolTipLocation(MouseEvent event) {
          return new Point(0, -16);
        }
      };
      updateLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    }
    return updateLabel;
  }

  public JLabel getAnnouncementLabel() {
    if (announcementLabel == null) {
      announcementLabel = new JLabel(IconLoader.Megaphone.getIcon()) {

        private static final long serialVersionUID = 1L;

        @Override
        public Point getToolTipLocation(MouseEvent event) {
          return new Point(0, -16);
        }
      };
      announcementLabel.setToolTipText(CLICK_TO_FETCH_ANNOUNCEMENT);
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
              swingUI.showMessage(COULD_NOT_FETCH_ANNOUNCEMENTS, ERROR_TITLE, IView.ERROR_MESSAGE);
            }

            @Override
            public void complete(String result) {
              if (result != null && result.length() > 0) {
                swingUI.showMessage(result, PUBLIC_ANNOUNCEMENT, IView.INFORMATION_MESSAGE);
                announcementProvider.dismissed();
              } else
                swingUI.showMessage(NO_ANNOUNCEMENTS, PUBLIC_ANNOUNCEMENT,
                    IView.INFORMATION_MESSAGE);
            }

          }, true);
        }
      });
    }
    return announcementLabel;
  }
  
  public JLabel getRecentChangesLabel() {
    if (recentChangesLabel == null) {
      recentChangesLabel = new JLabel(IconLoader.ScrollInformation.getIcon()){

        private static final long serialVersionUID = 1L;

        @Override
        public Point getToolTipLocation(MouseEvent event) {
          return new Point(0, -16);
        }
      };
      recentChangesLabel.setToolTipText(CLICK_UPDATES);
      recentChangesLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      
      recentChangesLabel.addMouseListener(new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
          List<Version> updates = plugInPort.getRecentUpdates();
          if (updates == null)
            swingUI.showMessage(NO_HISTORY, INFORMATION, IView.INFORMATION_MESSAGE);
          else {
            String html = UpdateChecker.createUpdateHTML(updates);
            UpdateDialog updateDialog = new UpdateDialog(swingUI.getOwnerFrame().getRootPane(), html, (String)null);
            updateDialog.setVisible(true);
          }
        }
      });
    }
    return recentChangesLabel;
  }

  private MemoryBar getMemoryPanel() {
    if (memoryPanel == null) {
      memoryPanel = new MemoryBar(true) {

        private static final long serialVersionUID = 1L;

        @Override
        public Point getToolTipLocation(MouseEvent event) {
          return new Point(0, -52);
        }
      };
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
  
  private JLabel getPositionLabel() {
    if (positionLabel == null) {
      positionLabel = new JLabel();
      positionLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
    }
    return positionLabel;
  }

  public JLabel getSizeLabel() {
    if (sizeLabel == null) {
      sizeLabel = new JLabel(IconLoader.Size.getIcon()) {

        private static final long serialVersionUID = 1L;

        @Override
        public Point getToolTipLocation(MouseEvent event) {
          return new Point(0, -16);
        }
      };
      sizeLabel.setFocusable(true);
      sizeLabel.setToolTipText("Click to calculate selection size");
      sizeLabel.addMouseListener(new MouseAdapter() {

        @Override
        public void mouseClicked(MouseEvent e) {
          Point2D[] sizes = plugInPort.calculateSelectionDimension();
          String text;
          if (sizes == null) {
            text = SELECTION_IS_EMPTY;
          } else {
            text =
                sizeFormat.format(sizes[0].getX()) + " x " + sizeFormat.format(sizes[0].getY()) + " in\n"
                    + sizeFormat.format(sizes[1].getX()) + " x " + sizeFormat.format(sizes[1].getY()) + " cm";
          }
          JOptionPane.showMessageDialog(SwingUtilities.getRootPane(StatusBar.this), text, SELECTION_SIZE,
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
    
    gbc.gridx++;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 0;
    add(getPositionLabel(), gbc);

    JPanel zoomPanel = new JPanel(new BorderLayout());
    zoomPanel.add(new JLabel(ZOOM + ": "), BorderLayout.WEST);
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
    gbc.insets = new Insets(0, 0, 0, 4);
    add(getRecentChangesLabel(), gbc);

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
        EventType.SELECTION_CHANGED, EventType.STATUS_MESSAGE_CHANGED, EventType.MOUSE_MOVED);
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
        Collection<IDIYComponent<?>> selection = (Collection<IDIYComponent<?>>) params[0];
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
        controlPointSlot = (Point2D) params[1];
        forceInstatiate = params.length > 2 ? (Boolean)params[2] : null;
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
        statusMessage = params.length > 0 ? (String) params[0] : null;
        refreshStatusText();
        break;
      case MOUSE_MOVED:
        mousePositionIn = (Point2D) params[1];
        mousePositionMm = (Point2D) params[2];        
        refreshPosition(ConfigurationManager.getInstance().readBoolean(Presenter.METRIC_KEY, true));
        break;
    }
  }

  private void refreshPosition(boolean metric) {
    Point2D mousePosition;
    if (metric)
      mousePosition = mousePositionMm;
    else 
      mousePosition = mousePositionIn;
    
    String unit = metric ? "mm" : "in";
    
    if (mousePosition == null)
      getPositionLabel().setText(null);
    else
      getPositionLabel().setText(String.format("x:%.2f%s y:%.2f%s", mousePosition.getX(), unit, mousePosition.getY(), unit));
  }

  private void refreshStatusText() {    
    String statusText = this.statusMessage;
    if (componentSlot == null) {
      if (componentNamesUnderCursor != null && !componentNamesUnderCursor.isEmpty()) {
        String formattedNames = Utils.toCommaString(componentNamesUnderCursor);
        statusText = "<html>" + DRAG_POINTS_OF + " " + formattedNames + "</html>";
      } else if (selectedComponentNames != null && !selectedComponentNames.isEmpty()) {
        StringBuilder builder = new StringBuilder();
        builder.append(Utils.toCommaString(selectedComponentNames.subList(0,
            Math.min(MAX_SELECTED_COMPONENTS, selectedComponentNames.size()))));
        if (selectedComponentNames.size() > MAX_SELECTED_COMPONENTS) {
          builder.append(" and " + (selectedComponentNames.size() - MAX_SELECTED_COMPONENTS) + " more");
        }
        if (!stuckComponentNames.isEmpty()) {
          builder.append(" (" + String.format(HOLD_TO_UNSTUCK, "<b>Ctrl</b>") + " ");
          builder.append(Utils.toCommaString(stuckComponentNames.subList(0, Math.min(5, stuckComponentNames.size()))));
          if (stuckComponentNames.size() > 5) {
            builder.append(" and " + (stuckComponentNames.size() - 5) + " more");
          }
          builder.append(")");
        }
        statusText = "<html>" + SELECTION + ": " + builder.toString() + "</html>";
      }
    } else {
      if (forceInstatiate != null && forceInstatiate)
        statusText =  "<html>" + DRAG_TO_PLACE_A_NEW + " <font color='blue'>" + componentSlot.getName()
            + "</font></html>";
      else
      switch (componentSlot.getCreationMethod()) {
        case POINT_BY_POINT:
          String count;
          if (controlPointSlot == null) {
            count = "first";
          } else {
            count = "second";
          }
          statusText =
              "<html>" + String.format(CLICK_TO_SET_POINT, count, "<font color='blue'>"
                  + componentSlot.getName() + "</font>", "<b>Esc</b>") + "</html>";
          break;
        case SINGLE_CLICK:
          statusText =
              "<html>" + String.format(CLICK_TO_CREATE, "<font color='blue'>" + componentSlot.getName()
              + "</font>", "<b>Esc</b>") + "</html>";
          break;
      }
    }
    
    // override any other status with this when in highlight mode, as we cannot do anything else
    if (plugInPort.getOperationMode() == OperationMode.HIGHLIGHT_CONNECTED_AREAS)
      statusText = HIGHLIGHT_CONNECTED_TIP;
    
    final String finalStatus = statusText;
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        getStatusLabel().setText(finalStatus);
      }
    });
  }
}
