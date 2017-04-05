package com.yahe.plat.protocol.plugin.tcp;

public class TcpNetCmd {
	/**
	 * 请求
	 */
    public static final int REQ = 0x8f;
    /**
     * 应答
     */
    public static final int ACK = 0x0f;
    /**
     * 登录消息
     */
	public static final int LOGIN_ID = 0x00;
	/**
	 * 召测数据
	 */
	public static final int READ_DATA_ID = 0x04;
	/**
	 * 时间设置
	 */
	public static final int SET_TIME_ID = 0x03;
	/**
	 * 退出登录
	 */
	public static final int LOGOUT_ID = 0x01;
	/**
	 * 参数设置
	 */
	public static final int SET_PARAM_ID = 0x02;
}
