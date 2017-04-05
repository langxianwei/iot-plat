package com.point.iot.manager.core.facade;

import com.tongqu.game.manager.request.bean.ResponseEvent;

public interface ResponseEventLogicHandler<REQ extends ResponseEvent> {
	void doExec(REQ req);
}
