package cn.he.zhao.bbs.exception;

import org.json.JSONObject;

/**
 * 描述:
 * RequestProcessAdviceException
 *
 * @Author HeFeng
 * @Create 2018-07-28 16:54
 */

public class RequestProcessAdviceException extends Exception {
    private JSONObject jsonObject;
    private static final long serialVersionUID = 4070666571307478762L;

    public RequestProcessAdviceException(JSONObject jsonObject) {
        super(jsonObject.toString());
        this.jsonObject = jsonObject;
    }

    public JSONObject getJsonObject() {
        return this.jsonObject;
    }
}