/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
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
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;

import org.diylc.common.ComponentType;
import org.diylc.common.DrawOption;
import org.diylc.core.ComponentForRender;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.utils.Pair;
import org.diylc.utils.ScaledBufferedImage;

/**
 * A layer that injects itself between the provided {@link G2DWrapper} instance and the underlying
 * {@link Graphics2D} instance. For all components that are enabled for caching, it stores a
 * {@link BufferedImage} representing the component. The cache does not detect changes to the
 * component itself: whoever changes a component must {@link #invalidate(IDIYComponent)} it. Zoom,
 * component state and draw options are recorded and compared each time.
 * 
 * @author bancika
 */
public class DrawingCache {

  // access-ordered, so iteration starts from the least recently used entry; guarded by itself
  // because bulkPrepare renders from multiple threads
  private final Map<IDIYComponent<?>, CacheValue> imageCache =
      new LinkedHashMap<IDIYComponent<?>, CacheValue>(16, 0.75f, true);
  private long imageCacheBytes = 0;

  // lookups made by bulkPrepare; outdated entries are re-rendered regardless of the budget, while
  // missing ones may have been evicted
  private final AtomicLong cacheHits = new AtomicLong();
  private final AtomicLong cacheMissesNotCached = new AtomicLong();
  private final AtomicLong cacheMissesOutdated = new AtomicLong();
  // entries that bulkPrepare rendered but were evicted again before the same repaint drew them,
  // a sign that the visible components alone exceed the budget
  private final AtomicLong rerenderedWhilePainting = new AtomicLong();

  private final Map<String, Pair<Counter, Counter>> renderStatsByType =
      new ConcurrentHashMap<String, Pair<Counter, Counter>>();

  // when set, each entry keeps a copy of its component and every cache hit is checked against it,
  // to find code paths that change a component without invalidating it
  private final boolean verifyCache = "true".equalsIgnoreCase(System.getProperty(VERIFY_CACHE));

  private static final Logger LOG = Logger.getLogger(DrawingCache.class);

  // evicted images are released straight away rather than lingering behind soft references until
  // the heap is nearly full, which lets G1 reclaim the large pixel arrays without a full GC
  private static final long MAX_CACHE_BYTES =
      Math.min(Runtime.getRuntime().maxMemory() / 4, 768L * 1024 * 1024);

  // 32 MB per image; a large board at high zoom on a HiDPI screen would otherwise take a big share
  // of the budget on its own
  private static final long MAX_CACHED_IMAGE_PIXELS = 8L * 1024 * 1024;

  public static String VERIFY_CACHE = "org.diylc.verifyDrawingCache";

  public static DrawingCache Instance = new DrawingCache();
  
  /**
   * Prepares cached renderings of those components from the specified map that have {@link ComponentDescriptor#enableCache()} set to true.
   * 
   * @param components
   * @param g2d
   * @param outlineMode
   * @param project
   * @param zoom
   */
  @SuppressWarnings("unchecked")
  public void bulkPrepare(Collection<ComponentForRender> components, G2DWrapper g2d, boolean outlineMode, Project project, 
      double zoom, double scaleFactor, Set<DrawOption> drawOptions) {
    // run all renders in parallel to utilize all CPU cores
    components.stream().parallel().forEach(x -> {      
      ComponentType type = ComponentProcessor.getInstance()
          .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) x.getComponent().getClass());

      if (type != null && type.getEnableCache()
          && shouldCache(x.getComponent(), x.getState(), zoom, scaleFactor)) {
        renderAndCache(x.getComponent(), g2d, x.getState(), outlineMode, project, zoom, scaleFactor,
            x.getZOrder(), drawOptions, true);
      }
    });
  }

  /**
   * Draws a component onto the specified {@link G2DWrapper}. If the component has {@link ComponentDescriptor#enableCache()} set to true,
   * the drawing will come from the cache. Otherwise, it will be drawn as usual.
   * 
   * @param component
   * @param g2d
   * @param componentState
   * @param outlineMode
   * @param project
   * @param zoom
   */
  @SuppressWarnings("unchecked")
  public void draw(IDIYComponent<?> component, G2DWrapper g2d, ComponentState componentState,
      boolean outlineMode, Project project, double zoom, double scaleFactor, int zOrder, Rectangle2D visibleRect, Set<DrawOption> drawOptions) {
    ComponentType type = ComponentProcessor.getInstance()
        .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) component.getClass());

    CacheValue value = null;
    if (type != null && type.getEnableCache()
        && shouldCache(component, componentState, zoom, scaleFactor)) {
      value = renderAndCache(component, g2d, componentState, outlineMode, project, zoom,
          scaleFactor, zOrder, drawOptions, false);
    }

    if (value != null) {
      Point2D firstPoint = component.getControlPoint(0);
      
      long componentStart = System.nanoTime();

      // only render the cached image if it's within the clipping region
      if (visibleRect == null || visibleRect.intersects(value.getCacheBounds())) {
        // temporarily scale the graphic to compensate for the zoom, since we are rendering an image with the zoom appled
        if (Math.abs(1.0 - zoom) > 1e-4) {
          g2d.getCanvasGraphics().scale(1 / zoom, 1 / zoom);
        }
        try {
          // draw cached image
          g2d.scale(1 / scaleFactor, 1 / scaleFactor);
          g2d.getCanvasGraphics().drawImage(value.getImage(), (int) ((firstPoint.getX() - value.getDx()) * zoom * scaleFactor),
              (int) ((firstPoint.getY() - value.getDy())* zoom * scaleFactor), null);
        } finally {
          if (Math.abs(1.0 - zoom) > 1e-4) {
            g2d.getCanvasGraphics().scale(zoom, zoom);
          }
        }
      }
      
      long componentEnd = System.nanoTime();

      getRenderStats(component).getSecond().add(componentEnd - componentStart);

      // copy over tracking area from the cache, regardless if we rendered or not
      g2d.merge(value.getOutlineParts(), value.getContinuityPositiveParts(),
          value.getContinuityNegativeParts());
      g2d.mergeDrawnBounds(value.getCacheBounds());
    } else {
      // either not cached, or nothing was rendered because the component is outside the clip;
      // drawing it directly still records its areas, which the netlist and continuity checks read,
      // while Java2D discards the pixels
      component.draw(g2d, componentState, outlineMode, project, g2d);
    }
    g2d.stopTracking();
    g2d.stopTrackingContinuityArea();
  }
     
  public void logStats() {
    synchronized (imageCache) {
      LOG.debug(String.format("Render cache contains %d elements, approx size is %d MB of %d MB",
          imageCache.size(), imageCacheBytes / 1024 / 1024, MAX_CACHE_BYTES / 1024 / 1024));
    }

    long hits = cacheHits.get();
    long missesNotCached = cacheMissesNotCached.get();
    long missesOutdated = cacheMissesOutdated.get();
    long lookups = hits + missesNotCached + missesOutdated;
    LOG.debug(String.format("Render cache hits: %d, misses: %d not cached + %d outdated, "
        + "hit rate: %.1f%%, re-rendered while painting: %d",
        hits, missesNotCached, missesOutdated, lookups == 0 ? 0d : 100d * hits / lookups,
        rerenderedWhilePainting.get()));

    String mapAsString = renderStatsByType.entrySet().stream().sorted((e1, e2) -> -Long.compare(e1.getValue().getFirst().getNanoTime() + e1.getValue().getSecond().getNanoTime(), 
        e2.getValue().getFirst().getNanoTime() + e2.getValue().getSecond().getNanoTime()))
        .map(e -> e.getKey() + ": render: " + e.getValue().getFirst() + ", paste: " + e.getValue().getSecond())
        .collect(Collectors.joining("; ", "{", "}"));
    
    LOG.debug("Cache render distribution stats: " + mapAsString);
  }

  public void clear() {
    synchronized (imageCache) {
      for (CacheValue value : imageCache.values()) {
        value.getImage().flush();
      }
      imageCache.clear();
      imageCacheBytes = 0;
    }
  }

  public void invalidate(Collection<IDIYComponent<?>> components) {
    synchronized (imageCache) {
      for (IDIYComponent<?> component : components) {
        release(imageCache.remove(component));
      }
    }
  }

  public void invalidate(IDIYComponent<?> component) {
    synchronized (imageCache) {
      release(imageCache.remove(component));
    }
  }

  private CacheValue getCachedValue(IDIYComponent<?> component) {
    synchronized (imageCache) {
      return imageCache.get(component);
    }
  }

  private void putCachedValue(IDIYComponent<?> component, CacheValue value) {
    synchronized (imageCache) {
      release(imageCache.put(component, value));
      imageCacheBytes += value.getSizeInBytes();
      // the entry just added is the most recently used one, so it is never evicted here, even if
      // it alone exceeds the budget
      Iterator<CacheValue> iterator = imageCache.values().iterator();
      while (imageCacheBytes > MAX_CACHE_BYTES && imageCache.size() > 1) {
        CacheValue eldest = iterator.next();
        iterator.remove();
        release(eldest);
      }
    }
  }

  // must be called while holding the imageCache lock
  private void release(CacheValue value) {
    if (value == null) {
      return;
    }
    imageCacheBytes -= value.getSizeInBytes();
    // drops the accelerated (VRAM / native) copy Java2D keeps for images drawn repeatedly, which
    // would otherwise live until the image is finalized
    value.getImage().flush();
  }

  private boolean shouldCache(IDIYComponent<?> component, ComponentState componentState,
      double zoom, double scaleFactor) {
    // a dragged component changes on every frame, so its image would be discarded right after
    // rendering while still pushing useful entries out of the cache
    if (componentState == ComponentState.DRAGGING) {
      return false;
    }
    Rectangle2D rect = component.getCachingBounds();
    double pixelScale = zoom * scaleFactor;
    return rect.getWidth() * pixelScale * rect.getHeight() * pixelScale <= MAX_CACHED_IMAGE_PIXELS;
  }

  private Pair<Counter, Counter> getRenderStats(IDIYComponent<?> component) {
    String key = component.getClass().getCanonicalName().replace("org.diylc.components.", "");
    return renderStatsByType.computeIfAbsent(key,
        k -> new Pair<Counter, Counter>(new Counter(), new Counter()));
  }

  private CacheValue renderAndCache(IDIYComponent<?> component, G2DWrapper g2d, ComponentState componentState,
      boolean outlineMode, Project project, double zoom, double scaleFactor, int zOrder,
      Set<DrawOption> drawOptions, boolean preparing) {

    if (!g2d.getClipBounds().intersects(component.getCachingBounds())) {
      return null;
    }

    // if we need to apply caching
    Point2D firstPoint = component.getControlPoint(0);

    // cache hit!
    CacheValue value = getCachedValue(component);

    // Also check if drawOptions changed (e.g., MONOCHROMATIC affects rendering)
    boolean stale = value == null || value.getState() != componentState || value.getZoom() != zoom
        || value.getOutlineMode() != outlineMode || value.getScaleFactor() != scaleFactor
        || !value.getDrawOptions().equals(drawOptions);
    if (!stale && verifyCache && !value.getComponent().equalsTo(component)) {
      LOG.warn("Cached drawing of " + component.getName() + " of type "
          + component.getClass().getName() + " was not invalidated after the component changed");
      stale = true;
    }
    if (stale) {
      // figure out size and placement of buffer image
      Rectangle2D rect = component.getCachingBounds();
      int width = (int)Math.round(rect.getWidth() * zoom);
      int height = (int)Math.round(rect.getHeight() * zoom);
      // calculate the position of the first point relative to the caching bounds
      int dx = (int) (firstPoint.getX() - rect.getX());
      int dy = (int) (firstPoint.getY() - rect.getY());
      
      if (width <= 0 || height <= 0) {
        return null;
      }

      if (!preparing) {
        rerenderedWhilePainting.incrementAndGet();
      } else if (value == null) {
        cacheMissesNotCached.incrementAndGet();
      } else {
        cacheMissesOutdated.incrementAndGet();
      }
      
      // create image
      BufferedImage image = new ScaledBufferedImage(width, height, BufferedImage.TYPE_INT_ARGB, scaleFactor);
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
      cg2d.translate(-firstPoint.getX() + dx, -firstPoint.getY() + dy);
      
      // wrap the graphics so we can track
      G2DWrapper wrapper = new G2DWrapper(cg2d, zoom, drawOptions);

      // initialize wrapper
      wrapper.startedDrawingComponent(zOrder);
      
      long componentStart = System.nanoTime();

      // now draw the component to the buffer image
      component.draw(wrapper, componentState, outlineMode, project, wrapper);
      
      long componentEnd = System.nanoTime();

      getRenderStats(component).getFirst().add(componentEnd - componentStart);

      // finalize wrapper
      wrapper.finishedDrawingComponent();

      cg2d.dispose();
      
      // output cached drawings to separate files 
//      File outputfile = new File("d:\\tmp\\image_" + component.getName() + ".png");
//      try {
//        ImageIO.write(image, "png", outputfile);
//      } catch (IOException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//      }
      
      if (wrapper.getOutlineParts().isEmpty()) {
        LOG.warn("Empty outline area detected for " + component.getName());
      }
      
      // add to the cache        
      value = new CacheValue(component, image, wrapper.getOutlineParts(),
          wrapper.getContinuityPositiveParts(), wrapper.getContinuityNegativeParts(),
          componentState, outlineMode, zoom, scaleFactor, dx, dy, rect, drawOptions);
      putCachedValue(component, value);
    } else if (preparing) {
      cacheHits.incrementAndGet();
    }
    
    return value;
  }

  private class CacheValue {
    // only kept when verifyCache is set
    IDIYComponent<?> component;
    BufferedImage image;
    // the shapes recorded while rendering, handed to the canvas wrapper on every paste
    List<Shape> outlineParts;
    Map<String, List<Shape>> continuityPositiveParts;
    Map<String, List<Shape>> continuityNegativeParts;
    ComponentState state;
    boolean outlineMode;
    double zoom;
    double scaleFactor;
    int dx;
    int dy;
    Rectangle2D cacheBounds;
    Set<DrawOption> drawOptions;

    public CacheValue(IDIYComponent<?> component, BufferedImage image, List<Shape> outlineParts,
        Map<String, List<Shape>> continuityPositiveParts,
        Map<String, List<Shape>> continuityNegativeParts,
        ComponentState state, boolean outlineMode, double zoom, double scaleFactor, int dx, int dy,
        Rectangle2D cacheBounds, Set<DrawOption> drawOptions) {
      super();
      this.cacheBounds = cacheBounds;
      if (verifyCache) {
        try {
          this.component = component.clone();
        } catch (CloneNotSupportedException e) {
        }
      }
      this.image = image;
      this.outlineParts = outlineParts;
      this.continuityPositiveParts = continuityPositiveParts;
      this.continuityNegativeParts = continuityNegativeParts;
      this.state = state;
      this.outlineMode = outlineMode;
      this.zoom = zoom;
      this.dx = dx;
      this.dy = dy;
      this.scaleFactor = scaleFactor;
      this.drawOptions = drawOptions;
    }

    public IDIYComponent<?> getComponent() {
      return component;
    }

    public BufferedImage getImage() {
      return image;
    }

    public List<Shape> getOutlineParts() {
      return outlineParts;
    }
    
    public Map<String, List<Shape>> getContinuityPositiveParts() {
      return continuityPositiveParts;
    }
    
    public Map<String, List<Shape>> getContinuityNegativeParts() {
      return continuityNegativeParts;
    }

    public ComponentState getState() {
      return state;
    }
    
    public boolean getOutlineMode() {
      return outlineMode;
    }
    
    public double getZoom() {
      return zoom;
    }
    
    public double getScaleFactor() {
      return scaleFactor;
    }

    public int getDx() {
      return dx;
    }
    
    public int getDy() {
      return dy;
    }
    
    public Rectangle2D getCacheBounds() {
      return cacheBounds;
    }
    
    public Set<DrawOption> getDrawOptions() {
      return drawOptions;
    }

    // TYPE_INT_ARGB, four bytes per pixel
    public long getSizeInBytes() {
      return (long) image.getWidth() * image.getHeight() * 4;
    }

    @Override
    public String toString() {
      return (component == null ? "" : component.getName() + ":") + state + ":" + zoom;
    }
  }
}
