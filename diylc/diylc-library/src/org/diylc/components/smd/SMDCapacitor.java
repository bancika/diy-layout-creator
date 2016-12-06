package org.diylc.components.smd;

import java.awt.Color;

import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.measures.Capacitance;

@ComponentDescriptor(name = "SMD Capacitor", author = "Branislav Stojkovic", category = "SMD",
    instanceNamePrefix = "C", description = "Surface mount capacitor", stretchable = false,
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE)
public class SMDCapacitor extends PassiveSMDComponent<Capacitance> {

  public static Color BODY_COLOR = Color.decode("#BD9347");
  public static Color BORDER_COLOR = BODY_COLOR.darker();

  private static final long serialVersionUID = 1L;

  public SMDCapacitor() {
    this.bodyColor = BODY_COLOR;
    this.borderColor = BORDER_COLOR;
  }

  @EditableProperty(validatorClass = PositiveMeasureValidator.class)
  public Capacitance getValue() {
    return value;
  }

  public void setValue(Capacitance value) {
    this.value = value;
  }
}
