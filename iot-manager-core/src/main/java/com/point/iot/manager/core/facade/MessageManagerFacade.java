package com.point.iot.manager.core.facade;

import java.util.Map;

/**
 * @author langxianwei
 * 
 */
public interface MessageManagerFacade {
	Map<Integer, MessageManagerLogicHandler> getFacadeMap();
}
