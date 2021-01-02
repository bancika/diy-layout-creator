package org.diylc.netlist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.diylc.components.passive.AxialElectrolyticCapacitor;
import org.diylc.components.passive.AxialFilmCapacitor;
import org.diylc.components.passive.CapacitorSymbol;
import org.diylc.components.passive.MultiSectionCapacitor;
import org.diylc.components.passive.RadialCeramicDiskCapacitor;
import org.diylc.components.passive.RadialElectrolytic;
import org.diylc.components.passive.RadialFilmCapacitor;
import org.diylc.components.passive.Resistor;
import org.diylc.components.passive.ResistorSymbol;
import org.diylc.components.semiconductors.DIL_IC;
import org.diylc.components.semiconductors.DiodeGlass;
import org.diylc.components.semiconductors.DiodePlastic;
import org.diylc.components.semiconductors.DiodeSymbol;
import org.diylc.components.semiconductors.LED;
import org.diylc.components.semiconductors.LEDSymbol;
import org.diylc.components.semiconductors.PhotoDiodeSymbol;
import org.diylc.components.semiconductors.SchottkyDiodeSymbol;
import org.diylc.components.semiconductors.ZenerDiodeSymbol;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.Resistance;

public class TangoNetlistParser extends AbstractNetlistParser {

  @Override
  public String getName() {
    return "Tango";
  }

  @Override
  public String getFileExt() {
    return "pcb";
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
      additionalOutputs.put("Polarized", true);
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
