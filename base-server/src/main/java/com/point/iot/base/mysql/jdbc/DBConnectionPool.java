package com.point.iot.base.mysql.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;

/**
 * @author chenyanlin
 * 
 */
public class DBConnectionPool {
	private Connection con = null;
	private int inUsed = 0; // 使用的连接数
	private ArrayList<Connection> freeConnections = new ArrayList<Connection>();// 容器，空闲连??
	private int minConn; // ????连接??
	private int maxConn; // ????连接
	private String name; // 连接池名??
	private String password; // 密码
	private String url; // 数据库连接地??
	private String driver; // 驱动
	private String user; // 用户??
	public Timer timer; // 定时

	public DBConnectionPool() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 创建连接??
	 * 
	 * @param driver
	 * @param name
	 * @param URL
	 * @param user
	 * @param password
	 * @param maxConn
	 */
	public DBConnectionPool(String name, String driver, String URL,
			String user, String password, int maxConn) {
		this.name = name;
		this.driver = driver;
		this.url = URL;
		this.user = user;
		this.password = password;
		this.maxConn = maxConn;
	}

	/**
	 * 用完，释放连??
	 * 
	 * @param con
	 */
	public synchronized void freeConnection(Connection con) {
		this.freeConnections.add(con);// 添加到空闲连接的末尾
		this.inUsed--;
	}

	/**
	 * timeout 根据timeout得到连接
	 * 
	 * @param timeout
	 * @return
	 */
	public synchronized Connection getConnection(long timeout) {
		Connection con = null;
		if (this.freeConnections.size() > 0) {
			con = (Connection) this.freeConnections.get(0);
			if (con == null)
				con = getConnection(timeout); // 继续获得连接
		} else {
			con = newConnection(); // 新建连接
		}
		if (this.maxConn == 0 || this.maxConn < this.inUsed) {
			con = null;// 达到????连接数，暂时不能获得连接了??
		}
		if (con != null) {
			this.inUsed++;
		}
		return con;
	}

	/**
	 * 
	 * 从连接池里得到连??
	 * 
	 * @return
	 */
	public Connection getConnection(){
		Connection con = null;
		if (this.freeConnections.size() > 0) {
			con = (Connection) this.freeConnections.get(0);
			this.freeConnections.remove(0);// 如果连接分配出去了，就从空闲连接里删??
			if (con == null)
				con = getConnection(); // 继续获得连接
		} else {
			con = newConnection(); // 新建连接
		}
		if (this.maxConn == 0 || this.maxConn < this.inUsed) {
			con = null;// 等待 超过????连接??
		}
		if (con != null) {
			this.inUsed++;
		}
		return con;
	}

	/**
	 *释放全部连接
	 * 
	 */
	public synchronized void release() {
		Iterator<Connection> allConns = this.freeConnections.iterator();
		while (allConns.hasNext()) {
			Connection con = (Connection) allConns.next();
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
		this.freeConnections.clear();

	}

	/**
	 * 创建新连??
	 * 
	 * @return
	 */
	private Connection newConnection() {
		try {
			Class.forName(driver);
			con = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("sorry can't find db driver!");
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.out.println("sorry can't create Connection!");
		}
		return con;

	}

	/**
	 * 定时处理函数
	 */
	public synchronized void TimerEvent() {
		// 暂时还没有实现以后会加上??
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

	/**
	 * @return the driver
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * @param driver
	 *            the driver to set
	 */
	public void setDriver(String driver) {
		this.driver = driver;
	}

	/**
	 * @return the maxConn
	 */
	public int getMaxConn() {
		return maxConn;
	}

	/**
	 * @param maxConn
	 *            the maxConn to set
	 */
	public void setMaxConn(int maxConn) {
		this.maxConn = maxConn;
	}

	/**
	 * @return the minConn
	 */
	public int getMinConn() {
		return minConn;
	}

	/**
	 * @param minConn
	 *            the minConn to set
	 */
	public void setMinConn(int minConn) {
		this.minConn = minConn;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}
}
