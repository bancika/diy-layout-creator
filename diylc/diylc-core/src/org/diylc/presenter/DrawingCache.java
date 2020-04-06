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
 */
package org.diylc.presenter;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.diylc.common.ComponentType;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.utils.Pair;

/**
 * A layer that injects itself between the provided {@link G2DWrapper} instance and the underlying
 * {@link Graphics2D} instance. For all components that are enabled for caching, it stores a
 * {@link BufferedImage} representing the component. The state of the component is recorded and
 * compared next time it needs to be rendered to make sure that the cache is up to date.
 * 
 * @author bancika
 */
public class DrawingCache {

  private Map<IDIYComponent<?>, CacheValue> imageCache = new HashMap<IDIYComponent<?>, CacheValue>();
  
  private Map<String, Pair<Counter, Counter>> renderStatsByType = new HashMap<String, Pair<Counter, Counter>>();

  private static final Logger LOG = Logger.getLogger(DrawingCache.class);

  public static DrawingCache Instance = new DrawingCache();

  @SuppressWarnings("unchecked")
  public void draw(IDIYComponent<?> component, G2DWrapper g2d, ComponentState componentState,
      boolean outlineMode, Project project, double zoom, boolean trackArea) {
    ComponentType type = ComponentProcessor.getInstance()
        .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) component.getClass());

    if (type.getEnableCache()) {
      // if we need to apply caching
      Point firstPoint = component.getControlPoint(0);

      // cache hit!
      CacheValue value = null;
      if (imageCache.containsKey(component))
        value = imageCache.get(component);

      // only honor the cache if the component hasn't changed in the meantime
      if (value == null || !value.getComponent().equalsTo(component)
          || value.getState() != componentState || value.getZoom() != zoom) {
        LOG.trace("Rendering " + component.getName() + " for cache.");
        Rectangle2D rect = component.getCachingBounds();
        int width = (int)Math.round(rect.getWidth() * zoom);
        int height = (int)Math.round(rect.getHeight() * zoom);
        // calculate the position of the first point relative to the caching bounds
        int dx = (int) (firstPoint.x - rect.getX());
        int dy = (int) (firstPoint.y - rect.getY());
        
        // create image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        // create graphics
        Graphics2D cg2d = image.createGraphics();

        // copy over rendering settings so our cached image has the same features
        cg2d.setRenderingHints(g2d.getRenderingHints());

        // set clip bounds to the whole image
        cg2d.setClip(0, 0, width, height);
        
        // apply zoom
        if (Math.abs(1.0 - zoom) > 1e-4) {
          cg2d.scale(zoom, zoom);
        }
        // translate to make the top-left corner of the component match (0, 0)
        cg2d.translate(-firstPoint.x + dx, -firstPoint.y + dy);
        
        // wrap the graphics so we can track
        G2DWrapper wrapper = new G2DWrapper(cg2d, zoom);

        // initialize wrapper
        wrapper.startedDrawingComponent();
        if (!trackArea) {
          wrapper.stopTracking();
        }
        
        long componentStart = System.nanoTime();

        // now draw the component to the buffer image
        component.draw(wrapper, componentState, outlineMode, project, wrapper);
        
        long componentEnd = System.nanoTime();
        
        Pair<Counter, Counter> stats;
        String key = component.getClass().getCanonicalName().replace("org.diylc.components.", "");
        if (renderStatsByType.containsKey(key)) {
          stats = renderStatsByType.get(key);
        } else {
          stats = new Pair<Counter, Counter>(new Counter(), new Counter());
          renderStatsByType.put(key, stats);
        }
        stats.getFirst().add(componentEnd - componentStart);

        // finalize wrapper
        wrapper.finishedDrawingComponent();

        cg2d.dispose();
        
        // output cached drawings to separate files 
//        File outputfile = new File("d:\\tmp\\image_" + component.getName() + ".png");
//        try {
//          ImageIO.write(image, "png", outputfile);
//        } catch (IOException e) {
//          // TODO Auto-generated catch block
//          e.printStackTrace();
//        }
        
        // add to the cache        
        value = new CacheValue(component, image, wrapper.getCurrentArea(), wrapper.getContinuityPositiveAreas(),
            wrapper.getContinuityNegativeAreas(), componentState, zoom, dx, dy);        
        imageCache.put(component, value);
      }
      
      long componentStart = System.nanoTime();

      // temporarily scale the graphic to compensate for the zoom, since we are rendering an image with the zoom appled
      if (Math.abs(1.0 - zoom) > 1e-4) {
        g2d.getCanvasGraphics().scale(1 / zoom, 1 / zoom);
      }
      try {
        // draw cached image
        g2d.getCanvasGraphics().drawImage(value.getImage(), (int) ((firstPoint.x - value.getDx()) * zoom),
            (int) ((firstPoint.y - value.getDy())* zoom), null);
      } finally {
        if (Math.abs(1.0 - zoom) > 1e-4) {
          g2d.getCanvasGraphics().scale(zoom, zoom);
        }
      }
      
      long componentEnd = System.nanoTime();
      
      Pair<Counter, Counter> stats;
      String key = component.getClass().getCanonicalName().replace("org.diylc.components.", "");
      if (renderStatsByType.containsKey(key)) {
        stats = renderStatsByType.get(key);
      } else {
        stats = new Pair<Counter, Counter>(new Counter(), new Counter());
        renderStatsByType.put(key, stats);
      }
      stats.getSecond().add(componentEnd - componentStart);

      // copy over tracking area from the cache
      g2d.merge(value.getCurrentArea(), value.getContinuityPositiveAreas(), value.getContinuityNegativeAreas());
    } else {
      // no caching, just draw as usual
      component.draw(g2d, componentState, outlineMode, project, g2d);
    }
  }
  
  public void logStats() {
    int totalSizeMB = imageCache.values().stream().map(x -> x.image.getData().getDataBuffer().getSize()).reduce(0, Integer::sum) * 4 / 1024 / 1024; // 4 bytes per pixel
    LOG.debug(String.format("Render cache contains %d elements, approx size is %d MB", imageCache.size(), totalSizeMB));
    
    String mapAsString = renderStatsByType.entrySet().stream().sorted((e1, e2) -> -Long.compare(e1.getValue().getFirst().getNanoTime() + e1.getValue().getSecond().getNanoTime(), 
        e2.getValue().getFirst().getNanoTime() + e2.getValue().getSecond().getNanoTime()))
        .map(e -> e.getKey() + ": render: " + e.getValue().getFirst() + ", paste: " + e.getValue().getSecond())
        .collect(Collectors.joining("; ", "{", "}"));
    
    LOG.debug("Cache render distribution stats: " + mapAsString);
  }

  public void clear() {
    imageCache.clear();
  }

  private class CacheValue {
    IDIYComponent<?> component;
    BufferedImage image;
    Area currentArea;
    Map<String, Area> continuityPositiveAreas;
    Map<String, Area> continuityNegativeAreas;
    ComponentState state;
    double zoom;
    int dx;
    int dy;

    public CacheValue(IDIYComponent<?> component, BufferedImage image, Area currentArea,
        Map<String, Area> continuityPositiveAreas,  Map<String, Area> continuityNegativeAreas,
        ComponentState state, double zoom, int dx, int dy) {
      super();
      // take a copy of the component, so we can check if it changed in the meantime
      try {
        this.component = component.clone();
      } catch (CloneNotSupportedException e) {
      }
      this.image = image;
      this.currentArea = currentArea;
      this.continuityPositiveAreas = continuityPositiveAreas;
      this.continuityNegativeAreas = continuityNegativeAreas;
      this.state = state;
      this.zoom = zoom;
      this.dx = dx;
      this.dy = dy;
    }

    public IDIYComponent<?> getComponent() {
      return component;
    }

    public BufferedImage getImage() {
      return image;
    }

    public Area getCurrentArea() {
      return currentArea;
    }
    
    public Map<String, Area> getContinuityPositiveAreas() {
      return continuityPositiveAreas;
    }
    
    public Map<String, Area> getContinuityNegativeAreas() {
      return continuityNegativeAreas;
    }

    public ComponentState getState() {
      return state;
    }
    
    public double getZoom() {
      return zoom;
    }
    
    public int getDx() {
      return dx;
    }
    
    public int getDy() {
      return dy;
    }

    @Override
    public String toString() {
      return component.getName() + ":" + state + ":" + zoom;
    }
  }
}
