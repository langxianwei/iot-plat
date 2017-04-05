package com.point.iot.mina.msg;

/**
 * 如果消息格式错误，则抛出这个异常
 * @author langxianwei
 *
 */
public class MsgFormatException extends Exception {


	/**
	 * 
	 */
	private static final long serialVersionUID = 6128288681001333893L;

	public MsgFormatException(String message) {
		super(message);
	}
}
