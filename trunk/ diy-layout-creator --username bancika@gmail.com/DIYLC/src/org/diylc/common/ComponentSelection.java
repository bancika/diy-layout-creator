package org.diylc.common;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.diylc.core.IComponentInstance;


/**
 * Represents component selection as a {@link List} of
 * {@link IComponentInstance} objects. Implements {@link Transferable}, so it is
 * suitable for clipboard usage.
 * 
 * @author Branislav Stojkovic
 */
public class ComponentSelection extends ArrayList<IComponentInstance> implements Transferable {

	private static final long serialVersionUID = 1L;

	public static final DataFlavor listFlavor = new DataFlavor(List.class, "Java List");

	public ComponentSelection() {
		super();
	}

	public ComponentSelection(Collection<IComponentInstance> selectedComponents) {
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
