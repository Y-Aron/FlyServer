package org.aron.server.network.connector.nio;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author: Y-ParamAop
 * @create: 2018-12-18 22:20
 **/
@Slf4j
public class IdleConnectionCleaner implements Runnable {
    private ScheduledExecutorService executor;
    private List<NioPoller> nioPollers;

    public IdleConnectionCleaner(List<NioPoller> nioPollers) {
        this.nioPollers = nioPollers;
    }

    /**
     * 启动定时器线程
     */
    public void start() {
        ThreadFactory factory = r -> new Thread(r, "IdleConnectionCleaner");
        executor = Executors.newSingleThreadScheduledExecutor(factory);
        executor.scheduleWithFixedDelay(this, 0, 5, TimeUnit.SECONDS);
    }

    /**
     * 关闭定时器线程
     */
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public void run() {
        for (NioPoller nioPoller : nioPollers) {
            log.debug("Cleaner 检测{} 所持有的Socket中..", nioPoller.getPollerName());
            nioPoller.cleanTimeoutSocket();
        }
        log.debug("检测结束");
    }
}
