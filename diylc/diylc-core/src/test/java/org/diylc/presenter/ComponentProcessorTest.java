package org.diylc.presenter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thoughtworks.xstream.XStream;
import org.diylc.common.ComponentType;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.PropertyWrapper;
import org.diylc.components.AbstractComponent;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.junit.Before;
import org.junit.Test;

public class ComponentProcessorTest {

    private ComponentProcessor processor;

    @Before
    public void setUp() {
        processor = ComponentProcessor.getInstance();
    }

    @ComponentDescriptor(
        name = "Test Component", 
        category = "Test", 
        author = "Test Author", 
        description = "Test Description",
        instanceNamePrefix = "TEST",
        creationMethod = CreationMethod.POINT_BY_POINT,
        zOrder = IDIYComponent.COMPONENT,
        flexibleZOrder = false,
        bomPolicy = BomPolicy.NEVER_SHOW,
        autoEdit = false,
        enableCache = true,
        keywordPolicy = KeywordPolicy.SHOW_VALUE,
        keywordTag = "",
        enableDatasheet = false,
        datasheetCreationStepCount = 0
    )
    public static class TestComponent extends AbstractComponent<Object> {
        private String testProperty = "test";
        private int numericProperty = 42;

        @EditableProperty
        public String getTestProperty() {
            return testProperty;
        }

        public void setTestProperty(String value) {
            this.testProperty = value;
        }

        @EditableProperty(name = "Number")
        public int getNumericProperty() {
            return numericProperty;
        }

        public void setNumericProperty(int value) {
            this.numericProperty = value;
        }

        // Implement required methods
        @Override
        public void drawIcon(java.awt.Graphics2D g2d, int width, int height) {}

        @Override
        public void draw(java.awt.Graphics2D g2d, org.diylc.core.ComponentState componentState,
                boolean outlineMode, org.diylc.core.Project project,
                org.diylc.core.IDrawingObserver drawingObserver) {}

        @Override
        public Point2D getControlPoint(int index) {
            return new Point(0, 0);
        }

        @Override
        public boolean isControlPointSticky(int index) {
            return true;
        }

        @Override
        public int getControlPointCount() {
            return 1;
        }

        @Override
        public void setControlPoint(Point2D point, int index) {}

        @Override
        public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
            return VisibilityPolicy.WHEN_SELECTED;
        }

        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public void setValue(Object value) {}
    }

    @Test
    public void testExtractComponentType() {
        ComponentType type = processor.extractComponentTypeFrom(TestComponent.class);
        
        assertNotNull("Component type should not be null", type);
        assertEquals("Test Component", type.getName());
        assertEquals("Test", type.getCategory());
        assertEquals("Test Author", type.getAuthor());
        assertEquals("Test Description", type.getDescription());
        assertEquals("TEST", type.getNamePrefix());
        assertEquals(TestComponent.class, type.getInstanceClass());
    }

    @Test
    public void testExtractProperties() {
        List<PropertyWrapper> properties = processor.extractProperties(TestComponent.class);
        
        assertNotNull("Properties list should not be null", properties);
        assertEquals("Should have 3 properties", 3, properties.size());
        
        // Find and verify testProperty
        PropertyWrapper testProp = findPropertyByName(properties, "TestProperty");
        assertNotNull("TestProperty should exist", testProp);
        assertEquals(String.class, testProp.getType());
        
        // Find and verify numericProperty
        PropertyWrapper numProp = findPropertyByName(properties, "Number");
        assertNotNull("Number property should exist", numProp);
        assertEquals(int.class, numProp.getType());

        // Find and verify name property from AbstractComponent
        PropertyWrapper nameProp = findPropertyByName(properties, "Name");
        assertNotNull("Name property should exist", nameProp);
        assertEquals(String.class, nameProp.getType());
    }

    @Test
    public void testGetMutualSelectionProperties() throws Exception {
        TestComponent comp1 = new TestComponent();
        TestComponent comp2 = new TestComponent();
        
        // Set same values
        comp1.setTestProperty("same");
        comp2.setTestProperty("same");
        comp1.setNumericProperty(42);
        comp2.setNumericProperty(42);
        comp1.setName("same name");
        comp2.setName("same name");
        
        List<PropertyWrapper> mutualProps = processor.getMutualSelectionProperties(
            Arrays.asList(comp1, comp2));
        
        assertNotNull("Mutual properties should not be null", mutualProps);
        assertEquals("Should have 3 mutual properties", 3, mutualProps.size());
        assertTrue("All properties should be unique", 
            mutualProps.stream().allMatch(PropertyWrapper::isUnique));
    }

    @Test
    public void testGetMutualSelectionPropertiesWithDifferentValues() throws Exception {
        TestComponent comp1 = new TestComponent();
        TestComponent comp2 = new TestComponent();
        
        // Set different values
        comp1.setTestProperty("value1");
        comp2.setTestProperty("value2");
        comp1.setNumericProperty(42);
        comp2.setNumericProperty(43);
        comp1.setName("name1");
        comp2.setName("name2");
        
        List<PropertyWrapper> mutualProps = processor.getMutualSelectionProperties(
            Arrays.asList(comp1, comp2));
        
        assertNotNull("Mutual properties should not be null", mutualProps);
        assertEquals("Should have 3 mutual properties", 3, mutualProps.size());
        
        // Each property should not be unique since they have different values
        for (PropertyWrapper prop : mutualProps) {
            assertFalse("Property '" + prop.getName() + "' should not be unique", 
                prop.isUnique());
        }
    }

    @Test
    public void testComponentPointsTouch() {
        TestComponent comp1 = new TestComponent();
        TestComponent comp2 = new TestComponent();
        
        assertTrue("Components with same point should touch",
            ComponentProcessor.componentPointsTouch(comp1, comp2));
    }

    @Test
    public void testHasStickyPoint() {
        TestComponent component = new TestComponent();
        assertTrue("Should have sticky point", ComponentProcessor.hasStickyPoint(component));
    }

    private PropertyWrapper findPropertyByName(List<PropertyWrapper> properties, String name) {
        return properties.stream()
            .filter(p -> p.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
} 
