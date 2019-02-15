package org.diylc.graph;

import org.diylc.core.IDIYComponent;
import org.diylc.core.ISwitch;

public class Position {

  private ISwitch theSwitch;
  private int position;

  public Position(ISwitch theSwitch, int position) {
    super();
    this.theSwitch = theSwitch;
    this.position = position;
  }

  public ISwitch getSwitch() {
    return theSwitch;
  }

  public int getPosition() {
    return position;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + position;
    result = prime * result + ((theSwitch == null) ? 0 : theSwitch.hashCode());
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
    Position other = (Position) obj;
    if (position != other.position)
      return false;
    if (theSwitch == null) {
      if (other.theSwitch != null)
        return false;
    } else if (!theSwitch.equals(other.theSwitch))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return (theSwitch instanceof IDIYComponent<?> ? ((IDIYComponent<?>) theSwitch).getName() + "." : "") + theSwitch.getPositionName(position);
  }
}
