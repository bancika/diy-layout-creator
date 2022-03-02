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
package org.diylc.components.smd;

import java.awt.Color;

import org.diylc.components.transform.PassiveSMDTransformer;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.measures.Resistance;

@ComponentDescriptor(name = "SMD Resistor", author = "Branislav Stojkovic", category = "SMD", instanceNamePrefix = "R",
    description = "Surface mount resistor", zOrder = IDIYComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_VALUE, transformer = PassiveSMDTransformer.class)
public class SMDResistor extends PassiveSMDComponent<Resistance> {

  public static Color BODY_COLOR = Color.gray;
  public static Color BORDER_COLOR = Color.gray.darker();

  private static final long serialVersionUID = 1L;

  public SMDResistor() {
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
  }

  @EditableProperty(validatorClass = PositiveMeasureValidator.class)
  public Resistance getValue() {
    return value;
  }

  public void setValue(Resistance value) {
    this.value = value;
  }
}
