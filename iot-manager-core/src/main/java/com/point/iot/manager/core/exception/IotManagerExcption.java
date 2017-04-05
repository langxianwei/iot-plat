package com.point.iot.manager.core.exception;
/**
 * @author langxianwei
 * 
 */
public class IotManagerExcption extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1744462522709860968L;
	public IotManagerExcption(String msg){
		super(msg);
	}
	public IotManagerExcption(Exception e){
		super(e);
	}
	public IotManagerExcption(String msg, Exception e){
		super(msg, e);
	}
}
