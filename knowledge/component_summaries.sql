DELETE FROM diylc_knowledge_base WHERE section = 'Summary: Guitar Components';
INSERT INTO diylc_knowledge_base (category, section, content) VALUES ('Component Summaries', 'Summary: Guitar Components', 'The Guitar category features components designed for guitar electronics, including various pickups (e.g., Schaller Megaswitch, Stingray Bass Pickup, Single Coil, Humbucker, P-90, Jazz Bass Pickup) and specialized switches (e.g., LP Toggle Switch, Lever Switch). Their properties such as color, orientation, marker options, type selections, and transparency allow realistic modeling of guitar circuits.');

DELETE FROM diylc_knowledge_base WHERE section = 'Summary: Passive Components';
INSERT INTO diylc_knowledge_base (category, section, content) VALUES ('Component Summaries', 'Summary: Passive Components', 'Passive components include fundamental circuit elements such as resistors, capacitors (electrolytic, film, tantalum, ceramic, mica, multi-section), inductors, and potentiometers. Key properties include electrical ratings (resistance, capacitance, voltage, power), physical dimensions, spacing, and visual display settings, which are essential for designing filters, bias networks, and other core circuit functions.');

DELETE FROM diylc_knowledge_base WHERE section = 'Summary: Board Components';
INSERT INTO diylc_knowledge_base (category, section, content) VALUES ('Component Summaries', 'Summary: Board Components', 'Board components represent the physical substrates for circuit assembly. This category includes blank boards, prototyping boards (such as Proto Board 780 Holes, Perf Board with pads, TriPad Boards), Tag Strips, and other specialized boards. Properties cover board shapes, dimensions, coordinate systems, color settings, borders, and options for underside display.');

DELETE FROM diylc_knowledge_base WHERE section = 'Summary: Schematic Symbols';
INSERT INTO diylc_knowledge_base (category, section, content) VALUES ('Component Summaries', 'Summary: Schematic Symbols', 'Schematic symbols are standardized graphical representations used in circuit diagrams to denote electrical components like resistors, capacitors, diodes, and transistors. They emphasize accurate sizing, labeling, and line styling to clearly communicate circuit functions.');

DELETE FROM diylc_knowledge_base WHERE section = 'Summary: Connectivity Components';
INSERT INTO diylc_knowledge_base (category, section, content) VALUES ('Component Summaries', 'Summary: Connectivity Components', 'Connectivity components include items such as connectors, jumpers, terminal blocks, and wiring interfaces. They represent the physical means of interconnecting circuit elements, with properties like pin count, spacing, orientation, and color coding ensuring accurate representation of connection points.');

DELETE FROM diylc_knowledge_base WHERE section = 'Summary: Electro-Mechanical Components';
INSERT INTO diylc_knowledge_base (category, section, content) VALUES ('Component Summaries', 'Summary: Electro-Mechanical Components', 'Electro-mechanical components blend electrical functionality with mechanical design. This category covers devices such as relays, various switches, and jacks, featuring mechanical dimensions, mounting details, and tactile properties that are essential for reliable physical operation.');

DELETE FROM diylc_knowledge_base WHERE section = 'Summary: Tube Components';
INSERT INTO diylc_knowledge_base (category, section, content) VALUES ('Component Summaries', 'Summary: Tube Components', 'Tube components are specialized for vintage and high-fidelity audio circuits, including vacuum tubes and associated circuitry. Their properties include filament specifications, tube configurations, pin arrangements, and thermal considerations, which are crucial for accurately modeling tube amplifiers and related designs.');

DELETE FROM diylc_knowledge_base WHERE section = 'Summary: Miscellaneous Components';
INSERT INTO diylc_knowledge_base (category, section, content) VALUES ('Component Summaries', 'Summary: Miscellaneous Components', 'The Miscellaneous Components category comprises various graphical elements, such as labels, images, graphical shapes, etc, that do not into other electronic categories. Their properties typically focus on display settings, positioning, and sizing options to help annotate and structure circuit diagrams effectively.');
