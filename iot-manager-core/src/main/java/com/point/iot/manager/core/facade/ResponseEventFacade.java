package com.point.iot.manager.core.facade;

import java.util.Map;

import com.tongqu.game.manager.request.bean.ResponseEvent;


public interface ResponseEventFacade {
	Map<Integer, ResponseEventLogicHandler<? extends ResponseEvent>> getFacadeMap();
}
