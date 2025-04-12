package org.diylc.swing.plugins.chatbot;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.lang.LangUtil;
import org.diylc.plugins.chatbot.model.ChatMessageEntity;
import org.diylc.plugins.chatbot.model.SubscriptionEntity;
import org.diylc.plugins.chatbot.service.ChatbotService;
import org.diylc.plugins.cloud.service.NotLoggedInException;
import org.diylc.swing.ISwingUI;
import org.diylc.utils.FileUtils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ChatbotPane extends JPanel {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(ChatbotPane.class);
  public static final String ME = "Me: ";
  public static final String AI_ASSISTANT = "AI Assistant: ";
  public static final String FREE_TIER = "Free";
  public static final String GET_PREMIUM_URL = "www.diy-fever.com/get-premium";

  public static Font DEFAULT_FONT = new Font("Square721 BT", Font.PLAIN, 12);
  private static final Font MONOSPACED_FONT = new Font("Monospaced", Font.PLAIN, 12);
  private static final Color TERMINAL_BG = new Color(40, 40, 40);
  private static final Color TERMINAL_FG = new Color(200, 200, 200);
  public static final String HTML_STYLE = String.format(
      "<style>" + "body { font-family: %s; font-size: %dpt; color: rgb(%d,%d,%d); background-color: rgb(%d,%d,%d); padding: 0px; margin: 0; }" + "pre { white-space: pre-wrap; margin: 0; }" + ".user { color: #98C379; }" +      // Softer green that's easier on the eyes
          ".assistant { color: #61AFEF; }" +  // Lighter, more vibrant blue
          ".system { color: #E5C07B; }" +     // Warmer, muted yellow
          ".temporary { color: #555555; }" + 
          "a { color: #C678DD; text-decoration: none; }" +  // Purple link color
          "a:hover { color: #E06C75; text-decoration: underline; }" +  // Red hover color
          "</style>", MONOSPACED_FONT.getFamily(),
      MONOSPACED_FONT.getSize(), TERMINAL_FG.getRed(), TERMINAL_FG.getGreen(),
      TERMINAL_FG.getBlue(), TERMINAL_BG.getRed(), TERMINAL_BG.getGreen(), TERMINAL_BG.getBlue());
  private static final Color PROMPT_BG = new Color(60, 60, 60);  // Slightly brighter than TERMINAL_BG

  private ISwingUI swingUI;
  private IPlugInPort plugInPort;
  private ChatbotService chatbotService;

  private JTextArea promptArea;
  private JEditorPane chatEditorPane;
  private JButton askButton;
  private JButton clearButton;
  private JButton premiumButton;

  private boolean loggedIn = false;
  private String projectFileName = null;

  public ChatbotPane(ISwingUI swingUI, IPlugInPort plugInPort) {
    super();
    this.swingUI = swingUI;
    this.plugInPort = plugInPort;
    this.chatbotService = new ChatbotService(plugInPort);
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

    // Button panel for Clear, Premium, and Ask buttons
    JPanel buttonPanel = new JPanel(new BorderLayout(10, 0));  // 10px horizontal gap between components
    buttonPanel.setBackground(TERMINAL_BG);
    
    // Left panel for Clear button
    JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    leftPanel.setBackground(TERMINAL_BG);
    leftPanel.add(getClearButton());
    
    // Center panel for Premium button
    JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    centerPanel.setBackground(TERMINAL_BG);
    centerPanel.add(getPremiumButton());
    
    // Right panel for Ask button
    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    rightPanel.setBackground(TERMINAL_BG);
    rightPanel.add(getAskButton());
    
    // Add all panels to the main button panel
    buttonPanel.add(leftPanel, BorderLayout.WEST);
    buttonPanel.add(centerPanel, BorderLayout.CENTER);
    buttonPanel.add(rightPanel, BorderLayout.EAST);

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
      final String placeholder = "Ask me anything";
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

  public void refreshChat(SubscriptionEntity subscriptionInfo) {
    boolean currentLoggedIn = plugInPort.getCloudService().isLoggedIn();
    String currentProjectFileName = FileUtils.extractFileName(plugInPort.getCurrentFileName());

    if (this.loggedIn == currentLoggedIn && Objects.equals(currentProjectFileName, this.projectFileName))
      return;

    this.loggedIn = currentLoggedIn;
    this.projectFileName = currentProjectFileName;

    clearChat();
    if (!currentLoggedIn) {
      appendSection(ChatbotService.SYSTEM, "Please log into your cloud account to use the AI assistant");
      getAskButton().setEnabled(false);
      getClearButton().setEnabled(false);
      getPremiumButton().setVisible(false);
    } else {
      if (subscriptionInfo != null) {
        getPremiumButton().setVisible(FREE_TIER.equals(subscriptionInfo.getTier()));
        String subscriptionInfoText = "Your are currently subscribed to the '" + subscriptionInfo.getTier() + "' tier, expiring on " + subscriptionInfo.getEndDate() + ", with " + subscriptionInfo.getRemainingCredits() + " credits remaining.";

        if (FREE_TIER.equals(subscriptionInfo.getTier())) {
          subscriptionInfoText +=
              " To subscribe to one of the premium tiers, unlock advanced AI models and get more credits, visit <a href='http://" + GET_PREMIUM_URL + "'>" + GET_PREMIUM_URL + "</a> and become a Patreon supporter. " +
                  "All the details about the AI Assistant limits and other benefits will be specified there. " +
                  "If you are already a Patreon supporter, make sure to link your account with the DIYLC Cloud account from the 'Cloud' menu.";
        } else {
          subscriptionInfoText += " Thank you for supporting further development of DIYLC!";
        }
        appendSection(ChatbotService.SYSTEM, subscriptionInfoText);
      }
      fetchChatHistory();
      getAskButton().setEnabled(true);
      getClearButton().setEnabled(true);
    }

    // Scroll to the end of the chat pane
    SwingUtilities.invokeLater(() -> {
      JScrollPane scrollPane = (JScrollPane) getChatEditorPane().getParent().getParent();
      JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
      verticalScrollBar.setValue(verticalScrollBar.getMaximum());
    });
  }

  private void fetchChatHistory() {
    try {
      List<ChatMessageEntity> chatHistory = plugInPort.getChatbotService().getChatHistory();
      chatHistory.forEach(message -> {
        appendSection(ChatbotService.USER, ME + message.getPrompt());
        appendSection(ChatbotService.ASSISTANT, AI_ASSISTANT + message.getResponse());
      });
    } catch (NotLoggedInException e) {
      appendSection(ChatbotService.SYSTEM, "Failed to retrieve chat history.");
    }
  }

  public JEditorPane getChatEditorPane() {
    if (chatEditorPane == null) {
      chatEditorPane = new JEditorPane("text/html", "<html></html>");
      chatEditorPane.setEditable(false);  // Make it read-only
      chatEditorPane.setFont(MONOSPACED_FONT);
      chatEditorPane.setBackground(TERMINAL_BG);
      chatEditorPane.setForeground(TERMINAL_FG);
      chatEditorPane.setCaretColor(TERMINAL_FG);  // Make the cursor visible on dark background

      chatEditorPane.setContentType("text/html");
      chatEditorPane.setText(HTML_STYLE + "<body></body>");

      chatEditorPane.addHyperlinkListener(new HyperlinkListener() {

        @Override
        public void hyperlinkUpdate(HyperlinkEvent hle) {
          if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
            System.out.println(hle.getURL());
            Desktop desktop = Desktop.getDesktop();
            try {
              desktop.browse(hle.getURL().toURI());
            } catch (Exception ex) {
              LOG.error("Could not open link: " + hle.getURL(), ex);
            }
          }
        }
      });

      refreshChat(null);
    }
    return chatEditorPane;
  }

  private void clearChat() {
    String sampleChat = getInitialChatContents();

    chatEditorPane.setContentType("text/html");
    chatEditorPane.setText(HTML_STYLE + "<body>" + sampleChat + "</body>");
  }

  private String getInitialChatContents() {
    Random r = new Random();

    String chatHtml =
        "<div class='system'>" + ChatbotService.WELCOME_MESSAGE + "</div><br><br>\n" +
            "<div class='assistant'>I'm here to help you design, build, and troubleshoot your electronics projects using DIY Layout Creator (DIYLC). You can ask me questions about:</div><br>\n" +
            //            "<ul>\n" +
            "\n" +
            "- <b>Electronics theory or concepts:</b> <i>e.g.,</i> '<span class='user'>" +ChatbotService.electronicsQuestions[r.nextInt(
            ChatbotService.electronicsQuestions.length)] + "</span>'<br>\n" +
            "- <b>DIYLC features and usage:</b> <i>e.g.,</i> '<span class='user'>" + ChatbotService.diylcQuestions[r.nextInt(ChatbotService.diylcQuestions.length)] + "</span>'<br>\n" +
            "- <b>Your current circuit project:</b> <i>e.g.,</i> '<span class='user'>" + ChatbotService.circuitQuestions[r.nextInt(ChatbotService.circuitQuestions.length)] + "</span>'<br>\n" +
            //            "</ul><br>\n" +
            "<br>\n" +
            "<div class='assistant'>Feel free to ask your own question or use one of these examples to get started!</div><br>\n";

    return chatHtml;
  }

  public JButton getAskButton() {
    if (askButton == null) {
      askButton = new JButton(LangUtil.translate("Send"));
      askButton.setBackground(TERMINAL_BG);
      askButton.setForeground(TERMINAL_FG);
      askButton.setFocusPainted(false);
      askButton.setBorderPainted(true);
      askButton.addActionListener(e -> {
        String prompt = getPromptArea().getText();
        appendSection(ChatbotService.USER, ME + prompt);

        String lang = ConfigurationManager.getInstance()
            .readString(IPlugInPort.LANGUAGE, IPlugInPort.LANGUAGE_DEFAULT);
        if (!IPlugInPort.LANGUAGE_DEFAULT.equals(lang)) {
          prompt += ". Respond in " + lang + " language.";
        }
        String finalPrompt = prompt;
        appendSection(ChatbotService.TEMPORARY, "Waiting for the response...");
        getPromptArea().setText(null);


        getAskButton().setEnabled(false);
        getClearButton().setEnabled(false);
        swingUI.executeBackgroundTask(new ITask<String>() {

          @Override
          public String doInBackground() throws Exception {
            return chatbotService.promptChatbot(finalPrompt);
          }

          @Override
          public void failed(Exception e) {
            appendSection(ChatbotService.SYSTEM, "Failed to retrieve the response from the server.");
            LOG.error("Failed to retrieve the response from the server", e);
            getClearButton().setEnabled(true);
          }

          @Override
          public void complete(String result) {
            appendSection(ChatbotService.ASSISTANT, AI_ASSISTANT + result);
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
      clearButton = new JButton(LangUtil.translate("Clear"));
      clearButton.setBackground(TERMINAL_BG);
      clearButton.setForeground(TERMINAL_FG);
      clearButton.setFocusPainted(false);
      clearButton.setBorderPainted(true);
      clearButton.addActionListener(e -> {

        swingUI.executeBackgroundTask(new ITask<Void>() {

          @Override
          public Void doInBackground() throws Exception {
            chatbotService.deleteChatHistory();
            return null;
          }

          @Override
          public void failed(Exception e) {
            appendSection(ChatbotService.SYSTEM, "Failed to delete chat history from the server.");
            LOG.error("Failed to delete chat history from the server", e);
            getClearButton().setEnabled(true);
          }

          @Override
          public void complete(Void result) {
            clearChat();
          }
        }, false);
      });
    }
    return clearButton;
  }

  public JButton getPremiumButton() {
    if (premiumButton == null) {
      premiumButton = new JButton(LangUtil.translate("Get Premium"));
      premiumButton.setBackground(TERMINAL_BG);
      premiumButton.setForeground(TERMINAL_FG);
      premiumButton.setFocusPainted(false);
      premiumButton.setBorderPainted(true);
      premiumButton.setVisible(false);
      premiumButton.addActionListener(e -> {

        try {
          java.awt.Desktop.getDesktop().browse(new java.net.URI("http://diy-fever.com/get-premium"));
        } catch (Exception ex) {
          LOG.error("Failed to open premium subscription page", ex);
          swingUI.showMessage("Failed to open premium subscription page. Please visit http://diy-fever.com/get-premium manually.", "Error",
              ISwingUI.ERROR_MESSAGE);
        }
      });
    }
    return premiumButton;
  }
}
