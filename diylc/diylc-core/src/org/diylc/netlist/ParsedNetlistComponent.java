package org.diylc.netlist;

import java.util.Map;

public class ParsedNetlistComponent {
  
  public Class<?> type;
  public Map<String, Object> values;
  
  public ParsedNetlistComponent() {   
  }
  
  public ParsedNetlistComponent(Class<?> type, Map<String, Object> values) {
    super();
    this.type = type;
    this.values = values;
  }

  public Class<?> getType() {
    return type;
  }
  
  public Map<String, Object> getValues() {
    return values;
  }
}
