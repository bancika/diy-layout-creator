package com.github.romankh3.image.comparison.model;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

import java.awt.Point;
import java.util.Objects;

/**
 * Object contained data for a rectangle.
 */
public class Rectangle {

    /**
     * Left Top {@link Point} on the {@link Rectangle}.
     */
    private Point minPoint;

    /**
     * Right bottom {@link Point} on the {@link Rectangle}.
     */
    private Point maxPoint;

    /**
     * Create empty instance of the {@link Rectangle}.
     */
    private Rectangle() {
        minPoint = new Point();
        maxPoint = new Point();
    }

    /**
     * Create clone object based on the provided {@link Rectangle}.
     *
     * @param rectangle provided {@link Rectangle} object.
     */
    public Rectangle(Rectangle rectangle) {
        this.minPoint = new Point(rectangle.getMinPoint().x, rectangle.getMinPoint().y);
        this.maxPoint = new Point(rectangle.getMaxPoint().x, rectangle.getMaxPoint().y);
    }

    /**
     * Create instance of the {@link Rectangle} based on the provided coordinates.
     *
     * @param minX minimal X-coordinate.
     * @param minY minimal Y-coordinate.
     * @param maxX maximal X-coordinate.
     * @param maxY maximal Y-coordinate.
     */
    public Rectangle(int minX, int minY, int maxX, int maxY) {
        this.minPoint = new Point(minX, minY);
        this.maxPoint = new Point(maxX, maxY);
    }

    /**
     * Create default {@link Rectangle} object.
     *
     * @return default rectangle {@link Rectangle}.
     */
    public static Rectangle createDefault() {
        Rectangle defaultRectangle = new Rectangle();

        defaultRectangle.setDefaultValues();

        return defaultRectangle;
    }

    /**
     * Create instance with zero points.
     *
     * @return created {@link Rectangle} instance.
     */
    public static Rectangle createZero() {
        Rectangle rectangle = new Rectangle();
        rectangle.makeZeroRectangle();
        return rectangle;
    }

    /**
     * Create new {@link Rectangle} via merging this and that.
     *
     * @param that {@link Rectangle} for merging with this.
     * @return new merged {@link Rectangle}.
     */
    public Rectangle merge(Rectangle that) {
        return new Rectangle(min(this.getMinPoint().x, that.getMinPoint().x),
                min(this.getMinPoint().y, that.getMinPoint().y),
                max(this.getMaxPoint().x, that.getMaxPoint().x),
                max(this.getMaxPoint().y, that.getMaxPoint().y));
    }

    /**
     * Check is that rectangle overlapp this.
     *
     * @param that {@link Rectangle} which checks with this.
     * @return true if this over lapp that, false otherwise.
     */
    public boolean isOverlapping(Rectangle that) {
        if (this.getMaxPoint().getY() < that.getMinPoint().getY() ||
                that.getMaxPoint().getY() < this.getMinPoint().getY()) {
            return false;
        }
        return this.getMaxPoint().getX() >= that.getMinPoint().getX() &&
                that.getMaxPoint().getX() >= this.getMinPoint().getX();
    }

    /**
     * Set default values for rectangle.
     */
    public void setDefaultValues() {
        this.maxPoint = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
        this.minPoint = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Make zero rectagle.
     */
    public void makeZeroRectangle() {
        this.minPoint = new Point();
        this.maxPoint = new Point();
    }

    /**
     * Size of the {@link Rectangle}, counted as width x height.
     *
     * @return the size of the {@link Rectangle}.
     */
    public Integer size() {
        return getWidth() * getHeight();
    }

    /**
     * Count the width of the {@link Rectangle}.
     * Min and max point are included, so real width is +1px
     *
     * @return rectangle width.
     */
    public int getWidth() {
        return maxPoint.x - minPoint.x + 1;
    }

    /**
     * Count the height of the {@link Rectangle}.
     * Min and max point are included, so real width is +1px.
     *
     * @return rectangle height.
     */
    public int getHeight() {
        return maxPoint.y - minPoint.y + 1;
    }

    /**
     * Check in the provided {@link Point} contains in the {@link Rectangle}.
     *
     * @param point provided {@link Point}.
     * @return {@code true} if provided {@link Point} contains, {@code false} - otherwise.
     */
    boolean containsPoint(Point point) {
        return  point.x >= minPoint.x && point.x<= maxPoint.x && point.y >= minPoint.y && point.y <= maxPoint.y;
    }

    public Point getMinPoint() {
        return minPoint;
    }

    public void setMinPoint(Point minPoint) {
        this.minPoint = minPoint;
    }

    public Point getMaxPoint() {
        return maxPoint;
    }

    public void setMaxPoint(Point maxPoint) {
        this.maxPoint = maxPoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Rectangle rectangle = (Rectangle) o;
        return minPoint.equals(rectangle.minPoint) &&
                maxPoint.equals(rectangle.maxPoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minPoint, maxPoint);
    }
}
