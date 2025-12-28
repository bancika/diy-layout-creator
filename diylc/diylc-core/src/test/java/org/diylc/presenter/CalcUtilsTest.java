package org.diylc.presenter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.awt.Color;
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

    @Test
    public void testCalculateLuminance() {
        // Test pure black
        assertEquals(0.0, CalcUtils.calculateLuminance(Color.BLACK), 0.001);
        
        // Test pure white
        assertEquals(255.0, CalcUtils.calculateLuminance(Color.WHITE), 0.001);
        
        // Test pure red
        double redLuminance = CalcUtils.calculateLuminance(Color.RED);
        assertEquals(76.245, redLuminance, 0.001); // 0.299 * 255
        
        // Test pure green
        double greenLuminance = CalcUtils.calculateLuminance(Color.GREEN);
        assertEquals(149.685, greenLuminance, 0.001); // 0.587 * 255
        
        // Test pure blue
        double blueLuminance = CalcUtils.calculateLuminance(Color.BLUE);
        assertEquals(29.07, blueLuminance, 0.001); // 0.114 * 255
        
        // Test middle gray (128, 128, 128)
        Color gray = new Color(128, 128, 128);
        double grayLuminance = CalcUtils.calculateLuminance(gray);
        assertEquals(128.0, grayLuminance, 0.001);
        
        // Test null color
        assertEquals(0.0, CalcUtils.calculateLuminance(null), 0.001);
        
        // Test custom color
        Color custom = new Color(100, 150, 200);
        double customLuminance = CalcUtils.calculateLuminance(custom);
        double expected = 0.299 * 100 + 0.587 * 150 + 0.114 * 200;
        assertEquals(expected, customLuminance, 0.001);
    }

    @Test
    public void testConvertToMonochrome() {
        // Test pure black -> should stay black
        Color result = CalcUtils.convertToMonochrome(Color.BLACK);
        assertEquals(Color.BLACK, result);
        
        // Test pure white -> should stay white
        result = CalcUtils.convertToMonochrome(Color.WHITE);
        assertEquals(Color.WHITE, result);
        
        // Test dark color -> should become black
        Color darkBlue = new Color(0, 0, 50);
        result = CalcUtils.convertToMonochrome(darkBlue);
        assertEquals(Color.BLACK, result);
        
        // Test light color -> should become white
        Color lightYellow = new Color(255, 255, 200);
        result = CalcUtils.convertToMonochrome(lightYellow);
        assertEquals(Color.WHITE, result);
        
        // Test middle gray -> should become white (threshold is 128, gray is exactly 128)
        Color gray = new Color(128, 128, 128);
        result = CalcUtils.convertToMonochrome(gray);
        assertEquals(Color.WHITE, result);
        
        // Test color just below threshold -> should become black
        Color darkGray = new Color(127, 127, 127);
        result = CalcUtils.convertToMonochrome(darkGray);
        assertEquals(Color.BLACK, result);
        
        // Test color just above threshold -> should become white
        Color lightGray = new Color(129, 129, 129);
        result = CalcUtils.convertToMonochrome(lightGray);
        assertEquals(Color.WHITE, result);
        
        // Test with custom threshold
        Color mediumColor = new Color(100, 100, 100);
        result = CalcUtils.convertToMonochrome(mediumColor, 50.0);
        assertEquals(Color.WHITE, result); // Luminance ~100, threshold 50, so white
        
        result = CalcUtils.convertToMonochrome(mediumColor, 150.0);
        assertEquals(Color.BLACK, result); // Luminance ~100, threshold 150, so black
        
        // Test null color
        assertNull(CalcUtils.convertToMonochrome(null));
    }

    @Test
    public void testConvertToMonochromeWithAlpha() {
        // Test color with alpha channel - alpha should be preserved
        Color transparentRed = new Color(255, 0, 0, 128);
        Color result = CalcUtils.convertToMonochrome(transparentRed);
        assertEquals(Color.WHITE.getRed(), result.getRed());
        assertEquals(Color.WHITE.getGreen(), result.getGreen());
        assertEquals(Color.WHITE.getBlue(), result.getBlue());
        assertEquals(128, result.getAlpha());
        
        // Test fully transparent color
        Color transparent = new Color(100, 150, 200, 0);
        result = CalcUtils.convertToMonochrome(transparent);
        assertEquals(0, result.getAlpha());
        
        // Test opaque color - alpha should remain 255
        Color opaque = new Color(50, 50, 50);
        result = CalcUtils.convertToMonochrome(opaque);
        assertEquals(255, result.getAlpha());
    }
}
