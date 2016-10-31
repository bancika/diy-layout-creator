package org.diylc.swing.gui.components;

import javax.swing.JTextArea;

/**
 * {@link JTextArea} that replaces new line characters with html &lt;br&gt; tags.
 * 
 * @author Branislav Stojkovic
 */
public class HTMLTextArea extends JTextArea {

  private static final long serialVersionUID = 1L;

  public HTMLTextArea(String text) {
    super(text.replace("<br>", "\n"));
  }

  public HTMLTextArea() {
    super();
  }

  @Override
  public String getText() {
    return super.getText().replace("\n", "<br>");
  }

  @Override
  public void setText(String t) {
    super.setText(t.replace("<br>", "\n"));
  }
}
