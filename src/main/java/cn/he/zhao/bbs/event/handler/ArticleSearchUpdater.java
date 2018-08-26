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

import cn.he.zhao.bbs.event.UpdateArticleEvent;
import cn.he.zhao.bbs.entity.Article;
import cn.he.zhao.bbs.entity.Tag;
import cn.he.zhao.bbs.service.SearchMgmtService;
import cn.he.zhao.bbs.util.JSONs;
import cn.he.zhao.bbs.util.Symphonys;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ArticleSearchUpdater implements ApplicationListener<UpdateArticleEvent> {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(ArticleSearchUpdater.class);

    /**
     * Search management service.
     */
    @Autowired
    private SearchMgmtService searchMgmtService;

    @Override
    public void onApplicationEvent(UpdateArticleEvent event) {
        final JSONObject data = event.event;
        LOGGER.trace( "Processing an event [type={0}, data={1}]", data);

        final JSONObject article = data.optJSONObject(Article.ARTICLE);
        if (Article.ARTICLE_TYPE_C_DISCUSSION == article.optInt(Article.ARTICLE_TYPE)
                || Article.ARTICLE_TYPE_C_THOUGHT == article.optInt(Article.ARTICLE_TYPE)) {
            return;
        }

        final String tags = article.optString(Article.ARTICLE_TAGS);
        if (StringUtils.containsIgnoreCase(tags, Tag.TAG_TITLE_C_SANDBOX)) {
            return;
        }

        if (Symphonys.getBoolean("algolia.enabled")) {
            searchMgmtService.updateAlgoliaDocument(JSONs.clone(article));
        }

        if (Symphonys.getBoolean("es.enabled")) {
            searchMgmtService.updateESDocument(JSONs.clone(article), Article.ARTICLE);
        }
    }

//    public void action(final Event<JSONObject> event) throws EventException {
//        final JSONObject data = event.getData();
//        LOGGER.trace( "Processing an event [type={0}, data={1}]", event.getType(), data);
//
//        final JSONObject article = data.optJSONObject(Article.ARTICLE);
//        if (Article.ARTICLE_TYPE_C_DISCUSSION == article.optInt(Article.ARTICLE_TYPE)
//                || Article.ARTICLE_TYPE_C_THOUGHT == article.optInt(Article.ARTICLE_TYPE)) {
//            return;
//        }
//
//        final String tags = article.optString(Article.ARTICLE_TAGS);
//        if (StringUtils.containsIgnoreCase(tags, Tag.TAG_TITLE_C_SANDBOX)) {
//            return;
//        }
//
//        if (Symphonys.getBoolean("algolia.enabled")) {
//            searchMgmtService.updateAlgoliaDocument(JSONs.clone(article));
//        }
//
//        if (Symphonys.getBoolean("es.enabled")) {
//            searchMgmtService.updateESDocument(JSONs.clone(article), Article.ARTICLE);
//        }
//    }
}
