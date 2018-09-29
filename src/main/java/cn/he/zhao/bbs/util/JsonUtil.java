package cn.he.zhao.bbs.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 描述:
 * json工具类
 *
 * @Author HeFeng
 * @Create 2018-09-03 14:31
 */
public class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);


    public static String objectToJson(Object data) {
        try {
            String string = MAPPER.writeValueAsString(data);
            return string;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONArray listToJSONArray(List list){
        JSONArray json = new JSONArray();
        for(Object pLog : list){
            String string = null;
            try {
                string = MAPPER.writeValueAsString(pLog);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            JSONObject jo = new JSONObject(string);

            json.put(jo);
        }
        return json;
    }

    public static void main(String[] args) throws Exception{

    }
}