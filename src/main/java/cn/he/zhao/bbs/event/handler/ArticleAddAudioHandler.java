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
import cn.he.zhao.bbs.model.Article;
import cn.he.zhao.bbs.util.URLs;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Article add audio handler.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 18, 2017
 * @since 2.1.0
 */
@Component
public class ArticleAddAudioHandler implements ApplicationListener<AddArticleEvent> {

    public static final Logger LOGGER = LoggerFactory.getLogger(ArticleAddAudioHandler.class);

    @Override
    public void onApplicationEvent(AddArticleEvent event) {
        final JSONObject data = event.event;
        LOGGER.trace("Processing an event [type={0}, data={1}]", data);

        final JSONObject originalArticle = data.optJSONObject(Article.ARTICLE);
        final String authorId = originalArticle.optString(Article.ARTICLE_AUTHOR_ID);

//        articleMgmtService.genArticleAudio(originalArticle, authorId);
    }
}
