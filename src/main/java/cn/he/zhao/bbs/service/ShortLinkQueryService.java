package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entityUtil.ArticleUtil;
import cn.he.zhao.bbs.entityUtil.TagUtil;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.spring.Stopwatchs;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
    private static final Pattern TAG_PATTERN = Pattern.compile(" \\[" + TagUtil.TAG_TITLE_PATTERN_STR + "\\](?!\\(.+\\)) ");

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
        Stopwatchs.start("LinkUtil article");

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

//                    final Query query = new Query().addProjection(ArticleUtil.ARTICLE_TITLE, String.class)
//                            .setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL, linkId));
                    final Article linkArticle = articleMapper.getByPrimaryKey(linkId);
                    if (null == linkArticle) {
                        continue;
                    }

//                    final JSONObject linkArticle = results.optJSONObject(0);
                    final String linkTitle = linkArticle.getArticleTitle();
                    String link = " [" + linkTitle + "](" +  SpringUtil.getServerPath() + "/article/" + linkId;
                    if (StringUtils.isNotBlank(queryStr)) {
                        link += "?" + queryStr;
                    }
                    link += ") ";

                    matcher.appendReplacement(contentBuilder, link);
                }

                matcher.appendTail(contentBuilder);
            } catch (final Exception e) {
                LOGGER.error( "Generates article link error", e);
            }

            matcher = ARTICLE_PATTERN_SIMPLE.matcher(contentBuilder.toString());
            contentBuilder = new StringBuffer();

            try {
                while (matcher.find()) {
                    final String linkId = StringUtils.substringBetween(matcher.group(), "[", "]");

//                    final Query query = new Query().addProjection(ArticleUtil.ARTICLE_TITLE, String.class)
//                            .setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL, linkId));
                    final Article linkArticle = articleMapper.getByPrimaryKey(linkId);
                    if (null == linkArticle) {
                        continue;
                    }

//                    final JSONObject linkArticle = results.optJSONObject(0);

                    final String linkTitle = linkArticle.getArticleTitle();
                    final String link = " [" + linkTitle + "](" +  SpringUtil.getServerPath() + "/article/" + linkId + ") ";

                    matcher.appendReplacement(contentBuilder, link);
                }

                matcher.appendTail(contentBuilder);
            } catch (final Exception e) {
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
        Stopwatchs.start("LinkUtil tag");

        try {
            final Matcher matcher = TAG_PATTERN.matcher(content);
            final StringBuffer contentBuilder = new StringBuffer();

            try {
                while (matcher.find()) {
                    final String linkTagTitle = StringUtils.substringBetween(matcher.group(), "[", "]");

                    if (StringUtils.equals(linkTagTitle, "x")) { // [x] => <input checked>
                        continue;
                    }

//                    final Query query = new Query().addProjection(TagUtil.TAG_TITLE, String.class)
//                            .addProjection(TagUtil.TAG_URI, String.class)
//                            .setFilter(new PropertyFilter(TagUtil.TAG_TITLE, FilterOperator.EQUAL, linkTagTitle));
                    final List<Tag> results = tagMapper.getByTagTitle(linkTagTitle);
                    if (0 == results.size()) {
                        continue;
                    }

                    final Tag linkTag = results.get(0);

                    final String linkTitle = linkTag.getTagTitle();
                    final String linkURI = linkTag.getTagURI();
                    final String link = " [" + linkTitle + "](" +  SpringUtil.getServerPath() + "/tag/" + linkURI + ") ";

                    matcher.appendReplacement(contentBuilder, link);
                }
                matcher.appendTail(contentBuilder);
            } catch (final Exception e) {
                LOGGER.error( "Generates tag link error", e);
            }

            return contentBuilder.toString();
        } finally {
            Stopwatchs.end();
        }
    }
}
