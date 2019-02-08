package org.diylc.graph;


public class Node {

  private String type;
  private String component;
  private String terminal;

  public Node() {}

  public String getType() {
    return type;
  }

  public String getComponent() {
    return component;
  }

  public String getTerminal() {
    return terminal;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((component == null) ? 0 : component.hashCode());
    result = prime * result + ((terminal == null) ? 0 : terminal.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Node other = (Node) obj;
    if (component == null) {
      if (other.component != null)
        return false;
    } else if (!component.equals(other.component))
      return false;
    if (terminal == null) {
      if (other.terminal != null)
        return false;
    } else if (!terminal.equals(other.terminal))
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return type + "." + component + "." + terminal;
  }
}
