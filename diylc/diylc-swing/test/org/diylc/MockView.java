package org.diylc;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.diylc.common.PropertyWrapper;
import org.diylc.core.IView;

public class MockView implements IView {

  @Override
  public void showMessage(String message, String title, int messageType) {
    // TODO Auto-generated method stub

  }

  @Override
  public int showConfirmDialog(String message, String title, int optionType, int messageType) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean editProperties(List<PropertyWrapper> properties,
      Set<PropertyWrapper> defaultedProperties, String title) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public File promptFileSave() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String showInputDialog(String message, String title) {
    // TODO Auto-generated method stub
    return null;
  }

}
