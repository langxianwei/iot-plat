package com.point.iot.base.websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class SocketServer {
	public SocketServer(int port,  int threadPoolSize) {
		NioSocketAcceptor acceptor = new NioSocketAcceptor();
		acceptor.setHandler(new ServerHandler());
//		acceptor.getFilterChain().addLast("mychian",new ProtocolCodecFilter(new TextLineCodecFactory()));
//		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
		acceptor.getFilterChain().addLast("exector", new ExecutorFilter(new OrderedThreadPoolExecutor(threadPoolSize)));
		SocketSessionConfig ssc = acceptor.getSessionConfig();// 建立连接的配置文件
		ssc.setReadBufferSize(4096);// 设置接收最大字节默认4096
		ssc.setReceiveBufferSize(10240);// 设置输入缓冲区的大小
		ssc.setSendBufferSize(10240);// 设置输出缓冲区的大小
		ssc.setReuseAddress(true);// 设置每一个非主监听连接的端口可以重用
		
		try {
			// 绑定端口
			acceptor.bind(new InetSocketAddress(port));
			System.out.println("服务器启动正常，监听端口 " + port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		new SocketServer(8080, 3);
	}
}
