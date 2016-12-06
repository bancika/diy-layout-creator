package org.diylc.components.smd;

import java.awt.Color;

import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.measures.Resistance;

@ComponentDescriptor(name = "SMD Resistor", author = "Branislav Stojkovic", category = "SMD", instanceNamePrefix = "C",
    description = "Surface mount resistor", stretchable = false, zOrder = IDIYComponent.COMPONENT,
    keywordPolicy = KeywordPolicy.SHOW_VALUE)
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
