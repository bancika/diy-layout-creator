package com.diyfever.diylc.common;

import com.diyfever.gui.simplemq.IMessageListener;

/**
 * Interface for plug-ins.
 * 
 * @author Branislav Stojkovic
 */
public interface IPlugIn extends IMessageListener<EventType> {

	/**
	 * Method that connects the plug-in with {@link IPlugInPort}. Called by the
	 * application when plug-in is installed.
	 * 
	 * @param plugInPort
	 */
	void connect(IPlugInPort plugInPort);

}
