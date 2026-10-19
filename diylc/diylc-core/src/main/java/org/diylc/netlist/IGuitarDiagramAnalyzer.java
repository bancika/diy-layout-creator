package org.diylc.netlist;

import java.util.List;

import org.diylc.core.annotations.Indexed;

@Indexed
public interface IGuitarDiagramAnalyzer {

  List<String> collectNotes(Netlist netlist);
}
