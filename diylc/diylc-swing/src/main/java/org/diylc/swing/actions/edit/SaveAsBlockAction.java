package org.diylc.swing.actions.edit;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IBlockProcessor;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;
import org.diylc.utils.IconLoader;

public class SaveAsBlockAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  /** How many recently used building blocks to offer in the name drop-down. */
  private static final int MAX_RECENT_BLOCKS = 10;

  private IPlugInPort plugInPort;

  public SaveAsBlockAction(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
    putValue(AbstractAction.NAME, "Save as Building Block");
    putValue(AbstractAction.SMALL_ICON, IconLoader.ComponentAdd.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Save as building block triggered");

    JComboBox<String> nameBox =
        new JComboBox<String>(getRecentBlockNames().toArray(new String[0]));
    nameBox.setEditable(true);
    // Start empty so an existing block is never overwritten by accident. The drop-down still
    // offers the most recently used blocks (most recent first), so a user editing a block can
    // pick its name back with a single click instead of retyping it.
    nameBox.setSelectedItem("");

    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.add(new JLabel("Block name:"), BorderLayout.NORTH);
    panel.add(nameBox, BorderLayout.CENTER);

    // Loop so that declining the overwrite prompt returns to the name dialog (with whatever was
    // typed still in it) rather than cancelling the whole action.
    while (true) {
      int result = JOptionPane.showConfirmDialog(null, panel, "Save as Building Block",
          JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (result != JOptionPane.OK_OPTION)
        return;

      // getEditor().getItem() captures in-progress typing as well as a picked item
      Object selected = nameBox.getEditor().getItem();
      String blockName = selected == null ? null : selected.toString().trim();
      if (blockName == null || blockName.isEmpty())
        return;

      if (blockExists(blockName)) {
        int overwrite = JOptionPane.showConfirmDialog(null,
            "A building block named \"" + blockName + "\" already exists. Overwrite it?",
            "Save as Building Block", JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (overwrite == JOptionPane.NO_OPTION)
          continue; // back to the name dialog
        if (overwrite != JOptionPane.YES_OPTION)
          return; // Cancel or dialog closed
      }

      plugInPort.saveSelectionAsBlock(blockName);
      return;
    }
  }

  @SuppressWarnings("unchecked")
  private boolean blockExists(String blockName) {
    Map<String, ?> blocks = (Map<String, ?>) ConfigurationManager.getInstance()
        .readObject(IBlockProcessor.BLOCKS_KEY, null);
    return blocks != null && blocks.containsKey(blockName);
  }

  @SuppressWarnings("unchecked")
  private List<String> getRecentBlockNames() {
    List<String> recent = (List<String>) ConfigurationManager.getInstance()
        .readObject(IPlugInPort.RECENT_COMPONENTS_KEY, null);
    List<String> blockNames = new ArrayList<String>();
    if (recent != null) {
      for (String identifier : recent) {
        if (identifier.startsWith(IBlockProcessor.BLOCK_PREFIX)) {
          blockNames.add(identifier.substring(IBlockProcessor.BLOCK_PREFIX.length()));
          if (blockNames.size() >= MAX_RECENT_BLOCKS)
            break;
        }
      }
    }
    return blockNames;
  }
}
