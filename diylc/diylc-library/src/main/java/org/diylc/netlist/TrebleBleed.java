package org.diylc.netlist;

import org.diylc.core.IDIYComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record TrebleBleed(Set<IDIYComponent<?>> components, TreeConnectionType connectionType) {

    public String toNote(IDIYComponent<?> volumePotComponent) {
        Set<IDIYComponent<?>> trebleBleedComponents = components();
        if (trebleBleedComponents != null && !trebleBleedComponents.isEmpty()) {

            TrebleBleed.AnalysisResult analyze = analyze();

            String strComponents =
                trebleBleedComponents.stream().sorted(Comparator.comparing(IDIYComponent::getName))
                    .map(c -> "'" + c.getName() + "'").collect(Collectors.joining(", "));
            String trebleBleedNote =
                strComponents + (trebleBleedComponents.size() == 1 ? " forms a " : " form a ");
            if (analyze.circuitType() != null) {
                trebleBleedNote += analyze.circuitType() + " ";
            }
            trebleBleedNote = trebleBleedNote + "treble bleed circuit on the '" + volumePotComponent
                .getName() + "' volume control";
            if (analyze.style() != null) {
                trebleBleedNote = trebleBleedNote + ", in the style of " + analyze.style();
            }
            return trebleBleedNote;
        }
        return null;
    }

    private AnalysisResult analyze() {
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
            style = "PRS, G&L, Ibanez, Ernie Ball Music Man (values not matching exactly)";
            if (capValues.size() == 1) {
                Double cVal = capValues.get(0);
                if (matches(cVal, 180e-6)) {
                    style = "PRS";
                } else if (matches(cVal, 200e-6)) {
                    style = "G&L";
                } else if (matches(cVal, 330e-6)) {
                    style = "Ibanez, Ernie Ball Music Man";
                }
            }
        } else if (components.size() == 2 && connectionType == TreeConnectionType.Series) {
            circuitType = "series";
            style = "Kinman, Fender Tone Saver (values not matching exactly)";
            if (capValues.size() == 1 && resValues.size() == 1) {
                Double cVal = capValues.get(0);
                Double rVal = resValues.get(0);
                if (matches(cVal, 1.2e-3) && matches(rVal, 130e3)) {
                    style = "Kinman";
                } else if (matches(cVal, 1e-3) && matches(rVal, 130e3)) {
                    style = "Fender Tone Saver (250K)";
                } else if (matches(cVal, 500e-6) && matches(rVal, 240e3)) {
                    style = "Fender Tone Saver (500K)";
                }
            }
        } else if (components.size() == 2 && connectionType == TreeConnectionType.Parallel) {
            circuitType = "parallel";
            style = "Fender, Suhr, Fralin, Bill Lawrence, Mojo Tone, TV Jones, DiMarzio, Seymour Duncan, Bare Knuckle Pickups, Emerson Custom (values not matching exactly)";
            if (capValues.size() == 1 && resValues.size() == 1) {
                Double cVal = capValues.get(0);
                Double rVal = resValues.get(0);
                if (matches(cVal, 330e-6) && matches(rVal, 80e3)) {
                    style = "Bill Lawrence";
                } else if (matches(cVal, 471e-6) && matches(rVal, 220e3)) {
                    style = "Mojo Tone";
                } else if (matches(cVal, 470e-6) && matches(rVal, 220e3)) {
                    style = "Emerson Custom";
                } else if (matches(cVal, 560e-6) && matches(rVal, 300e3)) {
                    style = "DiMarzio";
                } else if (matches(cVal, 680e-6) && matches(rVal, 150e3)) {
                    style = "John Suhr";
                } else if ((matches(cVal, 680e-6) || matches(cVal, 1e-3)) && matches(rVal, 220e3)) {
                    style = "Bare Knuckle Pickups";
                } else if ((matches(cVal, 1e-3) || matches(cVal, 2e-3)) && matches(rVal, 150e3)) {
                    style = "TV Jones";
                } else if (matches(cVal, 1e-3) && matches(rVal, 100e3)) {
                    style = "Seymour Duncan";
                } else if (matches(cVal, 2.5e-3) && matches(rVal, 200e3)) {
                    style = "Lindy Fralin";
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

    private record AnalysisResult(String circuitType, String style) {}
}
