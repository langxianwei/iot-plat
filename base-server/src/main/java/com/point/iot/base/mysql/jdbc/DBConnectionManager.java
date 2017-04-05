package com.point.iot.base.mysql.jdbc;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.point.iot.base.mysql.jdbc.DSConfigBean;
import com.point.iot.base.mysql.jdbc.ParseDSConfig;
import com.point.iot.utils.FilePathUtils;

/**
 * 管理类DBConnectionManager 支持对一个或多个由属性文件定义的数据库连接
 * 池的访问.客户程序可以调用getInstance()方法访问本类的唯一实例.
 */
public class DBConnectionManager {
	static Logger logger = Logger.getLogger(DBConnectionManager.class);
	static private DBConnectionManager instance; // 唯一实例
	static private int clients;

	private Vector drivers = new Vector();
	private PrintWriter log;
	private Hashtable pools = new Hashtable();

	/**
	 * 返回唯一实例.如果是第一次调用此方法,则创建实例
	 * 
	 * @return DBConnectionManager 唯一实例
	 */
	static synchronized public DBConnectionManager getInstance() {
		if (instance == null) {
			logger.debug("对象为空，重新获取！");
			instance = new DBConnectionManager();
		}
		clients++;
		return instance;
	}

	/**
	 * 建构函数私有以防止其它对象创建本类实例
	 */
	private DBConnectionManager() {
		init();
	}

	/**
	 * 将连接对象返回给由名字指定的连接池
	 * 
	 * @param name
	 *            在属性文件中定义的连接池名字
	 * @param con
	 *            连接对象
	 */
	public void freeConnection(String name, Connection con) {
		DBConnectionPool pool = (DBConnectionPool) pools.get(name);
		if (pool != null) {
			pool.freeConnection(con);
		}
	}

	/**
	 * 获得一个可用的(空闲的)连接.如果没有可用连接,且已有连接数小于最大连接数 限制,则创建并返回新连接
	 * 
	 * @param name
	 *            在属性文件中定义的连接池名字
	 * @return Connection 可用连接或null
	 */
	public Connection getConnection(String name) {
		DBConnectionPool pool = (DBConnectionPool) pools.get(name);
		if (pool != null) {
			Connection connection = pool.getConnection();
			try {
				boolean closed = connection.isClosed();
				//如果连接池中的connection已经超时断开,则重新获取
				while (closed) {
					connection = pool.getConnection();
					closed = connection.isClosed();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return connection;
		}
		return null;
	}

	/**
	 * 获得一个可用连接.若没有可用连接,且已有连接数小于最大连接数限制, 则创建并返回新连接.否则,在指定的时间内等待其它线程释放连接.
	 * 
	 * @param name
	 *            连接池名字
	 * @param time
	 *            以毫秒计的等待时间
	 * @return Connection 可用连接或null
	 */
	public Connection getConnection(String name, long time) {
		DBConnectionPool pool = (DBConnectionPool) pools.get(name);
		if (pool != null) {
			return pool.getConnection(time);
		}
		return null;
	}

	/**
	 * 关闭所有连接,撤销驱动程序的注册
	 */
	public synchronized void release() {
		// 等待直到最后一个客户程序调用
		if (--clients != 0) {
			return;
		}

		Enumeration allPools = pools.elements();
		while (allPools.hasMoreElements()) {
			DBConnectionPool pool = (DBConnectionPool) allPools.nextElement();
			pool.release();
		}
		Enumeration allDrivers = drivers.elements();
		while (allDrivers.hasMoreElements()) {
			Driver driver = (Driver) allDrivers.nextElement();
			try {
				DriverManager.deregisterDriver(driver);
			} catch (SQLException e) {
			}
		}
	}

	/**
	 * 根据指定属性创建连接池实例.
	 * 
	 * @param props
	 *            连接池属性
	 */
	private void createPools(Vector driverBeans) {
		Iterator it = driverBeans.iterator();
		while (it.hasNext()) {
			DSConfigBean dsConfigBean = (DSConfigBean) it.next();
			String poolName = dsConfigBean.getName();
			String url = dsConfigBean.getUrl();
			String user = dsConfigBean.getUsername();
			String password = dsConfigBean.getPassword();
			int maxconn = dsConfigBean.getMaxconn();

			DBConnectionPool pool = new DBConnectionPool(poolName, url, user,
					password, maxconn);
			pools.put(poolName, pool);
		}
	}
	/**
	 * 是否部署到服务器
	 */
	public static final boolean IS_WINDOW = ((System.getProperties()
			.getProperty("os.name")).indexOf("Windows") >= 0);

	public static final boolean IS_MAC = ((System.getProperties()
			.getProperty("os.name")).indexOf("Mac") >= 0);

	private final static String WINDOW_PATH = "D:" + File.separator + "config"
			+ File.separator;

	private final static String LINUX_PATH = File.separator + "home"
			+ File.separator + "yhplat" + File.separator + "properties"
			+ File.separator;

	/**
	 * 读取属性完成初始化
	 */
	private void init() {
		ParseDSConfig pd = new ParseDSConfig();
		// 读取数据库配置文件
		Vector driverBeans = pd.readConfigInfo( "dsconfig.xml" );
		loadDrivers(driverBeans);
		createPools(driverBeans);
	}
	public static String getFullFilePath(String fileName) {
		if (IS_WINDOW) {
			return WINDOW_PATH + fileName;
		} else if (IS_MAC) {
			return FilePathUtils.getFilePath("") + fileName;
		} else {
			return LINUX_PATH + fileName;
		}
	}

	 /**
	 * 装载和注册所有JDBC 驱动程序
	 *
	 * @param props 属性
	 */
	private void loadDrivers(Vector driverBeans) {
		logger.debug("----------------------->");
		Iterator iterator = driverBeans.iterator();
		while (iterator.hasNext()) {
			DSConfigBean dsConfigBean = (DSConfigBean)iterator.next();
			try {
				if (dsConfigBean.getDriver() != null && !"".equals(dsConfigBean.getDriver())){
					Driver driver = (Driver) Class.forName(dsConfigBean.getDriver())
							.newInstance();
					DriverManager.registerDriver(driver);
					drivers.addElement(driver);
					logger.debug("成功注册JDBC 驱动程序" + dsConfigBean.getDriver());
				}
			} catch (Exception e) {
				logger.error("注册驱动程序出错,",e);
			}
		}
	}


	/**
	 * 将文本信息与异常写入日志文件
	 */
	private void log(Throwable e, String msg) {
		log.println(new Date() + ": " + msg);
		e.printStackTrace(log);
	}
	/**
	 * 此内部类定义了一个连接池.它能够根据要求创建新连接,直到预定的最 大连接数为止.在返回连接给客户程序之前,它能够验证连接的有效性.
	 */
	class DBConnectionPool {
		private int checkedOut;
		private Vector<Connection> freeConnections = new Vector<Connection>();
		private int maxConn;
		private String name;
		private String password;
		private String URL;
		private String user;

		/**
		 * 创建新的连接池
		 * 
		 * @param name
		 *            连接池名字
		 * @param URL
		 *            数据库的JDBC URL
		 * @param user
		 *            数据库帐号,或null
		 * @param password
		 *            密码,或null
		 * @param maxConn
		 *            此连接池允许建立的最大连接数
		 */
		public DBConnectionPool(String name, String URL, String user,
				String password, int maxConn) {
			this.name = name;
			this.URL = URL;
			this.user = user;
			this.password = password;
			this.maxConn = maxConn;
		}

		/**
		 * 将不再使用的连接返回给连接池
		 * 
		 * @param con
		 *            客户程序释放的连接
		 */
		public synchronized void freeConnection(Connection con) {
			// 将指定连接加入到向量末尾
//			logger.debug("释放链接" + name + ",url=" + this.URL);

			freeConnections.addElement(con);
			checkedOut--;
			notifyAll();
		}

		/**
		 * 从连接池获得一个可用连接.如没有空闲的连接且当前连接数小于最大连接 数限制,则创建新连接.如原来登记为可用的连接不再有效,则从向量删除之,
		 * 然后递归调用自己以尝试新的可用连接.
		 */
		public synchronized Connection getConnection() {
			Connection con = null;
			if (freeConnections.size() > 0) {
				// 获取向量中第一个可用连接
				con = (Connection) freeConnections.firstElement();
				freeConnections.removeElementAt(0);
				try {
					if (con == null || con.isClosed()) {
						System.out.println("从连接池" + name + "删除一个无效连接");
						// 递归调用自己,尝试再次获取可用连接
						con = getConnection();
					}
				} catch (SQLException e) {
					System.out.println("从连接池" + name + "删除一个无效连接");
					// 递归调用自己,尝试再次获取可用连接
				}
			} else if (maxConn == 0 || checkedOut < maxConn) {
				con = newConnection();
			}
			if (con != null) {
				checkedOut++;
			}
			return con;
		}

		/**
		 * 从连接池获取可用连接.可以指定客户程序能够等待的最长时间 参见前一个getConnection()方法.
		 * 
		 * @param timeout
		 *            以毫秒计的等待时间限制
		 */
		public synchronized Connection getConnection(long timeout) {
			long startTime = new Date().getTime();
			Connection con;
			while ((con = getConnection()) == null) {
				try {
					wait(timeout);
				} catch (InterruptedException e) {
				}
				if ((new Date().getTime() - startTime) >= timeout) {
					// wait()返回的原因是超时
					return null;
				}
			}
			return con;
		}

		/**
		 * 关闭所有连接
		 */
		public synchronized void release() {
			Enumeration<Connection> allConnections = freeConnections.elements();
			while (allConnections.hasMoreElements()) {
				Connection con = (Connection) allConnections.nextElement();
				try {
					con.close();
					System.out.println("关闭连接池" + name + "中的一个连接");
				} catch (SQLException e) {
					System.out.println("无法关闭连接池" + name + "中的连接");
				}
			}
			freeConnections.removeAllElements();
		}

		/**
		 * 创建新的连接
		 */
		private Connection newConnection() {
			Connection con = null;
			try {
				if (user == null) {
					con = DriverManager.getConnection(URL);
				} else {
					con = DriverManager.getConnection(URL, user, password);
				}
				System.out.println("连接池" + name + "创建一个新的连接" + this.URL);
			} catch (SQLException e) {
				System.out.println("无法创建下列URL 的连接: " + URL);
				return null;
			}
			return con;
		}
	}

}