-- SQL script to insert DIYLC version history into diylc_knowledge table
-- Generated from update_archive.xml and update.xml data
-- Content is stored as JSON for easier parsing and flexibility

-- Version 3.0.4
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.0.4', 
'{
  "version": "3.0.4",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Copy/paste between two instances of the application"
    },
    {
      "type": "BUG FIX",
      "description": "Parsing numerical values in countries that use comma as decimal separator"
    },
    {
      "type": "BUG FIX",
      "description": "Dragging sticky points affects selectability of objects"
    },
    {
      "type": "BUG FIX",
      "description": "Click is sometimes not registered while adding a new component"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed NPE when using default values"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Allow shape control points of curves to overlap"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Added DIL IC first pin indentation"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Added transparency to the potentiometer"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Reduced number of layers"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Import from v1 files can now read label and electrolytic capacitor components and trace/pad colors"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Added worker thread for longer file operation, so GUI doesn''t freeze"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added right click menu on the canvas"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added \"send to back\" and \"bring to front\" options"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added label component"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added image component"
    }
  ],
  "releaseDate": "2011-01-27 21:00:00.000 CET"
}');

-- Version 3.0.5
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.0.5', 
'{
  "version": "3.0.5",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Curved trace does not show control points"
    },
    {
      "type": "BUG FIX",
      "description": "Potentiometer draws wrong at 180 degrees"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Reset component selection when tab is changed"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Resistor can show color bands"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Electrolytic capacitor has a new icon and new looks"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Solder pad can be square"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Added keyboard shortcuts to some menu options"
    },
    {
      "type": "IMPROVEMENT",
      "description": "V1 file importer can recognize hookup wire and transistors"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added support for creating and loading templates"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added TO-92 transistor"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added TO-220 transistor"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added LED"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added axial electrolytic capacitor"
    }
  ],
  "releaseDate": "2011-01-30 20:00:00.000 CET"
}');

-- Version 3.0.6
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.0.6', 
'{
  "version": "3.0.6",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Copy/paste doesn''t work for some components (potentiometer, transistor, etc)"
    },
    {
      "type": "BUG FIX",
      "description": "Resistor color codes are sometimes wrong"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Resistors can specify power handling"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Capacitors can specify voltage handling"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Fixed lead alignment issue"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Better scaling, grid lines are more precise"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved performance"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added resistor schematic symbol"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added capacitor schematic symbol"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added eyelet/turret component"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added Marshall/Trainwreck style boards"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added few types of trimmers"
    },
    {
      "type": "NEW FEATURE",
      "description": "Configurable rendering quality to improve speed"
    }
  ],
  "releaseDate": "2011-02-07 20:00:00.000 CET"
}');

-- Version 3.0.7
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.0.7', 
'{
  "version": "3.0.7",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Resistor and capacitor symbols are not clickable"
    },
    {
      "type": "BUG FIX",
      "description": "Boards sometimes render holes/pads on the edge"
    },
    {
      "type": "BUG FIX",
      "description": "Download link doesn''t work on Mac"
    },
    {
      "type": "BUG FIX",
      "description": "Ruler ticks don''t always match with the grid"
    },
    {
      "type": "BUG FIX",
      "description": "Micro symbol doesn''t get exported to PDF"
    },
    {
      "type": "IMPROVEMENT",
      "description": "DIL IC now has more pin options"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Send to Back/Bring to Front are more flexible and renamed to match what they do"
    },
    {
      "type": "NEW FEATURE",
      "description": "Auto-save project and restore if app crashes"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added support for themes"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added tube socket"
    },
    {
      "type": "NEW FEATURE",
      "description": "Sticky points may be turned off"
    },
    {
      "type": "NEW FEATURE",
      "description": "Snap to Grid may be turned off"
    },
    {
      "type": "NEW FEATURE",
      "description": "Auto-create solder pads"
    },
    {
      "type": "NEW FEATURE",
      "description": "Auto-edit mode"
    },
    {
      "type": "NEW FEATURE",
      "description": "Continuous component creation"
    }
  ],
  "releaseDate": "2011-02-27 20:00:00.000 CET"
}');

-- Version 3.0.8
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.0.8', 
'{
  "version": "3.0.8",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Sometimes it''s not possible to click on a component, especially when layout is large"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Curved traces and wires can have 2, 3 and 4 control points"
    },
    {
      "type": "NEW FEATURE",
      "description": "Switchable wheel zoom"
    },
    {
      "type": "NEW FEATURE",
      "description": "Automatic standing resistors, capacitors and diodes"
    },
    {
      "type": "NEW FEATURE",
      "description": "Vertical vero board"
    },
    {
      "type": "NEW FEATURE",
      "description": "Label has editable font"
    }
  ],
  "releaseDate": "2011-03-03 20:00:00.000 CET"
}');

-- Version 3.0.9
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.0.9', 
'{
  "version": "3.0.9",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Auto-created solder pads aren''t assigned with default settings and name"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Component edit dialog allows changing property values even when they are not the same. Boxes are marked yellow to designate multi-valued state."
    },
    {
      "type": "IMPROVEMENT",
      "description": "Component edit dialog should show mutual properties when components of different types are selected (e.g. curved trace and straight trace width)"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Ctrl+click should not clear selection when clicked on the canvas"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Increased PNG export resolution"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Auto-increment component names when copy-pasting"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Do not attempt to write auto-save files if user does not have permissions to create files"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Allow zero ohm resistors"
    },
    {
      "type": "NEW FEATURE",
      "description": "Outline mode that resembles PCB silkscreen"
    },
    {
      "type": "NEW FEATURE",
      "description": "Double click on a component button in the toolbar selects all components of that type"
    },
    {
      "type": "NEW FEATURE",
      "description": "Renumber selected components by X or Y axis"
    },
    {
      "type": "NEW FEATURE",
      "description": "Recently used components in the toolbar for easy navigation"
    }
  ],
  "releaseDate": "2011-08-28 20:00:00.000 CET"
}');

-- Version 3.1.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.1.0', 
'{
  "version": "3.1.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Selection sometimes highlights wrong component(s)"
    },
    {
      "type": "BUG FIX",
      "description": "Renumber function sometimes doesn''t work as expected"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Increased PNG export resolution to 300dpi"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Select All should not include locked components"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Better lead and label rendering when lead spacing is smaller than component body"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Allow more than 32 pins for IC"
    },
    {
      "type": "NEW FEATURE",
      "description": "Adjustable solder pad hole size"
    }
  ],
  "releaseDate": "2012-02-01 20:00:00.000 CET"
}');

-- Version 3.2.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.2.0', 
'{
  "version": "3.2.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Select components from context menu even when they are under other components"
    },
    {
      "type": "NEW FEATURE",
      "description": "Ctrl+Shift toggles \"Snap to Grid\" option"
    },
    {
      "type": "NEW FEATURE",
      "description": "Perfboards can show hole coordinates"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Smarter resistor labels; try not to overlap label with color code bands"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved import from v1 file format"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Imperial ruler is divided to .1\" instead of 1/4\""
    },
    {
      "type": "IMPROVEMENT",
      "description": "Allow finer grid spacing up to 0.5mm (0.02\")"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Do not hard-code voltage and power ratings"
    }
  ],
  "releaseDate": "2012-02-19 20:00:00.000 CET"
}');

-- Version 3.3.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.3.0', 
'{
  "version": "3.3.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Expand selection feature, useful for debugging"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Limit length of text in the status bar using \"and X more\" suffix"
    }
  ],
  "releaseDate": "2012-02-25 20:00:00.000 CET"
}');

-- Version 3.4.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.4.0', 
'{
  "version": "3.4.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "IC Symbol"
    },
    {
      "type": "NEW FEATURE",
      "description": "BJT Symbol"
    },
    {
      "type": "NEW FEATURE",
      "description": "MOSFET Symbol"
    },
    {
      "type": "NEW FEATURE",
      "description": "JFET Symbol"
    },
    {
      "type": "NEW FEATURE",
      "description": "Line connector"
    },
    {
      "type": "IMPROVEMENT",
      "description": "IC component can display name or value"
    },
    {
      "type": "BUG FIX",
      "description": "\"Index out of bounds\" exception logs when selecting components"
    },
    {
      "type": "BUG FIX",
      "description": "Loading files fails because encoding is not recognized"
    },
    {
      "type": "BUG FIX",
      "description": "Application breaks when resistance is set to 0 ohms"
    }
  ],
  "releaseDate": "2012-03-09 20:00:00.000 CET"
}');

-- Version 3.5.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.5.0', 
'{
  "version": "3.5.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Generic closed 1/4\" jack"
    },
    {
      "type": "NEW FEATURE",
      "description": "Cliff-style closed 1/4\" jack"
    },
    {
      "type": "NEW FEATURE",
      "description": "Ground Symbol"
    }
  ],
  "releaseDate": "2012-03-15 20:00:00.000 CET"
}');

-- Version 3.6.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.6.0', 
'{
  "version": "3.6.0",
  "changes": [
    {
      "type": "IMPROVEMENT",
      "description": "Fonts are exported to PDF"
    },
    {
      "type": "NEW FEATURE",
      "description": "Plastic DC jack"
    },
    {
      "type": "NEW FEATURE",
      "description": "Humbucker pickup"
    },
    {
      "type": "NEW FEATURE",
      "description": "Single coil pickup"
    }
  ],
  "releaseDate": "2012-03-21 20:00:00.000 CET"
}');

-- Version 3.7.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.7.0', 
'{
  "version": "3.7.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Lever guitar switch"
    },
    {
      "type": "NEW FEATURE",
      "description": "9V battery snap"
    }
  ],
  "releaseDate": "2012-03-27 20:00:00.000 CET"
}');

-- Version 3.8.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.8.0', 
'{
  "version": "3.8.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Right click rotates component while laying out on the canvas"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added DIY file association for Windows (run associations.bat)"
    },
    {
      "type": "IMPROVEMENT",
      "description": "More usable \"Bring to Forward\" and \"Send to Backward\""
    },
    {
      "type": "IMPROVEMENT",
      "description": "Faster copy-paste functionality"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved auto-save not to slow down the app"
    }
  ],
  "releaseDate": "2012-08-16 20:00:00.000 CET"
}');

-- Version 3.9.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.9.0', 
'{
  "version": "3.9.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Move selected components with arrow keys (and ctrl/shift)"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Right-click in the toolbar to select all components of the same type"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Click on a component in the toolbar sometimes doesn''t register"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Remove junk from BOM (eyelets, wires, traces)"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved \"Recently Used\" toolbox, doesn''t jump as you click"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Control number of \"Recently Used\" items from config.properties"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Faster component placement, auto-focus on \"Value\" box"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Transistors can show name or value"
    },
    {
      "type": "IMPROVEMENT",
      "description": "UNICODE font export to PDF files"
    },
    {
      "type": "IMPROVEMENT",
      "description": "More intuitive unsaved file warning message"
    }
  ],
  "releaseDate": "2012-08-20 20:00:00.000 CET"
}');

-- Version 3.10.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.10.0', 
'{
  "version": "3.10.0",
  "changes": [
    {
      "type": "IMPROVEMENT",
      "description": "Improved overall performance"
    },
    {
      "type": "BUG FIX",
      "description": "Should be more stable under OSX and not crash unexpectedly"
    }
  ],
  "releaseDate": "2012-08-20 20:00:00.000 CET"
}');

-- Version 3.11.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.11.0', 
'{
  "version": "3.11.0",
  "changes": [
    {
      "type": "IMPROVEMENT",
      "description": "Some components (like Image) can be placed in any layer using Send to Back/Bring to Front"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Replaced deep cloning library with manual implementation"
    }
  ],
  "releaseDate": "2012-08-21 20:00:00.000 CET"
}');

-- Version 3.12.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.12.0', 
'{
  "version": "3.12.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Rectangle selection triggers file (modified) state"
    },
    {
      "type": "BUG FIX",
      "description": "Two components are created instead of one"
    },
    {
      "type": "BUG FIX",
      "description": "Undo-Redo repeated several times messes up selection"
    },
    {
      "type": "BUG FIX",
      "description": "Loading DIY file that contains images sometimes breaks"
    },
    {
      "type": "BUG FIX",
      "description": "Image disappears while scrolling if not completely in the visible area"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Auto-Edit opens the editor on for components that make sense (e.g. NOT for traces, solder pads, etc)"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Better sorting in the BOM (e.g. R2 should go before R10)"
    }
  ],
  "releaseDate": "2012-08-23 20:00:00.000 CET"
}');

-- Version 3.13.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.13.0', 
'{
  "version": "3.13.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Problems opening older files that have BOM"
    },
    {
      "type": "NEW FEATURE",
      "description": "Polygon Ground Fill component"
    },
    {
      "type": "NEW FEATURE",
      "description": "TO-1 transistor component"
    },
    {
      "type": "NEW FEATURE",
      "description": "Multi-layer PCB support; outputs each layer''s trace mask to a separate page"
    },
    {
      "type": "NEW FEATURE",
      "description": "Editable BOM color"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Show voltage and power ratings in the BOM where applicable"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Folded TO-92 transistors"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Block user actions during long operations (print, export, etc)"
    }
  ],
  "releaseDate": "2012-08-23 20:00:00.000 CET"
}');

-- Version 3.14.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.14.0', 
'{
  "version": "3.14.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Ruler jumps when scrolling"
    },
    {
      "type": "BUG FIX",
      "description": "Error when importing V1 files that contain potentiometers"
    },
    {
      "type": "NEW FEATURE",
      "description": "Miniature and ultra-miniature PCB mount relays"
    },
    {
      "type": "NEW FEATURE",
      "description": "TO-3 transistors"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Folded electrolytic capacitors"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Folded TO-220 transistor"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Label vertical and horizontal alignment can be changed"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Editable pin spacing for all radial components"
    }
  ],
  "releaseDate": "2012-08-27 20:00:00.000 CET"
}');

-- Version 3.15.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.15.0', 
'{
  "version": "3.15.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added support for component templates"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Hidden \"default\" checkboxes from the editor because templates cover that functionality. If you need them back however, uncomment org.diylc.swing.gui.editor.PropertyEditorDialog.SHOW_DEFAULT_BOXES=true in the config.properties file"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Ability to rotate toggle switch"
    }
  ],
  "releaseDate": "2012-08-31 20:00:00.000 CET"
}');

-- Version 3.16.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.16.0', 
'{
  "version": "3.16.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Ability to rotate text"
    },
    {
      "type": "NEW FEATURE",
      "description": "Connector dot component"
    },
    {
      "type": "BUG FIX",
      "description": "Radial components become transparent by themself"
    },
    {
      "type": "BUG FIX",
      "description": "Context menu doesn''t show on MacOS"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Ability to flip standing diodes"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Allow zero sized holes on solder pads"
    }
  ],
  "releaseDate": "2012-09-17 20:00:00.000 CET"
}');

-- Version 3.17.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.17.0', 
'{
  "version": "3.17.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Triode tube symbol"
    },
    {
      "type": "NEW FEATURE",
      "description": "Pentode tube symbol"
    },
    {
      "type": "NEW FEATURE",
      "description": "Diode tube symbol"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Editable label color for leaded components"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Outline mode for switches, jacks, trimmers, potentiometers and transistors"
    }
  ],
  "releaseDate": "2012-09-24 20:00:00.000 CET"
}');

-- Version 3.18.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.18.0', 
'{
  "version": "3.18.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Components don''t show up in the toolbox on OSX"
    },
    {
      "type": "NEW FEATURE",
      "description": "Inductor symbol"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Component values (resistance, capacitance, etc) are created blank instead of defaulting to some values"
    }
  ],
  "releaseDate": "2012-09-24 20:00:00.000 CET"
}');

-- Version 3.19.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.19.0', 
'{
  "version": "3.19.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Cannot save a component template if value is not set"
    },
    {
      "type": "NEW FEATURE",
      "description": "Mirrored text for PCB artwork"
    }
  ],
  "releaseDate": "2012-09-24 20:00:00.000 CET"
}');

-- Version 3.20.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.20.0', 
'{
  "version": "3.20.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "BOM doesn''t work when there''s a component without a value set"
    },
    {
      "type": "BUG FIX",
      "description": "Editing multiple components doesn''t work there''s a component without a value set"
    },
    {
      "type": "NEW FEATURE",
      "description": "Diode symbol"
    },
    {
      "type": "NEW FEATURE",
      "description": "Zener diode symbol"
    },
    {
      "type": "NEW FEATURE",
      "description": "Schottky diode symbol"
    },
    {
      "type": "NEW FEATURE",
      "description": "LED symbol"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Bring back default check boxes in the editor"
    }
  ],
  "releaseDate": "2012-09-24 20:00:00.000 CET"
}');

-- Version 3.21.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.21.0', 
'{
  "version": "3.21.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Configuration and component templates are lost when restarting the app"
    }
  ],
  "releaseDate": "2012-10-1 20:00:00.000 CET"
}');

-- Version 3.22.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.22.0', 
'{
  "version": "3.22.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Two Voltage properties for ceramic capacitors"
    },
    {
      "type": "BUG FIX",
      "description": "Cannot create ceramic capacitor if there''s a default value for voltage"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Copy paste functionality improved"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Component templates save shape of the component (for wires, traces and such)"
    }
  ],
  "releaseDate": "2012-10-08 20:00:00.000 CET"
}');

-- Version 3.23.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.23.0', 
'{
  "version": "3.23.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "TO-3 transistor rotation is wrong for 180 degrees"
    },
    {
      "type": "BUG FIX",
      "description": "Pasted components are placed below traces"
    },
    {
      "type": "BUG FIX",
      "description": "Right click changes selection"
    },
    {
      "type": "NEW FEATURE",
      "description": "SIP IC component"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Editable colors for DIP IC"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Clearer standing component (mainly diode) reversion"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Ctrl + right click on component type in toolbox appends components of that type to selection"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Editable lead color; changed default lead color"
    }
  ],
  "releaseDate": "2012-10-08 20:00:00.000 CET"
}');

-- Version 3.24.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.24.0', 
'{
  "version": "3.24.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Snap to grid after releasing Ctrl button and creating a new component"
    },
    {
      "type": "BUG FIX",
      "description": "Paste throws an error"
    },
    {
      "type": "BUG FIX",
      "description": "Potentiometer doesn''t update when size only is changed"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Faster startup when using image component templates"
    }
  ],
  "releaseDate": "2013-03-04 20:00:00.000 CET"
}');

-- Version 3.25.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.25.0', 
'{
  "version": "3.25.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Large components disappear when none of the edges is in the visible area of the screen"
    },
    {
      "type": "NEW FEATURE",
      "description": "Breadboard component"
    }
  ],
  "releaseDate": "2013-04-22 20:00:00.000 CET"
}');

-- Version 3.26.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.26.0', 
'{
  "version": "3.26.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Large components disappear when none of the edges is in the visible area of the screen"
    },
    {
      "type": "NEW FEATURE",
      "description": "Ability to rotate selection"
    },
    {
      "type": "IMPROVEMENT",
      "description": "More predictable movement of multiple components at the same time"
    }
  ],
  "releaseDate": "2013-04-25 20:00:00.000 CET"
}');

-- Version 3.27.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.27.0', 
'{
  "version": "3.27.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Moving multiple components doesn''t honor snap to grid OFF setting"
    },
    {
      "type": "BUG FIX",
      "description": "Do not change selection on right click when right clicking on one of the selected components"
    },
    {
      "type": "BUG FIX",
      "description": "Right click rotation during component creation throws an error"
    },
    {
      "type": "NEW FEATURE",
      "description": "Ability to import existing DIY files into a project"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added primitive shapes - ellipse, polygon and (rounded) rectangle"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added tri-pad board (thanks Hauke!)"
    },
    {
      "type": "NEW FEATURE",
      "description": "Apply template to one or more existing components from the context menu"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Trace cuts can be placed between two holes for tighter layouts (double click trace to find \"Cut between holes\")"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Moved all schematic symbols to a dedicated category"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Allow grid spacing as little as 0.1mm"
    }
  ],
  "releaseDate": "2013-04-30 20:00:00.000 CET"
}');

-- Version 3.28.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.28.0', 
'{
  "version": "3.28.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Issue with dragging multiple components"
    },
    {
      "type": "BUG FIX",
      "description": "Issue with component selection"
    },
    {
      "type": "NEW FEATURE",
      "description": "Select all within a layer"
    }
  ],
  "releaseDate": "2013-08-15 20:00:00.000 CET"
}');

-- Version 3.29.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.29.0', 
'{
  "version": "3.29.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Issue with losing saved user configuration"
    },
    {
      "type": "BUG FIX",
      "description": "Issue with disabled \"Save as Template\" context menu item problem, as well as issue with strange rotation and probably few more"
    },
    {
      "type": "BUG FIX",
      "description": "Undo steps kept after opening a new file"
    },
    {
      "type": "NEW FEATURE",
      "description": "Transistors can be rotated and flipped vertically"
    },
    {
      "type": "NEW FEATURE",
      "description": "DIL ICs can show pin numbers"
    },
    {
      "type": "NEW FEATURE",
      "description": "Display both component value and name or neither of them"
    },
    {
      "type": "NEW FEATURE",
      "description": "Potentiometer schematic symbol added"
    }
  ],
  "releaseDate": "2016-04-20 20:00:00.000 CET"
}');

-- Version 3.30.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.30.0', 
'{
  "version": "3.30.0",
  "changes": [
    {
      "type": "IMPROVEMENT",
      "description": "Improved potentiometer symbol appearance"
    }
  ],
  "releaseDate": "2016-04-25 20:00:00.000 CET"
}');

-- Version 3.31.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.31.0', 
'{
  "version": "3.31.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Undo works only one level back (bug introduced in 3.29)"
    }
  ],
  "releaseDate": "2016-05-09 20:00:00.000 CET"
}');

-- Version 3.32.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.32.0', 
'{
  "version": "3.32.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Project Cloud feature introduced with global project sharing functionality"
    },
    {
      "type": "NEW FEATURE",
      "description": "Transformer core and coil schematic symbols added that can be used to put together custom transformer symbols"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added Public Announcements feature"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Potentiometer and transistor symbols can be flipped horizontally"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Reduced DIYLC package size"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Auto save and recovery made a bit smarter"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Popup dialogs work better with keyboard - Enter confirms, Escape dismisses dialogs"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Make order of items in the ''Edit Project'' dialog more logical"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Better support for old V1 file import"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Toggle switch terminal spacing can be changed"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Allow multi-line project description"
    },
    {
      "type": "BUG FIX",
      "description": "Changing Display to one trimmer affects all trimmers and does not get saved to the file"
    },
    {
      "type": "BUG FIX",
      "description": "Solder pad component should not allow negative size"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed silent error when having a resistor without a valid value (e.g. missing units)"
    }
  ],
  "releaseDate": "2016-11-12 20:00:00.000 CET"
}');

-- Version 3.33.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.33.0', 
'{
  "version": "3.33.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Terminal strip component added"
    },
    {
      "type": "NEW FEATURE",
      "description": "Battery schematic symbol added"
    },
    {
      "type": "NEW FEATURE",
      "description": "Glass diode component added"
    },
    {
      "type": "NEW FEATURE",
      "description": "SMD capacitor and resistor added"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Fixed the issue with zooming in on traces and similar components that would not change on zooming"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Tube, transistor and potentiometer symbols can be rotated"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Added percent text box next to the slider for editors like Alpha or Scale"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Better object line representation when zoomed in"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Increased maximum zoom range from 200% to 300%"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved pentode symbol without the suppressor grid"
    }
  ],
  "releaseDate": "2016-12-23 20:00:00.000 CET"
}');

-- Version 3.34.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.34.0', 
'{
  "version": "3.34.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Searchable component tree instead of the old toolbar (can be changed back from \"Config\" menu)"
    },
    {
      "type": "NEW FEATURE",
      "description": "Open 1/4\" Switchcraft-style jack added"
    },
    {
      "type": "NEW FEATURE",
      "description": "LP-style switch added"
    },
    {
      "type": "NEW FEATURE",
      "description": "P-90 pickup in both \"dog ear\" and \"soap bar\" variations added"
    },
    {
      "type": "NEW FEATURE",
      "description": "Telecaster bridge pickup added as a variation of single coil pickup"
    },
    {
      "type": "NEW FEATURE",
      "description": "Mini humbucker pickup added as a variation of Humbucker pickup"
    },
    {
      "type": "IMPROVEMENT",
      "description": "X-axis coordinates for all perfboards are now numberical"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved trace cut rendering to look more realistic"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved guitar pickup rendering to look more realistic"
    },
    {
      "type": "BUG FIX",
      "description": "Using unicode characters (like micro character) in template names breaks the whole config file"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed broken links in the Help menu"
    },
    {
      "type": "BUG FIX",
      "description": "TriPad board didn''t show coordinate labels even when configured to do so"
    }
  ],
  "releaseDate": "2016-12-30 20:00:00.000 CET"
}');

-- Version 3.35.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.35.0', 
'{
  "version": "3.35.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Cannot type \"q\" letter in any of the boxes around the app"
    },
    {
      "type": "NEW FEATURE",
      "description": "Mirror selection horizontally and vertically"
    },
    {
      "type": "NEW FEATURE",
      "description": "Ability to set a default template for component type (grey \"pin\" icon in the template popup)"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Default focus on \"Text\" field for label"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Consolidated popup menu items with \"Edit\" menu"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Renamed menu actions for renumbering to be clearer"
    }
  ],
  "releaseDate": "2017-01-05 20:00:00.000 CET"
}');

-- Version 3.36.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.36.0', 
'{
  "version": "3.36.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Ability to quickly save a group of components as a building block and use it later"
    },
    {
      "type": "NEW FEATURE",
      "description": "Show/hide rulers"
    },
    {
      "type": "NEW FEATURE",
      "description": "Show/hide the grid"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Ability to type in hex value in color editor"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Ability to rotate tube sockets for arbitrary angle and change color"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Flip tube schematic symbols"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Renamed \"template\" to \"variant\" as it is describes the meaning more closely"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Applying a variant/template shouldn''t affect component placement"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved rotation and mirroring of multiple components at the same time"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved resistor rendering to look more realistic"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Editable single coil pickup pole pieces (rods or rails) and pole piece color"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Editable humbucker pickup pole pieces (rods or rails) and pole piece color"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Editable P90 pickup pole piece color"
    },
    {
      "type": "BUG FIX",
      "description": "Component type gets selected while expanding folders in the component tree"
    }
  ],
  "releaseDate": "2017-01-26 20:00:00.000 CET"
}');

-- Version 3.37.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.37.0', 
'{
  "version": "3.37.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Thumbnail sometimes gets very small when uploading a project to the cloud"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Ability to change resistor shape (tubular or standard \"dog bone\")"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Added more control over Breadboard size, orientation and appearance"
    }
  ],
  "releaseDate": "2017-04-10 20:00:00.000 CET"
}');

-- Version 3.38.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.38.0', 
'{
  "version": "3.38.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added ''Recent Files'' to the main menu to keep track of the previous 10 files"
    },
    {
      "type": "NEW FEATURE",
      "description": "Hold Control key to zoom with mouse wheel. Zooming (somewhat) tracks mouse cursor position"
    },
    {
      "type": "NEW FEATURE",
      "description": "Ability to set project-wide default font through ''Edit Project Settings'' dialog"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added wizard installer for Windows that associates the app with .DIY files automatically"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Moved config and auto-save file locations to standard user directory instead of using the app directory"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Do not allow creating zero-length components (e.g. traces, lines, etc)"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Hookup wire can be sent to back behind boards"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Solder pads can be sent behind copper traces to allow creating white border around traces on top of a ground plane"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Ability to edit color of copper traces, curved traces and solder pads at the same time when they are all selected"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Tooltip doesn''t cover buttons in the right side of the status bar"
    },
    {
      "type": "BUG FIX",
      "description": "Component library empty if the app is installed in a folder that contains special characters in the name"
    }
  ],
  "releaseDate": "2017-04-20 20:00:00.000 CET"
}');

-- Version 3.39.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.39.0', 
'{
  "version": "3.39.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Assign F1-F12 keys as shortcuts for frequently used component types or building blocks"
    },
    {
      "type": "NEW FEATURE",
      "description": "Type-in selection nudge"
    },
    {
      "type": "NEW FEATURE",
      "description": "Show and hide each individual layer"
    },
    {
      "type": "NEW FEATURE",
      "description": "Red ticks on both rulers mark selection bounds, blue tick tracks cursor position"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improve precision for Solder Pad and Copper Trace by not rounding the size up or down"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Show selection size in both inches and centimeters"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Use standard OSX/Mac \"command\" key for menu shortcuts and to un-stuck components"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Use balloon to show announcements and update notifications"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Moved logs to user''s home directory"
    },
    {
      "type": "BUG FIX",
      "description": "Issues with DIL and SIL IC pin alignment when using metric grid and pin spacing"
    },
    {
      "type": "BUG FIX",
      "description": "Resistor changes shape on its own"
    }
  ],
  "releaseDate": "2017-04-25 00:00:00.000 CET"
}');

-- Version 3.40.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.40.0', 
'{
  "version": "3.40.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Import files created in DIYLC v2"
    },
    {
      "type": "NEW FEATURE",
      "description": "Ability to force components to the front or back of other components even outisde their designated layers, allowing placing components or jumpers below boards, etc"
    },
    {
      "type": "NEW FEATURE",
      "description": "''Tilde'' key repeats the last added component"
    },
    {
      "type": "NEW FEATURE",
      "description": "Duplicate selection option in Edit menu and popup menu (Ctrl+D)"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added bitchin new splash screen"
    },
    {
      "type": "NEW FEATURE",
      "description": "Potentiometers can show an optional shaft, can have either solder lugs or PCB pins and rendering includes the wafer"
    },
    {
      "type": "NEW FEATURE",
      "description": "Ability to change PCB board shape from rectangular to oval."
    },
    {
      "type": "IMPROVEMENT",
      "description": "Do not block the UI while checking for new version at startup (can be problematic when there''s connectivity issues)"
    },
    {
      "type": "IMPROVEMENT",
      "description": "DIL IC label rotates to fit the longer side of the component. Name and value are displayed in separate lines"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Better axial electrolytic rendering. Changed default color of all electrolytic capacitors"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Editable toggle switch color"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Changed border color of memory status bar in bottom-right corner not to be red under OSX"
    }
  ],
  "releaseDate": "2017-05-08 00:00:00.000 CET"
}');

-- Version 3.41.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.41.0', 
'{
  "version": "3.41.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Using right click to draw a selection rectangle doesn''t move objects under the cursor (useful for selecting multiple components that have a big board below)"
    },
    {
      "type": "NEW FEATURE",
      "description": "\"Cut Line\" component added to designate where a custom eyelet/turret boards needs to be cut"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Increased padding between schematic symbols and their labels"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Better label position on resistors"
    },
    {
      "type": "BUG FIX",
      "description": "Auto-save dialog pops up when running multiple instances of the app at the same time"
    },
    {
      "type": "BUG FIX",
      "description": "Left mouse click on a component in the component tree sometimes has no effect"
    }
  ],
  "releaseDate": "2018-12-11 00:00:00.000 CET"
}');

-- Version 3.41.1
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.41.1', 
'{
  "version": "3.41.1",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Automatically delete logs older than a week"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed building blocks that stopped working in version 3.40.0"
    }
  ],
  "releaseDate": "2018-12-11 00:00:00.000 CET"
}');

-- Version 3.42.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.42.0', 
'{
  "version": "3.42.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "New in \"Config\" menu - highlight connected areas through copper traces, leads, jumpers, etc..."
    },
    {
      "type": "NEW FEATURE",
      "description": "Holding \"shift\" key switches mouse wheel scrolling function from vertical to horizontal"
    },
    {
      "type": "NEW FEATURE",
      "description": "Three new trimmer potentiometer types"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Preserve z-order of components when copy-pasting"
    },
    {
      "type": "BUG FIX",
      "description": "File chooser dialogs don''t append file extension if folder path has a dot character in it"
    },
    {
      "type": "BUG FIX",
      "description": "\"Cut Line\" component sometimes disappears"
    },
    {
      "type": "BUG FIX",
      "description": "Measurement tool shows incorrect readings when zoomed in or out"
    },
    {
      "type": "BUG FIX",
      "description": "Components in the tree sometimes stop responding until deselected and selected again"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed BOM export to Excel"
    }
  ],
  "releaseDate": "2018-12-16 00:00:00.000 CET"
}');

-- Version 3.42.1
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.42.1', 
'{
  "version": "3.42.1",
  "changes": [
    {
      "type": "IMPROVEMENT",
      "description": "Added usage hints for new functionality"
    }
  ],
  "releaseDate": "2018-12-16 00:00:00.000 CET"
}');

-- Version 3.43.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.43.0', 
'{
  "version": "3.43.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added \"Dashed\" and \"Dotted\" style to \"Hookup Wire\", \"Jumper\" and \"Line\" components"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added five and seven control point options to curved components for more flexibility (literally)"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added \"Turret Lug\" component"
    }
  ],
  "releaseDate": "2018-12-17 00:00:00.000 CET"
}');

-- Version 3.44.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.44.0', 
'{
  "version": "3.44.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added few chassis-related components under \"Electromechanical\" category"
    },
    {
      "type": "IMPROVEMENT",
      "description": "All file export actions (PNG, PDF, BOM...) take the current file name as a default"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Center visible area to the selection when using mouse wheel to zoom in and out"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Change mouse cursor when in \"Highlight Connected Areas\" mode to avoid confusion"
    },
    {
      "type": "BUG FIX",
      "description": "Thin dashed and dotted lines turn to solid when zoomed in"
    }
  ],
  "releaseDate": "2018-12-18 00:00:00.000 CET"
}');

-- Version 3.44.1
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.44.1', 
'{
  "version": "3.44.1",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Fixed OSX \"No compatible version of Java 1.8+ is available.\" error"
    }
  ],
  "releaseDate": "2018-12-21 00:00:00.000 CET"
}');

-- Version 3.45.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.45.0', 
'{
  "version": "3.45.0",
  "changes": [
    {
      "type": "IMPROVEMENT",
      "description": "Changed the way fonts are initialized, hoping to reduce a chance of dialogs hanging"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed selection issues on right click in OSX that make it impossible to use Variants and other features"
    },
    {
      "type": "BUG FIX",
      "description": "Reverted undesired change for three-point curved traces and wires which made them slightly more rounded than before version 3.43.0"
    },
    {
      "type": "BUG FIX",
      "description": "\"Recent Files\" causes the whole menu to be rendered at two places under MacOS"
    }
  ],
  "releaseDate": "2018-12-25 00:00:00.000 CET"
}');

-- Version 3.46.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.46.0', 
'{
  "version": "3.46.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Oval solder pads, available as new types of regular \"Solder Pad\" components"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Faster click and popup menu response due to improved performance of selection area calculations, especially with bigger layouts"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Do not group multiple components automatically after being pasted"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Better error handing in V2 file importer"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Rotate commands now applies to \"Label\" and \"PCB Text\" components"
    },
    {
      "type": "BUG FIX",
      "description": "Clicking on a building block in the component tree sometimes makes the components go back to the top-left corner of the layout"
    },
    {
      "type": "BUG FIX",
      "description": "Ctrl + click doesn''t add components to the selection in MacOS"
    }
  ],
  "releaseDate": "2018-12-26 00:00:00.000 CET"
}');

-- Version 3.46.1
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.46.1', 
'{
  "version": "3.46.1",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Revert unintentional change for MacOs for \"command\" and \"control\" keys for zooming and selection"
    }
  ],
  "releaseDate": "2018-12-26 00:00:00.000 CET"
}');

-- Version 3.47.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.47.0', 
'{
  "version": "3.47.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Multi-section Electrolytic \"Can\" Capacitor added"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added visual indicator to the component tree showing how many variants, if any, exist for a component"
    },
    {
      "type": "NEW FEATURE",
      "description": "DIYLC now comes with predefined component variants out-of-the box, in addition to user-specific variants"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Changed default font to \"Square721 BT\" that looks more technical and is packaged with the app"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Right-click rotate while placing components on the canvas now applies to tube sockets as well"
    },
    {
      "type": "BUG FIX",
      "description": "Fix \"Mouse Wheel Zoom\" feature that got broken in 3.46.1"
    },
    {
      "type": "BUG FIX",
      "description": "Include Themes (under Config menu) in the Windows installer version"
    },
    {
      "type": "BUG FIX",
      "description": "Covered humbucker pickup throwing errors when rotated"
    }
  ],
  "releaseDate": "2019-01-09 00:00:00.000 CET"
}');

-- Version 3.48.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.48.0', 
'{
  "version": "3.48.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added \"Label Orientation\" property to leaded components that can be used to force component labels to be rendered horizontally instead of following component placement direction"
    },
    {
      "type": "NEW FEATURE",
      "description": "\"Jazz Bass Pickup\" added"
    },
    {
      "type": "NEW FEATURE",
      "description": "\"P-Bass Pickup\" added"
    },
    {
      "type": "NEW FEATURE",
      "description": "Filtertron pickup type added within \"Humbucker Pickup\" as well as screw pole pieces for all humbucker types"
    },
    {
      "type": "NEW FEATURE",
      "description": "\"DIP Switch\" added"
    },
    {
      "type": "NEW FEATURE",
      "description": "\"Tantalum Capacitor\" added"
    },
    {
      "type": "NEW FEATURE",
      "description": "\"IEC Socket\" added"
    },
    {
      "type": "NEW FEATURE",
      "description": "\"Logic Gate\" added with 8 main gate types"
    },
    {
      "type": "NEW FEATURE",
      "description": "Ability to have \"Single-coil Pickup\" and \"Humbucker Pickup\" with no poles, similar to EMG, Lace Sensor..."
    },
    {
      "type": "IMPROVEMENT",
      "description": "Render resistor color bands and capacitor polarity markings properly when zoomed in"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Do not allow negative values for properties indicating a size"
    },
    {
      "type": "BUG FIX",
      "description": "Project font gets reverted to \"Square721 BT\" on Undo"
    },
    {
      "type": "BUG FIX",
      "description": "Some actions do not register with Undo/Redo mechanism: project font, layer settings, group/ungroup"
    },
    {
      "type": "BUG FIX",
      "description": "When loaded from an existing file, \"Potentiometer Symbol\" doesn''t retain the specified orientation"
    }
  ],
  "releaseDate": "2019-01-13 00:00:00.000 CET"
}');

-- Version 3.49.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.49.0', 
'{
  "version": "3.49.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Middle mouse click activates mouse movement scroll mode, similar to web browsers"
    },
    {
      "type": "NEW FEATURE",
      "description": "When \"High Quality Rendering\" option is checked, some components are rendered with 3D shading for better looks"
    },
    {
      "type": "NEW FEATURE",
      "description": "\"Fuse Holder\" added"
    },
    {
      "type": "NEW FEATURE",
      "description": "\"Bridge Rectifier\" added"
    },
    {
      "type": "NEW FEATURE",
      "description": "Changed default project font again to \"Lucida Console\" because \"Square721 BT\" doesn''t render Ohm symbol properly"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added optional arrow to the \"Line\" component together with configurable thickness"
    },
    {
      "type": "NEW FEATURE",
      "description": "Ability to flip polarity of IC symbol"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Use hardware acceleration for rendering to improve performance and reduce memory usage"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Use transparency instead of rendering pins on top of transistor body"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Leads and wires are rendered better, especially when zoomed in"
    }
  ],
  "releaseDate": "2019-01-16 00:00:00.000 CET"
}');

-- Version 3.50.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.50.0', 
'{
  "version": "3.50.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "\"Signal Transformer\" added"
    },
    {
      "type": "NEW FEATURE",
      "description": "\"Pilot Lamp Holder\" added"
    },
    {
      "type": "NEW FEATURE",
      "description": "\"Radial Inductor\" added"
    },
    {
      "type": "NEW FEATURE",
      "description": "\"Toroidal Inductor\" added"
    },
    {
      "type": "NEW FEATURE",
      "description": "Configurable graphics hardware acceleration"
    },
    {
      "type": "NEW FEATURE",
      "description": "More flexibility with coordinate display for boards"
    },
    {
      "type": "NEW FEATURE",
      "description": "\"Recent Updates\" added to status bar and Help menu"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Fixed the issue with Ohm symbol rendering with some fonts and changed default project font back to \"Square721 BT\""
    },
    {
      "type": "IMPROVEMENT",
      "description": "Better color band rendering on resistors and smarter label positioning"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Added few pedal enclosure variants"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Chrome-style cursors for mouse scrolling with arrows showing the exact scrolling direction"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Do not render various jack components as completely transparent while dragging"
    },
    {
      "type": "BUG FIX",
      "description": "Since 3.49, straight traces are rendered 1px thinner than curved traces of the same thickness"
    },
    {
      "type": "BUG FIX",
      "description": "Since 3.49, some capacitor variants have disappeared. The fix brings them back"
    }
  ],
  "releaseDate": "2019-01-23 00:00:00.000 CET"
}');

-- Version 3.51.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.51.0', 
'{
  "version": "3.51.0",
  "changes": [
    {
      "type": "IMPROVEMENT",
      "description": "Better selection rendering for a few components, mostly electro-mechanical"
    },
    {
      "type": "BUG FIX",
      "description": "Jumper color switches to red on its own"
    },
    {
      "type": "BUG FIX",
      "description": "Lead alignment sometimes gets rendered off by 1px for resistor and capacitor schematic symbol"
    },
    {
      "type": "BUG FIX",
      "description": "Reverted Ohm symbol changes that made it render wrong on some systems"
    },
    {
      "type": "BUG FIX",
      "description": "Update dialog doesn''t show up when clicked on the light bulb icon in the status bar"
    }
  ],
  "releaseDate": "2019-01-28 00:00:00.000 CET"
}');

-- Version 3.52.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.52.0', 
'{
  "version": "3.52.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Ability to drag components directly from the tree to the canvas for faster process"
    },
    {
      "type": "NEW FEATURE",
      "description": "\"Twisted Leads\" added (DIYLC v2 fans rejoice)"
    },
    {
      "type": "NEW FEATURE",
      "description": "\"Fuse Symbol\" added"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Better rendering precision when exporting to PDF and PNG files"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Added confirmation dialog before deleting a building block"
    },
    {
      "type": "BUG FIX",
      "description": "\"PCB Text\" components are not included in trace mask exports"
    }
  ],
  "releaseDate": "2019-02-03 00:00:00.000 CET"
}');

-- Version 3.53.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.53.0', 
'{
  "version": "3.53.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added extra work space around the layout that can be used to store additional components and helpers, but does not get exported to a file. Configurable from the menu"
    },
    {
      "type": "NEW FEATURE",
      "description": "Ability to export and import building blocks and variants from the \"File\" menu"
    },
    {
      "type": "NEW FEATURE",
      "description": "Show mouse cursor coordinates in the status bar"
    },
    {
      "type": "NEW FEATURE",
      "description": "\"RCA Jack\" added"
    },
    {
      "type": "NEW FEATURE",
      "description": "Add switched type to \"Open 1/4\" Jack\" component"
    },
    {
      "type": "NEW FEATURE",
      "description": "Selectable coordinate origin point for all perforated boards"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Vast improvement of rendering performance and memory management, especially with large layouts"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Create a backup of config file when there is an issue with loading (for example running two DIYLC versions on the same machine) to avoid data loss"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Automatically select dropped components from the component tree after they get placed"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Better rendering of IC and transistor pins when zoomed in"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Better rendering of guitar pickups when dragging and in outline mode"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Better rendering of TO-3 transistor package"
    },
    {
      "type": "BUG FIX",
      "description": "Drag&Drop from the component tree sometimes picks up a wrong component type"
    },
    {
      "type": "BUG FIX",
      "description": "\"Select All\" menu option from the component tree context menu doesn''t work"
    },
    {
      "type": "BUG FIX",
      "description": "Components do not load if the installation directory name contains a special character, like ''!''"
    },
    {
      "type": "BUG FIX",
      "description": "Do not allow zero for size and spacing properties that could lead to crashes or unexpected behavior"
    }
  ],
  "releaseDate": "2019-02-09 00:00:00.000 CET"
}');

-- Version 3.54.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.54.0', 
'{
  "version": "3.54.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "\"Create Netlist\" feature added under File -> Analyze"
    },
    {
      "type": "NEW FEATURE",
      "description": "\"Analyze Guitar Wiring\" feature added under File -> Analyze"
    },
    {
      "type": "NEW FEATURE",
      "description": "Guitar pickups now have separate terminals for start and finish point of each coil. Single coil pickups have polarity property added"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added center-OFF options for \"Mini Toggle Switch\""
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ability to mark common lugs on all \"Lever Switch\" types with a different color"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improve performance of \"Image\" component and greatly reduce file size when images are present"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Allow a file to be loaded if it references a missing component property, but show a warning"
    },
    {
      "type": "BUG FIX",
      "description": "Version 3.53 would not start unless there was an older version running on the same machine before"
    }
  ],
  "releaseDate": "2019-02-26 00:00:00.000 CET"
}');

-- Version 3.55.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.55.0', 
'{
  "version": "3.55.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "\"Analyze Guitar Wiring\" can figure out if wiring is hum-cancelling and in or out of phase for each position, split-coil wiring, series/parallel humbucking mode, volume and tone potentiometers"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added two or four terminal hookup to bass pickups types"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added 4-position Tele switch type to the \"Lever Switch\" component"
    },
    {
      "type": "NEW FEATURE",
      "description": "Show wire diameter in mm and in next to AWG gauge"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Increase the range of zoom slider for images to 500%"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Increase the number of recently used files from 10 to 20"
    },
    {
      "type": "BUG FIX",
      "description": "Netlist algorithm would sometimes create overlapping groups of nodes which are supposed to be merged"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed bug with \"Highlight Connected Areas\" that makes random components conductive"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed rendering issue with \"Lever Switch\" when rotated"
    }
  ],
  "releaseDate": "2019-03-02 00:00:00.000 CET"
}');

-- Version 3.55.1
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.55.1', 
'{
  "version": "3.55.1",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Z-order of components is not retained in building blocks"
    }
  ],
  "releaseDate": "2019-03-02 00:00:00.000 CET"
}');

-- Version 3.56.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.56.0', 
'{
  "version": "3.56.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Ability to tag components and building blocks as favorites and recall them quickly from a separate folder in the component tree"
    },
    {
      "type": "NEW FEATURE",
      "description": "Generate SPICE-compatible netlist (still in beta stage)"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added pinout property to all transistor components"
    },
    {
      "type": "NEW FEATURE",
      "description": "Ability to enter arbitrary angle of rotation for \"Open 1/4\" Jack\" and \"Pilot Lamp Holder\""
    },
    {
      "type": "IMPROVEMENT",
      "description": "When in \"Highlight Connected Area\" mode, the status bar shows useful information making it harder to forget what the mode does"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Cleaner and more compact XML format for .DIY files resulting in files about half the size of originals. Backward compatible with older files, but will introduce issues when opening files from the new version using an old version!"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Detect when a file is created with a newer version of DIYLC and show a warning"
    },
    {
      "type": "BUG FIX",
      "description": "\"Highlight Connected Areas\" doesn''t follow multiple chained wires and jumpers"
    },
    {
      "type": "BUG FIX",
      "description": "\"Mini Toggle Switch\" doesn''t reset body size after changing terminal spacing"
    },
    {
      "type": "BUG FIX",
      "description": "\"Ground Symbol\" jumps around when being copy-pasted together with other components"
    }
  ],
  "releaseDate": "2019-03-10 00:00:00.000 CET"
}');

-- Version 3.57.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 3.57.0', 
'{
  "version": "3.57.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added an optional third control point to leaded components and some schematic symbols that can be used to set component label position. This feature is off by default to keep things backwards compatible and is driven by \"Movable Label\" property"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added mini toolbar in the top-right corner of the screen with transform actions exposed for quick access"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added \"Auto-Wrap Label\" component"
    },
    {
      "type": "NEW FEATURE",
      "description": "Auto-save plugin now works by periodically creating project backups in user\\.diylc\\backup directory and maintains file history, keeping the last X versions of the file. Maximum size for all backups combined is configurable and pre-set to 64MB"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Ability to show labels for standing components and control the position"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Assign a new name for pasted component if the project already contains components under that name"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Output SPICE node names in the \"Nxxx\" format"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improve \"Expand Selection\" feature to use the same algorithm as netlists for better precision"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improve performance of netlist calculations"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Honor \"Terminal Strip\" connectivity when creating a netlist"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Honor all possible combinations of \"DIP Switch\" positions when creating a netlist"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Merge switch configurations that yield with the same effective wiring for \"Analyze Guitar Wirings\" module, for a more compact report"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Scan \"library\" folder for any additional JAR files that may contain more components"
    },
    {
      "type": "BUG FIX",
      "description": "When pasting a group of components that contain at least one \"PCB Text\" instance, some of the other components get rendered mirrored while position the pasted components"
    },
    {
      "type": "BUG FIX",
      "description": "Some schematic symbols (transistors, potentiometer...) are not rendered properly when rotated and flipped at the same time"
    },
    {
      "type": "BUG FIX",
      "description": "Z-order of components sometimes gets wrong when importing a DIY file into another file"
    }
  ],
  "releaseDate": "2019-03-24 00:00:00.000 CET"
}');

-- Version 4.0.1
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.0.1', 
'{
  "version": "4.0.1",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added \"Tape Measure\" component for measuring the distance between any two points on the drawing"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ability to set board size explicitly by typing in dimensions, instead of dragging two control points"
    },
    {
      "type": "NEW FEATURE",
      "description": "New app bundle for Mac OS using appbundler instead of javabundler"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added \"Snap To Components\" feature that snaps control points to control points of the nearest components"
    },
    {
      "type": "NEW FEATURE",
      "description": "New app icon!"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ability to collapse and expand the component tree panel to the left edge of the screen"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added read-only \"Length\" property to curved components (i.e. \"Hookup Wire\") that calculates total length of the curve"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added \"Renumber On Paste\" option in the \"Config\" menu"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added orientation parameter to \"Ground Simbol\" to allow rotation"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Upgraded to Java 8"
    },
    {
      "type": "BUG FIX",
      "description": "The third control point of resistors and similar components appears in the Spice netlist by mistake"
    },
    {
      "type": "BUG FIX",
      "description": "Hide the 3rd control point for components that do not need it (Trace, Jumper, Line)"
    },
    {
      "type": "BUG FIX",
      "description": "\"Auto-Create Pads\" doesn''t work for ICs and similar non-stretchable components"
    },
    {
      "type": "BUG FIX",
      "description": "Threads on 1/4\" Closed Jack were not rendered at all"
    }
  ],
  "releaseDate": "2020-03-26 00:00:00.000 CET"
}');

-- Version 4.1.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.1.0', 
'{
  "version": "4.1.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added \"Dot Spacing\" property in project settings that shows dots on every N-th grid line intersection"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added \"P-0+o (Proto) Board\" component by Kristian Bl\u00e5sol"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added \"Pin Header\" component"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Added ability to delete default component variants"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Moved \"Tape Measure\" to \"Misc\" category"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Better \"Expand Selection\" logic. Works well with conductive components like traces and wires and regular components like resistors and capacitors"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Refactor dependencies to internal Java classes that are market as proprietary in Java 8 (could potentially affect \"Hi-Quality Rendering\" mode and loading older DIY files that contain images)"
    },
    {
      "type": "BUG FIX",
      "description": "Fix \"Ohm\" symbol rendering issue with some fonts by switching to the regular Omega character instead of the dedicated Ohm symbol that is absent in most fonts"
    },
    {
      "type": "BUG FIX",
      "description": "Issues with circuit analysis introduced in version 4.0.1. Wires and jumpers are made non-conductive by mistake."
    },
    {
      "type": "BUG FIX",
      "description": "Issues with connectivity check with some components (e.g. Jumper) where the middle of the component was considered as a connected point"
    },
    {
      "type": "BUG FIX",
      "description": "When moving or deleting trace cuts from a vero board, highlight connected areas would still render the area from before the cut was deleted"
    },
    {
      "type": "BUG FIX",
      "description": "4P5T and DP5T guitar switches were not properly configured for internal pin connections"
    },
    {
      "type": "BUG FIX",
      "description": "\"Humbucker Pickup\" doesn''t render second coil poles properly in certain scenarios"
    },
    {
      "type": "BUG FIX",
      "description": "Minor fix in tracking conductive areas"
    },
    {
      "type": "BUG FIX",
      "description": "Cannot edit \"Hookup Wire\" properties when opening existing files"
    }
  ],
  "releaseDate": "2020-04-03 00:00:00.000 CET"
}');

-- Version 4.2.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.2.0', 
'{
  "version": "4.2.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Exposed few of the most commonly used configuration items in the top-right toolbar for faster access"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added \"Ruler Inch Subdivision\" config option that allows inch rulers to be subdivided either using base of 2 (1/4, 1/8, 1/16) or base of 10 (1/10)"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added few out-of-the-box building blocks that can be deleted if unwanted"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added TO-126 transistor package"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added \"Duodecar B12C\" tube socket type"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved performance of \"Highlight Connected Areas\" feature, especially with bigger projects"
    },
    {
      "type": "BUG FIX",
      "description": "Choosing a default theme in the Config menut causes the app to fail to start and some components to fail to render"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed the issue with tube symbol mirroring"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed tube socket pins that can be dragged independently from the socket"
    }
  ],
  "releaseDate": "2020-04-05 00:00:00.000 CET"
}');

-- Version 4.3.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.3.0', 
'{
  "version": "4.3.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added component-level caching that should speed up rendering dramatically. While in experimental phase, it will be exposed as \"Enable Cache Boost\" item in the \"Config\" menu so it can be turned off if it causes any issues"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved performance math calculations used to model \"Twisted Wires\""
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved performance of \"Highlight Connected Areas\" feature"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Faster auto-scroll while dragging a component outside the visible portion of the canvas"
    },
    {
      "type": "BUG FIX",
      "description": "Older files that contain \"Open Jack 1/4\" components sometimes open with the jacks rotated the wrong way"
    },
    {
      "type": "BUG FIX",
      "description": "Switched terminal on the \"Open Jack 1/4\" does not stick to other components"
    },
    {
      "type": "BUG FIX",
      "description": "With multiple components selected, if one of them is a single-point component (pad, eyelet, etc), starting a drag operation on that components results in losing the selection on the others"
    }
  ],
  "releaseDate": "2020-04-09 00:00:00.000 CET"
}');

-- Version 4.4.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.4.0', 
'{
  "version": "4.4.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added \"Find\" functionality that locates and selects component(s) matching the search text"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added \"Sub-Mini Tube\" component"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added \"Tag Strip\" component for point-to-point layouts"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added \"Duo-Diode\" tube symbol"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added \"Access User Files\" in the Help menu that takes you to logs, backup and config file location"
    },
    {
      "type": "NEW FEATURE",
      "description": "Transistor components can display pinout instead of name or value"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added optional outer foil marking for all film capacitors"
    },
    {
      "type": "NEW FEATURE",
      "description": "\"Trace Cut\" can now be placed horizontally or vertically when \"Cut Between Lines\" is selected"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Prevent the app from closing while running background operations, like saving and exporting files"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed the issue with \"Highlight Connected Areas\" not recognizing some conductive areas"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed Proto Board and Breadboard issues with \"Cache Boost\" mode enabled"
    },
    {
      "type": "BUG FIX",
      "description": "All terminals of a \"Terminal Strip\" were connected together by mistake"
    },
    {
      "type": "BUG FIX",
      "description": "Transistor would disappear when \"Folded\" is selected"
    }
  ],
  "releaseDate": "2020-04-16 00:00:00.000 CET"
}');

-- Version 4.5.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.5.0', 
'{
  "version": "4.5.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "\"Add Flexible Leads\" action added in the context menu that replaces straight leads of leaded components with flexible leads and adds leads to some components that do not have them (e.g. Transistors, Tube Sockets, Potentiometers, etc)"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ability to lock and unlock individual components from the context menu"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added \"Rotary Selector Switch\" component"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added support for automated regression testing of core functionalities which should reduce regression bugs in the long run"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added support for rotating and mirroring potentiometer symbol and transistor symbols"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added mirrored pin display options for \"DIL IC\""
    },
    {
      "type": "IMPROVEMENT",
      "description": "Fixed grid alignment issues with ruler marks when both the ruler and grid is in metric units"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Standardize how folded transistors are displayed - updated TO-92 and TO-1 packages"
    }
  ],
  "releaseDate": "2020-04-20 00:00:00.000 CET"
}');

-- Version 4.6.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.6.0', 
'{
  "version": "4.6.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Internationalization - added support for translating UI of the app to other languages. Get in touch if you wish to translate DIYLC to your language"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added \"Sizing Mode\" to Image component that allows stretching the image freely using two control points"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added \"Loadline Editor\" to the File menu, to be used in the future to calculate tube or transistor bias point using loadlines"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Include themes and fonts in OSX app package"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Do not show control points of locked components while dragging other components"
    },
    {
      "type": "BUG FIX",
      "description": "Some boards (e.g. Breadboard) stop working at certain orientations"
    },
    {
      "type": "BUG FIX",
      "description": "Component leads on resistors, capacitors, etc disappear when they are very short"
    },
    {
      "type": "BUG FIX",
      "description": "Prevent \"Radial Film Capacitor\" from switching to standing mode when end points are too close"
    }
  ],
  "releaseDate": "2020-05-03 00:00:00.000 CET"
}');

-- Version 4.7.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.7.0', 
'{
  "version": "4.7.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added \"Crystal Oscillator\" component"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added Italian UI translation"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Dismissing the closing confirmation dialog using the \"X\" button should behave the same as selecting \"Cancel\""
    },
    {
      "type": "IMPROVEMENT",
      "description": "Apply translation to the context menu"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Fixed the orientation of the second coil of humbucker pickups. Now it''s north start, north finish, south finish, south start"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved Spanish translation"
    },
    {
      "type": "BUG FIX",
      "description": "Projects created with version older than 4.5 that contain \"Trace Cut\" are showing errors when loaded in 4.5"
    }
  ],
  "releaseDate": "2020-05-11 00:00:00.000 CET"
}');

-- Version 4.8.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.8.0', 
'{
  "version": "4.8.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added German UI translation"
    },
    {
      "type": "NEW FEATURE",
      "description": "Use ALT key to temporarily switch to \"Highlight Continuity Area\" mode"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Reset \"Highlight Continuity Area\" mode configuration on application exit to avoid confusion in the next session"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved Dutch translation"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Make file writes more resilient to crashes to avoid loss of data"
    },
    {
      "type": "BUG FIX",
      "description": "\"Outline Mode\" produces rendering issues when \"Cache Boost\" feature is enabled"
    },
    {
      "type": "BUG FIX",
      "description": "Program won''t start with some older configuration files due to different format of themes"
    }
  ],
  "releaseDate": "2020-09-04 00:00:00.000 CET"
}');

-- Version 4.9.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.9.0', 
'{
  "version": "4.9.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added \"Phono Jack\" schematic symbol"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added keyboard shortcuts for zoom in and out using standard Ctrl and +/- key combination and Ctrl+0 to reset to 100%"
    },
    {
      "type": "NEW FEATURE",
      "description": "On MacOS the app registers to open .DIY files"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Switched from iText to OrsonPDF library for PDF creation. Render text as vectors to ensure consistency with display and avoid font issues"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Rewritten most of the code behind Searchable Tree functionality to make it faster and more stable"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved rendering precision, especially when using metric units"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improve snap to grid when moving a node that connects several components"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved board sizing, keep explicit dimensions in sync with control point location"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Shorten donation label when the window is smaller"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Allow rotate and mirror actions on Pin Header component"
    }
  ],
  "releaseDate": "2020-09-18 00:00:00.000 CET"
}');

-- Version 4.10.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.10.0', 
'{
  "version": "4.10.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "macOS: bundle JRE8 with the application to avoid JRE/JDK installation issues"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Store configuration using the same XML format used for serializing DIY files"
    },
    {
      "type": "IMPROVEMENT",
      "description": "macOS: added meta +/- keyboard shortcuts for zooming"
    },
    {
      "type": "BUG FIX",
      "description": "macOS Sierra: the app starts and immediately stops on some older mac systems"
    },
    {
      "type": "BUG FIX",
      "description": "Favorites on building blocks show up as \"null\""
    }
  ],
  "releaseDate": "2020-11-19 00:00:00.000 CET"
}');

-- Version 4.11.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.11.0', 
'{
  "version": "4.11.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Windows: bundle Java JRE with the installation to avoid Java version issues"
    },
    {
      "type": "NEW FEATURE",
      "description": "macOS: added quick action toolbar that was already available for other platforms"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ability to rotate images"
    },
    {
      "type": "BUG FIX",
      "description": "Project cloud -> replace project had no effect"
    }
  ],
  "releaseDate": "2020-12-06 00:00:00.000 CET"
}');

-- Version 4.12.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.12.0', 
'{
  "version": "4.12.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added a button in the action toolbar to toggle between snap modes (none, grid, component)"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added pinch zooming functionality for compatible devices (tested on Mac and Win laptops)"
    },
    {
      "type": "IMPROVEMENT",
      "description": "macOS: added meta +/- keyboard shortcuts for zooming"
    },
    {
      "type": "IMPROVEMENT",
      "description": "macOS: two finger click shows the popup menu without deselecting a selected component"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Fixed issues when creating leaded components using drag and drop with ''Snap to Grid'' option switched on using a metric grid spacing (e.g. 1mm)"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Do not allow for components like traces, jumpers and wires to be reduced to nothing by dragging connected components"
    },
    {
      "type": "IMPROVEMENT",
      "description": "In cases when there''s already a component in the selection that is reduced to nothing, allow moving the selection to avoid getting stuck"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Moving a rigid component (e.g. tube/transistor symbol) which is not snapped to grid makes it snap back to the grid when ''Snap to Grid'' option is switched on"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Apply context menu rotation and mirroring to Trimmer Potentiometer components"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Apply context menu rotation and mirroring to SMD Capacitor and Resistor components"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Apply context menu rotation and mirroring to Tag Strip components"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Apply context menu rotation to Breadboard and P-0+o (Proto) Board components"
    },
    {
      "type": "IMPROVEMENT",
      "description": "When rotating Vero and Tri-Pad boards, change the orientation of strips to match the rotation angle"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Rotating and mirroring boards that display coordinates also changes coordinate origin"
    },
    {
      "type": "BUG FIX",
      "description": "Dragging components from the component tree to the canvas stops working once the component type is selected in the tree"
    },
    {
      "type": "BUG FIX",
      "description": "macOS: fixed language selection that does not work"
    }
  ],
  "releaseDate": "2020-12-13 00:00:00.000 CET"
}');

-- Version 4.13.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.13.0', 
'{
  "version": "4.13.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added ''Check Trace Proximity'' functionality under File -> Analyze menu that checks if there are any conductive surfaces (e.g. traces or pads) that are too close to each other"
    },
    {
      "type": "NEW FEATURE",
      "description": "''Tactile Micro-Switch'' component added"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ''Font Size Override'' property to most components that display text to allow control over font size"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ''Neutrik Jack'' component based on Neutrik NMJx series. Mono/Stereo and PCB/Panel mount options are available"
    },
    {
      "type": "NEW FEATURE",
      "description": "Fix for ''Label'' editor dialog slowness and freezing on some machines. Analyze fonts in the background to identify ones that are causing slowness when editing ''Label'' components and skip font preview for them to optimize performance"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved performance of the application when using dashed and dotted style for ''Hookup Wire'' component"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Technical change: replace mechanism for dynamic JAR loading to ensure compatibility with Java 11 and newer"
    }
  ],
  "releaseDate": "2020-12-20 00:00:00.000 CET"
}');

-- Version 4.14.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.14.0', 
'{
  "version": "4.14.0",
  "changes": [
    {
      "type": "IMPROVEMENT",
      "description": "Migrate application data (config, logs, backups) directory from ''$USER_HOME/.diylc'' to ''$USER_HOME/diylc'' to avoid it being hidden on some OSs"
    },
    {
      "type": "BUG FIX",
      "description": "macOS: splash screen hangs for several minutes on some machines"
    },
    {
      "type": "BUG FIX",
      "description": "macOS: logging does not produce files on Mac OS Sierra"
    }
  ],
  "releaseDate": "2020-12-28 00:00:00.000 CET"
}');

-- Version 4.15.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.15.0', 
'{
  "version": "4.15.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added ''File'' -> ''Import Netlist'' option that can import components from a Tango PCB file"
    },
    {
      "type": "NEW FEATURE",
      "description": "Portugu\u00eas translation added (thanks Pedro Rizzi!)"
    },
    {
      "type": "NEW FEATURE",
      "description": "Building blocks and Favorites added to the component toolbar"
    },
    {
      "type": "NEW FEATURE",
      "description": "Scroll buttons added to the component toolbar that appear when needed"
    },
    {
      "type": "NEW FEATURE",
      "description": "Toolbar drop down menu now contains options for managing favorites, keyboard shortcuts and selecting all components of type"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Save last selected tab in the component toolbar and restore it in the next session"
    },
    {
      "type": "IMPROVEMENT",
      "description": "When editing component properties, group Length and Width properties together"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed the issue that prevented old DIYLC v1 and v2 files to be opened"
    },
    {
      "type": "BUG FIX",
      "description": "Rotating ''Neutrik Jack'' produces rendering issues"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed some issues with exporting/importing building blocks and variants. If you still have issues, your variant/building block files might need to be manually updated to keep working"
    }
  ],
  "releaseDate": "2021-01-03 00:00:00.000 CET"
}');

-- Version 4.15.1
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.15.1', 
'{
  "version": "4.15.1",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "macOS: issues with scrollable toolbar that prevent it from starting on mac machines"
    }
  ],
  "releaseDate": "2021-01-04 00:00:00.000 CET"
}');

-- Version 4.16.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.16.0', 
'{
  "version": "4.16.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Flatpak integration for easier deployments to Linux systems"
    },
    {
      "type": "NEW FEATURE",
      "description": "When editing component colors, honor the specified ''alpha'' value specified on each color individually in addition to the component-level alpha property"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Color chooser dialog defaults to the existing color value being edited"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Single click to expand and collapse categories in the component tree"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved code robustness"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved Dark theme look"
    },
    {
      "type": "BUG FIX",
      "description": "Auto-Wrap Label crashes the application if it''s shrunken into zero-length"
    }
  ],
  "releaseDate": "2021-01-17 00:00:00.000 CET"
}');

-- Version 4.17.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.17.0', 
'{
  "version": "4.17.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added ''Eurorack Stripboard'' component, designed by David Haillant"
    },
    {
      "type": "NEW FEATURE",
      "description": "Allow multi-line text in label components"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Slightly fancier splash screen :)"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed a major memory leak when using Undo functionality"
    }
  ],
  "releaseDate": "2021-02-14 00:00:00.000 CET"
}');

-- Version 4.18.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.18.0', 
'{
  "version": "4.18.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added ''SVG Image'' component, to allow importing vector graphics into projects"
    },
    {
      "type": "BUG FIX",
      "description": "Mini Toggle Switch - lugs do not get rotated when changing orientation; rotate from the context menu doesn''t work"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed the issue with Perf Board, Vero Board and TriPad Board sometimes rendering an extra row with pads when there''s no space for them"
    }
  ],
  "releaseDate": "2021-07-11 00:00:00.000 CET"
}');

-- Version 4.19.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.19.0', 
'{
  "version": "4.19.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added ''Dial Scale'' component for designing device face-plates"
    },
    {
      "type": "NEW FEATURE",
      "description": "Keep grouped components together when pasting"
    }
  ],
  "releaseDate": "2021-08-21 00:00:00.000 CET"
}');

-- Version 4.20.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.20.0', 
'{
  "version": "4.20.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added ''Open 1/8\" Jack'' component"
    }
  ],
  "releaseDate": "2021-09-12 00:00:00.000 CET"
}');

-- Version 4.21.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.21.0', 
'{
  "version": "4.21.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Fixed compatibility issues with Java version 16 and above"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ''Gray'' theme"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Add shabang to run.sh for *nix systems"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Prevent the donation label from wrapping into several lines"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Use Backspace key to delete selection in Mac OS"
    }
  ],
  "releaseDate": "2022-02-16 00:00:00.000 CET"
}');

-- Version 4.21.1
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.21.1', 
'{
  "version": "4.21.1",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Fixed broken OSX app package"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added regulator pinouts to transistor body components"
    }
  ],
  "releaseDate": "2022-02-19 00:00:00.000 CET"
}');

-- Version 4.21.2
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.21.2', 
'{
  "version": "4.21.2",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Fixed broken Linux app package"
    }
  ],
  "releaseDate": "2022-02-21 00:00:00.000 CET"
}');

-- Version 4.21.3
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.21.3', 
'{
  "version": "4.21.3",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Fixed the issue with the app going back to .diylc user directory for configuration storage. Only diylc directory is used going forward, please copy config.xml from .diylc to diylc if you are missing any configuration"
    }
  ],
  "releaseDate": "2022-02-22 00:00:00.000 CET"
}');

-- Version 4.21.4
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.21.4', 
'{
  "version": "4.21.4",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Fixed the issue with stereo phono jack rendering issues when rotated"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved precision of zooming in and out when nothing is selected"
    }
  ],
  "releaseDate": "2022-02-27 00:00:00.000 CET"
}');

-- Version 4.21.5
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.21.5', 
'{
  "version": "4.21.5",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Some components show properties in the editor that are not applicable to them"
    }
  ],
  "releaseDate": "2022-02-27 00:00:00.000 CET"
}');

-- Version 4.22.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.22.0', 
'{
  "version": "4.22.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Fixed backwards compatibility issue with older files failing to open because of an issue with ''Mini Toggle Switch'' component serialization"
    },
    {
      "type": "BUG FIX",
      "description": "Fix component prefix for SMD Resistor"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Separate WIRING layer (containing ''Hookup Wire'' and ''Twisted Wire'' at the moment) from TRACE layer and render it above the COMPONENT layer by default"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Upgrade OSX application package built-in Java to JRE 1.8.0_321-b07"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added regression test suite that validates each release candidate version against a barrage of different DIY files"
    }
  ],
  "releaseDate": "2022-03-03 00:00:00.000 CET"
}');

-- Version 4.22.1
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.22.1', 
'{
  "version": "4.22.1",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Fixed backwards compatibility issue with older files failing to open because of an issue with ''Tape Measure'' component serialization"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed the issue with ''Add Flexible Leads'' functionality placing the flexible leads wrong on leaded components (e.g. resistors, capacitors...)"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed display issue with Trace layer showing up as Wiring in the menu"
    }
  ],
  "releaseDate": "2022-03-07 00:00:00.000 CET"
}');

-- Version 4.22.2
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.22.2', 
'{
  "version": "4.22.2",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Fixed the issue with application hanging when loading certain files"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed importing building blocks and variants from a file"
    }
  ],
  "releaseDate": "2022-03-18 00:00:00.000 CET"
}');

-- Version 4.23.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.23.0', 
'{
  "version": "4.23.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Fixed the issue with the application not starting under Windows"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ''Slide Switch'' component"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved rendering performance significantly"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Include JRE in the universal ZIP package (the embedded JRE is only for Windows)"
    }
  ],
  "releaseDate": "2022-04-23 00:00:00.000 CET"
}');

-- Version 4.24.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.24.0', 
'{
  "version": "4.24.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Fixed a bug that causes the background of exported images and PDFs to be very light gray instead of pure white"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Automatically run Java garbage collector when available memory reaches 10%"
    }
  ],
  "releaseDate": "2022-06-05 00:00:00.000 CET"
}');

-- Version 4.25.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.25.0', 
'{
  "version": "4.25.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Status bar shows a wrong number of selected components"
    },
    {
      "type": "BUG FIX",
      "description": "Fix run.sh to Unix line breaks"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Limit memory usage to avoid out of memory errors when ''Cache Boost'' feature is enabled on complex project"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Cleanup cache when a component is deleted from the project"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Added BJT B-C-E pinout to transistor components"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Changed resistor orange band color to more intense orange"
    }
  ],
  "releaseDate": "2022-07-09 00:00:00.000 CET"
}');

-- Version 4.26.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.26.0', 
'{
  "version": "4.26.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added ''Voltage Source'' symbol. Thank you, JD!"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added two DPDT ON/ON/ON variations to ''Mini Toggle Switch'' component"
    },
    {
      "type": "BUG FIX",
      "description": "The app crashes if there''s a component that is reduced to 0 pixels with ''Cache Boost'' enabled"
    },
    {
      "type": "BUG FIX",
      "description": "Removed ''Hardware Acceleration'' feature as it causes issues with modern graphic cards"
    },
    {
      "type": "IMPROVEMENT",
      "description": "When opening files from the ''Recent Files'' menu, show busy cursor instead of blocking the main app thread"
    }
  ],
  "releaseDate": "2022-08-27 00:00:00.000 CET"
}');

-- Version 4.27.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.27.0', 
'{
  "version": "4.27.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added ''Current Source'' symbol. Thank you, JD!"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Few minor improvements"
    }
  ],
  "releaseDate": "2022-08-28 00:00:00.000 CET"
}');

-- Version 4.28.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.28.0', 
'{
  "version": "4.28.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added ''4-pin Jumbo Tube Socket'' component for 211, 805 and 845 tubes"
    },
    {
      "type": "IMPROVEMENT",
      "description": "More convenient way of editing angle of rotation of all tube sockets"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Ability to rotate all tube sockets through the ''Transform Selection'' feature or keyboard shortcut"
    },
    {
      "type": "BUG FIX",
      "description": "Tube socket pin designations are not rendered until property editor dialog is opened and closed"
    }
  ],
  "releaseDate": "2022-09-25 00:00:00.000 CET"
}');

-- Version 4.29.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.29.0', 
'{
  "version": "4.29.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added 6 variations of Schaller Megaswitch"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ''DP3T (Import 5-Position Strat)'' type of lever switch"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ''Stingray Bass Pickup'' component. Thank you JD!"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ''Bulb'' schematic symbol. Thank you JD!"
    }
  ],
  "releaseDate": "2022-10-04 00:00:00.000 CET"
}');

-- Version 4.30.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.30.0', 
'{
  "version": "4.30.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added ''Rotary Switch (Sealed)'' component in several switching configurations"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ''Rotary Switch (Open)'' component in several switching configurations"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ''S1 Switch'' component that can be combined with potentiometer component or used on its own"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ''DP4T (6-Position Oak Grigsby)'' as a new type of Lever Switch component"
    },
    {
      "type": "NEW FEATURE",
      "description": "Ability to rotate ''Lever Switch'' and ''Schaller Megaswitch'' by any angle"
    },
    {
      "type": "NEW FEATURE",
      "description": "DIYLC Netlists can be generated including or excluding switching configurations"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Ability to rotate all guitar/bass pickups through the ''Transform Selection'' feature or keyboard shortcut"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved the speed and accuracy of circuit analysis and netlist calculation"
    }
  ],
  "releaseDate": "2022-10-22 00:00:00.000 CET"
}');

-- Version 4.30.1
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.30.1', 
'{
  "version": "4.30.1",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Fix the build that wouldn''t run due to a missing package"
    }
  ],
  "releaseDate": "2022-10-23 00:00:00.000 CET"
}');

-- Version 4.31.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.31.0', 
'{
  "version": "4.31.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added \"Switch (Latching)\" schematic symbol with many variations"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added \"Common Node\" component that ties all nodes with the same label together, e.g. B+, V+, GND, etc"
    },
    {
      "type": "IMPROVEMENT",
      "description": "\"Snap to Components\" features now works when creating new components as well as when moving existing ones"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Added more variants of existing and new components"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Increase the opacity of locked components"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved ground symbol mouse selection tracking and added stroke width as a parameter"
    },
    {
      "type": "BUG FIX",
      "description": "Ground symbol ties all grounded points together for circuit analysis"
    },
    {
      "type": "BUG FIX",
      "description": "Printed projects are sometimes missing parts of components or text"
    },
    {
      "type": "BUG FIX",
      "description": "\"Snap to\" configuration menu does not work properly when using translated UI to any language other than English"
    }
  ],
  "releaseDate": "2022-10-31 00:00:00.000 CET"
}');

-- Version 4.32.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.32.0', 
'{
  "version": "4.32.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added ''Radial Mica Capacitor'' component"
    },
    {
      "type": "NEW FEATURE",
      "description": "Introduced support for standard models of components, defined in the product datasheet. Right click menu in the toolbox now contains additional options for creating components with exact values and dimension as specified in the datasheet. Right click on the existing components now has ''Apply Model'' option which can apply properties of a standard component model from the datasheet to selected component(s)"
    },
    {
      "type": "NEW FEATURE",
      "description": "Axial Film Capacitor - added standard models from Mallory 150 and Solen Fast datasheet"
    },
    {
      "type": "NEW FEATURE",
      "description": "Radial Film Capacitor - added standard models from Orange Drop 225P, 715P and 716P, WIMA FKP-02, FKP-2, FKS-2, MKP-2, MKP-4, MKS-02, MKS-2 datasheet"
    },
    {
      "type": "NEW FEATURE",
      "description": "Axial Electrolytic Capacitor - added standard models from F&T A Typ, Illinois TTA, JJ ANH and Sprague TVA Atom datasheet"
    },
    {
      "type": "NEW FEATURE",
      "description": "Radial Electrolytic Capacitor - added standard models from Elna Silmic II, Nichicon FG, KZ, Panasonic FC, FM and HNG datasheet"
    },
    {
      "type": "NEW FEATURE",
      "description": "Radial Mica Capacitor - added standard models from Cornell Dubilier CD10, CD15, CD19, CD30, CD42, CDV19, CDV30 datasheet"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ''Tubular Beveled'' shape option to Resistor component and added Dale C*F55 and C*F60 resistor variants"
    }
  ],
  "releaseDate": "2022-11-11 00:00:00.000 CET"
}');

-- Version 4.33.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.33.0', 
'{
  "version": "4.33.0",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Fixed an issue that would cause capacitors to fail when creating, editing, moving or deleting"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added standard models in the drop down menu of the component toolbar"
    },
    {
      "type": "NEW FEATURE",
      "description": "Radial Electrolytic Capacitor - added standard models from Nichicon FW datasheet"
    },
    {
      "type": "NEW FEATURE",
      "description": "Radial Film Capacitor - added standard models from Epcos MKT datasheet"
    },
    {
      "type": "NEW FEATURE",
      "description": "Radial Film Capacitor - added ability to select package shape - drop or box style"
    }
  ],
  "releaseDate": "2022-11-16 00:00:00.000 CET"
}');

-- Version 4.34.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.34.0', 
'{
  "version": "4.34.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added ''Project Explorer'' pane in the right-hand side for quick access to components"
    },
    {
      "type": "NEW FEATURE",
      "description": "Keyboard shortcuts for toggling layer visibility using Ctrl + 1 through Ctrl + 6"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Circuit analysis algorithm that supports netlist extraction, ''Highlight connected Areas'' and ''Guitar Wiring Analyzer'' features improved to take PCB trace layer and component z-order into account for better accuracy"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Potentiometer bodies are no longer considered as conductive surfaces to avoid unwanted shorts"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Optimized memory usage when having many Hookup Wire components"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved conductivity tracking of Terminal Strip"
    },
    {
      "type": "BUG FIX",
      "description": "Do not apply any user-default property values when creating a standard datasheet model (e.g. a capacitor)"
    },
    {
      "type": "BUG FIX",
      "description": "Variants of boards having explicit sizing mode fail when trying to add to a project"
    },
    {
      "type": "BUG FIX",
      "description": "Font settings (e.g. bold) sometimes bleed into wrong component"
    }
  ],
  "releaseDate": "2022-12-09 00:00:00.000 CET"
}');

-- Version 4.35.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.35.0', 
'{
  "version": "4.35.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added ''1/8\" Cliff Jack'' component - stereo and mono"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ''Solder Lug'' component"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ''PCB Terminal Block'' component"
    },
    {
      "type": "NEW FEATURE",
      "description": "Show component values in the project explorer"
    },
    {
      "type": "NEW FEATURE",
      "description": "Double click in the project explorer to edit selected component"
    },
    {
      "type": "NEW FEATURE",
      "description": "Title Block added as one of factory building blocks"
    },
    {
      "type": "NEW FEATURE",
      "description": "''Renumber On Paste'' feature now applies to variants as well. When set to OFF, the app honors the name saved in the variant"
    },
    {
      "type": "NEW FEATURE",
      "description": "New file icons for Mac OS"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Ability to use up/down arrow keys in the project explorer to select components"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Use scroll buttons in ''Tabbed Toolbar'' instead of arranging tabs into multiple lines when the screen is not large enough to show them all"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Escape comma in BOM CSV files to avoid component names being split into separate columns"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Show jack type in the BOM and project explorer"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Show wire gauge in the BOM and project explorer"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved find tool usability, make the screen pan to the selected components"
    },
    {
      "type": "BUG FIX",
      "description": "Trace cut doesn''t affect continuity of vero board traces"
    },
    {
      "type": "BUG FIX",
      "description": "DIL IC pin labels incorrect in the netlist"
    },
    {
      "type": "BUG FIX",
      "description": "Diode symbol doesn''t appear in the BOM"
    },
    {
      "type": "BUG FIX",
      "description": "Upon starting or opening a project, the ruler is slightly miss-aligned with the project boundaries"
    }
  ],
  "releaseDate": "2022-12-23 00:00:00.000 CET"
}');

-- Version 4.36.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.36.0', 
'{
  "version": "4.36.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Title Block added as one of factory building blocks (was missing in 4.35 by mistake)"
    },
    {
      "type": "NEW FEATURE",
      "description": "Configurable max switch combination count for circuit analysis (using org.diylc.maxSwitchCombinations argument)"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Added resilience to the code that saves the configuration file to avoid it getting corrupted"
    },
    {
      "type": "IMPROVEMENT",
      "description": "When backing up the configuration file, append the current timestamp at the end instead of a ''~''"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improve the format of timestamp in file backups"
    },
    {
      "type": "BUG FIX",
      "description": "General application slowness when Inductor or Fuse Symbol components are added to the drawing"
    },
    {
      "type": "BUG FIX",
      "description": "Lever switch orientation not loaded correctly from files created prior to version 4.35"
    }
  ],
  "releaseDate": "2023-03-30 00:00:00.000 CET"
}');

-- Version 4.37.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.37.0', 
'{
  "version": "4.37.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "''Proto Board 780 Holes'' added with two variations (sponsored by Pete Olivier)"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Ability to rotate (almost) all components using keyboard shortcuts and ''Transform Selection'' menu"
    }
  ],
  "releaseDate": "2023-04-27 00:00:00.000 CET"
}');

-- Version 4.38.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.38.0', 
'{
  "version": "4.38.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Buzzer component and symbol added (thanks M0JXD)"
    },
    {
      "type": "NEW FEATURE",
      "description": "Resistor symbol now has two draw standards - ANSI and IEC (thanks M0JXD)"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Fixed run.sh application starter for Linux and Apple OSX with M1/M2 processors when using Java 17 or newer"
    },
    {
      "type": "IMPROVEMENT",
      "description": "When editing Label components, default focus to the text editor"
    },
    {
      "type": "BUG FIX",
      "description": "Unable to move selected components if one of them is close to being reduced to a point"
    },
    {
      "type": "BUG FIX",
      "description": "Toroidal Inductor hangs the application if it doesn''t have inductance value set"
    }
  ],
  "releaseDate": "2024-12-23 00:00:00.000 CET"
}');

-- Version 4.39.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.39.0', 
'{
  "version": "4.39.0",
  "changes": [
    {
      "type": "IMPROVEMENT",
      "description": "Upgraded JRE version of Mac builds to Temurin 17 and switched OpenGL to Metal rendering engine for improved performance"
    },
    {
      "type": "BUG FIX",
      "description": "Choosing \"Print\" option from the menu starts printing immediately before choosing printing preferences"
    }
  ],
  "releaseDate": "2025-01-25 00:00:00.000 CET"
}');

-- Version 4.40.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.40.0', 
'{
  "version": "4.40.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Freeway 3x3-03 Toggle switch added"
    },
    {
      "type": "NEW FEATURE",
      "description": "Guitar wiring analyzer can now detect treble bleed circuit on volume controls"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added headless mode for direct render to file without the GUI - use ''inputFile --export (--force) outputFile'' syntax"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added two types of 4PDT On/On/On Switches, e.g. DiMarzio EP1111 (thank you Lukasz Tekieli)"
    },
    {
      "type": "NEW FEATURE",
      "description": "Ability to set explicit dimensions of basic shapes - rectangles and ellipses"
    },
    {
      "type": "NEW FEATURE",
      "description": "Ability to change pitch and appearance of PCB Terminal Block component"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Better rendering of the effective circuit graph in the guitar wiring analyzer, showing series and parallel connections in a more intuitive way"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Increased max undo stack size to 128 versions"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Added 1-pole DIP switch option"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Allow guitar wiring analyzer when some of the switching configuration do not produce output"
    },
    {
      "type": "IMPROVEMENT",
      "description": "''Save As'' displays the current file name by default"
    },
    {
      "type": "IMPROVEMENT",
      "description": "''Proto Board 780 Holes'' now aligns with the grid"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Show Label text in ''Project Explorer'' in addition to the name"
    },
    {
      "type": "IMPROVEMENT",
      "description": "MacOS version for x86 platform upgraded to java 17 and metal rendering engine"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed null pointer exceptions when trying to add components to the layout in some cases"
    },
    {
      "type": "BUG FIX",
      "description": "Loading files that contain lines with end arrows sometimes produces errors in the log and lines not to be loaded"
    },
    {
      "type": "BUG FIX",
      "description": "Mouse click to select intermittently doesn''t register on Mac machines"
    },
    {
      "type": "BUG FIX",
      "description": "Ctrl + Minus sign sometimes doesn''t zoom out"
    }
  ],
  "releaseDate": "2025-02-22 00:00:00.000 CET"
}');

-- Version 4.41.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.41.0', 
'{
  "version": "4.41.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Export traces, pads (with holes), ground fill and PCB text to Gerber files"
    },
    {
      "type": "NEW FEATURE",
      "description": "Added ability to select PTH or NPTH holes for solder pads"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Optimized continuity calculations to be faster"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Increased maximum available memory for DIYLC to 4GB"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Upgraded Java JRE to version 17 LTE"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Added support for high DPI displays"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Added windows x64 build"
    },
    {
      "type": "BUG FIX",
      "description": "Pasting a building block sometimes messes up the z-order of components"
    },
    {
      "type": "BUG FIX",
      "description": "Pasting a building block sometimes switches selection to previously added components and canvas moves to the wrong position"
    },
    {
      "type": "BUG FIX",
      "description": "''Highlight Continuity Areas'' sometimes renders stale areas when clicked"
    }
  ],
  "releaseDate": "2025-03-08 00:00:00.000 CET"
}');

-- Version 4.41.1
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 4.41.1', 
'{
  "version": "4.41.1",
  "changes": [
    {
      "type": "BUG FIX",
      "description": "Do not mirror bottom layers for Gerber export as fabrication service providers expect a top view for everything"
    },
    {
      "type": "BUG FIX",
      "description": "Selecting components by drawing a rectangle around them works glitchy"
    }
  ],
  "releaseDate": "2025-03-08 00:00:00.000 CET"
}');

-- Version 5.0.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 5.0.0', 
'{
  "version": "5.0.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Switched to AppImage distribution for Linux system, abandoning Flatpak as a means of distribution. DIYLC AppImage will be available alongside with other executables on the official DIYLC GitHub page."
    },
    {
      "type": "IMPROVEMENT",
      "description": "Upgraded Windows installer version and Windows EXE launcher version"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed alignment issues of round elements (e.g. solder pads and holes) in exported Gerber files"
    },
    {
      "type": "BUG FIX",
      "description": "Fixed the issue with opening DIY files on Mac OS"
    }
  ],
  "releaseDate": "2025-03-09 00:00:00.000 CET"
}');

-- Version 5.1.0
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 5.1.0', 
'{
  "version": "5.1.0",
  "changes": [
    {
      "type": "NEW FEATURE",
      "description": "Added ability to render the underside of boards along with the top side for easier validation, building and debugging"
    },
    {
      "type": "NEW FEATURE",
      "description": "Configurable ''High DPI Rendering'' feature which makes the diagram smoother on high-resolution displays but can affect performance"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Significantly improved rendering performance"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Increased initial memory allocation to 1GB for smoother performance"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Upgraded source code structure to modern standards and introduced Maven"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Improved robustness of series/parallel graph construction code used in circuit analysis"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Omit boards, wires, traces, terminals, etc from Spice netlist output"
    },
    {
      "type": "IMPROVEMENT",
      "description": "Updated the windows installer (hopefully) to avoid false positive anti-virus detection"
    },
    {
      "type": "BUG FIX",
      "description": "Moving selected components with a mouse doesn''t register file modification action and doesn''t enable Undo action"
    },
    {
      "type": "BUG FIX",
      "description": "Rotating selected components sometimes wacks the alignment off the grid"
    },
    {
      "type": "BUG FIX",
      "description": "Ctrl+X (Command+X) shortcut for Cut action doesn''t work because ''Project Explorer'' steals the focus on X key press"
    }
  ],
  "releaseDate": "2025-03-24 00:00:00.000 CET"
}');

-- Version 5.1.1
INSERT INTO diylc_knowledge (category, section, content)
VALUES ('History', 'DIYLC Version 5.1.1', 
'{
  "version": "5.1.1",
  "changes": [
    {
      "type": "IMPROVEMENT",
      "description": "Include windows ZIP package"
    },
    {
      "type": "BUG FIX",
      "description": "Windows version doesn''t start in some cases"
    }
  ],
  "releaseDate": "2025-03-27 00:00:00.000 CET"
}');

