/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.plugins.compare.service;

import org.diylc.appframework.miscutils.InMemoryConfigurationManager;
import org.diylc.common.DrawOption;
import org.diylc.common.DummyView;
import org.diylc.common.IPlugInPort;
import org.diylc.netlist.Group;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.Node;
import org.diylc.netlist.NetlistException;
import org.diylc.plugins.compare.model.ConnectionDiff;
import org.diylc.plugins.compare.model.ComponentDiff;
import org.diylc.plugins.compare.model.CompareResults;
import org.diylc.presenter.Presenter;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompareService {

  private IPlugInPort plugInPort;

  public CompareService(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
  }

  public CompareResults compareWith(File file) {
    Presenter presenter = new Presenter(new DummyView(), InMemoryConfigurationManager.getInstance());
    presenter.loadProjectFromFile(file.getAbsolutePath());

    renderInMemory(presenter);

    try {
      List<Netlist> otherNetlists = presenter.extractNetlists(false);
      List<Netlist> thisNetlists = plugInPort.extractNetlists(false);
      if (otherNetlists.size() != 1 || thisNetlists.size() != 1) {
        return new CompareResults(false, List.of(), List.of());
      }
      return compare(thisNetlists.get(0), otherNetlists.get(0));
    } catch (NetlistException e) {
      throw new RuntimeException(e);
    }
  }

  private void renderInMemory(Presenter presenter) {
    Dimension d = presenter.getCanvasDimensions(false, false);
    BufferedImage img = new BufferedImage(
        Math.max(1, d.width), Math.max(1, d.height), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    presenter.draw(g2d, EnumSet.noneOf(DrawOption.class), null, null, null, null);
    g2d.dispose();
  }

  public CompareResults compare(Netlist netlist1, Netlist netlist2) {
    Set<String> currentComponents = extractNetlistedComponents(netlist1);
    Set<String> targetComponents = extractNetlistedComponents(netlist2);

    List<ComponentDiff> componentDiffs = new ArrayList<>();
    for (String name : currentComponents) {
      if (!targetComponents.contains(name)) {
        componentDiffs.add(new ComponentDiff(name, true));
      }
    }
    for (String name : targetComponents) {
      if (!currentComponents.contains(name)) {
        componentDiffs.add(new ComponentDiff(name, false));
      }
    }

    Set<String> currentConnections = extractConnections(netlist1);
    Set<String> targetConnections = extractConnections(netlist2);

    List<ConnectionDiff> connectionDiffs = new ArrayList<>();
    for (String conn : currentConnections) {
      if (!targetConnections.contains(conn)) {
        String[] parts = conn.split("\\|");
        if (targetComponents.contains(parts[0]) && targetComponents.contains(parts[2])) {
          connectionDiffs.add(new ConnectionDiff(parts[0], parts[1], parts[2], parts[3], true));
        }
      }
    }
    for (String conn : targetConnections) {
      if (!currentConnections.contains(conn)) {
        String[] parts = conn.split("\\|");
        if (currentComponents.contains(parts[0]) && currentComponents.contains(parts[2])) {
          connectionDiffs.add(new ConnectionDiff(parts[0], parts[1], parts[2], parts[3], false));
        }
      }
    }

    boolean matches = componentDiffs.isEmpty() && connectionDiffs.isEmpty();
    return new CompareResults(matches, connectionDiffs, componentDiffs);
  }

  private Set<String> extractNetlistedComponents(Netlist netlist) {
    Set<String> names = new HashSet<>();
    for (Group group : netlist.getGroups()) {
      for (Node node : group.getNodes()) {
        names.add(node.getComponent().getName());
      }
    }
    return names;
  }

  private Set<String> extractConnections(Netlist netlist) {
    Set<String> connections = new HashSet<>();
    for (Group group : netlist.getGroups()) {
      List<Node> nodes = new ArrayList<>(group.getNodes());
      for (int i = 0; i < nodes.size(); i++) {
        for (int j = i + 1; j < nodes.size(); j++) {
          connections.add(connectionKey(nodes.get(i), nodes.get(j)));
        }
      }
    }
    return connections;
  }

  private String connectionKey(Node a, Node b) {
    String keyA = a.getComponent().getName() + "|" + a.getComponent().getControlPointNodeName(a.getPointIndex());
    String keyB = b.getComponent().getName() + "|" + b.getComponent().getControlPointNodeName(b.getPointIndex());
    if (keyA.compareTo(keyB) <= 0) {
      return keyA + "|" + keyB;
    }
    return keyB + "|" + keyA;
  }
}
