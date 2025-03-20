package org.diylc.presenter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

import org.diylc.core.IDIYComponent;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Test;

public class CalcUtilsTest {

    @Test
    public void testRoundToGrid() {
        Size gridSpacing = new Size(10.0, SizeUnit.px);
        
        // Test exact grid points
        assertEquals(10.0, CalcUtils.roundToGrid(10.0, gridSpacing), 0.001);
        assertEquals(20.0, CalcUtils.roundToGrid(20.0, gridSpacing), 0.001);
        
        // Test rounding up
        assertEquals(10.0, CalcUtils.roundToGrid(8.0, gridSpacing), 0.001);
        assertEquals(20.0, CalcUtils.roundToGrid(16.0, gridSpacing), 0.001);
        
        // Test rounding down
        assertEquals(10.0, CalcUtils.roundToGrid(12.0, gridSpacing), 0.001);
        assertEquals(20.0, CalcUtils.roundToGrid(22.0, gridSpacing), 0.001);
        
        // Test with different grid spacing
        Size finerGrid = new Size(5.0, SizeUnit.px);
        assertEquals(5.0, CalcUtils.roundToGrid(6.0, finerGrid), 0.001);
        assertEquals(15.0, CalcUtils.roundToGrid(13.0, finerGrid), 0.001);
    }

    @Test
    public void testSnapPointToGrid() {
        Size gridSpacing = new Size(10.0, SizeUnit.px);
        Point2D point = new Point2D.Double(12.0, 18.0);
        
        CalcUtils.snapPointToGrid(point, gridSpacing);
        
        assertEquals(10.0, point.getX(), 0.001);
        assertEquals(20.0, point.getY(), 0.001);
        
        // Test with point exactly on grid
        point.setLocation(20.0, 30.0);
        CalcUtils.snapPointToGrid(point, gridSpacing);
        assertEquals(20.0, point.getX(), 0.001);
        assertEquals(30.0, point.getY(), 0.001);
        
        // Test with different grid spacing
        Size finerGrid = new Size(5.0, SizeUnit.px);
        point.setLocation(13.0, 28.0);
        CalcUtils.snapPointToGrid(point, finerGrid);
        assertEquals(15.0, point.getX(), 0.001);
        assertEquals(30.0, point.getY(), 0.001);
    }

    @Test
    public void testFindClosestMultiplierOf() {
        Size factor = new Size(1.0, SizeUnit.mm);
        Size target = new Size(3.5, SizeUnit.mm);
        int step = 1;
        
        Size result = CalcUtils.findClosestMultiplierOf(factor, target, step);
        assertEquals(4.0, result.getValue(), 0.001);
        assertEquals(SizeUnit.mm, result.getUnit());
        
        // Test with different step
        result = CalcUtils.findClosestMultiplierOf(factor, target, 2);
        assertEquals(4.0, result.getValue(), 0.001);
        
        // Test with different factor
        factor = new Size(0.5, SizeUnit.mm);
        result = CalcUtils.findClosestMultiplierOf(factor, target, step);
        assertEquals(3.5, result.getValue(), 0.001);
    }

    @Test
    public void testSnapPointToObjects() {
        Size gridSpacing = new Size(10.0, SizeUnit.px);
        Point2D point = new Point2D.Double(12.0, 18.0);
        
        // Create mock components
        IDIYComponent<?> component1 = mock(IDIYComponent.class);
        IDIYComponent<?> component2 = mock(IDIYComponent.class);
        
        // Setup component1 with a sticky point near our test point
        when(component1.getControlPointCount()).thenReturn(1);
        when(component1.isControlPointSticky(0)).thenReturn(true);
        when(component1.getControlPoint(0)).thenReturn(new Point2D.Double(10.0, 20.0));
        
        // Setup component2 with a non-sticky point
        when(component2.getControlPointCount()).thenReturn(1);
        when(component2.isControlPointSticky(0)).thenReturn(false);
        when(component2.getControlPoint(0)).thenReturn(new Point2D.Double(11.0, 19.0));
        
        List<IDIYComponent<?>> components = Arrays.asList(component1, component2);
        
        // Test snapping to sticky point
        CalcUtils.snapPointToObjects(point, gridSpacing, null, components);
        assertEquals(10.0, point.getX(), 0.001);
        assertEquals(20.0, point.getY(), 0.001);
        
        // Test not snapping when point is too far
        point.setLocation(25.0, 25.0);
        CalcUtils.snapPointToObjects(point, gridSpacing, null, components);
        assertEquals(25.0, point.getX(), 0.001);
        assertEquals(25.0, point.getY(), 0.001);
        
        // Test not snapping to non-sticky points
        point.setLocation(15.0, 23.0);  // Changed coordinates to be further from sticky point
        CalcUtils.snapPointToObjects(point, gridSpacing, null, components);
        assertEquals(15.0, point.getX(), 0.001);
        assertEquals(23.0, point.getY(), 0.001);
    }

    @Test
    public void testPointsMatch() {
        Point2D point1 = new Point2D.Double(10.0, 20.0);
        Point2D point2 = new Point2D.Double(10.0, 20.0);
        
        // Test exact match
        assertTrue(CalcUtils.pointsMatch(point1, point2, 0.001));
        
        // Test within delta
        point2.setLocation(10.1, 20.1);
        assertTrue(CalcUtils.pointsMatch(point1, point2, 0.2));
        
        // Test outside delta
        assertFalse(CalcUtils.pointsMatch(point1, point2, 0.05));
        
        // Test x coordinate outside delta
        point2.setLocation(11.0, 20.0);
        assertFalse(CalcUtils.pointsMatch(point1, point2, 0.5));
        
        // Test y coordinate outside delta
        point2.setLocation(10.0, 21.0);
        assertFalse(CalcUtils.pointsMatch(point1, point2, 0.5));
    }
}
