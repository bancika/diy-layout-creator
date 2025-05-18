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
package org.diylc.swing.plugins.test;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.apache.log4j.Logger;

import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.serialization.ProjectFileManager;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.plugins.file.FileFilterEnum;
import org.diylc.test.DIYTest;
import org.diylc.test.TestPlayer;
import org.diylc.test.TestPlayer.StepResult;

/**
 * Entry point class for help-related utilities.
 * 
 * @author Branislav Stojkovic
 */
public class TestMenuPlugin implements IPlugIn {

  private static final String TEST_TITLE = "Test";
  
  private static final Logger LOG = Logger.getLogger(TestMenuPlugin.class);

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;
  
  private ProjectFileManager projectFileManager;
  private TestPlayer testPlayer;

  public TestMenuPlugin(ISwingUI swingUI) {
    this.swingUI = swingUI;
    swingUI.injectMenuAction(new StartRecordAction(), TEST_TITLE);
    swingUI.injectMenuAction(new AddValidationAction(), TEST_TITLE);
    swingUI.injectMenuAction(new StopRecordAction(), TEST_TITLE);
    swingUI.injectMenuAction(new PlayTestAction(), TEST_TITLE);
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    projectFileManager = new ProjectFileManager(null);
    testPlayer = new TestPlayer();
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return null;
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {}



  class StartRecordAction extends AbstractAction {

    private static final long serialVersionUID = 1L;    

    public StartRecordAction() {
      super();      
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R,
          ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
      putValue(AbstractAction.NAME, "Start Recording");      
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      String name = swingUI.showInputDialog("Test Name:", "Start Recording");
      if (name == null)
        return;
      
      LOG.info("Starting test recording...");
      
      plugInPort.startRecording(name);
    }
  }
  
  class StopRecordAction extends AbstractAction {

    private static final long serialVersionUID = 1L;    

    public StopRecordAction() {
      super();      
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T,
          ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
      putValue(AbstractAction.NAME, "Stop Recording");      
    }

    @Override
    public void actionPerformed(ActionEvent e) {     
      DIYTest test = plugInPort.stopRecording();
      
      if (test == null)
        return;
      
      LOG.info("Stopping test recording...");            
      
      final File file = DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(),
          FileFilterEnum.TEST.getFilter(), new File(test.getName() + ".tst"), FileFilterEnum.TEST.getExtensions()[0], null);
      if (file != null) {
        swingUI.executeBackgroundTask(new ITask<Void>() {

          @Override
          public Void doInBackground() throws Exception {
            LOG.debug("Saving test to " + file.getAbsolutePath());
            projectFileManager.serializeTestToFile(test, file.getAbsolutePath());
            return null;
          }

          @Override
          public void complete(Void result) {}

          @Override
          public void failed(Exception e) {
            swingUI.showMessage("Could not save test: " + e.getMessage(), "Error",
                ISwingUI.ERROR_MESSAGE);
          }
        }, true);
      }
    }
  }
  
  class PlayTestAction extends AbstractAction {

    private static final long serialVersionUID = 1L;    

    public PlayTestAction() {
      super();      
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P,
          ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
      putValue(AbstractAction.NAME, "Play Test");      
    }

    @Override
    public void actionPerformed(ActionEvent e) {   
      LOG.info("Starting test recording...");
      
      final File file = DialogFactory.getInstance().showOpenDialog(FileFilterEnum.TEST.getFilter(),
          null, FileFilterEnum.TEST.getExtensions()[0], null);
            
      plugInPort.createNewProject();
      
      if (file != null) {
        swingUI.executeBackgroundTask(new ITask<List<StepResult>>() {

          @Override
          public List<StepResult> doInBackground() throws Exception {
            DIYTest test = projectFileManager.deserializeTestFromFile(file.getAbsolutePath());
            List<StepResult> testResults = testPlayer.play(plugInPort, test);
            return testResults;
          }

          @Override
          public void complete(List<StepResult> result) {
            StringBuilder sb = new StringBuilder();
            boolean hasError = false;
            for (StepResult r : result) {
              if (r.hasError()) {
                sb.append("Step ").append(r.getStep()).append(" status: ").append(r.getStatus()).append("\n");            
                hasError = true;
              }
            }
            
            if (!hasError)
              sb.append("Test executed SUCCESFULLY.");
            
            swingUI.showMessage(sb.toString(), "Test Results", hasError ? ISwingUI.ERROR_MESSAGE : ISwingUI.INFORMATION_MESSAGE);
          }

          @Override
          public void failed(Exception e) {
            swingUI.showMessage("Could not play test: " + e.getMessage(), "Error",
                ISwingUI.ERROR_MESSAGE);  
          }
        }, true);              
      }
    }
  }
  
  class AddValidationAction extends AbstractAction {

    private static final long serialVersionUID = 1L;    

    public AddValidationAction() {
      super();      
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V,
          ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
      putValue(AbstractAction.NAME, "Add Validation Step");      
    }

    @Override
    public void actionPerformed(ActionEvent e) {   
      LOG.info("Adding test validation step");
      Map<String, Object> params = new HashMap<String, Object>();
      params.put(DIYTest.SNAPSHOT, plugInPort.createTestSnapshot());
      plugInPort.addValidation(params);
      swingUI.showMessage("Validation step added.", "Test", ISwingUI.INFORMATION_MESSAGE);
    }
  }
}
