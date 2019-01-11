package org.diylc;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.SplashScreen;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.diylc.images.IconLoader;

public class DIYLCSplash {
  
  private Thread t;

  public DIYLCSplash(final SplashScreen splash) {
    if (splash == null)
      return;
    final Graphics2D g = splash.createGraphics();
    if (g == null)
      return;
    
    t = new Thread(new Runnable() {

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
  }
  
  public void start() {
    if (t != null)
      t.start();
  }

  private ImageIcon resistor = null;

  public ImageIcon getResistor() {
    if (resistor == null) {
      resistor = (ImageIcon) IconLoader.SplashResistor.getIcon();
    }
    return resistor;
  }

  private ImageIcon film = null;

  public ImageIcon getFilm() {
    if (film == null) {
      film = (ImageIcon) IconLoader.SplashFilm.getIcon();
    }
    return film;
  }

  private ImageIcon ceramic = null;

  public ImageIcon getCeramic() {
    if (ceramic == null) {
      ceramic = (ImageIcon) IconLoader.SplashCeramic.getIcon();
    }
    return ceramic;
  }

  private ImageIcon electrolytic = null;

  public ImageIcon getElectrolytic() {
    if (electrolytic == null) {
      electrolytic = (ImageIcon) IconLoader.SplashElectrolytic.getIcon();
    }
    return electrolytic;
  }

  private ImageIcon splash = null;

  public ImageIcon getSplash() {
    if (splash == null) {
      splash = (ImageIcon) IconLoader.Splash.getIcon();
    }
    return splash;
  }

  private Point resistorTarget = new Point(112, 114);
  private Point filmTarget = new Point(233, 113);
  private Point electrolyticTarget = new Point(261, 23);
  private Point ceramicTarget = new Point(352, 22);

  private int pxPerFrame = 3;

  public void renderSplashFrame(SplashScreen splash, Graphics2D g, int frame) {
    g.setComposite(AlphaComposite.Clear);
    getSplash().paintIcon(null, g, 0, 0);
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f - frame * 0.007f));
    getResistor().paintIcon(null, g, resistorTarget.x - pxPerFrame * frame, resistorTarget.y);
    getFilm().paintIcon(null, g, filmTarget.x, filmTarget.y + pxPerFrame * frame);
    getElectrolytic().paintIcon(null, g, electrolyticTarget.x, electrolyticTarget.y - pxPerFrame * frame);
    getCeramic().paintIcon(null, g, ceramicTarget.x + pxPerFrame * frame, ceramicTarget.y);
  }
}
