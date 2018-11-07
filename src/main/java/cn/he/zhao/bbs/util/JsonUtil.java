package cn.he.zhao.bbs.util;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.TypeReference;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

    private static ObjectMapper objectMapper;

    //init objectMapper
    static {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setSerializationInclusion(Inclusion.NON_NULL);
        objectMapper.disable(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES);
    }

    public static <T> T json2Bean(String json, Class<T> clazz) throws Exception {
        try {
            return objectMapper.readValue(json, clazz);
        } catch(Exception e) {
            LOGGER.error("convert json to bean failed", e);
            throw e;
        }
    }

    public static String objectToJson(Object data) {
        try {
            String string = MAPPER.writeValueAsString(data);
            return string;
        } catch (Exception e) {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            JSONObject jo = new JSONObject(string);

            json.put(jo);
        }
        return json;
    }

    public static List<JSONObject> listToJSONList(List list){
        List<JSONObject> jsons = new ArrayList<>();
        for(Object pLog : list){
            String string = null;
            try {
                string = MAPPER.writeValueAsString(pLog);
            } catch (Exception e) {
                e.printStackTrace();
            }
            JSONObject jo = new JSONObject(string);

            jsons.add(jo);
        }
        return jsons;
    }

    public static void main(String[] args) throws Exception{

    }
}