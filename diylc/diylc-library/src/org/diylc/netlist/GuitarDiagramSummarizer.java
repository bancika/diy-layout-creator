package org.diylc.netlist;

import java.util.ArrayList;
import java.util.List;

import org.diylc.common.INetlistSummarizer;

public class GuitarDiagramSummarizer implements INetlistSummarizer {

  @Override
  public String getName() {  
    return "Guitar Diagrams";
  }
  
  @Override
  public String getIconName() {
    return "Guitar";
  }
    
  public List<Summary> summarize(List<Netlist> netlists, Node preferredOutput) {    
    List<Summary> summaries = new ArrayList<Summary>();
    for (Netlist n : netlists)
      summaries.add(summarize(n, preferredOutput));
    
    return summaries;
  }
  
  private Summary summarize(Netlist netlist, Node preferredOutput) {
    List<String> notes = new ArrayList<String>();
    notes.add("test");
    return new Summary(netlist, notes);
  }

}
