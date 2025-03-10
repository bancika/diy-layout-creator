/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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
package org.diylc.components.misc;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.List;

/**
 * Represents a complete load characteristics, used for tubes and transistors.
 * 
 * @author bancika
 */
public class LoadlineEntity implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private String name;
  private double maxDissipation;
  private double maxVoltage;
  private double maxCurrent;
  private List<Line> lines;
  
  public LoadlineEntity() {
  }

  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public double getMaxDissipation() {
    return maxDissipation;
  }
  
  public void setMaxDissipation(double maxDissipation) {
    this.maxDissipation = maxDissipation;
  }
  
  public double getMaxVoltage() {
    return maxVoltage;
  }

  public void setMaxVoltage(double maxVoltage) {
    this.maxVoltage = maxVoltage;
  }

  public double getMaxCurrent() {
    return maxCurrent;
  }

  public void setMaxCurrent(double maxCurrent) {
    this.maxCurrent = maxCurrent;
  }
  
  public List<Line> getLines() {
    return lines;
  }
  
  public void setLines(List<Line> lines) {
    this.lines = lines;
  }

  // Individual load line, tied to the grid voltage
  public static class Line {
    private double voltage;
    private Point2D[] controlPoints;
    
    public double getVoltage() {
      return voltage;
    }
    
    public void setVoltage(double voltage) {
      this.voltage = voltage;
    }
    
    public Point2D[] getControlPoints() {
      return controlPoints;
    }
    
    public void setControlPoints(Point2D[] controlPoints) {
      this.controlPoints = controlPoints;
    }
  }
}
