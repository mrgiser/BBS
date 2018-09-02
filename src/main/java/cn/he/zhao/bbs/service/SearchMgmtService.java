package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.entity.*;

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
    private static final URLFetchService URL_FETCH_SVC = URLFetchServiceFactory.getURLFetchService();

    /**
     * Rebuilds ES index.
     */
    public void rebuildESIndex() {
        try {
            final HTTPRequest removeRequest = new HTTPRequest();
            removeRequest.setRequestMethod(HTTPRequestMethod.DELETE);
            removeRequest.setURL(new URL(ES_SERVER + "/" + ES_INDEX_NAME));
            URL_FETCH_SVC.fetch(removeRequest);

            final HTTPRequest createRequest = new HTTPRequest();
            createRequest.setRequestMethod(HTTPRequestMethod.PUT);
            createRequest.setURL(new URL(ES_SERVER + "/" + ES_INDEX_NAME));
            URL_FETCH_SVC.fetch(createRequest);

            final HTTPRequest mappingRequest = new HTTPRequest();
            mappingRequest.setRequestMethod(HTTPRequestMethod.POST);
            mappingRequest.setURL(new URL(ES_SERVER + "/" + ES_INDEX_NAME + "/" + Article.ARTICLE + "/_mapping"));

            final JSONObject mapping = new JSONObject();
            final JSONObject article = new JSONObject();
            mapping.put(Article.ARTICLE, article);
            final JSONObject properties = new JSONObject();
            article.put("properties", properties);
            final JSONObject title = new JSONObject();
            properties.put(Article.ARTICLE_TITLE, title);
            title.put("type", "string");
            title.put("analyzer", "ik_smart");
            title.put("search_analyzer", "ik_smart");
            final JSONObject content = new JSONObject();
            properties.put(Article.ARTICLE_CONTENT, content);
            content.put("type", "string");
            content.put("analyzer", "ik_smart");
            content.put("search_analyzer", "ik_smart");

            mappingRequest.setPayload(mapping.toString().getBytes("UTF-8"));

            URL_FETCH_SVC.fetch(mappingRequest);
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

            try {
                final HTTPRequest request = new HTTPRequest();
                request.addHeader(new HTTPHeader("X-Algolia-API-Key", key));
                request.addHeader(new HTTPHeader("X-Algolia-Application-Id", appId));
                request.setRequestMethod(HTTPRequestMethod.POST);

                request.setURL(new URL("https://" + host + "/1/indexes/" + index + "/clear"));

                final HTTPResponse response = URL_FETCH_SVC.fetch(request);
                if (200 != response.getResponseCode()) {
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
        final HTTPRequest request = new HTTPRequest();
        request.setRequestMethod(HTTPRequestMethod.POST);

        try {
            request.setURL(new URL(ES_SERVER + "/" + ES_INDEX_NAME + "/" + type + "/" + doc.optString(Keys.OBJECT_ID) + "/_update"));

            final JSONObject payload = new JSONObject();
            payload.put("doc", doc);
            payload.put("upsert", doc);

            request.setPayload(payload.toString().getBytes("UTF-8"));

            URL_FETCH_SVC.fetchAsync(request);
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
        final HTTPRequest request = new HTTPRequest();
        request.setRequestMethod(HTTPRequestMethod.DELETE);

        try {
            request.setURL(new URL(ES_SERVER + "/" + ES_INDEX_NAME + "/" + type + "/" + doc.getOid()));

            URL_FETCH_SVC.fetchAsync(request);
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

            try {
                final HTTPRequest request = new HTTPRequest();
                request.addHeader(new HTTPHeader("X-Algolia-API-Key", key));
                request.addHeader(new HTTPHeader("X-Algolia-Application-Id", appId));
                request.setRequestMethod(HTTPRequestMethod.PUT);

                final String id = doc.optString(Keys.OBJECT_ID);

                String content = doc.optString(Article.ARTICLE_CONTENT);
                content = Markdowns.toHTML(content);
                content = Jsoup.parse(content).text();

                doc.put(Article.ARTICLE_CONTENT, content);
                final byte[] data = doc.toString().getBytes("UTF-8");

                if (content.length() < 32) {
                    LOGGER.info( "This article is too small [length=" + data.length + "], so skip it [title="
                            + doc.optString(Article.ARTICLE_TITLE) + ", id=" + id + "]");
                    return;
                }

                if (data.length > 102400) {
                    LOGGER.info( "This article is too big [length=" + data.length + "], so skip it [title="
                            + doc.optString(Article.ARTICLE_TITLE) + ", id=" + id + "]");
                    return;
                }

                request.setURL(new URL("https://" + host + "/1/indexes/" + index + "/" + id));
                request.setPayload(data);

                final HTTPResponse response = URL_FETCH_SVC.fetch(request);
                if (200 != response.getResponseCode()) {
                    LOGGER.warn(new String(response.getContent(), "UTF-8"));
                }

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

            try {
                final HTTPRequest request = new HTTPRequest();
                request.addHeader(new HTTPHeader("X-Algolia-API-Key", key));
                request.addHeader(new HTTPHeader("X-Algolia-Application-Id", appId));
                request.setRequestMethod(HTTPRequestMethod.DELETE);

                final String id = doc.getOid();
                request.setURL(new URL("https://" + host + "/1/indexes/" + index + "/" + id));

                request.setPayload(doc.toString().getBytes("UTF-8"));

                final HTTPResponse response = URL_FETCH_SVC.fetch(request);
                if (200 != response.getResponseCode()) {
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
