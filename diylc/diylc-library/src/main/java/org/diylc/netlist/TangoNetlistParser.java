package org.diylc.netlist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import org.diylc.components.passive.AxialElectrolyticCapacitor;
import org.diylc.components.passive.AxialFilmCapacitor;
import org.diylc.components.passive.CapacitorSymbol;
import org.diylc.components.passive.MultiSectionCapacitor;
import org.diylc.components.passive.RadialCeramicDiskCapacitor;
import org.diylc.components.passive.RadialElectrolytic;
import org.diylc.components.passive.RadialFilmCapacitor;
import org.diylc.components.passive.Resistor;
import org.diylc.components.passive.ResistorSymbol;
import org.diylc.components.semiconductors.BJTSymbol;
import org.diylc.components.semiconductors.DIL_IC;
import org.diylc.components.semiconductors.DiodeGlass;
import org.diylc.components.semiconductors.DiodePlastic;
import org.diylc.components.semiconductors.DiodeSymbol;
import org.diylc.components.semiconductors.JFETSymbol;
import org.diylc.components.semiconductors.LED;
import org.diylc.components.semiconductors.LEDSymbol;
import org.diylc.components.semiconductors.MOSFETSymbol;
import org.diylc.components.semiconductors.PhotoDiodeSymbol;
import org.diylc.components.semiconductors.SchottkyDiodeSymbol;
import org.diylc.components.semiconductors.TransistorTO220;
import org.diylc.components.semiconductors.TransistorTO92;
import org.diylc.components.semiconductors.ZenerDiodeSymbol;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.Voltage;
import org.diylc.netlist.AbstractNetlistParser;

public class TangoNetlistParser extends AbstractNetlistParser {

  private static final Logger LOG = Logger.getLogger(TangoNetlistParser.class);

  @Override
  public String getName() {
    return "Tango";
  }

  @Override
  public String getFileExt() {
    return "net";
  }

  @Override
  public String getItemRegex() {
    return "\\[\\s*(?<Name>.+?)\\n(?<Type>.+?)\\n(?<Value>.+?)\\s*\\]";
  }

  @Override
  public List<Class<?>> getComponentTypeCandidates(String type,
      Map<String, Object> additionalOutputs) {
    List<Class<?>> types = new ArrayList<Class<?>>();

    if ("RESISTOR".equalsIgnoreCase(type)) {
      types.add(Resistor.class);
      types.add(ResistorSymbol.class);
    }

    if (type.toUpperCase().startsWith("CAP_CERAMIC")) {
      types.add(RadialCeramicDiskCapacitor.class);
      types.add(CapacitorSymbol.class);
    }

    if (type.toUpperCase().startsWith("CAP_FILM")) {
      types.add(AxialFilmCapacitor.class);
      types.add(RadialFilmCapacitor.class);
      types.add(CapacitorSymbol.class);
    }

    if (type.toUpperCase().startsWith("CAP_ELECTRO")) {
      boolean polarized = true;
      if (type.toUpperCase().endsWith("_NP")) {
        polarized = false;
        type = type.substring(0, type.length() - 3);
      }
      if (type.length() > "CAP_ELECTRO".length()) {
        String voltage = type.substring("CAP_ELECTRO".length() + 1) + "V";
        try {          
          additionalOutputs.put("VoltageNew", Voltage.parseVoltage(voltage));
        } catch (Exception e) {
          LOG.error("Error while parsing voltage " + voltage);
        }
      }
      additionalOutputs.put("Polarized", polarized);
      types.add(AxialElectrolyticCapacitor.class);
      types.add(RadialElectrolytic.class);
      types.add(MultiSectionCapacitor.class);
      types.add(CapacitorSymbol.class);
    }

    if (type.toUpperCase().startsWith("DIP")) {
      try {
        int pinCount = Integer.parseInt(type.substring(3));
        additionalOutputs.put("PinCount", DIL_IC.PinCount.valueOf("_" + pinCount));
      } catch (Exception e) {
      }

      types.add(DIL_IC.class);
    }

    if (type.equalsIgnoreCase("DIODE")) {
      types.add(DiodeGlass.class);
      types.add(DiodePlastic.class);
      types.add(SchottkyDiodeSymbol.class);
      types.add(PhotoDiodeSymbol.class);
      types.add(DiodeSymbol.class);
      types.add(ZenerDiodeSymbol.class);
    }

    if (type.equalsIgnoreCase("LED")) {
      types.add(LED.class);
      types.add(LEDSymbol.class);
    }

    if (type.equalsIgnoreCase("TO92")) {
      types.add(TransistorTO92.class);
      types.add(BJTSymbol.class);
      types.add(MOSFETSymbol.class);
      types.add(JFETSymbol.class);
    }

    if (type.equalsIgnoreCase("TO220")) {
      types.add(TransistorTO220.class);
      types.add(BJTSymbol.class);
      types.add(MOSFETSymbol.class);
      types.add(JFETSymbol.class);
    }

    return types;
  }

  @Override
  public List<String> getPropertyNames() {
    return Arrays.asList("Name", "Value");
  }

  @Override
  public Object parseValue(String type, String propertyName, String valueStr) {
    if ("RESISTOR".equalsIgnoreCase(type) && "Value".equals(propertyName))
      return Resistance.parseResistance(valueStr);
    if ((type.toUpperCase().startsWith("CAP_CERAMIC") || type.toUpperCase().startsWith("CAP_FILM")
        || type.toUpperCase().startsWith("CAP_ELECTRO")) && "Value".equals(propertyName))
      return Capacitance.parseCapacitance(valueStr + "F");
    return valueStr;
  }
}
