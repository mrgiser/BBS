package cn.he.zhao.bbs.util;

import cn.he.zhao.bbs.spring.Strings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 描述:
 * util
 *
 * @Author HeFeng
 * @Create 2018-07-30 16:20
 */
public class MyUtil {

    public static final String VERSION = "2.4.3";
    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    private static final Logger LOGGER = LoggerFactory.getLogger(MyUtil.class);
    private static final Properties LOCAL_PROPS = new Properties();
    private static final Properties LATKE_PROPS = new Properties();
    private static final Properties REMOTE_PROPS = new Properties();
    private static Locale locale;
    private static MyUtil.RuntimeMode runtimeMode;
    private static String startupTimeMillis = String.valueOf(System.currentTimeMillis());
    private static String staticResourceVersion;
    private static String serverScheme;
    private static String staticServerScheme;
    private static String serverHost;
    private static String staticServerHost;
    private static String serverPort;
    private static String staticServerPort;
    private static String server;
    private static String servePath;
    private static String staticServer;
    private static String staticServePath;
    private static String contextPath;
    private static String staticPath;
    private static String scanPath;
//    private static Server h2;

    private MyUtil() {
    }

    public static String getStaticResourceVersion() {
        if (null == staticResourceVersion) {
            staticResourceVersion = LATKE_PROPS.getProperty("staticResourceVersion");
            if (null == staticResourceVersion) {
                staticResourceVersion = startupTimeMillis;
            }
        }

        return staticResourceVersion;
    }

    public static void setStaticResourceVersion(String staticResourceVersion) {
        staticResourceVersion = staticResourceVersion;
    }

    public static String getServerScheme() {
        if (null == serverScheme) {
            serverScheme = LATKE_PROPS.getProperty("serverScheme");
            if (null == serverScheme) {
                throw new IllegalStateException("spring.properties [serverScheme] is empty");
            }
        }

        return serverScheme;
    }

    public static void setServerScheme(String serverScheme) {
        serverScheme = serverScheme;
    }

    public static String getServerHost() {
        if (null == serverHost) {
            serverHost = LATKE_PROPS.getProperty("serverHost");
            if (null == serverHost) {
                throw new IllegalStateException("spring.properties [serverHost] is empty");
            }
        }

        return serverHost;
    }

    public static void setServerHost(String serverHost) {
        serverHost = serverHost;
    }

    public static String getServerPort() {
        if (null == serverPort) {
            serverPort = LATKE_PROPS.getProperty("serverPort");
        }

        return serverPort;
    }

    public static void setServerPort(String serverPort) {
        serverPort = serverPort;
    }

    public static String getServer() {
        if (null == server) {
            StringBuilder serverBuilder = (new StringBuilder(getServerScheme())).append("://").append(getServerHost());
            String port = getServerPort();
            if (!Strings.isEmptyOrNull(port) && !port.equals("80")) {
                serverBuilder.append(':').append(port);
            }

            server = serverBuilder.toString();
        }

        return server;
    }

    public static String getServePath() {
        if (null == servePath) {
            servePath = getServer() + getContextPath();
        }

        return servePath;
    }

    public static String getStaticServerScheme() {
        if (null == staticServerScheme) {
            staticServerScheme = LATKE_PROPS.getProperty("staticServerScheme");
            if (null == staticServerScheme) {
                staticServerScheme = getServerScheme();
            }
        }

        return staticServerScheme;
    }

    public static void setStaticServerScheme(String staticServerScheme) {
        staticServerScheme = staticServerScheme;
    }

    public static String getStaticServerHost() {
        if (null == staticServerHost) {
            staticServerHost = LATKE_PROPS.getProperty("staticServerHost");
            if (null == staticServerHost) {
                staticServerHost = getServerHost();
            }
        }

        return staticServerHost;
    }

    public static void setStaticServerHost(String staticServerHost) {
        staticServerHost = staticServerHost;
    }

    public static String getStaticServerPort() {
        if (null == staticServerPort) {
            staticServerPort = LATKE_PROPS.getProperty("staticServerPort");
            if (null == staticServerPort) {
                staticServerPort = getServerPort();
            }
        }

        return staticServerPort;
    }

    public static void setStaticServerPort(String staticServerPort) {
        staticServerPort = staticServerPort;
    }

    public static String getStaticServer() {
        if (null == staticServer) {
            StringBuilder staticServerBuilder = (new StringBuilder(getStaticServerScheme())).append("://").append(getStaticServerHost());
            String port = getStaticServerPort();
            if (!Strings.isEmptyOrNull(port) && !port.equals("80")) {
                staticServerBuilder.append(':').append(port);
            }

            staticServer = staticServerBuilder.toString();
        }

        return staticServer;
    }

    public static String getStaticServePath() {
        if (null == staticServePath) {
            staticServePath = getStaticServer() + getStaticPath();
        }

        return staticServePath;
    }

    public static String getContextPath() {
        if (null != contextPath) {
            return contextPath;
        } else {
            String contextPathConf = LATKE_PROPS.getProperty("contextPath");
            if (null != contextPathConf) {
                contextPath = contextPathConf;
                return contextPath;
            } else {
                ServletContext servletContext = AbstractServletListener.getServletContext();
                contextPath = servletContext.getContextPath();
                return contextPath;
            }
        }
    }

    public static void setContextPath(String contextPath) {
        contextPath = contextPath;
    }

    public static String getStaticPath() {
        if (null == staticPath) {
            staticPath = LATKE_PROPS.getProperty("staticPath");
            if (null == staticPath) {
                staticPath = getContextPath();
            }
        }

        return staticPath;
    }

    public static void setStaticPath(String staticPath) {
        staticPath = staticPath;
    }

    public static String getScanPath() {
        if (null == scanPath) {
            scanPath = LATKE_PROPS.getProperty("scanPath");
        }

        return scanPath;
    }

    public static void setScanPath(String scanPath) {
        scanPath = scanPath;
    }

    public static void initRuntimeEnv() {
        LOGGER.trace("Initializes runtime environment from configuration file", new Object[0]);
        if (null == runtimeMode) {
            String runtimeModeValue = LATKE_PROPS.getProperty("runtimeMode");
            if (null != runtimeModeValue) {
                runtimeMode = MyUtil.RuntimeMode.valueOf(runtimeModeValue);
            } else {
                LOGGER.trace("Can't parse runtime mode in spring.properties, default to [PRODUCTION]", new Object[0]);
                runtimeMode = MyUtil.RuntimeMode.PRODUCTION;
            }
        }

        LOGGER.info( "Runtime mode is [{0}]", new Object[]{getRuntimeMode()});
        MyUtil.RuntimeDatabase runtimeDatabase = getRuntimeDatabase();
        LOGGER.info("Runtime database is [{0}]", new Object[]{runtimeDatabase});
        if (MyUtil.RuntimeDatabase.H2 == runtimeDatabase) {
            String newTCPServer = getLocalProperty("newTCPServer");
            if ("true".equals(newTCPServer)) {
                LOGGER.info("Starting H2 TCP server", new Object[0]);
                String jdbcURL = getLocalProperty("jdbc.URL");
                if (Strings.isEmptyOrNull(jdbcURL)) {
                    throw new IllegalStateException("The jdbc.URL in local.properties is required");
                }

                String[] parts = jdbcURL.split(":");
                if (parts.length != Integer.valueOf("5")) {
                    throw new IllegalStateException("jdbc.URL should like [jdbc:h2:tcp://localhost:8250/~/] (the port part is required)");
                }

                String port = parts[parts.length - 1];
                port = StringUtils.substringBefore(port, "/");
                LOGGER.trace( "H2 TCP port [{0}]", new Object[]{port});

                try {
                    h2 = Server.createTcpServer(new String[]{"-tcpPort", port, "-tcpAllowOthers"}).start();
                } catch (SQLException var7) {
                    String msg = "H2 TCP server create failed";
                    LOGGER.error("H2 TCP server create failed", var7);
                    throw new IllegalStateException("H2 TCP server create failed");
                }

                LOGGER.info("Started H2 TCP server");
            }
        }

        MyUtil.RuntimeCache runtimeCache = getRuntimeCache();
        LOGGER.info( "Runtime cache is [{0}]", new Object[]{runtimeCache});
        locale = new Locale("en_US");
    }

    public static MyUtil.RuntimeMode getRuntimeMode() {
        if (null == runtimeMode) {
            throw new RuntimeException("Runtime mode has not been initialized!");
        } else {
            return runtimeMode;
        }
    }

    public static void setRuntimeMode(MyUtil.RuntimeMode runtimeMode) {
        runtimeMode = runtimeMode;
    }

    public static MyUtil.RuntimeCache getRuntimeCache() {
        String runtimeCache = LOCAL_PROPS.getProperty("runtimeCache");
        if (null == runtimeCache) {
            LOGGER.debug("Not found [runtimeCache] in local.properties, uses [LOCAL_LRU] as default");
            return MyUtil.RuntimeCache.LOCAL_LRU;
        } else {
            MyUtil.RuntimeCache ret = MyUtil.RuntimeCache.valueOf(runtimeCache);
            if (null == ret) {
                throw new RuntimeException("Please configures a valid runtime cache in local.properties!");
            } else {
                return ret;
            }
        }
    }

    public static MyUtil.RuntimeDatabase getRuntimeDatabase() {
        String runtimeDatabase = LOCAL_PROPS.getProperty("runtimeDatabase");
        if (null == runtimeDatabase) {
            throw new RuntimeException("Please configures runtime database in local.properties!");
        } else {
            MyUtil.RuntimeDatabase ret = MyUtil.RuntimeDatabase.valueOf(runtimeDatabase);
            if (null == ret) {
                throw new RuntimeException("Please configures a valid runtime database in local.properties!");
            } else {
                return ret;
            }
        }
    }

    public static Locale getLocale() {
        if (null == locale) {
            throw new RuntimeException("Default locale has not been initialized!");
        } else {
            return locale;
        }
    }

    public static void setLocale(Locale locale) {
        locale = locale;
    }

    public static String getLocalProperty(String key) {
        return LOCAL_PROPS.getProperty(key);
    }

    public static String getLatkeProperty(String key) {
        return LATKE_PROPS.getProperty(key);
    }

    public static boolean isRemoteEnabled() {
        return !REMOTE_PROPS.isEmpty();
    }

    public static String getRemoteProperty(String key) {
        return REMOTE_PROPS.getProperty(key);
    }

//    public static void shutdown() {
//        try {
//            CronService.shutdown();
//            EXECUTOR_SERVICE.shutdown();
//            if (MyUtil.RuntimeCache.REDIS == getRuntimeCache()) {
//                RedisCache.shutdown();
//            }
//
//            Connections.shutdownConnectionPool();
//            if (MyUtil.RuntimeDatabase.H2 == getRuntimeDatabase()) {
//                String newTCPServer = getLocalProperty("newTCPServer");
//                if ("true".equals(newTCPServer)) {
//                    h2.stop();
//                    h2.shutdown();
//                    LOGGER.info( "Closed H2 TCP server", new Object[0]);
//                }
//            }
//        } catch (Exception var4) {
//            LOGGER.error( "Shutdowns Latke failed", var4);
//        }
//
//        Lifecycle.endApplication();
//        Enumeration drivers = DriverManager.getDrivers();
//
//        while(drivers.hasMoreElements()) {
//            Driver driver = (Driver)drivers.nextElement();
//
//            try {
//                DriverManager.deregisterDriver(driver);
//                LOGGER.trace( "Unregistered JDBC driver [" + driver + "]", new Object[0]);
//            } catch (SQLException var3) {
//                LOGGER.error( "Unregister JDBC driver [" + driver + "] failed", var3);
//            }
//        }
//
//    }

//    public static void setTimeZone(String timeZoneId) {
//        TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
//        Templates.MAIN_CFG.setTimeZone(timeZone);
//        Templates.MOBILE_CFG.setTimeZone(timeZone);
//    }

//    public static void loadSkin(String skinDirName) {
//        LOGGER.debug("Loading skin [dirName=" + skinDirName + ']');
//        ServletContext servletContext = AbstractServletListener.getServletContext();
//        Templates.MAIN_CFG.setServletContextForTemplateLoading(servletContext, "skins/" + skinDirName);
//        setTimeZone("Asia/Shanghai");
//        LOGGER.info("Loaded skins....");
//    }

    public static String getSkinName(String skinDirName) {
        try {
            Properties ret = new Properties();
            File file = getWebFile("/skins/" + skinDirName + "/skin.properties");
            ret.load(new FileInputStream(file));
            return ret.getProperty("name");
        } catch (Exception var3) {
            LOGGER.error("Read skin configuration error[msg={0}]", new Object[]{var3.getMessage()});
            return null;
        }
    }

    public static File getWebFile(String path) {
        ServletContext servletContext = AbstractServletListener.getServletContext();

        try {
            URL resource = servletContext.getResource(path);
            if (null == resource) {
                return null;
            } else {
                File ret = FileUtils.toFile(resource);
                if (null == ret) {
                    File tempdir = (File)servletContext.getAttribute("javax.servlet.context.tempdir");
                    ret = new File(tempdir.getPath() + path);
                    FileUtils.copyURLToFile(resource, ret);
                    ret.deleteOnExit();
                }

                return ret;
            }
        } catch (Exception var5) {
            LOGGER.error("Reads file [path=" + path + "] failed", var5);
            return null;
        }
    }

    static {
        LOGGER.debug("Loading spring.properties");

        InputStream resourceAsStream;
        try {
            resourceAsStream = MyUtil.class.getResourceAsStream("/spring.properties");
            if (null != resourceAsStream) {
                LATKE_PROPS.load(resourceAsStream);
                LOGGER.debug("Loaded spring.properties");
            }
        } catch (Exception var3) {
            LOGGER.error( "Not found spring.properties", new Object[0]);
            throw new RuntimeException("Not found spring.properties");
        }

        LOGGER.debug("Loading local.properties");

        try {
            resourceAsStream = MyUtil.class.getResourceAsStream("/local.properties");
            if (null != resourceAsStream) {
                LOCAL_PROPS.load(resourceAsStream);
                LOGGER.debug("Loaded local.properties");
            }
        } catch (Exception var2) {
            LOGGER.debug("Not found local.properties", new Object[0]);
        }

        LOGGER.debug("Loading remote.properties");

        try {
            resourceAsStream = MyUtil.class.getResourceAsStream("/remote.properties");
            if (null != resourceAsStream) {
                REMOTE_PROPS.load(resourceAsStream);
                LOGGER.debug("Loaded remote.properties");
            }
        } catch (Exception var1) {
            LOGGER.debug("Not found Latke remote.properties", new Object[0]);
        }

    }

    public static enum RuntimeMode {
        DEVELOPMENT,
        PRODUCTION;

        private RuntimeMode() {
        }
    }

    public static enum RuntimeCache {
        NONE,
        LOCAL_LRU,
        REDIS;

        private RuntimeCache() {
        }
    }

    public static enum RuntimeDatabase {
        NONE,
        ORACLE,
        MYSQL,
        H2,
        MSSQL;

        private RuntimeDatabase() {
        }
    }
}