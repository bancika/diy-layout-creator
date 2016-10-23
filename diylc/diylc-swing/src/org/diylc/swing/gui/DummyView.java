package org.diylc.swing.gui;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.diylc.common.PropertyWrapper;
import org.diylc.core.IView;

public class DummyView implements IView {

  @Override
  public int showConfirmDialog(String message, String title, int optionType, int messageType) {
    return 0;
  }

  @Override
  public void showMessage(String message, String title, int messageType) {}

  @Override
  public File promptFileSave() {
    return null;
  }

  @Override
  public boolean editProperties(List<PropertyWrapper> properties, Set<PropertyWrapper> defaultedProperties) {
    return false;
  }
}
