package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.util.Symphonys;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TuringQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TuringQueryService.class);

    /**
     * Enabled Turing Robot or not.
     */
    private static final boolean TURING_ENABLED = Symphonys.getBoolean("turing.enabled");

    /**
     * Turing Robot API.
     */
    private static final String TURING_API = Symphonys.get("turing.api");

    /**
     * Turing Robot Key.
     */
    private static final String TURING_KEY = Symphonys.get("turing.key");

    /**
     * Robot name.
     */
    public static final String ROBOT_NAME = Symphonys.get("turing.name");

    /**
     * Robot avatar.
     */
    public static final String ROBOT_AVATAR = Symphonys.get("turing.avatar");

    /**
     * URL fetch service.
     */
//    private static final URLFetchService URL_FETCH_SVC = URLFetchServiceFactory.getURLFetchService();

    private static RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectTimeout(3000)
            .setConnectionRequestTimeout(1000).setSocketTimeout(10000).build();

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Chat with Turing Robot.
     *
     * @param userName the specified user name
     * @param msg      the specified message
     * @return robot returned message, return {@code null} if not found
     */
    public String chat(final String userName, final String msg) {
        if (StringUtils.isBlank(userName) || StringUtils.isBlank(msg) || !TURING_ENABLED) {
            return null;
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();



//        final HTTPRequest request = new HTTPRequest();
//        request.setRequestMethod(HTTPRequestMethod.POST);

        try {
            HttpPost httpPost = new HttpPost(TURING_API);
//            request.setURL(new URL(TURING_API));

            final JSONObject reqData = new JSONObject();
            reqData.put("reqType", 0);
            final JSONObject perception = new JSONObject();
            final JSONObject inputText = new JSONObject();
            inputText.put("text", msg);
            perception.put("inputText", inputText);
            reqData.put("perception", perception);
            final JSONObject userInfo = new JSONObject();
            userInfo.put("apiKey", TURING_KEY);
            userInfo.put("userId", userName);
            userInfo.put("userIdName", userName);
            reqData.put("userInfo", userInfo);

            httpPost.setConfig(defaultRequestConfig);

            StringEntity strEntity = new StringEntity(reqData.toString(),"UTF-8");
            httpPost.setEntity(strEntity);
//            request.setPayload(reqData.toString().getBytes("UTF-8"));

            final CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            StatusLine status = response.getStatusLine();
            int statusCode = status.getStatusCode();
            String responseStr = EntityUtils.toString(entity);
            JSONObject json = JSON.parseObject(responseStr);

            final JSONObject intent = json.getJSONObject("intent");
            final int code = intent.getInteger("code");
            final JSONArray results = json.getJSONArray("results");
            switch (code) {
                case 5000:
                case 6000:
                case 4000:
                case 4001:
                case 4002:
                case 4003:
                case 4005:
                case 4007:
                case 4100:
                case 4200:
                case 4300:
                case 4400:
                case 4500:
                case 4600:
                case 4602:
                case 7002:
                case 8008:
                    LOGGER.error( "Turing query failed with code [" + code + "]");

                    return langPropsService.get("turingQuotaExceedLabel");
                case 10004:
                case 10019:
                case 10014:
                case 10013:
                case 10008:
                case 10011:
                    final StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < results.size(); i++) {
                        final JSONObject result = results.getJSONObject(i);
                        final String resultType = result.getString("resultType");
                        String values = result.getJSONObject("values").getString(resultType);
                        if (StringUtils.endsWithAny(values, new String[]{"jpg", "png", "gif"})) {
                            values = "![](" + values + ")";
                        }

                        builder.append(values).append("\n");
                    }
                    String ret = builder.toString();
                    ret = StringUtils.trim(ret);

                    return ret;
                default:
                    LOGGER.warn( "Turing Robot default return [" + json.toString() + "]");

                    return langPropsService.get("turingQuotaExceedLabel");
            }
        } catch (final Exception e) {
            LOGGER.error( "Chat with Turing Robot failed", e);
        }

        return null;
    }
}
