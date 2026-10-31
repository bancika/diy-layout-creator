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
package org.diylc.swing.plugins.chatbot;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.lang.LangUtil;
import org.diylc.plugins.chatbot.model.ChatMessageEntity;
import org.diylc.plugins.chatbot.model.ChatbotResponse;
import org.diylc.plugins.chatbot.model.AiEditScript;
import org.diylc.plugins.chatbot.model.AiEditOperation;
import org.diylc.plugins.chatbot.service.AiEditScriptEditor;
import org.diylc.plugins.chatbot.model.SubscriptionEntity;
import org.diylc.plugins.chatbot.service.ChatbotService;
import org.diylc.plugins.chatbot.service.JsonToHtmlConverter;
import org.diylc.plugins.cloud.service.NotLoggedInException;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.plugins.file.FileFilterEnum;
import org.diylc.utils.FileUtils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.diylc.plugins.chatbot.service.JsonToHtmlConverter.escapeHtml;

public class ChatbotPane extends JPanel {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(ChatbotPane.class);
  public static final String ME = LangUtil.translate("Me") + ": ";
  public static final String AI_ASSISTANT = LangUtil.translate("AI Assistant") + ": ";
  public static final String FREE_TIER = "Free";
  public static final String GET_PREMIUM_URL = "www.diy-fever.com/get-premium";
  private static final String APPLY_PROPOSAL_LINK = "diy://applyEditScript/";
  private static final String ASK_AGAIN_LINK = "diy://askAgain/";
  
  private static final String[] WAITING_MESSAGES = {
      "Consulting the digital oracle...",
      "Firing up the neural networks...",
      "Translating human intent into robot action...",
      "Asking the AI brain...",
      "Crunching some ones and zeros...",
      "The AI is thinking... smoke may appear!",
      "Our robot overlords are considering your prompt...",
      "The AI is processing your request. Give it a moment...",
      "Synthesizing a response for you...",
      "Waking up the AI hamster...",
      "Hold tight! Generating a response...",
      "The AI is formulating a plan...",
      "Bleep bloop... processing...",
      "Reticulating splines...",
      "Applying machine learning magic...",
      "Calculating the meaning of life, the universe, and everything...",
      "Polishing the silicon chips...",
      "Downloading more RAM...",
      "Training a tiny neural net just for this...",
      "Beep boop beep... generating...",
      "Asking ChatGPT's older, wiser cousin...",
      "Navigating the quantum realm...",
      "Spinning up the GPU fans...",
      "Aligning the data crystals...",
      "Pondering the digital orb...",
      "Feeding your prompt to the algorithmic beast...",
      "Engaging the flux capacitor...",
      "Warming up the AI's logic circuits...",
      "Transmitting your request into the cyber-ether...",
      "The AI is busy reading a textbook to answer this..."
  };

  public static Font DEFAULT_FONT = new Font("Square721 BT", Font.PLAIN, 12);
  private static final Font MONOSPACED_FONT = new Font("Monospaced", Font.PLAIN, 12);
  private static final Color TERMINAL_BG = new Color(40, 40, 40);
  private static final Color TERMINAL_FG = new Color(200, 200, 200);
  private static final Color BUTTON_BG = new Color(70, 70, 70);  // Brighter background for buttons
  private static final Color BUTTON_FG = new Color(240, 240, 240);  // Bright text for contrast
  private static final Color BUTTON_BORDER = new Color(100, 100, 100);  // Subtle border
  private static final Color BUTTON_HOVER = new Color(90, 90, 90);  // Hover state color
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
  private JButton exportButton;
  
  private final Map<String, Proposal> proposals = new HashMap<>();
  private int proposalCount = 0;

  private boolean loggedIn = false;
  private String projectFileName = null;
  private SubscriptionEntity subscriptionInfo;

  public ChatbotPane(ISwingUI swingUI, IPlugInPort plugInPort) {
    super();
    this.swingUI = swingUI;
    this.plugInPort = plugInPort;
    this.chatbotService = new ChatbotService(plugInPort);
    setName("AI Assistant");

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        e.consume();
      }

      @Override
      public void mousePressed(MouseEvent e) {
        e.consume();
      }
    });

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
    
    // Set minimum and preferred size for scroll pane
    promptScrollPane.setMinimumSize(new Dimension(200, desiredHeight));
    promptScrollPane.setPreferredSize(new Dimension(200, desiredHeight));
    
    // Enable vertical scrolling
    getPromptArea().setLineWrap(true);
    getPromptArea().setWrapStyleWord(true);
    getPromptArea().setRows(3);  // Set initial rows
    promptScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    
    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
    gbc.gridwidth = 2;  // Span both columns
    add(promptScrollPane, gbc);

    // Button panel for Clear, Premium, and Ask buttons
    JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.setBackground(TERMINAL_BG);
    
    // Left panel for secondary actions
    JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    leftPanel.setBackground(TERMINAL_BG);
    leftPanel.add(getClearButton());
    leftPanel.add(Box.createHorizontalStrut(10));
    leftPanel.add(getExportButton());
    
    // Right panel for primary action
    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    rightPanel.setBackground(TERMINAL_BG);
    rightPanel.add(getAskButton());
    
    // Add panels to the main button panel
    buttonPanel.add(leftPanel, BorderLayout.WEST);
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
          if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            if (e.isShiftDown()) {
              // Insert newline for Shift+Enter
              promptArea.insert("\n", promptArea.getCaretPosition());
            } else {
              e.consume(); // Always consume Enter to prevent multiple newlines
              if (getAskButton().isEnabled()) {
                getAskButton().doClick();
              }
            }
          }
        }
      });

      // Add placeholder text behavior
      final String placeholder = LangUtil.translate("Ask me anything");
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
    if (subscriptionInfo != null) {
      this.subscriptionInfo = subscriptionInfo;
    }

    if (this.loggedIn == currentLoggedIn && Objects.equals(currentProjectFileName, this.projectFileName))
      return;

    this.loggedIn = currentLoggedIn;
    this.projectFileName = currentProjectFileName;

    clearChat();
    if (!currentLoggedIn) {
      appendSection(ChatbotService.SYSTEM,
          LangUtil.translate("Please log into your cloud account to use the AI assistant"));
      getAskButton().setEnabled(false);
      getClearButton().setEnabled(false);
    } else {
      swingUI.executeBackgroundTask(new ITask<Void>() {
        @Override
        public Void doInBackground() throws Exception {
          fetchChatHistory();
          return null;
        }

        @Override
        public void failed(Exception e) {
        }

        @Override
        public void complete(Void result) {
          if (ChatbotPane.this.subscriptionInfo != null) {
            String subscriptionInfoText = String.format(LangUtil.translate(
                    "You are currently subscribed to the '%s' tier, expiring on %s, with %s credits remaining."),
                ChatbotPane.this.subscriptionInfo.getTier(),
                ChatbotPane.this.subscriptionInfo.getEndDate(),
                ChatbotPane.this.subscriptionInfo.getRemainingCredits());

            if (FREE_TIER.equals(ChatbotPane.this.subscriptionInfo.getTier())) {
              String premiumLink =
                  "<a href='http://" + GET_PREMIUM_URL + "'>" + GET_PREMIUM_URL + "</a>";
              subscriptionInfoText += " " + String.format(LangUtil.translate(
                  "To subscribe to one of the premium tiers, unlock advanced AI models and get more credits, visit %s and become a Patreon supporter."),
                  premiumLink)
                  + " " + LangUtil.translate(
                      "All the details about the AI Assistant limits and other benefits will be specified there.")
                  + " " + LangUtil.translate(
                      "If you are already a Patreon supporter, make sure to link your account with the DIYLC Cloud account from the 'Cloud' menu.");
            } else {
              subscriptionInfoText +=
                  " " + LangUtil.translate("Thank you for supporting further development of DIYLC!");
            }
            appendSection(ChatbotService.SYSTEM, subscriptionInfoText);
          }
          getAskButton().setEnabled(true);
          getClearButton().setEnabled(true);
        }
      }, false);
    }

    // Scroll to the end of the chat pane. Do not drive the vertical scrollbar directly:
    // setValue triggers BasicScrollPaneUI layout while the HTML FlowView can still have a
    // null viewBuffer (NPE in FlowView$FlowStrategy.layoutRow), e.g. when the chatbot
    // panel is not the active tab or not yet fully laid out.
    SwingUtilities.invokeLater(() -> {
      JEditorPane editor = getChatEditorPane();
      if (editor.getParent() == null || !editor.isDisplayable()) {
        return;
      }
      try {
        int len = editor.getDocument().getLength();
        if (len >= 0) {
          editor.setCaretPosition(len);
        }
      } catch (RuntimeException ex) {
        LOG.debug("Could not scroll chat view to end", ex);
      }
    });
  }

  private void fetchChatHistory() {
    try {
      List<ChatMessageEntity> chatHistory = plugInPort.getChatbotService().getChatHistory();
      chatHistory.forEach(message -> {
        appendSection(ChatbotService.USER, ME + escapeHtml(message.getPrompt()));
        appendSection(ChatbotService.ASSISTANT, AI_ASSISTANT + message.getResponse());
      });
    } catch (NotLoggedInException e) {
      appendSection(ChatbotService.SYSTEM, LangUtil.translate("Failed to retrieve chat history."));
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
            try {
              String urlStr = hle.getURL() != null ? hle.getURL().toString() : hle.getDescription();
              if (urlStr != null && urlStr.startsWith(APPLY_PROPOSAL_LINK)) {
                applyProposal(urlStr.substring(APPLY_PROPOSAL_LINK.length()));
                return;
              }
              if (urlStr != null && urlStr.startsWith(ASK_AGAIN_LINK)) {
                askAgain(urlStr.substring(ASK_AGAIN_LINK.length()));
                return;
              }
              
              if (hle.getURL() != null) {
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(hle.getURL().toURI());
              }
            } catch (Exception ex) {
              LOG.error("Could not process link click", ex);
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
    proposals.clear();

    chatEditorPane.setContentType("text/html");
    chatEditorPane.setText(HTML_STYLE + "<body>" + sampleChat + "</body>");
  }

  private String getInitialChatContents() {
    Random r = new Random();

    String example = LangUtil.translate("e.g.,");
    String chatHtml =
        "<div class='system'>" + LangUtil.translate(ChatbotService.WELCOME_MESSAGE) + "</div><br><br>\n" +
            "<div class='assistant'>" + LangUtil.translate("I'm here to help you design, build, and troubleshoot your electronics projects using DIY Layout Creator (DIYLC). You can ask me questions about:") + "</div><br>\n" +
            //            "<ul>\n" +
            "\n" +
            "- <b>" + LangUtil.translate("Electronics theory or concepts:") + "</b> <i>" + example + "</i> '<span class='user'>" +ChatbotService.electronicsQuestions[r.nextInt(
            ChatbotService.electronicsQuestions.length)] + "</span>'<br>\n" +
            "- <b>" + LangUtil.translate("DIYLC features and usage:") + "</b> <i>" + example + "</i> '<span class='user'>" + ChatbotService.diylcQuestions[r.nextInt(ChatbotService.diylcQuestions.length)] + "</span>'<br>\n" +
            "- <b>" + LangUtil.translate("Your current circuit project:") + "</b> <i>" + example + "</i> '<span class='user'>" + ChatbotService.circuitQuestions[r.nextInt(ChatbotService.circuitQuestions.length)] + "</span>'<br>\n" +
            "- <b>" + LangUtil.translate("Editing your circuit:") + "</b> <i>" + example + "</i> '<span class='user'>" + ChatbotService.editQuestions[r.nextInt(ChatbotService.editQuestions.length)] + "</span>'<br>\n" +
            "- <b>" + LangUtil.translate("Generating new layouts:") + "</b> <i>" + example + "</i> '<span class='user'>" + ChatbotService.generationQuestions[r.nextInt(ChatbotService.generationQuestions.length)] + "</span>'<br>\n" +
            //            "</ul><br>\n" +
            "<br>\n" +
            "<div class='assistant'>" + LangUtil.translate("Feel free to ask your own question or use one of these examples to get started!") + "</div><br>\n";

    return chatHtml;
  }

  public JButton getAskButton() {
    if (askButton == null) {
      askButton = new JButton(LangUtil.translate("Send"));
      styleButton(askButton);
      askButton.addActionListener(e -> {
        String prompt = getPromptArea().getText();
        getPromptArea().setText(null);
        ask(prompt);
      });
    }
    return askButton;
  }

  private void ask(String prompt) {
    appendSection(ChatbotService.USER, ME + escapeHtml(prompt));

    String finalPrompt = prompt;
    String lang = ConfigurationManager.getInstance()
        .readString(IPlugInPort.LANGUAGE, IPlugInPort.LANGUAGE_DEFAULT);
    if (!IPlugInPort.LANGUAGE_DEFAULT.equals(lang)) {
      finalPrompt += ". Respond in " + lang + " language.";
    }
    String requestPrompt = finalPrompt;
    String waitingMessage = LangUtil.translate(
        WAITING_MESSAGES[new Random().nextInt(WAITING_MESSAGES.length)]);
    appendSection(ChatbotService.TEMPORARY, waitingMessage);

    getAskButton().setEnabled(false);
    getClearButton().setEnabled(false);
    swingUI.executeBackgroundTask(new ITask<ChatbotResponse>() {

      @Override
      public ChatbotResponse doInBackground() throws Exception {
        return chatbotService.promptChatbot(requestPrompt);
      }

      @Override
      public void failed(Exception e) {
        appendSection(ChatbotService.SYSTEM,
            LangUtil.translate("Failed to retrieve the response from the server."));
        LOG.error("Failed to retrieve the response from the server", e);
        getClearButton().setEnabled(true);
      }

      @Override
      public void complete(ChatbotResponse result) {
        AiEditScript editScript = result.getEditScript();
        if (editScript != null && editScript.getOperations() != null
            && !editScript.getOperations().isEmpty()) {
          String id = Integer.toString(++proposalCount);
          proposals.put(id, new Proposal(editScript, result.getProjectFingerprint(), prompt));
          appendSection(ChatbotService.ASSISTANT,
              renderProposal(id, result.getString(), editScript));
        } else if (result.getString() != null) {
          appendSection(ChatbotService.ASSISTANT, AI_ASSISTANT + result.getString());
        } else if (editScript != null && editScript.getExplanation() != null) {
          // an edit script with nothing to do is the model declining, and its explanation is
          // the only account of why
          appendSection(ChatbotService.ASSISTANT,
              AI_ASSISTANT + escapeHtml(editScript.getExplanation()));
        } else {
          appendSection(ChatbotService.SYSTEM,
              LangUtil.translate("Failed to retrieve the response from the server."));
        }
        getClearButton().setEnabled(true);
      }
    }, false);
  }

  /**
   * The edit script comes from the model, including component names it echoes back from the
   * project, so all of it is escaped before it goes into the chat pane. The message is the
   * server's own formatted answer and is shown the way a plain answer would be.
   */
  private String renderProposal(String id, String message, AiEditScript editScript) {
    StringBuilder html = new StringBuilder();
    html.append(AI_ASSISTANT);
    if (message != null && !message.isBlank()) {
      // the server formats its message the same way as a plain answer, so it goes in as is
      html.append(message).append("<br><br>");
    }
    html.append("<b>" + LangUtil.translate("Proposed Changes:") + "</b><br>");
    if (editScript.getExplanation() != null) {
      html.append(escapeHtml(editScript.getExplanation())).append("<br><br>");
    }

    if (editScript.getOperations() != null) {
      Map<String, List<String>> groupedOps = new LinkedHashMap<>();
      // a z-order change names two components, a replace names a component and its new type and
      // a reconnect names a wire end and its new terminal, so each one gets a line of its own
      // instead of being folded into a list
      List<String> separateLines = new ArrayList<>();
      for (AiEditOperation op : editScript.getOperations()) {
        String action = op.getAction() != null ? op.getAction().toLowerCase() : "";
        String compStr = escapeHtml(op.getComponentName());
        switch (action) {
          case "add":
            if (op.getProperties() != null && op.getProperties().containsKey("Value")) {
              compStr += " (" + escapeHtml(op.getProperties().get("Value")) + ")";
            }
            break;
          case "modify":
            if (op.getProperties() != null) {
              compStr += " (" + String.join(", ", op.getProperties().entrySet().stream()
                  .map(e -> escapeHtml(e.getKey()) + "=" + escapeHtml(e.getValue()))
                  .toArray(String[]::new)) + ")";
            }
            break;
          case "replace":
            separateLines.add("\u21C4 " + String.format(LangUtil.translate("Replace %s with %s"),
                compStr, escapeHtml(op.getComponentType())));
            continue;
          case "reconnect":
            separateLines.add("\u21AA " + String.format(LangUtil.translate("Reconnect %s to %s"),
                escapeHtml(op.getComponentName() + "." + op.getTerminal()),
                escapeHtml(op.getToTerminal())));
            continue;
          case "send_behind":
            separateLines.add("\u2B07\uFE0F " + String.format(
                LangUtil.translate("Send %s behind %s"), compStr,
                escapeHtml(op.getReferenceComponent())));
            continue;
          case "bring_in_front":
            separateLines.add("\u2B06\uFE0F " + String.format(
                LangUtil.translate("Bring %s in front of %s"), compStr,
                escapeHtml(op.getReferenceComponent())));
            continue;
        }
        groupedOps.computeIfAbsent(action, k -> new ArrayList<>()).add(compStr);
      }

      for (Map.Entry<String, List<String>> entry : groupedOps.entrySet()) {
        String action = entry.getKey();
        String compsStr = String.join(", ", entry.getValue());
        switch (action) {
          case "add":
            html.append("\u2795 ").append(String.format(LangUtil.translate("Add %s"), compsStr));
            break;
          case "modify":
            html.append("\u270E ").append(String.format(LangUtil.translate("Modify %s"), compsStr));
            break;
          case "remove":
            html.append("\u274C ").append(String.format(LangUtil.translate("Remove %s"), compsStr));
            break;
          default:
            html.append("\u2022 ").append(escapeHtml(action)).append(" ").append(compsStr);
        }
        html.append("<br>");
      }
      for (String line : separateLines) {
        html.append(line).append("<br>");
      }
      html.append("<br>");
    }

    html.append("<a href='" + APPLY_PROPOSAL_LINK + id + "'><b>[ "
        + LangUtil.translate("Apply Changes") + " ]</b></a>");
    return html.toString();
  }

  /**
   * An edit script refers to components by name and places new ones by position, so it is only
   * safe to run against the project exactly as the model saw it. Anything that happened since,
   * whether a manual edit, another proposal being applied or a different project being opened,
   * shows up as a different fingerprint.
   */
  private void applyProposal(String id) {
    Proposal proposal = proposals.remove(id);
    if (proposal == null) {
      return;
    }
    swingUI.executeBackgroundTask(new ITask<String>() {

      @Override
      public String doInBackground() throws Exception {
        return chatbotService.getProjectFingerprint();
      }

      @Override
      public void failed(Exception e) {
        LOG.error("Could not check the project before applying the proposed changes", e);
        proposals.put(id, proposal);
      }

      @Override
      public void complete(String fingerprint) {
        if (!fingerprint.equals(proposal.projectFingerprint())) {
          proposals.put(id, proposal);
          replaceLink(APPLY_PROPOSAL_LINK + id, "<font color='orange'>" + LangUtil.translate(
              "The project has changed since these changes were proposed, so they can no longer be applied.")
              + "</font> <a href='" + ASK_AGAIN_LINK + id + "'>"
              + LangUtil.translate("Ask again") + "</a>");
          return;
        }

        AiEditScriptEditor editor = new AiEditScriptEditor(proposal.editScript());
        plugInPort.applyEditor(editor);

        String replacementText;
        if (!editor.getWarnings().isEmpty()) {
          String warningMsg = editor.getWarnings().stream()
              .map(JsonToHtmlConverter::escapeHtml)
              .collect(Collectors.joining("<br>"));
          replacementText = "<font color='orange'><b>\u26A0 "
              + LangUtil.translate("Warnings during apply:") + "</b><br>" + warningMsg + "</font>";
        } else {
          replacementText = "<b>\u2705 " + LangUtil.translate("Changes applied") + "</b>";
        }
        replaceLink(APPLY_PROPOSAL_LINK + id, replacementText);
      }
    }, false);
  }

  private void askAgain(String id) {
    Proposal proposal = proposals.remove(id);
    if (proposal == null) {
      return;
    }
    replaceLink(ASK_AGAIN_LINK + id, "");
    ask(proposal.prompt());
  }

  private void replaceLink(String href, String replacement) {
    // matched loosely because JEditorPane rewrites the markup it is given, quotes included
    String chatText = getChatEditorPane().getText();
    chatText = chatText.replaceAll(
        "(?is)<a\\s+href=['\"]" + Pattern.quote(href) + "['\"][^>]*>.*?</a>",
        Matcher.quoteReplacement(replacement));
    getChatEditorPane().setText(chatText);
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
      styleButton(clearButton);
      clearButton.addActionListener(e -> {
        // Show confirmation dialog
        int result = swingUI.showConfirmDialog(
            LangUtil.translate("Are you sure you want to clear the chat history for this project? This action cannot be undone!"),
            LangUtil.translate("AI Assistant"),
            ISwingUI.YES_NO_OPTION,
            ISwingUI.WARNING_MESSAGE);
            
        if (result == ISwingUI.YES_OPTION) {
          swingUI.executeBackgroundTask(new ITask<Void>() {

            @Override
            public Void doInBackground() throws Exception {
              chatbotService.deleteChatHistory();
              return null;
            }

            @Override
            public void failed(Exception e) {
              appendSection(ChatbotService.SYSTEM,
                  LangUtil.translate("Failed to delete chat history from the server."));
              LOG.error("Failed to delete chat history from the server", e);
              getClearButton().setEnabled(true);
            }

            @Override
            public void complete(Void result) {
              clearChat();
            }
          }, false);
        }
      });
    }
    return clearButton;
  }

  public JButton getExportButton() {
    if (exportButton == null) {
      exportButton = new JButton(LangUtil.translate("Export"));
      styleButton(exportButton);
      exportButton.addActionListener(e -> {
        String projectName = FileUtils.extractFileName(plugInPort.getCurrentFileName());
        File initialFile = new File(projectName + " - chat history.html");
        File file = DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(),
            FileFilterEnum.HTML.getFilter(), initialFile,
            FileFilterEnum.HTML.getExtensions()[0], null);
        if (file != null) {
          try {
            String content = getChatEditorPane().getText();
            Files.writeString(Paths.get(file.getAbsolutePath()), content);
            swingUI.showMessage(LangUtil.translate("Chat history exported successfully."),
                LangUtil.translate("Export"), ISwingUI.INFORMATION_MESSAGE);
          } catch (IOException ex) {
            LOG.error("Failed to export chat history to file", ex);
            swingUI.showMessage(
                String.format(LangUtil.translate("Could not save to file. %s"), ex.getMessage()),
                LangUtil.translate("Error"), ISwingUI.ERROR_MESSAGE);
          }
        }
      });
    }
    return exportButton;
  }

  private void styleButton(JButton button) {
    button.setBackground(BUTTON_BG);
    button.setForeground(BUTTON_FG);
    button.setEnabled(true);  // Reset enabled state to ensure proper color
    button.setFocusPainted(false);
    button.setBorderPainted(true);
    button.setContentAreaFilled(false);
    button.setOpaque(true);
    
    // Create a simple rounded border with padding
    button.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BUTTON_BORDER, 1),
        BorderFactory.createEmptyBorder(4, 8, 4, 8)
    ));
    
    // Add hover effect
    button.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseEntered(java.awt.event.MouseEvent evt) {
        if (button.isEnabled()) {
          button.setBackground(BUTTON_HOVER);
        }
      }
      public void mouseExited(java.awt.event.MouseEvent evt) {
        if (button.isEnabled()) {
          button.setBackground(BUTTON_BG);
        }
      }
    });
    
    // Set minimum and preferred size based on button type
    button.setMinimumSize(new Dimension(80, 25));
    button.setPreferredSize(new Dimension(80, 25));
  }

  /** An edit script waiting to be applied, with the prompt that produced it, to ask again. */
  private record Proposal(AiEditScript editScript, String projectFingerprint, String prompt) {
  }
}
