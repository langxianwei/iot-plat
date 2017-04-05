package com.point.iot.manager.core.exception;
/**
 * @author langxianwei
 * 
 */
public class SerializeException extends IotManagerExcption {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6367689805408879573L;

	public SerializeException(Exception e) {
		super(e);
	}
	public SerializeException(String msg) {
		super(msg);
	}
	public SerializeException(String msg, Exception e) {
		super(msg, e);
	}
}
