package org.diylc.swing.plugins.clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.diylc.core.IDIYComponent;

/**
 * Represents component selection as a {@link List} of {@link IDIYComponent}
 * objects. Implements {@link Transferable}, so it is suitable for clipboard
 * usage.
 * 
 * @author Branislav Stojkovic
 */
public class ComponentTransferable extends ArrayList<IDIYComponent<?>> implements Transferable {

	private static final long serialVersionUID = 1L;

	public static final DataFlavor listFlavor = new DataFlavor(ComponentTransferable.class, "application/diylc");

	public ComponentTransferable() {
		super();
	}

	public ComponentTransferable(Collection<IDIYComponent<?>> selectedComponents) {
		super(selectedComponents);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals(listFlavor)) {
			return this;
		}
		throw new UnsupportedFlavorException(flavor);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { listFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(listFlavor);
	}
}
