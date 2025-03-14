package org.diylc.common;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.EnumSet;

public abstract class AbstractPlugin implements IPlugIn {
    
    protected IPlugInPort plugInPort;
    
    @Override
    public void connect(IPlugInPort plugInPort) {
        this.plugInPort = plugInPort;
    }

    @Override
    public abstract EnumSet<EventType> getSubscribedEventTypes();

    @Override
    public abstract void processMessage(EventType eventType, Object... params);
} 