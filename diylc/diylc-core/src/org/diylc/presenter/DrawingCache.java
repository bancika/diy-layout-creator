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
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.diylc.common.ComponentType;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;

/**
 * A layer that injects itself between the provided {@link G2DWrapper} instance and the underlying {@link Graphics2D} instance.
 * For all components that are enabled for caching, it stores a {@link BufferedImage} representing the component.
 * The state of the component is recorded and compared next time it needs to be rendered to make sure that the cache is up to date.
 * 
 * @author bancika
 */
public class DrawingCache {

  private Map<IDIYComponent<?>, CacheValue> imageCache = new HashMap<IDIYComponent<?>, CacheValue>();
  
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
      Point lastPoint = component.getControlPoint(component.getControlPointCount() - 1);

      // cache hit!
      CacheValue value = null;
      if (imageCache.containsKey(component))
        value = imageCache.get(component);
      
      // only honor the cache if the component hasn't changed in the meantime
      if (value == null || !value.getComponent().equalsTo(component) || value.getState() != componentState) {        
        LOG.trace("Rendering " + component.getName() + " for cache.");
        int width = (int) (Math.abs(firstPoint.x - lastPoint.x) * zoom) + 2;
        int height = (int) (Math.abs(firstPoint.y - lastPoint.y) * zoom) + 2;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);        
        Graphics2D cg2d = image.createGraphics();

        // copy over rendering settings 
        cg2d.setRenderingHints(g2d.getRenderingHints());
        
        cg2d.setClip(0, 0, width, height);
        
        // translate to make the top-left corner of the component match (0, 0)
        cg2d.translate(-firstPoint.x, -firstPoint.y);
        G2DWrapper wrapper = new G2DWrapper(cg2d, zoom);
        
        wrapper.startedDrawingComponent();
        if (!trackArea) {
          wrapper.stopTracking();
        }
                
        // now draw the component to the buffer image
        component.draw(wrapper, componentState, outlineMode, project, wrapper);
        
        wrapper.finishedDrawingComponent();
                
        cg2d.dispose();                
        
        value = new CacheValue(component, image, wrapper, componentState);
        imageCache.put(component, value);
      }
      
      // draw cached image
      g2d.getCanvasGraphics().drawImage(value.getImage(), firstPoint.x, firstPoint.y, null);
      
      // copy over tracking area from the cache
      g2d.merge(value.getWrapper());
    } else {
      // no caching, just draw as usual
      component.draw(g2d, componentState, outlineMode, project, g2d);
    }
  }  
  
  private class CacheValue {
    IDIYComponent<?> component;
    BufferedImage image;
    G2DWrapper wrapper;       
    ComponentState state;
    
    public CacheValue(IDIYComponent<?> component, BufferedImage image, G2DWrapper wrapper, ComponentState state) {
      super();
      // take a copy of the component, so we can check if it changed in the meantime
      try {
        this.component = component.clone();
      } catch (CloneNotSupportedException e) {
      }
      this.image = image;
      this.wrapper = wrapper;
      this.state = state;
    }
    
    public IDIYComponent<?> getComponent() {
      return component;
    }

    public BufferedImage getImage() {
      return image;
    }
    
    public G2DWrapper getWrapper() {
      return wrapper;
    }
    
    public ComponentState getState() {
      return state;
    }
    
    @Override
    public String toString() {      
      return component.getName() + ":" + state;
    }
  }
}
