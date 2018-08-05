package cn.he.zhao.bbs.advice;

import cn.he.zhao.bbs.spring.Stopwatchs;
import org.aspectj.lang.JoinPoint;
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
 * StopwatchStartAdvice
 *
 * @Author HeFeng
 * @Create 2018-07-28 16:08
 */

@Aspect
@Component
public class StopwatchStartAdvice {

    final static Logger log = LoggerFactory.getLogger(StopwatchStartAdvice.class);

    ThreadLocal<Long> beginTime = new ThreadLocal<>();

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Pointcut("@annotation(anno)")
    public void StopwatchStartAdvice(StopWatchStartAnno anno) {
    }

    @Before("StopwatchStartAdvice(anno)")
    public void doBefore(JoinPoint joinPoint, StopWatchStartAnno anno) {
        final String requestURI = request.getRequestURI();
        Stopwatchs.start("Request URI [" + requestURI + ']');
    }
}