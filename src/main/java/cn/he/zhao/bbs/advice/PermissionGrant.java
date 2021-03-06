package cn.he.zhao.bbs.advice;

import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.spring.Stopwatchs;
import cn.he.zhao.bbs.entity.Permission;
import cn.he.zhao.bbs.entity.Role;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.service.RoleQueryService;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 描述:
 * PermissionGrant
 *
 * @Author HeFeng
 * @Create 2018-07-28 16:05
 */

@Aspect
@Component
public class PermissionGrant {
    final static Logger log = LoggerFactory.getLogger(PermissionGrant.class);

    ThreadLocal<Long> beginTime = new ThreadLocal<>();

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;
    /**
     * RoleUtil query service.
     */
    @Autowired
    private RoleQueryService roleQueryService;

    @Pointcut("@annotation(anno)")
    public void PermissionGrant(PermissionGrantAnno anno) {
    }

    @After("PermissionGrant(anno)")
    public void doAfter(JoinPoint joinPoint, PermissionGrantAnno anno) {

        Stopwatchs.start("Grant permissions");
        HttpServletRequest request = SpringUtil.getCurrentRequest();

        Map<String, Object> dataModel = null;
        Object[] obj = joinPoint.getArgs();
        for (Object argItem : obj) {
            if (argItem instanceof Map) {
                dataModel = (Map) argItem;
                final String requestURI = request.getRequestURI();

                final long elapsed = Stopwatchs.getElapsed("Request URI [" + requestURI + ']');
                dataModel.put(Common.ELAPSED, elapsed);
            }
        }

        try {

            final JSONObject user = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String roleId = null != user ? user.optString(User.USER_ROLE) : Role.ROLE_ID_C_VISITOR;
            final Map<String, JSONObject> permissionsGrant = roleQueryService.getPermissionsGrantMap(roleId);
            dataModel.put(Permission.PERMISSIONS, permissionsGrant);

            final JSONObject role = roleQueryService.getRole(roleId);

            String noPermissionLabel = langPropsService.get("noPermissionLabel");
            noPermissionLabel = noPermissionLabel.replace("{roleName}", role.optString(Role.ROLE_NAME));
            dataModel.put("noPermissionLabel", noPermissionLabel);
        } finally {
            Stopwatchs.end();
        }
    }
}