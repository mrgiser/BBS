package cn.he.zhao.bbs.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 描述:
 * SpringUtil
 *
 * @Author HeFeng
 * @Create 2018-08-05 15:12
 */
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * Symphony version.
     */
    public static final String VERSION = "3.1.0";

    @Value("${BBS.RuntimeMode}")
    private static String runtimeMode;

    @Value("${BBS.StaticServePath}")
    private static String StaticServePath;

    @Value("${BBS.staticResourceVersion}")
    private static String staticResourceVersion;

    private static String startupTimeMillis = String.valueOf(System.currentTimeMillis());

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(SpringUtil.applicationContext == null) {
            SpringUtil.applicationContext = applicationContext;
        }
    }

    //获取applicationContext
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    //通过name获取 Bean.
    public static Object getBean(String name){
        return getApplicationContext().getBean(name);
    }

    //通过class获取Bean.
    public static <T> T getBean(Class<T> clazz){
        return getApplicationContext().getBean(clazz);
    }

    //通过name,以及Clazz返回指定的Bean
    public static <T> T getBean(String name,Class<T> clazz){
        return getApplicationContext().getBean(name, clazz);
    }

    public static String getServerPath(){
        HttpServletRequest request = SpringUtil.getCurrentRequest();
        String serverPath = request.getScheme() //当前链接使用的协议
                +"://" + request.getServerName()//服务器地址
                + ":" + request.getServerPort() //端口号
                + request.getContextPath(); //应用名称
        return serverPath;
    }

    public static String getContextPath(){
        HttpServletRequest request = SpringUtil.getCurrentRequest();
        String contextPath = request.getContextPath(); //应用名称
        return contextPath;
    }

    public static String getServerHost(){
        HttpServletRequest request = SpringUtil.getCurrentRequest();
        return request.getServerName(); //服务器地址
    }

    public static RuntimeMode getRuntimeMode(){
        if (null != runtimeMode) {
            return SpringUtil.RuntimeMode.valueOf(runtimeMode);
        } else {
            return SpringUtil.RuntimeMode.PRO;
        }
    }

    public static String getStaticServePath(){
        return StaticServePath;
    }

    public static HttpServletRequest getCurrentRequest() throws IllegalStateException {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new IllegalStateException("当前线程中不存在 Request 上下文");
        }
        return attrs.getRequest();
    }

    public static HttpServletResponse getCurrentResponse() throws IllegalStateException {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new IllegalStateException("当前线程中不存在 Request 上下文");
        }
        return attrs.getResponse();
    }

    public static String getStaticResourceVersion() {
        if (null == staticResourceVersion) {
            staticResourceVersion = startupTimeMillis;
        }

        return staticResourceVersion;
    }

    public static enum RuntimeMode {
        DEV,
        PRO;

        private RuntimeMode() {
        }
    }
}