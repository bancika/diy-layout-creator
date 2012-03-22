package org.diylc.components.semiconductors;

import java.awt.Shape;

import org.diylc.components.AbstractLeadedComponent;

public abstract class AbstractDiodeSymbol extends AbstractLeadedComponent<String> {

	private static final long serialVersionUID = 1L;

	@Override
	protected boolean shouldShadeLeads() {
		return false;
	}
	
	@Override
	protected Shape getBodyShape() {	
		return null;
	}
}
