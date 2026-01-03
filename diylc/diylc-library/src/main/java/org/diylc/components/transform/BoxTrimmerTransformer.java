package org.diylc.components.transform;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.diylc.common.IComponentTransformer;
import org.diylc.common.Orientation;
import org.diylc.components.passive.BoxTrimmer;
import org.diylc.core.IDIYComponent;

public class BoxTrimmerTransformer implements IComponentTransformer {

    private enum Corner { TL, TR, BL, BR }

    // ---------- public API ----------

    @Override
    public boolean mirroringChangesCircuit() {
        // Pins are by index (0=CCW,1=Wiper,2=CW); mirror doesn't swap indices.
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
        if (!(component instanceof BoxTrimmer t)) return;
        if (direction != 1 && direction != -1) return;

        // capture old state
        Orientation oldO = t.getOrientation();
        Corner oldCorner = flagsToCorner(oldO, t.getScrewFlippedHorizontally(), t.getScrewFlippedVertically());

        // rotate CP0 about center (clockwise for direction=+1)
        Point2D p0 = t.getControlPoint(0);
        AffineTransform rot = AffineTransform.getRotateInstance((Math.PI / 2) * -direction, center.getX(), center.getY());
        Point2D p0r = new Point2D.Double();
        rot.transform(p0, p0r);

        // compute new orientation and new corner
        Orientation newO = rotateOrientation(oldO, direction);
        Corner newCorner = (direction == 1) ? rotateCornerCW(oldCorner) : rotateCornerCCW(oldCorner);

        // decode corner back to flags in new orientation
        boolean[] hv = cornerToFlags(newO, newCorner);

        // apply (CP0 first, then orientation regenerates pins)
        t.setControlPoint(p0r, 0);
        t.setOrientation(newO);
        t.setScrewFlippedHorizontally(hv[0]);
        t.setScrewFlippedVertically(hv[1]);
    }

    @Override
    public void mirror(IDIYComponent<?> component, Point2D center, int direction) {
        if (!(component instanceof BoxTrimmer t)) return;

        Orientation oldO = t.getOrientation();
        Corner oldCorner = flagsToCorner(oldO, t.getScrewFlippedHorizontally(), t.getScrewFlippedVertically());

        Point2D p0 = t.getControlPoint(0);
        Point2D p0m;

        Orientation newO;
        Corner newCorner;

        if (direction == IComponentTransformer.HORIZONTAL) {
            // mirror left-right
            p0m = new Point2D.Double(2 * center.getX() - p0.getX(), p0.getY());
            newO = mirrorOrientationHorizontal(oldO);
            newCorner = mirrorCornerHorizontal(oldCorner);
        } else {
            // mirror top-bottom
            p0m = new Point2D.Double(p0.getX(), 2 * center.getY() - p0.getY());
            newO = mirrorOrientationVertical(oldO);
            newCorner = mirrorCornerVertical(oldCorner);
        }

        boolean[] hv = cornerToFlags(newO, newCorner);

        t.setControlPoint(p0m, 0);
        t.setOrientation(newO);
        t.setScrewFlippedHorizontally(hv[0]);
        t.setScrewFlippedVertically(hv[1]);
    }

    // ---------- orientation index + transforms (table-driven) ----------

    private static int oIdx(Orientation o) {
        return switch (o) {
            case DEFAULT -> 0;
            case _90     -> 1;
            case _180    -> 2;
            case _270    -> 3;
        };
    }

    // rotate orientation clockwise / counterclockwise
    // index order: DEFAULT, _90, _180, _270
    private static final Orientation[] ORIENTATIONS = {
            Orientation.DEFAULT, Orientation._90, Orientation._180, Orientation._270
    };

    private static Orientation rotateOrientation(Orientation o, int direction) {
        int i = oIdx(o);
        int ni = (direction == 1) ? (i + 1) : (i + 3); // -1 == +3 mod 4
        return ORIENTATIONS[ni & 3];
    }

    // mirror tables: new orientation index after mirror
    private static final int[] ORIENT_MIRROR_H = { 2, 1, 0, 3 }; // DEFAULT<->_180, _90 stays, _270 stays
    private static final int[] ORIENT_MIRROR_V = { 0, 3, 2, 1 }; // _90<->_270, DEFAULT stays, _180 stays

    private static Orientation mirrorOrientationHorizontal(Orientation o) {
        return ORIENTATIONS[ORIENT_MIRROR_H[oIdx(o)]];
    }

    private static Orientation mirrorOrientationVertical(Orientation o) {
        return ORIENTATIONS[ORIENT_MIRROR_V[oIdx(o)]];
    }

    // ---------- corner mapping (table-driven, matches BoxTrimmer.draw()) ----------

    /**
     * CORNER_BY_FLAGS[orientationIndex][hvIndex] -> Corner
     * hvIndex encodes (h,v) as:
     *   bit0 = h (0/1)
     *   bit1 = v (0/1)
     * so:
     *   0: h=0,v=0
     *   1: h=1,v=0
     *   2: h=0,v=1
     *   3: h=1,v=1
     *
     * This matches draw() logic:
     * - DEFAULT, _90: normal (h: left->right, v: bottom->top)
     * - _180: horizontal meaning inverted
     * - _270: vertical meaning inverted
     */
    private static final Corner[][] CORNER_BY_FLAGS = {
            // DEFAULT
            { Corner.BL, Corner.BR, Corner.TL, Corner.TR },
            // _90
            { Corner.BL, Corner.BR, Corner.TL, Corner.TR },
            // _180
            { Corner.BR, Corner.BL, Corner.TR, Corner.TL },
            // _270
            { Corner.TL, Corner.TR, Corner.BL, Corner.BR }
    };

    private static final boolean[][][] FLAGS_BY_CORNER = buildFlagsByCorner();

    private static boolean[][][] buildFlagsByCorner() {
        boolean[][][] out = new boolean[4][4][2]; // [ori][corner][h/v]
        for (int oi = 0; oi < 4; oi++) {
            for (int hv = 0; hv < 4; hv++) {
                Corner c = CORNER_BY_FLAGS[oi][hv];
                boolean h = (hv & 1) != 0;
                boolean v = (hv & 2) != 0;
                out[oi][c.ordinal()][0] = h;
                out[oi][c.ordinal()][1] = v;
            }
        }
        return out;
    }

    private static Corner flagsToCorner(Orientation o, boolean h, boolean v) {
        int oi = oIdx(o);
        int hv = (h ? 1 : 0) | (v ? 2 : 0);
        return CORNER_BY_FLAGS[oi][hv];
    }

    private static boolean[] cornerToFlags(Orientation o, Corner c) {
        int oi = oIdx(o);
        boolean h = FLAGS_BY_CORNER[oi][c.ordinal()][0];
        boolean v = FLAGS_BY_CORNER[oi][c.ordinal()][1];
        return new boolean[] { h, v };
    }

    // ---------- corner transforms (table-driven) ----------

    // ordinal order: TL=0, TR=1, BL=2, BR=3
    private static final Corner[] CORNER_ROT_CW = {
            Corner.TR, // TL -> TR
            Corner.BR, // TR -> BR
            Corner.TL, // BL -> TL
            Corner.BL  // BR -> BL
    };

    private static final Corner[] CORNER_ROT_CCW = {
            Corner.BL, // TL -> BL
            Corner.TL, // TR -> TL
            Corner.BR, // BL -> BR
            Corner.TR  // BR -> TR
    };

    private static final Corner[] CORNER_MIRROR_H = {
            Corner.TR, // TL <-> TR
            Corner.TL,
            Corner.BR, // BL <-> BR
            Corner.BL
    };

    private static final Corner[] CORNER_MIRROR_V = {
            Corner.BL, // TL <-> BL
            Corner.BR, // TR <-> BR
            Corner.TL,
            Corner.TR
    };

    private static Corner rotateCornerCW(Corner c) {
        return CORNER_ROT_CW[c.ordinal()];
    }

    private static Corner rotateCornerCCW(Corner c) {
        return CORNER_ROT_CCW[c.ordinal()];
    }

    private static Corner mirrorCornerHorizontal(Corner c) {
        return CORNER_MIRROR_H[c.ordinal()];
    }

    private static Corner mirrorCornerVertical(Corner c) {
        return CORNER_MIRROR_V[c.ordinal()];
    }
}
