package org.diylc.netlist;

public interface ISpiceMapper {

  int mapToSpiceNode(int Index);
  
  String getSectionName(int pointIndex); 
}
