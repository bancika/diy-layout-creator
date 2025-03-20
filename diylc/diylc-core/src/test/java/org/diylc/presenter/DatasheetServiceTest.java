package org.diylc.presenter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DatasheetServiceTest {

    private DatasheetService service;
    
    @Mock
    private ClassLoader mockClassLoader;
    
    @Before
    public void setUp() {
        service = DatasheetService.getInstance();
        service.clearCache();  // Clear cache before each test
        Thread.currentThread().setContextClassLoader(mockClassLoader);
    }
    
    @Test
    public void testLoadDatasheet() throws Exception {
        // Create a mock datasheet content
        String datasheetContent = 
            "1N4001,50V,1A,1.1V\n" +
            "1N4002,100V,1A,1.1V\n" +
            "1N4003,200V,1A,1.1V";
            
        // Create a mock URL that returns our test content
        URL mockUrl = mock(URL.class);
        when(mockUrl.openStream()).thenReturn(
            new ByteArrayInputStream(datasheetContent.getBytes()));
            
        // Setup the mock ClassLoader to return our mock URL
        when(mockClassLoader.getResource("TestComponent.datasheet"))
            .thenReturn(mockUrl);
            
        // Test loading the datasheet
        List<String[]> result = service.loadDatasheet(TestComponent.class);
        
        // Verify results
        assertNotNull("Datasheet should not be null", result);
        assertEquals("Should have 3 entries", 3, result.size());
        
        // Verify first entry
        String[] firstEntry = result.get(0);
        assertEquals("1N4001", firstEntry[0]);
        assertEquals("50V", firstEntry[1]);
        assertEquals("1A", firstEntry[2]);
        assertEquals("1.1V", firstEntry[3]);
        
        // Verify second entry
        String[] secondEntry = result.get(1);
        assertEquals("1N4002", secondEntry[0]);
        assertEquals("100V", secondEntry[1]);
        
        // Verify third entry
        String[] thirdEntry = result.get(2);
        assertEquals("1N4003", thirdEntry[0]);
        assertEquals("200V", thirdEntry[1]);
    }
    
    @Test
    public void testLoadDatasheetFileNotFound() {
        // Setup the mock ClassLoader to return null (file not found)
        when(mockClassLoader.getResource("TestComponent.datasheet"))
            .thenReturn(null);
            
        List<String[]> result = service.loadDatasheet(TestComponent.class);
        
        assertNull("Result should be null when file is not found", result);
    }
    
    @Test
    public void testLookupExactMatch() throws Exception {
        // Create a mock datasheet content
        String datasheetContent = 
            "TEST,KEY,10.0\n" +
            "TEST,KEY,20.0\n" +
            "TEST,OTHER,15.0\n" +
            "OTHER,KEY,10.0";
            
        URL mockUrl = mock(URL.class);
        when(mockUrl.openStream()).thenReturn(
            new ByteArrayInputStream(datasheetContent.getBytes()));
        when(mockClassLoader.getResource("DatasheetServiceTest.datasheet"))
            .thenReturn(mockUrl);
        
        // Create test data
        String[] result = service.lookup(
            DatasheetServiceTest.class,  // Using test class itself as component class
            0.1,                         // 0.1% tolerance
            "TEST|KEY",                  // Key to look up
            10.0                         // Value to match
        );
        
        assertNotNull("Should find matching entry", result);
        assertEquals("TEST", result[0]);
        assertEquals("KEY", result[1]);
        assertEquals("10.0", result[2]);
    }
    
    @Test
    public void testLookupWithinTolerance() throws Exception {
        // Create a mock datasheet content
        String datasheetContent = 
            "TEST,KEY,10.0\n" +
            "TEST,KEY,20.0\n" +
            "TEST,OTHER,15.0\n" +
            "OTHER,KEY,10.0";
            
        URL mockUrl = mock(URL.class);
        when(mockUrl.openStream()).thenReturn(
            new ByteArrayInputStream(datasheetContent.getBytes()));
        when(mockClassLoader.getResource("DatasheetServiceTest.datasheet"))
            .thenReturn(mockUrl);
            
        // Should match 10.0 with 5% tolerance when looking for 10.2
        String[] result = service.lookup(
            DatasheetServiceTest.class,
            5.0,                         // 5% tolerance
            "TEST|KEY",
            10.2                         // Should match 10.0 within 5% tolerance
        );
        
        assertNotNull("Should find matching entry within tolerance", result);
        assertEquals("10.0", result[2]);
    }
    
    @Test
    public void testLookupNoMatch() throws Exception {
        // Create a mock datasheet content
        String datasheetContent = 
            "TEST,KEY,10.0\n" +
            "TEST,KEY,20.0\n" +
            "TEST,OTHER,15.0\n" +
            "OTHER,KEY,10.0";
            
        URL mockUrl = mock(URL.class);
        when(mockUrl.openStream()).thenReturn(
            new ByteArrayInputStream(datasheetContent.getBytes()));
        when(mockClassLoader.getResource("DatasheetServiceTest.datasheet"))
            .thenReturn(mockUrl);
            
        // Should not match when value is outside tolerance
        String[] result = service.lookup(
            DatasheetServiceTest.class,
            1.0,                         // 1% tolerance
            "TEST|KEY",
            15.0                         // No value close enough to match
        );
        
        assertNull("Should not find matching entry", result);
    }
    
    @Test
    public void testLookupNonexistentKey() throws Exception {
        // Create a mock datasheet content
        String datasheetContent = 
            "TEST,KEY,10.0\n" +
            "TEST,KEY,20.0\n" +
            "TEST,OTHER,15.0\n" +
            "OTHER,KEY,10.0";
            
        URL mockUrl = mock(URL.class);
        when(mockUrl.openStream()).thenReturn(
            new ByteArrayInputStream(datasheetContent.getBytes()));
        when(mockClassLoader.getResource("DatasheetServiceTest.datasheet"))
            .thenReturn(mockUrl);
            
        String[] result = service.lookup(
            DatasheetServiceTest.class,
            0.1,
            "NONEXISTENT|KEY",           // Key that doesn't exist in datasheet
            10.0
        );
        
        assertNull("Should not find matching entry for nonexistent key", result);
    }
    
    @Test
    public void testLookupWithTolerance() throws Exception {
        // Create a mock datasheet content with resistor values
        String datasheetContent = 
            "0805,1%,100,0.125W\n" +
            "0805,1%,220,0.125W\n" +
            "0805,1%,470,0.125W";
            
        URL mockUrl = mock(URL.class);
        when(mockUrl.openStream()).thenReturn(
            new ByteArrayInputStream(datasheetContent.getBytes()));
        when(mockClassLoader.getResource("TestComponent.datasheet"))
            .thenReturn(mockUrl);
            
        // Test lookup with 5% tolerance (should match 220 when looking for 225)
        String[] result = service.lookup(TestComponent.class, 5.0, "0805|1%", 225.0);
        
        assertNotNull("Should find matching entry within tolerance", result);
        assertEquals("0805", result[0]);
        assertEquals("1%", result[1]);
        assertEquals("220", result[2]);
    }
    
    @Test
    public void testLookupOutsideTolerance() throws Exception {
        // Create a mock datasheet content
        String datasheetContent = 
            "0805,1%,100,0.125W\n" +
            "0805,1%,220,0.125W\n" +
            "0805,1%,470,0.125W";
            
        URL mockUrl = mock(URL.class);
        when(mockUrl.openStream()).thenReturn(
            new ByteArrayInputStream(datasheetContent.getBytes()));
        when(mockClassLoader.getResource("TestComponent.datasheet"))
            .thenReturn(mockUrl);
            
        // Test lookup with value outside tolerance
        String[] result = service.lookup(TestComponent.class, 1.0, "0805|1%", 250.0);
        
        assertNull("Should not find matching entry outside tolerance", result);
    }
    
    @Test
    public void testLookupMultipleValues() throws Exception {
        // Create a mock datasheet content with multiple value matches
        String datasheetContent = 
            "TO220,NPN,50,5,0.7\n" +  // Vce, Ic, Vbe
            "TO220,NPN,60,8,0.7\n" +
            "TO220,NPN,100,10,0.7";
            
        URL mockUrl = mock(URL.class);
        when(mockUrl.openStream()).thenReturn(
            new ByteArrayInputStream(datasheetContent.getBytes()));
        when(mockClassLoader.getResource("TestComponent.datasheet"))
            .thenReturn(mockUrl);
            
        // Test lookup matching multiple values within tolerance
        String[] result = service.lookup(TestComponent.class, 5.0, "TO220|NPN", 
                                       98.0, 9.8, 0.71);
        
        assertNotNull("Should find matching entry for multiple values", result);
        assertEquals("TO220", result[0]);
        assertEquals("NPN", result[1]);
        assertEquals("100", result[2]);
        assertEquals("10", result[3]);
        assertEquals("0.7", result[4]);
    }
    
    // Test component class
    private static class TestComponent {
        // Empty class just for testing
    }
} 