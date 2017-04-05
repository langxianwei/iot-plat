package com.point.iot.manager.core.exception;
/**
 * @author langxianwei
 * 
 */
public class DeSerializeException extends IotManagerExcption {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5764676495038460919L;

	public DeSerializeException(String msg) {
		super(msg);
	}
	public DeSerializeException(Exception e){
		super(e);
	}
	public DeSerializeException(String msg, Exception e){
		super(msg, e);
	}
}
