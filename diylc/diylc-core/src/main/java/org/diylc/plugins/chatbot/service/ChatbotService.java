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
package org.diylc.plugins.chatbot.service;

import com.diyfever.httpproxy.PhpFlatProxy;
import com.diyfever.httpproxy.ProxyFactory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.simplemq.IMessageListener;
import org.diylc.common.ComponentType;
import org.diylc.common.EventType;
import org.diylc.common.IPlugInPort;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.IDIYComponent;
import org.diylc.core.ISwitch;
import org.diylc.netlist.*;
import org.diylc.plugins.chatbot.model.*;
import org.diylc.plugins.cloud.model.IServiceAPI;
import org.diylc.plugins.cloud.service.CloudException;
import org.diylc.plugins.cloud.service.NotLoggedInException;
import org.diylc.presenter.ComponentArea;
import org.diylc.presenter.ComponentProcessor;
import org.diylc.presenter.ContinuityArea;
import org.diylc.presenter.DrawingManager;
import org.diylc.utils.FileUtils;
import org.diylc.utils.ReflectionUtils;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.List;

import static org.diylc.utils.FileUtils.extractFileName;

/**
 * Contains all the back-end logic for using the chatbot.
 * 
 * @author Branislav Stojkovic
 */
public class ChatbotService {

  private final static Logger LOG = Logger.getLogger(ChatbotService.class);
  private static final ObjectMapper MAPPER = new ObjectMapper();
  public static final String SERVER_ERROR_PLEASE_TRY_AGAIN = "Server error, please try again.";

  static {
    MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }
  public static final String WELCOME_MESSAGE = "Welcome to the DIYLC AI Assistant!";

  private final IPlugInPort plugInPort;

  private IChatbotAPI service;

  private String currentProjectName;

  public ChatbotService(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    this.plugInPort.getMessageDispatcher().registerListener(new IMessageListener<EventType>() {


      @Override
      public EnumSet<EventType> getSubscribedEventTypes() {
        return EnumSet.of(EventType.PROJECT_LOADED, EventType.PROJECT_SAVED);
      }

      @Override
      public void processMessage(EventType eventType, Object... objects) {
        if (eventType == EventType.PROJECT_LOADED) {
          ChatbotService.this.currentProjectName = extractFileName((String)objects[2]);
        } else if (eventType == EventType.PROJECT_SAVED) {
          String newProjectName = extractFileName((String)objects[0]);
          if (!Objects.equals(ChatbotService.this.currentProjectName, newProjectName)) {
            if (plugInPort.getCloudService().isLoggedIn()) {
              LOG.info("Migrating project chat history from " + ChatbotService.this.currentProjectName + " to " + newProjectName);
              getService().updateChatProject(plugInPort.getCloudService().getCurrentUsername(),
                  plugInPort.getCloudService().getCurrentToken(),
                  plugInPort.getCloudService().getMachineId(),
                  ChatbotService.this.currentProjectName, newProjectName);
            }
            ChatbotService.this.currentProjectName = newProjectName;
          }
        }
      }
    });
  }

  private IChatbotAPI getService() {
    if (service == null) {
      String serviceUrl = System.getProperty("org.diylc.cloud.baseUrl",
          ConfigurationManager.getInstance().readString(IServiceAPI.URL_KEY, "http://www.diy-fever.com/diylc/api/v1")) + "/ai";
      ProxyFactory factory = new ProxyFactory(new PhpFlatProxy());
      service = factory.createProxy(IChatbotAPI.class, serviceUrl);
    }
    return service;
  }

  public ChatbotResponse promptChatbot(String prompt) throws NotLoggedInException, CloudException {
    if (!plugInPort.getCloudService().isLoggedIn())
      throw new NotLoggedInException();

    // TODO: comment out before the release, prompts are logged for testing only
    LOG.debug("Prompting chatbot: " + prompt);

    String currentFile = plugInPort.getCurrentFileName();
    String fileName = extractFileName(currentFile);

    AiProject aiProject = extractAiProject();
    File circuitFile = getCircuitFile(aiProject);
    File clientAnalysisFile = getClientAnalysisFile(aiProject);

    try {
      String rawResponse = getService().promptChatbot(plugInPort.getCloudService().getCurrentUsername(),
          plugInPort.getCloudService().getCurrentToken(),
          plugInPort.getCloudService().getMachineId(), fileName, circuitFile, clientAnalysisFile, prompt);
      
      // TODO: comment out before the release, responses are logged for testing only
//      LOG.debug("Raw chatbot response: " + rawResponse);
      
      if (rawResponse == null || rawResponse.isBlank()) {
          throw new CloudException(SERVER_ERROR_PLEASE_TRY_AGAIN);
      }
      
      // Try to parse as ChatbotResponse (covers both standard chat and interactive edit)
      ChatbotResponse response = new ChatbotResponse();
      String trimmed = rawResponse.trim();
      if (trimmed.startsWith("{")) {
        try {
          ObjectMapper mapper = new ObjectMapper();
          response = mapper.readValue(trimmed, ChatbotResponse.class);
        } catch (Exception parseEx) {
          LOG.warn("Could not parse response as structured JSON, treating as plain text: " + parseEx.getMessage());
          response.setString(rawResponse);
        }
      } else {
        response.setString(rawResponse);
      }
      response.setProjectFingerprint(fingerprint(aiProject));

      return response;
    } catch (CloudException ce) {
      throw ce;
    } catch (Exception e) {
      LOG.error("Chatbot error", e);
      throw new CloudException(SERVER_ERROR_PLEASE_TRY_AGAIN);
    } finally {
      if (circuitFile != null && circuitFile.exists()) {
        circuitFile.delete();
      }
      if (clientAnalysisFile != null && clientAnalysisFile.exists()) {
        clientAnalysisFile.delete();
      }
    }
  }

  public String analyzeCircuit() throws NotLoggedInException, CloudException {
    if (!plugInPort.getCloudService().isLoggedIn())
      throw new NotLoggedInException();

    LOG.info("Analyzing circuit...");

    String currentFile = plugInPort.getCurrentFileName();
    String fileName = extractFileName(currentFile);

    AiProject aiProject = extractAiProject();
    File circuitFile = getCircuitFile(aiProject);
    File clientAnalysisFile = getClientAnalysisFile(aiProject);

    try {
      String jsonResult = getService().analyzeCircuit(plugInPort.getCloudService().getCurrentUsername(),
          plugInPort.getCloudService().getCurrentToken(),
          plugInPort.getCloudService().getMachineId(), fileName, circuitFile, clientAnalysisFile);

      if (jsonResult == null || jsonResult.trim().isEmpty()) {
        throw new CloudException(SERVER_ERROR_PLEASE_TRY_AGAIN);
      }

      return JsonToHtmlConverter.convertToHtml(jsonResult);
    } catch (Exception e) {
      LOG.error("AI Analyzer error", e);
      throw new CloudException(SERVER_ERROR_PLEASE_TRY_AGAIN);
    } finally {
      if (circuitFile != null && circuitFile.exists()) {
        circuitFile.delete();
      }
      if (clientAnalysisFile != null && clientAnalysisFile.exists()) {
        clientAnalysisFile.delete();
      }
    }
  }

  private File getCircuitFile(AiProject aiProject) {
    if (aiProject == null) {
      return null;
    }
    try {
      String aiProjectStr = MAPPER.writeValueAsString(aiProject);
      File circuitFile = File.createTempFile("diylc_circuit_", ".txt");
      try (FileWriter writer = new FileWriter(circuitFile, StandardCharsets.UTF_8)) {
        writer.write(aiProjectStr);
      }
      circuitFile.deleteOnExit();
      return circuitFile;
    } catch (Exception e) {
      LOG.error("Error saving circuit file", e);
      return null;
    }
  }

  private File getClientAnalysisFile(AiProject aiProject) {
    if (aiProject == null || !aiProject.tags().contains("guitar")) {
      return null;
    }
    IGuitarDiagramAnalyzer guitarDiagramAnalyzer = null;
    try {
      guitarDiagramAnalyzer = ReflectionUtils.getGuitarDiagramAnalyzer();
    } catch (Exception e) {
      LOG.error("Could not get guitar diagram analyzer", e);
      return null;
    }
    if (guitarDiagramAnalyzer == null) {
      return null;
    }
    try {
      List<Netlist> netlists = plugInPort.extractNetlists(true);
      Map<String, List<String>> analysis = new LinkedHashMap<>();
      for (Netlist netlist : netlists) {
        List<String> notes = guitarDiagramAnalyzer.collectNotes(netlist);
        if (notes != null && !notes.isEmpty()) {
          analysis.put(netlist.getSwitchSetupString(), notes);
        }
      }
      if (analysis.isEmpty()) {
        return null;
      }
      String analysisStr = MAPPER.writeValueAsString(analysis);
      File analysisFile = File.createTempFile("diylc_client_analysis_", ".txt");
      try (FileWriter writer = new FileWriter(analysisFile, StandardCharsets.UTF_8)) {
        writer.write(analysisStr);
      }
      analysisFile.deleteOnExit();
      return analysisFile;
    } catch (Exception e) {
      LOG.error("Could not extract guitar diagram notes", e);
      return null;
    }
  }

  /**
   * @return a fingerprint of the current project, comparable with
   *         {@link ChatbotResponse#getProjectFingerprint()} to tell whether the project still looks
   *         the way it did when the response was produced
   */
  public String getProjectFingerprint() {
    return fingerprint(extractAiProject());
  }

  /**
   * Hashes the part of the project description that an edit script depends on: the components,
   * their terminals and z-order, and how the terminals are connected. Net names and the order in
   * which nets and their members are listed are left out, because the netlist does not promise to
   * produce them in the same order twice.
   */
  static String fingerprint(AiProject aiProject) {
    if (aiProject == null) {
      return "";
    }
    List<List<String>> nets = new ArrayList<>();
    for (Set<String> net : aiProject.nets().values()) {
      nets.add(net.stream().sorted().toList());
    }
    nets.sort(Comparator.comparing(net -> String.join(",", net)));
    try {
      byte[] canonical =
          MAPPER.writeValueAsString(List.of(aiProject.components(), nets)).getBytes(
              StandardCharsets.UTF_8);
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical));
    } catch (JsonProcessingException | NoSuchAlgorithmException e) {
      throw new IllegalStateException("Could not fingerprint the project", e);
    }
  }

  private AiProject extractAiProject() {
    if (plugInPort.getCurrentProject().getComponents().isEmpty()) {
      return null;
    }
    DrawingManager drawingManager = plugInPort.getDrawingManager();
    List<ContinuityArea> continuityAreas = drawingManager.getContinuityAreas();
    return AiProjectBuilder.build(plugInPort.getCurrentProject(), continuityAreas, component -> {
      ComponentArea area = drawingManager.getComponentArea(component);
      return area == null ? null : area.getOutlineBounds();
    }, plugInPort.getSelectedComponents());
  }

  public SubscriptionEntity getSubscriptionInfo() throws NotLoggedInException {
    if (!plugInPort.getCloudService().isLoggedIn())
      throw new NotLoggedInException();

    return getService().getSubscriptionInfo(plugInPort.getCloudService().getCurrentUsername(),
        plugInPort.getCloudService().getCurrentToken(), plugInPort.getCloudService().getMachineId());
  }

  public void deleteChatHistory() throws NotLoggedInException {
    if (!plugInPort.getCloudService().isLoggedIn())
      throw new NotLoggedInException();

    getService().deleteChatHistory(plugInPort.getCloudService().getCurrentUsername(),
        plugInPort.getCloudService().getCurrentToken(), plugInPort.getCloudService().getMachineId(),
        FileUtils.extractFileName(plugInPort.getCurrentFileName()));
  }

  public List<ChatMessageEntity> getChatHistory() throws NotLoggedInException {
    if (!plugInPort.getCloudService().isLoggedIn())
      throw new NotLoggedInException();

    return getService().getChatHistory(plugInPort.getCloudService().getCurrentUsername(),
        plugInPort.getCloudService().getCurrentToken(), plugInPort.getCloudService().getMachineId(),
        FileUtils.extractFileName(plugInPort.getCurrentFileName()));
  }

  public static final String ASSISTANT = "assistant";
  public static final String SYSTEM = "system";
  public static final String USER = "user";
  public static final String TEMPORARY = "temporary";

  public static final String[] electronicsQuestions = {
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

  public static final String[] diylcQuestions = {
      "How do I add a component to my layout in DIYLC?",
      "How can I quickly find a specific component in DIYLC's component toolbox?",
      "How do I move or reposition components on the canvas in DIYLC?",
      "How do I edit or change a component's properties (e.g. value or color) in DIYLC?",
      "Is there a shortcut to duplicate or repeat the last component I placed in DIYLC?",
      "How can I group multiple components so I can move or edit them together in DIYLC?",
      "What are 'building blocks' in DIYLC and how do I create or use them?",
      "How do I save my DIYLC project and reopen it later on?",
      "How do I export my DIYLC layout as an image or PDF file?",
      "Can DIYLC generate a bill of materials (BOM) for my project, and how would I do that?",
      "How can I change or hide different layers in a DIYLC project to manage overlapping components?",
      "Is it possible to view the underside of a board in DIYLC, and how can I enable that?",
      "What does the 'Highlight Connected Areas' feature do in DIYLC and how do I use it for debugging?",
      "How can I analyze a guitar wiring diagram using DIYLC's built-in tools?",
      "How do I assign a custom keyboard shortcut to a component in DIYLC for quick access?",
      "What are component variants in DIYLC and how can I create and apply them?",
      "What is the continuous creation mode in DIYLC and how does it help when adding many components?",
      "How can I create a custom component to add to DIYLC's component library?",
      "Does DIYLC support plug-ins, and how can I add new functionality to the app with them?",
      "What is the DIYLC cloud feature and how can I share or download projects using it?"
  };

  public static final String[] circuitQuestions = {
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
  public static final String[] editQuestions = {
      "Can you move the volume and tone pots so they line up evenly along the front panel?",
      "Can you replace the 1N4148 clipping diodes with red LEDs?",
      "Can you swap the ceramic disc capacitors in the signal path for film capacitors?",
      "Can you change all the 10k resistors in this layout to 4.7k?",
      "Can you move the input and output jacks to opposite sides of the enclosure?",
      "Can you replace the toggle switch with a 3PDT footswitch for true bypass?",
      "Can you shift the whole circuit a few holes to the left to make room for a power filter?",
      "Can you add a 100uF filter capacitor across the 9V supply?",
      "Can you remove the unused jumpers and trace cuts from the stripboard?",
      "Can you reconnect the input jack ground wire to the star ground point instead?",
      "Can you rewire the pickup selector so the middle pickup is out of phase?",
      "Can you bring the hookup wires in front of the board so they're easier to follow?",
      "Can you send the chassis panel behind all the other components?",
      "Can you recolor the wiring so ground wires are black and power wires are red?",
      "Can you replace the linear volume pot with an audio taper one?",
      "Can you move the cathode resistors and bypass caps closer to their tube sockets?",
      "Can you add a 1M pulldown resistor to the input jack?",
      "Can you double the value of the output coupling capacitor for a little more bass?",
      "Can you spread out the components on this perfboard so they're easier to solder?",
      "Can you swap the positions of the two transistors?",
      "Can you replace the BC549 transistors with 2N5088s and set the correct pinout?",
      "Can you tidy up the wiring so the wires don't cross over the pots?",
      "Can you move the LED and its current limiting resistor to the top edge of the board?",
      "Can you remove the tone control and wire the volume pot straight to the output jack?",
      "Can you replace the carbon comp resistors in the preamp with metal film ones?",
      "Can you turn the IC around so pin 1 points toward the input side of the board?",
      "Can you rename the components so they're numbered from the input to the output?"
  };
  public static final String[] generationQuestions = {
      "Can you lay out an empty pedal enclosure with input, output and DC jacks, a footswitch and an LED?",
      "Can you generate a stripboard layout for a Fuzz Face?",
      "Can you design a PCB for a simple single-transistor treble booster?",
      "Can you build a turret board layout for a tweed Deluxe style amp?",
      "Can you lay out an eyelet board for a tweed Champ style amp?",
      "Can you create an amp chassis with the transformers, tube sockets, pots and jacks in place?",
      "Can you generate Strat wiring with a five-way switch, one volume and two tones?",
      "Can you generate Telecaster wiring with a four-way switch?",
      "Can you create a Les Paul wiring harness with 50s style wiring?",
      "Can you lay out Jazz Bass wiring with two volumes and a master tone?",
      "Can you design a perfboard layout for an LM386 practice amplifier?",
      "Can you design a PCB for an op amp clean boost built around a TL072?",
      "Can you generate a stripboard layout for a Tube Screamer style overdrive?",
      "Can you lay out a passive Fender style tone stack on a small turret board?",
      "Can you generate a tube amp power supply with a bridge rectifier and filter capacitors?",
      "Can you design a 9V power filter with reverse polarity protection on a small piece of perfboard?",
      "Can you lay out a buffer on stripboard that fits next to my existing board?",
      "Can you add a series effects loop with send and return jacks to my amp?",
      "Can you generate a tremolo section and place it next to my existing amp layout?",
      "Can you lay out the front panel of a two-channel preamp with all its pots, jacks and switches?",
      "Can you lay out a top-mounted jack arrangement for a 1590B enclosure?",
      "Can you generate a breadboard layout for a 555 LED flasher so I can test it first?",
      "Can you wire up the offboard components for this pedal: jacks, footswitch, LED and pots?",
      "Can you build a tag strip layout for the input stage of a tube amp?",
      "Can you lay out an Arduino Nano on a breadboard with an OLED display and a push button?"
  };
}
