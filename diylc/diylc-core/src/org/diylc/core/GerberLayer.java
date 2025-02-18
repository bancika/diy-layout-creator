package org.diylc.core;

public class GerberLayer {
  
  private String function;
  private String extension;
  private boolean negative;
  
  public GerberLayer(String function, String extension) {
    this(function, extension, false);
  }
  
  public GerberLayer(String function, String extension, boolean negative) {
    super();
    this.function = function;
    this.extension = extension;
    this.negative = negative;
  }

  public String getFunction() {
    return function;
  }
  
  public String getExtension() {
    return extension;
  }
  
  public boolean isNegative() {
    return negative;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((extension == null) ? 0 : extension.hashCode());
    result = prime * result + ((function == null) ? 0 : function.hashCode());
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
    GerberLayer other = (GerberLayer) obj;
    if (extension == null) {
      if (other.extension != null)
        return false;
    } else if (!extension.equals(other.extension))
      return false;
    if (function == null) {
      if (other.function != null)
        return false;
    } else if (!function.equals(other.function))
      return false;
    return true;
  }
}
