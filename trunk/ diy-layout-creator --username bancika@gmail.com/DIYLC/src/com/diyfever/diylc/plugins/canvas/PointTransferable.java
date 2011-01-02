package com.diyfever.diylc.plugins.canvas;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class PointTransferable implements Transferable {

	public static final DataFlavor pointFlavor = new DataFlavor(Point.class, "Java Point");

	private Point startPoint;

	public PointTransferable(Point startPoint) {
		super();
		this.startPoint = startPoint;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals(pointFlavor)) {
			return startPoint;
		}
		throw new UnsupportedFlavorException(flavor);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { pointFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(pointFlavor);
	}

}
