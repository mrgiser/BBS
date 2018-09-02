package cn.he.zhao.bbs.entity;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述:
 * JSONObject test
 *
 * @Author HeFeng
 * @Create 2018-09-02 13:27
 */
public class JSONObjectTest {
    public static final Logger logger = LoggerFactory.getLogger(JSONObjectTest.class);

    public static void main(String[] args) {
        JSONObject object = new JSONObject();
        char i = 0;
        char ii = 1;
        object.put("test",i);
        object.put("test2",ii);

        logger.info("test" + object.optBoolean("test"));
        logger.info("test2" + object.optBoolean("test2"));
    }

}