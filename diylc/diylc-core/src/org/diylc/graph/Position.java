package org.diylc.graph;

public class Position {

  private String ownerType;
  private String owner;
  private String name;
  
  public Position() {    
  }

  public String getOwnerType() {
    return ownerType;
  }

  public String getOwner() {
    return owner;
  }

  public String getName() {
    return name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((owner == null) ? 0 : owner.hashCode());
    result = prime * result + ((ownerType == null) ? 0 : ownerType.hashCode());
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
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (owner == null) {
      if (other.owner != null)
        return false;
    } else if (!owner.equals(other.owner))
      return false;
    if (ownerType == null) {
      if (other.ownerType != null)
        return false;
    } else if (!ownerType.equals(other.ownerType))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return ownerType + "." + owner + "." + name;
  }
}
