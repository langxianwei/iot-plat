package com.point.iot.base.mysql.jdbc;

import java.sql.Types;

public class FunctionParam {
	/**
	 * 输入输出类型
	 */
	private String inputType;
	/**
	 * 值
	 */
	private Object value;
	/**
	 * 参数位置
	 */
	private int idx;
	/**
	 * 值类型
	 */
	private int types;
	
	public FunctionParam(int idx, Object value, String inputType, int types){
		if ( inputType == null || "".equals(inputType)){
			inputType = "in";
		}
		this.inputType = inputType;
		this.value = value;
		this.idx = idx;
		this.types = types;
	}

	public int getTypes() {
		return types;
	}

	public void setTypes(int types) {
		this.types = types;
	}

	public String getInputType() {
		return inputType;
	}

	public void setInputType(String inputType) {
		this.inputType = inputType;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public int getIdx() {
		return idx;
	}

	public void setIdx(int idx) {
		this.idx = idx;
	}
	
}
