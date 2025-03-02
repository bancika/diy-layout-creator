package org.diylc.netlist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

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
}
