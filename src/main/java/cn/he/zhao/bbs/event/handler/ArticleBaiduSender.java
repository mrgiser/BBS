/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.he.zhao.bbs.event.handler;

import cn.he.zhao.bbs.event.AddArticleEvent;
import cn.he.zhao.bbs.entity.Article;
import cn.he.zhao.bbs.entity.Tag;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.util.Symphonys;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
public class ArticleBaiduSender implements ApplicationListener<AddArticleEvent> {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(ArticleBaiduSender.class);

    /**
     * Baidu data token.
     */
    private static final String TOKEN = Symphonys.get("baidu.data.token");

    /**
     * Sends the specified URLs to Baidu.
     *
     * @param urls the specified URLs
     */
    public static void sendToBaidu( final String... urls) {
        if (ArrayUtils.isEmpty(urls)) {
            return;
        }

        new Thread(() -> {
            try {
                final URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();

                final HTTPRequest request = new HTTPRequest();
                request.setURL(new URL("http://data.zz.baidu.com/urls?site=" + SpringUtil.getServerHost() + "&token=" + TOKEN));
                request.setRequestMethod(HTTPRequestMethod.POST);
                request.addHeader(new HTTPHeader(Common.USER_AGENT, "curl/7.12.1"));
                request.addHeader(new HTTPHeader("Host", "data.zz.baidu.com"));
                request.addHeader(new HTTPHeader("Content-Type", "text/plain"));
                request.addHeader(new HTTPHeader("Connection", "close"));

                final String urlsStr = StringUtils.join(urls, "\n");
                request.setPayload(urlsStr.getBytes());

                final HTTPResponse response = urlFetchService.fetch(request);
                LOGGER.info(new String(response.getContent(), "UTF-8"));

                LOGGER.debug("Sent [" + urlsStr + "] to Baidu");
            } catch (final Exception e) {
                LOGGER.error("Ping Baidu spider failed", e);
            }
        }).start();
    }

    @Override
    public void onApplicationEvent(AddArticleEvent event) {
        final JSONObject data = event.event;
        LOGGER.trace( "Processing an event [type={0}, data={1}]",  data);

        if (SpringUtil.RuntimeMode.PRO != SpringUtil.getRuntimeMode() || StringUtils.isBlank(TOKEN)) {
            return;
        }

        try {
            final JSONObject article = data.getJSONObject(Article.ARTICLE);
            final int articleType = article.optInt(Article.ARTICLE_TYPE);
            if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType || Article.ARTICLE_TYPE_C_THOUGHT == articleType) {
                return;
            }

            final String tags = article.optString(Article.ARTICLE_TAGS);
            if (StringUtils.containsIgnoreCase(tags, Tag.TAG_TITLE_C_SANDBOX)) {
                return;
            }

            final String articlePermalink = SpringUtil.getServerPath() + article.optString(Article.ARTICLE_PERMALINK);

            sendToBaidu(articlePermalink);
        } catch (final Exception e) {
            LOGGER.error("Sends the article to Baidu error", e);
        }
    }

//    public void action(final Event<JSONObject> event) throws EventException {
//        final JSONObject data = event.getData();
//        LOGGER.trace( "Processing an event [type={0}, data={1}]", event.getType(), data);
//
//        if (Latkes.RuntimeMode.PRODUCTION != Latkes.getRuntimeMode() || StringUtils.isBlank(TOKEN)) {
//            return;
//        }
//
//        try {
//            final JSONObject article = data.getJSONObject(Article.ARTICLE);
//            final int articleType = article.optInt(Article.ARTICLE_TYPE);
//            if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType || Article.ARTICLE_TYPE_C_THOUGHT == articleType) {
//                return;
//            }
//
//            final String tags = article.optString(Article.ARTICLE_TAGS);
//            if (StringUtils.containsIgnoreCase(tags, Tag.TAG_TITLE_C_SANDBOX)) {
//                return;
//            }
//
//            final String articlePermalink =  SpringUtil.getServerPath() + article.optString(Article.ARTICLE_PERMALINK);
//
//            sendToBaidu(articlePermalink);
//        } catch (final Exception e) {
//            LOGGER.error( "Sends the article to Baidu error", e);
//        }
//    }

}
