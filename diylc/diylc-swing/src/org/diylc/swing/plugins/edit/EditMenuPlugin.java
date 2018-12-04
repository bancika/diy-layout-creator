/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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
package org.diylc.swing.plugins.edit;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.util.EnumSet;

import org.diylc.appframework.undo.IUndoListener;
import org.diylc.appframework.undo.UndoHandler;
import org.diylc.common.EventType;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.ExpansionMode;
import org.diylc.core.Project;
import org.diylc.images.IconLoader;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;

public class EditMenuPlugin implements IPlugIn, ClipboardOwner {

  private static final String EDIT_TITLE = "Edit";
  private static final String TRANSFORM_TITLE = "Transform Selection";
  private static final String RENUMBER_TITLE = "Renumber Selection";
  private static final String EXPAND_TITLE = "Expand Selection";

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;

  private Clipboard clipboard;

  private ActionFactory.CutAction cutAction;
  private ActionFactory.CopyAction copyAction;
  private ActionFactory.PasteAction pasteAction;
  private ActionFactory.DuplicateAction duplicateAction;
  private ActionFactory.EditSelectionAction editSelectionAction;
  private ActionFactory.DeleteSelectionAction deleteSelectionAction;
  private ActionFactory.GroupAction groupAction;
  private ActionFactory.UngroupAction ungroupAction;
  private ActionFactory.SendToBackAction sendToBackAction;
  private ActionFactory.BringToFrontAction bringToFrontAction;
  private ActionFactory.NudgeAction nudgeAction;
  private ActionFactory.RenumberAction renumberXAxisAction;
  private ActionFactory.RenumberAction renumberYAxisAction;
  private ActionFactory.ExpandSelectionAction expandSelectionAllAction;
  private ActionFactory.ExpandSelectionAction expandSelectionImmediateAction;
  private ActionFactory.ExpandSelectionAction expandSelectionSameTypeAction;
  private ActionFactory.SaveAsTemplateAction saveAsTemplateAction;
  private ActionFactory.SaveAsBlockAction saveAsBlockAction;
  private ActionFactory.RotateSelectionAction rotateClockwiseAction;
  private ActionFactory.RotateSelectionAction rotateCounterClockwiseAction;
  private ActionFactory.MirrorSelectionAction mirrorHorizontallyAction;
  private ActionFactory.MirrorSelectionAction mirrorVerticallyAction;

  private UndoHandler<Project> undoHandler;

  public EditMenuPlugin(ISwingUI swingUI) {
    this.swingUI = swingUI;
    // SecurityManager securityManager = System.getSecurityManager();
    // if (securityManager != null) {
    // try {
    // securityManager.checkSystemClipboardAccess();
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // }
    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    undoHandler = new UndoHandler<Project>(new IUndoListener<Project>() {

      @Override
      public void actionPerformed(Project currentState) {
        plugInPort.loadProject(currentState, false);
      }
    });
    clipboard.addFlavorListener(new FlavorListener() {

      @Override
      public void flavorsChanged(FlavorEvent e) {
        refreshActions();
      }
    });
  }

  public ActionFactory.CutAction getCutAction() {
    if (cutAction == null) {
      cutAction = ActionFactory.getInstance().createCutAction(plugInPort, clipboard, this);
    }
    return cutAction;
  }

  public ActionFactory.CopyAction getCopyAction() {
    if (copyAction == null) {
      copyAction = ActionFactory.getInstance().createCopyAction(plugInPort, clipboard, this);
    }
    return copyAction;
  }

  public ActionFactory.PasteAction getPasteAction() {
    if (pasteAction == null) {
      pasteAction = ActionFactory.getInstance().createPasteAction(plugInPort, clipboard);
    }
    return pasteAction;
  }

  public ActionFactory.DuplicateAction getDuplicateAction() {
    if (duplicateAction == null) {
      duplicateAction = ActionFactory.getInstance().createDuplicateAction(plugInPort);
    }
    return duplicateAction;
  }

  public ActionFactory.EditSelectionAction getEditSelectionAction() {
    if (editSelectionAction == null) {
      editSelectionAction = ActionFactory.getInstance().createEditSelectionAction(plugInPort);
    }
    return editSelectionAction;
  }

  public ActionFactory.DeleteSelectionAction getDeleteSelectionAction() {
    if (deleteSelectionAction == null) {
      deleteSelectionAction = ActionFactory.getInstance().createDeleteSelectionAction(plugInPort);
    }
    return deleteSelectionAction;
  }

  public ActionFactory.GroupAction getGroupAction() {
    if (groupAction == null) {
      groupAction = ActionFactory.getInstance().createGroupAction(plugInPort);
    }
    return groupAction;
  }

  public ActionFactory.UngroupAction getUngroupAction() {
    if (ungroupAction == null) {
      ungroupAction = ActionFactory.getInstance().createUngroupAction(plugInPort);
    }
    return ungroupAction;
  }

  public ActionFactory.SendToBackAction getSendToBackAction() {
    if (sendToBackAction == null) {
      sendToBackAction = ActionFactory.getInstance().createSendToBackAction(plugInPort);
    }
    return sendToBackAction;
  }

  public ActionFactory.BringToFrontAction getBringToFrontAction() {
    if (bringToFrontAction == null) {
      bringToFrontAction = ActionFactory.getInstance().createBringToFrontAction(plugInPort);
    }
    return bringToFrontAction;
  }

  public ActionFactory.NudgeAction getNudgeAction() {
    if (nudgeAction == null) {
      nudgeAction = ActionFactory.getInstance().createNudgeAction(plugInPort);
    }
    return nudgeAction;
  }

  public ActionFactory.RenumberAction getRenumberXAxisAction() {
    if (renumberXAxisAction == null) {
      renumberXAxisAction = ActionFactory.getInstance().createRenumberAction(plugInPort, true);
    }
    return renumberXAxisAction;
  }

  public ActionFactory.RenumberAction getRenumberYAxisAction() {
    if (renumberYAxisAction == null) {
      renumberYAxisAction = ActionFactory.getInstance().createRenumberAction(plugInPort, false);
    }
    return renumberYAxisAction;
  }

  public ActionFactory.ExpandSelectionAction getExpandSelectionAllAction() {
    if (expandSelectionAllAction == null) {
      expandSelectionAllAction = ActionFactory.getInstance().createExpandSelectionAction(plugInPort, ExpansionMode.ALL);
    }
    return expandSelectionAllAction;
  }

  public ActionFactory.ExpandSelectionAction getExpandSelectionImmediateAction() {
    if (expandSelectionImmediateAction == null) {
      expandSelectionImmediateAction =
          ActionFactory.getInstance().createExpandSelectionAction(plugInPort, ExpansionMode.IMMEDIATE);
    }
    return expandSelectionImmediateAction;
  }

  public ActionFactory.ExpandSelectionAction getExpandSelectionSameTypeAction() {
    if (expandSelectionSameTypeAction == null) {
      expandSelectionSameTypeAction =
          ActionFactory.getInstance().createExpandSelectionAction(plugInPort, ExpansionMode.SAME_TYPE);
    }
    return expandSelectionSameTypeAction;
  }

  public ActionFactory.SaveAsTemplateAction getSaveAsTemplateAction() {
    if (saveAsTemplateAction == null) {
      saveAsTemplateAction = ActionFactory.getInstance().createSaveAsTemplateAction(plugInPort);
    }
    return saveAsTemplateAction;
  }

  public ActionFactory.SaveAsBlockAction getSaveAsBlockAction() {
    if (saveAsBlockAction == null) {
      saveAsBlockAction = ActionFactory.getInstance().createSaveAsBlockAction(plugInPort);
    }
    return saveAsBlockAction;
  }

  public ActionFactory.RotateSelectionAction getRotateClockwiseAction() {
    if (rotateClockwiseAction == null) {
      rotateClockwiseAction = ActionFactory.getInstance().createRotateSelectionAction(plugInPort, 1);
    }
    return rotateClockwiseAction;
  }

  public ActionFactory.RotateSelectionAction getRotateCounterclockwiseAction() {
    if (rotateCounterClockwiseAction == null) {
      rotateCounterClockwiseAction = ActionFactory.getInstance().createRotateSelectionAction(plugInPort, -1);
    }
    return rotateCounterClockwiseAction;
  }

  public ActionFactory.MirrorSelectionAction getMirrorHorizontallyAction() {
    if (mirrorHorizontallyAction == null) {
      mirrorHorizontallyAction =
          ActionFactory.getInstance().createMirrorSelectionAction(plugInPort, IComponentTransformer.HORIZONTAL);
    }
    return mirrorHorizontallyAction;
  }

  public ActionFactory.MirrorSelectionAction getMirrorVerticallyAction() {
    if (mirrorVerticallyAction == null) {
      mirrorVerticallyAction =
          ActionFactory.getInstance().createMirrorSelectionAction(plugInPort, IComponentTransformer.VERTICAL);
    }
    return mirrorVerticallyAction;
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;

    ActionFactory actionFactory = ActionFactory.getInstance();

    swingUI.injectMenuAction(undoHandler.getUndoAction(), EDIT_TITLE);
    swingUI.injectMenuAction(undoHandler.getRedoAction(), EDIT_TITLE);
    swingUI.injectMenuAction(null, EDIT_TITLE);
    swingUI.injectMenuAction(getCutAction(), EDIT_TITLE);
    swingUI.injectMenuAction(getCopyAction(), EDIT_TITLE);
    swingUI.injectMenuAction(getPasteAction(), EDIT_TITLE);
    swingUI.injectMenuAction(getDuplicateAction(), EDIT_TITLE);
    swingUI.injectMenuAction(null, EDIT_TITLE);
    swingUI.injectMenuAction(actionFactory.createSelectAllAction(plugInPort), EDIT_TITLE);
    swingUI.injectMenuAction(getEditSelectionAction(), EDIT_TITLE);
    swingUI.injectMenuAction(getDeleteSelectionAction(), EDIT_TITLE);
    swingUI.injectSubmenu(TRANSFORM_TITLE, IconLoader.MagicWand.getIcon(), EDIT_TITLE);
    swingUI.injectMenuAction(getRotateClockwiseAction(), TRANSFORM_TITLE);
    swingUI.injectMenuAction(getRotateCounterclockwiseAction(), TRANSFORM_TITLE);
    swingUI.injectMenuAction(null, TRANSFORM_TITLE);
    swingUI.injectMenuAction(getMirrorHorizontallyAction(), TRANSFORM_TITLE);
    swingUI.injectMenuAction(getMirrorVerticallyAction(), TRANSFORM_TITLE);
    swingUI.injectMenuAction(null, TRANSFORM_TITLE);
    swingUI.injectMenuAction(getNudgeAction(), TRANSFORM_TITLE);
    swingUI.injectMenuAction(null, TRANSFORM_TITLE);
    swingUI.injectMenuAction(getSendToBackAction(), TRANSFORM_TITLE);
    swingUI.injectMenuAction(getBringToFrontAction(), TRANSFORM_TITLE);
    swingUI.injectMenuAction(null, TRANSFORM_TITLE);
    swingUI.injectMenuAction(getGroupAction(), TRANSFORM_TITLE);
    swingUI.injectMenuAction(getUngroupAction(), TRANSFORM_TITLE);
    swingUI.injectMenuAction(null, EDIT_TITLE);
    // swingUI.injectMenuAction(getSaveAsTemplateAction(), EDIT_TITLE);
    swingUI.injectSubmenu(RENUMBER_TITLE, IconLoader.Sort.getIcon(), EDIT_TITLE);
    swingUI.injectMenuAction(getRenumberXAxisAction(), RENUMBER_TITLE);
    swingUI.injectMenuAction(getRenumberYAxisAction(), RENUMBER_TITLE);
    swingUI.injectSubmenu(EXPAND_TITLE, IconLoader.BranchAdd.getIcon(), EDIT_TITLE);
    swingUI.injectMenuAction(getExpandSelectionAllAction(), EXPAND_TITLE);
    swingUI.injectMenuAction(getExpandSelectionImmediateAction(), EXPAND_TITLE);
    swingUI.injectMenuAction(getExpandSelectionSameTypeAction(), EXPAND_TITLE);
    swingUI.injectMenuAction(null, EDIT_TITLE);
    swingUI.injectMenuAction(actionFactory.createEditProjectAction(plugInPort), EDIT_TITLE);

    refreshActions();
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.of(EventType.SELECTION_CHANGED, EventType.PROJECT_MODIFIED, EventType.PROJECT_LOADED);
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public void processMessage(EventType eventType, Object... params) {
    switch (eventType) {
      case SELECTION_CHANGED:
        refreshActions();
        break;
      case PROJECT_MODIFIED:
        undoHandler.stateChanged((Project) params[0], (Project) params[1], (String) params[2]);
        break;
      case PROJECT_LOADED:
        if ((Boolean) params[1])
          undoHandler.reset();
        break;
    }
  }

  private void refreshActions() {
    boolean enabled = !plugInPort.getSelectedComponents().isEmpty();
    getCutAction().setEnabled(enabled);
    getCopyAction().setEnabled(enabled);
    getDuplicateAction().setEnabled(enabled);
    try {
      getPasteAction().setEnabled(clipboard.isDataFlavorAvailable(ComponentTransferable.listFlavor));
    } catch (Exception e) {
      getPasteAction().setEnabled(false);
    }
    getEditSelectionAction().setEnabled(enabled);
    getDeleteSelectionAction().setEnabled(enabled);
    getGroupAction().setEnabled(enabled);
    getExpandSelectionAllAction().setEnabled(enabled);
    getExpandSelectionImmediateAction().setEnabled(enabled);
    getExpandSelectionSameTypeAction().setEnabled(enabled);
    getNudgeAction().setEnabled(enabled);
    getUngroupAction().setEnabled(enabled);
    getSendToBackAction().setEnabled(enabled);
    getBringToFrontAction().setEnabled(enabled);
    getRotateClockwiseAction().setEnabled(enabled);
    getRotateCounterclockwiseAction().setEnabled(enabled);
    getMirrorHorizontallyAction().setEnabled(enabled);
    getMirrorVerticallyAction().setEnabled(enabled);
    getSaveAsTemplateAction().setEnabled(enabled);
    getSaveAsBlockAction().setEnabled(enabled);
  }

  // ClipboardOwner

  @Override
  public void lostOwnership(Clipboard clipboard, Transferable contents) {
    refreshActions();
  }
}
