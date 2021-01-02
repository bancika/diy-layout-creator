package org.diylc.netlist;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TangoParserTests {

  @Test
  public void testParse1() {
    String content = "[\r\n" + 
        "R9\r\n" + 
        "RESISTOR\r\n" + 
        "100k\r\n" + 
        "\r\n" + 
        "]\r\n" + 
        "[\r\n" + 
        "D4\r\n" + 
        "LED\r\n" + 
        "LED\r\n" + 
        "\r\n" + 
        "]\r\n" + 
        "[\r\n" + 
        "C3\r\n" + 
        "CAP_FILM\r\n" + 
        "82n\r\n" + 
        "\r\n" + 
        "]\r\n" + 
        "[\r\n" + 
        "C2\r\n" + 
        "CAP_ELECTRO_200\r\n" + 
        "1u\r\n" + 
        "\r\n" + 
        "]\r\n" + 
        "[\r\n" + 
        "C1\r\n" + 
        "CAP_CERAMIC1\r\n" + 
        "820p\r\n" + 
        "\r\n" + 
        "]\r\n" + 
        "[\r\n" + 
        "U2\r\n" + 
        "DIP14\r\n" + 
        "TL074\r\n" + 
        "\r\n" + 
        "]\r\n" + 
        "[\r\n" + 
        "D1\r\n" + 
        "DIODE\r\n" + 
        "Zener 12V\r\n" + 
        "\r\n" + 
        "]\r\n" + 
        "[\r\n" + 
        "B1\r\n" + 
        "PADS2\r\n" + 
        "9V\r\n" + 
        "\r\n" + 
        "]\r\n" + 
        "(\r\n" + 
        "unnamed_net15\r\n" + 
        "U4-11\r\n" + 
        ")\r\n" + 
        "(\r\n" + 
        "unnamed_net14\r\n" + 
        "U3-3\r\n" + 
        "U1-3\r\n" + 
        "R7-1\r\n" + 
        "R8-2\r\n" + 
        "U2-3\r\n" + 
        ")\r\n" + 
        "(\r\n" + 
        "unnamed_net13\r\n" + 
        "R1-2\r\n" + 
        "B1-1\r\n" + 
        ")";
    
    List<String> outputWarnings = new ArrayList<String>();
    try {
      TangoNetlistParser parser = new TangoNetlistParser();
      List<ParsedNetlistEntry> parsed = parser.parse(content, outputWarnings);
      Assert.assertNotNull(parsed);
    } catch (NetlistParseException e) {
      fail("Unexpected error:" + e.getMessage());
    }
  }
}
