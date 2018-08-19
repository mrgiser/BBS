package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.model.my.*;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.commons.lang.time.DateFormatUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Sitemap query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Mar 31, 2018
 * @since 1.6.0
 */
@Service
public class SitemapQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SitemapQueryService.class);

    /**
     * Article Mapper.
     */
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * Domain cache.
     */
    @Autowired
    private DomainCache domainCache;

    /**
     * Generates index for the specified sitemap.
     *
     * @param sitemap the specified sitemap
     */
    public void genIndex(final Sitemap sitemap) {
        final Sitemap.URL url = new Sitemap.URL();
        url.setLoc( SpringUtil.getServerPath());
        url.setChangeFreq("always");
        url.setPriority("1.0");

        sitemap.addURL(url);
    }

    /**
     * Generates domains for the specified sitemap.
     *
     * @param sitemap the specified sitemap
     */
    public void genDomains(final Sitemap sitemap) {
        final List<JSONObject> domains = domainCache.getDomains(Integer.MAX_VALUE);

        for (final JSONObject domain : domains) {
            final String permalink =  SpringUtil.getServerPath() + "/domain/" + domain.optString(Domain.DOMAIN_URI);

            final Sitemap.URL url = new Sitemap.URL();
            url.setLoc(permalink);
            url.setChangeFreq("always");
            url.setPriority("0.9");

            sitemap.addURL(url);
        }
    }

    /**
     * Generates articles for the specified sitemap.
     *
     * @param sitemap the specified sitemap
     */
    public void genArticles(final Sitemap sitemap) {
        final Query query = new Query().setCurrentPageNum(1).setPageCount(Integer.MAX_VALUE).
                addProjection(Keys.OBJECT_ID, String.class).
                addProjection(Article.ARTICLE_UPDATE_TIME, Long.class).
                setFilter(new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, Article.ARTICLE_STATUS_C_INVALID)).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        try {
            final JSONArray articles = articleMapper.get(query).getJSONArray(Keys.RESULTS);

            for (int i = 0; i < articles.length(); i++) {
                final JSONObject article = articles.getJSONObject(i);
                final long id = article.getLong(Keys.OBJECT_ID);
                final String permalink =  SpringUtil.getServerPath() + "/article/" + id;

                final Sitemap.URL url = new Sitemap.URL();
                url.setLoc(permalink);
                final Date updateDate = new Date(id);
                final String lastMod = DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(updateDate);
                url.setLastMod(lastMod);

                sitemap.addURL(url);
            }
        } catch (final Exception e) {
            LOGGER.error( "Gets sitemap articles failed", e);
        }
    }
}
