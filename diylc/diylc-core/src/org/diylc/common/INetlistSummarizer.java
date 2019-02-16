package org.diylc.common;

import java.util.List;

import org.diylc.netlist.Netlist;
import org.diylc.netlist.Node;
import org.diylc.netlist.Summary;

public interface INetlistSummarizer {

  /**   
   * @return summarizer name
   */
  String getName();
  
  /**   
   * @return name of the icon to use
   */
  String getIconName();
  
  /**
   * Summarizes all {@link Netlist}s provided. 
   * 
   * @param netlists
   * @param preferredOutput optional, needed only where there's more than one possible output node.
   * @return
   */
  List<Summary> summarize(List<Netlist> netlists, Node preferredOutput);
}
