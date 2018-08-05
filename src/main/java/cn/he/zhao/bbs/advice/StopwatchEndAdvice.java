package cn.he.zhao.bbs.advice;

import cn.he.zhao.bbs.spring.Stopwatchs;
import cn.he.zhao.bbs.spring.Strings;
import cn.he.zhao.bbs.model.Common;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
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
 * StopwatchEndAdvice
 *
 * @Author HeFeng
 * @Create 2018-07-28 16:07
 */

@Aspect
@Component
public class StopwatchEndAdvice {

    final static Logger log = LoggerFactory.getLogger(StopwatchEndAdvice.class);

    ThreadLocal<Long> beginTime = new ThreadLocal<>();

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Pointcut("@annotation(anno)")
    public void StopwatchEndAdvice(StopWatchEndAnno anno) {
    }

    @After("StopwatchEndAdvice(anno)")
    public void doAfter(JoinPoint joinPoint, StopWatchEndAnno anno) {
        Stopwatchs.end();

        Object[] obj = joinPoint.getArgs();
        for (Object argItem : obj) {
            if (argItem instanceof Map) {
                final Map<String, Object> dataModel = (Map) argItem;
                final String requestURI = request.getRequestURI();

                final long elapsed = Stopwatchs.getElapsed("Request URI [" + requestURI + ']');
                dataModel.put(Common.ELAPSED, elapsed);
            }
        }
//        if (null != renderer) {
//            final Map<String, Object> dataModel = renderer.getRenderDataModel();
//            final String requestURI = request.getRequestURI();
//
//            final long elapsed = Stopwatchs.getElapsed("Request URI [" + requestURI + ']');
//            dataModel.put(Common.ELAPSED, elapsed);
//        }

        log.trace( "Stopwatch: {0}    {1}", Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat());

    }
}