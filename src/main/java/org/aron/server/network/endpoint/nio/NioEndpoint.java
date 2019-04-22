package org.aron.server.network.endpoint.nio;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.aron.server.network.connector.nio.IdleConnectionCleaner;
import org.aron.server.network.connector.nio.NioAcceptor;
import org.aron.server.network.connector.nio.NioPoller;
import org.aron.server.network.dispatcher.nio.NioDispatcher;
import org.aron.server.network.endpoint.Endpoint;
import org.aron.server.network.wrapper.nio.NioSocketWrapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-18 20:54
 **/
@Slf4j
public class NioEndpoint extends Endpoint {

    /**
     * 初始化轮询器个数
     * Runtime.getRuntime().availableProcessors() Java虚拟机内cpu个数
     */
    private int pollerCount = Math.min(2, Runtime.getRuntime().availableProcessors());

    private ServerSocketChannel server;

    @Getter
    private volatile boolean isRunning = true;
    private List<NioPoller> nioPollers;
    /**
     * nio 分发器
     */
    private NioDispatcher nioDispatcher;

    /**
     * poller 轮询器
     */
    private AtomicInteger pollerRotate = new AtomicInteger(0);
    /**
     * 连接超时时间
     */
    @Getter
    private int keepAliveTimeout = 3 * 1000;
    /**
     * 针对keep-alive连接，如果长期没有数据交换则将其关闭
     */
    private IdleConnectionCleaner cleaner;

    @Override
    public void start(int port) throws IOException {
        initDispatcherServlet();
        initServerSocket(port);
        initPoller();
        initAcceptor();
        initIdleSocketCleaner();
        log.info("server is start listening to the nio mode running at http://127.0.0.1:{}", port);
    }

    @Override
    public void close() throws IOException {
        isRunning = false;
        // 关闭定时器线程
        cleaner.shutdown();
        for (NioPoller nioPoller: nioPollers) {
            nioPoller.close();
        }
        nioDispatcher.shutdown();
        server.close();
    }

    /**
     * 调用dispatcher,处理这个读就绪的客户端连接
     */
    public void execute(NioSocketWrapper socketWrapper) {
        nioDispatcher.doDispatch(socketWrapper);
    }

    /**
     * 轮询Poller, 实现负载均衡
     */
    private NioPoller getPoller() {
        int index = Math.abs(pollerRotate.incrementAndGet()) % nioPollers.size();
        return nioPollers.get(index);
    }

    /**
     * 以阻塞方式来接收一个客户端的链接
     */
    public SocketChannel accept() throws IOException {
        return server.accept();
    }

    /**
     * 将Acceptor接收到的socket放到轮询到的一个Poller的Queue中
     */
    public void registerToPoller(SocketChannel socketChannel) throws IOException {
        server.configureBlocking(false);
        getPoller().register(socketChannel, true);
        server.configureBlocking(true);
    }

    private void initServerSocket(int port) throws IOException {
        server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(port));
        server.configureBlocking(true);
    }

    /**
     * 初始化 NIO分发器
     */
    private void initDispatcherServlet() {
        nioDispatcher = new NioDispatcher();
    }

    /**
     * 初始化轮询器列表 即开启pollerCount个轮询器守护线程
     */
    private void initPoller() throws IOException {
        nioPollers = new ArrayList<>(pollerCount);
        for (int i = 0; i < pollerCount; i++) {
            String pollName = "NioPoller-" + i;
            NioPoller nioPoller = new NioPoller(this, pollName);
            Thread thread = new Thread(nioPoller, pollName);
            thread.setDaemon(true);
            thread.start();
            nioPollers.add(nioPoller);
        }
    }

    /**
     * 初始化Acceptor
     */
    @Override
    public void initAcceptor() {
        NioAcceptor nioAcceptor = new NioAcceptor(this);
        Thread thread = new Thread(nioAcceptor, "NIO-Acceptor");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * 初始化定时器线程
     */
    private void initIdleSocketCleaner() {
        cleaner = new IdleConnectionCleaner(nioPollers);
        cleaner.start();
    }

}
