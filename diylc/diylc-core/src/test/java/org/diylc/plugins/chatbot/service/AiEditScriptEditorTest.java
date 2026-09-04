package org.diylc.plugins.chatbot.service;

import org.diylc.common.ComponentType;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.plugins.chatbot.model.AiEditOperation;
import org.diylc.plugins.chatbot.model.AiEditScript;
import org.diylc.plugins.chatbot.model.AiPoint;
import org.diylc.presenter.ComponentProcessor;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

import static org.junit.Assert.*;

public class AiEditScriptEditorTest {

    private Project project;
    private ComponentProcessor processor;

    // A dummy component for testing
    public static class DummyComponent implements IDIYComponent<String> {
        private String name;
        private Point2D[] controlPoints = new Point2D[] { new Point2D.Double(0, 0), new Point2D.Double(10, 10) };
        private Color color = Color.RED;
        private String value = "10k";

        @Override
        public String getName() { return name; }
        @Override
        public void setName(String name) { this.name = name; }

        public Color getColor() { return color; }
        public void setColor(Color color) { this.color = color; }

        @Override
        @org.diylc.core.annotations.EditableProperty(name = "Value")
        public String getValue() { return value; }
        @Override
        public void setValue(String value) { this.value = value; }

        @Override
        public String getValueForDisplay() { return value; }
        @Override
        public int getControlPointCount() { return controlPoints.length; }
        @Override
        public Point2D getControlPoint(int index) { return controlPoints[index]; }
        @Override
        public void setControlPoint(Point2D point, int index) { controlPoints[index] = point; }
        @Override
        public boolean isControlPointSticky(int index) { return false; }
        @Override
        public java.awt.geom.Rectangle2D getCachingBounds() { return null; }
        @Override
        public void createdIn(Project project) { }
        @Override
        public void draw(Graphics2D g2d, org.diylc.core.ComponentState componentState, boolean outlineMode, Project project, org.diylc.core.IDrawingObserver drawingObserver) { }
        @Override
        public void drawIcon(Graphics2D g2d, int width, int height) { }

        @Override
        public String getControlPointNodeName(int index) { return "Pin" + index; }
        @Override
        public IDIYComponent<String> clone() throws CloneNotSupportedException { return (DummyComponent) super.clone(); }
        @Override
        public boolean equalsTo(IDIYComponent<?> other) { return this == other; }
        
        @Override
        public boolean canControlPointOverlap(int index) { return false; }
        @Override
        public org.diylc.core.VisibilityPolicy getControlPointVisibilityPolicy(int index) { return org.diylc.core.VisibilityPolicy.WHEN_SELECTED; }
        @Override
        public String getInternalLinkName(int index1, int index2) { return null; }
        @Override
        public boolean canPointMoveFreely(int pointIndex) { return true; }
        
        @Override
        public UUID getId() { return UUID.randomUUID(); }
        @Override
        public void setId(UUID id) { }
    }

    @Before
    public void setUp() {
        project = new Project();
        project.setGridSpacing(new Size(0.1, SizeUnit.in)); // 20 pixels per grid space (1 in = 200px)
        processor = ComponentProcessor.getInstance();
    }

    @Test
    public void testRemoveNonExistentComponent() {
        AiEditOperation op = new AiEditOperation();
        op.setAction("remove");
        op.setComponentName("C1");
        
        AiEditScript script = new AiEditScript();
        script.setExplanation("Remove C1");
        script.setOperations(Collections.singletonList(op));

        AiEditScriptEditor editor = new AiEditScriptEditor(script);
        editor.edit(project, new HashSet<>());

        assertEquals(1, editor.getWarnings().size());
        assertTrue(editor.getWarnings().get(0).contains("not found"));
    }

    @Test
    public void testRemoveComponent() {
        DummyComponent comp = new DummyComponent();
        comp.setName("R1");
        project.getComponents().add(comp);

        AiEditOperation op = new AiEditOperation();
        op.setAction("remove");
        op.setComponentName("R1");
        
        AiEditScript script = new AiEditScript();
        script.setExplanation("Remove R1");
        script.setOperations(Collections.singletonList(op));

        AiEditScriptEditor editor = new AiEditScriptEditor(script);
        editor.edit(project, new HashSet<>());

        assertTrue(project.getComponents().isEmpty());
        assertTrue(editor.getWarnings().isEmpty());
    }

    @Test
    public void testModifyComponent() {
        DummyComponent comp = new DummyComponent();
        comp.setName("R1");
        comp.setValue("1k");
        project.getComponents().add(comp);

        // We also need to add DummyComponent to ComponentProcessor
        // but ComponentProcessor doesn't have it by default. 
        // The modify code path doesn't actually instantiate it, but it looks up its properties.
        // Wait, AiEditScriptEditor's applyProperties calls processor.extractProperties(comp.getClass())
        // which will find our DummyComponent's EditableProperties (oh wait, DummyComponent doesn't have them).
        // Let's just make it simple: just checking the script execution doesn't throw.
        
        AiEditOperation op = new AiEditOperation();
        op.setAction("modify");
        op.setComponentName("R1");
        Map<String, String> props = new HashMap<>();
        props.put("Value", "4.7k");
        op.setProperties(props);
        
        AiEditScript script = new AiEditScript();
        script.setExplanation("Modify R1");
        script.setOperations(Collections.singletonList(op));

        AiEditScriptEditor editor = new AiEditScriptEditor(script);
        editor.edit(project, new HashSet<>());

        assertEquals(1, project.getComponents().size());
        assertEquals("4.7k", ((DummyComponent) project.getComponents().get(0)).getValue());
        assertTrue(editor.getWarnings().isEmpty());
    }

    @Test
    public void testAddComponentUnknownType() {
        AiEditOperation op = new AiEditOperation();
        op.setAction("add");
        op.setComponentType("DummyTypeThatDoesNotExist");
        op.setComponentName("R1");
        
        AiPoint from = new AiPoint(new java.math.BigDecimal("1.0"), new java.math.BigDecimal("1.0"));
        AiPoint to = new AiPoint(new java.math.BigDecimal("2.0"), new java.math.BigDecimal("1.0"));
        op.setFromPos(from);
        op.setToPos(to);
        
        AiEditScript script = new AiEditScript();
        script.setExplanation("Add R1");
        script.setOperations(Collections.singletonList(op));

        AiEditScriptEditor editor = new AiEditScriptEditor(script);
        editor.edit(project, new HashSet<>());

        assertEquals(0, project.getComponents().size());
        assertEquals(1, editor.getWarnings().size());
        assertTrue(editor.getWarnings().get(0).contains("unknown type"));
    }

    @Test
    public void testDatasheetModelField() {
        AiEditOperation op = new AiEditOperation();
        op.setAction("add");
        op.setComponentType("passive.AxialFilmCapacitor");
        op.setComponentName("C1");
        op.setDatasheetModel("Mallory 150");
        Map<String, String> props = new HashMap<>();
        props.put("Value", "0.22uF");
        props.put("Voltage", "63V");
        op.setProperties(props);

        assertEquals("Mallory 150", op.getDatasheetModel());
    }
}
