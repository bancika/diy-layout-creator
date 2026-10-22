/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

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
package org.diylc.plugins.chatbot.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.diylc.common.IProjectEditor;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.plugins.chatbot.model.AiEditOperation;
import org.diylc.plugins.chatbot.model.AiEditScript;

/**
 * Executes an AiEditScript against a Project. Each action is carried out by an
 * {@link IAiEditHandler}; this class only decides the order in which they run.
 * <p>
 * Uses three passes:
 * <ul>
 *   <li><b>Pass 1</b>: Remove, Modify, Replace, Reconnect and Add (non-wires), in the order the
 *   script lists them. Sets properties and determines final pin geometry.</li>
 *   <li><b>Pass 2</b>: Add Wires. Resolves terminal references to the final pixel coordinates of
 *   the components from Pass 1.</li>
 *   <li><b>Pass 3</b>: Z-order changes, once every component they may name exists.</li>
 * </ul>
 */
public class AiEditScriptEditor implements IProjectEditor {

  private static final Logger LOG = Logger.getLogger(AiEditScriptEditor.class);

  private static final Map<String, IAiEditHandler> HANDLERS = Map.of(
      "remove", new RemoveHandler(),
      "modify", new ModifyHandler(),
      "replace", new ReplaceHandler(),
      "reconnect", new ReconnectHandler(),
      "add", new AddHandler(),
      "send_behind", new ZOrderHandler(false),
      "bring_in_front", new ZOrderHandler(true));
  // an add that names two terminals rather than a position is a wire
  private static final IAiEditHandler ADD_WIRE = new AddWireHandler();

  private final AiEditScript editScript;
  private final List<String> warnings = new ArrayList<>();

  public AiEditScriptEditor(AiEditScript editScript) {
    this.editScript = editScript;
  }

  @Override
  public String getEditAction() {
    return "AI Edit: "
        + (editScript.getExplanation() != null ? editScript.getExplanation() : "Circuit modified");
  }

  public List<String> getWarnings() {
    return warnings;
  }

  @Override
  public Set<IDIYComponent<?>> edit(Project project, Set<IDIYComponent<?>> selection) {
    if (editScript.getOperations() == null) {
      return selection;
    }

    AiEditContext context = new AiEditContext(project, warnings);

    // Pass 1 keeps the order of the script, so that an add followed by a modify of the same part,
    // or a remove followed by an add that reuses the name, does what the model wrote
    List<AiEditOperation> pass1Ops = new ArrayList<>();
    List<AiEditOperation> pass2Ops = new ArrayList<>();
    List<AiEditOperation> zOrderOps = new ArrayList<>();

    for (AiEditOperation op : editScript.getOperations()) {
      if (findHandler(op) instanceof ZOrderHandler) {
        zOrderOps.add(op);
      } else if ("add".equalsIgnoreCase(op.getAction()) && op.getFromTerminal() != null
          && op.getToTerminal() != null) {
        pass2Ops.add(op);
      } else {
        pass1Ops.add(op);
      }
    }

    for (AiEditOperation op : pass1Ops) {
      apply(op, findHandler(op), context);
    }
    for (AiEditOperation op : pass2Ops) {
      apply(op, ADD_WIRE, context);
    }
    for (AiEditOperation op : zOrderOps) {
      apply(op, findHandler(op), context);
    }

    if (!context.getNewSelection().isEmpty()) {
      return context.getNewSelection();
    }
    // a script that only removes leaves nothing new to select, and the old selection may still
    // hold the components it just took out of the project
    Set<IDIYComponent<?>> remaining = new HashSet<>(selection);
    remaining.removeAll(context.getRemoved());
    return remaining;
  }

  private static IAiEditHandler findHandler(AiEditOperation op) {
    return op.getAction() == null ? null : HANDLERS.get(op.getAction().toLowerCase(Locale.ROOT));
  }

  private void apply(AiEditOperation op, IAiEditHandler handler, AiEditContext context) {
    // an action this version does not know is skipped, as it always has been
    if (handler == null) {
      return;
    }
    try {
      handler.apply(op, context);
    } catch (Exception e) {
      LOG.error("Failed to execute operation: " + op, e);
      warnings.add("Error executing " + op.getAction() + " on " + op.getComponentName() + ": "
          + e.getMessage());
    }
  }
}
