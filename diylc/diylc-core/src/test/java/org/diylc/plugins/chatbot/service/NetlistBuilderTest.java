//package org.diylc.plugins.chatbot.service;
//
//import org.diylc.core.IContinuity;
//import org.diylc.core.IDIYComponent;
//import org.diylc.core.ComponentState;
//import org.diylc.core.IDrawingObserver;
//import org.diylc.core.Project;
//import org.diylc.core.VisibilityPolicy;
//import org.diylc.netlist.Node;
//import org.diylc.presenter.ContinuityArea;
//import org.junit.Test;
//import org.diylc.components.AbstractComponent;
//
//import java.awt.Graphics2D;
//import java.awt.geom.Area;
//import java.awt.geom.Point2D;
//import java.util.*;
//
//import static org.junit.Assert.*;
//
//public class NetlistBuilderTest {
//
//    // Mock component that implements IContinuity
//    private static class MockConductiveComponent extends AbstractComponent<Void> implements IContinuity {
//        private final Point2D[] points;
//        private final boolean[][] connections;
//
//        public MockConductiveComponent(Point2D[] points, boolean[][] connections) {
//            this.points = points;
//            this.connections = connections;
//        }
//
//        @Override
//        public int getControlPointCount() {
//            return points.length;
//        }
//
//        @Override
//        public Point2D getControlPoint(int index) {
//            return points[index];
//        }
//
//        @Override
//        public boolean isControlPointSticky(int index) {
//            return true;
//        }
//
//        @Override
//        public boolean arePointsConnected(int index1, int index2) {
//            return connections[index1][index2];
//        }
//
//        @Override
//        public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project, IDrawingObserver drawingObserver) {
//            // Not needed for testing
//        }
//
//        @Override
//        public void drawIcon(Graphics2D g2d, int width, int height) {
//            // Not needed for testing
//        }
//
//        @Override
//        public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
//            return VisibilityPolicy.ALWAYS;
//        }
//
//        @Override
//        public void setControlPoint(Point2D point, int index) {
//            // Not needed for testing
//        }
//
//        @Override
//        public Void getValue() {
//            return null;
//        }
//
//        @Override
//        public void setValue(Void value) {
//            // Not needed for testing
//        }
//    }
//
//    // Mock component that doesn't implement IContinuity
//    private static class MockNonConductiveComponent extends AbstractComponent<Void> {
//        private final Point2D[] points;
//
//        public MockNonConductiveComponent(Point2D[] points) {
//            this.points = points;
//        }
//
//        @Override
//        public int getControlPointCount() {
//            return points.length;
//        }
//
//        @Override
//        public Point2D getControlPoint(int index) {
//            return points[index];
//        }
//
//        @Override
//        public boolean isControlPointSticky(int index) {
//            return true;
//        }
//
//        @Override
//        public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project, IDrawingObserver drawingObserver) {
//            // Not needed for testing
//        }
//
//        @Override
//        public void drawIcon(Graphics2D g2d, int width, int height) {
//            // Not needed for testing
//        }
//
//        @Override
//        public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
//            return VisibilityPolicy.ALWAYS;
//        }
//
//        @Override
//        public void setControlPoint(Point2D point, int index) {
//            // Not needed for testing
//        }
//
//        @Override
//        public Void getValue() {
//            return null;
//        }
//
//        @Override
//        public void setValue(Void value) {
//            // Not needed for testing
//        }
//    }
//
//    @Test
//    public void testBuildNets() {
//        // Create test components
//        Point2D[] points1 = {
//            new Point2D.Double(0, 0),
//            new Point2D.Double(10, 0),
//            new Point2D.Double(20, 0)
//        };
//        boolean[][] connections1 = {
//            {false, true, false},
//            {true, false, true},
//            {false, true, false}
//        };
//        MockConductiveComponent comp1 = new MockConductiveComponent(points1, connections1);
//
//        Point2D[] points2 = {
//            new Point2D.Double(30, 0),
//            new Point2D.Double(40, 0)
//        };
//        MockNonConductiveComponent comp2 = new MockNonConductiveComponent(points2);
//
//        // Create a continuity area that connects comp1 and comp2
//        Area area = new Area();
//        area.add(new Area(new java.awt.geom.Rectangle2D.Double(15, -5, 20, 10)));
//        ContinuityArea continuityArea = new ContinuityArea(0, area);
//
//        // Build nets
//        List<IDIYComponent<?>> components = Arrays.asList(comp1, comp2);
//        List<ContinuityArea> continuityAreas = Collections.singletonList(continuityArea);
//        List<Set<Node>> nets = NetlistBuilder.buildNets(components, continuityAreas);
//
//        // Verify results
//        assertEquals("Should have exactly one net", 1, nets.size());
//        Set<Node> net = nets.get(0);
//        assertEquals("Net should contain all terminals", 5, net.size());
//
//        // Verify that all terminals are connected
//        Set<Integer> terminalIndices = new HashSet<>();
//        for (Node node : net) {
//            terminalIndices.add(node.getPointIndex());
//            System.out.println("Found terminal: " + node.getPointIndex() + " at " + node.getPoint2D());
//        }
//        System.out.println("All terminals: " + terminalIndices);
//        assertEquals("Should contain all terminal indices", 5, terminalIndices.size());
//        assertTrue("Should contain terminal 0", terminalIndices.contains(0));
//        assertTrue("Should contain terminal 1", terminalIndices.contains(1));
//        assertTrue("Should contain terminal 2", terminalIndices.contains(2));
//        assertTrue("Should contain terminal 3", terminalIndices.contains(3));
//        assertTrue("Should contain terminal 4", terminalIndices.contains(4));
//    }
//}
