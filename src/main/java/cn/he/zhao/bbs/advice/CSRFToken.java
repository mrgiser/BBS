package cn.he.zhao.bbs.advice;

import cn.he.zhao.bbs.model.Common;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.util.Sessions;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 描述:
 * CSRFToken
 *
 * @Author HeFeng
 * @Create 2018-07-28 15:57
 */

@Aspect
@Component
public class CSRFToken {
    final static Logger log = LoggerFactory.getLogger(CSRFCheck.class);

    ThreadLocal<Long> beginTime = new ThreadLocal<>();

    @Pointcut("@annotation(csrfCheckAnno)")
    public void CSRFToken(CSRFCheckAnno csrfCheckAnno) {
    }

    @After("CSRFToken(csrfCheckAnno)")
    public void doAfter(JoinPoint joinPoint, CSRFCheckAnno csrfCheckAnno) {
        Object[] obj = joinPoint.getArgs();
        for (Object argItem : obj) {
            System.out.println("---->now-->argItem:" + argItem);
            if (argItem instanceof Map) {
                Map dataModel = (Map) argItem;
                HttpServletRequest request = SpringUtil.getCurrentRequest();
                dataModel.put(Common.CSRF_TOKEN, Sessions.getCSRFToken(request));
            }
            System.out.println("---->after-->argItem:" + argItem);
        }

//        if (null != renderer) {
//            final Map<String, Object> dataModel = renderer.getRenderDataModel();
//
//            dataModel.put(Common.CSRF_TOKEN, Sessions.getCSRFToken(context.getRequest()));
//        }
    }
}