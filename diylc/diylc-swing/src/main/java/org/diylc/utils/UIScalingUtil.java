package org.diylc.utils;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;


// Please note: This class emerged over time. It gets the job done, but it's horrible. Even the commenting is horrible.




/**
 * TL;DR:
 * <p>
 * Call GUIScaling.initialize() at application start on the Swing thread.
 * <p>
 * If you set your own Component font sizes or border sizes or window sizes, multiply them by GUIScaling.GUISCALINGFACTOR_COMPONENTSANDFONTS and/or use the
 * helper methods newDimension() and scaleForComponent(). Works on Java 8 and 9.
 * <p>
 * If you do your own custom graphics and want to have control down to the actual pixel, create an instance of GUIScalingCustomGraphics to obtain your
 * Graphics2D at scaling 1 and your component's true physical width and height (Which Java 9 reports differently!), and scale all your graphics using
 * GUIScaling.GUISCALINGFACTOR_CUSTOMGRAPHICS and/or use the helper method scaleForCustom(). The helper method scaleForRealComponentSize() can transform your
 * mouse coordinates to the real physical coordinate, which Java 9 reports differently!
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * GUIScaling class v[4, 2022-01-15 07!00 UTC] by dreamspace-president.com
 * <p>
 * This Swing class detects the system's display scaling setting, which is important to make your GUI and custom graphics scale properly like the user wants it.
 * On a 4K display, for example, you'd probably set 200% in your system.
 * <p>
 * Not tested with Java less than 8!
 * <p>
 * On Java 8 (and with most but not all (e.g. no the default) LooksAndFeels), component sizes (e.g. JButton) and their font sizes will scale automatically, but
 * if you have a certain border width in mind, or decided for a certain min and default window size or a certain font size, you have to upscale those on a
 * non-100%-system. With this class, just multiply the values with GUISCALINGFACTOR_COMPONENTSANDFONTS. Done. newDimension() and scaleForComponent() help with
 * that.
 * <p>
 * On Java 9, component sizes and their font sizes DO NOT SCALE from the perspective of the application, but in reality they are scaled: A window set to 800x600
 * size will really be 1600x1200, but it will still report half this size when asked. A border of 50 pixels will really be 100 pixels. A Graphics2D object
 * (paint method etc.) will have a scaling of 2! (Not if you just create a BufferedImage object and do createGraphics(), the scale here will be 1.) So, you
 * don't have to bother with GUI scaling here at all. YOU CAN STILL USE GUISCALINGFACTOR_COMPONENTSANDFONTS, because this class will set it to 1 on Java 9. This
 * is detected by indeed checking the scaling of a Graphics2D object. So, your Java 8 and 9 component/font code will be exactly the same in regards to scaling.
 * <p>
 * CUSTOM GRAPHICS: If you do your own painting and want to insist on true physical pixels (in which case obviously you'd have to scale your fonts with
 * GUISCALINGFACTOR_CUSTOMGRAPHICS instead of GUISCALINGFACTOR_COMPONENTSANDFONTS), on Java 9 you have to reset the scaling of the Graphics2D object the
 * paint(Component)() method gives you from 2 to 1, and (also Java 9) you have to adjust the width/height reported by your component. Both is done by making an
 * instance of GUIScalingCustomGraphics. You can do this blindly on Java 8 and 9, your code will stay the same. And then, apply this class'
 * GUISCALINGFACTOR_CUSTOMGRAPHICS to scale everything according to system settings. Or, instead of insisting on true physical pixels, you could trust Java 9
 * and not mess with the initial scaling - but then you'd have to distinguish whether you're dealing with Java 8 or 9, because on 8, you'd still have to scale
 * your custom graphics. In case you decide for this, use GUISCALINGFACTOR_COMPONENTSANDFONTS for your custom graphics instead of
 * GUISCALINGFACTOR_CUSTOMGRAPHICS because the former will be ***1*** on Java 9 but will be proper (e.g. 2.0 for a 200% system) on Java 8.
 * <p>
 * A weird problem that comes with Java 9: If you use the mouse coordinates as reported by the system (instead of, say, quasi-fix the physical mouse pointer
 * invisibly at the screen center and make your own pointer based on coordinate differences), you will have HALF THE USUAL RESOLUTION. On Java 8, a 3840x2160
 * screen will give you according mouse coordinates, but on Java 9, you get half these coordinates (if the system is set to scaling 200%). While
 * scaleForRealComponentSize() helps correct this, a custom drawn mouse pointer will now step in 2 pixel distances, it can not reach every individual pixel any
 * longer. I wish they had updated the MouseEvent class accordingly with additional float methods.
 */
final public class UIScalingUtil { // INITIAL TOUCHING of this class MUST be on Swing thread!

  private static final Logger LOG = Logger.getLogger(UIScalingUtil.class);

  /**
   * Call this at the start of your application ON THE SWING THREAD. This initializes the class and hence its values.
   */
  public static void initialize() {

    LOG.info("UIScalingUtil initialized");
  }


  public static void setLookAndFeelDefault() {
    // The last three (Nimbus etc.) DO NOT automatically scale their font sizes with the system's GUI scaling,
    // so using the font size in those cases to derive the scaling WILL FAIL.
    // Btw., the JButton font size at 100% Windows 10 system scaling is 11.0 in all cases but the last three.
    UIScalingUtil.setLookAndFeel("Windows",
        UIManager.getSystemLookAndFeelClassName(),
        UIManager.getCrossPlatformLookAndFeelClassName(),
        "Windows Classic",
        "Nimbus",
        "Metal",
        "CDE/Motif");
  }


  /**
   * By calling this, you ALSO initialize the class, so you don't HAVE TO use initialize() in that case (but it really doesn't matter). And you can indeed set
   * a LookAndFeel of your choice, even though initialization of this class also sets AND TEMPORARILY USES a LookAndFeel.
   *
   * @param intendedLAFIs ANYTHING, but ideally a LookAndFeel name or several. The first value that equalsIgnoreCase an installed LookAndFeelInfo.getName()
   *                      will be used.
   */
  public static void setLookAndFeel(final String... intendedLAFIs) {

    if (intendedLAFIs != null && intendedLAFIs.length > 0) {
      final UIManager.LookAndFeelInfo[] installedLAFIs = UIManager.getInstalledLookAndFeels();
      LAFILOOP:
      for (String intendedLAFI : intendedLAFIs) {
        for (final UIManager.LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels()) {
          if (lafi.getName().equalsIgnoreCase(intendedLAFI)) {
            try {
              UIManager.setLookAndFeel(lafi.getClassName());
              break LAFILOOP;
            } catch (Exception e) {
              continue LAFILOOP;
            }
          }
        }
      }
    } else {
      throw new IllegalArgumentException("intendedLAFIs is null or empty.");
    }
  }


  /**
   * Convenience method, compatible with Java 8 and 9.
   */
  public static Dimension newDimension(final int w,
      final int h) {

    return new Dimension(scaleForComponent(w), scaleForComponent(h));
  }


  /**
   * @param v E.g. the width of a component, or the size of a border.
   * @return v scaled by the necessary display scaling factor for components and fonts, compatible with Java 8 and 9.
   */
  public static int scaleForComponent(final double v) {

    return (int) Math.round(v * GUISCALINGFACTOR_COMPONENTSANDFONTS);
  }


  /**
   * @param v E.g. the width of a rectangle being drawn in a paint() or paintComponent() override.
   * @return v scaled by the necessary display scaling factor for custom graphics, compatible with Java 8 and 9.
   */
  public static int scaleForCustom(final double v) {

    return (int) Math.round(v * GUISCALINGFACTOR_CUSTOMGRAPHICS);
  }


  /**
   * E.g. when the GraphicsContext tells you the screen bounds. Which in Java 8 on 2x 4K desktop will be the full 4K numbers. But, same conditions except Java
   * 9, will be half that. Even though you still need the full size.
   * <p>
   * But this is just an example case. The method was originally made for different cases.
   *
   * @param v E.g. the width as reported by a component. (Java 9 on 200% desktop reports e.g. 200, but the physical size is actually 400. This method returns
   *          400 then.)
   * @return v scaled so that it represents real physical pixels, compatible with Java 8 and 9.
   */
  public static int scaleForRealComponentSize(final double v) {

    return (int) Math.round(v * GUISCALINGFACTOR_REALCOMPONENTSIZE);
  }


  /**
   * E.g. when you scaled the mouse location with the partner method, and now you want to use the mouse action result to scale/locate a window - then you need
   * to UNDO the damage.
   */
  public static int scaleForRealComponentSizeInverted(final double v) {

    return (int) Math.round(v / GUISCALINGFACTOR_REALCOMPONENTSIZE);
  }


  public static Point scaleMouseEventCoordinatesForCustomGraphics(final Point mouseEventPoint) {

    return new Point(scaleForRealComponentSize(mouseEventPoint.x),
        scaleForRealComponentSize(mouseEventPoint.y));
  }


  /**
   * @param font A font instance (Or null. Returns null.) whose size has been derived kinda like this: "new JLabel().getFont().getSize()" So it will look
   *             correct when used in components, no matter the current Java version. ......... WAIT WTF why does that look correct on Java 8
   *             ?!??!?!?!?!?!?!?! Anyway ... when you want to use THAT font in custom drawing, you'll have a bad time once you get on Java 9. Because
   *             components will have SMALLER font sizes than on Java 8 on a 200% desktop because their Graphics objects are scaled. But if you use custom
   *             drawing, you'll use the class GUIScalingCustomGraphics below, which reset the scaling to 1. But then the font is too small. THIS METHOD
   *             RETURNS THE SCALED FONT independent of the Java version.
   * @return
   */
  public static Font scaleFontForCustom(final Font font) {

    if (font != null) {
      return font.deriveFont(font.getSize2D() * (float) GUISCALINGFACTOR_REALCOMPONENTSIZE);
    }
    return null;
  }


  /**
   * @param font A font instance (Or null. Returns null.) whose size is just a constant number in your source code, and which you want to use in an already
   *             normalized custom graphics context (meaning you used GUIScaling.GUIScalingCustomGraphics on it.) This font has to be scaled simply by the
   *             derived System scaling factor to look correct. This method does that.
   * @return
   */
  public static Font scaleMagicConstantFontForCustom(final Font font) {

    if (font != null) {
      return font.deriveFont(font.getSize2D() * (float) GUISCALINGFACTOR_SYSTEM);
    }
    return null;
  }


  /**
   * For Java 9, but can blindly be used in Java 8, too. Ensures that the scaling of a paint(Component)()'s Graphics2D object is 1. Conveniently does the
   * usual casting, too.
   * <p>
   * Also calculates the physical pixel width/height of the component, which is reported differently on Java 9 if the display scaling is not 100%.
   */
  final public static class GUIScalingCustomGraphics {


    final public double guiScalingFactor_manualDrawing = GUISCALINGFACTOR_CUSTOMGRAPHICS;
    final public double guiScalingFactor_fontsThatLookCorrectInComponents = GUISCALINGFACTOR_FONTINCUSTOMGRAPHICSCONTEXT;

    final public Component component; // Just for convenience. You can hand the whole instance down your paint call hierarchy.
    final public int w; // The physical pixel width of the component.
    final public int h; // dto. height
    final public Graphics2D g; // Scale will be 1, even on Java 9 with a non-100% display scaling.


    /**
     * @param component NOT NULL. The component (e.g. JPanel or JFrame) whose paint() method you're overriding.
     * @param graphics  NOT NULL. The Graphics argument given to your paint() method.
     */
    public GUIScalingCustomGraphics(final Component component,
        final Graphics graphics) {

      this.component = component;
      w = scaleForRealComponentSize(component.getWidth());
      h = scaleForRealComponentSize(component.getHeight());

      g = (Graphics2D) graphics;
      final AffineTransform t = g.getTransform();
      final double xTrans = t.getTranslateX();
      final double yTrans = t.getTranslateY();
      t.setToScale(1, 1);
      t.translate(xTrans, yTrans);
      g.setTransform(t);
    }


    /**
     * @param graphics NOT NULL. The Graphics argument given to your paint() method.
     */
    public GUIScalingCustomGraphics(final Graphics graphics) {

      component = null;
      w = 0;
      h = 0;

      g = (Graphics2D) graphics;
      final AffineTransform t = g.getTransform();
      t.setToScale(1, 1);
      g.setTransform(t);
    }


    /**
     * @param x E.g. the width of a rectangle to be drawn.
     * @return x scaled so that it represents real physical pixels, compatible with Java 8 and 9.
     */
    public int scale(final double x) {

      return (int) Math.round(x * guiScalingFactor_manualDrawing);
    }


    /**
     * Scales the font and then sets it. Assumption: The given font looked correct in components. In Java 9, it would be TOO SMALL in custom graphics via
     * this class. This method fixes that.
     *
     * @param font NULL = null will be returned and G's font will not change.
     * @return returns the set font for convenience. NULL if your argument was null.
     */
    public Font setFont(final Font font) {

      final Font ret = font == null ? null : font.deriveFont(font.getSize2D() * (float) guiScalingFactor_fontsThatLookCorrectInComponents);
      g.setFont(ret);
      return ret;
    }


    @Override
    public String toString() {

      return "[GUIScalingCustomGraphics" +
          " guiScalingFactor_manualDrawing=" + guiScalingFactor_manualDrawing +
          " w=" + w +
          " h=" + h +
          " component=" + component +
          " g=" + g +
          ']';
    }


    /**
     * Convenience method that saves you unnecessary calls and code clutter. In your live painting method, you'll probably check first if your component has
     * any pixels, aka if with AND height are at least 1. If they're not, you return before doing anything.
     * <p>
     * This method is a shortcut. Call this right at the beginning. If it returns true, you abort.
     */
    public boolean isSizeZero() {

      return w < 1 || h < 1;
    }

  }


  /**
   * A tiny rectangle will be filled with the current color and width/height of HAIRLINETHICKNESS.
   */
  public static void drawPixel(final Graphics2D g,
      final int x,
      final int y) {

    g.fillRect(x - UIScalingUtil.HAIRLINETHICKNESS_HALF,
        y - UIScalingUtil.HAIRLINETHICKNESS_HALF,
        UIScalingUtil.HAIRLINETHICKNESS,
        UIScalingUtil.HAIRLINETHICKNESS);
  }


  final public static double JBUTTONFONTSIZE_ON_100PERCENTSCALE_JAVA8_W10_WITH_LOOKANDFEEL_WINDOWSORSYSTEMORXPLATFORMORWINCLASSIC = 11.0;
  final public static float JBUTTONFONTSIZE_ON_UNKNOWNSCALE_UNKNOWNJAVA_UNKNOWNOS_WITH_LOOKANDFEEL_WINDOWSORSYSTEMORXPLATFORMORWINCLASSIC;

  final public static double GUISCALINGFACTOR_SYSTEM; // The scaling set in the system.
  final public static double GUISCALINGFACTOR_COMPONENTSANDFONTS; // The scaling necessary if you set component/font sizes yourself.
  final public static double GUISCALINGFACTOR_CUSTOMGRAPHICS; // The scaling necessary if you want your custom graphics, too, to be scaled according to System settings.
  final public static double GUISCALINGFACTOR_REALCOMPONENTSIZE; // The factor by which getWidth() and such return values have to be multiplied, because Java 9 reports them differently.
  final public static double GUISCALINGFACTOR_FONTINCUSTOMGRAPHICSCONTEXT; // (This is exactly the custom graphics scaling probed by this class.) The factor by which a proper looking font would have to be scaled when used in custom graphics whose scale is 1. (Java 9 pre-scales it to e.g. 2 if Desktop is at 200%, then you reset that with the class above. Then the fonts that look right in the components will be TOO SMALL in the custom graphics. Use this factor / the method above to fix that.)


  final public static int HAIRLINETHICKNESS; // A "hairline" would be a line with pixel thickness 1. However, if the GUI is scaled up, that should be a little thicker. This value will have a value of AT LEAST 1. If the GUI scaling of the system is 200%, the value will be 2. If it's 175%, the value will ALSO be 2 due to rounding.

  final public static int HAIRLINETHICKNESS_HALF;
  final public static double HAIRLINETHICKNESS_HALF_DOUBLE;


  static {

    if (!SwingUtilities.isEventDispatchThread()) { // This also makes sure an obfuscator doesn't remove this method and its calls.
      throw new IllegalStateException("Must be initialized on Swing thread!");
    }

    LOG.debug("Initializing GUI scaling ...");

    UIScalingUtil.setLookAndFeelDefault();

    JBUTTONFONTSIZE_ON_UNKNOWNSCALE_UNKNOWNJAVA_UNKNOWNOS_WITH_LOOKANDFEEL_WINDOWSORSYSTEMORXPLATFORMORWINCLASSIC =
        new JButton().getFont().getSize2D();  // 21.0 on 200% desktop on Java 8 // 11.0 on 100% desktop on Java 8

    final Integer[] paintScalingInPercent = new Integer[1];

    final JDialog bruteForceJava9ScalingCheck = new JDialog((Frame) null, "", true) {

      {
        //                setLocation(-1000, -1000); // Outamysight!
        setLocation(100,
            100); // else you might have compatibility problems (see stackoverflow where I posted this class)

        final Runnable fallbackInCaseOlderJavaVersionDoesNotEndUpClosingThisWindow = () -> {
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          SwingUtilities.invokeLater(() -> {
            paintScalingInPercent[0] = 100;
            dispose();
          });
        };
        final Thread t = new Thread(fallbackInCaseOlderJavaVersionDoesNotEndUpClosingThisWindow);
        t.setDaemon(true);
        t.setName("GUI scaling detector fallback thread");
        t.start();
      }

      @Override
      public void paint(final Graphics graphics) {

        final Graphics2D g = (Graphics2D) graphics;
        final AffineTransform originalTransform = g.getTransform();
        paintScalingInPercent[0] = (int) Math.round(originalTransform.getScaleX() * 100);
        dispose();
      }
    };

    bruteForceJava9ScalingCheck.setVisible(true); // This call blocks until dispose() is reached.

    if (paintScalingInPercent[0] == null) {

      throw new Error("Unexpected behavior: Modal dialog did not block!");

    } else if (paintScalingInPercent[0] != 100) {

      // Must be Java 9 (or newer?).

      GUISCALINGFACTOR_SYSTEM = paintScalingInPercent[0] * 0.01;
      GUISCALINGFACTOR_COMPONENTSANDFONTS = 1; // Java 9 does everything. The developer's considerations are made unnecessary/harmless by this "1".
      GUISCALINGFACTOR_CUSTOMGRAPHICS = GUISCALINGFACTOR_SYSTEM;
      GUISCALINGFACTOR_FONTINCUSTOMGRAPHICSCONTEXT = GUISCALINGFACTOR_SYSTEM;

    } else {

      // Either Java 8 (or older?) or scaling IS just at the normal 1 (100).

      final double factorPreliminary = JBUTTONFONTSIZE_ON_UNKNOWNSCALE_UNKNOWNJAVA_UNKNOWNOS_WITH_LOOKANDFEEL_WINDOWSORSYSTEMORXPLATFORMORWINCLASSIC / JBUTTONFONTSIZE_ON_100PERCENTSCALE_JAVA8_W10_WITH_LOOKANDFEEL_WINDOWSORSYSTEMORXPLATFORMORWINCLASSIC;
      //            System.err.println("FIX MEEEEEEEEEEEEEEEEEEEEEEEEEEEEE!");
      //            final double factorPreliminary = 1;

      // If we just divide the two, we get 1.454545... on a 150% desktop, because the font sizes
      // chosen by Java are integer values, so we experience a rounding error.
      // The crappy but probably in most cases nicely working solution is: We round the result to .25 steps!

      GUISCALINGFACTOR_SYSTEM = Math.round(factorPreliminary * 4) / 4d;
      GUISCALINGFACTOR_COMPONENTSANDFONTS = GUISCALINGFACTOR_SYSTEM;
      GUISCALINGFACTOR_CUSTOMGRAPHICS = GUISCALINGFACTOR_SYSTEM;
      GUISCALINGFACTOR_FONTINCUSTOMGRAPHICSCONTEXT = 1; // No font scaling, the fonts used will look correct in custom scaling without extra treatment.
    }

    GUISCALINGFACTOR_REALCOMPONENTSIZE = GUISCALINGFACTOR_CUSTOMGRAPHICS / GUISCALINGFACTOR_COMPONENTSANDFONTS;


    HAIRLINETHICKNESS = (int) Math.max(1, Math.round(GUISCALINGFACTOR_CUSTOMGRAPHICS));
    HAIRLINETHICKNESS_HALF_DOUBLE = HAIRLINETHICKNESS * 0.5;
    HAIRLINETHICKNESS_HALF = (int) Math.round(HAIRLINETHICKNESS_HALF_DOUBLE);

    //        System.err.println("GUISCALINGFACTOR_SYSTEM             = " + GUISCALINGFACTOR_SYSTEM);
    //        System.err.println("GUISCALINGFACTOR_COMPONENTSANDFONTS = " + GUISCALINGFACTOR_COMPONENTSANDFONTS);
    //        System.err.println("GUISCALINGFACTOR_CUSTOMGRAPHICS     = " + GUISCALINGFACTOR_CUSTOMGRAPHICS);
    //        System.err.println("GUISCALINGFACTOR_REALCOMPONENTSIZE  = " + GUISCALINGFACTOR_REALCOMPONENTSIZE);

   LOG.debug("... done.");

  }

}
