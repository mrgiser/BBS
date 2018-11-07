package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entityUtil.ArticleUtil;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.util.Markdowns;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.entity.*;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.net.UnknownHostException;

/**
 * Search management service.
 * <p>
 * Uses <a href="https://www.elastic.co/products/elasticsearch">Elasticsearch</a> or
 * <a href="https://www.algolia.com">Algolia</a> as the underlying engine.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.2.6, May 22, 2018
 * @since 1.4.0
 */
@Service
public class SearchMgmtService {

    /**
     * Elasticsearch index name.
     */
    public static final String ES_INDEX_NAME = "symphony";

    /**
     * Elasticsearch serve address.
     */
    public static final String ES_SERVER = Symphonys.get("es.server");

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchMgmtService.class);

    /**
     * URL fetch service.
     */
//    private static final URLFetchService URL_FETCH_SVC = URLFetchServiceFactory.getURLFetchService();

    private static RequestConfig defaultRequestConfig = RequestConfig.custom().setConnectTimeout(3000)
            .setConnectionRequestTimeout(1000).setSocketTimeout(10000).build();

    /**
     * Rebuilds ES index.
     */
    public void rebuildESIndex() {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {
            final HttpDelete removeRequest = new HttpDelete(ES_SERVER + "/" + ES_INDEX_NAME);
            removeRequest.setConfig(defaultRequestConfig);
            httpClient.execute(removeRequest);
//            removeRequest.setRequestMethod(HTTPRequestMethod.DELETE);
//            removeRequest.setURL(new URL(ES_SERVER + "/" + ES_INDEX_NAME));
//            URL_FETCH_SVC.fetch(removeRequest);

            final HttpPut createRequest = new HttpPut(ES_SERVER + "/" + ES_INDEX_NAME);
            createRequest.setConfig(defaultRequestConfig);
            httpClient.execute(createRequest);
//            createRequest.setRequestMethod(HTTPRequestMethod.PUT);
//            createRequest.setURL(new URL(ES_SERVER + "/" + ES_INDEX_NAME));
//            URL_FETCH_SVC.fetch(createRequest);

            final HttpPost mappingRequest = new HttpPost(ES_SERVER + "/" + ES_INDEX_NAME + "/" + ArticleUtil.ARTICLE + "/_mapping");
//            mappingRequest.setRequestMethod(HTTPRequestMethod.POST);
//            mappingRequest.setURL(new URL(ES_SERVER + "/" + ES_INDEX_NAME + "/" + Article.ARTICLE + "/_mapping"));

            final JSONObject mapping = new JSONObject();
            final JSONObject article = new JSONObject();
            mapping.put(ArticleUtil.ARTICLE, article);
            final JSONObject properties = new JSONObject();
            article.put("properties", properties);
            final JSONObject title = new JSONObject();
            properties.put(ArticleUtil.ARTICLE_TITLE, title);
            title.put("type", "string");
            title.put("analyzer", "ik_smart");
            title.put("search_analyzer", "ik_smart");
            final JSONObject content = new JSONObject();
            properties.put(ArticleUtil.ARTICLE_CONTENT, content);
            content.put("type", "string");
            content.put("analyzer", "ik_smart");
            content.put("search_analyzer", "ik_smart");

            mappingRequest.setConfig(defaultRequestConfig);
            StringEntity strEntity = new StringEntity(mapping.toString(),"UTF-8");
            mappingRequest.setEntity(strEntity);
            httpClient.execute(mappingRequest);
//            mappingRequest.setPayload(mapping.toString().getBytes("UTF-8"));

//            URL_FETCH_SVC.fetch(mappingRequest);
        } catch (final Exception e) {
            LOGGER.error( "Removes index failed", e);
        }
    }

    /**
     * Rebuilds Algolia index.
     */
    public void rebuildAlgoliaIndex() {
        final int maxRetries = 3;
        int retries = 1;

        final String appId = Symphonys.get("algolia.appId");
        final String index = Symphonys.get("algolia.index");
        final String key = Symphonys.get("algolia.adminKey");

        while (retries <= maxRetries) {
            String host = appId + "-" + retries + ".algolianet.com";

            CloseableHttpClient httpClient = HttpClients.createDefault();
            try {
                final HttpPost request = new HttpPost("https://" + host + "/1/indexes/" + index + "/clear");
                request.addHeader("X-Algolia-API-Key", key);
                request.addHeader("X-Algolia-Application-Id", appId);
//                request.addHeader(new HTTPHeader("X-Algolia-API-Key", key));
//                request.addHeader(new HTTPHeader("X-Algolia-Application-Id", appId));
//                request.setRequestMethod(HTTPRequestMethod.POST);

//                request.setURL(new URL("https://" + host + "/1/indexes/" + index + "/clear"));

                request.setConfig(defaultRequestConfig);
                final CloseableHttpResponse response = httpClient.execute(request);
//                final HTTPResponse response = URL_FETCH_SVC.fetch(request);
                StatusLine status = response.getStatusLine();
                int statusCode = status.getStatusCode();
                if (200 != statusCode) {
                    LOGGER.warn(response.toString());
                }

                break;
            } catch (final UnknownHostException e) {
                LOGGER.error( "Clear index failed [UnknownHostException=" + host + "]");

                retries++;

                if (retries > maxRetries) {
                    LOGGER.error( "Clear index failed [UnknownHostException]");
                }
            } catch (final Exception e) {
                LOGGER.error( "Clear index failed", e);

                break;
            }
        }
    }

    /**
     * Updates/Adds indexing the specified document in ES.
     *
     * @param doc  the specified document
     * @param type the specified document type
     */
    public void updateESDocument(final JSONObject doc, final String type) {

        CloseableHttpClient httpClient = HttpClients.createDefault();


        final HttpPost request = new HttpPost(ES_SERVER + "/" + ES_INDEX_NAME + "/" + type + "/" + doc.optString(Keys.OBJECT_ID) + "/_update");
//        request.setRequestMethod(HTTPRequestMethod.POST);

        try {
//            request.setURL(new URL(ES_SERVER + "/" + ES_INDEX_NAME + "/" + type + "/" + doc.optString(Keys.OBJECT_ID) + "/_update"));

            final JSONObject payload = new JSONObject();
            payload.put("doc", doc);
            payload.put("upsert", doc);

            request.setConfig(defaultRequestConfig);
            StringEntity strEntity = new StringEntity(payload.toString(),"UTF-8");
            request.setEntity(strEntity);
            httpClient.execute(request);
//            request.setPayload(payload.toString().getBytes("UTF-8"));

//            URL_FETCH_SVC.fetchAsync(request);
        } catch (final Exception e) {
            LOGGER.error( "Updates doc failed", e);
        }
    }

    /**
     * Removes the specified document in ES.
     *
     * @param doc  the specified document
     * @param type the specified document type
     */
    public void removeESDocument(final BaseEntity doc, final String type) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        final HttpDelete request = new HttpDelete(ES_SERVER + "/" + ES_INDEX_NAME + "/" + type + "/" + doc.getOid());
//        request.setRequestMethod(HTTPRequestMethod.DELETE);

        try {
//            request.setURL(new URL(ES_SERVER + "/" + ES_INDEX_NAME + "/" + type + "/" + doc.getOid()));

            request.setConfig(defaultRequestConfig);
            httpClient.execute(request);
//            URL_FETCH_SVC.fetchAsync(request);
        } catch (final Exception e) {
            LOGGER.error( "Updates doc failed", e);
        }
    }

    /**
     * Updates/Adds indexing the specified document in Algolia.
     *
     * @param doc the specified document
     */
    public void updateAlgoliaDocument(final JSONObject doc) {
        final int maxRetries = 3;
        int retries = 1;

        final String appId = Symphonys.get("algolia.appId");
        final String index = Symphonys.get("algolia.index");
        final String key = Symphonys.get("algolia.adminKey");

        while (retries <= maxRetries) {
            String host = appId + "-" + retries + ".algolianet.com";

            CloseableHttpClient httpClient = HttpClients.createDefault();
            try {
                final String id = doc.optString(Keys.OBJECT_ID);

                final HttpPut request = new HttpPut("https://" + host + "/1/indexes/" + index + "/" + id);
                request.addHeader("X-Algolia-API-Key", key);
                request.addHeader("X-Algolia-Application-Id", appId);
//                request.addHeader(new HTTPHeader("X-Algolia-API-Key", key));
//                request.addHeader(new HTTPHeader("X-Algolia-Application-Id", appId));
//                request.setRequestMethod(HTTPRequestMethod.PUT);

                String content = doc.optString(ArticleUtil.ARTICLE_CONTENT);
                content = Markdowns.toHTML(content);
                content = Jsoup.parse(content).text();

                doc.put(ArticleUtil.ARTICLE_CONTENT, content);
                final byte[] data = doc.toString().getBytes("UTF-8");

                if (content.length() < 32) {
                    LOGGER.info( "This article is too small [length=" + data.length + "], so skip it [title="
                            + doc.optString(ArticleUtil.ARTICLE_TITLE) + ", id=" + id + "]");
                    return;
                }

                if (data.length > 102400) {
                    LOGGER.info( "This article is too big [length=" + data.length + "], so skip it [title="
                            + doc.optString(ArticleUtil.ARTICLE_TITLE) + ", id=" + id + "]");
                    return;
                }

//                request.setURL(new URL("https://" + host + "/1/indexes/" + index + "/" + id));
//                StringEntity strEntity = new StringEntity(data);
                ByteArrayEntity byteArrayEntity = new ByteArrayEntity(data);
                request.setEntity(byteArrayEntity);
                request.setConfig(defaultRequestConfig);
//                request.setPayload(data);

                final CloseableHttpResponse response = httpClient.execute(request);
                StatusLine status = response.getStatusLine();
                int statusCode = status.getStatusCode();
                if (200 != statusCode) {
                    HttpEntity entity = response.getEntity();
                    String responseStr = EntityUtils.toString(entity);
                    LOGGER.warn(responseStr);
                }

//                final HTTPResponse response = URL_FETCH_SVC.fetch(request);
//                if (200 != response.getResponseCode()) {
//                    LOGGER.warn(new String(response.getContent(), "UTF-8"));
//                }

                break;
            } catch (final UnknownHostException e) {
                LOGGER.warn( "Index failed [UnknownHostException=" + host + "]");

                retries++;

                if (retries > maxRetries) {
                    LOGGER.error( "Index failed [UnknownHostException], doc [" + doc + "]");
                }
            } catch (final Exception e) {
                LOGGER.error( "Index failed [doc=" + doc + "]", e);

                break;
            }

            try {
                Thread.sleep(100);
            } catch (final Exception e) {
                LOGGER.error( "Sleep error", e);
            }
        }
    }

    /**
     * Removes the specified document in Algolia.
     *
     * @param doc the specified document
     */
    public void removeAlgoliaDocument(final BaseEntity doc) {
        final int maxRetries = 3;
        int retries = 1;

        final String appId = Symphonys.get("algolia.appId");
        final String index = Symphonys.get("algolia.index");
        final String key = Symphonys.get("algolia.adminKey");

        while (retries <= maxRetries) {
            String host = appId + "-" + retries + ".algolianet.com";

            CloseableHttpClient httpClient = HttpClients.createDefault();
            try {
                final String id = doc.getOid();
                final HttpDelete request = new HttpDelete("https://" + host + "/1/indexes/" + index + "/" + id);
                request.addHeader("X-Algolia-API-Key", key);
                request.addHeader("X-Algolia-Application-Id", appId);
//                request.addHeader(new HTTPHeader("X-Algolia-API-Key", key));
//                request.addHeader(new HTTPHeader("X-Algolia-Application-Id", appId));
//                request.setRequestMethod(HTTPRequestMethod.DELETE);


//                request.setURL(new URL("https://" + host + "/1/indexes/" + index + "/" + id));

                // TODO: 2018/11/7 没有增加strEntity
//                StringEntity strEntity = new StringEntity(doc.toString(),"UTF-8");
//                request.setEntity(strEntity);

//                request.setPayload(doc.toString().getBytes("UTF-8"));

                request.setConfig(defaultRequestConfig);
                final CloseableHttpResponse response = httpClient.execute(request);
                StatusLine status = response.getStatusLine();
                int statusCode = status.getStatusCode();
//                final HTTPResponse response = URL_FETCH_SVC.fetch(request);
                if (200 != statusCode) {
                    LOGGER.warn(response.toString());
                }

                break;
            } catch (final UnknownHostException e) {
                LOGGER.warn( "Remove object failed [UnknownHostException=" + host + "]");

                retries++;

                if (retries > maxRetries) {
                    LOGGER.error( "Remove object failed [UnknownHostException]");
                }
            } catch (final Exception e) {
                LOGGER.error( "Remove object failed", e);

                break;
            }
        }
    }
}
