package cn.he.zhao.bbs.advice;

import cn.he.zhao.bbs.exception.RequestProcessAdviceException;
import cn.he.zhao.bbs.model.UserExt;
import cn.he.zhao.bbs.model.my.Keys;
import cn.he.zhao.bbs.model.my.User;
import cn.he.zhao.bbs.service.UserMgmtService;
import cn.he.zhao.bbs.service.UserQueryService;
import cn.he.zhao.bbs.spring.SpringUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 描述:
 * LoginCheck
 *
 * @Author HeFeng
 * @Create 2018-07-28 16:02
 */

@Aspect
@Component
public class LoginCheck {
    final static Logger log = LoggerFactory.getLogger(LoginCheck.class);

    ThreadLocal<Long> beginTime = new ThreadLocal<>();

    /**
     * User query service.
     */
    @Autowired
    private UserQueryService userQueryService;

    /**
     * User management service.
     */
    @Autowired
    private UserMgmtService userMgmtService;

    @Pointcut("@annotation(anno)")
    public void LoginCheck(LoginCheckAnno anno) {
    }

    @Before("LoginCheck(anno)")
    public void doBefore(JoinPoint joinPoint, LoginCheckAnno anno) throws RequestProcessAdviceException {

        final JSONObject exception = new JSONObject();
        HttpServletRequest request = SpringUtil.getCurrentRequest();
        exception.put(Keys.MSG, HttpServletResponse.SC_UNAUTHORIZED + ", " + request.getRequestURI());
        exception.put(Keys.STATUS_CODE, HttpServletResponse.SC_UNAUTHORIZED);

        try {
            JSONObject currentUser = userQueryService.getCurrentUser(request);
            HttpServletResponse response = SpringUtil.getCurrentResponse();
            if (null == currentUser && !userMgmtService.tryLogInWithCookie(request, response)) {
                throw new RequestProcessAdviceException(exception);
            }

            currentUser = userQueryService.getCurrentUser(request);
            final int point = currentUser.optInt(UserExt.USER_POINT);
            final int appRole = currentUser.optInt(UserExt.USER_APP_ROLE);
            if (UserExt.USER_APP_ROLE_C_HACKER == appRole) {
                currentUser.put(UserExt.USER_T_POINT_HEX, Integer.toHexString(point));
            } else {
                currentUser.put(UserExt.USER_T_POINT_CC, UserExt.toCCString(point));
            }

            request.setAttribute(User.USER, currentUser);
        } catch (final Exception e) {
            log.error("Login check failed");

            throw new RequestProcessAdviceException(exception);
        }
    }
}