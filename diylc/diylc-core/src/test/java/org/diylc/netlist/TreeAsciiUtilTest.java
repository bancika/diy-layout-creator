package org.diylc.netlist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.diylc.netlist.TreeAsciiUtil;

public class TreeAsciiUtilTest {

  @Test
  public void testConcatenateMultiLineSerial() {
    List<String> result = TreeAsciiUtil.concatenateMultiLineSerial("-",
        Arrays.asList(Arrays.asList("xx", "yy"), Arrays.asList("xxx", "yyy", "aaa")));
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("xx&nbsp;xxx", result.get(0));
    assertEquals("yy-yyy", result.get(1));
    assertEquals("&nbsp;&nbsp;&nbsp;aaa", result.get(2));
  }

  @Test
  public void testConcatenateMultiLineSerial_singleInput() {
    List<List<String>> elementList = Arrays.asList(Arrays.asList("xx", "yy"));
    List<String> result = TreeAsciiUtil.concatenateMultiLineSerial("-", elementList);
    assertNotNull(result);
    assertEquals(Arrays.asList("xx", "yy"), result);
  }
  
  @Test
  public void testConcatenateMultiLineParallel() {
    List<String> result = TreeAsciiUtil.concatenateMultiLineParallel(
        Arrays.asList(Arrays.asList("xx", "yy"), Arrays.asList("xxx", "yyy", "aaa")));
    
    System.out.println("<font face=\"Courier New\">" + String.join("<br>", result) +"</font>");
    assertNotNull(result);
//    assertEquals(3, result.size());
//    assertEquals("xx&nbsp;xxx", result.get(0));
//    assertEquals("yy-yyy", result.get(1));
//    assertEquals("&nbsp;&nbsp;&nbsp;aaa", result.get(2));
  }
  
  @Test
  public void testConcatenateMultiLineParallel_4_lines() {
    List<String> result = TreeAsciiUtil.concatenateMultiLineParallel(
        Arrays.asList(Arrays.asList("xx"), Arrays.asList("xx"), Arrays.asList("xx"), Arrays.asList("xx")));
    
    System.out.println("<font face=\"Courier New\">" + String.join("<br>", result) +"</font>");
    assertNotNull(result);
//    assertEquals(3, result.size());
//    assertEquals("xx&nbsp;xxx", result.get(0));
//    assertEquals("yy-yyy", result.get(1));
//    assertEquals("&nbsp;&nbsp;&nbsp;aaa", result.get(2));
  }

  @Test
  public void testSimpleParallel() {
    List<String> result = TreeAsciiUtil.concatenateMultiLineParallel(
        Arrays.asList(Arrays.asList("xx"), Arrays.asList("yy")));
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("&nbsp;&#x250C;xx&#x2510;&nbsp;", result.get(0));
    assertEquals("&#x2500;&#x2524;&nbsp;&nbsp;&#x251C;&#x2500;", result.get(1));
    assertEquals("&nbsp;&#x2514;yy&#x2518;&nbsp;", result.get(2));
  }

  @Test
  public void testHtmlLength() {
    assertEquals(3, TreeAsciiUtil.htmlLength("abc"));
    assertEquals(1, TreeAsciiUtil.htmlLength("&nbsp;"));
    assertEquals(1, TreeAsciiUtil.htmlLength("&#x2500;"));
    assertEquals(5, TreeAsciiUtil.htmlLength("a&nbsp;b&#x2500;c"));
    assertEquals(0, TreeAsciiUtil.htmlLength(""));
    assertEquals(0, TreeAsciiUtil.htmlLength(null));
  }
  
  @Test
  public void testGenerateString() {
    assertEquals("", TreeAsciiUtil.generateString("a", 0));
    assertEquals("", TreeAsciiUtil.generateString("a", -1));
    assertEquals("aaa", TreeAsciiUtil.generateString("a", 3));
    assertEquals("&nbsp;&nbsp;", TreeAsciiUtil.generateString("&nbsp;", 2));
  }
  
  @Test
  public void testConcatenateMultiLineSerialSimple() {
    List<String> element1 = Arrays.asList("A", "B", "C");
    List<String> element2 = Arrays.asList("D", "E", "F");
    
    List<List<String>> elements = Arrays.asList(element1, element2);
    List<String> result = TreeAsciiUtil.concatenateMultiLineSerial(" --- ", elements);
    
    assertEquals(3, result.size());
    assertEquals("A&nbsp;D", result.get(0));
    assertEquals("B --- E", result.get(1));
    assertEquals("C&nbsp;F", result.get(2));
  }
  
  @Test
  public void testConcatenateMultiLineSerialDifferentHeights() {
    List<String> element1 = Arrays.asList("A", "B", "C");
    List<String> element2 = Arrays.asList("D");
    
    List<List<String>> elements = Arrays.asList(element1, element2);
    List<String> result = TreeAsciiUtil.concatenateMultiLineSerial(" --- ", elements);
    
    assertEquals(3, result.size());
    assertEquals("A&nbsp;&nbsp;", result.get(0));
    assertEquals("B --- D", result.get(1));
    assertEquals("C&nbsp;&nbsp;", result.get(2));
  }
  
  @Test
  public void testConcatenateMultiLineParallelSimple() {
    List<String> element1 = Arrays.asList("A", "B", "C");
    List<String> element2 = Arrays.asList("D", "E", "F");
    
    List<List<String>> elements = Arrays.asList(element1, element2);
    List<String> result = TreeAsciiUtil.concatenateMultiLineParallel(elements);
    
    assertEquals(7, result.size()); // 3 lines for element1 + 1 separator + 3 lines for element2
  }
  
  @Test
  public void testConcatenateMultiLineParallelEmptyInput() {
    assertEquals(0, TreeAsciiUtil.concatenateMultiLineParallel(Collections.emptyList()).size());
    assertEquals(0, TreeAsciiUtil.concatenateMultiLineParallel(null).size());
  }
  
  @Test
  public void testConcatenateMultiLineParallelSingleElement() {
    List<String> element = Arrays.asList("A", "B");
    List<List<String>> elements = Arrays.asList(element);
    
    List<String> result = TreeAsciiUtil.concatenateMultiLineParallel(elements);
    assertEquals(2, result.size());
    // We just get back the original element
    assertEquals(element.size(), result.size());
  }
}
