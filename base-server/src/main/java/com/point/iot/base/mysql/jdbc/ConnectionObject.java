package com.point.iot.base.mysql.jdbc;

import java.sql.Connection;
import java.util.Date;

/**
 * 单个连接对象信息
 * 
 * @author Leolian
 * @version 1.0
 */
public class ConnectionObject {
	private boolean isUse = false;
	private Connection conn = null;
	// 用户最后一次访问该连接方法的时间
	private long lastAccessTime = new Date().getTime();

	public ConnectionObject() {

	}

	/**
	 * 设置连接对象
	 * 
	 * @param conn
	 */
	public void setConnection(Connection conn) {
		this.conn = conn;
		this.isUse = false;
	}

	/**
	 * 获得连接对象
	 * 
	 * @return
	 */
	public Connection getConnection() {
		if (!isUse()) {
			if (null == this.conn) {
				return null;
			} else {
				setUse(true);
				lastAccessTime = new Date().getTime();
				return this.conn;
			}
		} else {
			return null;
		}
	}

	public boolean isUse() {
		return isUse;
	}

	public void setUse(boolean isUse) {
		this.isUse = isUse;
	}

	/**
	 * 关闭连接对象
	 * 
	 */
	synchronized public void close() {
		if (!isClosed()) {
			try {
				conn.close();
			} catch (Exception ex) {

			}
		}
	}

	/**
	 * 连接对象是否关闭
	 * 
	 * @return
	 */
	public boolean isClosed() {
		try {
			return conn.isClosed() ? true : false;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * 返回最后使用时间
	 * 
	 * @return
	 */
	public long getLastAccessTime() {
		return this.lastAccessTime;
	}
}
