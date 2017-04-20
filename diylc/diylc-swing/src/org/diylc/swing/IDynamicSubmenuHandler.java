package org.diylc.swing;

import java.util.List;

public interface IDynamicSubmenuHandler {

  void onActionPerformed(String name);

  List<String> getAvailableItems();
}
