package org.diylc.swing.actions.file;

import com.jsevy.jdxf.DXFDocument;
import com.jsevy.jdxf.DXFGraphics;
import com.orsonpdf.PDFDocument;
import com.orsonpdf.PDFGraphics2D;
import com.orsonpdf.PDFHints;
import com.orsonpdf.Page;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.plugins.file.FileFilterEnum;
import org.diylc.swingframework.IDrawingProvider;
import org.diylc.swingframework.export.DrawingExporter;
import org.diylc.utils.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;

public class ExportDXFAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private static final int SCREEN_RESOLUTION = Toolkit.getDefaultToolkit().getScreenResolution();

  private IDrawingProvider drawingProvider;
  private ISwingUI swingUI;
  private IPlugInPort plugInPort;
  private String defaultSuffix;

  public ExportDXFAction(IPlugInPort plugInPort, IDrawingProvider drawingProvider,
      ISwingUI swingUI, String defaultSuffix) {
    super();
    this.plugInPort = plugInPort;
    this.drawingProvider = drawingProvider;
    this.swingUI = swingUI;
    this.defaultSuffix = defaultSuffix;
    putValue(AbstractAction.NAME, "Export to DXF");
    putValue(AbstractAction.SMALL_ICON, IconLoader.PDF.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("ExportDXFAction triggered");

    File initialFile = null;
    String currentFile = plugInPort.getCurrentFileName();
    if (currentFile != null) {
      File cFile = new File(currentFile);
      initialFile =
          new File(cFile.getName().replaceAll("(?i)\\.diy", "") + defaultSuffix + ".dxf");
    }

    final File file = DialogFactory.getInstance().showSaveDialog(swingUI.getOwnerFrame(),
        FileFilterEnum.PDF.getFilter(), initialFile, FileFilterEnum.DXF.getExtensions()[0], null);
    if (file != null) {
      swingUI.executeBackgroundTask(new ITask<Void>() {

        @Override
        public Void doInBackground() throws Exception {
          ActionFactory.LOG.debug("Exporting to " + file.getAbsolutePath());

          DXFDocument dxfDocument = new
              DXFDocument(file.getName());
          DXFGraphics dxfGraphics =
              dxfDocument.getGraphics();

          Dimension d = drawingProvider.getSize();
          double factor = 1;//(double)(72.0F / (float)SCREEN_RESOLUTION);

          for(int i = 0; i < drawingProvider.getPageCount(); ++i) {
            dxfGraphics.setRenderingHint(PDFHints.KEY_DRAW_STRING_TYPE, PDFHints.VALUE_DRAW_STRING_TYPE_VECTOR);
            drawingProvider.draw(i, dxfGraphics, factor);
          }

          String dxfText = dxfDocument.toDXFString();
          FileWriter fileWriter = new FileWriter(file.getAbsolutePath());
          fileWriter.write(dxfText);
          fileWriter.flush();
          fileWriter.close();

          return null;
        }

        @Override
        public void complete(Void result) {}

        @Override
        public void failed(Exception e) {
          swingUI.showMessage("Could not export to DXF. " + e.getMessage(), "Error",
              ISwingUI.ERROR_MESSAGE);
        }
      }, true);
    }
  }
}
