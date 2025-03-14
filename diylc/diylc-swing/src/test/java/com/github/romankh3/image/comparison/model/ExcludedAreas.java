package com.github.romankh3.image.comparison.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * The area that will be excluded, masked, in the image.
 */
public class ExcludedAreas {

    /**
     * The collection of the areas which would be excluded from the comparison.
     */
    private final List<Rectangle> excluded;

    /**
     * Create empty instance of the {@link ExcludedAreas}.
     */
    public ExcludedAreas() {
        excluded = new ArrayList<>();
    }

    /**
     * Create instance of the {@link ExcludedAreas} with provided {@link Rectangle} areas.
     *
     * @param excluded provided collection of the {@link Rectangle} objects.
     */
    public ExcludedAreas(List<Rectangle> excluded) {
        this.excluded = excluded;
    }

    /**
     * Check if this {@link Point} contains in the {@link ExcludedAreas#excluded}
     * collection of the {@link Rectangle}.
     *
     * @param point the {@link Point} object to be checked.
     *
     * @return {@code true} if this {@link Point} contains in areas from {@link ExcludedAreas#excluded}.
     */
    public boolean contains(Point point) {
        return excluded.stream().anyMatch(rectangle -> rectangle.containsPoint(point));
    }

    public List<Rectangle> getExcluded() {
        return excluded;
    }
}
