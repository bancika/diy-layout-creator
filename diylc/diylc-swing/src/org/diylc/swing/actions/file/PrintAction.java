package org.diylc.swing.actions.file;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Files;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.diylc.common.ITask;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.images.IconLoader;
import org.diylc.swingframework.IDrawingProvider;
import org.diylc.swingframework.export.DrawingExporter;

public class PrintAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private IDrawingProvider drawingProvider;
    private ISwingUI swingUI;

    public PrintAction(IDrawingProvider drawingProvider, ISwingUI swingUI, int keyModifiers) {
      super();
      this.drawingProvider = drawingProvider;
      this.swingUI = swingUI;
      putValue(AbstractAction.NAME, "Print...");
      putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, keyModifiers));
      putValue(AbstractAction.SMALL_ICON, IconLoader.Print.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      ActionFactory.LOG.info("PrintAction triggered");
      swingUI.executeBackgroundTask(new ITask<Void>() {

        @Override
        public Void doInBackground() throws Exception {
          File tempPdf = Files.createTempFile("diylc", ".pdf").toFile();
          DrawingExporter.getInstance().exportPDF(PrintAction.this.drawingProvider, tempPdf);
          
//          PDDocument document = PDDocument.load(tempPdf);
//          PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
//          PrinterJob job = PrinterJob.getPrinterJob();
//          PDFPageable pdfPageable = new PDFPageable(document);        
//          job.setPageable(pdfPageable);
//          job.setPrintService(printService);
//          job.print();
          DrawingExporter.getInstance().print(PrintAction.this.drawingProvider);
          return null;
        }

        @Override
        public void complete(Void result) {}

        @Override
        public void failed(Exception e) {
          swingUI.showMessage("Could not print. " + e.getMessage(), "Error",
              ISwingUI.ERROR_MESSAGE);
        }
      }, true);          
    }
  }