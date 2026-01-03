package org.diylc.components.transform;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.diylc.common.IComponentTransformer;
import org.diylc.common.Orientation;
import org.diylc.components.passive.BoxTrimmer;
import org.diylc.core.IDIYComponent;

public class BoxTrimmerTransformer implements IComponentTransformer {

  private enum Corner { TL, TR, BL, BR }

  @Override
  public boolean mirroringChangesCircuit() {
    // In this component, pins are defined by index (0=CCW,1=Wiper,2=CW),
    // and mirroring only changes geometry/orientation, not pin identity.
    return false;
  }

  @Override
  public boolean canMirror(IDIYComponent<?> component) {
    return component instanceof BoxTrimmer;
  }

  @Override
  public boolean canRotate(IDIYComponent<?> component) {
    return component instanceof BoxTrimmer;
  }

  @Override
  public void rotate(IDIYComponent<?> component, Point2D center, int direction) {
    if (!(component instanceof BoxTrimmer)) return;
    if (direction != 1 && direction != -1) return;

    BoxTrimmer t = (BoxTrimmer) component;

    // 1) capture old state
    Orientation oldO = t.getOrientation();
    boolean oldH = t.getScrewFlippedHorizontally();
    boolean oldV = t.getScrewFlippedVertically();
    Corner oldCorner = flagsToCorner(oldO, oldH, oldV);

    // 2) rotate CP0
    Point2D p0 = t.getControlPoint(0);
    AffineTransform rot = AffineTransform.getRotateInstance(
            (Math.PI / 2) * -direction, // +direction means clockwise in DIYLC usage
            center.getX(), center.getY()
    );
    Point2D p0r = new Point2D.Double();
    rot.transform(p0, p0r);

    // 3) rotate orientation
    Orientation newO = rotateOrientation(oldO, direction);

    // 4) rotate the screw corner geometrically, then map back to flags for new orientation
    Corner newCorner = (direction == 1) ? rotateCornerCW(oldCorner) : rotateCornerCCW(oldCorner);
    boolean[] hv = cornerToFlags(newO, newCorner);

    // 5) apply (order matters with your component: set CP0, then orientation)
    t.setControlPoint(p0r, 0);
    t.setOrientation(newO);
    t.setScrewFlippedHorizontally(hv[0]);
    t.setScrewFlippedVertically(hv[1]);
  }

  @Override
  public void mirror(IDIYComponent<?> component, Point2D center, int direction) {
    if (!(component instanceof BoxTrimmer)) return;

    BoxTrimmer t = (BoxTrimmer) component;

    // 1) capture old state
    Orientation oldO = t.getOrientation();
    boolean oldH = t.getScrewFlippedHorizontally();
    boolean oldV = t.getScrewFlippedVertically();
    Corner oldCorner = flagsToCorner(oldO, oldH, oldV);

    // 2) mirror CP0
    Point2D p0 = t.getControlPoint(0);
    Point2D p0m;

    Corner newCorner;
    Orientation newO = oldO;

    if (direction == IComponentTransformer.HORIZONTAL) {
      // mirror left-right about vertical axis through center.x
      p0m = new Point2D.Double(2 * center.getX() - p0.getX(), p0.getY());

      // orientation change under horizontal mirror:
      // DEFAULT <-> _180, _90 and _270 unchanged (in your pin-direction model)
      newO = mirrorOrientationHorizontal(oldO);

      // screw corner mirror
      newCorner = mirrorCornerHorizontal(oldCorner);
    } else {
      // mirror top-bottom about horizontal axis through center.y
      p0m = new Point2D.Double(p0.getX(), 2 * center.getY() - p0.getY());

      // orientation change under vertical mirror:
      // _90 <-> _270, DEFAULT and _180 unchanged
      newO = mirrorOrientationVertical(oldO);

      // screw corner mirror
      newCorner = mirrorCornerVertical(oldCorner);
    }

    // 3) map mirrored corner back to flags for the new orientation
    boolean[] hv = cornerToFlags(newO, newCorner);

    // 4) apply
    t.setControlPoint(p0m, 0);
    t.setOrientation(newO);
    t.setScrewFlippedHorizontally(hv[0]);
    t.setScrewFlippedVertically(hv[1]);
  }

  // ---------------- orientation transforms ----------------

  private static Orientation rotateOrientation(Orientation o, int direction) {
    if (direction == 1) { // clockwise
      switch (o) {
        case DEFAULT: return Orientation._90;
        case _90:     return Orientation._180;
        case _180:    return Orientation._270;
        case _270:    return Orientation.DEFAULT;
      }
    } else { // counterclockwise
      switch (o) {
        case DEFAULT: return Orientation._270;
        case _270:    return Orientation._180;
        case _180:    return Orientation._90;
        case _90:     return Orientation.DEFAULT;
      }
    }
    return o;
  }

  private static Orientation mirrorOrientationHorizontal(Orientation o) {
    switch (o) {
      case DEFAULT: return Orientation._180;
      case _180:    return Orientation.DEFAULT;
      case _90:     return Orientation._90;
      case _270:    return Orientation._270;
    }
    return o;
  }

  private static Orientation mirrorOrientationVertical(Orientation o) {
    switch (o) {
      case _90:     return Orientation._270;
      case _270:    return Orientation._90;
      case DEFAULT: return Orientation.DEFAULT;
      case _180:    return Orientation._180;
    }
    return o;
  }

  // ---------------- corner transforms (pure geometry) ----------------

  private static Corner rotateCornerCW(Corner c) {
    switch (c) {
      case TL: return Corner.TR;
      case TR: return Corner.BR;
      case BR: return Corner.BL;
      case BL: return Corner.TL;
    }
    return c;
  }

  private static Corner rotateCornerCCW(Corner c) {
    switch (c) {
      case TL: return Corner.BL;
      case BL: return Corner.BR;
      case BR: return Corner.TR;
      case TR: return Corner.TL;
    }
    return c;
  }

  private static Corner mirrorCornerHorizontal(Corner c) { // left-right
    switch (c) {
      case TL: return Corner.TR;
      case TR: return Corner.TL;
      case BL: return Corner.BR;
      case BR: return Corner.BL;
    }
    return c;
  }

  private static Corner mirrorCornerVertical(Corner c) { // top-bottom
    switch (c) {
      case TL: return Corner.BL;
      case BL: return Corner.TL;
      case TR: return Corner.BR;
      case BR: return Corner.TR;
    }
    return c;
  }

  // ---------------- mapping between (orientation, H/V flags) and absolute corner ----------------
  // This matches your draw() logic exactly.

  private static Corner flagsToCorner(Orientation o, boolean h, boolean v) {
    // Interpret what (h,v) means for each orientation as implemented in BoxTrimmer.draw()
    switch (o) {
      case DEFAULT:
      case _90:
        // h: left->right, v: bottom->top
        if (!h && !v) return Corner.BL;
        if ( h && !v) return Corner.BR;
        if (!h &&  v) return Corner.TL;
        return Corner.TR;

      case _180:
        // h meaning is inverted vs DEFAULT, v still bottom->top
        if (!h && !v) return Corner.BR;
        if ( h && !v) return Corner.BL;
        if (!h &&  v) return Corner.TR;
        return Corner.TL;

      case _270:
        // h normal, v meaning inverted (top when v=false)
        if (!h && !v) return Corner.TL;
        if ( h && !v) return Corner.TR;
        if (!h &&  v) return Corner.BL;
        return Corner.BR;
    }
    return Corner.BL;
  }

  private static boolean[] cornerToFlags(Orientation o, Corner c) {
    boolean h = false, v = false;

    switch (o) {
      case DEFAULT:
      case _90:
        // h: left->right, v: bottom->top
        switch (c) {
          case BL: h = false; v = false; break;
          case BR: h = true;  v = false; break;
          case TL: h = false; v = true;  break;
          case TR: h = true;  v = true;  break;
        }
        break;

      case _180:
        // inverted h, normal v
        switch (c) {
          case BR: h = false; v = false; break;
          case BL: h = true;  v = false; break;
          case TR: h = false; v = true;  break;
          case TL: h = true;  v = true;  break;
        }
        break;

      case _270:
        // normal h, inverted v (top when v=false)
        switch (c) {
          case TL: h = false; v = false; break;
          case TR: h = true;  v = false; break;
          case BL: h = false; v = true;  break;
          case BR: h = true;  v = true;  break;
        }
        break;
    }

    return new boolean[] { h, v };
  }
}
