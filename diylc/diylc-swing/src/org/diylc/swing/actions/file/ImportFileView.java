package org.diylc.swing.actions.file;

import org.diylc.common.PropertyWrapper;
import org.diylc.core.IView;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.Set;

@Component
public class ImportFileView implements IView {

        @Override
        public int showConfirmDialog(String message, String title, int optionType,
        int messageType) {
            return JOptionPane.showConfirmDialog(null, message, title, optionType, messageType);
        }

        @Override
        public void showMessage(String message, String title, int messageType) {
            JOptionPane.showMessageDialog(null, message, title, messageType);
        }

        @Override
        public String showInputDialog(String message, String title) {
            return JOptionPane.showInputDialog(null, message, title, JOptionPane.QUESTION_MESSAGE);
        }

        @Override
        public File promptFileSave() {
            return null;
        }

        @Override
        public boolean editProperties(List< PropertyWrapper > properties,
                                      Set<PropertyWrapper> defaultedProperties, String title) {
            return false;
        }
}
