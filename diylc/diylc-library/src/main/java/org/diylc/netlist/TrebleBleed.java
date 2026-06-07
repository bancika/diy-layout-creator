package org.diylc.netlist;

import org.diylc.core.IDIYComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record TrebleBleed(Set<IDIYComponent<?>> components, TreeConnectionType connectionType) {

    public AnalysisResult analyze() {
        List<Double> capValues = new ArrayList<>();
        List<Double> resValues = new ArrayList<>();
        for (IDIYComponent<?> c : components) {
            if (c.getValue() instanceof org.diylc.core.measures.Capacitance) {
                Double nv = ((org.diylc.core.measures.Capacitance) c.getValue()).getNormalizedValue();
                if (nv != null) capValues.add(nv);
            } else if (c.getValue() instanceof org.diylc.core.measures.Resistance) {
                Double nv = ((org.diylc.core.measures.Resistance) c.getValue()).getNormalizedValue();
                if (nv != null) resValues.add(nv);
            }
        }

        String circuitType;
        String style;
        if (components.size() == 1) {
            circuitType = null;
            style = "PRS, G&L";
            if (capValues.size() == 1) {
                Double cVal = capValues.get(0);
                if (matches(cVal, 180e-6)) {
                    style = "PRS";
                } else if (matches(cVal, 200e-6)) {
                    style = "G&L";
                }
            }
        } else if (components.size() == 2 && connectionType == TreeConnectionType.Series) {
            circuitType = "series";
            style = "Kinman";
            if (capValues.size() == 1 && resValues.size() == 1) {
                Double cVal = capValues.get(0);
                Double rVal = resValues.get(0);
                if (matches(cVal, 1.2e-3) && matches(rVal, 130e3)) {
                    style = "Kinman";
                }
            }
        } else if (components.size() == 2 && connectionType == TreeConnectionType.Parallel) {
            circuitType = "parallel";
            style = "Fender, Suhr, Fralin, Bill Lawrence, Mojo Tone, TV Jones, DiMarzio, Seymour Duncan";
            if (capValues.size() == 1 && resValues.size() == 1) {
                Double cVal = capValues.get(0);
                Double rVal = resValues.get(0);
                if (matches(cVal, 330e-6) && matches(rVal, 80e3)) {
                    style = "Bill Lawrence";
                } else if (matches(cVal, 471e-6) && matches(rVal, 220e3)) {
                    style = "Mojo Tone";
                } else if (matches(cVal, 560e-6) && matches(rVal, 300e3)) {
                    style = "DiMarzio";
                } else if (matches(cVal, 680e-6) && matches(rVal, 150e3)) {
                    style = "John Suhr";
                } else if ((matches(cVal, 1e-3) || matches(cVal, 2e-3)) && matches(rVal, 150e3)) {
                    style = "TV Jones";
                } else if (matches(cVal, 1e-3) && matches(rVal, 100e3)) {
                    style = "Seymour Duncan";
                } else if (matches(cVal, 2.5e-3) && matches(rVal, 200e3)) {
                    style = "Lindy Fralin";
                }
            }
        } else if (components.size() == 3) {
            circuitType = null;
            style = null;
            if (capValues.size() == 1 && resValues.size() == 2) {
                Double cVal = capValues.get(0);
                if (matches(cVal, 1.2e-3) && ((matches(resValues.get(0), 150e3) && matches(resValues.get(1), 20e3)) || (matches(resValues.get(0), 20e3) && matches(resValues.get(1), 150e3)))) {
                    style = "Fender";
                }
            }
        } else {
            circuitType = null;
            style = null;
        }

        return new AnalysisResult(circuitType, style);
    }

    private static boolean matches(Double val, double expected) {
        return val != null && Math.abs(val - expected) < expected * 1e-4;
    }

    public record AnalysisResult(String circuitType, String style) {}
}
