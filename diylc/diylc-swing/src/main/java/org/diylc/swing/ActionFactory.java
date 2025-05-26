/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.swing;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.diylc.swing.actions.analyze.*;
import org.diylc.swingframework.IDrawingProvider;

import org.diylc.common.INetlistAnalyzer;
import org.diylc.common.IPlugInPort;
import org.diylc.core.ExpansionMode;
import org.diylc.core.Theme;
import org.diylc.swing.actions.CheckProximityAction;
import org.diylc.swing.actions.ComponentBrowserAction;
import org.diylc.swing.actions.ConfigAction;
import org.diylc.swing.actions.ExportGerberAction;
import org.diylc.swing.actions.ExportPNGAction;
import org.diylc.swing.actions.FindAction;
import org.diylc.swing.actions.FlexibleLeadsAction;
import org.diylc.swing.actions.RenumberAction;
import org.diylc.swing.actions.ThemeAction;
import org.diylc.swing.actions.ToggleAction;
import org.diylc.swing.actions.edit.BringToFrontAction;
import org.diylc.swing.actions.edit.CopyAction;
import org.diylc.swing.actions.edit.CutAction;
import org.diylc.swing.actions.edit.DeleteSelectionAction;
import org.diylc.swing.actions.edit.DuplicateAction;
import org.diylc.swing.actions.edit.EditProjectAction;
import org.diylc.swing.actions.edit.EditSelectionAction;
import org.diylc.swing.actions.edit.ExpandSelectionAction;
import org.diylc.swing.actions.edit.GroupAction;
import org.diylc.swing.actions.edit.MirrorSelectionAction;
import org.diylc.swing.actions.edit.NudgeAction;
import org.diylc.swing.actions.edit.PasteAction;
import org.diylc.swing.actions.edit.RotateSelectionAction;
import org.diylc.swing.actions.edit.SaveAsBlockAction;
import org.diylc.swing.actions.edit.SaveAsTemplateAction;
import org.diylc.swing.actions.edit.SelectAllAction;
import org.diylc.swing.actions.edit.SendToBackAction;
import org.diylc.swing.actions.edit.UngroupAction;
import org.diylc.swing.actions.file.CreateBomAction;
import org.diylc.swing.actions.file.ExitAction;
import org.diylc.swing.actions.file.ExportBlocksAction;
import org.diylc.swing.actions.file.ExportPDFAction;
import org.diylc.swing.actions.file.ExportVariantsAction;
import org.diylc.swing.actions.file.ImportAction;
import org.diylc.swing.actions.file.ImportBlocksAction;
import org.diylc.swing.actions.file.ImportNetlistAction;
import org.diylc.swing.actions.file.ImportVariantsAction;
import org.diylc.swing.actions.file.NewAction;
import org.diylc.swing.actions.file.OpenAction;
import org.diylc.swing.actions.file.PrintAction;
import org.diylc.swing.actions.file.SaveAction;
import org.diylc.swing.actions.file.SaveAsAction;

public class ActionFactory {

  public static final Logger LOG = Logger.getLogger(ActionFactory.class);

  private static ActionFactory instance;

  public static ActionFactory getInstance() {
    if (instance == null) {
      instance = new ActionFactory();
    }
    return instance;
  }

  private ActionFactory() {}

  // File menu actions.

  public NewAction createNewAction(IPlugInPort plugInPort) {
    return new NewAction(plugInPort);
  }

  public OpenAction createOpenAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    return new OpenAction(plugInPort, swingUI);
  }

  public ImportAction createImportAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    return new ImportAction(plugInPort, swingUI);
  }

  public ImportNetlistAction createImportNetlistAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    return new ImportNetlistAction(plugInPort, swingUI);
  }

  public SaveAction createSaveAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    return new SaveAction(plugInPort, swingUI);
  }

  public SaveAsAction createSaveAsAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    return new SaveAsAction(plugInPort, swingUI);
  }

  public CreateBomAction createBomAction(IPlugInPort plugInPort) {
    return new CreateBomAction(plugInPort);
  }

  public ExportPDFAction createExportPDFAction(IPlugInPort plugInPort,
      IDrawingProvider drawingProvider, ISwingUI swingUI, String defaultSuffix) {
    return new ExportPDFAction(plugInPort, drawingProvider, swingUI, defaultSuffix);
  }
  
  public ExportGerberAction createExportGerberAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    return new ExportGerberAction(plugInPort, swingUI);
  }

  public ExportPNGAction createExportPNGAction(IPlugInPort plugInPort,
      IDrawingProvider drawingProvider, ISwingUI swingUI, String defaultSuffix) {
    return new ExportPNGAction(plugInPort, drawingProvider, swingUI, defaultSuffix);
  }

  public PrintAction createPrintAction(IDrawingProvider drawingProvider, ISwingUI swingUI, int keyModifiers) {
    return new PrintAction(drawingProvider, swingUI, keyModifiers);
  }

  public ExportVariantsAction createExportVariantsAction(ISwingUI swingUI, IPlugInPort plugInPort) {
    return new ExportVariantsAction(swingUI, plugInPort);
  }

  public ImportVariantsAction createImportVariantsAction(ISwingUI swingUI, IPlugInPort plugInPort) {
    return new ImportVariantsAction(swingUI, plugInPort);
  }

  public ExportBlocksAction createExportBlocksAction(ISwingUI swingUI) {
    return new ExportBlocksAction(swingUI);
  }

  public ImportBlocksAction createImportBlocksAction(ISwingUI swingUI, IPlugInPort plugInPort) {
    return new ImportBlocksAction(swingUI, plugInPort);
  }

  public ExitAction createExitAction(IPlugInPort plugInPort) {
    return new ExitAction(plugInPort);
  }

  // Edit menu actions.

  public CutAction createCutAction(IPlugInPort plugInPort, Clipboard clipboard,
      ClipboardOwner clipboardOwner) {
    return new CutAction(plugInPort, clipboard, clipboardOwner);
  }

  public CopyAction createCopyAction(IPlugInPort plugInPort, Clipboard clipboard,
      ClipboardOwner clipboardOwner) {
    return new CopyAction(plugInPort, clipboard, clipboardOwner);
  }

  public PasteAction createPasteAction(IPlugInPort plugInPort, Clipboard clipboard) {
    return new PasteAction(plugInPort, clipboard);
  }

  public DuplicateAction createDuplicateAction(IPlugInPort plugInPort) {
    return new DuplicateAction(plugInPort);
  }

  public SelectAllAction createSelectAllAction(IPlugInPort plugInPort) {
    return new SelectAllAction(plugInPort);
  }

  public GroupAction createGroupAction(IPlugInPort plugInPort) {
    return new GroupAction(plugInPort);
  }

  public UngroupAction createUngroupAction(IPlugInPort plugInPort) {
    return new UngroupAction(plugInPort);
  }

  public EditProjectAction createEditProjectAction(IPlugInPort plugInPort) {
    return new EditProjectAction(plugInPort);
  }

  public EditSelectionAction createEditSelectionAction(IPlugInPort plugInPort) {
    return new EditSelectionAction(plugInPort);
  }

  public DeleteSelectionAction createDeleteSelectionAction(IPlugInPort plugInPort) {
    return new DeleteSelectionAction(plugInPort);
  }

  public SaveAsTemplateAction createSaveAsTemplateAction(IPlugInPort plugInPort) {
    return new SaveAsTemplateAction(plugInPort);
  }

  public SaveAsBlockAction createSaveAsBlockAction(IPlugInPort plugInPort) {
    return new SaveAsBlockAction(plugInPort);
  }

  public ExpandSelectionAction createExpandSelectionAction(IPlugInPort plugInPort,
      ExpansionMode expansionMode) {
    return new ExpandSelectionAction(plugInPort, expansionMode);
  }

  public RotateSelectionAction createRotateSelectionAction(IPlugInPort plugInPort, int direction) {
    return new RotateSelectionAction(plugInPort, direction);
  }

  public MirrorSelectionAction createMirrorSelectionAction(IPlugInPort plugInPort, int direction) {
    return new MirrorSelectionAction(plugInPort, direction);
  }

  public SendToBackAction createSendToBackAction(IPlugInPort plugInPort) {
    return new SendToBackAction(plugInPort);
  }

  public BringToFrontAction createBringToFrontAction(IPlugInPort plugInPort) {
    return new BringToFrontAction(plugInPort);
  }

  public NudgeAction createNudgeAction(IPlugInPort plugInPort) {
    return new NudgeAction(plugInPort);
  }

  public FindAction createFindAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    return new FindAction(plugInPort, swingUI);
  }

  public FlexibleLeadsAction createFlexibleLeadsAction(IPlugInPort plugInPort) {
    return new FlexibleLeadsAction(plugInPort);
  }

  // Config actions.

  public ConfigAction createConfigAction(IPlugInPort plugInPort, String title, String configKey,
      boolean defaultValue) {
    return new ConfigAction(plugInPort, title, configKey, defaultValue);
  }

  public ToggleAction createToggleAction(String title, String configKey, String groupName,
      String defaultValue) {
    return new ToggleAction(title, configKey, groupName, defaultValue, null);
  }

  public ToggleAction createToggleAction(String title, String configKey, String groupName,
      String defaultValue, Icon icon) {
    return new ToggleAction(title, configKey, groupName, defaultValue, icon);
  }

  public ThemeAction createThemeAction(IPlugInPort plugInPort, Theme theme) {
    return new ThemeAction(plugInPort, theme);
  }

  public ComponentBrowserAction createComponentBrowserAction(String browserType) {
    return new ComponentBrowserAction(browserType);
  }

  public RenumberAction createRenumberAction(IPlugInPort plugInPort, boolean xAxisFirst) {
    return new RenumberAction(plugInPort, xAxisFirst);
  }

  public GenerateNetlistAction createGenerateNetlistAction(IPlugInPort plugInPort, ISwingUI swingUI,
      boolean includeSwitches) {
    return new GenerateNetlistAction(plugInPort, swingUI, includeSwitches);
  }

  public AiAnalyzeAction createAiAnalyzeAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    return new AiAnalyzeAction(plugInPort, swingUI);
  }

  public CompareAction createCompareAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    return new CompareAction(plugInPort, swingUI);
  }

  public SummarizeNetlistAction createSummarizeNetlistAction(IPlugInPort plugInPort,
      ISwingUI swingUI, INetlistAnalyzer summarizer, boolean includeSwitches) {
    return new SummarizeNetlistAction(plugInPort, swingUI, summarizer, includeSwitches);
  }

  public CheckProximityAction createCheckProximityAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    return new CheckProximityAction(plugInPort, swingUI);
  }

  public HighlightConnectedAreasAction createHighlightConnectedAreasAction(IPlugInPort plugInPort) {
    return new HighlightConnectedAreasAction(plugInPort);
  }
}
