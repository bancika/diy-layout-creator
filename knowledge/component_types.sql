DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Schaller Megaswitch';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Schaller Megaswitch', '{
  "name" : "Schaller Megaswitch",
  "description" : "Several variations of Schaller Megaswitch",
  "category" : "Guitar",
  "properties" : [ {
    "name" : "Selected Position",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Markers",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.guitar.SchallerMegaSwitch$MegaSwitchType(E,E+,M,P,S,T)"
  }, {
    "name" : "Angle",
    "type" : "org.diylc.core.Angle"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Stingray Bass Pickup';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Stingray Bass Pickup', '{
  "name" : "Stingray Bass Pickup",
  "description" : "Humbucker Bass Pickup for Musicman Stingray and similar guitars",
  "category" : "Guitar",
  "properties" : [ {
    "name" : "Pole Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Model",
    "type" : "java.lang.String"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Single Coil Pickup';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Single Coil Pickup', '{
  "name" : "Single Coil Pickup",
  "description" : "Single coil guitar pickup, both Strat and Tele style",
  "category" : "Guitar",
  "properties" : [ {
    "name" : "Base",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lugs",
    "type" : "java.awt.Color"
  }, {
    "name" : "Pole Pieces",
    "type" : "org.diylc.components.guitar.SingleCoilPickup$PolePieceType(Single Rods,Single Rail,Double Rods,Double Rails,None)"
  }, {
    "name" : "Pole Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.guitar.SingleCoilPickup$SingleCoilType(Stratocaster,Telecaster)"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Coil(s)",
    "type" : "org.diylc.components.guitar.AbstractGuitarPickup$Polarity(Single - North,Single - South,Humbucking - 4 leads)"
  }, {
    "name" : "Model",
    "type" : "java.lang.String"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: LP Toggle Switch';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: LP Toggle Switch', '{
  "name" : "LP Toggle Switch",
  "description" : "Les Paul style 3 position toggle switch",
  "category" : "Guitar",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Lever Switch';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Lever Switch', '{
  "name" : "Lever Switch",
  "description" : "Strat-style lever switch",
  "category" : "Guitar",
  "properties" : [ {
    "name" : "Selected Position",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Markers",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.guitar.LeverSwitch$LeverSwitchType(DP3T (Standard 3-Position Strat),DP3T (Standard 5-Position Strat),DP3T (Import 5-Position Strat),4P5T (Super/Mega),DP4T (4-Position Tele),DP4T (6-Position Oak Grigsby),DP5T,6P5T (5-Position 6 Pole AxLabs)),4P3T (3-Position 3 Pole AxLabs),DP3T (Import 5-Position 2502N)"
  }, {
    "name" : "Angle",
    "type" : "org.diylc.core.Angle"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: P- Bass Pickup';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: P- Bass Pickup', '{
  "name" : "P- Bass Pickup",
  "description" : "Split-coil pickup for P-Bass and similar guitars",
  "category" : "Guitar",
  "properties" : [ {
    "name" : "Pole Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Coil(s)",
    "type" : "org.diylc.components.guitar.AbstractGuitarPickup$Polarity(Single - North,Single - South,Humbucking - 4 leads)"
  }, {
    "name" : "Model",
    "type" : "java.lang.String"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Humbucker Pickup';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Humbucker Pickup', '{
  "name" : "Humbucker Pickup",
  "description" : "Double-coil humbucker guitar pickup (PAF, Mini Humbuckers, Filtertrons)",
  "category" : "Guitar",
  "properties" : [ {
    "name" : "Bobbin 1",
    "type" : "java.awt.Color"
  }, {
    "name" : "Bobbin 2",
    "type" : "java.awt.Color"
  }, {
    "name" : "Pole Pieces 1",
    "type" : "org.diylc.components.guitar.HumbuckerPickup$PolePieceType(Rods,Rail,Screws,None)"
  }, {
    "name" : "Pole Pieces 2",
    "type" : "org.diylc.components.guitar.HumbuckerPickup$PolePieceType(Rods,Rail,Screws,None)"
  }, {
    "name" : "Pole Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Legs",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "Cover",
    "type" : "boolean"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.guitar.HumbuckerPickup$HumbuckerType(PAF,Mini,Filtertron)"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Model",
    "type" : "java.lang.String"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: P-90 Single Coil Pickup';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: P-90 Single Coil Pickup', '{
  "name" : "P-90 Single Coil Pickup",
  "description" : "Single coil P-90 guitar pickup, both \"dog ear\" and \"soap bar\"",
  "category" : "Guitar",
  "properties" : [ {
    "name" : "Pole Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.guitar.P90Pickup$P90Type(Dog Ear,Soap Bar)"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Coil(s)",
    "type" : "org.diylc.components.guitar.AbstractGuitarPickup$Polarity(Single - North,Single - South,Humbucking - 4 leads)"
  }, {
    "name" : "Model",
    "type" : "java.lang.String"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Jazz Bass Pickup';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Jazz Bass Pickup', '{
  "name" : "Jazz Bass Pickup",
  "description" : "Single coil pickup for Jazz Bass and similar guitars",
  "category" : "Guitar",
  "properties" : [ {
    "name" : "Pole Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Coil(s)",
    "type" : "org.diylc.components.guitar.AbstractGuitarPickup$Polarity(Single - North,Single - South,Humbucking - 4 leads)"
  }, {
    "name" : "Model",
    "type" : "java.lang.String"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Freeway 3X3-03 Toggle';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Freeway 3X3-03 Toggle', '{
  "name" : "Freeway 3X3-03 Toggle",
  "description" : "Freeway 3X3-03 Toggle Switch",
  "category" : "Guitar",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Selected Position",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Markers",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: S1 Switch';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: S1 Switch', '{
  "name" : "S1 Switch",
  "description" : "Fender S1 4 pole pushbutton switch",
  "category" : "Guitar",
  "properties" : [ {
    "name" : "Markers",
    "type" : "boolean"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Trimmer Potentiometer';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Trimmer Potentiometer', '{
  "name" : "Trimmer Potentiometer",
  "description" : "Various types of board mounted trimmer potentiometers",
  "category" : "Passive",
  "properties" : [ {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.passive.TrimmerPotentiometer$TrimmerType(Horizontal Small 1,Horizontal Small 2,Horizontal X-Small,Horizontal Medium,Horizontal Large,Vertical Inline,Vertical Offset 1,Vertical Offset 2)"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Taper",
    "type" : "org.diylc.components.passive.Taper(Lin,Log,Rev log,W,S)"
  }, {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Resistance"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Toroidal Inductor';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Toroidal Inductor', '{
  "name" : "Toroidal Inductor",
  "description" : "Ferrite core torroidal inductor mounted vertically",
  "category" : "Passive",
  "properties" : [ {
    "name" : "Resistance",
    "type" : "org.diylc.core.measures.Resistance"
  }, {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Inductance"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Tantalum Capacitor';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Tantalum Capacitor', '{
  "name" : "Tantalum Capacitor",
  "description" : "Vertically mounted tantalum capacitor",
  "category" : "Passive",
  "properties" : [ {
    "name" : "Voltage",
    "type" : "org.diylc.core.measures.Voltage"
  }, {
    "name" : "Marker",
    "type" : "java.awt.Color"
  }, {
    "name" : "Tick",
    "type" : "java.awt.Color"
  }, {
    "name" : "Folded",
    "type" : "boolean"
  }, {
    "name" : "Invert Polarity",
    "type" : "boolean"
  }, {
    "name" : "Height",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Diameter",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Capacitance"
  }, {
    "name" : "Lead Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Type",
    "type" : "java.lang.String"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Electrolytic Capacitor (Axial)';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Electrolytic Capacitor (Axial)', '{
  "name" : "Electrolytic Capacitor (Axial)",
  "description" : "Axial electrolytic capacitor, similar to Sprague Atom, F&T, etc",
  "category" : "Passive",
  "properties" : [ {
    "name" : "Voltage",
    "type" : "org.diylc.core.measures.Voltage"
  }, {
    "name" : "Marker",
    "type" : "java.awt.Color"
  }, {
    "name" : "Tick",
    "type" : "java.awt.Color"
  }, {
    "name" : "Polarized",
    "type" : "boolean"
  }, {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Capacitance"
  }, {
    "name" : "Type",
    "type" : "java.lang.String"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Inductor (Radial)';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Inductor (Radial)', '{
  "name" : "Inductor (Radial)",
  "description" : "Vertically mounted ferrite core inductor",
  "category" : "Passive",
  "properties" : [ {
    "name" : "Resistance",
    "type" : "org.diylc.core.measures.Resistance"
  }, {
    "name" : "Folded",
    "type" : "boolean"
  }, {
    "name" : "Rim",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Height",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Diameter",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Inductance"
  }, {
    "name" : "Lead Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Type",
    "type" : "java.lang.String"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Film Capacitor (Axial)';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Film Capacitor (Axial)', '{
  "name" : "Film Capacitor (Axial)",
  "description" : "Axial film capacitor, similar to Mallory 150s",
  "category" : "Passive",
  "properties" : [ {
    "name" : "Reverse (standing)",
    "type" : "boolean"
  }, {
    "name" : "Outer Foil Mark",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "Outer Foil",
    "type" : "java.awt.Color"
  }, {
    "name" : "Voltage",
    "type" : "org.diylc.core.measures.Voltage"
  }, {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Capacitance"
  }, {
    "name" : "Type",
    "type" : "java.lang.String"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Resistor';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Resistor', '{
  "name" : "Resistor",
  "description" : "Resistor layout symbol",
  "category" : "Passive",
  "properties" : [ {
    "name" : "Reverse (standing)",
    "type" : "boolean"
  }, {
    "name" : "Power Rating",
    "type" : "org.diylc.core.measures.Power"
  }, {
    "name" : "Color code",
    "type" : "org.diylc.common.ResistorColorCode(None,4 band,5 band)"
  }, {
    "name" : "Shape",
    "type" : "org.diylc.components.passive.Resistor$ResistorShape(Tubular,Tubular Beveled,Standard)"
  }, {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Resistance"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Ceramic Capacitor (Radial)';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Ceramic Capacitor (Radial)', '{
  "name" : "Ceramic Capacitor (Radial)",
  "description" : "Standard radial ceramic capacitor",
  "category" : "Passive",
  "properties" : [ {
    "name" : "Voltage",
    "type" : "org.diylc.core.measures.Voltage"
  }, {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Capacitance"
  }, {
    "name" : "Lead Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Type",
    "type" : "java.lang.String"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Electrolytic Capacitor (Radial)';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Electrolytic Capacitor (Radial)', '{
  "name" : "Electrolytic Capacitor (Radial)",
  "description" : "Vertically mounted electrolytic capacitor, polarized or bipolar",
  "category" : "Passive",
  "properties" : [ {
    "name" : "Voltage",
    "type" : "org.diylc.core.measures.Voltage"
  }, {
    "name" : "Marker",
    "type" : "java.awt.Color"
  }, {
    "name" : "Tick",
    "type" : "java.awt.Color"
  }, {
    "name" : "Polarized",
    "type" : "boolean"
  }, {
    "name" : "Folded",
    "type" : "boolean"
  }, {
    "name" : "Invert Polarity",
    "type" : "boolean"
  }, {
    "name" : "Height",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Diameter",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Capacitance"
  }, {
    "name" : "Lead Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Type",
    "type" : "java.lang.String"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Mini Signal Transformer';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Mini Signal Transformer', '{
  "name" : "Mini Signal Transformer",
  "description" : "Miniature PCB-mount signal transformer with EI core",
  "category" : "Passive",
  "properties" : [ {
    "name" : "Lead Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Coil",
    "type" : "java.awt.Color"
  }, {
    "name" : "Coil Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Core",
    "type" : "java.awt.Color"
  }, {
    "name" : "Core Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Winding Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Core Thickness",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Core Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Coil Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Coil Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Primary CT",
    "type" : "boolean"
  }, {
    "name" : "Secondary CT",
    "type" : "boolean"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Mica Capacitor (Radial)';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Mica Capacitor (Radial)', '{
  "name" : "Mica Capacitor (Radial)",
  "description" : "Standard radial mica capacitor",
  "category" : "Passive",
  "properties" : [ {
    "name" : "Voltage",
    "type" : "org.diylc.core.measures.Voltage"
  }, {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Capacitance"
  }, {
    "name" : "Lead Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Type",
    "type" : "java.lang.String"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Multi-Section Capacitor';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Multi-Section Capacitor', '{
  "name" : "Multi-Section Capacitor",
  "description" : "Multi-section vertically mounted electrolytic capacitor, similar to JJ, CE and others",
  "category" : "Passive",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Pin Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Base",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Diameter",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Voltage",
    "type" : "org.diylc.core.measures.Voltage"
  }, {
    "name" : "Value",
    "type" : "[Lorg.diylc.core.measures.Capacitance;"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Film Capacitor (Radial)';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Film Capacitor (Radial)', '{
  "name" : "Film Capacitor (Radial)",
  "description" : "Radial film capacitor, similar to Sprague Orange Drop",
  "category" : "Passive",
  "properties" : [ {
    "name" : "Lead Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Shape",
    "type" : "org.diylc.components.passive.RadialFilmCapacitor$RadialFilmCapacitorShape(Drop,Box)"
  }, {
    "name" : "Outer Foil Mark",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "Outer Foil",
    "type" : "java.awt.Color"
  }, {
    "name" : "Voltage",
    "type" : "org.diylc.core.measures.Voltage"
  }, {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Capacitance"
  }, {
    "name" : "Type",
    "type" : "java.lang.String"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Crystal Oscillator';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Crystal Oscillator', '{
  "name" : "Crystal Oscillator",
  "description" : "Radial crystal oscillator",
  "category" : "Passive",
  "properties" : [ {
    "name" : "Pin Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Frequency"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Potentiometer';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Potentiometer', '{
  "name" : "Potentiometer",
  "description" : "Panel mount potentiometer with solder lugs",
  "category" : "Passive",
  "properties" : [ {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Nut",
    "type" : "java.awt.Color"
  }, {
    "name" : "Wafer",
    "type" : "java.awt.Color"
  }, {
    "name" : "Diameter",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Lug Size",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "View",
    "type" : "org.diylc.components.passive.PotentiometerPanel$View(Shaft Down,Shaft Up)"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.passive.PotentiometerPanel$Type(Through Hole,PCB)"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Taper",
    "type" : "org.diylc.components.passive.Taper(Lin,Log,Rev log,W,S)"
  }, {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Resistance"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: LED';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: LED', '{
  "name" : "LED",
  "description" : "Light Emitting Diode",
  "category" : "Semiconductors",
  "properties" : [ {
    "name" : "Size",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: SIP IC';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: SIP IC', '{
  "name" : "SIP IC",
  "description" : "Single-in-line package IC",
  "category" : "Semiconductors",
  "properties" : [ {
    "name" : "Indent",
    "type" : "java.awt.Color"
  }, {
    "name" : "Pins",
    "type" : "org.diylc.components.semiconductors.SIL_IC$PinCount(2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20)"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Pin Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Transistor (TO-1)';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Transistor (TO-1)', '{
  "name" : "Transistor (TO-1)",
  "description" : "Transistor with small metal body",
  "category" : "Semiconductors",
  "properties" : [ {
    "name" : "Pin spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.components.semiconductors.AbstractTransistorPackage$TransistorDisplay(Name,Value,None,Both,Pinout)"
  }, {
    "name" : "Folded",
    "type" : "boolean"
  }, {
    "name" : "Pinout",
    "type" : "org.diylc.components.semiconductors.TransistorPinout(E-B-C     BJT,C-B-E     BJT,B-C-E     BJT,D-S-G     JFET,G-S-D     JFET,D-G-S     JFET,S-G-D     JFET,G-D-S     JFET,D-S-G     MOSFET,G-S-D     MOSFET,D-G-S     MOSFET,S-G-D     MOSFET,G-D-S     MOSFET,I-G-O     REGULATOR,O-G-I     REGULATOR,G-O-I     REGULATOR,A-O-I     REGULATOR,G-I-O     REGULATOR)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Transistor (TO-3)';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Transistor (TO-3)', '{
  "name" : "Transistor (TO-3)",
  "description" : "Transistor with large metal body",
  "category" : "Semiconductors",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Transistor (TO-126)';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Transistor (TO-126)', '{
  "name" : "Transistor (TO-126)",
  "description" : "Transistors with a hole for heat sink mounting",
  "category" : "Semiconductors",
  "properties" : [ {
    "name" : "Lead Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.components.semiconductors.AbstractTransistorPackage$TransistorDisplay(Name,Value,None,Both,Pinout)"
  }, {
    "name" : "Folded",
    "type" : "boolean"
  }, {
    "name" : "Pinout",
    "type" : "org.diylc.components.semiconductors.TransistorPinout(E-B-C     BJT,C-B-E     BJT,B-C-E     BJT,D-S-G     JFET,G-S-D     JFET,D-G-S     JFET,S-G-D     JFET,G-D-S     JFET,D-S-G     MOSFET,G-S-D     MOSFET,D-G-S     MOSFET,S-G-D     MOSFET,G-D-S     MOSFET,I-G-O     REGULATOR,O-G-I     REGULATOR,G-O-I     REGULATOR,A-O-I     REGULATOR,G-I-O     REGULATOR)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Transistor (TO-220)';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Transistor (TO-220)', '{
  "name" : "Transistor (TO-220)",
  "description" : "Transistors with metal tab for heat sink mounting",
  "category" : "Semiconductors",
  "properties" : [ {
    "name" : "Lead Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Tab",
    "type" : "java.awt.Color"
  }, {
    "name" : "Tab Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.components.semiconductors.AbstractTransistorPackage$TransistorDisplay(Name,Value,None,Both,Pinout)"
  }, {
    "name" : "Folded",
    "type" : "boolean"
  }, {
    "name" : "Pinout",
    "type" : "org.diylc.components.semiconductors.TransistorPinout(E-B-C     BJT,C-B-E     BJT,B-C-E     BJT,D-S-G     JFET,G-S-D     JFET,D-G-S     JFET,S-G-D     JFET,G-D-S     JFET,D-S-G     MOSFET,G-S-D     MOSFET,D-G-S     MOSFET,S-G-D     MOSFET,G-D-S     MOSFET,I-G-O     REGULATOR,O-G-I     REGULATOR,G-O-I     REGULATOR,A-O-I     REGULATOR,G-I-O     REGULATOR)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Diode (plastic)';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Diode (plastic)', '{
  "name" : "Diode (plastic)",
  "description" : "Plastic diode, like most rectifier, zener, schottky, etc.",
  "category" : "Semiconductors",
  "properties" : [ {
    "name" : "Reverse (standing)",
    "type" : "boolean"
  }, {
    "name" : "Marker",
    "type" : "java.awt.Color"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Bridge Rectifier';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Bridge Rectifier', '{
  "name" : "Bridge Rectifier",
  "description" : "Few variations of bridge rectifier chips",
  "category" : "Semiconductors",
  "properties" : [ {
    "name" : "Type",
    "type" : "org.diylc.components.semiconductors.BridgeRectifier$RectifierType(DFM A,DFM B,Round WOG A,Round WOG B,In-Line D-44,Square BR-3)"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Voltage",
    "type" : "org.diylc.core.measures.Voltage"
  }, {
    "name" : "Current",
    "type" : "org.diylc.core.measures.Current"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Transistor (TO-92)';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Transistor (TO-92)', '{
  "name" : "Transistor (TO-92)",
  "description" : "Transistor with small plastic or epoxy body",
  "category" : "Semiconductors",
  "properties" : [ {
    "name" : "Pin spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.components.semiconductors.AbstractTransistorPackage$TransistorDisplay(Name,Value,None,Both,Pinout)"
  }, {
    "name" : "Folded",
    "type" : "boolean"
  }, {
    "name" : "Pinout",
    "type" : "org.diylc.components.semiconductors.TransistorPinout(E-B-C     BJT,C-B-E     BJT,B-C-E     BJT,D-S-G     JFET,G-S-D     JFET,D-G-S     JFET,S-G-D     JFET,G-D-S     JFET,D-S-G     MOSFET,G-S-D     MOSFET,D-G-S     MOSFET,S-G-D     MOSFET,G-D-S     MOSFET,I-G-O     REGULATOR,O-G-I     REGULATOR,G-O-I     REGULATOR,A-O-I     REGULATOR,G-I-O     REGULATOR)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: DIP IC';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: DIP IC', '{
  "name" : "DIP IC",
  "description" : "Dual-in-line package IC",
  "category" : "Semiconductors",
  "properties" : [ {
    "name" : "Display Pin #s",
    "type" : "org.diylc.components.semiconductors.DIL_IC$DisplayNumbers(No,DIP,Connector,DIP (Mirrored),Connector (Mirrored))"
  }, {
    "name" : "Indent",
    "type" : "java.awt.Color"
  }, {
    "name" : "Pins",
    "type" : "org.diylc.components.semiconductors.DIL_IC$PinCount(4,6,8,10,12,14,16,18,20,22,24,26,28,30,32,34,36,38,40,42,44,46,48,50)"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Pin Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Row Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Diode (glass)';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Diode (glass)', '{
  "name" : "Diode (glass)",
  "description" : "Glass diode, like most small signal diodes.",
  "category" : "Semiconductors",
  "properties" : [ {
    "name" : "Reverse (standing)",
    "type" : "boolean"
  }, {
    "name" : "Marker",
    "type" : "java.awt.Color"
  }, {
    "name" : "Inside Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Blank Board';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Blank Board', '{
  "name" : "Blank Board",
  "description" : "Blank circuit board",
  "category" : "Boards",
  "properties" : [ {
    "name" : "Shape",
    "type" : "org.diylc.components.boards.BlankBoard$Type(Round,Square)"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Coordinate Origin",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateOrigin(Top Left,Top Right,Bottom Right,Bottom Left)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Underside Display",
    "type" : "org.diylc.core.BoardUndersideDisplay(None,Above,Below,Left,Right)"
  }, {
    "name" : "Underside Offset",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Underside Transparency",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "Dimension Mode",
    "type" : "org.diylc.components.boards.AbstractBoard$BoardSizingMode(Opposing Points,Explicit Dimensions)"
  }, {
    "name" : "Explicit Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Explicit Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Proto Board 780 Holes';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Proto Board 780 Holes', '{
  "name" : "Proto Board 780 Holes",
  "description" : "Prototyping board similar to Radio Shack 276-168B or MPJA 33304",
  "category" : "Boards",
  "properties" : [ {
    "name" : "Pad Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Trace Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Text Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Board Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.boards.ProtoBoard780$ProtoBoard780Type(RS,MPJA)"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Tag Strip';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Tag Strip', '{
  "name" : "Tag Strip",
  "description" : "Row of terminals for point-to-point construction",
  "category" : "Boards",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Terminals",
    "type" : "int"
  }, {
    "name" : "Terminal Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Board",
    "type" : "java.awt.Color"
  }, {
    "name" : "Mounting Lugs",
    "type" : "org.diylc.components.boards.TagStrip$TagStripMount(Central,Outside)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Perf Board w/ Pads';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Perf Board w/ Pads', '{
  "name" : "Perf Board w/ Pads",
  "description" : "Perforated board with solder pads",
  "category" : "Boards",
  "properties" : [ {
    "name" : "Pad color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Coordinates",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateDisplay(None,One Side,Both Sides)"
  }, {
    "name" : "Coordinate Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Coordinate Origin",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateOrigin(Top Left,Top Right,Bottom Right,Bottom Left)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Underside Display",
    "type" : "org.diylc.core.BoardUndersideDisplay(None,Above,Below,Left,Right)"
  }, {
    "name" : "Underside Offset",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Underside Transparency",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "X",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateType(Letters,Numbers)"
  }, {
    "name" : "Y",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateType(Letters,Numbers)"
  }, {
    "name" : "Dimension Mode",
    "type" : "org.diylc.components.boards.AbstractBoard$BoardSizingMode(Opposing Points,Explicit Dimensions)"
  }, {
    "name" : "Explicit Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Explicit Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: TriPad Board';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: TriPad Board', '{
  "name" : "TriPad Board",
  "description" : "Perforated FR4 board with copper strips connecting 3 holes in a row (aka TriPad Board)",
  "category" : "Boards",
  "properties" : [ {
    "name" : "Holes Per Strip",
    "type" : "int"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.OrientationHV(Vertical,Horizontal)"
  }, {
    "name" : "Strip Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Coordinates",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateDisplay(None,One Side,Both Sides)"
  }, {
    "name" : "Coordinate Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Coordinate Origin",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateOrigin(Top Left,Top Right,Bottom Right,Bottom Left)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Underside Display",
    "type" : "org.diylc.core.BoardUndersideDisplay(None,Above,Below,Left,Right)"
  }, {
    "name" : "Underside Offset",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Underside Transparency",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "X",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateType(Letters,Numbers)"
  }, {
    "name" : "Y",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateType(Letters,Numbers)"
  }, {
    "name" : "Dimension Mode",
    "type" : "org.diylc.components.boards.AbstractBoard$BoardSizingMode(Opposing Points,Explicit Dimensions)"
  }, {
    "name" : "Explicit Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Explicit Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Eurorack Stripboard';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Eurorack Stripboard', '{
  "name" : "Eurorack Stripboard",
  "description" : "David Haillant''s eurorack format stripboard for prototyping and building simple modules",
  "category" : "Boards",
  "properties" : [ {
    "name" : "Pad Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Trace Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Text Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Board Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: P-0+o (Proto) Board';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: P-0+o (Proto) Board', '{
  "name" : "P-0+o (Proto) Board",
  "description" : "A prototyping board for modular synths, central bus with V+ GND and V-",
  "category" : "Boards",
  "properties" : [ {
    "name" : "Underside Pad Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Underside Trace Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Pad Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Trace Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Text Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Board Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Eyelet Board';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Eyelet Board', '{
  "name" : "Eyelet Board",
  "description" : "Perforated board with eyelets",
  "category" : "Boards",
  "properties" : [ {
    "name" : "Eyelet color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Coordinates",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateDisplay(None,One Side,Both Sides)"
  }, {
    "name" : "Coordinate Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Coordinate Origin",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateOrigin(Top Left,Top Right,Bottom Right,Bottom Left)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Underside Display",
    "type" : "org.diylc.core.BoardUndersideDisplay(None,Above,Below,Left,Right)"
  }, {
    "name" : "Underside Offset",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Underside Transparency",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "X",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateType(Letters,Numbers)"
  }, {
    "name" : "Y",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateType(Letters,Numbers)"
  }, {
    "name" : "Dimension Mode",
    "type" : "org.diylc.components.boards.AbstractBoard$BoardSizingMode(Opposing Points,Explicit Dimensions)"
  }, {
    "name" : "Explicit Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Explicit Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Marshall Style Perf Board';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Marshall Style Perf Board', '{
  "name" : "Marshall Style Perf Board",
  "description" : "Perforated board as found on some Marshall and Trainwreck amps",
  "category" : "Boards",
  "properties" : [ {
    "name" : "Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Coordinates",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateDisplay(None,One Side,Both Sides)"
  }, {
    "name" : "Coordinate Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Coordinate Origin",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateOrigin(Top Left,Top Right,Bottom Right,Bottom Left)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Underside Display",
    "type" : "org.diylc.core.BoardUndersideDisplay(None,Above,Below,Left,Right)"
  }, {
    "name" : "Underside Offset",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Underside Transparency",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "X",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateType(Letters,Numbers)"
  }, {
    "name" : "Y",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateType(Letters,Numbers)"
  }, {
    "name" : "Dimension Mode",
    "type" : "org.diylc.components.boards.AbstractBoard$BoardSizingMode(Opposing Points,Explicit Dimensions)"
  }, {
    "name" : "Explicit Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Explicit Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Breadboard';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Breadboard', '{
  "name" : "Breadboard",
  "description" : "Prototyping solderless breadboard",
  "category" : "Boards",
  "properties" : [ {
    "name" : "Size",
    "type" : "org.diylc.components.boards.Breadboard$BreadboardSize(Half,Full)"
  }, {
    "name" : "Power Strip",
    "type" : "org.diylc.components.boards.Breadboard$PowerStripPosition(Inline,Offset)"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Vero Board';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Vero Board', '{
  "name" : "Vero Board",
  "description" : "Perforated FR4 board with copper strips connecting all holes in a row",
  "category" : "Boards",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.OrientationHV(Vertical,Horizontal)"
  }, {
    "name" : "Strip Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Coordinates",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateDisplay(None,One Side,Both Sides)"
  }, {
    "name" : "Coordinate Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Coordinate Origin",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateOrigin(Top Left,Top Right,Bottom Right,Bottom Left)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Underside Display",
    "type" : "org.diylc.core.BoardUndersideDisplay(None,Above,Below,Left,Right)"
  }, {
    "name" : "Underside Offset",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Underside Transparency",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "X",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateType(Letters,Numbers)"
  }, {
    "name" : "Y",
    "type" : "org.diylc.components.boards.AbstractBoard$CoordinateType(Letters,Numbers)"
  }, {
    "name" : "Dimension Mode",
    "type" : "org.diylc.components.boards.AbstractBoard$BoardSizingMode(Opposing Points,Explicit Dimensions)"
  }, {
    "name" : "Explicit Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Explicit Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Terminal Strip';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Terminal Strip', '{
  "name" : "Terminal Strip",
  "description" : "Row of terminals for point-to-point construction",
  "category" : "Boards",
  "properties" : [ {
    "name" : "Center Terminal",
    "type" : "boolean"
  }, {
    "name" : "Board Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Hole Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Terminals",
    "type" : "int"
  }, {
    "name" : "Terminal Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Board",
    "type" : "java.awt.Color"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: SVG Image';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: SVG Image', '{
  "name" : "SVG Image",
  "description" : "Scalable Vector Graphics",
  "category" : "Misc",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Sizing Mode",
    "type" : "org.diylc.components.misc.SVGImage$ImageSizingMode(Opposing Points,Scale)"
  }, {
    "name" : "Scale",
    "type" : "byte"
  }, {
    "name" : "Image",
    "type" : "[B"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Common Node';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Common Node', '{
  "name" : "Common Node",
  "description" : "Label that ties all nodes together",
  "category" : "Misc",
  "properties" : [ {
    "name" : "Font Size",
    "type" : "int"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Font",
    "type" : "java.awt.Font"
  }, {
    "name" : "Font Bold",
    "type" : "boolean"
  }, {
    "name" : "Background",
    "type" : "java.awt.Color"
  }, {
    "name" : "Shape",
    "type" : "org.diylc.components.misc.CommonNode$CommonNodeShape(Rectangle,Circle)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: PCB Text';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: PCB Text', '{
  "name" : "PCB Text",
  "description" : "Mirrored text for PCB artwork",
  "category" : "Misc",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Vertical Alignment",
    "type" : "org.diylc.common.VerticalAlignment(Top,Center,Bottom)"
  }, {
    "name" : "Horizontal Alignment",
    "type" : "org.diylc.common.HorizontalAlignment(Left,Center,Right)"
  }, {
    "name" : "Font Size",
    "type" : "int"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Font",
    "type" : "java.awt.Font"
  }, {
    "name" : "Font Italic",
    "type" : "boolean"
  }, {
    "name" : "Text",
    "type" : "java.lang.String"
  }, {
    "name" : "Font Bold",
    "type" : "boolean"
  }, {
    "name" : "Layer",
    "type" : "org.diylc.common.PCBLayer(Bottom,Top,Inner 1,Inner 2,Inner 3,Inner 4,Inner 5,Inner 6)"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Dial Scale';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Dial Scale', '{
  "name" : "Dial Scale",
  "description" : "Control panel dial scale",
  "category" : "Misc",
  "properties" : [ {
    "name" : "Diameter",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Marker Size",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Tick Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Font",
    "type" : "java.awt.Font"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.chassis.DialScale$DialScaleType(Ticks,Dots (Uniform),Dots (Gradual),Numeric (0-10),Numeric (1-11),Numeric (1-10),Numeric (1-12),Numeric (Even, No Ticks),Numeric (Even, With Ticks))"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Bill of Materials';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Bill of Materials', '{
  "name" : "Bill of Materials",
  "description" : "",
  "category" : "Misc",
  "properties" : [ {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Tape Measure';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Tape Measure', '{
  "name" : "Tape Measure",
  "description" : "Measures distance between the two points",
  "category" : "Misc",
  "properties" : [ {
    "name" : "Decimals",
    "type" : "int"
  }, {
    "name" : "Thickness",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Arrow Size",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Style",
    "type" : "org.diylc.common.LineStyle(Solid,Dashed,Dotted)"
  }, {
    "name" : "Unit",
    "type" : "org.diylc.core.measures.SizeUnit(px,mm,cm,m,in,ft,yd)"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Image';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Image', '{
  "name" : "Image",
  "description" : "User defined image",
  "category" : "Misc",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Sizing Mode",
    "type" : "org.diylc.components.misc.Image$ImageSizingMode(Opposing Points,Scale)"
  }, {
    "name" : "Scale",
    "type" : "byte"
  }, {
    "name" : "Image",
    "type" : "[B"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Auto-Wrap Label';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Auto-Wrap Label', '{
  "name" : "Auto-Wrap Label",
  "description" : "User defined label with auto-wrapped text",
  "category" : "Misc",
  "properties" : [ {
    "name" : "Horizontal Alignment",
    "type" : "org.diylc.common.HorizontalAlignment(Left,Center,Right)"
  }, {
    "name" : "Font Size",
    "type" : "int"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Font",
    "type" : "java.awt.Font"
  }, {
    "name" : "Font Italic",
    "type" : "boolean"
  }, {
    "name" : "Font Bold",
    "type" : "boolean"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: PCB Buzzer';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: PCB Buzzer', '{
  "name" : "PCB Buzzer",
  "description" : "Vertically mounted Buzzer, active or passive",
  "category" : "Misc",
  "properties" : [ {
    "name" : "Hole",
    "type" : "java.awt.Color"
  }, {
    "name" : "Marker",
    "type" : "java.awt.Color"
  }, {
    "name" : "Polarized",
    "type" : "boolean"
  }, {
    "name" : "Folded",
    "type" : "boolean"
  }, {
    "name" : "Invert Polarity",
    "type" : "boolean"
  }, {
    "name" : "Height",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Diameter",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Voltage"
  }, {
    "name" : "Lead Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Type",
    "type" : "java.lang.String"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Label';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Label', '{
  "name" : "Label",
  "description" : "User defined label",
  "category" : "Misc",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Vertical Alignment",
    "type" : "org.diylc.common.VerticalAlignment(Top,Center,Bottom)"
  }, {
    "name" : "Horizontal Alignment",
    "type" : "org.diylc.common.HorizontalAlignment(Left,Center,Right)"
  }, {
    "name" : "Font Size",
    "type" : "int"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Font",
    "type" : "java.awt.Font"
  }, {
    "name" : "Font Italic",
    "type" : "boolean"
  }, {
    "name" : "Font Bold",
    "type" : "boolean"
  }, {
    "name" : "Text",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Ground Fill';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Ground Fill', '{
  "name" : "Ground Fill",
  "description" : "Polygonal ground fill area",
  "category" : "Connectivity",
  "properties" : [ {
    "name" : "Edges",
    "type" : "org.diylc.components.connectivity.GroundFill$PointCount(3,4,5,6,7,8)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Layer",
    "type" : "org.diylc.common.PCBLayer(Bottom,Top,Inner 1,Inner 2,Inner 3,Inner 4,Inner 5,Inner 6)"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Hookup Wire';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Hookup Wire', '{
  "name" : "Hookup Wire",
  "description" : "Flexible wire with two control points",
  "category" : "Connectivity",
  "properties" : [ {
    "name" : "Stripe Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Stripe",
    "type" : "boolean"
  }, {
    "name" : "AWG",
    "type" : "org.diylc.components.connectivity.AWG(#8 / 3.26mm,#10 / 2.59mm,#12 / 2.05mm,#14 / 1.63mm,#16 / 1.29mm,#18 / 1.02mm,#20 / 0.81mm,#22 / 0.64mm,#24 / 0.51mm,#26 / 0.40mm,#28 / 0.32mm,#30 / 0.25mm,#32 / 0.20mm,#34 / 0.16mm,#36 / 0.13mm)"
  }, {
    "name" : "Point Count",
    "type" : "org.diylc.components.AbstractCurvedComponent$PointCount(Two,Three,Four,Five,Seven)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Style",
    "type" : "org.diylc.common.LineStyle(Solid,Dashed,Dotted)"
  }, {
    "name" : "Smooth",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Multimeter Probe';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Multimeter Probe', '{
  "name" : "Multimeter Probe",
  "description" : "Multimeter Probe",
  "category" : "Connectivity",
  "properties" : [ {
    "name" : "Angle",
    "type" : "org.diylc.common.Orientation45(Default,45 degrees clockwise,90 degrees clockwise,135 degrees clockwise,180 degrees clockwise,225 degrees clockwise,270 degrees clockwise,315 degrees clockwise)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Dot';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Dot', '{
  "name" : "Dot",
  "description" : "Connector dot",
  "category" : "Connectivity",
  "properties" : [ {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Size",
    "type" : "org.diylc.core.measures.Size"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Solder Lug';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Solder Lug', '{
  "name" : "Solder Lug",
  "description" : "Locking terminal lug commonly used for chassis ground connections",
  "category" : "Connectivity",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Trace Cut';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Trace Cut', '{
  "name" : "Trace Cut",
  "description" : "Designates the place where a trace on the vero board needs to be cut",
  "category" : "Connectivity",
  "properties" : [ {
    "name" : "Cut Orientation",
    "type" : "org.diylc.common.OrientationHV(Vertical,Horizontal)"
  }, {
    "name" : "Cut Between Holes",
    "type" : "boolean"
  }, {
    "name" : "Hole Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Board",
    "type" : "java.awt.Color"
  }, {
    "name" : "Size",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Curved Trace';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Curved Trace', '{
  "name" : "Curved Trace",
  "description" : "Curved copper trace with two control points",
  "category" : "Connectivity",
  "properties" : [ {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Layer",
    "type" : "org.diylc.common.PCBLayer(Bottom,Top,Inner 1,Inner 2,Inner 3,Inner 4,Inner 5,Inner 6)"
  }, {
    "name" : "Point Count",
    "type" : "org.diylc.components.AbstractCurvedComponent$PointCount(Two,Three,Four,Five,Seven)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Smooth",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Copper Trace';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Copper Trace', '{
  "name" : "Copper Trace",
  "description" : "Straight copper trace",
  "category" : "Connectivity",
  "properties" : [ {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Layer",
    "type" : "org.diylc.common.PCBLayer(Bottom,Top,Inner 1,Inner 2,Inner 3,Inner 4,Inner 5,Inner 6)"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Jumper';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Jumper', '{
  "name" : "Jumper",
  "description" : "",
  "category" : "Connectivity",
  "properties" : [ {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Style",
    "type" : "org.diylc.common.LineStyle(Solid,Dashed,Dotted)"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Cut Line';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Cut Line', '{
  "name" : "Cut Line",
  "description" : "Cut line",
  "category" : "Connectivity",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.OrientationHV(Vertical,Horizontal)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Turret Lug';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Turret Lug', '{
  "name" : "Turret Lug",
  "description" : "Turret terminal lug",
  "category" : "Connectivity",
  "properties" : [ {
    "name" : "Hole Size",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Size",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Twisted Leads';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Twisted Leads', '{
  "name" : "Twisted Leads",
  "description" : "A pair of flexible leads twisted tighly together",
  "category" : "Connectivity",
  "properties" : [ {
    "name" : "Stripe Color 1",
    "type" : "java.awt.Color"
  }, {
    "name" : "Stripe Color 2",
    "type" : "java.awt.Color"
  }, {
    "name" : "Color 1",
    "type" : "java.awt.Color"
  }, {
    "name" : "AWG",
    "type" : "org.diylc.components.connectivity.AWG(#8 / 3.26mm,#10 / 2.59mm,#12 / 2.05mm,#14 / 1.63mm,#16 / 1.29mm,#18 / 1.02mm,#20 / 0.81mm,#22 / 0.64mm,#24 / 0.51mm,#26 / 0.40mm,#28 / 0.32mm,#30 / 0.25mm,#32 / 0.20mm,#34 / 0.16mm,#36 / 0.13mm)"
  }, {
    "name" : "Color 2",
    "type" : "java.awt.Color"
  }, {
    "name" : "Stripe 1",
    "type" : "boolean"
  }, {
    "name" : "Stripe 2",
    "type" : "boolean"
  }, {
    "name" : "Point Count",
    "type" : "org.diylc.components.AbstractCurvedComponent$PointCount(Two,Three,Four,Five,Seven)"
  }, {
    "name" : "Smooth",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: PCB Terminal Block';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: PCB Terminal Block', '{
  "name" : "PCB Terminal Block",
  "description" : "Horizontal PCB terminal block with 5mm pitch",
  "category" : "Connectivity",
  "properties" : [ {
    "name" : "Screw Position",
    "type" : "org.diylc.components.connectivity.PCBTerminalBlock$ScrewPosition(Center,Offset)"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Pitch",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Pins",
    "type" : "org.diylc.components.connectivity.PCBTerminalBlock$PCBTerminalBlockCount(Two,Three,Four,Five,Six,Seven,Eight,Nine,Ten)"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Eyelet';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Eyelet', '{
  "name" : "Eyelet",
  "description" : "Eyelet or turret terminal",
  "category" : "Connectivity",
  "properties" : [ {
    "name" : "Hole Size",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Size",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Solder Pad';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Solder Pad', '{
  "name" : "Solder Pad",
  "description" : "Copper solder pad, round or square",
  "category" : "Connectivity",
  "properties" : [ {
    "name" : "Hole Type",
    "type" : "org.diylc.components.connectivity.SolderPad$HoleType(None,Plated Through Hole,Non-Plated Through Hole)"
  }, {
    "name" : "Hole",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Layer",
    "type" : "org.diylc.common.PCBLayer(Bottom,Top,Inner 1,Inner 2,Inner 3,Inner 4,Inner 5,Inner 6)"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.connectivity.SolderPad$Type(Round,Square,Oval horizontal,Oval vertical)"
  }, {
    "name" : "Size",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Tube Socket';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Tube Socket', '{
  "name" : "Tube Socket",
  "description" : "Various types of tube/valve sockets",
  "category" : "Tubes",
  "properties" : [ {
    "name" : "Electrode Labels",
    "type" : "java.lang.String"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Mount",
    "type" : "org.diylc.components.tube.TubeSocket$Mount(Chassis,PCB)"
  }, {
    "name" : "Designation",
    "type" : "java.lang.String"
  }, {
    "name" : "Base",
    "type" : "org.diylc.components.tube.TubeSocket$Base(Noval B9A,Octal,Small-button B7G,Duodecar B12C)"
  }, {
    "name" : "Angle",
    "type" : "org.diylc.core.Angle"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: 4-pin Jumbo Tube Socket';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: 4-pin Jumbo Tube Socket', '{
  "name" : "4-pin Jumbo Tube Socket",
  "description" : "4-pin Jumbo ceramic tube socket for 211, 805 and 845 tubes",
  "category" : "Tubes",
  "properties" : [ {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Terminal",
    "type" : "java.awt.Color"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Designation",
    "type" : "java.lang.String"
  }, {
    "name" : "Angle",
    "type" : "org.diylc.core.Angle"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Sub-Mini Tube';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Sub-Mini Tube', '{
  "name" : "Sub-Mini Tube",
  "description" : "Sub-miniature (pencil) vacuum tube",
  "category" : "Tubes",
  "properties" : [ {
    "name" : "Lead Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Lead Count",
    "type" : "org.diylc.components.tube.SubminiTube$PinCount(3,4,5,6,7,8,9,10)"
  }, {
    "name" : "Lead Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Diameter",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Pin Arrangement",
    "type" : "org.diylc.components.tube.SubminiTube$PinArrangement(In-line,Circular)"
  }, {
    "name" : "Top Lead",
    "type" : "boolean"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Folded",
    "type" : "boolean"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Voltage Source';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Voltage Source', '{
  "name" : "Voltage Source",
  "description" : "Voltage Source schematic symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Source Type",
    "type" : "org.diylc.components.misc.VoltageSourceSymbol$SourceType(AC,DC)"
  }, {
    "name" : "Voltage",
    "type" : "org.diylc.core.measures.Voltage"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Position",
    "type" : "org.diylc.common.LabelPosition(Above,Below)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Transformer Core';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Transformer Core', '{
  "name" : "Transformer Core",
  "description" : "Transformer core symbol. Use multiple instances together with \"Transformer Coil Symbol\"<br>to draw transformer schematics.",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Potentiometer';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Potentiometer', '{
  "name" : "Potentiometer",
  "description" : "Potentiometer symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Flip",
    "type" : "org.diylc.components.semiconductors.SymbolFlipping(None,X-axis,Y-axis)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Schottky Diode';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Schottky Diode', '{
  "name" : "Schottky Diode",
  "description" : "Schottky diode schematic symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Label Position",
    "type" : "org.diylc.common.LabelPosition(Above,Below)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Transformer Coil';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Transformer Coil', '{
  "name" : "Transformer Coil",
  "description" : "Transformer coil symbol. Use multiple instances together with \"Transformer Core Symbol\"<br>to draw transformer schematics.",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Size",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Voltage"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Zener Diode';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Zener Diode', '{
  "name" : "Zener Diode",
  "description" : "Zener diode schematic symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Label Position",
    "type" : "org.diylc.common.LabelPosition(Above,Below)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Photo Diode';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Photo Diode', '{
  "name" : "Photo Diode",
  "description" : "Photo Diode schematic symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Label Position",
    "type" : "org.diylc.common.LabelPosition(Above,Below)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: MOSFET';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: MOSFET', '{
  "name" : "MOSFET",
  "description" : "MOSFET transistor schematic symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Channel",
    "type" : "org.diylc.components.semiconductors.FETPolarity(Negative,Positive)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Flip",
    "type" : "org.diylc.components.semiconductors.SymbolFlipping(None,X-axis,Y-axis)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Logic Gate';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Logic Gate', '{
  "name" : "Logic Gate",
  "description" : "Basic logic gate schematic symbols",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.semiconductors.LogicGateSymbol$GateType(Buffer,Not,Or,And,Xor,Nor,Nand,Xnor)"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Triode';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Triode', '{
  "name" : "Triode",
  "description" : "Triode tube symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Directly heated",
    "type" : "boolean"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Heaters",
    "type" : "boolean"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Flip",
    "type" : "org.diylc.components.semiconductors.SymbolFlipping(None,X-axis,Y-axis)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Switch (Latching)';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Switch (Latching)', '{
  "name" : "Switch (Latching)",
  "description" : "Schematic symbol of various types of latching switches",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Poles",
    "type" : "org.diylc.components.electromechanical.SwitchLatchingSymbol$PoleCount(One,Two,Three,Four,Five,Six)"
  }, {
    "name" : "Configuration",
    "type" : "org.diylc.components.electromechanical.SwitchLatchingSymbol$SwitchConfiguration(1 throws / 2 positions,2 throws / 2 positions,2 throws / 3 positions / OFF even positions,3 throws / 3 positions,3 throws / 5 positions / SHORT even positions,4 throws / 4 positions,5 throws / 5 positions,6 throws / 6 positions,7 throws / 7 positions,8 throws / 8 positions,9 throws / 9 positions,10 throws / 10 positions,11 throws / 11 positions,12 throws / 12 positions)"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: JFET';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: JFET', '{
  "name" : "JFET",
  "description" : "JFET transistor schematic symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Channel",
    "type" : "org.diylc.components.semiconductors.FETPolarity(Negative,Positive)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Flip",
    "type" : "org.diylc.components.semiconductors.SymbolFlipping(None,X-axis,Y-axis)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Bulb';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Bulb', '{
  "name" : "Bulb",
  "description" : "Bulb schematic symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Filament Voltage",
    "type" : "org.diylc.core.measures.Voltage"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Position",
    "type" : "org.diylc.common.LabelPosition(Above,Below)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Diode';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Diode', '{
  "name" : "Diode",
  "description" : "Diode schematic symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Label Position",
    "type" : "org.diylc.common.LabelPosition(Above,Below)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Inductor';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Inductor', '{
  "name" : "Inductor",
  "description" : "Inductor schematic symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Resistance",
    "type" : "org.diylc.core.measures.Resistance"
  }, {
    "name" : "Current",
    "type" : "org.diylc.core.measures.Current"
  }, {
    "name" : "Core",
    "type" : "boolean"
  }, {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Inductance"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Position",
    "type" : "org.diylc.common.LabelPosition(Above,Below)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Duo-Diode';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Duo-Diode', '{
  "name" : "Duo-Diode",
  "description" : "Duo-diode tube symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Directly heated",
    "type" : "boolean"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Heaters",
    "type" : "boolean"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Flip",
    "type" : "org.diylc.components.semiconductors.SymbolFlipping(None,X-axis,Y-axis)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Diode Tube';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Diode Tube', '{
  "name" : "Diode Tube",
  "description" : "Diode tube symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Directly heated",
    "type" : "boolean"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Heaters",
    "type" : "boolean"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Flip",
    "type" : "org.diylc.components.semiconductors.SymbolFlipping(None,X-axis,Y-axis)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: LED';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: LED', '{
  "name" : "LED",
  "description" : "Diode schematic symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Label Position",
    "type" : "org.diylc.common.LabelPosition(Above,Below)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: IC';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: IC', '{
  "name" : "IC",
  "description" : "IC symbol with 3 or 5 contacts",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Contacts",
    "type" : "org.diylc.components.semiconductors.ICPointCount(3,5)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Flip",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Resistor';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Resistor', '{
  "name" : "Resistor",
  "description" : "Resistor schematic symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Draw Standard",
    "type" : "org.diylc.components.passive.ResistorSymbol$DrawStyle(ANSI,IEC)"
  }, {
    "name" : "Power Rating",
    "type" : "org.diylc.core.measures.Power"
  }, {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Resistance"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Position",
    "type" : "org.diylc.common.LabelPosition(Above,Below)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Battery';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Battery', '{
  "name" : "Battery",
  "description" : "Battery schematic symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Voltage",
    "type" : "org.diylc.core.measures.Voltage"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Position",
    "type" : "org.diylc.common.LabelPosition(Above,Below)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Phono Jack';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Phono Jack', '{
  "name" : "Phono Jack",
  "description" : "Connectors typically used for analog audio signals",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Type",
    "type" : "org.diylc.components.electromechanical.PhonoJackSymbol$PhonoJackType(Mono,Stereo)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Flip",
    "type" : "org.diylc.components.semiconductors.SymbolFlipping(None,X-axis,Y-axis)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Buzzer Symbol';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Buzzer Symbol', '{
  "name" : "Buzzer Symbol",
  "description" : "Buzzer schematic symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Voltage",
    "type" : "org.diylc.core.measures.Voltage"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Position",
    "type" : "org.diylc.common.LabelPosition(Above,Below)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Capacitor';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Capacitor', '{
  "name" : "Capacitor",
  "description" : "Capacitor schematic symbol with an optional polarity sign",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Voltage",
    "type" : "org.diylc.core.measures.Voltage"
  }, {
    "name" : "Polarized",
    "type" : "boolean"
  }, {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Capacitance"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Position",
    "type" : "org.diylc.common.LabelPosition(Above,Below)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: BJT';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: BJT', '{
  "name" : "BJT",
  "description" : "Bipolar junction transistor schematic symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Polarity",
    "type" : "org.diylc.components.semiconductors.BJTPolarity(NPN,PNP)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Flip",
    "type" : "org.diylc.components.semiconductors.SymbolFlipping(None,X-axis,Y-axis)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Current Source';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Current Source', '{
  "name" : "Current Source",
  "description" : "Current Source schematic symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Current",
    "type" : "org.diylc.core.measures.Current"
  }, {
    "name" : "Source Style",
    "type" : "org.diylc.components.misc.CurrentSourceSymbol$SourceStyle(Standard,Alternate)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Position",
    "type" : "org.diylc.common.LabelPosition(Above,Below)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Ground';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Ground', '{
  "name" : "Ground",
  "description" : "Ground schematic symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Stroke",
    "type" : "int"
  }, {
    "name" : "Style",
    "type" : "org.diylc.components.misc.GroundSymbol$GroundSymbolType(Default,Triangle)"
  }, {
    "name" : "Size",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Pentode';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Pentode', '{
  "name" : "Pentode",
  "description" : "Pentode tube symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Suppressor grid",
    "type" : "boolean"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Heaters",
    "type" : "boolean"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Flip",
    "type" : "org.diylc.components.semiconductors.SymbolFlipping(None,X-axis,Y-axis)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Fuse';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Fuse', '{
  "name" : "Fuse",
  "description" : "Fuse schematic symbol",
  "category" : "Schematic Symbols",
  "properties" : [ {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Current"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label Position",
    "type" : "org.diylc.common.LabelPosition(Above,Below)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Label Orientation",
    "type" : "org.diylc.components.AbstractLeadedComponent$LabelOriantation(Directional,Horizontal)"
  }, {
    "name" : "Label Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Lead Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Ellipse';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Ellipse', '{
  "name" : "Ellipse",
  "description" : "Elliptical area",
  "category" : "Shapes",
  "properties" : [ {
    "name" : "Dimension Mode",
    "type" : "org.diylc.components.shapes.AbstractShapeWithDimensions$ShapeSizingMode(Opposing Points,Explicit Dimensions)"
  }, {
    "name" : "Explicit Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Explicit Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Border Thickness",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Rectangle';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Rectangle', '{
  "name" : "Rectangle",
  "description" : "Ractangular area, with or withouth rounded edges",
  "category" : "Shapes",
  "properties" : [ {
    "name" : "Radius",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Dimension Mode",
    "type" : "org.diylc.components.shapes.AbstractShapeWithDimensions$ShapeSizingMode(Opposing Points,Explicit Dimensions)"
  }, {
    "name" : "Explicit Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Explicit Length",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Border Thickness",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Line';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Line', '{
  "name" : "Line",
  "description" : "Line with optional arrows",
  "category" : "Shapes",
  "properties" : [ {
    "name" : "Thickness",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Arrow Size",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Start Arrow",
    "type" : "boolean"
  }, {
    "name" : "End Arrow",
    "type" : "boolean"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Style",
    "type" : "org.diylc.common.LineStyle(Solid,Dashed,Dotted)"
  }, {
    "name" : "Moveable Label",
    "type" : "boolean"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Polygon';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Polygon', '{
  "name" : "Polygon",
  "description" : "Polygonal area",
  "category" : "Shapes",
  "properties" : [ {
    "name" : "Edges",
    "type" : "org.diylc.components.shapes.Polygon$PointCount(3,4,5,6,7,8)"
  }, {
    "name" : "Border Thickness",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: SMD Resistor';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: SMD Resistor', '{
  "name" : "SMD Resistor",
  "description" : "Surface mount resistor",
  "category" : "SMD",
  "properties" : [ {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Resistance"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Size",
    "type" : "org.diylc.components.smd.PassiveSMDComponent$SMDSize(0805,1206,1210,1806,1812)"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: SMD Capacitor';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: SMD Capacitor', '{
  "name" : "SMD Capacitor",
  "description" : "Surface mount capacitor",
  "category" : "SMD",
  "properties" : [ {
    "name" : "Value",
    "type" : "org.diylc.core.measures.Capacitance"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Size",
    "type" : "org.diylc.components.smd.PassiveSMDComponent$SMDSize(0805,1206,1210,1806,1812)"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: DIP Switch';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: DIP Switch', '{
  "name" : "DIP Switch",
  "description" : "Dual-in-line package switch",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Tick",
    "type" : "java.awt.Color"
  }, {
    "name" : "Pin Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Switches",
    "type" : "org.diylc.components.electromechanical.DIPSwitch$SwitchCount(1,2,3,4,5,6,7,8,9,10,11,12)"
  }, {
    "name" : "Row Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Width",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: 9V Battery Snap';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: 9V Battery Snap', '{
  "name" : "9V Battery Snap",
  "description" : "",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Plastic DC Jack';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Plastic DC Jack', '{
  "name" : "Plastic DC Jack",
  "description" : "Panel mount plastic DC jack",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Polarity",
    "type" : "org.diylc.components.electromechanical.DCPolarity(None,Center Positive,Center Negative)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Closed 1/4" Jack';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Closed 1/4" Jack', '{
  "name" : "Closed 1/4\" Jack",
  "description" : "Enclosed panel mount 1/4\" phono jack",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.electromechanical.JackType(Mono,Stereo)"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Rotary Selector Switch';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Rotary Selector Switch', '{
  "name" : "Rotary Selector Switch",
  "description" : "Single pole rotary switch, typically used for impedance selector",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Positions",
    "type" : "org.diylc.components.electromechanical.RotarySelectorSwitch$RotaryPositionCount(Two,Three,Four)"
  }, {
    "name" : "Labels",
    "type" : "boolean"
  }, {
    "name" : "Timing",
    "type" : "org.diylc.components.electromechanical.SwitchTiming(Shorting,Non-shorting)"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Tactile Micro-Switch';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Tactile Micro-Switch', '{
  "name" : "Tactile Micro-Switch",
  "description" : "4-pin tactile momentary switch",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Pilot Lamp Holder';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Pilot Lamp Holder', '{
  "name" : "Pilot Lamp Holder",
  "description" : "Fender-style pilot bulb holder for T2 and T-3 ï¿½ miniature bayonet lamps",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Angle",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Chassis Panel';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Chassis Panel', '{
  "name" : "Chassis Panel",
  "description" : "One side of a chassis, with or withouth rounded edges",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Radius",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Border Thickness",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Elliptical Cutout';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Elliptical Cutout', '{
  "name" : "Elliptical Cutout",
  "description" : "Elliptical chassis cutout",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Border Thickness",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Cliff 1/4" Jack';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Cliff 1/4" Jack', '{
  "name" : "Cliff 1/4\" Jack",
  "description" : "Cliff-style closed panel mount 1/4\" phono jack",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.electromechanical.JackType(Mono,Stereo)"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Cliff 1/8" Jack';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Cliff 1/8" Jack', '{
  "name" : "Cliff 1/8\" Jack",
  "description" : "Cliff-style closed 1/8\" phono jack",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Nut",
    "type" : "boolean"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.electromechanical.JackType(Mono,Stereo)"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Mini Relay';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Mini Relay', '{
  "name" : "Mini Relay",
  "description" : "Miniature PCB mount relay, like Omron G5V-1 or G5V-2",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Voltage",
    "type" : "org.diylc.core.measures.Voltage"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.electromechanical.MiniRelay$RelayType(SPDT,DPDT)"
  }, {
    "name" : "Size",
    "type" : "org.diylc.components.electromechanical.MiniRelay$RelaySize(Miniature,Ultra-miniature)"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Polygonal Cutout';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Polygonal Cutout', '{
  "name" : "Polygonal Cutout",
  "description" : "Polygonal chassis cutout",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Edges",
    "type" : "org.diylc.components.chassis.PolygonalCutout$PointCount(3,4,5,6,7,8)"
  }, {
    "name" : "Border Thickness",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Rotary Switch (Sealed)';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Rotary Switch (Sealed)', '{
  "name" : "Rotary Switch (Sealed)",
  "description" : "Sealed plastic rotary switch in several different switching configurations",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Terminals",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Mount",
    "type" : "org.diylc.components.electromechanical.RotarySwitchSealed$Mount(Chassis,PCB)"
  }, {
    "name" : "Timing",
    "type" : "org.diylc.components.electromechanical.SwitchTiming(Shorting,Non-shorting)"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.electromechanical.RotarySwitchSealedType(1P12T,2P6T,3P4T,4P3T)"
  }, {
    "name" : "Angle",
    "type" : "org.diylc.core.Angle"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Neutrik 1/4" Jack';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Neutrik 1/4" Jack', '{
  "name" : "Neutrik 1/4\" Jack",
  "description" : "1/4\" mono/stereo phono jack based on Neutrik NMJx series, PCB or Panel-mount",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Mount",
    "type" : "org.diylc.components.electromechanical.NeutrikJack1_4$Mount(PCB,Panel)"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.electromechanical.JackType(Mono,Stereo)"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Open 1/4" Jack';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Open 1/4" Jack', '{
  "name" : "Open 1/4\" Jack",
  "description" : "Switchcraft-style open panel mount 1/4\" phono jack, stereo and mono",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Labels",
    "type" : "boolean"
  }, {
    "name" : "Angle",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.electromechanical.OpenJack1_4$OpenJackType(Mono,Stereo,Switched)"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Open 1/8" Jack';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Open 1/8" Jack', '{
  "name" : "Open 1/8\" Jack",
  "description" : "Switchcraft-style open panel mount 1/8\" phono jack, stereo and mono",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Labels",
    "type" : "boolean"
  }, {
    "name" : "Angle",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.electromechanical.OpenJack1_8$OpenJackType(Mono,Stereo,Switched)"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: IEC Socket';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: IEC Socket', '{
  "name" : "IEC Socket",
  "description" : "Panel mounted IEC power socket",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Fuse Holder (Panel)';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Fuse Holder (Panel)', '{
  "name" : "Fuse Holder (Panel)",
  "description" : "Panel mounted fuse holder",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.OrientationHV(Vertical,Horizontal)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Display",
    "type" : "org.diylc.common.Display(Name,Value,None,Both)"
  }, {
    "name" : "Fuse",
    "type" : "boolean"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  }, {
    "name" : "Value",
    "type" : "java.lang.String"
  }, {
    "name" : "Font Size Override",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Rotary Switch (Open)';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Rotary Switch (Open)', '{
  "name" : "Rotary Switch (Open)",
  "description" : "Open rotary switch in several different switching configurations",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Angle Offset",
    "type" : "org.diylc.core.Angle"
  }, {
    "name" : "Terminals",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Selected Position",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Markers",
    "type" : "boolean"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Timing",
    "type" : "org.diylc.components.electromechanical.SwitchTiming(Shorting,Non-shorting)"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.electromechanical.RotarySwitchOpenType(2P5T,2P6T,4P5T,4P6T)"
  }, {
    "name" : "Angle",
    "type" : "org.diylc.core.Angle"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Rectangular Cutout';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Rectangular Cutout', '{
  "name" : "Rectangular Cutout",
  "description" : "Rectangular chassis cutout, with or without rounded edges",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Radius",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Border Thickness",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: RCA Jack';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: RCA Jack', '{
  "name" : "RCA Jack",
  "description" : "Panel mount RCA phono jack socket",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Pin Header';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Pin Header', '{
  "name" : "Pin Header",
  "description" : "PCB mount male pin header with editable number or pins and pin spacing",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Rows",
    "type" : "int"
  }, {
    "name" : "Columns",
    "type" : "int"
  }, {
    "name" : "Shrouded",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "Shroud",
    "type" : "java.awt.Color"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.Orientation(Default,90 degrees clockwise,180 degrees clockwise,270 degrees clockwise)"
  }, {
    "name" : "Color",
    "type" : "java.awt.Color"
  }, {
    "name" : "Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Mini Toggle Switch';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Mini Toggle Switch', '{
  "name" : "Mini Toggle Switch",
  "description" : "Panel mounted mini toggle switch",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Pad",
    "type" : "java.awt.Color"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.OrientationHV(Vertical,Horizontal)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Selected Position",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Markers",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "Terminal",
    "type" : "java.awt.Color"
  }, {
    "name" : "Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.electromechanical.ToggleSwitchType(SPST,SPDT (On/On),SPDT (On/Off/On),DPDT (On/On),DPDT (On/Off/On),DPDT (On/On/On Type 1),DPDT (On/On/On Type 2),3PDT,3PDT (On/Off/On),4PDT,4PDT (On/Off/On),4PDT (On/On/On Type 1),4PDT (On/On/On Type 2),5PDT,5PDT (On/Off/On))"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  } ]
}');
DELETE FROM diylc_knowledge_base WHERE section = 'Component Type: Slide Switch';
INSERT INTO diylc_knowledge_base (category, section, content) ('Component Types', 'Component Type: Slide Switch', '{
  "name" : "Slide Switch",
  "description" : "Panel mounted slide switch",
  "category" : "Electro-Mechanical",
  "properties" : [ {
    "name" : "Column Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Show Bracket",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "Bracket",
    "type" : "java.awt.Color"
  }, {
    "name" : "Orientation",
    "type" : "org.diylc.common.OrientationHV(Vertical,Horizontal)"
  }, {
    "name" : "Body",
    "type" : "java.awt.Color"
  }, {
    "name" : "Border",
    "type" : "java.awt.Color"
  }, {
    "name" : "Label",
    "type" : "java.awt.Color"
  }, {
    "name" : "Selected Position",
    "type" : "java.lang.Integer"
  }, {
    "name" : "Markers",
    "type" : "java.lang.Boolean"
  }, {
    "name" : "Row Spacing",
    "type" : "org.diylc.core.measures.Size"
  }, {
    "name" : "Type",
    "type" : "org.diylc.components.electromechanical.SlideSwitchType(SPDT,DPDT,DP3T)"
  }, {
    "name" : "Alpha",
    "type" : "java.lang.Byte"
  }, {
    "name" : "Name",
    "type" : "java.lang.String"
  } ]
}');
