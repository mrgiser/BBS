package cn.he.zhao.bbs.event;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

/**
 * 描述:
 * Add Comment
 *
 * @Author HeFeng
 * @Create 2018-07-27 17:27
 */
public class AddCommentEvent extends ApplicationEvent {

    public static final Logger LOGGER = LoggerFactory.getLogger(AddCommentEvent.class);
    public final JSONObject event;
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public AddCommentEvent(Object source, JSONObject event) {
        super(source);
        this.event = event;
    }

    /**
     * 自定义监听器触发的透传打印方法
     */
    public void printMsg()
    {
        LOGGER.trace("Processing an event [data={1}]",  event);
    }
}