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
package cn.he.zhao.bbs.controller;

import cn.he.zhao.bbs.cache.ArticleCache;
import cn.he.zhao.bbs.cache.DomainCache;
import cn.he.zhao.bbs.cache.TagCache;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.util.Symphonys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Cache processor.
 * <ul>
 * <li>Refreshes cache (/cron/refresh-cache), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Apr 4, 2018
 * @since 2.6.0
 */
@Controller
public class CacheProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheProcessor.class);

    /**
     * Tag cache.
     */
    @Autowired
    private TagCache tagCache;

    /**
     * Domain cache.
     */
    @Autowired
    private DomainCache domainCache;

    /**
     * Article cache.
     */
    @Autowired
    private ArticleCache articleCache;

    /**
     * Refreshes cache.
     * <ul>
     * <li>Tags</li>
     * <li>Domains</li>
     * </ul>
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/cron/refresh-cache", method = RequestMethod.GET)
    public void refreshCache(Map<String, Object> dataModel,
                             final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        domainCache.loadDomains();
        articleCache.loadPerfectArticles();
        articleCache.loadSideHotArticles();
        articleCache.loadSideRandomArticles();
        tagCache.loadTags();

//        dataModel.put(Keys.STATUS_CODE,true);
        dataModel.put(Keys.STATUS_CODE,true);
    }
}
