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

  public static final String ASSISTANT = "assistant";
  public static final String SYSTEM = "system";
  public static final String USER = "user";
  public static final String TEMPORARY = "temporary";

  String[] electronicsQuestions = {
      "What is Ohm's Law, and how do voltage, current, and resistance relate to each other?",
      "Why are resistors important in circuits, and what happens if the resistor value is incorrect?",
      "What is a capacitor, and how does it behave differently from a resistor?",
      "How does an inductor work, and where might you find one in common DIY projects?",
      "What's the difference between AC (Alternating Current) and DC (Direct Current), and where is each used?",
      "How do diodes work, and why are they essential in electronics?",
      "What's the difference between series and parallel circuits, and why does it matter?",
      "How does a transistor function, and what role does it typically play in a circuit?",
      "Why is grounding important in electronic circuits, and what exactly does it do?",
      "What is impedance, and why is it important when connecting speakers or audio equipment?",
      "What exactly is an operational amplifier (op amp), and what are its common uses in audio circuits?",
      "How does a relay work, and when would you choose a relay over a transistor or MOSFET?",
      "What's the purpose of using a fuse or circuit breaker in a DIY electronics project?",
      "Why do certain electronic components need specific voltage ratings?",
      "What is electromagnetic interference (EMI), and how can you minimize it in your projects?",
      "What does polarity mean for components like electrolytic capacitors and diodes, and why is it important?",
      "How does filtering work to remove unwanted noise from audio signals or power supplies?",
      "What is signal clipping in audio circuits, and how can you prevent it?",
      "Why do some circuits require a heatsink, and how do heatsinks help protect components?",
      "What is frequency response, and why is it crucial for audio circuits like guitar pedals and amplifiers?",
      "How do you calculate the correct resistor value for limiting current to an LED?",
      "What is the role of a voltage regulator, and how does it keep voltage stable?",
      "How can you measure and verify voltage and current safely using a multimeter?",
      "What does it mean when a circuit is described as having a 'high input impedance' or 'low output impedance,' and why does it matter?",
      "How can you determine if components in your circuit are properly rated for the voltage and current they're experiencing?"
  };

  String[] diylcQuestions = {
      "How do I add a component to my layout in DIYLC?",
      "How can I quickly find a specific component in DIYLC?s component toolbox?",
      "How do I move or reposition components on the canvas in DIYLC?",
      "How do I edit or change a component?s properties (e.g. value or color) in DIYLC?",
      "Is there a shortcut to duplicate or repeat the last component I placed in DIYLC?",
      "How can I group multiple components so I can move or edit them together in DIYLC?",
      "What are ?building blocks? in DIYLC and how do I create or use them?",
      "How do I save my DIYLC project and reopen it later on?",
      "How do I export my DIYLC layout as an image or PDF file?",
      "Can DIYLC generate a bill of materials (BOM) for my project, and how would I do that?",
      "How can I change or hide different layers in a DIYLC project to manage overlapping components?",
      "Is it possible to view the underside of a board in DIYLC, and how can I enable that?",
      "What does the ?Highlight Connected Areas? feature do in DIYLC and how do I use it for debugging?",
      "How can I analyze a guitar wiring diagram using DIYLC?s built-in tools?",
      "How do I assign a custom keyboard shortcut to a component in DIYLC for quick access?",
      "What are component variants in DIYLC and how can I create and apply them?",
      "What is the continuous creation mode in DIYLC and how does it help when adding many components?",
      "How can I create a custom component to add to DIYLC?s component library?",
      "Does DIYLC support plug-ins, and how can I add new functionality to the app with them?",
      "What is the DIYLC cloud feature and how can I share or download projects using it?"
  };

  String[] circuitQuestions = {
       "What's the purpose of this specific resistor in my circuit?",
        "How do I correctly determine capacitor values for my power supply?",
        "How should I route wiring to reduce interference and noise?",
        "Which component should I choose for this switching application, a MOSFET or BJT?",
        "How do I verify if my ground connections are optimal?",
        "Why might this transistor get hot during operation?",
        "What's the best approach to simplify this section of my circuit?",
        "How can I ensure my circuit is safe against short circuits?",
        "What could be causing voltage instability in this circuit?",
        "How do I properly size resistors to protect LEDs?",
        "What are the steps to debug unexpected oscillations in my circuit?",
        "Should I add a bypass capacitor here, and what value should I use?",
        "How can I check if my components are operating within their rated specifications?",
        "Is this the right way to connect multiple power rails in my design?",
        "How can I test my circuit before physically assembling it?",
        "What are good layout practices to minimize electromagnetic interference?",
        "How do I identify if my circuit has any redundant or unnecessary components?",
        "Why might the voltage regulator not output the expected voltage?",
        "What's the recommended method for isolating digital and analog grounds?",
        "How do I safely integrate high-voltage components into my DIY circuit?",
        "Why isn't my LED lighting up when powered?",
        "How do I troubleshoot a circuit that's drawing more current than expected?",
        "Why does my amplifier circuit produce a humming or buzzing sound?",
        "How can I detect and fix intermittent connections in my circuit?",
        "What should I check if my circuit isn't responding to input signals as expected?",
        "How do I determine if my soldering joints are causing reliability issues?",
        "What steps can I take if my microcontroller circuit isn't functioning as intended?",
        "How do I troubleshoot a noisy or unstable sensor reading?"
  };
  
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

      String sampleChat = getInitialChatContents();

      chatEditorPane.setContentType("text/html");
      chatEditorPane.setText(htmlStyle + "<body><pre>" + sampleChat + "</pre></body>");
    }
    return chatEditorPane;
  }

  private String getInitialChatContents() {
    Random r = new Random();

    String chatHtml =
        "<div class='system'>Welcome to the DIYLC AI Assistant!</div><br><br>\n" +
            "<div class='assistant'>I'm here to help you design, build, and troubleshoot your electronics projects using DIY Layout Creator (DIYLC). You can ask me questions about:</div><br>\n" +
//            "<ul>\n" +
            "\n" +
            "- <b>Electronics theory or concepts:</b> <i>e.g.,</i> '<span class='user'>" + electronicsQuestions[r.nextInt(
            electronicsQuestions.length)] + "</span>'<br>\n" +
            "- <b>DIYLC features and usage:</b> <i>e.g.,</i> '<span class='user'>" + diylcQuestions[r.nextInt(diylcQuestions.length)] + "</span>'<br>\n" +
            "- <b>Your current circuit project:</b> <i>e.g.,</i> '<span class='user'>" + circuitQuestions[r.nextInt(circuitQuestions.length)] + "</span>'<br>\n" +
//            "</ul><br>\n" +
            "<br>\n" +
            "<div class='assistant'>Feel free to ask your own question or use one of these examples to get started!</div><br>\n";

    if (!CloudPresenter.Instance.isLoggedIn()) {
      chatHtml = chatHtml + "<div class='system' id='login'>Please log into your DIYLC Cloud account in order to use the AI Assistant.</div><br>\n";
    }

    return chatHtml;
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
        appendSection(USER, prompt);
        appendSection(TEMPORARY, "Waiting for the response...");
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
            appendSection(SYSTEM, "Failed to retrieve the response from the server. Error: " + e.getMessage());
            LOG.error("Failed to retrieve the response from the server", e);
            getClearButton().setEnabled(true);
          }

          @Override
          public void complete(String result) {
            appendSection(ASSISTANT, result);
            getClearButton().setEnabled(true);
          }
        }, true);
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
