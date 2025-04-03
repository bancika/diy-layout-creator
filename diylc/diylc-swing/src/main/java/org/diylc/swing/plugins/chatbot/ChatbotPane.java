package org.diylc.swing.plugins.chatbot;

import org.apache.log4j.Logger;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.plugins.chatbot.presenter.ChatbotPresenter;
import org.diylc.plugins.cloud.presenter.CloudPresenter;
import org.diylc.swing.ISwingUI;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Random;

public class ChatbotPane extends JPanel {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(ChatbotPane.class);
  
  public static Font DEFAULT_FONT = new Font("Square721 BT", Font.PLAIN, 12);
  private static final Font MONOSPACED_FONT = new Font("Monospaced", Font.PLAIN, 12);
  private static final Color TERMINAL_BG = new Color(40, 40, 40);
  private static final Color TERMINAL_FG = new Color(200, 200, 200);
  private static final Color PROMPT_BG = new Color(60, 60, 60);  // Slightly brighter than TERMINAL_BG

  private ISwingUI swingUI;
  private IPlugInPort plugInPort;
  private ChatbotPresenter chatbotPresenter;

  private JTextArea promptArea;
  private JEditorPane chatEditorPane;
  private JButton askButton;
  private JButton clearButton;

  public ChatbotPane(ISwingUI swingUI, IPlugInPort plugInPort) {
    super();
    this.swingUI = swingUI;
    this.plugInPort = plugInPort;
    this.chatbotPresenter = new ChatbotPresenter(plugInPort);
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
    Dimension promptSize = new Dimension(200, desiredHeight);
    getPromptArea().setMinimumSize(promptSize);
    getPromptArea().setPreferredSize(promptSize);
    promptScrollPane.setMinimumSize(new Dimension(200, desiredHeight + 10));
    promptScrollPane.setPreferredSize(new Dimension(200, desiredHeight + 10));
    
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

    setPreferredSize(new Dimension(360, 400));
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
      
      // Add Enter key handling
      promptArea.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent e) {
          if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER && !e.isShiftDown()) {
            e.consume(); // Prevent the newline from being added
            if (getAskButton().isEnabled()) {
              getAskButton().doClick();
            }
          }
        }
      });
      
      // Calculate height based on font metrics
      int lineHeight = promptArea.getFontMetrics(promptArea.getFont()).getHeight();
      int desiredHeight = lineHeight * 3 + 10; // 3 lines + padding
      promptArea.setMinimumSize(new Dimension(200, desiredHeight));
      promptArea.setPreferredSize(new Dimension(200, desiredHeight));

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
          ".user { color: #98C379; }" +      // Softer green that's easier on the eyes
          ".assistant { color: #61AFEF; }" +  // Lighter, more vibrant blue
          ".system { color: #E5C07B; }" +     // Warmer, muted yellow
          ".temporary { color: #555555; }" +
          "</style>",
          MONOSPACED_FONT.getFamily(),
          MONOSPACED_FONT.getSize(),
          TERMINAL_FG.getRed(), TERMINAL_FG.getGreen(), TERMINAL_FG.getBlue(),
          TERMINAL_BG.getRed(), TERMINAL_BG.getGreen(), TERMINAL_BG.getBlue());

      String sampleChat = chatbotPresenter.getInitialChatContents();

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
        appendSection(ChatbotPresenter.USER, prompt);
        appendSection(ChatbotPresenter.TEMPORARY, "Waiting for the response...");
        getPromptArea().setText(null);


        getAskButton().setEnabled(false);
        getClearButton().setEnabled(false);
        swingUI.executeBackgroundTask(new ITask<String>() {

          @Override
          public String doInBackground() throws Exception {
            return chatbotPresenter.promptChatbot(prompt);
          }

          @Override
          public void failed(Exception e) {
            appendSection(ChatbotPresenter.SYSTEM, "Failed to retrieve the response from the server. Error: " + e.getMessage());
            LOG.error("Failed to retrieve the response from the server", e);
            getClearButton().setEnabled(true);
          }

          @Override
          public void complete(String result) {
            appendSection(ChatbotPresenter.ASSISTANT, result);
            getClearButton().setEnabled(true);
          }
        }, false);
      });
    }
    return askButton;
  }

  private void appendSection(String style, String insertText) {
    String text = getChatEditorPane().getText();
    // remove any temporary divs
    String regex = "(?s)<div\\s+class=[\"']temporary[\"'][^>]*>.*?</div>\\s*<br>\\s*";
    text = text.replaceAll(regex, "");
    // add a new div
    int bodyCloseIndex = text.indexOf("</body>");
    text = text.substring(0, bodyCloseIndex) +
        "\n<div class='" + style + "'>" + insertText + "</div><br>" +
        text.substring(bodyCloseIndex);
    getChatEditorPane().setText(text);
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
