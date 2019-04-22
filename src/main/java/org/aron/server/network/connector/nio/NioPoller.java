package org.aron.server.network.connector.nio;

import org.aron.server.network.endpoint.nio.NioEndpoint;
import org.aron.server.network.wrapper.nio.NioSocketWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-18 21:12
 **/
@Slf4j
public class NioPoller implements Runnable{

    private NioEndpoint nioEndpoint;
    @Getter
    private Selector selector;
    /**
     * 并发队列-无界非阻塞
     */
    private Queue<PollerEvent> eventQueue;
    /**
     * 轮询器名称
     */
    @Getter
    private String pollerName;
    private Map<SocketChannel, NioSocketWrapper> socketMap;

    public NioPoller(NioEndpoint nioEndpoint, String pollerName) throws IOException {
        this.socketMap = new ConcurrentHashMap<>();
        this.nioEndpoint = nioEndpoint;
        this.selector = Selector.open();
        this.eventQueue = new ConcurrentLinkedQueue<>();
        this.pollerName = pollerName;
    }

    /**
     * 注册一个新的或旧的socket至Poller中
     * @param socketChannel socket通道
     * @param isNewSocket 是否是新建的socket
     */
    public void register(SocketChannel socketChannel, boolean isNewSocket) {
        log.debug("Acceptor将连接到socket放入{}的Queue中", pollerName);
        NioSocketWrapper wrapper;
        if (isNewSocket) {
            // 设置waitBegin
            wrapper = new NioSocketWrapper(nioEndpoint, socketChannel, this, true);
            // 用于cleaner检测超时的socket和关闭socket
            socketMap.put(socketChannel, wrapper);
        } else {
            wrapper = socketMap.get(socketChannel);
            wrapper.setWorking(false);
        }
        wrapper.setWaitBegin(System.currentTimeMillis());
        // 添加一个PollerEvent至队列中
        eventQueue.offer(new PollerEvent(wrapper));
        // 某个线程调用select()方法后阻塞了,即使没有通道已经就绪,也有办法让其从select()方法返回
        // 只要让其他线程在第一个线程调用select()方法的那个对象上调用Selector.wakeup()方法即可
        selector.wakeup();
    }

    /**
     * 清除过期的Socket
     */
    public void cleanTimeoutSocket() {
        for (Iterator<Map.Entry<SocketChannel, NioSocketWrapper>> it = socketMap.entrySet().iterator(); it.hasNext();) {
            NioSocketWrapper wrapper = it.next().getValue();
            log.debug("缓存中的socket: {}", wrapper);
            if (!wrapper.getSocketChannel().isConnected()) {
                log.debug("该socket已被关闭");
                it.remove();
                continue;
            }
            if (wrapper.isWorking()) {
                log.debug("该socket正在工作中,不予关闭");
                continue;
            }
            if (System.currentTimeMillis() - wrapper.getWaitBegin() > nioEndpoint.getKeepAliveTimeout()) {
                log.debug("{} keepAlive已过期", wrapper.getSocketChannel());
                try {
                    wrapper.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                it.remove();
            }
        }
    }

    @Override
    public void run() {
        log.debug("nio-{} start listening", Thread.currentThread().getName());
        while (nioEndpoint.isRunning()) {
            try {
                events();
                if (selector.select() <= 0) {
                    continue;
                }
                log.debug("select() 返回,开始获取当前选择器中所有注册的监听事件");
                // 获取当前选择器中所有注册的监听事件
                for (Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();iterator.hasNext();) {
                    SelectionKey key = iterator.next();
                    // 如果"read"事件已就绪
                    if (key.isReadable()) {
                        log.debug("serverSocket读已就绪,准备read");
                        NioSocketWrapper attachment = (NioSocketWrapper) key.attachment();
                        if (attachment != null) {
                            processSocket(attachment);
                        }
                    }
                    // 处理完毕后, 取消当前的选择键
                    iterator.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClosedSelectorException e) {
                log.debug("{} 对应的selector已关闭", this.pollerName);
            }
        }
    }

    /**
     * 关闭轮询器
     */
    public void close() throws IOException {
        for (NioSocketWrapper wrapper : socketMap.values()) {
            wrapper.close();
        }
        eventQueue.clear();
        selector.close();
    }

    private void processSocket(NioSocketWrapper attachment) {
        attachment.setWorking(true);
        nioEndpoint.execute(attachment);
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean events() {
        log.debug("Queue大小为{}，清空Queue，将连接到的Socket注册到Selector中", eventQueue.size());
        boolean result = false;
        PollerEvent pollerEvent;
        for (int i = 0, size = eventQueue.size(); i < size && (pollerEvent = eventQueue.poll()) != null; i++) {
            result = true;
            pollerEvent.run();
        }
        return result;
    }

    @Data
    @AllArgsConstructor
    private static class PollerEvent implements Runnable {

        private NioSocketWrapper wrapper;

        @Override
        public void run() {
            log.debug("将SocketChannel的读事件注册到Poller的Selector中");
            if (wrapper.getSocketChannel().isOpen()) {
                try {
                    wrapper.getSocketChannel()
                            .register(wrapper.getNioPoller().getSelector(), SelectionKey.OP_READ, wrapper);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            } else {
                log.error("socket已经关闭，无法注册到Poller");
            }
        }
    }
}
