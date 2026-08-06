package org.diylc.swing.actions.analyze;

import org.diylc.common.IPlugInPort;
import org.diylc.swing.ISwingUI;
import org.diylc.swingframework.TextDialog;
import org.diylc.utils.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;

public class AiAnalyzeAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private static final String[] WAITING_MESSAGES = {
      "The AI is analyzing your circuit. This might take a little while...",
      "Please wait while the AI processes your schematic...",
      "AI analysis in progress. Grab a coffee, this could take a minute...",
      "The AI is examining your components. This will take some time...",
      "Sending your circuit to the AI. Please be patient...",
      "The AI is working on your layout. This may take a moment...",
      "Hold tight! The AI is taking a deep dive into your circuit...",
      "AI review is underway. This process takes a little while...",
      "The AI is evaluating your design. Thanks for your patience...",
      "Analyzing your circuit with AI. This is a complex task and will take some time...",
      "The AI is currently inspecting your schematic. This could take a while...",
      "Please hold while the AI computes your circuit analysis...",
      "AI processing started. This operation may take several seconds...",
      "The AI is reviewing your layout. This might take a bit of time...",
      "Circuit analysis by AI in progress. Please wait a moment...",
      "The AI is carefully examining your design. This will take a while...",
      "Running AI diagnostics on your circuit. Please be patient...",
      "The AI is generating your analysis. This process can take some time...",
      "Your schematic is being analyzed by the AI. This may take a little while...",
      "AI is hard at work on your circuit. Thanks for waiting...",
      "The AI is analyzing your circuit. Please hold while it makes sure no magic smoke escapes...",
      "Sending your schematic to the AI. This might take a while, AI needs its coffee too...",
      "The AI is working on your layout. It takes time to translate circuits into binary poetry...",
      "Hold tight! The AI is calculating the optimal path of least resistance...",
      "AI review is underway. This could take a minute, depending on how tangled your wires are..."
  };

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;

  public AiAnalyzeAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    super();
    this.plugInPort = plugInPort;
    this.swingUI = swingUI;
    putValue(AbstractAction.NAME, "AI Circuit Analyzer");
    putValue(AbstractAction.SMALL_ICON, IconLoader.Brain.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    String message = WAITING_MESSAGES[(int) (Math.random() * WAITING_MESSAGES.length)];
    JOptionPane optionPane = new JOptionPane(message,
        JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, IconLoader.BrainBig.getIcon(),
        new Object[] {"Cancel"});
    JDialog dialog = optionPane.createDialog(swingUI.getOwnerFrame(), "AI Circuit Analyzer");
    dialog.setModal(true);

    SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {

      @Override
      protected String doInBackground() throws Exception {
        return plugInPort.getChatbotService().analyzeCircuit();
      }

      @Override
      protected void done() {
        dialog.dispose();
        if (isCancelled()) {
          return;
        }
        try {
          String res = get();
          if (res == null || res.isEmpty()) {
            swingUI.showMessage("The generated analysis is empty, nothing to show.",
                "AI Circuit Analyzer", ISwingUI.INFORMATION_MESSAGE);
            return;
          }
          new TextDialog(swingUI.getOwnerFrame().getRootPane(), res, "AI Circuit Analyzer",
              new Dimension(800, 600)).setVisible(true);
        } catch (Exception ex) {
          String message =
              Optional.ofNullable(ex.getCause()).map(Throwable::getMessage).orElse(ex.getMessage());
          swingUI.showMessage(message, "AI Circuit Analyzer", ISwingUI.ERROR_MESSAGE);
        }
      }
    };

    worker.execute();
    dialog.setVisible(true);

    if (!worker.isDone()) {
      worker.cancel(true);
    }
  }
}
