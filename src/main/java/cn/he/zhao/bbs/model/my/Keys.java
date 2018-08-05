package cn.he.zhao.bbs.model.my;

/**
 * 描述:
 * latke中移植而来的Keys
 *
 * @Author HeFeng
 * @Create 2018-07-24 15:29
 */
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

public final class Keys {
    public static final String TOKEN = "token";
    public static final String MSG = "msg";
    public static final String EVENTS = "events";
    public static final String CODE = "code";
    public static final String STATUS_CODE = "sc";
    public static final String SESSION_ID = "sId";
    public static final String RESULTS = "rslts";
    // TODO: 2018/7/24  从latke中直接使用oid，可能存在影响
    public static final String OBJECT_ID = "oId";
    public static final String OBJECT_IDS = "oIds";
    public static final String LOCALE = "locale";
    public static final String LANGUAGE = "lang";
    public static final DateFormat SIMPLE_DATE_FORMAT1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final String TEMAPLTE_DIR_NAME = "templateDirName";
    public static final String EXCLUDES = "excludes";
    public static final String REQUEST = "request";
    public static final String FREEMARKER_ACTION = "FreeMarkerAction";

    private Keys() {
    }

    public static void fillServer(Map<String, Object> dataModel) {
//        dataModel.put("serverScheme", Latkes.getServerScheme());
//        dataModel.put("serverHost", Latkes.getServerHost());
//        dataModel.put("serverPort", Latkes.getServerPort());
//        dataModel.put("server", Latkes.getServer());
//        dataModel.put("contextPath", Latkes.getContextPath());
//        dataModel.put("servePath", Latkes.getServePath());
//        dataModel.put("staticServerScheme", Latkes.getStaticServerScheme());
//        dataModel.put("staticServerHost", Latkes.getStaticServerHost());
//        dataModel.put("staticServerPort", Latkes.getStaticServerPort());
//        dataModel.put("staticServer", Latkes.getStaticServer());
//        dataModel.put("staticPath", Latkes.getStaticPath());
//        dataModel.put("staticServePath", Latkes.getStaticServePath());
    }

    public static void fillRuntime(Map<String, Object> dataModel) {
//        dataModel.put("runtimeCache", Latkes.getRuntimeCache().name());
//        dataModel.put("runtimeDatabase", Latkes.getRuntimeDatabase().name());
//        dataModel.put("runtimeMode", Latkes.getRuntimeMode().name());
    }

    public static final class Runtime {
        public static final String RUNTIME_CACHE = "runtimeCache";
        public static final String RUNTIME_DATABASE = "runtimeDatabase";
        public static final String RUNTIME_MODE = "runtimeMode";

        private Runtime() {
        }
    }

    public static final class Server {
        public static final String SERVER_SCHEME = "serverScheme";
        public static final String SERVER_HOST = "serverHost";
        public static final String SERVER_PORT = "serverPort";
        public static final String SERVER = "server";
        public static final String STATIC_SERVER_SCHEME = "staticServerScheme";
        public static final String STATIC_SERVER_HOST = "staticServerHost";
        public static final String STATIC_SERVER_PORT = "staticServerPort";
        public static final String STATIC_SERVER = "staticServer";
        public static final String CONTEXT_PATH = "contextPath";
        public static final String STATIC_PATH = "staticPath";
        public static final String SERVE_PATH = "servePath";
        public static final String STATIC_SERVE_PATH = "staticServePath";

        private Server() {
        }
    }

    public static final class HttpRequest {
        public static final String IS_SEARCH_ENGINE_BOT = "isSearchEngineBot";
        public static final String REQUEST_STATIC_RESOURCE_CHECKED = "requestStaticResourceChecked";
        public static final String IS_REQUEST_STATIC_RESOURCE = "isRequestStaticResource";
        public static final String START_TIME_MILLIS = "startTimeMillis";
        public static final String REQUEST_URI = "requestURI";
        public static final String REQUEST_METHOD = "requestMethod";

        private HttpRequest() {
        }
    }
}