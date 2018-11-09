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

import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.entityUtil.ArticleUtil;
import cn.he.zhao.bbs.entityUtil.OptionUtil;
import cn.he.zhao.bbs.entityUtil.UserExtUtil;
import cn.he.zhao.bbs.entityUtil.feed.RSSCategory;
import cn.he.zhao.bbs.entityUtil.feed.RSSChannel;
import cn.he.zhao.bbs.entityUtil.feed.RSSItem;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Locales;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.util.Emotions;
import cn.he.zhao.bbs.util.Symphonys;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Feed RSS processor.
 *
 * <ul>
 * <li>Generates recent articles' RSS (/rss/recent.xml), GET/HEAD</li>
 * <li>Generates domain articles' RSS (/rss/domain/{domainURL}.xml), GET/HEAD</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jul 5, 2018
 * @since 3.1.0
 */
@Controller
public class FeedProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedProcessor.class);

    /**
     * Article query service.
     */
    @Autowired
    private ArticleQueryService articleQueryService;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * OptionUtil query service.
     */
    @Autowired
    private OptionQueryService optionQueryService;

    /**
     * Domain query service.
     */
    @Autowired
    private DomainQueryService domainQueryService;

    /**
     * Generates recent articles' RSS.
     *
     */
    @RequestMapping(value = "/rss/recent.xml", method = {RequestMethod.GET, RequestMethod.HEAD})
    public void genRecentRSS(Map<String, Object> dataModel, final HttpServletResponse response) {
//        final RssRenderer renderer = new RssRenderer();
//        context.setRenderer(renderer);

        try {
            response.setContentType("application/rss+xml");
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();


            final RSSChannel channel = new RSSChannel();
            final JSONObject result = articleQueryService.getRecentArticles(UserExtUtil.USER_AVATAR_VIEW_MODE_C_STATIC, 0, 1, Symphonys.getInt("indexArticlesCnt"));
            final List<JSONObject> articles = (List<JSONObject>) result.get(ArticleUtil.ARTICLES);
            for (int i = 0; i < articles.size(); i++) {
                RSSItem item = getItem(articles, i);
                channel.addItem(item);
            }
            channel.setTitle(langPropsService.get("symphonyLabel"));
            channel.setLastBuildDate(new Date());
            channel.setLink( SpringUtil.getServerPath());
            channel.setAtomLink( SpringUtil.getServerPath() + "/rss/recent.xml");
            channel.setGenerator("Symphony v" + SpringUtil.VERSION + ", https://sym.b3log.org");
            final String localeString = optionQueryService.getOption("miscLanguage").getOptionValue();
            final String country = Locales.getCountry(localeString).toLowerCase();
            final String language = Locales.getLanguage(localeString).toLowerCase();
            channel.setLanguage(language + '-' + country);
            channel.setDescription(langPropsService.get("symDescriptionLabel"));

//            renderer.setContent(channel.toString());
            writer.write(channel.toString());
            writer.close();

        } catch (final Exception e) {
            LOGGER.error( "Generates recent articles' RSS failed", e);

            try {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Generates domain articles' RSS.
     *

     */
    @RequestMapping(value = "/rss/domain/{domainURI}.xml", method = {RequestMethod.GET, RequestMethod.HEAD})
    public void genDomainRSS(Map<String, Object> dataModel,final HttpServletResponse response, final String domainURI) {
//        final RssRenderer renderer = new RssRenderer();
//        context.setRenderer(renderer);

        try {
            response.setContentType("application/rss+xml");
            response.setCharacterEncoding("UTF-8");
            PrintWriter writer = response.getWriter();

            final Domain domain = domainQueryService.getByURI(domainURI);
            if (null == domain) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);

                return;
            }

            final RSSChannel channel = new RSSChannel();
            final String domainId = domain.getOid();
            final JSONObject result = articleQueryService.getDomainArticles(UserExtUtil.USER_AVATAR_VIEW_MODE_C_STATIC, domainId, 1, Symphonys.getInt("indexArticlesCnt"));
            final List<JSONObject> articles = (List<JSONObject>) result.get(ArticleUtil.ARTICLES);
            for (int i = 0; i < articles.size(); i++) {
                RSSItem item = getItem(articles, i);
                channel.addItem(item);
            }
            channel.setTitle(langPropsService.get("symphonyLabel"));
            channel.setLastBuildDate(new Date());
            channel.setLink( SpringUtil.getServerPath());
            channel.setAtomLink( SpringUtil.getServerPath() + "/rss/" + domainURI + ".xml");
            channel.setGenerator("Symphony v" + SpringUtil.VERSION + ", https://sym.b3log.org");
            final String localeString = optionQueryService.getOption("miscLanguage").getOptionValue();
            final String country = Locales.getCountry(localeString).toLowerCase();
            final String language = Locales.getLanguage(localeString).toLowerCase();
            channel.setLanguage(language + '-' + country);
            channel.setDescription(langPropsService.get("symDescriptionLabel"));

//            renderer.setContent(channel.toString());
            writer.write(channel.toString());
            writer.close();
        } catch (final Exception e) {
            LOGGER.error( "Generates recent articles' RSS failed", e);

            try {
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private RSSItem getItem(final List<JSONObject> articles, int i) throws org.json.JSONException {
        final JSONObject article = articles.get(i);
        final RSSItem ret = new RSSItem();
        String title = article.getString(ArticleUtil.ARTICLE_TITLE);
        title = Emotions.toAliases(title);
        ret.setTitle(title);
        String description = article.getString(ArticleUtil.ARTICLE_T_PREVIEW_CONTENT);
        description = Emotions.toAliases(description);
        ret.setDescription(description);
        final Date pubDate = (Date) article.get(ArticleUtil.ARTICLE_UPDATE_TIME);
        ret.setPubDate(pubDate);
        final String link =  SpringUtil.getServerPath() + article.getString(ArticleUtil.ARTICLE_PERMALINK);
        ret.setLink(link);
        ret.setGUID(link);
        ret.setAuthor(article.optString(ArticleUtil.ARTICLE_T_AUTHOR_NAME));
        final String tagsString = article.getString(ArticleUtil.ARTICLE_TAGS);
        final String[] tagStrings = tagsString.split(",");
        for (final String tagString : tagStrings) {
            final RSSCategory catetory = new RSSCategory();
            ret.addCatetory(catetory);
            catetory.setTerm(tagString);
        }

        return ret;
    }
}
