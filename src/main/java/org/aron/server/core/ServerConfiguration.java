package org.aron.server.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aron.commons.utils.PropertyUtils;
import org.aron.context.core.impl.AnnotationApplicationContext;
import org.aron.context.error.AnnotationException;
import org.aron.context.error.BeanInstantiationException;
import org.aron.server.error.ServerException;

import java.io.IOException;
import java.util.Map;

/**
 * @author: Y-Aron
 * @create: 2019-02-08 11:45
 **/
@Slf4j
public class ServerConfiguration {
    public static final String SESSION_NAME = "JSESSIONID";

    private static final int defaultPort = 8080;
    public static int PORT;

    private static final String defaultConnector = "bio";

    public static String CONNECTOR;

    private static final long defaultUploadMaxSize = 100;
    public static long UPLOAD_MAX_SIZE;

    private static final String defaultUploadUnit = "mb";
    public static String UPLOAD_UNIT;

    private static final String defaultUploadDir = "upload";
    public static String UPLOAD_DIR;

    @Getter
    private static ServletContext servletContext = new ServletContext();

    static {
        try {
            load();
        } catch (ServerException | IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 初始化配置信息
     */
    private static void load() throws ServerException, IOException {
        Map<String, String> map = PropertyUtils.loadAll(ServerConfiguration.class.getResource("/").getPath());
        // 设置连接模式
        CONNECTOR = map.get("server.connector");
        if (StringUtils.isBlank(CONNECTOR)) {
            CONNECTOR = defaultConnector;
        }
        if (!StringUtils.equalsAnyIgnoreCase(CONNECTOR, "bio", "nio")) {
            throw new ServerException(CONNECTOR + " is not bio, nio connection modes");
        }
        // 设置连接端口号
        String port = map.get("server.port");
        if (StringUtils.isBlank(port)) {
            PORT = defaultPort;
        } else {
            try {
                PORT = Integer.parseInt(port);
            } catch (NumberFormatException e) {
                throw new ServerException(PORT + " is not int number");
            }
        }
        // 设置上传文件最大值
        String maxSize = map.get("server.upload.max.size");
        if (StringUtils.isBlank(maxSize)) {
            UPLOAD_MAX_SIZE = defaultUploadMaxSize;
        } else {
            try {
                UPLOAD_MAX_SIZE = Long.parseLong(maxSize);
            } catch (NumberFormatException e) {
                throw new ServerException(UPLOAD_MAX_SIZE + " is not long number");
            }
        }
        // 设置上传文件的数值单位
        UPLOAD_UNIT = map.get("server.upload.unit");
        if (StringUtils.isBlank(UPLOAD_UNIT)) {
            UPLOAD_UNIT = defaultUploadUnit;
        } else {
            if (!StringUtils.equalsAnyIgnoreCase(UPLOAD_UNIT, "mb", "gb", "tb", "kb")) {
                throw new ServerException(UPLOAD_UNIT + " is not mb gb tb kb");
            }
        }
        // 设置上传文件目录
        UPLOAD_DIR = map.get("server.upload.dir");
        if (StringUtils.isBlank(UPLOAD_DIR)) {
            UPLOAD_DIR = defaultUploadDir;
        }
    }

    public void init(AnnotationApplicationContext context) throws ServerException {
        try {
            servletContext.buildContext(context);
            servletContext.setServerConfiguration(this);
        } catch (ServerException | ClassNotFoundException e) {
            throw new ServerException(e.getMessage());
        }
    }

    public void init(Class<?> root, String[] scanPkg, String[] filterPkg, Class<?>... classes) throws ServerException {
        AnnotationApplicationContext context = new AnnotationApplicationContext(root);
        context.setScanPackages(scanPkg);
        context.setFilterPackages(filterPkg);
        try {
            context.init();
            if (ArrayUtils.isNotEmpty(classes)) {
                for (Class<?> clazz : classes) {
                    context.setBean(clazz);
                }
            }
            servletContext.buildContext(context);
            servletContext.setServerConfiguration(this);
        } catch (BeanInstantiationException | AnnotationException | ClassNotFoundException e) {
            throw new ServerException(e.getMessage());
        }
    }
}
