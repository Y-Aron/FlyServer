package org.aron.server.network.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.aron.server.core.ServerConfiguration;
import org.aron.server.core.ServletContext;
import org.aron.server.error.ServletExceptionHandler;
import org.aron.server.error.impl.ServletExceptionHandlerImpl;
import org.aron.server.network.wrapper.SocketWrapper;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-17 10:47
 **/
@Slf4j
public abstract class AbstractDispatcher {
    /**
     * 获取上下文
     */
    protected ServletContext servletContext;
    /**
     * 线程池
     */
    protected ThreadPoolExecutor pool;

    /**
     * 异常处理程序
     */
    protected ServletExceptionHandler handler;

    /**
     * 线程池线程个数
     */
    private static final int THREAD_POOL_SIZE = 10;
    /**
     * 线程池线程最大个数
     */
    private static final int THREAD_POOL_MAX_SIZE = 100;
    /**
     * 线程保持连接时间(second)
     */
    private static final int KEEP_ALIVE_TIME = 1;

    public AbstractDispatcher() {
        this.servletContext = ServerConfiguration.getServletContext();
        this.handler = new ServletExceptionHandlerImpl();
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count;
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Worker Pool-" + count ++);
            }
        };
        this.pool = new ThreadPoolExecutor(THREAD_POOL_SIZE,
                THREAD_POOL_MAX_SIZE,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(200),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        pool.shutdown();
        servletContext.destroy();
    }

    protected void closeSocketWrapper(SocketWrapper socketWrapper) {
        try {
            socketWrapper.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 请求转发
     */
    public abstract void doDispatch(SocketWrapper socketWrapper) throws IOException;
}
