package cn.he.zhao.bbs.advice;

import cn.he.zhao.bbs.exception.RequestProcessAdviceException;
import cn.he.zhao.bbs.entity.UserExt;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.service.UserQueryService;
import cn.he.zhao.bbs.spring.SpringUtil;
import org.aspectj.lang.JoinPoint;
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
 * UserBlockCheck
 *
 * @Author HeFeng
 * @Create 2018-07-28 16:06
 */

@Aspect
@Component
public class UserBlockCheck {
    final static Logger log = LoggerFactory.getLogger(UserBlockCheck.class);

    ThreadLocal<Long> beginTime = new ThreadLocal<>();

    /**
     * User query service.
     */
    @Autowired
    private UserQueryService userQueryService;

    @Pointcut("@annotation(anno)")
    public void UserBlockCheck(UserBlockCheckAnno anno) {
    }

    @Before("UserBlockCheck(anno)")
    public void doBefore(JoinPoint joinPoint, UserBlockCheckAnno anno) throws RequestProcessAdviceException{

        HttpServletRequest request = SpringUtil.getCurrentRequest();

        final JSONObject exception = new JSONObject();
        exception.put(Keys.MSG, HttpServletResponse.SC_NOT_FOUND);
        exception.put(Keys.STATUS_CODE, HttpServletResponse.SC_NOT_FOUND);

        String userName = null;
        Object[] obj = joinPoint.getArgs();
        for (Object argItem : obj) {
            if (argItem instanceof String) {
                userName = (String) argItem;
            }
        }


        if (UserExt.NULL_USER_NAME.equals(userName)) {
            exception.put(Keys.MSG, "Nil User [" + userName + ", requestURI=" + request.getRequestURI() + "]");
            throw new RequestProcessAdviceException(exception);
        }

        final JSONObject user = userQueryService.getUserByName(userName);
        if (null == user) {
            exception.put(Keys.MSG, "Not found user [" + userName + ", requestURI=" + request.getRequestURI() + "]");
            throw new RequestProcessAdviceException(exception);
        }

        if (UserExt.USER_STATUS_C_NOT_VERIFIED == user.optInt(UserExt.USER_STATUS)) {
            exception.put(Keys.MSG, "Unverified User [" + userName + ", requestURI=" + request.getRequestURI() + "]");
            throw new RequestProcessAdviceException(exception);
        }

        if (UserExt.USER_STATUS_C_INVALID == user.optInt(UserExt.USER_STATUS)) {
            exception.put(Keys.MSG, "Blocked User [" + userName + ", requestURI=" + request.getRequestURI() + "]");
            throw new RequestProcessAdviceException(exception);
        }

        request.setAttribute(User.USER, user);
    }
}