package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Short link query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.2.2.0, Jun 9, 2018
 * @since 1.3.0
 */
@Service
public class ShortLinkQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ShortLinkQueryService.class);

    /**
     * Article pattern - simple.
     */
    private static final Pattern ARTICLE_PATTERN_SIMPLE = Pattern.compile(" \\[\\d{13,15}\\] ");

    /**
     * Article pattern - full.
     */
    private static final Pattern ARTICLE_PATTERN_FULL
            = Pattern.compile("(?:^|[^\"'\\](])(" +  SpringUtil.getServerPath() + "/article/\\d{13,15}[?\\w&=#%:]*(\\b|$))");

    /**
     * Tag title pattern.
     */
    private static final Pattern TAG_PATTERN = Pattern.compile(" \\[" + Tag.TAG_TITLE_PATTERN_STR + "\\](?!\\(.+\\)) ");

    /**
     * Article Mapper.
     */
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * Tag Mapper.
     */
    @Autowired
    private TagMapper tagMapper;

    /**
     * Processes article short link (article id).
     *
     * @param content the specified content
     * @return processed content
     */
    public String linkArticle(final String content) {
        Stopwatchs.start("Link article");

        StringBuffer contentBuilder = new StringBuffer();
        try {
            Matcher matcher = ARTICLE_PATTERN_FULL.matcher(content);
            final String[] codeBlocks = StringUtils.substringsBetween(content, "```", "```");
            String codes = "";
            if (null != codeBlocks) {
                codes = String.join("", codeBlocks);
            }
            try {
                while (matcher.find()) {
                    final String url = StringUtils.trim(matcher.group());
                    if (StringUtils.containsIgnoreCase(codes, url)) {
                        continue;
                    }
                    String linkId;
                    String queryStr = null;
                    if (StringUtils.contains(url, "?")) {
                        linkId = StringUtils.substringBetween(matcher.group(), "/article/", "?");
                        queryStr = StringUtils.substringAfter(url, "?");
                    } else {
                        linkId = StringUtils.substringAfter(matcher.group(), "/article/");
                    }

                    final Query query = new Query().addProjection(Article.ARTICLE_TITLE, String.class)
                            .setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL, linkId));
                    final JSONArray results = articleMapper.get(query).optJSONArray(Keys.RESULTS);
                    if (0 == results.length()) {
                        continue;
                    }

                    final JSONObject linkArticle = results.optJSONObject(0);
                    final String linkTitle = linkArticle.optString(Article.ARTICLE_TITLE);
                    String link = " [" + linkTitle + "](" +  SpringUtil.getServerPath() + "/article/" + linkId;
                    if (StringUtils.isNotBlank(queryStr)) {
                        link += "?" + queryStr;
                    }
                    link += ") ";

                    matcher.appendReplacement(contentBuilder, link);
                }

                matcher.appendTail(contentBuilder);
            } catch (final MapperException e) {
                LOGGER.error( "Generates article link error", e);
            }

            matcher = ARTICLE_PATTERN_SIMPLE.matcher(contentBuilder.toString());
            contentBuilder = new StringBuffer();

            try {
                while (matcher.find()) {
                    final String linkId = StringUtils.substringBetween(matcher.group(), "[", "]");

                    final Query query = new Query().addProjection(Article.ARTICLE_TITLE, String.class)
                            .setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL, linkId));
                    final JSONArray results = articleMapper.get(query).optJSONArray(Keys.RESULTS);
                    if (0 == results.length()) {
                        continue;
                    }

                    final JSONObject linkArticle = results.optJSONObject(0);

                    final String linkTitle = linkArticle.optString(Article.ARTICLE_TITLE);
                    final String link = " [" + linkTitle + "](" +  SpringUtil.getServerPath() + "/article/" + linkId + ") ";

                    matcher.appendReplacement(contentBuilder, link);
                }

                matcher.appendTail(contentBuilder);
            } catch (final MapperException e) {
                LOGGER.error( "Generates article link error", e);
            }

            return contentBuilder.toString();
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Processes tag short link (tag id).
     *
     * @param content the specified content
     * @return processed content
     */
    public String linkTag(final String content) {
        Stopwatchs.start("Link tag");

        try {
            final Matcher matcher = TAG_PATTERN.matcher(content);
            final StringBuffer contentBuilder = new StringBuffer();

            try {
                while (matcher.find()) {
                    final String linkTagTitle = StringUtils.substringBetween(matcher.group(), "[", "]");

                    if (StringUtils.equals(linkTagTitle, "x")) { // [x] => <input checked>
                        continue;
                    }

                    final Query query = new Query().addProjection(Tag.TAG_TITLE, String.class)
                            .addProjection(Tag.TAG_URI, String.class)
                            .setFilter(new PropertyFilter(Tag.TAG_TITLE, FilterOperator.EQUAL, linkTagTitle));
                    final JSONArray results = tagMapper.get(query).optJSONArray(Keys.RESULTS);
                    if (0 == results.length()) {
                        continue;
                    }

                    final JSONObject linkTag = results.optJSONObject(0);

                    final String linkTitle = linkTag.optString(Tag.TAG_TITLE);
                    final String linkURI = linkTag.optString(Tag.TAG_URI);
                    final String link = " [" + linkTitle + "](" +  SpringUtil.getServerPath() + "/tag/" + linkURI + ") ";

                    matcher.appendReplacement(contentBuilder, link);
                }
                matcher.appendTail(contentBuilder);
            } catch (final MapperException e) {
                LOGGER.error( "Generates tag link error", e);
            }

            return contentBuilder.toString();
        } finally {
            Stopwatchs.end();
        }
    }
}
