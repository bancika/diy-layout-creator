package org.diylc;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.diylc.swing.images.IconLoader;

/**
 * Custom splash screen with animated components. Does not use standard SplashScreen because of issues with OS X and Java 8.
 * 
 * @author bancika
 *
 */
public class DIYLCSplash extends JDialog {

	private static final long serialVersionUID = 1L;
	private Thread t;
	
	private int frameNumber = 90;
	
	private Point resistorTarget = new Point(112, 114);
	private Point filmTarget = new Point(233, 113);
	private Point electrolyticTarget = new Point(261, 23);
	private Point ceramicTarget = new Point(352, 22);

	private int pxPerFrame = 3;

	public DIYLCSplash() {
		setPreferredSize(new Dimension(getSplash().getIconWidth(), getSplash().getIconHeight()));
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setUndecorated(true);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setAlwaysOnTop(true);
        repaint();    

		t = new Thread(new Runnable() {

			@Override
			public void run() {
				for (int i = 90; i >= 0; i--) {
					if (!isVisible())
						return;
					frameNumber = i;
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {							
							repaint();
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
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setComposite(AlphaComposite.SrcOver);
		getSplash().paintIcon(null, g, 0, 0);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f - frameNumber * 0.007f));
		getResistor().paintIcon(null, g, resistorTarget.x - pxPerFrame * frameNumber, resistorTarget.y);
		getFilm().paintIcon(null, g, filmTarget.x, filmTarget.y + pxPerFrame * frameNumber);
		getElectrolytic().paintIcon(null, g, electrolyticTarget.x, electrolyticTarget.y - pxPerFrame * frameNumber);
		getCeramic().paintIcon(null, g, ceramicTarget.x + pxPerFrame * frameNumber, ceramicTarget.y);
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

	private ImageIcon getFilm() {
		if (film == null) {
			film = (ImageIcon) IconLoader.SplashFilm.getIcon();
		}
		return film;
	}

	private ImageIcon ceramic = null;

	private ImageIcon getCeramic() {
		if (ceramic == null) {
			ceramic = (ImageIcon) IconLoader.SplashCeramic.getIcon();
		}
		return ceramic;
	}

	private ImageIcon electrolytic = null;

	private ImageIcon getElectrolytic() {
		if (electrolytic == null) {
			electrolytic = (ImageIcon) IconLoader.SplashElectrolytic.getIcon();
		}
		return electrolytic;
	}

	private ImageIcon splash = null;

	private ImageIcon getSplash() {
		if (splash == null) {
			splash = (ImageIcon) IconLoader.Splash.getIcon();
		}
		return splash;
	}	
}
