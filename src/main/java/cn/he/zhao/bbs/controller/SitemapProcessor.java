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

import cn.he.zhao.bbs.advice.*;
import cn.he.zhao.bbs.model.*;
import cn.he.zhao.bbs.model.my.*;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Sitemap processor.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Sep 24, 2016
 * @since 1.6.0
 */
@Controller
public class SitemapProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SitemapProcessor.class);

    /**
     * Sitemap query service.
     */
    @Autowired
    private SitemapQueryService sitemapQueryService;

    /**
     * Returns the sitemap.
     *
     * @param context the specified context
     */
    @RequestMapping(value = "/sitemap.xml", method = RequestMethod.GET)
    public void sitemap(final HTTPRequestContext context) {
        final TextXMLRenderer renderer = new TextXMLRenderer();

        context.setRenderer(renderer);

        final Sitemap sitemap = new Sitemap();

        try {
            LOGGER.info( "Generating sitemap....");

            sitemapQueryService.genIndex(sitemap);
            sitemapQueryService.genDomains(sitemap);
            sitemapQueryService.genArticles(sitemap);

            final String content = sitemap.toString();

            LOGGER.info( "Generated sitemap");

            renderer.setContent(content);
        } catch (final Exception e) {
            LOGGER.error( "Get blog article feed error", e);

            try {
                context.getResponse().sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
