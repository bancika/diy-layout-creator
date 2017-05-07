package org.diylc;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.SplashScreen;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.PropertyInjector;
import org.diylc.images.IconLoader;
import org.diylc.presenter.Presenter;
import org.diylc.swing.gui.MainFrame;
import org.diylc.swing.gui.TemplateDialog;

/**
 * Main class that runs DIYLC.
 * 
 * @author Branislav Stojkovic
 * 
 * @see Presenter
 * @see MainFrame
 */
public class DIYLCStarter {

  private static final Logger LOG = Logger.getLogger(DIYLCStarter.class);

  private static final String SCRIPT_RUN = "org.diylc.scriptRun";

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    URL url = DIYLCStarter.class.getResource("log4j.properties");
    Properties properties = new Properties();
    try {
      properties.load(url.openStream());
      PropertyConfigurator.configure(properties);
    } catch (Exception e) {
      LOG.error("Could not initialize log4j configuration", e);
    }
    
    // Initialize splash screen
    final SplashScreen splash = SplashScreen.getSplashScreen();
    if (splash != null) {
      final Graphics2D g = splash.createGraphics();
      if (g != null) {
        Thread t = new Thread(new Runnable() {

          @Override
          public void run() {
            for (int i = 90; i >= 0; i--) {
              if (!splash.isVisible())
                return;
              final int frame = i;
              SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                  renderSplashFrame(splash, g, frame);
                  splash.update();
                }
              });
              try {
                Thread.sleep(10);
              } catch (InterruptedException e) {
              }
            }
          }
        });
        t.start();
      }
    }

//    try {
//      Thread.sleep(20000);
//    } catch (InterruptedException e1) {
//      // TODO Auto-generated catch block
//      e1.printStackTrace();
//    }

    ConfigurationManager.initialize("diylc");

    LOG.debug("Java version: " + System.getProperty("java.runtime.version") + " by "
        + System.getProperty("java.vm.vendor"));
    LOG.debug("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));

    LOG.info("Starting DIYLC with working directory " + System.getProperty("user.dir"));

    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      LOG.error("Could not set Look&Feel", e);
    }

    String val = System.getProperty(SCRIPT_RUN);
    if (!"true".equals(val)) {
      int response =
          JOptionPane.showConfirmDialog(null, "It is not recommended to run DIYLC by clicking on the diylc.jar file.\n"
              + "Please use diylc.exe on Windows or run.sh on OSX/Linux to ensure the best\n"
              + "performance and reliability. Do you want to continue?", "DIYLC", JOptionPane.YES_NO_OPTION,
              JOptionPane.WARNING_MESSAGE);
      if (response != JOptionPane.YES_OPTION) {
        System.exit(0);
      }
    }

    MainFrame mainFrame = new MainFrame();
    mainFrame.setLocationRelativeTo(null);
    mainFrame.setVisible(true);
    if (args.length > 0) {
      mainFrame.getPresenter().loadProjectFromFile(args[0]);
    } else {
      boolean showTemplates = ConfigurationManager.getInstance().readBoolean(TemplateDialog.SHOW_TEMPLATES_KEY, false);
      if (showTemplates) {
        TemplateDialog templateDialog = new TemplateDialog(mainFrame, mainFrame.getPresenter());
        if (!templateDialog.getFiles().isEmpty()) {
          templateDialog.setVisible(true);
        }
      }
    }

    properties = new Properties();
    try {
      LOG.info("Injecting default properties.");
      File f = new File("config.properties");
      if (f.exists()) {
        properties.load(new FileInputStream(f));
        PropertyInjector.injectProperties(properties);
      }
    } catch (Exception e) {
      LOG.error("Could not read config.properties file", e);
    }
  }

  // Splash screen stuff below

  private static ImageIcon resistor = null;

  public static ImageIcon getResistor() {
    if (resistor == null) {
      resistor = (ImageIcon)IconLoader.SplashResistor.getIcon();
    }
    return resistor;
  }

  private static ImageIcon film = null;

  public static ImageIcon getFilm() {
    if (film == null) {
      film = (ImageIcon)IconLoader.SplashFilm.getIcon();
    }
    return film;
  }

  private static ImageIcon ceramic = null;

  public static ImageIcon getCeramic() {
    if (ceramic == null) {
      ceramic = (ImageIcon)IconLoader.SplashCeramic.getIcon();
    }
    return ceramic;
  }

  private static ImageIcon electrolytic = null;

  public static ImageIcon getElectrolytic() {
    if (electrolytic == null) {
      electrolytic = (ImageIcon)IconLoader.SplashElectrolytic.getIcon();
    }
    return electrolytic;
  }

  private static ImageIcon splash = null;

  public static ImageIcon getSplash() {
    if (splash == null) {
      splash = (ImageIcon)IconLoader.Splash.getIcon();
    }
    return splash;
  }

  private static Point resistorTarget = new Point(112, 114);
  private static Point filmTarget = new Point(233, 113);
  private static Point electrolyticTarget = new Point(261, 23);
  private static Point ceramicTarget = new Point(352, 22);

  private static int pxPerFrame = 3;

  static void renderSplashFrame(SplashScreen splash, Graphics2D g, int frame) {
    g.setComposite(AlphaComposite.Clear);
    getSplash().paintIcon(null, g, 0, 0);
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
    getResistor().paintIcon(null, g, resistorTarget.x - pxPerFrame * frame, resistorTarget.y);
    getFilm().paintIcon(null, g, filmTarget.x, filmTarget.y + pxPerFrame * frame);
    getElectrolytic().paintIcon(null, g, electrolyticTarget.x, electrolyticTarget.y - pxPerFrame * frame);
    getCeramic().paintIcon(null, g, ceramicTarget.x + pxPerFrame * frame, ceramicTarget.y);
  }
}
