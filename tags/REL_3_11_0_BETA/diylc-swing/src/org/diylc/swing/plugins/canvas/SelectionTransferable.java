package org.diylc.swing.plugins.canvas;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import org.diylc.core.IDIYComponent;


public class SelectionTransferable implements Transferable {

	public static final DataFlavor selectionFlavor = new DataFlavor(List.class,
			"Component Selection");

	private Point startPoint;
	private List<IDIYComponent<?>> selection;

	public SelectionTransferable(List<IDIYComponent<?>> selection, Point startPoint) {
		super();
		this.selection = selection;
		this.startPoint = startPoint;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals(selectionFlavor)) {
			return selection;
		}
		if (flavor.equals(PointTransferable.pointFlavor)) {
			return startPoint;
		}
		throw new UnsupportedFlavorException(flavor);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { selectionFlavor, PointTransferable.pointFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(selectionFlavor) || flavor.equals(selectionFlavor);
	}
}
