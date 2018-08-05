package cn.he.zhao.bbs.advice;

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

/**
 * 描述:
 * AnonymousViewCheck
 *
 * @Author HeFeng
 * @Create 2018-07-28 15:51
 */

@Aspect
@Component
public class AnonymousViewCheck {

    final static Logger log = LoggerFactory.getLogger(AnonymousViewCheck.class);

    ThreadLocal<Long> beginTime = new ThreadLocal<>();

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Pointcut("@annotation(anonymousViewCheckAnno)")
    public void anonymousViewCheck(AnonymousViewCheckAnno anonymousViewCheckAnno) {
    }

    @Before("anonymousViewCheck(anonymousViewCheckAnno)")
    public void doBefore(JoinPoint joinPoint, AnonymousViewCheckAnno anonymousViewCheckAnno) {
        // 记录请求到达时间
        beginTime.set(System.currentTimeMillis());
        log.info("msg:{}", anonymousViewCheckAnno.value());
    }

    @After("anonymousViewCheck(anonymousViewCheckAnno)")
    public void doAfter(AnonymousViewCheckAnno anonymousViewCheckAnno) {
        log.info("cy666 statistic time:{}, msg:{}", System.currentTimeMillis() - beginTime.get(), anonymousViewCheckAnno.value());
    }
}