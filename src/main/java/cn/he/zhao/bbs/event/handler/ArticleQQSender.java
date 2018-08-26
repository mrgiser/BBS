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
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.util.URLs;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.net.URL;

/**
 * Sends an article to QQ qun via <a href="https://github.com/b3log/xiaov">XiaoV</a>.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.4.0.2, May 29, 2016
 * @since 1.4.0
 */
@Component
public class ArticleQQSender implements ApplicationListener<AddArticleEvent> {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(ArticleQQSender.class);

    /**
     * URL fetch service.
     */
    private static final URLFetchService URL_FETCH_SVC = URLFetchServiceFactory.getURLFetchService();

    private void sendToXiaoV(final String msg) {
        final String xiaovAPI = Symphonys.get("xiaov.api");
        final String xiaovKey = Symphonys.get("xiaov.key");

        final HTTPRequest request = new HTTPRequest();
        request.setRequestMethod(HTTPRequestMethod.POST);

        try {
            request.setURL(new URL(xiaovAPI + "/qq"));

            final String body = "key=" + URLs.encode(xiaovKey)
                    + "&msg=" + URLs.encode(msg)
                    + "&user=" + URLs.encode("sym");
            request.setPayload(body.getBytes("UTF-8"));

            final HTTPResponse response = URL_FETCH_SVC.fetch(request);
            final int sc = response.getResponseCode();
            if (HttpServletResponse.SC_OK != sc) {
                LOGGER.warn("Sends message to XiaoV status code is [" + sc + "]");
            }
        } catch (final Exception e) {
            LOGGER.error( "Sends message to XiaoV failed: " + e.getMessage());
        }
    }

    @Override
    public void onApplicationEvent(AddArticleEvent event) {
        final JSONObject data = event.event;
        LOGGER.trace("Processing an event [type={0}, data={1}]", data);

        if (!Symphonys.getBoolean("xiaov.enabled")) {
            return;
        }

        try {
            final JSONObject article = data.getJSONObject(Article.ARTICLE);
            final int articleType = article.optInt(Article.ARTICLE_TYPE);
            if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType || Article.ARTICLE_TYPE_C_THOUGHT == articleType) {
                return;
            }

            final String title = article.optString(Article.ARTICLE_TITLE);
            final String permalink = article.optString(Article.ARTICLE_PERMALINK);
            final String msg = title + " " + SpringUtil.getServerPath() + permalink;
            sendToXiaoV(msg);

        } catch (final Exception e) {
            LOGGER.error("Sends the article to QQ group error", e);
        }
    }
}
