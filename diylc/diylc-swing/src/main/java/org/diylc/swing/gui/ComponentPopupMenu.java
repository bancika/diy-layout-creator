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
package org.diylc.swing.gui;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.log4j.Logger;

import org.diylc.clipboard.ComponentTransferable;
import org.diylc.common.ComponentType;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.IPlugInPort;
import org.diylc.core.ExpansionMode;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Template;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.actions.FlexibleLeadsAction;
import org.diylc.swing.actions.edit.BringToFrontAction;
import org.diylc.swing.actions.edit.CopyAction;
import org.diylc.swing.actions.edit.CutAction;
import org.diylc.swing.actions.edit.DeleteSelectionAction;
import org.diylc.swing.actions.edit.DuplicateAction;
import org.diylc.swing.actions.edit.EditSelectionAction;
import org.diylc.swing.actions.edit.ExpandSelectionAction;
import org.diylc.swing.actions.edit.GroupAction;
import org.diylc.swing.actions.edit.MirrorSelectionAction;
import org.diylc.swing.actions.edit.NudgeAction;
import org.diylc.swing.actions.edit.PasteAction;
import org.diylc.swing.actions.edit.RotateSelectionAction;
import org.diylc.swing.actions.edit.SaveAsBlockAction;
import org.diylc.swing.actions.edit.SaveAsTemplateAction;
import org.diylc.swing.actions.edit.SendToBackAction;
import org.diylc.swing.actions.edit.UngroupAction;
import org.diylc.swing.images.IconLoader;
import org.diylc.swing.plugins.toolbox.ComponentButtonFactory;

public class ComponentPopupMenu extends JPopupMenu implements ClipboardOwner {

  private static final long serialVersionUID = 1L;
  
  private static final Logger LOG = Logger.getLogger(ComponentPopupMenu.class);
  
  private JMenu selectionMenu;
  private JMenu expandMenu;
  private JMenu transformMenu;
  private JMenu applyTemplateMenu;
  private JMenu applyModelMenu;
  private JMenu lockMenu;
  private JMenu unlockMenu;
  
  private CutAction cutAction;
  private CopyAction copyAction;
  private PasteAction pasteAction;
  private DuplicateAction duplicateAction;
  private EditSelectionAction editSelectionAction;
  private DeleteSelectionAction deleteSelectionAction;
  private SaveAsTemplateAction saveAsTemplateAction;
  private SaveAsBlockAction saveAsBlockAction;
  private GroupAction groupAction;
  private UngroupAction ungroupAction;
  private SendToBackAction sendToBackAction;
  private BringToFrontAction bringToFrontAction;
  private NudgeAction nudgeAction;
  private ExpandSelectionAction expandSelectionAllAction;
  private ExpandSelectionAction expandSelectionImmediateAction;
  private ExpandSelectionAction expandSelectionSameTypeAction;
  private RotateSelectionAction rotateClockwiseAction;
  private RotateSelectionAction rotateCounterclockwiseAction;
  private MirrorSelectionAction mirrorHorizontallyAction;
  private MirrorSelectionAction mirrorVerticallyAction;
  private FlexibleLeadsAction flexibleLeadsAction;

  private IPlugInPort plugInPort;
  private Clipboard clipboard;
  private JComponent owner;

  public ComponentPopupMenu(IPlugInPort plugInPort, JComponent owner, boolean showSelectionMenu) {
    this.plugInPort = plugInPort;
    this.owner = owner;
    this.clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    
    if (showSelectionMenu) {
      add(getSelectionMenu());
      addSeparator();
    }
    
    add(getCutAction());
    add(getCopyAction());
    add(getPasteAction());
    add(getDuplicateAction());
    addSeparator();
    add(getEditSelectionAction());
    add(getDeleteSelectionAction());
    add(getTransformMenu());
    add(getSaveAsTemplateAction());
    add(getApplyTemplateMenu());
    add(getApplyModelMenu());
    add(getSaveAsBlockAction());
    add(getExpandMenu());
    addSeparator();
    add(getLockMenu());
    add(getUnlockMenu());
    addSeparator();
    add(getFlexibleLeadsAction());
    addSeparator();
    add(ActionFactory.getInstance().createEditProjectAction(plugInPort));
  }
  
  public JMenu getSelectionMenu() {
    if (selectionMenu == null) {
      selectionMenu = new TranslatedMenu("Select");
      selectionMenu.setIcon(IconLoader.ElementsSelection.getIcon());
    }
    return selectionMenu;
  }

  public JMenu getExpandMenu() {
    if (expandMenu == null) {
      expandMenu = new TranslatedMenu("Expand Selection");
      expandMenu.setIcon(IconLoader.BranchAdd.getIcon());
      expandMenu.add(getExpandSelectionAllAction());
      expandMenu.add(getExpandSelectionImmediateAction());
      expandMenu.add(getExpandSelectionSameTypeAction());
    }
    return expandMenu;
  }

  public JMenu getLockMenu() {
    if (lockMenu == null) {
      lockMenu = new TranslatedMenu("Lock");
      lockMenu.setIcon(IconLoader.Lock.getIcon());
    }
    return lockMenu;
  }

  public JMenu getUnlockMenu() {
    if (unlockMenu == null) {
      unlockMenu = new TranslatedMenu("Unlock");
      unlockMenu.setIcon(IconLoader.Unlock.getIcon());
    }
    return unlockMenu;
  }

  public JMenu getTransformMenu() {
    if (transformMenu == null) {
      transformMenu = new TranslatedMenu("Transform Selection");
      transformMenu.setIcon(IconLoader.MagicWand.getIcon());
      transformMenu.add(getRotateClockwiseAction());
      transformMenu.add(getRotateCounterclockwiseAction());
      transformMenu.addSeparator();
      transformMenu.add(getMirrorHorizontallyAction());
      transformMenu.add(getMirrorVerticallyAction());
      transformMenu.addSeparator();
      transformMenu.add(getNudgeAction());
      transformMenu.addSeparator();
      transformMenu.add(getSendToBackAction());
      transformMenu.add(getBringToFrontAction());
      transformMenu.addSeparator();
      transformMenu.add(getGroupAction());
      transformMenu.add(getUngroupAction());
    }
    return transformMenu;
  }

  public JMenu getApplyTemplateMenu() {
    if (applyTemplateMenu == null) {
      applyTemplateMenu = new TranslatedMenu("Apply Variant");
      applyTemplateMenu.setIcon(IconLoader.BriefcaseInto.getIcon());
    }
    return applyTemplateMenu;
  }
  
  public JMenu getApplyModelMenu() {
    if (applyModelMenu == null) {
      applyModelMenu = new TranslatedMenu("Apply Model");
      applyModelMenu.setIcon(IconLoader.Barcode.getIcon());      
    }
    return applyModelMenu;
  }
  
  public void prepareAndShowAt(Collection<IDIYComponent<?>> componentsForSelection, Collection<IDIYComponent<?>> componentsForLock, int x, int y) {
    boolean enabled = !plugInPort.getSelectedComponents().isEmpty();
    getCutAction().setEnabled(enabled);
    getCopyAction().setEnabled(enabled);
    getDuplicateAction().setEnabled(enabled);
    try {
      getPasteAction().setEnabled(
          clipboard.isDataFlavorAvailable(ComponentTransferable.listFlavor));
    } catch (Exception ex) {
      getPasteAction().setEnabled(false);
    }
    getEditSelectionAction().setEnabled(enabled);
    getDeleteSelectionAction().setEnabled(enabled);
    getExpandSelectionAllAction().setEnabled(enabled);
    getExpandSelectionImmediateAction().setEnabled(enabled);
    getExpandSelectionSameTypeAction().setEnabled(enabled);
    getGroupAction().setEnabled(enabled);
    getUngroupAction().setEnabled(enabled);
    getNudgeAction().setEnabled(enabled);
    getSendToBackAction().setEnabled(enabled);
    getBringToFrontAction().setEnabled(enabled);
    getRotateClockwiseAction().setEnabled(enabled);
    getRotateCounterclockwiseAction().setEnabled(enabled);
    getMirrorHorizontallyAction().setEnabled(enabled);
    getMirrorVerticallyAction().setEnabled(enabled);
    getFlexibleLeadsAction().setEnabled(enabled);

    getSaveAsTemplateAction()
        .setEnabled(plugInPort.getSelectedComponents().size() == 1);
    getSaveAsBlockAction().setEnabled(plugInPort.getSelectedComponents().size() > 1);
    
    updateSelectionMenu(componentsForSelection);
    updateApplyTemplateMenu();
    updateApplyModelMenu();
    updateLock(componentsForLock);
    show(owner, x, y);
  }
  
  private void updateSelectionMenu(Collection<IDIYComponent<?>> componentsAt) {
    getSelectionMenu().removeAll();
    for (IDIYComponent<?> component : componentsAt) {
      JMenuItem item = new JMenuItem(component.getName());
      final IDIYComponent<?> finalComponent = component;
      item.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
          newSelection.add(finalComponent);
          plugInPort.setSelection(newSelection, false);          
        }
      });
      getSelectionMenu().add(item);
    }
  }
  
  private void updateApplyTemplateMenu() {
    JMenu applyMenu = getApplyTemplateMenu();
    applyMenu.removeAll();
    List<Template> templates = null;

    try {
      templates = plugInPort.getVariantsForSelection();
    } catch (Exception e) {
      LOG.info("Could not load variants for selection");
      applyMenu.setEnabled(false);
    }

    if (templates == null) {
      applyMenu.setEnabled(false);
      return;
    }

    applyMenu.setEnabled(templates.size() > 0);

    for (Template template : templates) {
      JMenuItem item = new JMenuItem(template.getName());
      final Template finalTemplate = template;
      item.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          plugInPort.applyVariantToSelection(finalTemplate);
        }
      });
      applyMenu.add(item);
    }
  }
  
  private void updateApplyModelMenu() {
    JMenu applyMenu = getApplyModelMenu();
    applyMenu.removeAll();
    
    Collection<ComponentType> selectedComponentTypes = plugInPort.getSelectedComponentTypes();
    if (selectedComponentTypes.size() != 1) {
      applyMenu.setEnabled(false);
      return;
    }
    
    ComponentType componentType = selectedComponentTypes.stream().findFirst().get();
    List<String[]> datasheet = componentType.getDatasheet();
    if (datasheet == null || datasheet.isEmpty()) {
      applyMenu.setEnabled(false);
      return;
    }
    
    applyMenu.setEnabled(true);
    List<Component> datasheetItems = ComponentButtonFactory.createDatasheetItems(plugInPort, componentType, new ArrayList<String>(), model -> {
      LOG.info("Applying datasheet model " + String.join(", ", model));      
      plugInPort.applyModelToSelection(model);
    });
    for (Component item : datasheetItems) {
      applyMenu.add(item);
    }
  }
  
  private void updateLock(Collection<IDIYComponent<?>> componentsAt) {
    getLockMenu().removeAll();
    getUnlockMenu().removeAll();    
    if (componentsAt.size() == 0) {
      getLockMenu().setEnabled(false);
      getUnlockMenu().setEnabled(false);
    } else {
      boolean hasLocked = false;
      boolean hasUnlocked = false;
      for (IDIYComponent<?> c : componentsAt) {
        if (plugInPort.getCurrentProject().getLockedComponents().contains(c)) {
          getUnlockMenu().add(new LockAction(c, false));
          hasLocked = true;
        } else {
          getLockMenu().add(new LockAction(c, true));
          hasUnlocked = true;
        }
      }
      getLockMenu().setEnabled(hasUnlocked);
      getUnlockMenu().setEnabled(hasLocked);
    }
  }

  public CutAction getCutAction() {
    if (cutAction == null) {
      cutAction = ActionFactory.getInstance().createCutAction(plugInPort, clipboard, this);
    }
    return cutAction;
  }

  public CopyAction getCopyAction() {
    if (copyAction == null) {
      copyAction = ActionFactory.getInstance().createCopyAction(plugInPort, clipboard, this);
    }
    return copyAction;
  }

  public PasteAction getPasteAction() {
    if (pasteAction == null) {
      pasteAction = ActionFactory.getInstance().createPasteAction(plugInPort, clipboard);
    }
    return pasteAction;
  }

  public DuplicateAction getDuplicateAction() {
    if (duplicateAction == null) {
      duplicateAction = ActionFactory.getInstance().createDuplicateAction(plugInPort);
    }
    return duplicateAction;
  }

  public EditSelectionAction getEditSelectionAction() {
    if (editSelectionAction == null) {
      editSelectionAction = ActionFactory.getInstance().createEditSelectionAction(plugInPort);
    }
    return editSelectionAction;
  }

  public DeleteSelectionAction getDeleteSelectionAction() {
    if (deleteSelectionAction == null) {
      deleteSelectionAction = ActionFactory.getInstance().createDeleteSelectionAction(plugInPort);
    }
    return deleteSelectionAction;
  }

  public RotateSelectionAction getRotateClockwiseAction() {
    if (rotateClockwiseAction == null) {
      rotateClockwiseAction =
          ActionFactory.getInstance().createRotateSelectionAction(plugInPort, 1);
    }
    return rotateClockwiseAction;
  }

  public RotateSelectionAction getRotateCounterclockwiseAction() {
    if (rotateCounterclockwiseAction == null) {
      rotateCounterclockwiseAction =
          ActionFactory.getInstance().createRotateSelectionAction(plugInPort, -1);
    }
    return rotateCounterclockwiseAction;
  }

  public MirrorSelectionAction getMirrorHorizontallyAction() {
    if (mirrorHorizontallyAction == null) {
      mirrorHorizontallyAction = ActionFactory.getInstance().createMirrorSelectionAction(plugInPort,
          IComponentTransformer.HORIZONTAL);
    }
    return mirrorHorizontallyAction;
  }

  public MirrorSelectionAction getMirrorVerticallyAction() {
    if (mirrorVerticallyAction == null) {
      mirrorVerticallyAction = ActionFactory.getInstance().createMirrorSelectionAction(plugInPort,
          IComponentTransformer.VERTICAL);
    }
    return mirrorVerticallyAction;
  }

  public FlexibleLeadsAction getFlexibleLeadsAction() {
    if (flexibleLeadsAction == null)
      flexibleLeadsAction = ActionFactory.getInstance().createFlexibleLeadsAction(plugInPort);
    return flexibleLeadsAction;
  }

  public SaveAsTemplateAction getSaveAsTemplateAction() {
    if (saveAsTemplateAction == null) {
      saveAsTemplateAction = ActionFactory.getInstance().createSaveAsTemplateAction(plugInPort);
    }
    return saveAsTemplateAction;
  }

  public SaveAsBlockAction getSaveAsBlockAction() {
    if (saveAsBlockAction == null) {
      saveAsBlockAction = ActionFactory.getInstance().createSaveAsBlockAction(plugInPort);
    }
    return saveAsBlockAction;
  }

  public GroupAction getGroupAction() {
    if (groupAction == null) {
      groupAction = ActionFactory.getInstance().createGroupAction(plugInPort);
    }
    return groupAction;
  }

  public UngroupAction getUngroupAction() {
    if (ungroupAction == null) {
      ungroupAction = ActionFactory.getInstance().createUngroupAction(plugInPort);
    }
    return ungroupAction;
  }

  public SendToBackAction getSendToBackAction() {
    if (sendToBackAction == null) {
      sendToBackAction = ActionFactory.getInstance().createSendToBackAction(plugInPort);
    }
    return sendToBackAction;
  }

  public BringToFrontAction getBringToFrontAction() {
    if (bringToFrontAction == null) {
      bringToFrontAction = ActionFactory.getInstance().createBringToFrontAction(plugInPort);
    }
    return bringToFrontAction;
  }

  public NudgeAction getNudgeAction() {
    if (nudgeAction == null) {
      nudgeAction = ActionFactory.getInstance().createNudgeAction(plugInPort);
    }
    return nudgeAction;
  }

  public ExpandSelectionAction getExpandSelectionAllAction() {
    if (expandSelectionAllAction == null) {
      expandSelectionAllAction =
          ActionFactory.getInstance().createExpandSelectionAction(plugInPort, ExpansionMode.ALL);
    }
    return expandSelectionAllAction;
  }

  public ExpandSelectionAction getExpandSelectionImmediateAction() {
    if (expandSelectionImmediateAction == null) {
      expandSelectionImmediateAction = ActionFactory.getInstance()
          .createExpandSelectionAction(plugInPort, ExpansionMode.IMMEDIATE);
    }
    return expandSelectionImmediateAction;
  }

  public ExpandSelectionAction getExpandSelectionSameTypeAction() {
    if (expandSelectionSameTypeAction == null) {
      expandSelectionSameTypeAction = ActionFactory.getInstance()
          .createExpandSelectionAction(plugInPort, ExpansionMode.SAME_TYPE);
    }
    return expandSelectionSameTypeAction;
  }

  @Override
  public void lostOwnership(Clipboard clipboard, Transferable contents) {
    LOG.debug("Lost clipboard ownership");
  }
  
  class LockAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IDIYComponent<?> component;
    private boolean locked;

    public LockAction(IDIYComponent<?> component, boolean locked) {
      super();
      this.locked = locked;
      this.component = component;
      putValue(AbstractAction.NAME, component.getName());      
    }

    @Override
    public void actionPerformed(ActionEvent e) {
     plugInPort.lockComponent(component, locked); 
    }   
  }
}
