package org.diylc.presenter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

/**
 * {@link Graphics2D} wrapper that keeps track of all drawing actions and
 * creates an {@link Area} that corresponds to drawn objects. Before each
 * component is drawn, {@link #startedDrawingComponent()} should be called.
 * After the component is drawn, area may be retrieved using
 * {@link #finishedDrawingComponent()}. Graphics configuration (color, font,
 * etc) is reset between each two components.
 * 
 * @author Branislav Stojkovic
 */
class G2DWrapper extends Graphics2D {

	public static int LINE_SENSITIVITY_MARGIN = 2;
	public static int CURVE_SENSITIVITY = 10;

	private boolean drawingComponent = false;

	private Graphics2D canvasGraphics;
	private Stroke originalStroke;
	private Color originalColor;
	private Composite originalComposite;
	private AffineTransform originalTx;
	private Font originalFont;
	private AffineTransform currentTx;
	private Area currentArea;

	/**
	 * Creates a wrapper around specified {@link Graphics2D} object.
	 * 
	 * @param canvasGraphics
	 */
	public G2DWrapper(Graphics2D canvasGraphics) {
		super();
		this.canvasGraphics = canvasGraphics;
		currentArea = new Area();
		currentTx = new AffineTransform();
	}

	/**
	 * Clears out the current area and caches canvas settings.
	 */
	public void startedDrawingComponent() {
		drawingComponent = true;
		currentArea = new Area();
		originalStroke = canvasGraphics.getStroke();
		originalColor = canvasGraphics.getColor();
		originalTx = canvasGraphics.getTransform();
		originalComposite = canvasGraphics.getComposite();
		originalFont = canvasGraphics.getFont();
		currentTx = new AffineTransform();
	}

	/**
	 * Reverts {@link Graphics2D} settings and returns area drawn by component
	 * in the meantime.
	 * 
	 * @return
	 */
	public Area finishedDrawingComponent() {
		drawingComponent = false;
		canvasGraphics.setStroke(originalStroke);
		canvasGraphics.setColor(originalColor);
		canvasGraphics.setTransform(originalTx);
		canvasGraphics.setComposite(originalComposite);
		canvasGraphics.setFont(originalFont);
		return currentArea;
	}

	/**
	 * Appends shape interior to the current component area.
	 * 
	 * @param s
	 */
	private void appendShape(Shape s) {
		if (!drawingComponent) {
			return;
		}
		Area area = new Area(s);
		area.transform(currentTx);
		currentArea.add(area);
	}

	/**
	 * Appends shape outline to the current component area.
	 * 
	 * @param s
	 */
	private void appendShapeOutline(Shape s) {
		if (!drawingComponent) {
			return;
		}
		PathIterator pathIterator = s.getPathIterator(null);
		double thickness;
		if (getStroke() instanceof BasicStroke) {
			thickness = ((BasicStroke) getStroke()).getLineWidth() + 2 * LINE_SENSITIVITY_MARGIN;
		} else {
			thickness = 2 * LINE_SENSITIVITY_MARGIN;
		}
		Point2D prevPoint = null;
		while (!pathIterator.isDone()) {
			double[] coord = new double[6];
			int type = pathIterator.currentSegment(coord);
			switch (type) {
			case PathIterator.SEG_MOVETO:
				prevPoint = new Point2D.Double(coord[0], coord[1]);
				break;
			case PathIterator.SEG_LINETO:
				// Represent straight line with a rectangle.
				Point2D nextPoint = new Point2D.Double(coord[0], coord[1]);
				Double theta = Math.atan2(nextPoint.getY() - prevPoint.getY(), nextPoint.getX()
						- prevPoint.getX());
				double width = Math.sqrt(Math.pow(nextPoint.getX() - prevPoint.getX(), 2)
						+ Math.pow(nextPoint.getY() - prevPoint.getY(), 2));
				double midX = (prevPoint.getX() + nextPoint.getX()) / 2;
				double midY = (prevPoint.getY() + nextPoint.getY()) / 2;
				Rectangle2D rect = new Rectangle2D.Double(midX - width / 2, midY - thickness / 2,
						width, thickness);
				Area area = new Area(rect);
				area.transform(AffineTransform.getRotateInstance(theta, midX, midY));
				appendShape(area);
				// Set the prev point to line end
				prevPoint = nextPoint;
				break;
			case PathIterator.SEG_CUBICTO:
				CubicCurve2D cubicCurve = new CubicCurve2D.Double(prevPoint.getX(), prevPoint
						.getY(), coord[0], coord[1], coord[2], coord[3], coord[4], coord[5]);
				addCubicCurveArea(cubicCurve);
				prevPoint = new Point2D.Double(coord[4], coord[5]);
				break;
			case PathIterator.SEG_QUADTO:
				QuadCurve2D quadCurve = new QuadCurve2D.Double(prevPoint.getX(), prevPoint.getY(),
						coord[0], coord[1], coord[2], coord[3]);
				addQuadCurveArea(quadCurve);
				prevPoint = new Point2D.Double(coord[3], coord[3]);
				break;
			}
			pathIterator.next();
		}
	}

	/**
	 * Adds a cubic curve to the component area. It uses subdivision to
	 * approximate the curve with a series of straight lines.
	 * 
	 * @param curve
	 */
	private void addCubicCurveArea(CubicCurve2D curve) {
		double d = curve.getP1().distance(curve.getP2());
		// When end points are close enough, approximate the curve with a
		// straight line.
		if (d < CURVE_SENSITIVITY) {
			appendShapeOutline(new Line2D.Double(curve.getP1(), curve.getP2()));
		} else {
			CubicCurve2D leftSubcurve = new CubicCurve2D.Double();
			CubicCurve2D rightSubcurve = new CubicCurve2D.Double();
			curve.subdivide(leftSubcurve, rightSubcurve);
			addCubicCurveArea(leftSubcurve);
			addCubicCurveArea(rightSubcurve);
		}
	}

	/**
	 * Adds a quad curve to the component area. It uses subdivision to
	 * approximate the curve with a series of straight lines.
	 * 
	 * @param curve
	 */
	private void addQuadCurveArea(QuadCurve2D curve) {
		double d = curve.getP1().distance(curve.getP2());
		// When end points are close enough, approximate the curve with a
		// straight line.
		if (d < CURVE_SENSITIVITY) {
			appendShapeOutline(new Line2D.Double(curve.getP1(), curve.getP2()));
		} else {
			QuadCurve2D leftSubcurve = new QuadCurve2D.Double();
			QuadCurve2D rightSubcurve = new QuadCurve2D.Double();
			curve.subdivide(leftSubcurve, rightSubcurve);
			addQuadCurveArea(leftSubcurve);
			addQuadCurveArea(rightSubcurve);
		}
	}

	@Override
	public void addRenderingHints(Map<?, ?> hints) {
		canvasGraphics.addRenderingHints(hints);
	}

	@Override
	public void clip(Shape s) {
		canvasGraphics.clip(s);
	}

	@Override
	public void draw(Shape s) {
		canvasGraphics.draw(s);
		appendShapeOutline(s);
	}

	@Override
	public void drawGlyphVector(GlyphVector g, float x, float y) {
		canvasGraphics.drawGlyphVector(g, x, y);
	}

	@Override
	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
		// FIXME: process map
		return canvasGraphics.drawImage(img, xform, obs);
	}

	@Override
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
		canvasGraphics.drawImage(img, op, x, y);
		// FIXME: process map
	}

	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		canvasGraphics.drawRenderableImage(img, xform);
		// FIXME: process map
	}

	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		canvasGraphics.drawRenderedImage(img, xform);
		// FIXME: process map
	}

	@Override
	public void drawString(String str, int x, int y) {
		canvasGraphics.drawString(str, x, y);
		// FIXME: process map
	}

	@Override
	public void drawString(String str, float x, float y) {
		canvasGraphics.drawString(str, x, y);
		// FIXME: process map
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, int x, int y) {
		canvasGraphics.drawString(iterator, x, y);
		// FIXME: process map
	}

	@Override
	public void drawString(AttributedCharacterIterator iterator, float x, float y) {
		canvasGraphics.drawString(iterator, x, y);
		// FIXME: process map
	}

	@Override
	public void fill(Shape s) {
		canvasGraphics.fill(s);
		appendShape(s);
	}

	@Override
	public Color getBackground() {
		return canvasGraphics.getBackground();
	}

	@Override
	public Composite getComposite() {
		return canvasGraphics.getComposite();
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		return canvasGraphics.getDeviceConfiguration();
	}

	@Override
	public FontRenderContext getFontRenderContext() {
		return canvasGraphics.getFontRenderContext();
	}

	@Override
	public Paint getPaint() {
		return canvasGraphics.getPaint();
	}

	@Override
	public Object getRenderingHint(Key hintKey) {
		return canvasGraphics.getRenderingHint(hintKey);
	}

	@Override
	public RenderingHints getRenderingHints() {
		return canvasGraphics.getRenderingHints();
	}

	@Override
	public Stroke getStroke() {
		return canvasGraphics.getStroke();
	}

	@Override
	public AffineTransform getTransform() {
		return canvasGraphics.getTransform();
	}

	@Override
	public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
		return canvasGraphics.hit(rect, s, onStroke);
	}

	@Override
	public void rotate(double theta) {
		canvasGraphics.rotate(theta);
		currentTx.rotate(theta);
	}

	@Override
	public void rotate(double theta, double x, double y) {
		canvasGraphics.rotate(theta, x, y);
		currentTx.rotate(theta, x, y);
	}

	@Override
	public void scale(double sx, double sy) {
		canvasGraphics.scale(sx, sy);
		currentTx.scale(sx, sy);
	}

	@Override
	public void setBackground(Color color) {
		canvasGraphics.setBackground(color);
		// FIXME: fix map
	}

	@Override
	public void setComposite(Composite comp) {
		canvasGraphics.setComposite(comp);
		// FIXME: check this.
	}

	@Override
	public void setPaint(Paint paint) {
		canvasGraphics.setPaint(paint);
		// FIXME: check this
	}

	@Override
	public void setRenderingHint(Key hintKey, Object hintValue) {
		canvasGraphics.setRenderingHint(hintKey, hintValue);
		// FIXME: check this
	}

	@Override
	public void setRenderingHints(Map<?, ?> hints) {
		canvasGraphics.setRenderingHints(hints);
		// FIXME: check this
	}

	@Override
	public void setStroke(Stroke s) {
		canvasGraphics.setStroke(s);
	}

	@Override
	public void setTransform(AffineTransform Tx) {
		canvasGraphics.setTransform(Tx);
		currentTx = Tx;
	}

	@Override
	public void shear(double shx, double shy) {
		canvasGraphics.shear(shx, shy);
		currentTx.shear(shx, shy);
	}

	@Override
	public void transform(AffineTransform Tx) {
		canvasGraphics.transform(Tx);
		currentTx.concatenate(Tx);
	}

	@Override
	public void translate(int x, int y) {
		canvasGraphics.translate(x, y);
		currentTx.translate(x, y);
	}

	@Override
	public void translate(double tx, double ty) {
		canvasGraphics.translate(tx, ty);
		currentTx.translate(tx, ty);
	}

	@Override
	public void clearRect(int x, int y, int width, int height) {
		canvasGraphics.clearRect(x, y, width, height);
	}

	@Override
	public void clipRect(int x, int y, int width, int height) {
		canvasGraphics.clipRect(x, y, width, height);
	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		canvasGraphics.copyArea(x, y, width, height, dx, dy);
	}

	@Override
	public Graphics create() {
		return canvasGraphics.create();
	}

	@Override
	public void dispose() {
		canvasGraphics.dispose();
	}

	@Override
	public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		canvasGraphics.drawArc(x, y, width, height, startAngle, arcAngle);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
		// FIXME: map
		return canvasGraphics.drawImage(img, x, y, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
		// FIXME: map
		return canvasGraphics.drawImage(img, x, y, bgcolor, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
		// FIXME: map
		return canvasGraphics.drawImage(img, x, y, width, height, observer);
	}

	@Override
	public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor,
			ImageObserver observer) {
		// FIXME: map
		return canvasGraphics.drawImage(img, x, y, width, height, bgcolor, observer);
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1,
			int sx2, int sy2, ImageObserver observer) {
		// FIXME: map
		return canvasGraphics.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
	}

	@Override
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1,
			int sx2, int sy2, Color bgcolor, ImageObserver observer) {
		// FIXME: map
		return drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
	}

	@Override
	public void drawLine(int x1, int y1, int x2, int y2) {
		canvasGraphics.drawLine(x1, y1, x2, y2);
		appendShapeOutline(new Line2D.Double(x1, y1, x2, y2));
	}

	@Override
	public void drawOval(int x, int y, int width, int height) {
		canvasGraphics.drawOval(x, y, width, height);
		appendShapeOutline(new Ellipse2D.Double(x, y, width, height));
	}

	@Override
	public void drawPolygon(int[] points, int[] points2, int points3) {
		canvasGraphics.drawPolygon(points, points2, points3);
		appendShapeOutline(new Polygon(points, points2, points3));
	}

	@Override
	public void drawPolyline(int[] points, int[] points2, int points3) {
		canvasGraphics.drawPolyline(points, points2, points3);
	}

	@Override
	public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
		canvasGraphics.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
		appendShapeOutline(new RoundRectangle2D.Double(x, y, width, height, arcWidth, arcHeight));
	}

	@Override
	public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
		canvasGraphics.fillArc(x, y, width, height, startAngle, arcAngle);
		appendShape(new Arc2D.Double(x, y, width, height, startAngle, arcAngle, Arc2D.PIE));
	}

	@Override
	public void fillOval(int x, int y, int width, int height) {
		canvasGraphics.fillOval(x, y, width, height);
		appendShape(new Ellipse2D.Double(x, y, width, height));
	}

	@Override
	public void fillPolygon(int[] points, int[] points2, int points3) {
		canvasGraphics.fillPolygon(points, points2, points3);
		appendShape(new Polygon(points, points2, points3));
	}

	@Override
	public void drawRect(int x, int y, int width, int height) {
		canvasGraphics.drawRect(x, y, width, height);
		appendShapeOutline(new Rectangle(x, y, width, height));
	}

	@Override
	public void fillRect(int x, int y, int width, int height) {
		canvasGraphics.fillRect(x, y, width, height);
		appendShape(new Rectangle(x, y, width, height));
	}

	@Override
	public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
		canvasGraphics.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
		appendShape(new RoundRectangle2D.Double(x, y, width, height, arcWidth, arcHeight));
	}

	@Override
	public Shape getClip() {
		return canvasGraphics.getClip();
	}

	@Override
	public Rectangle getClipBounds() {
		return canvasGraphics.getClipBounds();
	}

	@Override
	public Color getColor() {
		return canvasGraphics.getColor();
	}

	@Override
	public Font getFont() {
		return canvasGraphics.getFont();
	}

	@Override
	public FontMetrics getFontMetrics(Font f) {
		return canvasGraphics.getFontMetrics(f);
	}

	@Override
	public void setClip(Shape clip) {
		canvasGraphics.setClip(clip);
	}

	@Override
	public void setClip(int x, int y, int width, int height) {
		canvasGraphics.setClip(x, y, width, height);
	}

	@Override
	public void setColor(Color c) {
		canvasGraphics.setColor(c);
	}

	@Override
	public void setFont(Font font) {
		canvasGraphics.setFont(font);
	}

	@Override
	public void setPaintMode() {
		canvasGraphics.setPaintMode();
	}

	@Override
	public void setXORMode(Color c1) {
		canvasGraphics.setXORMode(c1);
	}
}
