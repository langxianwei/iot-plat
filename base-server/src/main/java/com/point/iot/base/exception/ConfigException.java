package com.point.iot.base.exception;
public class ConfigException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3966083565047494636L;
	public ConfigException(String msg){
		super(msg);
	}
	public ConfigException(Exception e){
		super(e);
	}
	public ConfigException(String msg, Exception e){
		super(msg, e);
	}
}
