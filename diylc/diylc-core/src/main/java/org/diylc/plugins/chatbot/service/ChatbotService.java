/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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
import org.diylc.plugins.cloud.service.NotLoggedInException;
import org.diylc.presenter.ComponentProcessor;
import org.diylc.presenter.ContinuityArea;
import org.diylc.utils.FileUtils;
import org.diylc.utils.ReflectionUtils;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
      String serviceUrl =
          ConfigurationManager.getInstance().readString(IServiceAPI.URL_KEY, "http://www.diy-fever.com/diylc/api/v1/ai");
      ProxyFactory factory = new ProxyFactory(new PhpFlatProxy());
      service = factory.createProxy(IChatbotAPI.class, serviceUrl);
    }
    return service;
  }

  public String promptChatbot(String prompt) throws NotLoggedInException {
    if (!plugInPort.getCloudService().isLoggedIn())
      throw new NotLoggedInException();

    LOG.info("Prompting chatbot: " + prompt);

    String currentFile = plugInPort.getCurrentFileName();
    String fileName = extractFileName(currentFile);

    File aiProjectFile = getAiProjectFile();

    try {
      return getService().promptChatbot(
          plugInPort.getCloudService().getCurrentUsername(),
          plugInPort.getCloudService().getCurrentToken(),
          plugInPort.getCloudService().getMachineId(),
          fileName,
          aiProjectFile,
          prompt);
    } finally {
      // Clean up the temp file immediately after use
      if (aiProjectFile != null && aiProjectFile.exists()) {
        aiProjectFile.delete();
      }
    }
  }

  public String analyzeCircuit() throws NotLoggedInException {
    if (!plugInPort.getCloudService().isLoggedIn())
      throw new NotLoggedInException();

    LOG.info("Analyzing circuit...");

    String currentFile = plugInPort.getCurrentFileName();
    String fileName = extractFileName(currentFile);

    File aiProjectFile = getAiProjectFile();

    try {
      String jsonResult = getService().analyzeCircuit(plugInPort.getCloudService().getCurrentUsername(),
          plugInPort.getCloudService().getCurrentToken(),
          plugInPort.getCloudService().getMachineId(), fileName, aiProjectFile);
      return JsonToHtmlConverter.convertToHtml(jsonResult);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      // Clean up the temp file immediately after use
      if (aiProjectFile != null && aiProjectFile.exists()) {
        aiProjectFile.delete();
      }
    }
  }

  private File getAiProjectFile() {
    File aiProjectFile = null;
    AiProject aiProject = extractAiProject();
    if (aiProject == null) {
      return null;
    }
    try {
      String aiProjectStr = null;
      try {
        aiProjectStr = MAPPER.writeValueAsString(aiProject);
      } catch (JsonProcessingException e) {
        LOG.error("Could not serialize aiProject to JSON", e);
      }
      if (aiProjectStr != null) {
        aiProjectFile = File.createTempFile("diylc_netlist_", ".txt");
        try (FileWriter writer = new FileWriter(aiProjectFile)) {
          writer.write("""
              Take these notes into account when analyzing the circuit
              - Coordinates are represented in pixels assuming 200px/in resolution.
              - Each component will either have a set of terminals that are connectable to other components or one or two points defining its position.
              - Netlists of connected terminals are defined in the 'nets' structure.
              - Analyze labels - they could provide clues about nearby terminals and what they represent.
              """);
          if (aiProject.switches() != null && !aiProject.switches().isEmpty()) {
            writer.write("""
              - Switches are defined in the 'switches' structure. Each switch will have a list of available position and the collection of internal terminals that are connected in each of the positions. Take the switching matrix into consideration when analyzing the circuit operation.
              """);
          }
          if (aiProject.tags().contains("guitar")) {
            IGuitarDiagramAnalyzer guitarDiagramAnalyzer =
                ReflectionUtils.getGuitarDiagramAnalyzer();
            if (guitarDiagramAnalyzer != null) {
              try {
                List<Netlist> netlists = plugInPort.extractNetlists(true);
                writer.write("""
                    - The circuit represents a guitar wiring diagram and below are some of the known characteristics in each combination of switch positions:
                    """);
                netlists.forEach(netlist -> {
                  List<String> notes = guitarDiagramAnalyzer.collectNotes(netlist);
                  try {
                    writer.write(netlist.getSwitchSetupString() + ": \n");
                    for (String note : notes) {
                      writer.write("  - " + note + "\n");
                    }
                  } catch (IOException ex) {
                    LOG.warn("Could not write guitar diagram notes", ex);
                  }
                });
              } catch (NetlistException | IOException ex) {
                LOG.warn("Could not extract guitar diagram netlist", ex);
              }
            }
          }
          writer.write("\nBelow is the JSON describing the circuit in detail.\n");
          writer.write(aiProjectStr);
        }
        // Make sure the temp file is deleted when the JVM exits
        aiProjectFile.deleteOnExit();
      }
    } catch (Exception e) {
      LOG.error("Error extracting or saving netlist", e);
    }
    return aiProjectFile;
  }

  private AiProject extractAiProject() {
    if (plugInPort.getCurrentProject().getComponents().isEmpty()) {
      return null;
    }
    List<ContinuityArea> continuityAreas = plugInPort.getDrawingManager().getContinuityAreas();
    return AiProjectBuilder.build(plugInPort.getCurrentProject(), continuityAreas);
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
}
