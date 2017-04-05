package com.point.iot.mina.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.point.iot.mina.codc.factory.MessageCodecFactory;
import com.point.iot.mina.msg.MessageHandler;

/**
 * 服务器
 * @author langxianwei
 *
 */
public class SocketServer {
	Logger logger = Logger.getLogger(SocketServer.class) ;
	private ServerHandler mServerHandler;
	private NioSocketAcceptor acceptor;

	public SocketServer(int port, MessageHandler msgHandler) {
		this(port, msgHandler, 10);
	}

	public SocketServer(int port, MessageHandler msgHandler, int threadPoolSize) {
		mServerHandler = new ServerHandler(msgHandler);

		acceptor = new NioSocketAcceptor();
		acceptor.setHandler(mServerHandler);// 设置接收器的处理程序
		acceptor.setReuseAddress(true) ;
//		acceptor.getFilterChain().addLast("exector", new ExecutorFilter(new OrderedThreadPoolExecutor(10)));//建立有序线程池
		acceptor.getFilterChain().addLast("exector", new ExecutorFilter(new OrderedThreadPoolExecutor(Runtime.getRuntime().availableProcessors()+1)));//建立有序线程池
//		acceptor.getFilterChain().addLast("exector", new ExecutorFilter(new OrderedThreadPoolExecutor(128)));//建立有序线程池
/*		Executor threadPool = Executors.newFixedThreadPool(threadPoolSize);// 建立线程池
		acceptor.getFilterChain().addLast("exector",
				new ExecutorFilter(threadPool));*/
		acceptor.getFilterChain().addLast("codec",//解码\转码
				new ProtocolCodecFilter(new MessageCodecFactory(Charset.forName("UTF-8"))));

		try {
			// 绑定端口
			acceptor.bind(new InetSocketAddress(port));
			logger.info("服务器启动正常，监听端口 " + port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		SocketSessionConfig ssc = acceptor.getSessionConfig();// 建立连接的配置文件
		ssc.setReadBufferSize(4096);// 设置接收最大字节默认4096
//		ssc.setReceiveBufferSize(1024);// 设置输入缓冲区的大小
//		ssc.setSendBufferSize(1024);// 设置输出缓冲区的大小
//		ssc.setReuseAddress(true);// 设置每一个非主监听连接的端口可以重用
		ssc.setTcpNoDelay(false);
		
		
	}
	
	public int getManagedSessionCount() {
		return acceptor.getManagedSessionCount();
	}
	
	public Map<Long, IoSession> getManagedSessions() {
		return acceptor.getManagedSessions();
	}
}
