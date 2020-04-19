package org.diylc.test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DIYTest implements Serializable {    
  
  private static final long serialVersionUID = 1L;
  
  public static final String MOUSE_MOVED = "mouseMoved";
  public static final String MOUSE_CLICKED = "mouseClicked";
  public static final String SET_COMPONENT_SLOT = "setComponentSlot";
  public static final String VALIDATION = "validation";
  public static final String DRAG_START = "dragStart";
  public static final String DRAG_OVER = "dragOver";
  public static final String DRAG_END = "dragEnd";
  
  // validations
  public static final String SNAPSHOT = "snapshot";
  public static final String COMPLETE = "complete";
  public static final String PROJECT = "project";
  public static final String SELECTION = "selection";
  
  private String name;
  private List<TestStep> steps;
  
  public DIYTest() {
  }
  
  public DIYTest(String name) {
    this.name = name;
    steps = new ArrayList<DIYTest.TestStep>();
  }
  
  public void addStep(String action, Map<String, Object> params) {
    steps.add(new TestStep(action, params));
  }
  
  public String getName() {
    return name;
  }
  
  public List<TestStep> getSteps() {
    return steps;
  }

  public class TestStep implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String action;
    private Map<String, Object> params;     
    
    public TestStep() {
    }
    
    public TestStep(String action, Map<String, Object> params) {
      super();
      this.action = action;
      this.params = params;
    }

    public String getAction() {
      return action;
    }
    
    public Map<String, Object> getParams() {
      return params;
    }
    
    @Override
    public String toString() {
      return action;
    }
  }
}
