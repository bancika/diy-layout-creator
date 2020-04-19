package org.diylc.test;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.diylc.common.ComponentType;
import org.diylc.common.IPlugInPort;
import org.diylc.core.Template;
import org.diylc.test.DIYTest.TestStep;

public class TestPlayer {
  
  private static final Logger LOG = Logger.getLogger(TestPlayer.class);
  
  public static final int DELAY_MS = 2;
  
  public List<StepResult> play(IPlugInPort plugInPort, DIYTest test) {
    LOG.info("Playing test: " + test.getName());
    List<TestStep> steps = test.getSteps();
    List<StepResult> res = new ArrayList<TestPlayer.StepResult>();
    for (int i = 0; i < steps.size(); i++) {
      TestStep step = steps.get(i);
      try {
        switch(step.getAction()) {
          case DIYTest.SET_COMPONENT_SLOT:
            plugInPort.setNewComponentTypeSlot((ComponentType)step.getParams().get("componentType"), (Template)step.getParams().get("template"), (boolean)step.getParams().get("forceInstatiate"));
            res.add(new StepResult(i, step.getAction(), "OK", false));
            break;
          case DIYTest.MOUSE_MOVED:
            plugInPort.mouseMoved((Point)step.getParams().get("point"), (boolean)step.getParams().get("ctrlDown"), (boolean)step.getParams().get("shiftDown"), (boolean)step.getParams().get("altDown"));
            res.add(new StepResult(i, step.getAction(), "OK", false));
            break;
          case DIYTest.MOUSE_CLICKED:            
            plugInPort.mouseClicked((Point)step.getParams().get("point"), (int)step.getParams().get("button"), (boolean)step.getParams().get("ctrlDown"), 
                (boolean)step.getParams().get("shiftDown"), (boolean)step.getParams().get("altDown"), (int)step.getParams().get("clickCount"));
            res.add(new StepResult(i, step.getAction(), "OK", false));
            break;
          case DIYTest.DRAG_START:
            plugInPort.dragStarted((Point)step.getParams().get("point"), (int)step.getParams().get("dragAction"), (boolean)step.getParams().get("forceSelectionRect"));
            res.add(new StepResult(i, step.getAction(), "OK", false));
            break;
          case DIYTest.DRAG_OVER:
            plugInPort.dragOver((Point)step.getParams().get("point"));
            res.add(new StepResult(i, step.getAction(), "OK", false));
            break;
          case DIYTest.DRAG_END:
            plugInPort.dragEnded((Point)step.getParams().get("point"));
            res.add(new StepResult(i, step.getAction(), "OK", false));
            break;
          case DIYTest.VALIDATION:
            res.add(runValidation(i, plugInPort, step.getParams()));
            break;
          default:
            res.add(new StepResult(i, step.getAction(), "Invalid action: " + step.getAction(), true));
        }
      } catch (Exception e) {
        res.add(new StepResult(i, step.getAction(), "Step failed with error: " + e.getMessage(), true));
      }
      
      try {
        Thread.sleep(DELAY_MS);
      } catch (InterruptedException e) {       
      }
    }
    LOG.info("Finished test: " + test.getName());
    return res;
  }
  
  private StepResult runValidation(int step, IPlugInPort plugInPort, Map<String, Object> params) {
    if (params.containsKey(DIYTest.SNAPSHOT)) {
      Snapshot snap = (Snapshot) params.get(DIYTest.SNAPSHOT);
      if (!snap.equalsTo(plugInPort.createTestSnapshot())) {
        return new StepResult(step, DIYTest.VALIDATION, "Snapshots do not match", true);
      }
    }
    return new StepResult(step, DIYTest.VALIDATION, "OK", false);
  }
  
  public class StepResult {
    private int step;
    private String action;
    private String status;
    private boolean hasError;
    
    public StepResult(int step, String action, String status, boolean hasError) {
      super();
      this.step = step;
      this.action = action;
      this.status = status;
      this.hasError = hasError;
    }
    
    public int getStep() {
      return step;
    }
    
    public String getAction() {
      return action;
    }
    
    public String getStatus() {
      return status;
    }
    
    public boolean hasError() {
      return hasError;
    }
  }
}
