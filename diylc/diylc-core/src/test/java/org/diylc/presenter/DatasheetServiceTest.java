package org.diylc.presenter;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DatasheetServiceTest {
    private DatasheetService service;
    private static final Class<?> TEST_CLASS = DatasheetServiceTest.class;

    @Before
    public void setUp() {
        service = DatasheetService.getInstance();
        service.clearCache();
    }

    @Test
    public void testLoadDatasheet() {
        List<String[]> result = service.loadDatasheet(TEST_CLASS);
        assertNotNull(result);
        assertEquals(4, result.size());
        assertArrayEquals(new String[]{"TEST", "KEY", "10.0", "20.0"}, result.get(0));
    }

    @Test
    public void testLoadDatasheetFileNotFound() {
        List<String[]> result = service.loadDatasheet(String.class);
        assertNull(result);
    }

    @Test
    public void testLookupExactMatch() {
        String[] result = service.lookup(TEST_CLASS, 0.0, "TEST|KEY", 10.0);
        assertNotNull(result);
        assertEquals("10.0", result[2]);
    }

    @Test
    public void testLookupWithinTolerance() {
        String[] result = service.lookup(TEST_CLASS, 5.0, "TEST|KEY", 10.5);
        assertNotNull(result);
        assertEquals("10.0", result[2]);
    }

    @Test
    public void testLookupOutsideTolerance() {
        String[] result = service.lookup(TEST_CLASS, 5.0, "TEST|KEY", 12.0);
        assertNull(result);
    }

    @Test
    public void testLookupNoMatch() {
        String[] result = service.lookup(TEST_CLASS, 0.0, "TEST,KEY", 30.0);
        assertNull(result);
    }

    @Test
    public void testLookupNonexistentKey() {
        String[] result = service.lookup(TEST_CLASS, 0.0, "NONEXISTENT,KEY", 10.0);
        assertNull(result);
    }

    @Test
    public void testLookupWithTolerance() {
        String[] result = service.lookup(TEST_CLASS, 50.0, "TEST|KEY", 15.0);
        assertNotNull(result);
        assertEquals("10.0", result[2]);
    }

    @Test
    public void testLookupMultipleValues() {
        String[] result = service.lookup(TEST_CLASS, 0.0, "TEST|KEY", 10.0, 20.0);
        assertNotNull(result);
        assertEquals("10.0", result[2]);
        assertEquals("20.0", result[3]);
    }
} 
