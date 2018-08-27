package cn.he.zhao.bbs.advice;

import cn.he.zhao.bbs.exception.RequestProcessAdviceException;
import cn.he.zhao.bbs.entity.Common;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.util.Sessions;
import org.apache.commons.lang.StringUtils;
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

/**
 * 描述:
 * CSRFCheck
 *
 * @Author HeFeng
 * @Create 2018-07-28 15:54
 */

@Aspect
@Component
public class CSRFCheck {
    final static Logger log = LoggerFactory.getLogger(CSRFCheck.class);

    @Autowired
    private LangPropsService langPropsService;

    @Pointcut("@annotation(csrfCheckAnno)")
    public void csrfCheck(CSRFCheckAnno csrfCheckAnno) {
    }

    @Before("csrfCheck(csrfCheckAnno)")
    public void doBefore(JoinPoint joinPoint, CSRFCheckAnno csrfCheckAnno) throws RequestProcessAdviceException {

        final JSONObject exception = new JSONObject();
        exception.put(Keys.MSG, langPropsService.get("csrfCheckFailedLabel"));
        exception.put(Keys.STATUS_CODE, false);

        // 1. Check Referer
        HttpServletRequest request = SpringUtil.getCurrentRequest();
        final String referer = request.getHeader("Referer");
        if (!StringUtils.startsWith(referer, SpringUtil.getServerPath())) {
            throw new RequestProcessAdviceException(exception);
        }

        // 2. Check Token
        final String clientToken = request.getHeader(Common.CSRF_TOKEN);
        final String serverToken = Sessions.getCSRFToken(request);

        if (!StringUtils.equals(clientToken, serverToken)) {
            throw new RequestProcessAdviceException(exception);
        }
    }
}