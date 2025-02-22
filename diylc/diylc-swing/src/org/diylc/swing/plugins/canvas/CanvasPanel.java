/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
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
 * 
 */
package org.diylc.swing.plugins.canvas;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.image.VolatileImage;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.IConfigurationManager;
import org.diylc.common.ComponentType;
import org.diylc.common.DrawOption;
import org.diylc.common.IBlockProcessor.InvalidBlockException;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.plugins.tree.TreePanel;

/**
 * GUI class used to draw onto.
 * 
 * @author Branislav Stojkovic
 */
public class CanvasPanel extends JComponent implements Autoscroll {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(CanvasPlugin.class);

  public static boolean RENDER_VISIBLE_RECT_ONLY = true;

  private IPlugInPort plugInPort;

  private Image bufferImage;
  private GraphicsConfiguration screenGraphicsConfiguration;

  public boolean useHardwareAcceleration = false;

  // static final EnumSet<DrawOption> DRAW_OPTIONS =
  // EnumSet.of(DrawOption.GRID,
  // DrawOption.SELECTION, DrawOption.ZOOM, DrawOption.CONTROL_POINTS);
  // static final EnumSet<DrawOption> DRAW_OPTIONS_ANTI_ALIASING =
  // EnumSet.of(DrawOption.GRID,
  // DrawOption.SELECTION, DrawOption.ZOOM, DrawOption.ANTIALIASING,
  // DrawOption.CONTROL_POINTS);

  private HashMap<String, ComponentType> componentTypeCache;

  private IConfigurationManager<?> configManager;
  
  private boolean clickInProgress = false;

  public CanvasPanel(IPlugInPort plugInPort, IConfigurationManager<?> configManager) {
    this.plugInPort = plugInPort;
    this.configManager = configManager;

//    this.useHardwareAcceleration =
//        configManager.readBoolean(IPlugInPort.HARDWARE_ACCELERATION, false);
    setFocusable(true);
    initializeListeners();
    initializeDnD();
    GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] devices = graphicsEnvironment.getScreenDevices();
    screenGraphicsConfiguration = devices[0].getDefaultConfiguration();

    initializeActions();
  }

  public void invalidateCache() {
    bufferImage = null;
  }

  public HashMap<String, ComponentType> getComponentTypeCache() {
    if (componentTypeCache == null) {
      componentTypeCache = new HashMap<String, ComponentType>();
      for (Entry<String, List<ComponentType>> entry : this.plugInPort.getComponentTypes()
          .entrySet()) {
        for (ComponentType type : entry.getValue())
          componentTypeCache.put(type.getInstanceClass().getCanonicalName(), type);
      }
    }
    return componentTypeCache;
  }
  
  public void setClickInProgress(boolean clickInProgress) {
    this.clickInProgress = clickInProgress;
  }

  public boolean isClickInProgress() {
    return clickInProgress;
  }

  private void initializeDnD() {
    // Initialize drag source recognizer.
    DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this,
        DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK,
        new CanvasDragGestureListener(plugInPort, this));
    // Initialize drop target.
    new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new CanvasTargetListener(plugInPort),
        true);
  }

  private void initializeActions() {
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_QUOTE, 0), "repeatLast");

    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSlot");

    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.CTRL_DOWN_MASK), "zoomIn");
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.CTRL_DOWN_MASK), "zoomIn");
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.META_DOWN_MASK), "zoomIn");
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
      .put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.CTRL_DOWN_MASK), "zoomIn");
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.META_DOWN_MASK), "zoomIn");
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, KeyEvent.META_DOWN_MASK), "zoomIn");

    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, KeyEvent.CTRL_DOWN_MASK), "zoomOut");
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.CTRL_DOWN_MASK), "zoomOut");
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, KeyEvent.META_DOWN_MASK), "zoomOut");
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, KeyEvent.META_DOWN_MASK), "zoomOut");
    
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.CTRL_DOWN_MASK), "resetZoom");
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, KeyEvent.CTRL_DOWN_MASK), "resetZoom");
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_0, KeyEvent.META_DOWN_MASK), "resetZoom");
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, KeyEvent.META_DOWN_MASK), "resetZoom");

    for (int i = 1; i <= 12; i++) {
      final int x = i;
      getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
          .put(KeyStroke.getKeyStroke(KeyEvent.VK_F1 + i - 1, 0), "functionKey" + i);
      getActionMap().put("functionKey" + i, new AbstractAction() {

        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          functionKeyPressed(x);
        }
      });
    }

//    getActionMap().put("zoomIn", new AbstractAction() {
//
//      private static final long serialVersionUID = 1L;
//
//      @Override
//      public void actionPerformed(ActionEvent e) {
//        LOG.debug("Keyboard zoom-in triggered");
//        double oldZoom = CanvasPanel.this.plugInPort.getZoomLevel();
//        Double[] availableZoomLevels = CanvasPanel.this.plugInPort.getAvailableZoomLevels();
//        int index = Arrays.binarySearch(availableZoomLevels, oldZoom);
//        if (index < availableZoomLevels.length - 1)
//          CanvasPanel.this.plugInPort.setZoomLevel(availableZoomLevels[index + 1]);
//      }
//    });
//
//    getActionMap().put("zoomOut", new AbstractAction() {
//
//      private static final long serialVersionUID = 1L;
//
//      @Override
//      public void actionPerformed(ActionEvent e) {
//        LOG.debug("Keyboard zoom-out triggered");
//        double oldZoom = CanvasPanel.this.plugInPort.getZoomLevel();
//        Double[] availableZoomLevels = CanvasPanel.this.plugInPort.getAvailableZoomLevels();
//        int index = Arrays.binarySearch(availableZoomLevels, oldZoom);
//        if (index > 0)
//          CanvasPanel.this.plugInPort.setZoomLevel(availableZoomLevels[index - 1]);
//      }
//    });

    getActionMap().put("resetZoom", new AbstractAction() {

      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        LOG.debug("Keyboard reset zoom triggered");
        CanvasPanel.this.plugInPort.setZoomLevel(1d);
      }
    });

    getActionMap().put("clearSlot", new AbstractAction() {

      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        CanvasPanel.this.plugInPort.setNewComponentTypeSlot(null, null, null, false);
      }
    });

    getActionMap().put("repeatLast", new AbstractAction() {

      private static final long serialVersionUID = 1L;

      @SuppressWarnings("unchecked")
      @Override
      public void actionPerformed(ActionEvent e) {
        List<String> recent =
            (List<String>) configManager.readObject(IPlugInPort.RECENT_COMPONENTS_KEY, null);
        if (recent != null && !recent.isEmpty()) {
          String clazz = recent.get(0);
          Map<String, List<ComponentType>> componentTypes =
              CanvasPanel.this.plugInPort.getComponentTypes();
          for (Map.Entry<String, List<ComponentType>> entry : componentTypes.entrySet()) {
            for (ComponentType type : entry.getValue()) {
              if (type.getInstanceClass().getCanonicalName().equals(clazz)) {
                CanvasPanel.this.plugInPort.setNewComponentTypeSlot(type, null, null, false);
                // hack: fake mouse movement to repaint
                CanvasPanel.this.plugInPort.mouseMoved(getMousePosition(), false, false, false);
                return;
              }
            }
          }
        }
      }
    });
  }

  @SuppressWarnings("unchecked")
  protected void functionKeyPressed(int i) {
    HashMap<String, String> shortcutMap =
        (HashMap<String, String>) configManager.readObject(TreePanel.COMPONENT_SHORTCUT_KEY, null);
    if (shortcutMap == null)
      return;
    String typeName = shortcutMap.get("F" + i);
    if (typeName == null)
      return;
    if (typeName.startsWith("block:")) {
      String blockName = typeName.substring(6);
      try {
        plugInPort.loadBlock(blockName);
      } catch (InvalidBlockException e) {
        LOG.error("Could not find block assigned to shortcut: " + blockName);
      }
    } else {
      ComponentType type = getComponentTypeCache().get(typeName);
      if (type == null) {
        LOG.error("Could not find type: " + typeName);
        return;
      }
      this.plugInPort.setNewComponentTypeSlot(type, null, null, false);
    }

    // hack: fake mouse movement to repaint
    this.plugInPort.mouseMoved(getMousePosition(), false, false, false);
  }

  protected void createBufferImage() {
    int imageWidth;
    int imageHeight;
    if (RENDER_VISIBLE_RECT_ONLY) {
      Rectangle visibleRect = getVisibleRect();
      imageWidth = visibleRect.width;
      imageHeight = visibleRect.height;
    } else {
      imageWidth = getWidth();
      imageHeight = getHeight();
    }

    if (useHardwareAcceleration) {
      bufferImage =
          screenGraphicsConfiguration.createCompatibleVolatileImage(imageWidth, imageHeight);
      ((VolatileImage) bufferImage).validate(screenGraphicsConfiguration);
    } else {
      bufferImage = createImage(imageWidth, imageHeight);
    }
  }

  @Override
  public void paint(Graphics g) {
    if (plugInPort == null) {
      return;
    }
    if (bufferImage == null) {
      createBufferImage();
    }
    Graphics2D g2d = (Graphics2D) bufferImage.getGraphics();

    Rectangle visibleRect = getVisibleRect();

    int x = 0;
    int y = 0;

    if (RENDER_VISIBLE_RECT_ONLY) {
      x = visibleRect.x;
      y = visibleRect.y;
      g2d.translate(-x, -y);
    } else {
      g2d.setClip(visibleRect);
    }


    Set<DrawOption> drawOptions =
        EnumSet.of(DrawOption.SELECTION, DrawOption.ZOOM, DrawOption.CONTROL_POINTS);

    if (configManager.readBoolean(IPlugInPort.ANTI_ALIASING_KEY, true)) {
      drawOptions.add(DrawOption.ANTIALIASING);
    }
    if (configManager.readBoolean(IPlugInPort.OUTLINE_KEY, false)) {
      drawOptions.add(DrawOption.OUTLINE_MODE);
    }
    if (configManager.readBoolean(IPlugInPort.SHOW_GRID_KEY, true)) {
      drawOptions.add(DrawOption.GRID);
    }
    if (configManager.readBoolean(IPlugInPort.EXTRA_SPACE_KEY, true)) {
      drawOptions.add(DrawOption.EXTRA_SPACE);
    }
    if (configManager.readBoolean(IPlugInPort.CACHING_ENABLED_KEY, true)) {
      drawOptions.add(DrawOption.ENABLE_CACHING);
    }
    if (configManager.readBoolean(IPlugInPort.LOCKED_ALPHA, true)) {
      drawOptions.add(DrawOption.LOCKED_ALPHA);
    }

    plugInPort.draw(g2d, drawOptions, null, null, visibleRect);

    if (useHardwareAcceleration) {
      VolatileImage volatileImage = (VolatileImage) bufferImage;
      do {
        try {
          if (volatileImage.contentsLost()) {
            createBufferImage();
          }
          // int validation =
          // volatileImage.validate(screenGraphicsConfiguration);
          // if (validation == VolatileImage.IMAGE_INCOMPATIBLE) {
          // createBufferImage();
          // }
          g.drawImage(bufferImage, x, y, this);
        } catch (NullPointerException e) {
          createBufferImage();
        }
      } while (volatileImage == null || volatileImage.contentsLost());
    } else {
      g.drawImage(bufferImage, x, y, this);
      // bufferImage.flush();
    }
    g2d.dispose();
  }

  @Override
  public void update(Graphics g) {
    paint(g);
  }

  private void initializeListeners() {
    addComponentListener(new ComponentAdapter() {

      @Override
      public void componentResized(ComponentEvent e) {
        invalidateCache();
        invalidate();
      }
    });
    // addKeyListener(new KeyAdapter() {
    //
    // @Override
    // public void keyPressed(KeyEvent e) {
    // if (e.getKeyCode() == KeyEvent.VK_DELETE) {
    // plugInPort.deleteSelectedComponents();
    // }
    // // plugInPort.mouseMoved(getMousePosition(), e.isControlDown(),
    // // e.isShiftDown(), e
    // // .isAltDown());
    // }
    // });
  }

  // Autoscroll

  private int autoScrollSize = 64;

  @Override
  public void autoscroll(Point cursorLocn) {
    scrollRectToVisible(new Rectangle(cursorLocn.x - autoScrollSize, cursorLocn.y - autoScrollSize,
        autoScrollSize * 2, autoScrollSize * 2));
  }

  @Override
  public Insets getAutoscrollInsets() {
    Rectangle rect = getVisibleRect();
    return new Insets(rect.y - autoScrollSize, rect.x - autoScrollSize,
        rect.y + rect.height + autoScrollSize, rect.x + rect.width + autoScrollSize);
  }

  public void setUseHardwareAcceleration(boolean useHardwareAcceleration) {
//    this.useHardwareAcceleration = useHardwareAcceleration;
//    bufferImage = null;
  }
}
