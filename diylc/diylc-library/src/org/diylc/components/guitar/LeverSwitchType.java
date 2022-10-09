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
package org.diylc.components.guitar;

public enum LeverSwitchType {
  
  DP3T("DP3T (Standard 3-Position Strat)"), DP3T_5pos("DP3T (Standard 5-Position Strat)"), DP3T_5pos_Import("DP3T (Import 5-Position Strat)"),
  _4P5T("4P5T (Super/Mega)"), DP4T("DP4T (4-Position Tele)"), _6_WAY_OG("DP4T (6-Position Oak Grigsby)"), DP5T("DP5T");

  private String title;

  private LeverSwitchType(String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return title;
  }
}