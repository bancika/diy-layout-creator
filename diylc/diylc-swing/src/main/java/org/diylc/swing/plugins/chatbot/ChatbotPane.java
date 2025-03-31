package org.diylc.swing.plugins.chatbot;

import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.core.IView;
import org.diylc.plugins.chatbot.presenter.ChatbotPresenter;
import org.diylc.plugins.cloud.presenter.CloudPresenter;
import org.diylc.plugins.cloud.presenter.NotLoggedInException;
import org.diylc.swing.ISwingUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ChatbotPane extends JPanel {

  private static final long serialVersionUID = 1L;

  public static Font DEFAULT_FONT = new Font("Square721 BT", Font.PLAIN, 12);
  private static final Font MONOSPACED_FONT = new Font("Monospaced", Font.PLAIN, 12);
  private static final Color TERMINAL_BG = new Color(40, 40, 40);
  private static final Color TERMINAL_FG = new Color(200, 200, 200);
  private static final Color PROMPT_BG = new Color(60, 60, 60);  // Slightly brighter than TERMINAL_BG

  private ISwingUI swingUI;
  private IPlugInPort plugInPort;

  private JLabel titleLabel;
  private JTextArea promptArea;
  private JScrollPane chatScrollPane;
  private JEditorPane chatEditorPane;
  private JButton askButton;
  private JButton clearButton;

  public ChatbotPane(ISwingUI swingUI, IPlugInPort plugInPort) {
    super();
    this.swingUI = swingUI;
    this.plugInPort = plugInPort;
    setName("AI Assistant");

    // Set panel background color
    setBackground(TERMINAL_BG);

    setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(5, 5, 5, 5);

    // Chat scroll pane takes most space
    JScrollPane chatScrollPane = new JScrollPane(getChatEditorPane(),
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    chatScrollPane.setBackground(TERMINAL_BG);
    chatScrollPane.getViewport().setBackground(TERMINAL_BG);
    chatScrollPane.setBorder(BorderFactory.createLineBorder(TERMINAL_BG));
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.gridwidth = 2;  // Span both columns
    add(chatScrollPane, gbc);

    // Prompt label
    JLabel promptLabel = new JLabel("Prompt:");
    promptLabel.setForeground(TERMINAL_FG);
    promptLabel.setFont(DEFAULT_FONT);
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;
    gbc.gridwidth = 1;
//    add(promptLabel, gbc);

    // Prompt scroll pane
    JScrollPane promptScrollPane = new JScrollPane(getPromptArea(),
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    promptScrollPane.setBackground(TERMINAL_BG);
    promptScrollPane.getViewport().setBackground(TERMINAL_BG);
    promptScrollPane.setBorder(BorderFactory.createLineBorder(TERMINAL_BG));
    
    // Calculate height based on font size and lines
    int lineHeight = getPromptArea().getFontMetrics(getPromptArea().getFont()).getHeight();
    int desiredHeight = lineHeight * 3 + 10; // 3 lines + padding
    
    // Set minimum and preferred size for both text area and scroll pane
    Dimension promptSize = new Dimension(150, desiredHeight);
    getPromptArea().setMinimumSize(promptSize);
    getPromptArea().setPreferredSize(promptSize);
    promptScrollPane.setMinimumSize(new Dimension(150, desiredHeight + 10));
    promptScrollPane.setPreferredSize(new Dimension(150, desiredHeight + 10));
    
    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
    gbc.gridwidth = 2;  // Span both columns
    add(promptScrollPane, gbc);

    // Button panel for Clear and Ask buttons
    JPanel buttonPanel = new JPanel(new BorderLayout(5, 0));
    buttonPanel.setBackground(TERMINAL_BG);
    buttonPanel.add(getClearButton(), BorderLayout.WEST);
    buttonPanel.add(getAskButton(), BorderLayout.EAST);

    gbc.gridx = 0;  // Start from first column
    gbc.gridy = 4;
    gbc.weightx = 1.0;  // Allow horizontal stretching
    gbc.weighty = 0.0;
    gbc.gridwidth = 2;  // Span both columns
    gbc.fill = GridBagConstraints.HORIZONTAL;  // Fill horizontally
    add(buttonPanel, gbc);

    setPreferredSize(new Dimension(300, 400));
  }

  public JTextArea getPromptArea() {
    if (promptArea == null) {
      promptArea = new JTextArea();
      promptArea.setLineWrap(true);
      promptArea.setWrapStyleWord(true);
      promptArea.setRows(3);  // Set to 3 lines height
      promptArea.setFont(DEFAULT_FONT);
      promptArea.setBackground(PROMPT_BG);  // Use brighter background
      promptArea.setForeground(TERMINAL_FG);
      promptArea.setCaretColor(TERMINAL_FG);
      
      // Calculate height based on font metrics
      int lineHeight = promptArea.getFontMetrics(promptArea.getFont()).getHeight();
      int desiredHeight = lineHeight * 3 + 10; // 3 lines + padding
      promptArea.setMinimumSize(new Dimension(150, desiredHeight));
      promptArea.setPreferredSize(new Dimension(150, desiredHeight));

      // Add placeholder text behavior
      final String placeholder = "Type your question here...";
      promptArea.setText(placeholder);
      promptArea.setForeground(new Color(128, 128, 128));  // Dimmer text for placeholder

      // Initially disable Ask button since we start with placeholder
      getAskButton().setEnabled(false);

      promptArea.addFocusListener(new java.awt.event.FocusListener() {
        @Override
        public void focusGained(java.awt.event.FocusEvent e) {
          if (promptArea.getText().equals(placeholder)) {
            promptArea.setText("");
            promptArea.setForeground(TERMINAL_FG);
            getAskButton().setEnabled(false);  // Disable when clearing placeholder
          }
        }

        @Override
        public void focusLost(java.awt.event.FocusEvent e) {
          if (promptArea.getText().isEmpty()) {
            promptArea.setText(placeholder);
            promptArea.setForeground(new Color(128, 128, 128));
            getAskButton().setEnabled(false);  // Disable when empty/placeholder
          }
        }
      });

      // Add document listener to monitor text changes
      promptArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        private void updateAskButton() {
          String text = promptArea.getText();
          boolean hasContent = !text.isEmpty() && !text.equals(placeholder);
          getAskButton().setEnabled(hasContent);
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
          updateAskButton();
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
          updateAskButton();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
          updateAskButton();
        }
      });
    }
    return promptArea;
  }

  public JEditorPane getChatEditorPane() {
    if (chatEditorPane == null) {
      chatEditorPane = new JEditorPane("text/html", "<html></html>");
      chatEditorPane.setEditable(false);  // Make it read-only
      chatEditorPane.setFont(MONOSPACED_FONT);
      chatEditorPane.setBackground(TERMINAL_BG);
      chatEditorPane.setForeground(TERMINAL_FG);
      chatEditorPane.setCaretColor(TERMINAL_FG);  // Make the cursor visible on dark background
      
      // Set up HTML styling
      String htmlStyle = String.format(
          "<style>" +
          "body { font-family: %s; font-size: %dpt; color: rgb(%d,%d,%d); background-color: rgb(%d,%d,%d); padding: 0px; margin: 0; }" +
          "pre { white-space: pre-wrap; margin: 0; }" +
          ".user { color: #4CAF50; }" +
          ".assistant { color: #2196F3; }" +
          ".system { color: #FF9800; }" +
          "</style>",
          MONOSPACED_FONT.getFamily(),
          MONOSPACED_FONT.getSize(),
          TERMINAL_FG.getRed(), TERMINAL_FG.getGreen(), TERMINAL_FG.getBlue(),
          TERMINAL_BG.getRed(), TERMINAL_BG.getGreen(), TERMINAL_BG.getBlue());
      
      // Sample chat history
      String sampleChat = 
          "<div class='system'>Welcome to DIYLC AI Assistant! How can I help you today?</div>\n\n" +
          "<div class='user'>How do I create a simple circuit?</div>\n\n" +
          "<div class='assistant'>To create a simple circuit:\n" +
          "1. Drag components from the component panel\n" +
          "2. Connect them by dragging from one terminal to another\n" +
          "3. Use the grid to align components neatly</div>\n\n" +
          "<div class='user'>What's the best way to organize components?</div>\n\n" +
          "<div class='assistant'>For best organization:\n" +
          "- Group related components together\n" +
          "- Use consistent spacing\n" +
          "- Label important connections\n" +
          "- Consider signal flow direction</div>";
      
      chatEditorPane.setContentType("text/html");
      chatEditorPane.setText(htmlStyle + "<body><pre>" + sampleChat + "</pre></body>");
    }
    return chatEditorPane;
  }

  public JButton getAskButton() {
    if (askButton == null) {
      askButton = new JButton("Ask");
      askButton.setBackground(TERMINAL_BG);
      askButton.setForeground(TERMINAL_FG);
      askButton.setFocusPainted(false);
      askButton.setBorderPainted(true);
      askButton.addActionListener(e -> {
        final String prompt = getPromptArea().getText();
        appendSection("user", prompt);
        getPromptArea().setText(null);
        String currentFile = plugInPort.getCurrentFileName();
        String fileName = extractFileName(currentFile);

        getAskButton().setEnabled(false);
        getClearButton().setEnabled(false);
        swingUI.executeBackgroundTask(new ITask<String>() {

          @Override
          public String doInBackground() throws Exception {
            return ChatbotPresenter.Instance.promptChatbot(fileName, "", prompt);
          }

          @Override
          public void failed(Exception e) {
            swingUI.showMessage("Failed to ask AI Assistant. Error: " + e.getMessage(), "AI Assistant Error",
                IView.ERROR_MESSAGE);
            getClearButton().setEnabled(true);
          }

          @Override
          public void complete(String result) {
            appendSection("assistant", result);
            getClearButton().setEnabled(true);
          }
        }, true);
      });
    }
    return askButton;
  }

  private void appendSection(String style, String insertText) {
    String text = getChatEditorPane().getText();
    int bodyCloseIndex = text.indexOf("</body>");
    text = text.substring(0, bodyCloseIndex) +
        "\n<div class='" + style + "'>" + insertText + "</div>\n" +
        text.substring(bodyCloseIndex);
    getChatEditorPane().setText(text);
  }

  /**
   * Extracts the base file name without extension from a full path.
   * For example: "/path/to/myfile.diy" returns "myfile"
   */
  private String extractFileName(String path) {
    if (path == null || path.isEmpty()) {
      return "Untitled";
    }
    // Get the last part after the last separator
    String fileName = path.substring(path.lastIndexOf(File.separator) + 1);
    // Remove the extension if present
    int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex > 0) {
      fileName = fileName.substring(0, dotIndex);
    }
    return fileName;
  }

  public JButton getClearButton() {
    if (clearButton == null) {
      clearButton = new JButton("Clear");
      clearButton.setBackground(TERMINAL_BG);
      clearButton.setForeground(TERMINAL_FG);
      clearButton.setFocusPainted(false);
      clearButton.setBorderPainted(true);
    }
    return clearButton;
  }
}
