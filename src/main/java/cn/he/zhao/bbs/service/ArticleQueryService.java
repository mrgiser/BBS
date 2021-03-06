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
package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.cache.ArticleCache;
import cn.he.zhao.bbs.channel.ArticleChannel;
import cn.he.zhao.bbs.entityUtil.*;
import cn.he.zhao.bbs.entityUtil.my.CollectionUtils;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.Pagination;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.spring.*;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.util.*;
import cn.he.zhao.bbs.validate.UserRegisterValidation;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.sqlsource.PageSqlSource;
import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Article query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 2.27.36.9, Jun 19, 2018
 * @since 0.2.0
 */
@Service
public class ArticleQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleQueryService.class);

    /**
     * Count to fetch article tags for relevant articles.
     */
    private static final int RELEVANT_ARTICLE_RANDOM_FETCH_TAG_CNT = 3;

    /**
     * Article Mapper.
     */
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * Comment Mapper.
     */
    @Autowired
    private CommentMapper commentMapper;

    /**
     * Tag-Article Mapper.
     */
    @Autowired
    private TagArticleMapper tagArticleMapper;

    /**
     * Tag Mapper.
     */
    @Autowired
    private TagMapper tagMapper;

    /**
     * User Mapper.
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * Domain tag Mapper.
     */
    @Autowired
    private DomainTagMapper domainTagMapper;

    /**
     * Comment query service.
     */
    @Autowired
    private CommentQueryService commentQueryService;

    /**
     * User query service.
     */
    @Autowired
    private UserQueryService userQueryService;

    /**
     * Avatar query service.
     */
    @Autowired
    private AvatarQueryService avatarQueryService;

    /**
     * Short link query service.
     */
    @Autowired
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * FollowUtil query service.
     */
    @Autowired
    private FollowQueryService followQueryService;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Article cache.
     */
    @Autowired
    private ArticleCache articleCache;

    /**
     * Gets following user articles.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param userId         the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return following tag articles, returns an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getFollowingUserArticles(final int avatarViewMode, final String userId,
                                                     final int currentPageNum, final int pageSize) throws Exception {
        final List<JSONObject> users = (List<JSONObject>) followQueryService.getFollowingUsers(
                avatarViewMode, userId, 1, Integer.MAX_VALUE).opt(Keys.RESULTS);
        if (users.isEmpty()) {
            return Collections.emptyList();
        }

//        final Query query = new Query()
//                .addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
//                .setPageSize(pageSize).setCurrentPageNum(currentPageNum);

        PageHelper.startPage(currentPageNum,pageSize,"oId DESC");
        final List<String> followingUserIds = new ArrayList<>();
        for (final JSONObject user : users) {
            followingUserIds.add(user.optString(Keys.OBJECT_ID));
        }

//        final List<Filter> filters = new ArrayList<>();
//        filters.add(new PropertyFilter(ArticleUtil.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, ArticleUtil.ARTICLE_STATUS_C_INVALID));
//        filters.add(new PropertyFilter(ArticleUtil.ARTICLE_TYPE, FilterOperator.NOT_EQUAL, ArticleUtil.ARTICLE_TYPE_C_DISCUSSION));
//        filters.add(new PropertyFilter(ArticleUtil.ARTICLE_AUTHOR_ID, FilterOperator.IN, followingUserIds));
//        query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

//        query.addProjection(Keys.OBJECT_ID, String.class).
//                addProjection(ArticleUtil.ARTICLE_STICK, Long.class).
//                addProjection(ArticleUtil.ARTICLE_CREATE_TIME, Long.class).
//                addProjection(ArticleUtil.ARTICLE_UPDATE_TIME, Long.class).
//                addProjection(ArticleUtil.ARTICLE_LATEST_CMT_TIME, Long.class).
//                addProjection(ArticleUtil.ARTICLE_AUTHOR_ID, String.class).
//                addProjection(ArticleUtil.ARTICLE_TITLE, String.class).
//                addProjection(ArticleUtil.ARTICLE_STATUS, Integer.class).
//                addProjection(ArticleUtil.ARTICLE_VIEW_CNT, Integer.class).
//                addProjection(ArticleUtil.ARTICLE_TYPE, Integer.class).
//                addProjection(ArticleUtil.ARTICLE_PERMALINK, String.class).
//                addProjection(ArticleUtil.ARTICLE_TAGS, String.class).
//                addProjection(ArticleUtil.ARTICLE_LATEST_CMTER_NAME, String.class).
//                addProjection(ArticleUtil.ARTICLE_SYNC_TO_CLIENT, Boolean.class).
//                addProjection(ArticleUtil.ARTICLE_COMMENT_CNT, Integer.class).
//                addProjection(ArticleUtil.ARTICLE_ANONYMOUS, Integer.class).
//                addProjection(ArticleUtil.ARTICLE_PERFECT, Integer.class).
//                addProjection(ArticleUtil.ARTICLE_CONTENT, String.class).
//                addProjection(ArticleUtil.ARTICLE_QNA_OFFER_POINT, Integer.class);

        List<Article> result = null;
        try {
            Stopwatchs.start("Query following user articles");

            result = articleMapper.getByStatusTypeID( ArticleUtil.ARTICLE_STATUS_C_INVALID,
                    ArticleUtil.ARTICLE_TYPE_C_DISCUSSION,followingUserIds);
        } catch (final Exception e) {
            LOGGER.error( "Gets following user articles failed", e);

            throw new Exception(e);
        } finally {
            Stopwatchs.end();
        }

//        final JSONArray data = result.optJSONArray(Keys.RESULTS);
//        final List<JSONObject> ret = CollectionUtils.jsonArrayToList(data);

        List<JSONObject> jsonObjects = JsonUtil.listToJSONList(result);
        try {
            organizeArticles(avatarViewMode, jsonObjects);
        } catch (final Exception e) {
            LOGGER.error( "Organizes articles failed", e);

            throw new Exception(e);
        }

        return jsonObjects;
    }

    /**
     * Gets following tag articles.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param userId         the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return following tag articles, returns an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getFollowingTagArticles(final int avatarViewMode, final String userId,
                                                    final int currentPageNum, final int pageSize) throws Exception {
        final List<JSONObject> tags = (List<JSONObject>) followQueryService.getFollowingTags(
                userId, 1, Integer.MAX_VALUE).opt(Keys.RESULTS);
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }

//        final Map<String, Class<?>> articleFields = new HashMap<>();
//        articleFields.put(Keys.OBJECT_ID, String.class);
//        articleFields.put(Article.ARTICLE_STICK, Long.class);
//        articleFields.put(Article.ARTICLE_CREATE_TIME, Long.class);
//        articleFields.put(Article.ARTICLE_UPDATE_TIME, Long.class);
//        articleFields.put(Article.ARTICLE_LATEST_CMT_TIME, Long.class);
//        articleFields.put(Article.ARTICLE_AUTHOR_ID, String.class);
//        articleFields.put(Article.ARTICLE_TITLE, String.class);
//        articleFields.put(Article.ARTICLE_STATUS, Integer.class);
//        articleFields.put(Article.ARTICLE_VIEW_CNT, Integer.class);
//        articleFields.put(Article.ARTICLE_TYPE, Integer.class);
//        articleFields.put(Article.ARTICLE_PERMALINK, String.class);
//        articleFields.put(Article.ARTICLE_TAGS, String.class);
//        articleFields.put(Article.ARTICLE_LATEST_CMTER_NAME, String.class);
//        articleFields.put(Article.ARTICLE_SYNC_TO_CLIENT, Boolean.class);
//        articleFields.put(Article.ARTICLE_COMMENT_CNT, Integer.class);
//        articleFields.put(Article.ARTICLE_ANONYMOUS, Integer.class);
//        articleFields.put(Article.ARTICLE_PERFECT, Integer.class);
//        articleFields.put(Article.ARTICLE_CONTENT, String.class);
//        articleFields.put(Article.ARTICLE_QNA_OFFER_POINT, Integer.class);

        return getArticlesByTags2(avatarViewMode, currentPageNum, pageSize,  tags);
    }

    /**
     * Gets the next article.
     *
     * @param articleId the specified article id
     * @return permalink and title, <pre>
     * {
     *     "articlePermalink": "",
     *     "articleTitle": "",
     *     "articleTitleEmoj": "",
     *     "articleTitleEmojUnicode": ""
     * }
     * </pre>, returns {@code null} if not found
     */
    public JSONObject getNextPermalink(final String articleId) {
        Stopwatchs.start("Get next");

        try {
//            final Query query = new Query().setFilter(
//                    new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN, articleId)).
//                    addSort(Keys.OBJECT_ID, SortDirection.ASCENDING).
//                    addProjection(Article.ARTICLE_PERMALINK, String.class).
//                    addProjection(Article.ARTICLE_TITLE, String.class).
//                    setCurrentPageNum(1).setPageCount(1).setPageSize(1);

            PageHelper.startPage(1,1,"oId ASCE");

            final List<Article> result = articleMapper.getGreaterThanID(articleId);
            if (0 == result.size()) {
                return null;
            }

            final Article ret = result.get(0);
            if (null == ret) {
                return null;
            }

            JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(ret));

            final String title = Encode.forHtml(ret.getArticleTitle());
            jsonObject.put(ArticleUtil.ARTICLE_T_TITLE_EMOJI, Emotions.convert(title));
            jsonObject.put(ArticleUtil.ARTICLE_T_TITLE_EMOJI_UNICODE, EmojiParser.parseToUnicode(title));

            return jsonObject;
        } catch (final Exception e) {
            LOGGER.error( "Gets next article permalink failed", e);

            return null;
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets the previous article.
     *
     * @param articleId the specified article id
     * @return permalink and title, <pre>
     * {
     *     "articlePermalink": "",
     *     "articleTitle": "",
     *     "articleTitleEmoj": "",
     *     "articleTitleEmojUnicode": ""
     * }
     * </pre>, returns {@code null} if not found
     */
    public JSONObject getPreviousPermalink(final String articleId) {
        Stopwatchs.start("Get previous");

        try {
//            final Query query = new Query().setFilter(
//                    new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN, articleId)).
//                    addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
//                    addProjection(Article.ARTICLE_PERMALINK, String.class).
//                    addProjection(Article.ARTICLE_TITLE, String.class).
//                    setCurrentPageNum(1).setPageCount(1).setPageSize(1);

            PageHelper.startPage(1,1,"oId DESC");

            final List<Article> result = articleMapper.getLessThanID(articleId);
            if (0 == result.size()) {
                return null;
            }

            final Article ret = result.get(0);
            if (null == ret) {
                return null;
            }

            JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(ret));

            final String title = Encode.forHtml(ret.getArticleTitle());
            jsonObject.put(ArticleUtil.ARTICLE_T_TITLE_EMOJI, Emotions.convert(title));
            jsonObject.put(ArticleUtil.ARTICLE_T_TITLE_EMOJI_UNICODE, EmojiParser.parseToUnicode(title));

            return jsonObject;
        } catch (final Exception e) {
            LOGGER.error( "Gets previous article permalink failed", e);

            return null;
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Get an articles by the specified title.
     *
     * @param title the specified title
     * @return article, returns {@code null} if not found
     */
    public Article getArticleByTitle(final String title) {
        try {
            return articleMapper.getByTitle(title);
        } catch (final Exception e) {
            LOGGER.error( "Gets article by title [" + title + "] failed", e);

            return null;
        }
    }

    /**
     * Gets article count of the specified day.
     *
     * @param day the specified day
     * @return article count
     */
    public int getArticleCntInDay(final Date day) {
        final long time = day.getTime();
        final long start = Times.getDayStartTime(time);
        final long end = Times.getDayEndTime(time);

//        final Query query = new Query().setFilter(CompositeFilterOperator.and(
//                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, start),
//                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN, end),
//                new PropertyFilter(ArticleUtil.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, ArticleUtil.ARTICLE_STATUS_C_INVALID)
//        ));

        try {
            return (int) articleMapper.countByTimeAndStatus(start,end,ArticleUtil.ARTICLE_STATUS_C_INVALID);
        } catch (final Exception e) {
            LOGGER.error( "Count day article failed", e);

            return 1;
        }
    }

    /**
     * Gets article count of the specified month.
     *
     * @param day the specified month
     * @return article count
     */
    public int getArticleCntInMonth(final Date day) {
        final long time = day.getTime();
        final long start = Times.getMonthStartTime(time);
        final long end = Times.getMonthEndTime(time);

//        final Query query = new Query().setFilter(CompositeFilterOperator.and(
//                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, start),
//                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN, end),
//                new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, Article.ARTICLE_STATUS_C_INVALID)
//        ));

        try {
            return (int) articleMapper.countByTimeAndStatus(start,end,ArticleUtil.ARTICLE_STATUS_C_INVALID);
        } catch (final Exception e) {
            LOGGER.error( "Count month article failed", e);

            return 1;
        }
    }

    /**
     * Gets articles by the specified page number and page size.
     *
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @param type1          the specified types
     * @return articles, return an empty list if not found
     * @throws Exception service exception
     */
    public List<Article> getValidArticles(final int currentPageNum, final int pageSize, final int type1, final int type2) throws Exception {
        try {
//            final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
//                    .setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);

            PageHelper.startPage(currentPageNum,pageSize,"oId DESC");
//            if (null != types && types.length > 0) {
//                final List<Filter> typeFilters = new ArrayList<>();
//                for (int i = 0; i < types.length; i++) {
//                    final int type = types[i];
//
//                    typeFilters.add(new PropertyFilter(ArticleUtil.ARTICLE_TYPE, FilterOperator.EQUAL, type));
//                }
//
//                final CompositeFilter typeFilter = new CompositeFilter(CompositeFilterOperator.OR, typeFilters);
//                final List<Filter> filters = new ArrayList<>();
//                filters.add(typeFilter);
//                filters.add(new PropertyFilter(ArticleUtil.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, Article.ARTICLE_STATUS_C_INVALID));
//
//                query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));
//            } else {
//                query.setFilter(new PropertyFilter(ArticleUtil.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, ArticleUtil.ARTICLE_STATUS_C_INVALID));
//            }

            final List<Article> result = articleMapper.getByArticleTypeAndStatus(type1,type2,ArticleUtil.ARTICLE_STATUS_C_INVALID);

            return result;
        } catch (final Exception e) {
            LOGGER.error( "Gets articles failed", e);

            throw new Exception(e);
        }
    }

    /**
     * Gets domain articles.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param domainId       the specified domain id
     * @param currentPageNum the specified current page number
     * @param pageSize       the specified page size
     * @return result
     * @throws Exception service exception
     */
    public JSONObject getDomainArticles(final int avatarViewMode, final String domainId,
                                        final int currentPageNum, final int pageSize) throws Exception {
        final JSONObject ret = new JSONObject();
        ret.put(ArticleUtil.ARTICLES, (Object) Collections.emptyList());

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, 0);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, (Object) Collections.emptyList());

        try {
            final JSONArray domainTags = domainTagMapper.getByDomainId(domainId, 1, Integer.MAX_VALUE)
                    .optJSONArray(Keys.RESULTS);

            if (domainTags.length() <= 0) {
                return ret;
            }

            final List<String> tagIds = new ArrayList<>();
            for (int i = 0; i < domainTags.length(); i++) {
                tagIds.add(domainTags.optJSONObject(i).optString(TagUtil.TAG + "_" + Keys.OBJECT_ID));
            }

//            Query query = new Query().setFilter(
//                    new PropertyFilter(TagUtil.TAG + "_" + Keys.OBJECT_ID, FilterOperator.IN, tagIds)).
//                    setCurrentPageNum(currentPageNum).setPageSize(pageSize).
//                    addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
            PageHelper.startPage(currentPageNum,pageSize,"oId DESC");
            PageInfo<TagArticle> result = new PageInfo<>(tagArticleMapper.getByTagIds(tagIds));
            final List<TagArticle> tagArticles = result.getList();
            if (tagArticles.size() <= 0) {
                return ret;
            }

            final int pageCount = result.getPages();

            final int windowSize = Symphonys.getInt("latestArticlesWindowSize");

            final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
            pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
            pagination.put(Pagination.PAGINATION_PAGE_NUMS, (Object) pageNums);

            final Set<String> articleIds = new HashSet<>();
            for (int i = 0; i < tagArticles.size(); i++) {
                articleIds.add(tagArticles.get(i).getArticle_oid());
            }

//            query = new Query().setFilter(CompositeFilterOperator.and(
//                    new PropertyFilter(Keys.OBJECT_ID, FilterOperator.IN, articleIds),
//                    new PropertyFilter(ArticleUtil.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, ArticleUtil.ARTICLE_STATUS_C_INVALID))).
//                    setPageCount(1).addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

            PageHelper.startPage(1,Integer.MAX_VALUE,"oId DESC");
            final List<Article> articles = articleMapper.getByArticleIds(articleIds,ArticleUtil.ARTICLE_STATUS_C_INVALID);

            List<JSONObject> jsonObjectList = JsonUtil.listToJSONList(articles);
            try {

                organizeArticles(avatarViewMode, jsonObjectList);
            } catch (final Exception e) {
                LOGGER.error( "Organizes articles failed", e);

                throw new Exception(e);
            }

            final Integer participantsCnt = Symphonys.getInt("latestArticleParticipantsCnt");
//            List<JSONObject> jsonObjects = null;
//            for (int i = 0; i < articles.size(); i++) {
//                JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(articles.get(i)));
//                jsonObjects.add(jsonObject);
//            }
            genParticipants(avatarViewMode, jsonObjectList, participantsCnt);

            ret.put(ArticleUtil.ARTICLES, (Object) jsonObjectList);

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets domain articles error", e);

            throw new Exception(e);
        }
    }

    /**
     * Gets the relevant articles of the specified article with the specified fetch size.
     * <p>
     * The relevant articles exist the same tag with the specified article.
     * </p>
     *
     * @param avatarViewMode the specified avatar view mode
     * @param article        the specified article
     * @param fetchSize      the specified fetch size
     * @return relevant articles, returns an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getRelevantArticles(final int avatarViewMode, final JSONObject article, final int fetchSize)
            throws Exception {
        final String tagsString = article.optString(ArticleUtil.ARTICLE_TAGS);
        String[] tagTitles = tagsString.split(",");
        final List<String> excludedB3logTitles = new ArrayList<>();
        for (int i = 0; i < tagTitles.length; i++) {
            if (!"B3log".equalsIgnoreCase(tagTitles[i])) {
                excludedB3logTitles.add(tagTitles[i]);
            }
        }
        if (excludedB3logTitles.size() < 1) {
            excludedB3logTitles.add("B3log");
        }
        tagTitles = excludedB3logTitles.toArray(new String[0]);
        final int tagTitlesLength = tagTitles.length;
        final int subCnt = tagTitlesLength > RELEVANT_ARTICLE_RANDOM_FETCH_TAG_CNT
                ? RELEVANT_ARTICLE_RANDOM_FETCH_TAG_CNT : tagTitlesLength;

        final List<Integer> tagIdx = CollectionUtils.getRandomIntegers(0, tagTitlesLength, subCnt);
        final int subFetchSize = fetchSize / subCnt;
        final Set<String> fetchedArticleIds = new HashSet<>();

        final List<Article> ret = new ArrayList<>();
        try {
            for (int i = 0; i < tagIdx.size(); i++) {
                final String tagTitle = tagTitles[tagIdx.get(i)].trim();

                final Tag tag = tagMapper.getByTitle(tagTitle);
                final String tagId = tag.getOid();
                List<TagArticle> tagArticleRelations = tagArticleMapper.getByTagId(tagId, 1, subFetchSize);

//                final JSONArray tagArticleRelations = result.optJSONArray(Keys.RESULTS);

                final Set<String> articleIds = new HashSet<>();
                for (int j = 0; j < tagArticleRelations.size(); j++) {
                    final String articleId = tagArticleRelations.get(j).getArticle_oid();

                    if (fetchedArticleIds.contains(articleId)) {
                        continue;
                    }

                    articleIds.add(articleId);
                    fetchedArticleIds.add(articleId);
                }

                articleIds.remove(article.optString(Keys.OBJECT_ID));

//                final Query query = new Query().setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.IN, articleIds)).
//                        addProjection(Article.ARTICLE_TITLE, String.class).
//                        addProjection(Article.ARTICLE_PERMALINK, String.class).
//                        addProjection(Article.ARTICLE_AUTHOR_ID, String.class);
                List<Article> result = articleMapper.getByArticleIds2(articleIds);

                ret.addAll(result);
            }

            List<JSONObject> jsonObjects = JsonUtil.listToJSONList(ret);
            organizeArticles(avatarViewMode, jsonObjects);

            return jsonObjects;
        } catch (final Exception e) {
            LOGGER.error( "Gets relevant articles failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets interest articles.
     *
     * @param currentPageNum the specified current page number
     * @param pageSize       the specified fetch size
     * @param tagTitles      the specified tag titles
     * @return articles, return an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getInterests(final int currentPageNum, final int pageSize, final String... tagTitles)
            throws Exception {
        try {
            final List<Tag> tagList = new ArrayList<>();
            for (final String tagTitle : tagTitles) {
                final Tag tag = tagMapper.getByTitle(tagTitle);
                if (null == tag) {
                    continue;
                }

                tagList.add(tag);
            }

//            final Map<String, Class<?>> articleFields = new HashMap<>();
//            articleFields.put(Article.ARTICLE_TITLE, String.class);
//            articleFields.put(Article.ARTICLE_PERMALINK, String.class);
//            articleFields.put(Article.ARTICLE_CREATE_TIME, Long.class);
//            articleFields.put(Article.ARTICLE_AUTHOR_ID, String.class);

            final List<JSONObject> ret = new ArrayList<>();

            if (!tagList.isEmpty()) {
                final List<JSONObject> tagArticles
                        = getArticlesByTags(UserExtUtil.USER_AVATAR_VIEW_MODE_C_STATIC, currentPageNum, pageSize, tagList);

                ret.addAll(tagArticles);
            }

            if (ret.size() < pageSize) {
//                final List<Filter> filters = new ArrayList<>();
//                filters.add(new PropertyFilter(ArticleUtil.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, ArticleUtil.ARTICLE_STATUS_C_INVALID));
//                filters.add(new PropertyFilter(ArticleUtil.ARTICLE_TYPE, FilterOperator.NOT_EQUAL, ArticleUtil.ARTICLE_TYPE_C_DISCUSSION));

                PageHelper.startPage(currentPageNum,pageSize,"oId DESC");
//                final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
//                        .setPageCount(currentPageNum).setPageSize(pageSize).setCurrentPageNum(1);
//                query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));
//                for (final Map.Entry<String, Class<?>> articleField : articleFields.entrySet()) {
//                    query.addProjection(articleField.getKey(), articleField.getValue());
//                }

                final List<Article> result = articleMapper.getByStatusType(ArticleUtil.ARTICLE_STATUS_C_INVALID, ArticleUtil.ARTICLE_TYPE_C_DISCUSSION);

                final List<JSONObject> recentArticles = JsonUtil.listToJSONList(result);
                ret.addAll(recentArticles);
            }

            final Iterator<JSONObject> iterator = ret.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                final JSONObject article = iterator.next();
                article.put(ArticleUtil.ARTICLE_PERMALINK,  SpringUtil.getServerPath() + article.optString(ArticleUtil.ARTICLE_PERMALINK));

                article.remove(ArticleUtil.ARTICLE_T_AUTHOR);
                article.remove(ArticleUtil.ARTICLE_AUTHOR_ID);
                article.remove(ArticleUtil.ARTICLE_T_PARTICIPANTS);
                article.remove(ArticleUtil.ARTICLE_T_PARTICIPANT_NAME);
                article.remove(ArticleUtil.ARTICLE_T_PARTICIPANT_THUMBNAIL_URL);
                article.remove(ArticleUtil.ARTICLE_LATEST_CMT_TIME);
                article.remove(ArticleUtil.ARTICLE_LATEST_CMTER_NAME);
                article.remove(ArticleUtil.ARTICLE_UPDATE_TIME);
                article.remove(ArticleUtil.ARTICLE_T_HEAT);
                article.remove(ArticleUtil.ARTICLE_T_TITLE_EMOJI);
                article.remove(ArticleUtil.ARTICLE_T_TITLE_EMOJI_UNICODE);
                article.remove(Common.TIME_AGO);
                article.remove(Common.CMT_TIME_AGO);
                article.remove(ArticleUtil.ARTICLE_T_TAG_OBJS);
                article.remove(ArticleUtil.ARTICLE_STICK);
                article.remove(ArticleUtil.ARTICLE_T_PREVIEW_CONTENT);
                article.remove(ArticleUtil.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "20");
                article.remove(ArticleUtil.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "48");
                article.remove(ArticleUtil.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "210");
                article.remove(ArticleUtil.ARTICLE_T_STICK_REMAINS);

                long createTime = 0;
                final Object time = article.get(ArticleUtil.ARTICLE_CREATE_TIME);
                if (time instanceof Date) {
                    createTime = ((Date) time).getTime();
                } else {
                    createTime = (Long) time;
                }
                article.put(ArticleUtil.ARTICLE_CREATE_TIME, createTime);

                i++;
                if (i > pageSize) {
                    iterator.remove();
                }
            }

            return ret;
        } catch (final Exception  e) {
            LOGGER.error( "Gets interests failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets news (perfect articles).
     *
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return articles, return an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getNews(final int currentPageNum, final int pageSize) throws Exception {
        try {
//            final Query query = new Query().
//                    setFilter(new PropertyFilter(ArticleUtil.ARTICLE_PERFECT, FilterOperator.EQUAL, ArticleUtil.ARTICLE_PERFECT_C_PERFECT)).
//                    addProjection(ArticleUtil.ARTICLE_TITLE, String.class).
//                    addProjection(ArticleUtil.ARTICLE_PERMALINK, String.class).
//                    addProjection(ArticleUtil.ARTICLE_CREATE_TIME, Long.class).
//                    addSort(ArticleUtil.ARTICLE_CREATE_TIME, SortDirection.DESCENDING);
            final List<Article> result = articleMapper.getByNewsArticlePerfect(ArticleUtil.ARTICLE_PERFECT_C_PERFECT);

            final List<JSONObject> ret = JsonUtil.listToJSONList(result);
            for (final JSONObject article : ret) {
                article.put(ArticleUtil.ARTICLE_PERMALINK,  SpringUtil.getServerPath() + article.optString(ArticleUtil.ARTICLE_PERMALINK));
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets news failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets articles by the specified tags (order by article create date desc).
     *
     * @param avatarViewMode the specified avatar view mode
     * @param tags           the specified tags
     * @param currentPageNum the specified page number
//     * @param articleFields  the specified article fields to return
     * @param pageSize       the specified page size
     * @return articles, return an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getArticlesByTags(final int avatarViewMode, final int currentPageNum, final int pageSize,
                                               final List<Tag> tags) throws Exception {
        try {
            final List<String> oIds = new ArrayList<>();
            for (final Tag tag : tags) {
                oIds.add(tag.getOid());
            }

//            Filter filter;
//            if (oIds.size() >= 2) {
//                filter = new CompositeFilter(CompositeFilterOperator.OR, filters);
//            } else {
//                filter = filters.get(0);
//            }

            // XXX: 这里的分页是有问题的，后面取文章的时候会少（因为一篇文章可以有多个标签，但是文章 id 一样）
            PageHelper.startPage(currentPageNum,pageSize,"oId DESC");
//            Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
//                    setFilter(filter).setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);

            List<TagArticle> tagArticleRelations = tagArticleMapper.getByTagIds(oIds);
//            final JSONArray tagArticleRelations = result.optJSONArray(Keys.RESULTS);

            final Set<String> articleIds = new HashSet<>();
            for (int i = 0; i < tagArticleRelations.size(); i++) {
                articleIds.add(tagArticleRelations.get(i).getArticle_oid());
            }

//            query = new Query().setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.IN, articleIds)).
//                    addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
//            for (final Map.Entry<String, Class<?>> articleField : articleFields.entrySet()) {
//                query.addProjection(articleField.getKey(), articleField.getValue());
//            }

            List<Article> result = articleMapper.getByArticleIds2(articleIds);

//            final List<JSONObject> ret = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            final List<JSONObject> ret = JsonUtil.listToJSONList(result);
            organizeArticles(avatarViewMode, ret);

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets articles by tags [tagLength=" + tags.size() + "] failed", e);
            throw new Exception(e);
        }
    }

    public List<JSONObject> getArticlesByTags2(final int avatarViewMode, final int currentPageNum, final int pageSize,
                                              final List<JSONObject> tags) throws Exception {
        try {
            final List<String> oIds = new ArrayList<>();
            for (final JSONObject tag : tags) {
                oIds.add(tag.optString(Keys.OBJECT_ID));
            }


            // XXX: 这里的分页是有问题的，后面取文章的时候会少（因为一篇文章可以有多个标签，但是文章 id 一样）
            PageHelper.startPage(currentPageNum,pageSize,"oId DESC");

            List<TagArticle> tagArticleRelations = tagArticleMapper.getByTagIds(oIds);
//            final JSONArray tagArticleRelations = result.optJSONArray(Keys.RESULTS);

            final Set<String> articleIds = new HashSet<>();
            for (int i = 0; i < tagArticleRelations.size(); i++) {
                articleIds.add(tagArticleRelations.get(i).getArticle_oid());
            }


            List<Article> result = articleMapper.getByArticleIds2(articleIds);

            final List<JSONObject> ret = JsonUtil.listToJSONList(result);
            organizeArticles(avatarViewMode, ret);

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets articles by tags [tagLength=" + tags.size() + "] failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets articles by the specified city (order by article create date desc).
     *
     * @param avatarViewMode the specified avatar view mode
     * @param city           the specified city
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return articles, return an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getArticlesByCity(final int avatarViewMode, final String city,
                                              final int currentPageNum, final int pageSize) throws Exception {
        try {
//            final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
//                    setFilter(new PropertyFilter(ArticleUtil.ARTICLE_CITY, FilterOperator.EQUAL, city))
//                    .setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);

            PageHelper.startPage(currentPageNum,pageSize,"oId DESC");
            final List<Article> result = articleMapper.getByCity(city);

            List<JSONObject> jsonObjectList = JsonUtil.listToJSONList(result);
            organizeArticles(avatarViewMode, jsonObjectList);

            final Integer participantsCnt = Symphonys.getInt("cityArticleParticipantsCnt");
//            final List<JSONObject> ret = JsonUtil.listToJSONList(result);
            genParticipants(avatarViewMode, jsonObjectList, participantsCnt);

            return jsonObjectList;
        } catch (final Exception e) {
            LOGGER.error( "Gets articles by city [" + city + "] failed", e);

            throw new Exception(e);
        }
    }

    /**
     * Gets articles by the specified tag (order by article create date desc).
     *
     * @param avatarViewMode the specified avatar view mode
     * @param sortMode       the specified sort mode, 0: default, 1: hot, 2: score, 3: reply, 4: perfect
     * @param tag            the specified tag
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return articles, return an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getArticlesByTag(final int avatarViewMode, final int sortMode, final JSONObject tag,
                                             final int currentPageNum, final int pageSize) throws Exception {
        try {
//            Query query = new Query();
            String orderBy = "";
            switch (sortMode) {
                case 0:
                    orderBy = "oId DESC";
//                    PageHelper.startPage(currentPageNum,pageSize,"oId DESC");
//                    query.addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
//                            setFilter(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tag.optString(Keys.OBJECT_ID)))
//                            .setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);

                    break;
                case 1:
                    orderBy = "articleCommentCount DESC,oId DESC";
//                    PageHelper.startPage(currentPageNum,pageSize,"articleCommentCount DESC,oId DESC");
//                    query.addSort(ArticleUtil.ARTICLE_COMMENT_CNT, SortDirection.DESCENDING).
//                            addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
//                            setFilter(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tag.optString(Keys.OBJECT_ID)))
//                            .setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);

                    break;
                case 2:
                    orderBy = "redditScore DESC,oId DESC";
//                    PageHelper.startPage(currentPageNum,pageSize,"redditScore DESC,oId DESC");
//                    query.addSort(ArticleUtil.REDDIT_SCORE, SortDirection.DESCENDING).
//                            addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
//                            setFilter(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tag.optString(Keys.OBJECT_ID)))
//                            .setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);

                    break;
                case 3:
                    orderBy = "articleLatestCmtTime DESC,oId DESC";
//                    PageHelper.startPage(currentPageNum,pageSize,"articleLatestCmtTime DESC,oId DESC");
//                    query.addSort(ArticleUtil.ARTICLE_LATEST_CMT_TIME, SortDirection.DESCENDING).
//                            addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
//                            setFilter(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tag.optString(Keys.OBJECT_ID)))
//                            .setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);

                    break;
                case 4:
                    orderBy = "articlePerfect DESC,oId DESC";
//                    PageHelper.startPage(currentPageNum,pageSize,"articlePerfect DESC,oId DESC");
//                    query.addSort(ArticleUtil.ARTICLE_PERFECT, SortDirection.DESCENDING).
//                            addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
//                            setFilter(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tag.optString(Keys.OBJECT_ID)))
//                            .setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);

                    break;
                default:
                    LOGGER.warn("Unknown sort mode [" + sortMode + "]");
                    orderBy = "oId DESC";
//                    PageHelper.startPage(currentPageNum,pageSize,"oId DESC");
//                    query.addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
//                            setFilter(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tag.optString(Keys.OBJECT_ID)))
//                            .setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);
            }

            PageHelper.startPage(currentPageNum,pageSize,orderBy);
            List<TagArticle> tagArticleRelations = tagArticleMapper.getByTagId(tag.optString(Keys.OBJECT_ID));
//            final JSONArray tagArticleRelations = result.optJSONArray(Keys.RESULTS);

            final List<String> articleIds = new ArrayList<>();
            for (int i = 0; i < tagArticleRelations.size(); i++) {
                articleIds.add(tagArticleRelations.get(i).getArticle_oid());
            }

//            query = new Query().setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.IN, articleIds)).
//                    addProjection(Keys.OBJECT_ID, String.class).
//                    addProjection(Article.ARTICLE_STICK, Long.class).
//                    addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
//                    addProjection(Article.ARTICLE_UPDATE_TIME, Long.class).
//                    addProjection(Article.ARTICLE_LATEST_CMT_TIME, Long.class).
//                    addProjection(Article.ARTICLE_AUTHOR_ID, String.class).
//                    addProjection(Article.ARTICLE_TITLE, String.class).
//                    addProjection(Article.ARTICLE_STATUS, Integer.class).
//                    addProjection(Article.ARTICLE_VIEW_CNT, Integer.class).
//                    addProjection(Article.ARTICLE_TYPE, Integer.class).
//                    addProjection(Article.ARTICLE_PERMALINK, String.class).
//                    addProjection(Article.ARTICLE_TAGS, String.class).
//                    addProjection(Article.ARTICLE_LATEST_CMTER_NAME, String.class).
//                    addProjection(Article.ARTICLE_SYNC_TO_CLIENT, Boolean.class).
//                    addProjection(Article.ARTICLE_COMMENT_CNT, Integer.class).
//                    addProjection(Article.ARTICLE_ANONYMOUS, Integer.class).
//                    addProjection(Article.ARTICLE_PERFECT, Integer.class).
//                    addProjection(Article.ARTICLE_CONTENT, String.class).
//                    addProjection(Article.ARTICLE_QNA_OFFER_POINT, Integer.class);

//            PageHelper.startPage(currentPageNum,pageSize,orderBy);
            List<Article> ret = articleMapper.getByArticleIds2(articleIds);

//            final List<JSONObject> ret = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            switch (sortMode) {
                default:
                    LOGGER.warn("Unknown sort mode [" + sortMode + "]");
                case 0:
                    Collections.sort(ret, (o1, o2) -> o2.getOid().compareTo(o1.getOid()));

                    break;
                case 1:
                    Collections.sort(ret, (o1, o2) -> {
                        final int v = o2.getArticleCommentCount() - o1.getArticleCommentCount();
                        if (0 == v) {
                            return o2.getOid().compareTo(o1.getOid());
                        }

                        return v > 0 ? 1 : -1;
                    });

                    break;
                case 2:
                    Collections.sort(ret, (o1, o2) -> {
                        final double v = o2.getRedditScore() - o1.getRedditScore();
                        if (0 == v) {
                            return o2.getOid().compareTo(o1.getOid());
                        }

                        return v > 0 ? 1 : -1;
                    });

                    break;
                case 3:
                    Collections.sort(ret, (o1, o2) -> {
                        final long v = (o2.getArticleLatestCmtTime()
                                - o1.getArticleLatestCmtTime());
                        if (0 == v) {
                            return o2.getOid().compareTo(o1.getOid());
                        }

                        return v > 0 ? 1 : -1;
                    });

                    break;
                case 4:
                    Collections.sort(ret, (o1, o2) -> {
                        final long v = (o2.getArticlePerfect() - o1.getArticlePerfect());
                        if (0 == v) {
                            return o2.getOid().compareTo(o1.getOid());
                        }

                        return v > 0 ? 1 : -1;
                    });

                    break;
            }

            List<JSONObject> jsonObjectList = JsonUtil.listToJSONList(ret);
            organizeArticles(avatarViewMode, jsonObjectList);

            final Integer participantsCnt = Symphonys.getInt("tagArticleParticipantsCnt");
//            List<JSONObject> jsonObjects = JsonUtil.listToJSONList(jsonObjectList);
            genParticipants(avatarViewMode, jsonObjectList, participantsCnt);

            return jsonObjectList;
        } catch (final Exception e) {
            LOGGER.error( "Gets articles by tag [tagTitle=" + tag.optString(TagUtil.TAG_TITLE) + "] failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets an article by the specified client article id.
     *
     * @param authorId        the specified author id
     * @param clientArticleId the specified client article id
     * @return article, return {@code null} if not found
     * @throws Exception service exception
     */
    public Article getArticleByClientArticleId(final String authorId, final String clientArticleId) throws Exception {
//        final List<Filter> filters = new ArrayList<>();
//        filters.add(new PropertyFilter(ArticleUtil.ARTICLE_CLIENT_ARTICLE_ID, FilterOperator.EQUAL, clientArticleId));
//        filters.add(new PropertyFilter(ArticleUtil.ARTICLE_AUTHOR_ID, FilterOperator.EQUAL, authorId));
//
//        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));
        try {
            final List<Article> result = articleMapper.getByClientArticleId(clientArticleId,authorId);
//            final JSONArray array = result.optJSONArray(Keys.RESULTS);
            if (0 == result.size()) {
                return null;
            }

            return result.get(0);
        } catch (final Exception e) {
            LOGGER.error( "Gets article [clientArticleId=" + clientArticleId + "] failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets an article with {@link #organizeArticle(int, JSONObject)} by the specified id.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param articleId      the specified id
     * @return article, return {@code null} if not found
     * @throws Exception service exception
     */
    public JSONObject getArticleById(final int avatarViewMode, final String articleId) throws Exception {
        Stopwatchs.start("Get article by id");
        try {
            final Article ret = articleMapper.get(articleId);
            if (null == ret) {
                return null;
            }

            JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(ret));
            organizeArticle(avatarViewMode, jsonObject);

            return jsonObject;
        } catch (final Exception e) {
            LOGGER.error( "Gets an article [articleId=" + articleId + "] failed", e);
            throw new Exception(e);
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets an article by the specified id.
     *
     * @param articleId the specified id
     * @return article, return {@code null} if not found
     * @throws Exception service exception
     */
    public Article getArticle(final String articleId) throws Exception {
        try {
            final Article ret = articleMapper.get(articleId);
            if (null == ret) {
                return null;
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets an article [articleId=" + articleId + "] failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets preview content of the article specified with the given article id.
     *
     * @param articleId the given article id
     * @param request   the specified request
     * @return preview content
     * @throws Exception service exception
     */
    public String getArticlePreviewContent(final String articleId, final HttpServletRequest request) throws Exception {
        final Article article = getArticle(articleId);
        if (null == article) {
            return null;
        }

        final int articleType = article.getArticleType();
        if (ArticleUtil.ARTICLE_TYPE_C_THOUGHT == articleType) {
            return null;
        }

        Stopwatchs.start("Get preview content");

        try {
            final int length = Integer.valueOf("150");
            String ret = article.getArticleContent();
            final String authorId = article.getArticleAuthorId();
            final UserExt author = userQueryService.getUser(authorId);

            if (null != author && UserExtUtil.USER_STATUS_C_INVALID == author.getUserStatus()
                    || ArticleUtil.ARTICLE_STATUS_C_INVALID == article.getArticleStatus()) {
                return Jsoup.clean(langPropsService.get("articleContentBlockLabel"), Whitelist.none());
            }

            final Set<String> userNames = userQueryService.getUserNames(ret);
            final UserExt currentUser = userQueryService.getCurrentUser(request);
            final String currentUserName = null == currentUser ? "" : currentUser.getUserName();
            final String authorName = author.getUserName();
            if (ArticleUtil.ARTICLE_TYPE_C_DISCUSSION == articleType && !authorName.equals(currentUserName)) {
                boolean invited = false;
                for (final String userName : userNames) {
                    if (userName.equals(currentUserName)) {
                        invited = true;

                        break;
                    }
                }

                if (!invited) {
                    String blockContent = langPropsService.get("articleDiscussionLabel");
                    blockContent = blockContent.replace("{user}", UserExtUtil.getUserLink(authorName));

                    return blockContent;
                }
            }

            ret = Emotions.convert(ret);
            ret = Markdowns.toHTML(ret);

            ret = Jsoup.clean(ret, Whitelist.none());
            if (ret.length() >= length) {
                ret = StringUtils.substring(ret, 0, length) + " ....";
            }

            return ret;
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets the user articles with the specified user id, page number and page size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param userId         the specified user id
     * @param anonymous      the specified article anonymous
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return user articles, return an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getUserArticles(final int avatarViewMode, final String userId, final int anonymous,
                                            final int currentPageNum, final int pageSize) throws Exception {

        PageHelper.startPage(currentPageNum,pageSize,"articleCreateTime DESC");
//        final Query query = new Query().addSort(ArticleUtil.ARTICLE_CREATE_TIME, SortDirection.DESCENDING)
//                .setCurrentPageNum(currentPageNum).setPageSize(pageSize).
//                        setFilter(CompositeFilterOperator.and(
//                                new PropertyFilter(ArticleUtil.ARTICLE_AUTHOR_ID, FilterOperator.EQUAL, userId),
//                                new PropertyFilter(ArticleUtil.ARTICLE_ANONYMOUS, FilterOperator.EQUAL, anonymous),
//                                new PropertyFilter(ArticleUtil.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, ArticleUtil.ARTICLE_STATUS_C_INVALID)));
        try {
            final PageInfo<Article> result = new PageInfo<>(articleMapper.getByArticleAuthorIdArticleAnonymousStatus(userId,anonymous,ArticleUtil.ARTICLE_STATUS_C_INVALID));
            final List<Article> ret = result.getList();

            List<JSONObject> jsonObjects = JsonUtil.listToJSONList(ret);
            if (jsonObjects.isEmpty()) {
                return jsonObjects;
            }

//            final JSONObject pagination = result.(Pagination.PAGINATION);
            final long recordCount = result.getTotal();
            final int pageCount = result.getPages();

            final JSONObject first = jsonObjects.get(0);

            first.put(Pagination.PAGINATION_RECORD_COUNT, recordCount);
            first.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);

            organizeArticles(avatarViewMode, jsonObjects);
            // TODO: 2018/11/11 需要增加分页数据

            return jsonObjects;
        } catch (final Exception e) {
            LOGGER.error( "Gets user articles failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets side hot articles.
     *
     * @return side hot articles, returns an empty list if not found
     */
    public List<JSONObject> getSideHotArticles() {
        return articleCache.getSideHotArticles();
    }

    /**
     * Gets side random articles.
     *
     * @return recent articles, returns an empty list if not found
     */
    public List<JSONObject> getSideRandomArticles() {
        return articleCache.getSideRandomArticles();
    }

    /**
     * Gets the random articles with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param fetchSize      the specified fetch size
     * @return random articles, returns an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getRandomArticles(final int avatarViewMode, final int fetchSize) throws Exception {
        try {
            final List<Article> ret = articleMapper.getRandomly(fetchSize);
            List<JSONObject> jsonObjectList = JsonUtil.listToJSONList(ret);
            organizeArticles(avatarViewMode, jsonObjectList);

            return jsonObjectList;
        } catch (final Exception e) {
            LOGGER.error( "Gets random articles failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Makes article showing filters.
     *
     * @return filter the article showing to user
     */
    private CompositeFilter makeArticleShowingFilter() {
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(ArticleUtil.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, ArticleUtil.ARTICLE_STATUS_C_INVALID));
        filters.add(new PropertyFilter(ArticleUtil.ARTICLE_TYPE, FilterOperator.NOT_EQUAL, ArticleUtil.ARTICLE_TYPE_C_DISCUSSION));
        return new CompositeFilter(CompositeFilterOperator.AND, filters);
    }

    /**
     * Makes recent article showing filters.
     *
     * @return filter the article showing to user
     */
    private CompositeFilter makeRecentArticleShowingFilter() {
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(ArticleUtil.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, ArticleUtil.ARTICLE_STATUS_C_INVALID));
        filters.add(new PropertyFilter(ArticleUtil.ARTICLE_TYPE, FilterOperator.NOT_EQUAL, ArticleUtil.ARTICLE_TYPE_C_DISCUSSION));
        filters.add(new PropertyFilter(ArticleUtil.ARTICLE_TAGS, FilterOperator.NOT_EQUAL, Tag.TAG_TITLE_C_SANDBOX));
        filters.add(new PropertyFilter(ArticleUtil.ARTICLE_TAGS, FilterOperator.NOT_LIKE, "B3log%"));
        return new CompositeFilter(CompositeFilterOperator.AND, filters);
    }

    /**
     * Makes the recent (sort by create time desc) articles with the specified fetch size.
     *
     * @param currentPageNum the specified current page number
     * @param fetchSize      the specified fetch size
     * @return recent articles query
     */
    private Query makeRecentDefaultQuery(final int currentPageNum, final int fetchSize) {
        final Query ret = new Query()
                .addSort(ArticleUtil.ARTICLE_STICK, SortDirection.DESCENDING)
                .addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                .setPageSize(fetchSize).setCurrentPageNum(currentPageNum);
        ret.setFilter(makeRecentArticleShowingFilter());
//        ret.addProjection(Keys.OBJECT_ID, String.class).
//                addProjection(Article.ARTICLE_STICK, Long.class).
//                addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
//                addProjection(Article.ARTICLE_UPDATE_TIME, Long.class).
//                addProjection(Article.ARTICLE_LATEST_CMT_TIME, Long.class).
//                addProjection(Article.ARTICLE_AUTHOR_ID, String.class).
//                addProjection(Article.ARTICLE_TITLE, String.class).
//                addProjection(Article.ARTICLE_STATUS, Integer.class).
//                addProjection(Article.ARTICLE_VIEW_CNT, Integer.class).
//                addProjection(Article.ARTICLE_TYPE, Integer.class).
//                addProjection(Article.ARTICLE_PERMALINK, String.class).
//                addProjection(Article.ARTICLE_TAGS, String.class).
//                addProjection(Article.ARTICLE_LATEST_CMTER_NAME, String.class).
//                addProjection(Article.ARTICLE_SYNC_TO_CLIENT, Boolean.class).
//                addProjection(Article.ARTICLE_COMMENT_CNT, Integer.class).
//                addProjection(Article.ARTICLE_ANONYMOUS, Integer.class).
//                addProjection(Article.ARTICLE_PERFECT, Integer.class).
//                addProjection(Article.ARTICLE_BAD_CNT, Integer.class).
//                addProjection(Article.ARTICLE_GOOD_CNT, Integer.class).
//                addProjection(Article.ARTICLE_COLLECT_CNT, Integer.class).
//                addProjection(Article.ARTICLE_WATCH_CNT, Integer.class).
//                addProjection(Article.ARTICLE_UA, String.class).
//                addProjection(Article.ARTICLE_CONTENT, String.class).
//                addProjection(Article.ARTICLE_QNA_OFFER_POINT, Integer.class);


        return ret;
    }

    /**
     * Makes the recent (sort by comment count desc) articles with the specified fetch size.
     *
     * @param currentPageNum the specified current page number
     * @param fetchSize      the specified fetch size
     * @return recent articles query
     */
    private Query makeRecentHotQuery(final int currentPageNum, final int fetchSize) {
        final String id = String.valueOf(DateUtils.addMonths(new Date(), -1).getTime());

        final Query ret = new Query()
                .addSort(ArticleUtil.ARTICLE_STICK, SortDirection.DESCENDING)
                .addSort(ArticleUtil.ARTICLE_COMMENT_CNT, SortDirection.DESCENDING)
                .addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                .setPageSize(fetchSize).setCurrentPageNum(currentPageNum);

        final CompositeFilter compositeFilter = makeRecentArticleShowingFilter();
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, id));
        filters.addAll(compositeFilter.getSubFilters());

        ret.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));
//        ret.addProjection(Keys.OBJECT_ID, String.class).
//                addProjection(Article.ARTICLE_STICK, Long.class).
//                addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
//                addProjection(Article.ARTICLE_UPDATE_TIME, Long.class).
//                addProjection(Article.ARTICLE_LATEST_CMT_TIME, Long.class).
//                addProjection(Article.ARTICLE_AUTHOR_ID, String.class).
//                addProjection(Article.ARTICLE_TITLE, String.class).
//                addProjection(Article.ARTICLE_STATUS, Integer.class).
//                addProjection(Article.ARTICLE_VIEW_CNT, Integer.class).
//                addProjection(Article.ARTICLE_TYPE, Integer.class).
//                addProjection(Article.ARTICLE_PERMALINK, String.class).
//                addProjection(Article.ARTICLE_TAGS, String.class).
//                addProjection(Article.ARTICLE_LATEST_CMTER_NAME, String.class).
//                addProjection(Article.ARTICLE_SYNC_TO_CLIENT, Boolean.class).
//                addProjection(Article.ARTICLE_COMMENT_CNT, Integer.class).
//                addProjection(Article.ARTICLE_ANONYMOUS, Integer.class).
//                addProjection(Article.ARTICLE_PERFECT, Integer.class).
//                addProjection(Article.ARTICLE_BAD_CNT, Integer.class).
//                addProjection(Article.ARTICLE_GOOD_CNT, Integer.class).
//                addProjection(Article.ARTICLE_COLLECT_CNT, Integer.class).
//                addProjection(Article.ARTICLE_WATCH_CNT, Integer.class).
//                addProjection(Article.ARTICLE_UA, String.class).
//                addProjection(Article.ARTICLE_CONTENT, String.class).
//                addProjection(Article.ARTICLE_QNA_OFFER_POINT, Integer.class);

        return ret;
    }

    /**
     * Makes the recent (sort by score desc) articles with the specified fetch size.
     *
     * @param currentPageNum the specified current page number
     * @param fetchSize      the specified fetch size
     * @return recent articles query
     */
    private Query makeRecentGoodQuery(final int currentPageNum, final int fetchSize) {
        final Query ret = new Query()
                .addSort(ArticleUtil.ARTICLE_STICK, SortDirection.DESCENDING)
                .addSort(ArticleUtil.REDDIT_SCORE, SortDirection.DESCENDING)
                .addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                .setPageSize(fetchSize).setCurrentPageNum(currentPageNum);
        ret.setFilter(makeRecentArticleShowingFilter());
//        ret.addProjection(Keys.OBJECT_ID, String.class).
//                addProjection(Article.ARTICLE_STICK, Long.class).
//                addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
//                addProjection(Article.ARTICLE_UPDATE_TIME, Long.class).
//                addProjection(Article.ARTICLE_LATEST_CMT_TIME, Long.class).
//                addProjection(Article.ARTICLE_AUTHOR_ID, String.class).
//                addProjection(Article.ARTICLE_TITLE, String.class).
//                addProjection(Article.ARTICLE_STATUS, Integer.class).
//                addProjection(Article.ARTICLE_VIEW_CNT, Integer.class).
//                addProjection(Article.ARTICLE_TYPE, Integer.class).
//                addProjection(Article.ARTICLE_PERMALINK, String.class).
//                addProjection(Article.ARTICLE_TAGS, String.class).
//                addProjection(Article.ARTICLE_LATEST_CMTER_NAME, String.class).
//                addProjection(Article.ARTICLE_SYNC_TO_CLIENT, Boolean.class).
//                addProjection(Article.ARTICLE_COMMENT_CNT, Integer.class).
//                addProjection(Article.ARTICLE_ANONYMOUS, Integer.class).
//                addProjection(Article.ARTICLE_PERFECT, Integer.class).
//                addProjection(Article.ARTICLE_BAD_CNT, Integer.class).
//                addProjection(Article.ARTICLE_GOOD_CNT, Integer.class).
//                addProjection(Article.ARTICLE_COLLECT_CNT, Integer.class).
//                addProjection(Article.ARTICLE_WATCH_CNT, Integer.class).
//                addProjection(Article.ARTICLE_UA, String.class).
//                addProjection(Article.ARTICLE_CONTENT, String.class).
//                addProjection(Article.ARTICLE_QNA_OFFER_POINT, Integer.class);

        return ret;
    }

    /**
     * Makes the recent (sort by latest comment time desc) articles with the specified fetch size.
     *
     * @param currentPageNum the specified current page number
     * @param fetchSize      the specified fetch size
     * @return recent articles query
     */
    private Query makeRecentReplyQuery(final int currentPageNum, final int fetchSize) {
        final Query ret = new Query()
                .addSort(ArticleUtil.ARTICLE_STICK, SortDirection.DESCENDING)
                .addSort(ArticleUtil.ARTICLE_LATEST_CMT_TIME, SortDirection.DESCENDING)
                .addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                .setPageSize(fetchSize).setCurrentPageNum(currentPageNum);
        ret.setFilter(makeRecentArticleShowingFilter());
//        ret.addProjection(Keys.OBJECT_ID, String.class).
//                addProjection(Article.ARTICLE_STICK, Long.class).
//                addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
//                addProjection(Article.ARTICLE_UPDATE_TIME, Long.class).
//                addProjection(Article.ARTICLE_LATEST_CMT_TIME, Long.class).
//                addProjection(Article.ARTICLE_AUTHOR_ID, String.class).
//                addProjection(Article.ARTICLE_TITLE, String.class).
//                addProjection(Article.ARTICLE_STATUS, Integer.class).
//                addProjection(Article.ARTICLE_VIEW_CNT, Integer.class).
//                addProjection(Article.ARTICLE_TYPE, Integer.class).
//                addProjection(Article.ARTICLE_PERMALINK, String.class).
//                addProjection(Article.ARTICLE_TAGS, String.class).
//                addProjection(Article.ARTICLE_LATEST_CMTER_NAME, String.class).
//                addProjection(Article.ARTICLE_SYNC_TO_CLIENT, Boolean.class).
//                addProjection(Article.ARTICLE_COMMENT_CNT, Integer.class).
//                addProjection(Article.ARTICLE_ANONYMOUS, Integer.class).
//                addProjection(Article.ARTICLE_PERFECT, Integer.class).
//                addProjection(Article.ARTICLE_BAD_CNT, Integer.class).
//                addProjection(Article.ARTICLE_GOOD_CNT, Integer.class).
//                addProjection(Article.ARTICLE_COLLECT_CNT, Integer.class).
//                addProjection(Article.ARTICLE_WATCH_CNT, Integer.class).
//                addProjection(Article.ARTICLE_UA, String.class).
//                addProjection(Article.ARTICLE_CONTENT, String.class).
//                addProjection(Article.ARTICLE_QNA_OFFER_POINT, Integer.class);

        return ret;
    }

    /**
     * Makes the top articles with the specified fetch size.
     *
     * @param currentPageNum the specified current page number
     * @param fetchSize      the specified fetch size
     * @return top articles query
     */
    private Query makeTopQuery(final int currentPageNum, final int fetchSize) {
        final Query query = new Query()
                .addSort(ArticleUtil.REDDIT_SCORE, SortDirection.DESCENDING)
                .addSort(ArticleUtil.ARTICLE_LATEST_CMT_TIME, SortDirection.DESCENDING)
                .setPageCount(1).setPageSize(fetchSize).setCurrentPageNum(currentPageNum);

        query.setFilter(makeArticleShowingFilter());
        return query;
    }

    /**
     * Gets the recent (sort by create time) articles with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param sortMode       the specified sort mode, 0: default, 1: hot, 2: score, 3: reply
     * @param currentPageNum the specified current page number
     * @param fetchSize      the specified fetch size
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "articles": [{
     *         "oId": "",
     *         "articleTitle": "",
     *         "articleContent": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @throws Exception service exception
     */
    public JSONObject getRecentArticles(final int avatarViewMode, final int sortMode,
                                        final int currentPageNum, final int fetchSize)
            throws Exception {
        final JSONObject ret = new JSONObject();

        Query query;
        switch (sortMode) {
            case 0:
//                query = makeRecentDefaultQuery(currentPageNum, fetchSize);
                final Query ret = new Query()
                        .addSort(ArticleUtil.ARTICLE_STICK, SortDirection.DESCENDING)
                        .addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                        .setPageSize(fetchSize).setCurrentPageNum(currentPageNum);
                ret.setFilter(makeRecentArticleShowingFilter());

                break;
            case 1:
                query = makeRecentHotQuery(currentPageNum, fetchSize);

                break;
            case 2:
                query = makeRecentGoodQuery(currentPageNum, fetchSize);

                break;
            case 3:
                query = makeRecentReplyQuery(currentPageNum, fetchSize);

                break;
            default:
                LOGGER.warn("Unknown sort mode [" + sortMode + "]");
                query = makeRecentDefaultQuery(currentPageNum, fetchSize);
        }

        JSONObject result = null;

        try {
            Stopwatchs.start("Query recent articles");

            result = articleMapper.get(query);
        } catch (final Exception e) {
            LOGGER.error( "Gets articles failed", e);

            throw new Exception(e);
        } finally {
            Stopwatchs.end();
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);

        final int windowSize = Symphonys.getInt("latestArticlesWindowSize");

        final List<Integer> pageNums = Paginator.paginate(currentPageNum, fetchSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, (Object) pageNums);

        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> articles = CollectionUtils.jsonArrayToList(data);

        try {
            organizeArticles(avatarViewMode, articles);
        } catch (final Exception e) {
            LOGGER.error( "Organizes articles failed", e);

            throw new Exception(e);
        }

        //final Integer participantsCnt = Symphonys.getInt("latestArticleParticipantsCnt");
        //genParticipants(articles, participantsCnt);
        ret.put(ArticleUtil.ARTICLES, (Object) articles);

        return ret;
    }

    /**
     * Gets the index recent (sort by create time) articles.
     *
     * @param avatarViewMode the specified avatar view mode
     * @return recent articles, returns an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getIndexRecentArticles(final int avatarViewMode) throws Exception {
        try {
            List<JSONObject> ret;
            List<Article> result;
            Stopwatchs.start("Query index recent articles");
            try {
                result = articleMapper.getRecentArticles(Symphonys.getInt("indexListCnt"));
            } finally {
                Stopwatchs.end();
            }

            ret = JsonUtil.listToJSONList(result);
            organizeArticles(avatarViewMode, ret);

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets index recent articles failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets the hot articles with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param fetchSize      the specified fetch size
     * @return hot articles, returns an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getHotArticles(final int avatarViewMode, final int fetchSize) throws Exception {
//        final Query query = makeTopQuery(1, fetchSize);
//        final Query query = new Query()
//                .addSort(ArticleUtil.REDDIT_SCORE, SortDirection.DESCENDING)
//                .addSort(ArticleUtil.ARTICLE_LATEST_CMT_TIME, SortDirection.DESCENDING)
//                .setPageCount(1).setPageSize(fetchSize).setCurrentPageNum(currentPageNum);
        PageHelper.startPage(1,fetchSize,"redditScore DESC, articleLatestCmtTime DESC");

//        final List<Filter> filters = new ArrayList<>();
//        filters.add(new PropertyFilter(ArticleUtil.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, ArticleUtil.ARTICLE_STATUS_C_INVALID));
//        filters.add(new PropertyFilter(ArticleUtil.ARTICLE_TYPE, FilterOperator.NOT_EQUAL, ArticleUtil.ARTICLE_TYPE_C_DISCUSSION));
//        return new CompositeFilter(CompositeFilterOperator.AND, filters);
//        query.setFilter(filters);
//        query.setFilter(makeArticleShowingFilter());


        try {
            List<JSONObject> ret;
            Stopwatchs.start("Query hot articles");
            try {
                final List<Article> result = articleMapper.getByStatusType(ArticleUtil.ARTICLE_STATUS_C_INVALID,ArticleUtil.ARTICLE_TYPE_C_DISCUSSION);
                ret = JsonUtil.listToJSONList(result);
            } finally {
                Stopwatchs.end();
            }

            organizeArticles(avatarViewMode, ret);

            Stopwatchs.start("Checks author status");
            try {
                for (final JSONObject article : ret) {
                    final String authorId = article.optString(ArticleUtil.ARTICLE_AUTHOR_ID);

                    final UserExt author = userMapper.get(authorId);

                    if (UserExtUtil.USER_STATUS_C_INVALID == author.getUserStatus()) {
                        article.put(ArticleUtil.ARTICLE_TITLE, langPropsService.get("articleTitleBlockLabel"));
                        article.put(ArticleUtil.ARTICLE_T_TITLE_EMOJI, langPropsService.get("articleTitleBlockLabel"));
                        article.put(ArticleUtil.ARTICLE_T_TITLE_EMOJI_UNICODE, langPropsService.get("articleTitleBlockLabel"));
                    }
                }
            } finally {
                Stopwatchs.end();
            }

//            final Integer participantsCnt = Symphonys.getInt("indexArticleParticipantsCnt");
//            genParticipants(ret, participantsCnt);
            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets index articles failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets the perfect articles with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param currentPageNum the specified current page number
     * @param fetchSize      the specified fetch size
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "articles": [{
     *         "oId": "",
     *         "articleTitle": "",
     *         "articleContent": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @throws Exception service exception
     */
    public JSONObject getPerfectArticles(final int avatarViewMode, final int currentPageNum, final int fetchSize)
            throws Exception {
//        final Query query = new Query()
//                .addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
//                .setCurrentPageNum(currentPageNum).setPageSize(fetchSize);
        PageHelper.startPage(currentPageNum,fetchSize,"oId DESC");
//        query.setFilter(new PropertyFilter(ArticleUtil.ARTICLE_PERFECT, FilterOperator.EQUAL, ArticleUtil.ARTICLE_PERFECT_C_PERFECT));

        final JSONObject ret = new JSONObject();

        PageInfo<Article> result = null;

        try {
            Stopwatchs.start("Query recent articles");

            result = new PageInfo<>(articleMapper.getByArticlePerfect(ArticleUtil.ARTICLE_PERFECT_C_PERFECT));
        } catch (final Exception e) {
            LOGGER.error( "Gets articles failed", e);

            throw new Exception(e);
        } finally {
            Stopwatchs.end();
        }

//        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);
        final int pageCount = result.getPages();

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);

        final int windowSize = Symphonys.getInt("latestArticlesWindowSize");

        final List<Integer> pageNums = Paginator.paginate(currentPageNum, fetchSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, (Object) pageNums);

//        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> articles = JsonUtil.listToJSONList(result.getList());

        try {
            organizeArticles(avatarViewMode, articles);
        } catch (final Exception e) {
            LOGGER.error( "Organizes articles failed", e);

            throw new Exception(e);
        }

        //final Integer participantsCnt = Symphonys.getInt("latestArticleParticipantsCnt");
        //genParticipants(articles, participantsCnt);
        ret.put(ArticleUtil.ARTICLES, (Object) articles);

        return ret;
    }

    /**
     * Gets the index hot articles with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @return hot articles, returns an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getIndexHotArticles(final int avatarViewMode) throws Exception {
//        final Query query = new Query()
//                .addSort(ArticleUtil.REDDIT_SCORE, SortDirection.DESCENDING)
//                .addSort(ArticleUtil.ARTICLE_LATEST_CMT_TIME, SortDirection.DESCENDING)
//                .setPageCount(1).setPageSize(Symphonys.getInt("indexListCnt")).setCurrentPageNum(1);
        PageHelper.startPage(1, Symphonys.getInt("indexListCnt"),"redditScore DESC, articleLatestCmtTime DESC");
//        query.setFilter(makeArticleShowingFilter());
//        filters.add(new PropertyFilter(ArticleUtil.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, ArticleUtil.ARTICLE_STATUS_C_INVALID));
//        filters.add(new PropertyFilter(ArticleUtil.ARTICLE_TYPE, FilterOperator.NOT_EQUAL, ArticleUtil.ARTICLE_TYPE_C_DISCUSSION));
//        query.addProjection(Keys.OBJECT_ID, String.class).
//                addProjection(Article.ARTICLE_STICK, Long.class).
//                addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
//                addProjection(Article.ARTICLE_UPDATE_TIME, Long.class).
//                addProjection(Article.ARTICLE_LATEST_CMT_TIME, Long.class).
//                addProjection(Article.ARTICLE_AUTHOR_ID, String.class).
//                addProjection(Article.ARTICLE_TITLE, String.class).
//                addProjection(Article.ARTICLE_STATUS, Integer.class).
//                addProjection(Article.ARTICLE_VIEW_CNT, Integer.class).
//                addProjection(Article.ARTICLE_TYPE, Integer.class).
//                addProjection(Article.ARTICLE_PERMALINK, String.class).
//                addProjection(Article.ARTICLE_TAGS, String.class).
//                addProjection(Article.ARTICLE_LATEST_CMTER_NAME, String.class).
//                addProjection(Article.ARTICLE_SYNC_TO_CLIENT, Boolean.class).
//                addProjection(Article.ARTICLE_COMMENT_CNT, Integer.class).
//                addProjection(Article.ARTICLE_ANONYMOUS, Integer.class).
//                addProjection(Article.ARTICLE_PERFECT, Integer.class);

        try {
            List<JSONObject> ret;
            Stopwatchs.start("Query index hot articles");
            try {
                final List<Article> result = articleMapper.getByStatusType(ArticleUtil.ARTICLE_STATUS_C_INVALID,ArticleUtil.ARTICLE_TYPE_C_DISCUSSION);
                ret = JsonUtil.listToJSONList(result);
            } finally {
                Stopwatchs.end();
            }

            organizeArticles(avatarViewMode, ret);

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets index hot articles failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets the index perfect articles.
     *
     * @return hot articles, returns an empty list if not found
     */
    public List<JSONObject> getIndexPerfectArticles() {
        return articleCache.getPerfectArticles();
    }

    /**
     * Gets the recent articles with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param currentPageNum the specified current page number
     * @param fetchSize      the specified fetch size
     * @return recent articles, returns an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getRecentArticlesWithComments(final int avatarViewMode,
                                                          final int currentPageNum, final int fetchSize) throws Exception {
        return getArticles(avatarViewMode, makeRecentDefaultQuery(currentPageNum, fetchSize));
    }

    /**
     * Gets the index articles with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param currentPageNum the specified current page number
     * @param fetchSize      the specified fetch size
     * @return recent articles, returns an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getTopArticlesWithComments(final int avatarViewMode,
                                                       final int currentPageNum, final int fetchSize) throws Exception {
        return getArticles(avatarViewMode, makeTopQuery(currentPageNum, fetchSize));
    }

    /**
     * The specific articles.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param query          conditions
     * @return articles
     * @throws Exception service exception
     */
    private List<JSONObject> getArticles(final int avatarViewMode, final Query query) throws Exception {
        try {
            final JSONObject result = articleMapper.get(query);
            final List<JSONObject> ret = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            organizeArticles(avatarViewMode, ret);
            final List<JSONObject> stories = new ArrayList<>();

            for (final JSONObject article : ret) {
                final JSONObject story = new JSONObject();
                final String authorId = article.optString(ArticleUtil.ARTICLE_AUTHOR_ID);
                final UserExt author = userMapper.get(authorId);
                if (UserExtUtil.USER_STATUS_C_INVALID == author.getUserStatus()) {
                    story.put("title", langPropsService.get("articleTitleBlockLabel"));
                } else {
                    story.put("title", article.optString(ArticleUtil.ARTICLE_TITLE));
                }
                story.put("id", article.optLong("oId"));
                story.put("url",  SpringUtil.getServerPath() + article.optString(ArticleUtil.ARTICLE_PERMALINK));
                story.put("user_display_name", article.optString(ArticleUtil.ARTICLE_T_AUTHOR_NAME));
                story.put("user_job", author.getUserIntro());
                story.put("comment_html", article.optString(ArticleUtil.ARTICLE_CONTENT));
                story.put("comment_count", article.optInt(ArticleUtil.ARTICLE_COMMENT_CNT));
                story.put("vote_count", article.optInt(ArticleUtil.ARTICLE_GOOD_CNT));
                story.put("created_at", formatDate(article.get(ArticleUtil.ARTICLE_CREATE_TIME)));
                story.put("user_portrait_url", article.optString(ArticleUtil.ARTICLE_T_AUTHOR_THUMBNAIL_URL));
                story.put("comments", getAllComments(avatarViewMode, article.optString("oId")));
                final String tagsString = article.optString(ArticleUtil.ARTICLE_TAGS);
                String[] tags = null;
                if (!Strings.isEmptyOrNull(tagsString)) {
                    tags = tagsString.split(",");
                }
                story.put("badge", tags == null ? "" : tags[0]);
                stories.add(story);
            }
            final Integer participantsCnt = Symphonys.getInt("indexArticleParticipantsCnt");
            genParticipants(avatarViewMode, stories, participantsCnt);
            return stories;
        } catch (final Exception e) {
            LOGGER.error( "Gets index articles failed", e);

            throw new Exception(e);
        }
    }

    /**
     * Gets the article comments with the specified article id.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param articleId      the specified article id
     * @return comments, return an empty list if not found
     * @throws Exception    service exception
     * @throws JSONException       json exception
     * @throws Exception Mapper exception
     */
    private List<JSONObject> getAllComments(final int avatarViewMode, final String articleId)
            throws Exception, JSONException, Exception {
        final List<JSONObject> commments = new ArrayList<>();
        final List<JSONObject> articleComments = commentQueryService.getArticleComments(
                avatarViewMode, articleId, 1, Integer.MAX_VALUE, UserExtUtil.USER_COMMENT_VIEW_MODE_C_TRADITIONAL);
        for (final JSONObject ac : articleComments) {
            final JSONObject comment = new JSONObject();
            final UserExt author = userMapper.get(ac.optString(CommentUtil.COMMENT_AUTHOR_ID));
            comment.put("id", ac.optLong("oId"));
            comment.put("body_html", ac.optString(CommentUtil.COMMENT_CONTENT));
            comment.put("depth", 0);
            comment.put("user_display_name", ac.optString(CommentUtil.COMMENT_T_AUTHOR_NAME));
            comment.put("user_job", author.getUserIntro());
            comment.put("vote_count", 0);
            comment.put("created_at", formatDate(ac.get(CommentUtil.COMMENT_CREATE_TIME)));
            comment.put("user_portrait_url", ac.optString(CommentUtil.COMMENT_T_ARTICLE_AUTHOR_THUMBNAIL_URL));
            commments.add(comment);
        }
        return commments;
    }

    /**
     * The demand format date.
     *
     * @param date the original date
     * @return the format date like "2015-08-03T07:26:57Z"
     */
    private String formatDate(final Object date) {
        return DateFormatUtils.format(((Date) date).getTime(), "yyyy-MM-dd")
                + "T" + DateFormatUtils.format(((Date) date).getTime(), "HH:mm:ss") + "Z";
    }

    /**
     * Organizes the specified articles.
     *
     * @param avatarViewMode the specified avatarViewMode
     * @param articles       the specified articles
     * @throws Exception Mapper exception
     * @see #organizeArticle(int, JSONObject)
     */
    public void organizeArticles(final int avatarViewMode, final List<JSONObject> articles) throws Exception {
        Stopwatchs.start("Organize articles");
        try {
            for (final JSONObject article : articles) {
                organizeArticle(avatarViewMode, article);
            }
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Organizes the specified article.
     * <ul>
     * <li>converts create/update/latest comment time (long) to date type and format string</li>
     * <li>generates author thumbnail URL</li>
     * <li>generates author name</li>
     * <li>escapes article title &lt; and &gt;</li>
     * <li>generates article heat</li>
     * <li>generates article view count display format(1k+/1.5k+...)</li>
     * <li>generates time ago text</li>
     * <li>generates comment time ago text</li>
     * <li>generates stick remains minutes</li>
     * <li>anonymous process</li>
     * <li>builds tag objects</li>
     * <li>generates article preview content</li>
     * <li>extracts the first image URL</li>
     * <li>image processing if using Qiniu</li>
     * </ul>
     *
     * @param avatarViewMode the specified avatar view mode
     * @param article        the specified article
     * @throws Exception Mapper exception
     */
    public void organizeArticle(final int avatarViewMode, final JSONObject article) throws Exception {
        article.put(ArticleUtil.ARTICLE_T_ORIGINAL_CONTENT, article.optString(ArticleUtil.ARTICLE_CONTENT));
        toArticleDate(article);
        genArticleAuthor(avatarViewMode, article);

        final String previewContent = getArticleMetaDesc(article);
        article.put(ArticleUtil.ARTICLE_T_PREVIEW_CONTENT, previewContent);

        if (StringUtils.length(previewContent) > 100) {
            article.put(ArticleUtil.ARTICLE_T_THUMBNAIL_URL, getArticleThumbnail(article));
        } else {
            article.put(ArticleUtil.ARTICLE_T_THUMBNAIL_URL, "");
        }

        qiniuImgProcessing(article);

        final String title = Encode.forHtml(article.optString(ArticleUtil.ARTICLE_TITLE));
        article.put(ArticleUtil.ARTICLE_TITLE, title);

        article.put(ArticleUtil.ARTICLE_T_TITLE_EMOJI, Emotions.convert(title));
        article.put(ArticleUtil.ARTICLE_T_TITLE_EMOJI_UNICODE, EmojiParser.parseToUnicode(title));

        if (ArticleUtil.ARTICLE_STATUS_C_INVALID == article.optInt(ArticleUtil.ARTICLE_STATUS)) {
            article.put(ArticleUtil.ARTICLE_TITLE, langPropsService.get("articleTitleBlockLabel"));
            article.put(ArticleUtil.ARTICLE_T_TITLE_EMOJI, langPropsService.get("articleTitleBlockLabel"));
            article.put(ArticleUtil.ARTICLE_T_TITLE_EMOJI_UNICODE, langPropsService.get("articleTitleBlockLabel"));
            article.put(ArticleUtil.ARTICLE_CONTENT, langPropsService.get("articleContentBlockLabel"));
        }

        final String articleId = article.optString(Keys.OBJECT_ID);
        Integer viewingCnt = ArticleChannel.ARTICLE_VIEWS.get(articleId);
        if (null == viewingCnt) {
            viewingCnt = 0;
        }

        article.put(ArticleUtil.ARTICLE_T_HEAT, viewingCnt);

        final int viewCnt = article.optInt(ArticleUtil.ARTICLE_VIEW_CNT);
        final double views = (double) viewCnt / 1000;
        if (views >= 1) {
            final DecimalFormat df = new DecimalFormat("#.#");
            article.put(ArticleUtil.ARTICLE_T_VIEW_CNT_DISPLAY_FORMAT, df.format(views) + "K");
        }

        final long stick = article.optLong(ArticleUtil.ARTICLE_STICK);
        long expired;
        if (stick > 0) {
            expired = stick + Symphonys.getLong("stickArticleTime");
            final long remainsMills = Math.abs(System.currentTimeMillis() - expired);

            article.put(ArticleUtil.ARTICLE_T_STICK_REMAINS, (int) Math.floor((double) remainsMills / 1000 / 60));
        } else {
            article.put(ArticleUtil.ARTICLE_T_STICK_REMAINS, 0);
        }

        String articleLatestCmterName = article.optString(ArticleUtil.ARTICLE_LATEST_CMTER_NAME);
        if (StringUtils.isNotBlank(articleLatestCmterName)
                && UserRegisterValidation.invalidUserName(articleLatestCmterName)) {
            articleLatestCmterName = UserExtUtil.ANONYMOUS_USER_NAME;
            article.put(ArticleUtil.ARTICLE_LATEST_CMTER_NAME, articleLatestCmterName);
        }

//        final Query query = new Query()
//                .setPageCount(1).setCurrentPageNum(1).setPageSize(1)
//                .setFilter(new PropertyFilter(CommentUtil.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId)).
//                        addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        PageHelper.startPage(1,1,"oId DESC");
        final List<Comment> cmts = commentMapper.getByCommentOnArticleId(articleId);
        if (cmts.size() > 0) {
            final Comment latestCmt = cmts.get(0);
            JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(latestCmt));
//            latestCmt.setClientCommentId(latestCmt.optString(CommentUtil.COMMENT_CLIENT_COMMENT_ID));
            article.put(ArticleUtil.ARTICLE_T_LATEST_CMT, jsonObject);
        }

        // builds tag objects
        final String tagsStr = article.optString(ArticleUtil.ARTICLE_TAGS);
        final String[] tagTitles = tagsStr.split(",");

        final List<JSONObject> tags = new ArrayList<>();
        for (final String tagTitle : tagTitles) {
            final JSONObject tag = new JSONObject();
            tag.put(TagUtil.TAG_TITLE, tagTitle);

            final String uri = tagMapper.getURIByTitle(tagTitle);
            if (null != uri) {
                tag.put(TagUtil.TAG_URI, uri);
            } else {
                tag.put(TagUtil.TAG_URI, tagTitle);

                tagMapper.getURIByTitle(tagTitle);
            }

            tags.add(tag);
        }
        article.put(ArticleUtil.ARTICLE_T_TAG_OBJS, (Object) tags);
    }

    /**
     * Gets the first image URL of the specified article.
     *
     * @param article the specified article
     * @return the first image URL, returns {@code ""} if not found
     */
    private String getArticleThumbnail(final JSONObject article) {
        final int articleType = article.optInt(ArticleUtil.ARTICLE_TYPE);
        if (ArticleUtil.ARTICLE_TYPE_C_THOUGHT == articleType) {
            return "";
        }

        final String content = article.optString(ArticleUtil.ARTICLE_CONTENT);
        final String html = Markdowns.toHTML(content);
        String ret = StringUtils.substringBetween(html, "<img src=\"", "\"");

        final boolean qiniuEnabled = Symphonys.getBoolean("qiniu.enabled");
        if (qiniuEnabled) {
            final String qiniuDomain = Symphonys.get("qiniu.domain");
            if (StringUtils.startsWith(ret, qiniuDomain)) {
                ret = StringUtils.substringBefore(ret, "?");
                ret += "?imageView2/1/w/" + 180 + "/h/" + 135 + "/format/jpg/interlace/1/q";
            } else {
                ret = "";
            }
        } else {
            if (!StringUtils.startsWith(ret,  SpringUtil.getServerPath())) {
                ret = "";
            }
        }

        if (StringUtils.isBlank(ret)) {
            ret = "";
        }

        return ret;
    }

    /**
     * Qiniu image processing.
     *
     * @param article the specified article
     * @return the first image URL, returns {@code ""} if not found
     */
    private void qiniuImgProcessing(final JSONObject article) {
        final boolean qiniuEnabled = Symphonys.getBoolean("qiniu.enabled");
        if (!qiniuEnabled) {
            return;
        }

        final int articleType = article.optInt(ArticleUtil.ARTICLE_TYPE);
        if (ArticleUtil.ARTICLE_TYPE_C_THOUGHT == articleType) {
            return;
        }

        final String qiniuDomain = Symphonys.get("qiniu.domain");
        String content = article.optString(ArticleUtil.ARTICLE_CONTENT);
        final String html = Markdowns.toHTML(content);

        final String[] imgSrcs = StringUtils.substringsBetween(html, "<img src=\"", "\"");
        if (null == imgSrcs) {
            return;
        }

        for (final String imgSrc : imgSrcs) {
            if (!StringUtils.startsWith(imgSrc, qiniuDomain) || StringUtils.contains(imgSrc, ".gif")) {
                continue;
            }

            content = StringUtils.replaceOnce(content, imgSrc, imgSrc + "?imageView2/2/w/768/format/jpg/interlace/0/q");
        }

        article.put(ArticleUtil.ARTICLE_CONTENT, content);
    }

    /**
     * Converts the specified article create/update/latest comment time (long) to date type and format str.
     *
     * @param article the specified article
     */
    private void toArticleDate(final JSONObject article) {
        article.put(Common.TIME_AGO, Times.getTimeAgo(article.optLong(ArticleUtil.ARTICLE_CREATE_TIME), Locales.getLocale()));
        article.put(Common.CMT_TIME_AGO, Times.getTimeAgo(article.optLong(ArticleUtil.ARTICLE_LATEST_CMT_TIME), Locales.getLocale()));
        final Date createDate = new Date(article.optLong(ArticleUtil.ARTICLE_CREATE_TIME));
        article.put(ArticleUtil.ARTICLE_CREATE_TIME, createDate);
        article.put(ArticleUtil.ARTICLE_CREATE_TIME_STR, DateFormatUtils.format(createDate, "yyyy-MM-dd HH:mm:ss"));
        final Date updateDate = new Date(article.optLong(ArticleUtil.ARTICLE_UPDATE_TIME));
        article.put(ArticleUtil.ARTICLE_UPDATE_TIME, updateDate);
        article.put(ArticleUtil.ARTICLE_UPDATE_TIME_STR, DateFormatUtils.format(updateDate, "yyyy-MM-dd HH:mm:ss"));
        final Date latestCmtDate = new Date(article.optLong(ArticleUtil.ARTICLE_LATEST_CMT_TIME));
        article.put(ArticleUtil.ARTICLE_LATEST_CMT_TIME, latestCmtDate);
        article.put(ArticleUtil.ARTICLE_LATEST_CMT_TIME_STR, DateFormatUtils.format(latestCmtDate, "yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Generates the specified article author name and thumbnail URL.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param article        the specified article
     * @throws Exception Mapper exception
     */
    private void genArticleAuthor(final int avatarViewMode, final JSONObject article) throws Exception {
        final String authorId = article.optString(ArticleUtil.ARTICLE_AUTHOR_ID);

        final UserExt author = userMapper.get(authorId);
        article.put(ArticleUtil.ARTICLE_T_AUTHOR, author);

        if (ArticleUtil.ARTICLE_ANONYMOUS_C_ANONYMOUS == article.optInt(ArticleUtil.ARTICLE_ANONYMOUS)) {
            article.put(ArticleUtil.ARTICLE_T_AUTHOR_NAME, UserExtUtil.ANONYMOUS_USER_NAME);
            article.put(ArticleUtil.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "210", avatarQueryService.getDefaultAvatarURL("210"));
            article.put(ArticleUtil.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "48", avatarQueryService.getDefaultAvatarURL("48"));
            article.put(ArticleUtil.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "20", avatarQueryService.getDefaultAvatarURL("20"));
        } else {
            JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(author));
            article.put(ArticleUtil.ARTICLE_T_AUTHOR_NAME, author.getUserName());
            article.put(ArticleUtil.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "210",
                    avatarQueryService.getAvatarURLByUser(avatarViewMode, jsonObject, "210"));
            article.put(ArticleUtil.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "48",
                    avatarQueryService.getAvatarURLByUser(avatarViewMode, jsonObject, "48"));
            article.put(ArticleUtil.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "20",
                    avatarQueryService.getAvatarURLByUser(avatarViewMode, jsonObject, "20"));
        }
    }

    /**
     * Generates participants for the specified articles.
     *
     * @param avatarViewMode  the specified avatar view mode
     * @param articles        the specified articles
     * @param participantsCnt the specified generate size
     * @throws Exception service exception
     */
    public void genParticipants(final int avatarViewMode,
                                final List<JSONObject> articles, final Integer participantsCnt) throws Exception {
        Stopwatchs.start("Generates participants");
        try {
            for (final JSONObject article : articles) {
                article.put(ArticleUtil.ARTICLE_T_PARTICIPANTS, (Object) Collections.emptyList());

                if (article.optInt(ArticleUtil.ARTICLE_COMMENT_CNT) < 1) {
                    continue;
                }

                final List<JSONObject> articleParticipants = getArticleLatestParticipants(
                        avatarViewMode, article.optString(Keys.OBJECT_ID), participantsCnt);
                article.put(ArticleUtil.ARTICLE_T_PARTICIPANTS, (Object) articleParticipants);
            }
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets the article participants (commenters) with the specified article article id and fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param articleId      the specified article id
     * @param fetchSize      the specified fetch size
     * @return article participants, for example,      <pre>
     * [
     *     {
     *         "oId": "",
     *         "articleParticipantName": "",
     *         "articleParticipantThumbnailURL": "",
     *         "articleParticipantThumbnailUpdateTime": long,
     *         "commentId": ""
     *     }, ....
     * ]
     * </pre>, returns an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getArticleLatestParticipants(final int avatarViewMode,
                                                         final String articleId, final int fetchSize) throws Exception {
//        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
//                .setFilter(new PropertyFilter(CommentUtil.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId))
//                .addProjection(Keys.OBJECT_ID, String.class)
//                .addProjection(Comment.COMMENT_AUTHOR_ID, String.class)
//                .setPageCount(1).setCurrentPageNum(1).setPageSize(fetchSize);
        PageHelper.startPage(1,fetchSize,"oId DESC");
        final List<JSONObject> ret = new ArrayList<>();

        try {
            final List<Comment> records = commentMapper.getByCommentOnArticleId(articleId);
            List<JSONObject> jsonObjectList = JsonUtil.listToJSONList(records);

            final List<JSONObject> comments = new ArrayList<>();
//            final JSONArray records = result.optJSONArray(Keys.RESULTS);
            for (int i = 0; i < jsonObjectList.size(); i++) {
                final JSONObject comment = jsonObjectList.get(i);

                boolean exist = false;
                // deduplicate
                for (final JSONObject c : comments) {
                    if (comment.optString(CommentUtil.COMMENT_AUTHOR_ID).equals(
                            c.optString(CommentUtil.COMMENT_AUTHOR_ID))) {
                        exist = true;

                        break;
                    }
                }

                if (!exist) {
                    comments.add(comment);
                }
            }

            for (final JSONObject comment : comments) {
                final String userId = comment.optString(CommentUtil.COMMENT_AUTHOR_ID);

                final UserExt commenter = userMapper.get(userId);
                JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(commenter));
                final String email = commenter.getUserEmail();

                String thumbnailURL = Symphonys.get("defaultThumbnailURL");
                if (!UserExtUtil.DEFAULT_CMTER_EMAIL.equals(email)) {

                    thumbnailURL = avatarQueryService.getAvatarURLByUser(avatarViewMode, jsonObject, "48");
                }

                final JSONObject participant = new JSONObject();
                participant.put(ArticleUtil.ARTICLE_T_PARTICIPANT_NAME, jsonObject.optString(User.USER_NAME));
                participant.put(ArticleUtil.ARTICLE_T_PARTICIPANT_THUMBNAIL_URL, thumbnailURL);
                participant.put(ArticleUtil.ARTICLE_T_PARTICIPANT_THUMBNAIL_UPDATE_TIME,
                        jsonObject.optLong(UserExtUtil.USER_UPDATE_TIME));
                participant.put(ArticleUtil.ARTICLE_T_PARTICIPANT_URL, jsonObject.optString(User.USER_URL));
                participant.put(Keys.OBJECT_ID, jsonObject.optString(Keys.OBJECT_ID));
                participant.put(CommentUtil.COMMENT_T_ID, comment.optString(Keys.OBJECT_ID));

                ret.add(participant);
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets article [" + articleId + "] participants failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Processes the specified article content.
     * <ul>
     * <li>Generates &#64;username home URL</li>
     * <li>Markdowns</li>
     * <li>Generates secured article content</li>
     * <li>Blocks the article if need</li>
     * <li>Generates emotion images</li>
     * <li>Generates article link with article id</li>
     * <li>Generates article abstract (preview content)</li>
     * <li>Generates article ToC</li>
     * </ul>
     *
     * @param article the specified article, for example,
     *                "articleTitle": "",
     *                ....,
     *                "author": {}
     * @param request the specified request
     * @throws Exception service exception
     */
    public void processArticleContent(final JSONObject article, final HttpServletRequest request)
            throws Exception {
        Stopwatchs.start("Process content");

        try {
            final JSONObject author = article.optJSONObject(ArticleUtil.ARTICLE_T_AUTHOR);
            if (null != author && UserExtUtil.USER_STATUS_C_INVALID == author.optInt(UserExtUtil.USER_STATUS)
                    || ArticleUtil.ARTICLE_STATUS_C_INVALID == article.optInt(ArticleUtil.ARTICLE_STATUS)) {
                article.put(ArticleUtil.ARTICLE_TITLE, langPropsService.get("articleTitleBlockLabel"));
                article.put(ArticleUtil.ARTICLE_T_TITLE_EMOJI, langPropsService.get("articleTitleBlockLabel"));
                article.put(ArticleUtil.ARTICLE_T_TITLE_EMOJI_UNICODE, langPropsService.get("articleTitleBlockLabel"));
                article.put(ArticleUtil.ARTICLE_CONTENT, langPropsService.get("articleContentBlockLabel"));
                article.put(ArticleUtil.ARTICLE_T_PREVIEW_CONTENT, Jsoup.clean(langPropsService.get("articleContentBlockLabel"), Whitelist.none()));
                article.put(ArticleUtil.ARTICLE_T_TOC, "");
                article.put(ArticleUtil.ARTICLE_REWARD_CONTENT, "");
                article.put(ArticleUtil.ARTICLE_REWARD_POINT, 0);
                article.put(ArticleUtil.ARTICLE_QNA_OFFER_POINT, 0);

                return;
            }

            article.put(ArticleUtil.ARTICLE_T_PREVIEW_CONTENT, article.optString(ArticleUtil.ARTICLE_TITLE));

            String articleContent = article.optString(ArticleUtil.ARTICLE_CONTENT);
            article.put(Common.DISCUSSION_VIEWABLE, true);

            final UserExt currentUser = userQueryService.getCurrentUser(request);
            final String currentUserName = null == currentUser ? "" : currentUser.getUserName();
            final String currentRole = null == currentUser ? "" : currentUser.getUserRole();
            final String authorName = article.optString(ArticleUtil.ARTICLE_T_AUTHOR_NAME);

            final int articleType = article.optInt(ArticleUtil.ARTICLE_TYPE);
            if (ArticleUtil.ARTICLE_TYPE_C_DISCUSSION == articleType
                    && !authorName.equals(currentUserName) && !RoleUtil.ROLE_ID_C_ADMIN.equals(currentRole)) {
                boolean invited = false;

                final Set<String> userNames = userQueryService.getUserNames(articleContent);
                for (final String userName : userNames) {
                    if (userName.equals(currentUserName)) {
                        invited = true;

                        break;
                    }
                }

                if (!invited) {
                    String blockContent = langPropsService.get("articleDiscussionLabel");
                    blockContent = blockContent.replace("{user}", UserExtUtil.getUserLink(authorName));

                    article.put(ArticleUtil.ARTICLE_CONTENT, blockContent);
                    article.put(Common.DISCUSSION_VIEWABLE, false);
                    article.put(ArticleUtil.ARTICLE_REWARD_CONTENT, "");
                    article.put(ArticleUtil.ARTICLE_REWARD_POINT, 0);
                    article.put(ArticleUtil.ARTICLE_QNA_OFFER_POINT, 0);
                    article.put(ArticleUtil.ARTICLE_T_TOC, "");
                    article.put(ArticleUtil.ARTICLE_AUDIO_URL, "");

                    return;
                }
            }

            if (ArticleUtil.ARTICLE_TYPE_C_THOUGHT != articleType) {
                articleContent = shortLinkQueryService.linkArticle(articleContent);
                articleContent = shortLinkQueryService.linkTag(articleContent);
                articleContent = Emotions.convert(articleContent);
                article.put(ArticleUtil.ARTICLE_CONTENT, articleContent);
            }

            if (article.optInt(ArticleUtil.ARTICLE_REWARD_POINT) > 0) {
                String rewardContent = article.optString(ArticleUtil.ARTICLE_REWARD_CONTENT);
                rewardContent = shortLinkQueryService.linkArticle(rewardContent);
                rewardContent = shortLinkQueryService.linkTag(rewardContent);
                rewardContent = Emotions.convert(rewardContent);
                article.put(ArticleUtil.ARTICLE_REWARD_CONTENT, rewardContent);
            }

            markdown(article);
            articleContent = article.optString(ArticleUtil.ARTICLE_CONTENT);

            if (ArticleUtil.ARTICLE_TYPE_C_THOUGHT != articleType) {
                articleContent = MP3Players.render(articleContent);
                articleContent = VideoPlayers.render(articleContent);
            }

            article.put(ArticleUtil.ARTICLE_CONTENT, articleContent);
            article.put(ArticleUtil.ARTICLE_T_PREVIEW_CONTENT, getArticleMetaDesc(article));
            article.put(ArticleUtil.ARTICLE_T_TOC, getArticleToC(article));
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets articles by the specified request json object.
     *
     * @param avatarViewMode    the specified avatar view mode
     * @param requestJSONObject the specified request json object, for example
     *                          "oId": "", // optional
     *                          "paginationCurrentPageNum": 1,
     *                          "paginationPageSize": 20,
     *                          "paginationWindowSize": 10
//     * @param articleFields     the specified article fields to return
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "articles": [{
     *         "oId": "",
     *         "articleTitle": "",
     *         "articleContent": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @throws Exception service exception
     * @see Pagination
     */
    public JSONObject getArticles(final int avatarViewMode,
                                  final JSONObject requestJSONObject/*, final Map<String, Class<?>> articleFields*/) throws Exception {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
//        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
//                addSort(ArticleUtil.ARTICLE_STICK, SortDirection.DESCENDING).
//                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        PageHelper.startPage(currentPageNum, pageSize,"articleStick DESC, oId DESC");
//        for (final Map.Entry<String, Class<?>> articleField : articleFields.entrySet()) {
//            query.addProjection(articleField.getKey(), articleField.getValue());
//        }

        List<JSONObject> articles = new ArrayList<>();
        int pageCount = 0;
        Article article = null;
        try {
            if (requestJSONObject.has(Keys.OBJECT_ID)) {
//            query.setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL, requestJSONObject.optString(Keys.OBJECT_ID)));
                article = articleMapper.getByOid(requestJSONObject.optString(Keys.OBJECT_ID));
                pageCount = 1;
                articles.add(new JSONObject(JsonUtil.objectToJson(article)));
            } else {
                PageInfo<Article> result = new PageInfo<>(articleMapper.getAll());
                articles = JsonUtil.listToJSONList(result.getList());
                pageCount = result.getPages();
            }


        } catch (final Exception e) {
            LOGGER.error("Gets articles failed", e);

            throw new Exception(e);
        }

//        pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

//        final JSONArray data = result.optJSONArray(Keys.RESULTS);
//        final List<JSONObject> articles = CollectionUtils.jsonArrayToList(data);

        try {
            organizeArticles(avatarViewMode, articles);
        } catch (final Exception e) {
            LOGGER.error( "Organizes articles failed", e);

            throw new Exception(e);
        }

        ret.put(ArticleUtil.ARTICLES, articles);

        return ret;
    }

    /**
     * Markdowns the specified article content.
     * <ul>
     * <li>Markdowns article content/reward content</li>
     * <li>Generates secured article content/reward content</li>
     * </ul>
     *
     * @param article the specified article content
     */
    private void markdown(final JSONObject article) {
        String content = article.optString(ArticleUtil.ARTICLE_CONTENT);

        final int articleType = article.optInt(ArticleUtil.ARTICLE_TYPE);
        if (ArticleUtil.ARTICLE_TYPE_C_THOUGHT != articleType) {
            content = Markdowns.toHTML(content);
            content = Markdowns.clean(content,  SpringUtil.getServerPath() + article.optString(ArticleUtil.ARTICLE_PERMALINK));
        } else {
            final Document.OutputSettings outputSettings = new Document.OutputSettings();
            outputSettings.prettyPrint(false);

            content = Jsoup.clean(content,  SpringUtil.getServerPath() + article.optString(ArticleUtil.ARTICLE_PERMALINK),
                    Whitelist.relaxed().addAttributes(":all", "id", "target", "class").
                            addTags("span", "hr").addAttributes("iframe", "src", "width", "height")
                            .addAttributes("audio", "controls", "src"), outputSettings);

            content = content.replace("\n", "\\n").replace("'", "\\'")
                    .replace("\"", "\\\"");
        }

        article.put(ArticleUtil.ARTICLE_CONTENT, content);

        if (article.optInt(ArticleUtil.ARTICLE_REWARD_POINT) > 0) {
            String rewardContent = article.optString(ArticleUtil.ARTICLE_REWARD_CONTENT);
            rewardContent = Markdowns.toHTML(rewardContent);
            rewardContent = Markdowns.clean(rewardContent,
                     SpringUtil.getServerPath() + article.optString(ArticleUtil.ARTICLE_PERMALINK));
            article.put(ArticleUtil.ARTICLE_REWARD_CONTENT, rewardContent);
        }
    }

    /**
     * Gets meta description content of the specified article.
     *
     * @param article the specified article
     * @return meta description
     */
    public String getArticleMetaDesc(final JSONObject article) {
        final String articleId = article.optString(Keys.OBJECT_ID);
        String articleAbstract = articleCache.getArticleAbstract(articleId);
        if (StringUtils.isNotBlank(articleAbstract)) {
            return articleAbstract;
        }

        Stopwatchs.start("Meta Desc");
        try {
            final int articleType = article.optInt(ArticleUtil.ARTICLE_TYPE);
            if (ArticleUtil.ARTICLE_TYPE_C_THOUGHT == articleType) {
                return "....";
            }

            if (ArticleUtil.ARTICLE_TYPE_C_DISCUSSION == articleType) {
                return langPropsService.get("articleAbstractDiscussionLabel", Locales.getLocale());
            }

            final int length = Integer.valueOf("150");

            String ret = article.optString(ArticleUtil.ARTICLE_CONTENT);
            ret = Emotions.clear(ret);
            try {
                ret = Markdowns.toHTML(ret);
            } catch (final Exception e) {
                LOGGER.error( "Parses article abstract failed [id=" + articleId + ", md=" + ret + "]");
                throw e;
            }

            final Whitelist whitelist = Whitelist.basicWithImages();
            whitelist.addTags("object", "video");
            ret = Jsoup.clean(ret, whitelist);

            final int threshold = 20;
            String[] pics = StringUtils.substringsBetween(ret, "<img", ">");
            if (null != pics) {
                if (pics.length > threshold) {
                    pics = Arrays.copyOf(pics, threshold);
                }

                final String[] picsRepl = new String[pics.length];
                for (int i = 0; i < picsRepl.length; i++) {
                    picsRepl[i] = langPropsService.get("picTagLabel", Locales.getLocale());
                    pics[i] = "<img" + pics[i] + ">";

                    if (i > threshold) {
                        break;
                    }
                }

                ret = StringUtils.replaceEach(ret, pics, picsRepl);
            }

            String[] objs = StringUtils.substringsBetween(ret, "<object>", "</object>");
            if (null != objs) {
                if (objs.length > threshold) {
                    objs = Arrays.copyOf(objs, threshold);
                }

                final String[] objsRepl = new String[objs.length];
                for (int i = 0; i < objsRepl.length; i++) {
                    objsRepl[i] = langPropsService.get("objTagLabel", Locales.getLocale());
                    objs[i] = "<object>" + objs[i] + "</object>";

                    if (i > threshold) {
                        break;
                    }
                }

                ret = StringUtils.replaceEach(ret, objs, objsRepl);
            }

            objs = StringUtils.substringsBetween(ret, "<video", "</video>");
            if (null != objs) {
                if (objs.length > threshold) {
                    objs = Arrays.copyOf(objs, threshold);
                }

                final String[] objsRepl = new String[objs.length];
                for (int i = 0; i < objsRepl.length; i++) {
                    objsRepl[i] = langPropsService.get("objTagLabel", Locales.getLocale());
                    objs[i] = "<video" + objs[i] + "</video>";

                    if (i > threshold) {
                        break;
                    }
                }

                ret = StringUtils.replaceEach(ret, objs, objsRepl);
            }

            String tmp = Jsoup.clean(Jsoup.parse(ret).text(), Whitelist.none());
            if (tmp.length() >= length && null != pics) {
                tmp = StringUtils.substring(tmp, 0, length) + " ....";
                ret = tmp.replaceAll("\"", "'");

                articleCache.putArticleAbstract(articleId, ret);

                return ret;
            }

            String[] urls = StringUtils.substringsBetween(ret, "<a", "</a>");
            if (null != urls) {
                if (urls.length > threshold) {
                    urls = Arrays.copyOf(urls, threshold);
                }

                final String[] urlsRepl = new String[urls.length];
                for (int i = 0; i < urlsRepl.length; i++) {
                    urlsRepl[i] = langPropsService.get("urlTagLabel", Locales.getLocale());
                    urls[i] = "<a" + urls[i] + "</a>";
                }

                ret = StringUtils.replaceEach(ret, urls, urlsRepl);
            }

            tmp = Jsoup.clean(Jsoup.parse(ret).text(), Whitelist.none());
            if (tmp.length() >= length) {
                tmp = StringUtils.substring(tmp, 0, length) + " ....";
            }

            ret = tmp.replaceAll("\"", "'");

            articleCache.putArticleAbstract(articleId, ret);

            return ret;
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets ToC of the specified article.
     *
     * @param article the specified article
     * @return ToC
     */
    private String getArticleToC(final JSONObject article) {
        Stopwatchs.start("ToC");

        if (ArticleUtil.ARTICLE_TYPE_C_THOUGHT == article.optInt(ArticleUtil.ARTICLE_TYPE)) {
            return "";
        }

        try {
            final String content = article.optString(ArticleUtil.ARTICLE_CONTENT);
            final Document doc = Jsoup.parse(content, StringUtils.EMPTY, Parser.htmlParser());
            doc.outputSettings().prettyPrint(false);
            final Elements hs = doc.select("h1, h2, h3, h4, h5");
            if (hs.size() < 3) {
                return "";
            }

            final StringBuilder listBuilder = new StringBuilder();
            listBuilder.append("<ul class=\"article-toc\">");
            for (int i = 0; i < hs.size(); i++) {
                final Element element = hs.get(i);
                final String tagName = element.tagName().toLowerCase();
                final String text = element.text();
                final String id = "toc_" + tagName + "_" + i;
                element.attr("id", id);
                listBuilder.append("<li class='toc-").append(tagName).append("'><a data-id=\"").append(id).append("\" href=\"javascript:Comment._bgFade($('#").append(id).append("'))\">").append(text).append(
                        "</a></li>");
            }
            listBuilder.append("</ul>");

            article.put(ArticleUtil.ARTICLE_CONTENT, doc.select("body").html());

            return listBuilder.toString();
        } finally {
            Stopwatchs.end();
        }
    }
}
