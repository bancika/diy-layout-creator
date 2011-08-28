package org.diylc.components.passive;

public enum Voltage {

	_16V, _25V, _63V, _100V, _160V, _250V, _300V, _350V, _400V, _500V, _630V, _1KV;
	
	@Override
	public String toString() {
		return name().replace("_", "");
	}
}
