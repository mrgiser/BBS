package cn.he.zhao.bbs.spring;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * 描述:
 * Requests
 *
 * @Author HeFeng
 * @Create 2018-08-01 16:04
 */
public final class Requests {
    public static final String PAGINATION_PATH_PATTERN = "*/*/*";
    private static final Logger LOGGER = LoggerFactory.getLogger(Requests.class);
    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final int DEFAULT_WINDOW_SIZE = 20;
    private static final Pattern MOBILE_USER_AGENT_PATTERN = Pattern.compile("android.+mobile|avantgo|bada|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge|maemo|midp|mmp|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)|plucker|pocket|psp|symbian|treo|up.(browser|link)|ucweb|vodafone|wap|webos|windows (ce|phone)|xda|xiino|htc|MQQBrowser", 2);
    private static final Pattern SEARCH_ENGINE_BOT_USER_AGENT_PATTERN = Pattern.compile("spider|bot|fetcher|crawler|google|yahoo|sogou|youdao|xianguo|rss|monitor|bae|b3log|symphony|solo|rhythm|pipe", 2);
    private static final int COOKIE_EXPIRY = 86400;

    private Requests() {
    }

    public static void log(HttpServletRequest httpServletRequest, Level level, Logger logger) {
        if (logger.isLoggable(level)) {
            logger.log(level, getLog(httpServletRequest), new Object[0]);
        }
    }

    public static String getLog(HttpServletRequest httpServletRequest) {
        String indents = "    ";
        StringBuilder logBuilder = (new StringBuilder("Request [")).append(Strings.LINE_SEPARATOR);
        logBuilder.append("    ").append("method=").append(httpServletRequest.getMethod()).append(",").append(Strings.LINE_SEPARATOR);
        logBuilder.append("    ").append("URL=").append(httpServletRequest.getRequestURL()).append(",").append(Strings.LINE_SEPARATOR);
        logBuilder.append("    ").append("contentType=").append(httpServletRequest.getContentType()).append(",").append(Strings.LINE_SEPARATOR);
        logBuilder.append("    ").append("characterEncoding=").append(httpServletRequest.getCharacterEncoding()).append(",").append(Strings.LINE_SEPARATOR);
        logBuilder.append("    ").append("local=[").append(Strings.LINE_SEPARATOR);
        logBuilder.append("    ").append("    ").append("addr=").append(httpServletRequest.getLocalAddr()).append(",").append(Strings.LINE_SEPARATOR);
        logBuilder.append("    ").append("    ").append("port=").append(httpServletRequest.getLocalPort()).append(",").append(Strings.LINE_SEPARATOR);
        logBuilder.append("    ").append("    ").append("name=").append(httpServletRequest.getLocalName()).append("],").append(Strings.LINE_SEPARATOR);
        logBuilder.append("    ").append("remote=[").append(Strings.LINE_SEPARATOR);
        logBuilder.append("    ").append("    ").append("addr=").append(getRemoteAddr(httpServletRequest)).append(",").append(Strings.LINE_SEPARATOR);
        logBuilder.append("    ").append("    ").append("port=").append(httpServletRequest.getRemotePort()).append(",").append(Strings.LINE_SEPARATOR);
        logBuilder.append("    ").append("    ").append("host=").append(httpServletRequest.getRemoteHost()).append("],").append(Strings.LINE_SEPARATOR);
        logBuilder.append("    ").append("headers=[").append(Strings.LINE_SEPARATOR);
        StringBuilder headerLogBuilder = new StringBuilder();
        Enumeration headerNames = httpServletRequest.getHeaderNames();

        while(headerNames.hasMoreElements()) {
            String name = (String)headerNames.nextElement();
            String value = httpServletRequest.getHeader(name);
            headerLogBuilder.append("    ").append("    ").append(name).append("=").append(value);
            headerLogBuilder.append(Strings.LINE_SEPARATOR);
        }

        headerLogBuilder.append("    ").append("]");
        logBuilder.append(headerLogBuilder.toString()).append(Strings.LINE_SEPARATOR).append("]");
        return logBuilder.toString();
    }

    public static String getRemoteAddr(HttpServletRequest request) {
        String ret = request.getHeader("X-forwarded-for");
        if (Strings.isEmptyOrNull(ret)) {
            ret = request.getHeader("X-Real-IP");
        }

        return Strings.isEmptyOrNull(ret) ? request.getRemoteAddr() : ret.split(",")[0];
    }

    public static String mobileSwitchToggle(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String ret = null;
        if (null != cookies && 0 != cookies.length) {
            try {
                for(int i = 0; i < cookies.length; ++i) {
                    Cookie cookie = cookies[i];
                    if ("btouch_switch_toggle".equals(cookie.getName())) {
                        ret = cookie.getValue();
                    }
                }
            } catch (Exception var5) {
                LOGGER.error( "Parses cookie failed", var5);
            }

            return ret;
        } else {
            return ret;
        }
    }

    public static boolean searchEngineBotRequest(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return Strings.isEmptyOrNull(userAgent) ? false : SEARCH_ENGINE_BOT_USER_AGENT_PATTERN.matcher(userAgent).find();
    }

    public static boolean hasBeenServed(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (null != cookies && 0 != cookies.length) {
            boolean needToCreate = true;
            boolean needToAppend = true;
            JSONArray cookieJSONArray = null;

            Cookie c;
            try {
                int i = 0;

                while(true) {
                    if (i >= cookies.length) {
                        if (needToCreate) {
                            StringBuilder builder = (new StringBuilder("[")).append("\"").append(request.getRequestURI()).append("\"]");
                            c = new Cookie("visited", URLEncoder.encode(builder.toString(), "UTF-8"));
                            c.setMaxAge(86400);
                            c.setPath("/");
                            response.addCookie(c);
                        } else if (needToAppend) {
                            cookieJSONArray.put(request.getRequestURI());
                            c = new Cookie("visited", URLEncoder.encode(cookieJSONArray.toString(), "UTF-8"));
                            c.setMaxAge(86400);
                            c.setPath("/");
                            response.addCookie(c);
                        }
                        break;
                    }

                    Cookie cookie = cookies[i];
                    if ("visited".equals(cookie.getName())) {
                        String value = URLDecoder.decode(cookie.getValue(), "UTF-8");
                        cookieJSONArray = new JSONArray(value);
                        if (null == cookieJSONArray || 0 == cookieJSONArray.length()) {
                            return false;
                        }

                        needToCreate = false;

                        for(int j = 0; j < cookieJSONArray.length(); ++j) {
                            String visitedURL = cookieJSONArray.optString(j);
                            if (request.getRequestURI().equals(visitedURL)) {
                                needToAppend = false;
                                return true;
                            }
                        }
                    }

                    ++i;
                }
            } catch (Exception var11) {
                LOGGER.warn( "Parses cookie failed, clears the cookie[name=visited]", new Object[0]);
                c = new Cookie("visited", (String)null);
                c.setMaxAge(0);
                c.setPath("/");
                response.addCookie(c);
            }

            return false;
        } else {
            return false;
        }
    }

    public static boolean mobileRequest(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return Strings.isEmptyOrNull(userAgent) ? false : MOBILE_USER_AGENT_PATTERN.matcher(userAgent).find();
    }

    public static JSONObject buildPaginationRequest(String path) {
        Integer currentPageNum = getCurrentPageNum(path);
        Integer pageSize = getPageSize(path);
        Integer windowSize = getWindowSize(path);
        JSONObject ret = new JSONObject();
        ret.put("paginationCurrentPageNum", currentPageNum);
        ret.put("paginationPageSize", pageSize);
        ret.put("paginationWindowSize", windowSize);
        return ret;
    }

    public static int getCurrentPageNum(String path) {
        LOGGER.trace("Getting current page number[path={0}]", new Object[]{path});
        if (!Strings.isEmptyOrNull(path) && !path.equals("/")) {
            String currentPageNumber = path.split("/")[0];
            return !Strings.isNumeric(currentPageNumber) ? 1 : Integer.valueOf(currentPageNumber);
        } else {
            return 1;
        }
    }

    public static int getPageSize(String path) {
        LOGGER.trace( "Page number[string={0}]", new Object[]{path});
        if (Strings.isEmptyOrNull(path)) {
            return 15;
        } else {
            String[] parts = path.split("/");
            if (1 >= parts.length) {
                return 15;
            } else {
                String pageSize = parts[1];
                return !Strings.isNumeric(pageSize) ? 15 : Integer.valueOf(pageSize);
            }
        }
    }

    public static int getWindowSize(String path) {
        LOGGER.trace("Page number[string={0}]", new Object[]{path});
        if (Strings.isEmptyOrNull(path)) {
            return 20;
        } else {
            String[] parts = path.split("/");
            if (2 >= parts.length) {
                return 20;
            } else {
                String windowSize = parts[2];
                return !Strings.isNumeric(windowSize) ? 20 : Integer.valueOf(windowSize);
            }
        }
    }

    public static JSONObject parseRequestJSONObject(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json");
        StringBuilder sb = new StringBuilder();
        String errMsg = "Can not parse request[requestURI=" + request.getRequestURI() + ", method=" + request.getMethod() + "], returns an empty json object";

        try {
            BufferedReader reader;
            try {
                reader = request.getReader();
            } catch (IllegalStateException var7) {
                reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
            }

            for(String line = reader.readLine(); null != line; line = reader.readLine()) {
                sb.append(line);
            }

            reader.close();
            String tmp = sb.toString();
            if (Strings.isEmptyOrNull(tmp)) {
                tmp = "{}";
            }

            return new JSONObject(tmp);
        } catch (Exception var8) {
            LOGGER.error(errMsg, var8);
            return new JSONObject();
        }
    }
}