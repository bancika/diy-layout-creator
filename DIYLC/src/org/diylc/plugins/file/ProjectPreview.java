package org.diylc.plugins.file;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.EnumSet;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.diylc.common.BadPositionException;
import org.diylc.common.DrawOption;
import org.diylc.common.IPlugInPort;
import org.diylc.core.Project;
import org.diylc.gui.IView;
import org.diylc.presenter.Presenter;

import com.diyfever.gui.IFileChooserAccessory;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * {@link JComponent} that shows preview of the selected project in
 * {@link JFileChooser}. It's hooked onto {@link JFileChooser} as
 * {@link PropertyChangeListener} and refreshes when file is selected.
 * 
 * @author Branislav Stojkovic
 */
public class ProjectPreview extends JPanel implements PropertyChangeListener, IFileChooserAccessory {

	private static final long serialVersionUID = 1L;

	private IPlugInPort presenter;
	private XStream xStream;
	private Project emptyProject;
	private RenderComponent renderComponent;
	private JLabel nameLabel;

	public ProjectPreview() {
		super();

		setPreferredSize(new Dimension(140, 128));
		presenter = new Presenter(new IView() {

			@Override
			public void addComponent(JComponent component, int position)
					throws BadPositionException {
			}

			@Override
			public void addMenuAction(Action action, String menuName) {
			}

			@Override
			public void addSubmenu(String name, Icon icon, String parentMenuName) {
			}

			@Override
			public int showConfirmDialog(String message, String title, int optionType,
					int messageType) {
				return 0;
			}

			@Override
			public void showMessage(String message, String title, int messageType) {
			}
		});
		xStream = new XStream(new DomDriver());

		emptyProject = new Project();
		emptyProject.setTitle("");

		renderComponent = new RenderComponent();
		nameLabel = new JLabel();

		// setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(renderComponent);
		add(nameLabel);
	}

	// PropertyChangeListener

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		boolean update = false;
		String prop = evt.getPropertyName();

		Project selectedProject = emptyProject;
		if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
			update = true;
		} else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
			File selectedFile = (File) evt.getNewValue();
			try {
				FileInputStream in = new FileInputStream(selectedFile);
				selectedProject = (Project) xStream.fromXML(in);
				in.close();
			} catch (Exception e) {
			}
			update = true;
		}

		nameLabel.setText(selectedProject.getTitle());
		presenter.loadProject(selectedProject, true);

		if (update) {
			if (renderComponent.isShowing()) {
				renderComponent.repaint();
			}
		}
	}

	class RenderComponent extends JComponent {

		private static final long serialVersionUID = 1L;

		public RenderComponent() {
			setPreferredSize(new Dimension(128, 96));
			// setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		}

		@Override
		public void paint(Graphics g) {
			super.paint(g);

			Graphics2D g2d = (Graphics2D) g;
			Dimension d = presenter.getCanvasDimensions(false);
			// System.out.println(d);
			Rectangle rect = getBounds();
			// System.out.println(rect);

			double projectRatio = d.getWidth() / d.getHeight();
			double actualRatio = rect.getWidth() / rect.getHeight();
			double zoomRatio;
			if (projectRatio > actualRatio) {
				zoomRatio = rect.getWidth() / d.getWidth();
			} else {
				zoomRatio = rect.getHeight() / d.getHeight();
			}
			// d.setSize(d.getWidth() * zoomRatio, d.getHeight() * zoomRatio);
			// int x = (int) (rect.getWidth() - d.getWidth() * zoomRatio) / 2;
			// int y = (int) (rect.getHeight() - d.getHeight() * zoomRatio) / 2;

			// System.out.println(x + "," + y);

			// g2d.translate(x, y);
			g2d.scale(zoomRatio, zoomRatio);
			presenter.draw(g2d, EnumSet.noneOf(DrawOption.class), null);

			g2d.setColor(Color.black);
			g2d.drawRect(0, 0, d.width - (int) (1 / zoomRatio), d.height - (int) (1 / zoomRatio));
		}
	}

	// IFileChooserAccessory

	@Override
	public void install(JFileChooser fileChooser) {
		fileChooser.setAccessory(this);
		fileChooser.addPropertyChangeListener(this);
	}
}
