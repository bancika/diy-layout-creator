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
package org.diylc.netlist;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.diylc.core.ICommonNode;
import org.diylc.core.IDIYComponent;

/**
 * Represents a single node in a {@link Netlist}, uniquely defined by a component and a control point.
 * 
 * @author Branislav Stojkovic
 */
public class Node implements Comparable<Node> {

  /** Pin names that mark an absent connection, so repeats of them are not one net. */
  private static final Set<String> NON_NET_NAMES =
      new HashSet<String>(Arrays.asList("NC", "RSV"));

  private IDIYComponent<?> component;
  private int pointIndex;

  public Node(IDIYComponent<?> component, int pointIndex) {
    super();
    this.component = component;
    this.pointIndex = pointIndex;
  }

  public IDIYComponent<?> getComponent() {
    return component;
  }

  public int getPointIndex() {
    return pointIndex;
  }
  
  public String getDisplayName() {
    return sanitizeNodeName(component.getControlPointNodeName(pointIndex));
  }

  /**
   * Strips the parenthesized annotation off a raw control point name, so a netlist reads
   * <code>MCU1.D11</code> rather than <code>MCU1.D11 (~, MOSI)</code>. The bare name identifies the
   * pin; everything in parentheses documents its alternate functions and only adds noise here.
   *
   * <p>A trailing disambiguator such as the <code>_1</code> in <code>GND_1</code> is kept, so the
   * netlist can still say which pad a connection reached. Whether two such pins are the same
   * net is a separate question, answered by {@link #railName(String)}.
   *
   * @param nodeName raw name from {@link IDIYComponent#getControlPointNodeName(int)}
   * @return the sanitized name, or null if the raw name was null
   */
  public static String sanitizeNodeName(String nodeName) {
    if (nodeName == null) {
      return null;
    }
    int parenIndex = nodeName.indexOf('(');
    String sanitized = (parenIndex == -1 ? nodeName : nodeName.substring(0, parenIndex)).trim();
    // Never let sanitizing leave a pin nameless
    return sanitized.isEmpty() ? nodeName.trim() : sanitized;
  }

  /**
   * The rail a pin belongs to: its sanitized name with a trailing <code>_1</code>, <code>_2</code>
   * disambiguator removed, so <code>GND_1</code> and <code>GND_2</code> report the same rail.
   *
   * <p>Only a numeric suffix is dropped. An underscore does three unrelated jobs across the
   * pinouts in this library: it disambiguates a repeated rail, it namespaces a connector's signals
   * (<code>ETH_TX-</code>, <code>USB_D+</code>), and it qualifies a genuinely separate net
   * (<code>MISO_16U2</code>, <code>3V3_EN</code>). Cutting at the first underscore instead would
   * report an Ethernet header as one rail.
   *
   * @param nodeName raw name from {@link IDIYComponent#getControlPointNodeName(int)}
   * @return the rail name, or null if the pin has no name or names no net at all
   */
  public static String railName(String nodeName) {
    String sanitized = sanitizeNodeName(nodeName);
    if (sanitized == null || sanitized.isEmpty()) {
      return null;
    }
    String rail = sanitized.replaceAll("_\\d+$", "");
    return NON_NET_NAMES.contains(rail) ? null : rail;
  }
  
  public Point2D getPoint2D() {
    return component.getControlPoint(pointIndex);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Node)) return false;
    Node other = (Node) obj;
    return Objects.equals(component, other.component) && pointIndex == other.pointIndex;
  }

  @Override
  public int hashCode() {
    return Objects.hash(component, pointIndex);
  }

  @Override
  public String toString() {
    if (component instanceof ICommonNode) {
      return ((ICommonNode)component).getCommonNodeLabel();
    }
    if (component.getControlPointCount() == 1)
      return component.getName();
    return component.getName() + "." + getDisplayName() /*+ " @ (" + component.getControlPoint(pointIndex).getX() + ":" + component.getControlPoint(pointIndex).getY() + ")"*/;
  }

  @Override
  public int compareTo(Node o) {
    return toString().compareToIgnoreCase(o.toString());
  }
}
