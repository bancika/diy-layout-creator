package org.diylc.netlist;

import java.util.List;

public interface IGuitarDiagramAnalyzer {

  List<String> collectNotes(Netlist netlist);
}
