package cn.he.zhao.bbs.advice;

import cn.he.zhao.bbs.exception.RequestProcessAdviceException;
import cn.he.zhao.bbs.spring.Stopwatchs;
import cn.he.zhao.bbs.model.Permission;
import cn.he.zhao.bbs.model.Role;
import cn.he.zhao.bbs.model.my.Keys;
import cn.he.zhao.bbs.model.my.User;
import cn.he.zhao.bbs.service.RoleQueryService;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.util.Symphonys;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 描述:
 * PermissionCheck
 *
 * @Author HeFeng
 * @Create 2018-07-28 16:04
 */

@Aspect
@Component
public class PermissionCheck {
    final static Logger log = LoggerFactory.getLogger(PermissionCheck.class);

    /**
     * URL permission rules.
     * <p>
     * &lt;"url:method", permissions&gt;
     * </p>
     */
    private static final Map<String, Set<String>> URL_PERMISSION_RULES = new HashMap<>();

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;
    /**
     * Role query service.
     */
    @Autowired
    private RoleQueryService roleQueryService;

    static {
        // Loads permission URL rules
        final String prefix = "permission.rule.url.";

        final Set<String> keys = Symphonys.CFG.keySet();
        for (final String key : keys) {
            if (key.startsWith(prefix)) {
                final String value = Symphonys.CFG.getString(key);
                final Set<String> permissions = new HashSet<>(Arrays.asList(value.split(",")));

                URL_PERMISSION_RULES.put(key, permissions);
            }
        }
    }

    @Pointcut("@annotation(anno)")
    public void PermissionCheck(PermissionCheckAnno anno) {
    }

    @Before("PermissionCheck(anno)")
    public void doBefore(JoinPoint joinPoint, PermissionCheckAnno anno) throws RequestProcessAdviceException {
        Stopwatchs.start("Check Permissions");

        try {

            final JSONObject exception = new JSONObject();
            exception.put(Keys.MSG, langPropsService.get("noPermissionLabel"));
            exception.put(Keys.STATUS_CODE, HttpServletResponse.SC_FORBIDDEN);

            final String prefix = "permission.rule.url.";
            final String requestURI = request.getRequestURI();
            final String method = request.getMethod();
            String rule = prefix;

            final RequestDispatchHandler requestDispatchHandler
                    = (RequestDispatchHandler) DispatcherServlet.SYS_HANDLER.get(2 /* DispatcherServlet#L69 */);

            try {
                final Method doMatch = RequestDispatchHandler.class.getDeclaredMethod("doMatch",
                        String.class, String.class);
                doMatch.setAccessible(true);
                final MatchResult matchResult = (MatchResult) doMatch.invoke(requestDispatchHandler, requestURI, method);

                rule += matchResult.getMatchedPattern() + "." + method;
            } catch (final Exception e) {
                log.error( "Match method failed", e);

                throw new RequestProcessAdviceException(exception);
            }

            final Set<String> requisitePermissions = URL_PERMISSION_RULES.get(rule);
            if (null == requisitePermissions) {
                return;
            }

            final JSONObject user = (JSONObject) request.getAttribute(User.USER);
            final String roleId = null != user ? user.optString(User.USER_ROLE) : Role.ROLE_ID_C_VISITOR;
            final Set<String> grantPermissions = roleQueryService.getPermissions(roleId);

            if (!Permission.hasPermission(requisitePermissions, grantPermissions)) {
                throw new RequestProcessAdviceException(exception);
            }
        } finally {
            Stopwatchs.end();
        }
    }
}