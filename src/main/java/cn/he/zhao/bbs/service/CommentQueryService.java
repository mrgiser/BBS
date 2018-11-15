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

import cn.he.zhao.bbs.entityUtil.CommentUtil;
import cn.he.zhao.bbs.entityUtil.CommonUtil;
import cn.he.zhao.bbs.entityUtil.UserExtUtil;
import cn.he.zhao.bbs.spring.*;
import cn.he.zhao.bbs.mapper.ArticleMapper;
import cn.he.zhao.bbs.mapper.CommentMapper;
import cn.he.zhao.bbs.mapper.UserMapper;
import cn.he.zhao.bbs.entity.Article;
import cn.he.zhao.bbs.entity.Comment;
import cn.he.zhao.bbs.entity.UserExt;
import cn.he.zhao.bbs.entityUtil.my.CollectionUtils;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.Pagination;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.util.*;
import cn.he.zhao.bbs.validate.UserRegisterValidation;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.owasp.encoder.Encode;

import javax.swing.*;
import java.util.*;

/**
 * Comment management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.12.1.1, Jun 27, 2018
 * @since 0.2.0
 */
@Service
public class CommentQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentQueryService.class);

    /**
     * RevisionUtil query service.
     */
    @Autowired
    private RevisionQueryService revisionQueryService;

    /**
     * Comment Mapper.
     */
    @Autowired
    private CommentMapper commentMapper;

    /**
     * Article Mapper.
     */
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * User Mapper.
     */
    @Autowired
    private UserMapper userMapper;

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
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Short link query service.
     */
    @Autowired
    private ShortLinkQueryService shortLinkQueryService;


    /**
     * Gets the URL of a comment.
     *
     * @param commentId the specified comment id
     * @param sortMode  the specified sort mode
     * @param pageSize  the specified comment page size
     * @return comment URL, return {@code null} if not found
     */
    public String getCommentURL(final String commentId, final int sortMode, final int pageSize) {
        try {
            final Comment comment = commentMapper.get(commentId);
            if (null == comment) {
                return null;
            }

            final String articleId = comment.getCommentOnArticleId();
            final Article article = articleMapper.getByPrimaryKey(articleId);
            if (null == article) {
                return null;
            }
            String title = Encode.forHtml(article.getArticleTitle());
            title = Emotions.convert(title);
            final int commentPage = getCommentPage(articleId, commentId, sortMode, pageSize);

            return "<a href=\"" + SpringUtil.getServerPath() + "/article/" + articleId + "?p=" + commentPage
                    + "&m=" + sortMode + "#" + commentId + "\" target=\"_blank\">" + title + "</a>";
        } catch (final Exception e) {
            LOGGER.error("Gets comment URL failed", e);

            return null;
        }
    }

    /**
     * Gets the offered (accepted) comment of an article specified by the given article id.
     *
     * @param avatarViewMode  the specified avatar view mode
     * @param commentViewMode the specified comment view mode
     * @param articleId       the given article id
     * @return accepted comment, return {@code null} if not found
     */
    public JSONObject getOfferedComment(final int avatarViewMode, final int commentViewMode, final String articleId) {
        Stopwatchs.start("Gets accepted comment");
        JSONObject result = new JSONObject();
        try {

            PageHelper.startPage(1, 1);

//            final Query query = new Query().addSort(CommentUtil.COMMENT_SCORE, SortDirection.DESCENDING).setCurrentPageNum(1).setPageCount(1)
//                    .setFilter(CompositeFilterOperator.and(
//                            new PropertyFilter(CommentUtil.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId),
//                            new PropertyFilter(CommentUtil.COMMENT_QNA_OFFERED, FilterOperator.EQUAL, CommentUtil.COMMENT_QNA_OFFERED_C_YES),
//                            new PropertyFilter(CommentUtil.COMMENT_STATUS, FilterOperator.EQUAL, CommentUtil.COMMENT_STATUS_C_VALID)
//                    ));
            try {
                final List<Comment> comments = commentMapper.getAcceptedCommentsForArticle(articleId);
                if (comments.isEmpty()) {
                    return null;
                }

                final Comment ret = comments.get(0);
                organizeComment(avatarViewMode, ret);

                final int pageSize = Symphonys.getInt("articleCommentsPageSize");
//                ret.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, getCommentPage(
//                        articleId, ret.optString(Keys.OBJECT_ID), commentViewMode, pageSize));
                result.put("comment", ret);
                result.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, getCommentPage(
                        articleId, ret.getOid(), commentViewMode, pageSize));

                return result;
            } catch (final Exception e) {
                LOGGER.error("Gets accepted comment failed", e);

                return null;
            }
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets the page number of a comment.
     *
     * @param articleId the specified article id
     * @param commentId the specified comment id
     * @param sortMode  the specified sort mode
     * @param pageSize  the specified comment page size
     * @return page number, return {@code 1} if occurs exception
     */
    public int getCommentPage(final String articleId, final String commentId, final int sortMode, final int pageSize) {
        PageHelper.startPage(1, Integer.MAX_VALUE);
//        final Query numQuery = new Query()
//                .setPageSize(Integer.MAX_VALUE).setCurrentPageNum(1).setPageCount(1);

        Stopwatchs.start("Get comment page");

        try {
            long num = 0;
            switch (sortMode) {
                case UserExtUtil.USER_COMMENT_VIEW_MODE_C_TRADITIONAL: {
                    PageHelper.startPage(1, Integer.MAX_VALUE, "OId ASCE");
                    num = commentMapper.countByArticleIdAndLessCommentId(articleId, commentId);


                    break;
//                    numQuery.setFilter(CompositeFilterOperator.and(
//                        new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId),
//                        new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN, commentId)
//                )).addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);
                }

                case UserExtUtil.USER_COMMENT_VIEW_MODE_C_REALTIME: {
                    PageHelper.startPage(1, Integer.MAX_VALUE, "OId ASCE");
                    num = commentMapper.countByArticleIdAndGREACommentId(articleId, commentId);
                    break;
                    //                    numQuery.setFilter(CompositeFilterOperator.and(
//                            new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId),
//                            new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN, commentId)
//                    )).addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
                }

            }
            return (int) ((num / pageSize) + 1);
        } catch (final Exception e) {
            LOGGER.error("Gets comment page failed", e);

            return 1;
        } finally {
            Stopwatchs.end();
        }

    }

    /**
     * Gets original comment of a comment specified by the given comment id.
     *
     * @param avatarViewMode  the specified avatar view mode
     * @param commentViewMode the specified comment view mode
     * @param commentId       the given comment id
     * @return original comment, return {@code null} if not found
     */
    public JSONObject getOriginalComment(final int avatarViewMode, final int commentViewMode, final String commentId) {
        try {
            final Comment comment = commentMapper.get(commentId);

            organizeComment(avatarViewMode, comment);

            final int pageSize = Symphonys.getInt("articleCommentsPageSize");

            final JSONObject ret = new JSONObject();

            final JSONObject commentAuthor = (JSONObject) comment.getCommenter();
            if (UserExtUtil.USER_XXX_STATUS_C_PRIVATE == commentAuthor.optInt(UserExtUtil.USER_UA_STATUS)) {
                ret.put(CommentUtil.COMMENT_UA, "");
            }

            ret.put(CommentUtil.COMMENT_T_AUTHOR_NAME, comment.getCommentAuthorName());
            ret.put(CommentUtil.COMMENT_T_AUTHOR_THUMBNAIL_URL, comment.getCommentAuthorThumbnailURL());
            ret.put(CommentUtil.TIME_AGO, comment.getTimeAgo());
            ret.put(CommentUtil.COMMENT_CREATE_TIME_STR, comment.getCommentCreateTimeStr());
            // TODO: 2018/11/15 comment 没有一下值，设置为默认值？
//            ret.put(Common.REWARED_COUNT, comment.getcommo (Common.REWARED_COUNT));
//            ret.put(Common.REWARDED, comment.get(Common.REWARDED));
            ret.put(Common.REWARED_COUNT, "");
            ret.put(Common.REWARDED, false);
            ret.put(Keys.OBJECT_ID, commentId);
            ret.put(CommentUtil.COMMENT_CONTENT, comment.getCommentContent());
            ret.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, getCommentPage(
                    comment.getCommentOnArticleId(), commentId,
                    commentViewMode, pageSize));

            return ret;
        } catch (final Exception e) {
            LOGGER.error("Get replies failed", e);

            return null;
        }
    }

    /**
     * Gets replies of a comment specified by the given comment id.
     *
     * @param avatarViewMode  the specified avatar view mode
     * @param commentViewMode the specified comment view mode
     * @param commentId       the given comment id
     * @return a list of replies, return an empty list if not found
     */
    public List<JSONObject> getReplies(final int avatarViewMode, final int commentViewMode, final String commentId) {

        PageHelper.startPage(1,Integer.MAX_VALUE,"oId DESC");
//        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
//                setPageSize(Integer.MAX_VALUE).setCurrentPageNum(1).setPageCount(1)
//                .setFilter(CompositeFilterOperator.and(
//                        new PropertyFilter(CommentUtil.COMMENT_ORIGINAL_COMMENT_ID, FilterOperator.EQUAL, commentId),
//                        new PropertyFilter(CommentUtil.COMMENT_STATUS, FilterOperator.EQUAL, CommentUtil.COMMENT_STATUS_C_VALID)
//                ));
        try {
            final List<Comment> comments = commentMapper.getRepliesOfComment(commentId,CommentUtil.COMMENT_STATUS_C_VALID);

            organizeComments(avatarViewMode, comments);

            final int pageSize = Symphonys.getInt("articleCommentsPageSize");

            final List<JSONObject> ret = new ArrayList<>();
            for (final Comment comment : comments) {
                final JSONObject reply = new JSONObject();
                ret.add(reply);

                final JSONObject commentAuthor = (JSONObject) comment.getCommenter();
                if (UserExtUtil.USER_XXX_STATUS_C_PRIVATE == commentAuthor.optInt(UserExtUtil.USER_UA_STATUS)) {
                    reply.put(CommentUtil.COMMENT_UA, "");
                }

                reply.put(CommentUtil.COMMENT_T_AUTHOR_NAME, comment.getCommentAuthorName());
                reply.put(CommentUtil.COMMENT_T_AUTHOR_THUMBNAIL_URL, comment.getCommentAuthorThumbnailURL());
                reply.put(Common.TIME_AGO, comment.getTimeAgo());
                reply.put(CommentUtil.COMMENT_CREATE_TIME_STR, comment.getCommentCreateTimeStr());
                // TODO: 2018/11/15 comment 没有一下值，设置为默认值？
//                reply.put(Common.REWARED_COUNT, comment.optString(Common.REWARED_COUNT));
//                reply.put(Common.REWARDED, comment.optBoolean(Common.REWARDED));
                reply.put(Common.REWARED_COUNT, "");
                reply.put(Common.REWARDED, false);
                reply.put(Keys.OBJECT_ID, comment.getOid());
                reply.put(CommentUtil.COMMENT_CONTENT, comment.getCommentContent());
                reply.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, getCommentPage(
                        comment.getCommentOnArticleId(), reply.optString(Keys.OBJECT_ID),
                        commentViewMode, pageSize));
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error("Get replies failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Gets nice comments of an article specified by the given article id.
     *
     * @param avatarViewMode  the specified avatar view mode
     * @param commentViewMode the specified comment view mode
     * @param articleId       the given article id
     * @param fetchSize       the specified fetch size
     * @return a list of nice comments, return an empty list if not found
     */
    public List<JSONObject> getNiceComments(final int avatarViewMode, final int commentViewMode,
                                            final String articleId, final int fetchSize) {
        Stopwatchs.start("Gets nice comments");
        try {
            PageHelper.startPage(1,fetchSize,"commentScore DESC") ;
//            final Query query = new Query().addSort(CommentUtil.COMMENT_SCORE, SortDirection.DESCENDING).
//                    setPageSize(fetchSize).setCurrentPageNum(1).setPageCount(1)
//                    .setFilter(CompositeFilterOperator.and(
//                            new PropertyFilter(CommentUtil.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId),
//                            new PropertyFilter(CommentUtil.COMMENT_SCORE, FilterOperator.GREATER_THAN, 0D),
//                            new PropertyFilter(CommentUtil.COMMENT_STATUS, FilterOperator.EQUAL, CommentUtil.COMMENT_STATUS_C_VALID)
//                    ));
            try {
                final List<Comment> ret = commentMapper.getNiceCommentsofArticle(articleId,0D,CommentUtil.COMMENT_STATUS_C_VALID);

                organizeComments(avatarViewMode, ret);
                List<JSONObject> jsonObjects = JsonUtil.listToJSONList(ret);

                final int pageSize = Symphonys.getInt("articleCommentsPageSize");

                for (final JSONObject comment : jsonObjects) {
                    comment.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, getCommentPage(
                            articleId, comment.optString(Keys.OBJECT_ID),
                            commentViewMode, pageSize));
                }

                return jsonObjects;
            } catch (final Exception e) {
                LOGGER.error("Get nice comments failed", e);

                return Collections.emptyList();
            }
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets comment count of the specified day.
     *
     * @param day the specified day
     * @return comment count
     */
    public int getCommentCntInDay(final Date day) {
        final long time = day.getTime();
        final long start = Times.getDayStartTime(time);
        final long end = Times.getDayEndTime(time);

//        final Query query = new Query().setFilter(CompositeFilterOperator.and(
//                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, start),
//                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN, end),
//                new PropertyFilter(CommentUtil.COMMENT_STATUS, FilterOperator.EQUAL, CommentUtil.COMMENT_STATUS_C_VALID)
//        ));

        try {
            return (int) commentMapper.countByTime(start,end,CommentUtil.COMMENT_STATUS_C_VALID);
        } catch (final Exception e) {
            LOGGER.error("Count day comment failed", e);

            return 1;
        }
    }

    /**
     * Gets comment count of the specified month.
     *
     * @param day the specified month
     * @return comment count
     */
    public int getCommentCntInMonth(final Date day) {
        final long time = day.getTime();
        final long start = Times.getMonthStartTime(time);
        final long end = Times.getMonthEndTime(time);

//        final Query query = new Query().setFilter(CompositeFilterOperator.and(
//                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, start),
//                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN, end),
//                new PropertyFilter(CommentUtil.COMMENT_STATUS, FilterOperator.EQUAL, CommentUtil.COMMENT_STATUS_C_VALID)
//        ));

        try {
            return (int) commentMapper.countByTime(start,end, CommentUtil.COMMENT_STATUS_C_VALID);
        } catch (final Exception e) {
            LOGGER.error("Count month comment failed", e);

            return 1;
        }
    }

    /**
     * Gets a comment with {@link #organizeComment(int, Comment)} by the specified comment id.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param commentId      the specified comment id
     * @return comment, returns {@code null} if not found
     * @throws Exception service exception
     */
    public Comment getCommentById(final int avatarViewMode, final String commentId) throws Exception {

        try {
            final Comment ret = commentMapper.get(commentId);
            if (null == ret) {
                return null;
            }

            organizeComment(avatarViewMode, ret);

            return ret;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);

            throw new Exception("Gets comment[id=" + commentId + "] failed");
        }
    }

    /**
     * Gets a comment by the specified id.
     *
     * @param commentId the specified id
     * @return comment, return {@code null} if not found
     * @throws Exception service exception
     */
    public Comment getComment(final String commentId) throws Exception {
        try {
            final Comment ret = commentMapper.get(commentId);

            if (null == ret) {
                return null;
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error("Gets a comment [commentId=" + commentId + "] failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets the latest comments with the specified fetch size.
     * <p>
     * <p>
     * The returned comments content is plain text.
     * </p>
     *
     * @param avatarViewMode the specified avatar view mode
     * @param fetchSize      the specified fetch size
     * @return the latest comments, returns an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getLatestComments(final int avatarViewMode, final int fetchSize) throws Exception {
        final Query query = new Query().addSort(Comment.COMMENT_CREATE_TIME, SortDirection.DESCENDING)
                .setCurrentPageNum(1).setPageSize(fetchSize).setPageCount(1);
        try {
            final JSONObject result = commentMapper.get(query);
            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            for (final JSONObject comment : ret) {
                comment.put(Comment.COMMENT_CREATE_TIME, comment.optLong(Comment.COMMENT_CREATE_TIME));
                final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
                final JSONObject article = articleMapper.get(articleId);
                comment.put(Comment.COMMENT_T_ARTICLE_TITLE, Emotions.clear(article.optString(Article.ARTICLE_TITLE)));
                comment.put(Comment.COMMENT_T_ARTICLE_PERMALINK, article.optString(Article.ARTICLE_PERMALINK));

                final String commenterId = comment.optString(Comment.COMMENT_AUTHOR_ID);
                final JSONObject commenter = userMapper.get(commenterId);

                if (UserExt.USER_STATUS_C_INVALID == commenter.optInt(UserExt.USER_STATUS)
                        || Comment.COMMENT_STATUS_C_INVALID == comment.optInt(Comment.COMMENT_STATUS)) {
                    comment.put(Comment.COMMENT_CONTENT, langPropsService.get("commentContentBlockLabel"));
                }

                if (Article.ARTICLE_TYPE_C_DISCUSSION == article.optInt(Article.ARTICLE_TYPE)) {
                    comment.put(Comment.COMMENT_CONTENT, "....");
                }

                String content = comment.optString(Comment.COMMENT_CONTENT);
                content = Emotions.clear(content);
                content = Jsoup.clean(content, Whitelist.none());
                if (StringUtils.isBlank(content)) {
                    comment.put(Comment.COMMENT_CONTENT, "....");
                } else {
                    comment.put(Comment.COMMENT_CONTENT, content);
                }

                final String commenterEmail = commenter.optString(User.USER_EMAIL);
                String avatarURL = Symphonys.get("defaultThumbnailURL");
                if (!UserExt.DEFAULT_CMTER_EMAIL.equals(commenterEmail)) {
                    avatarURL = avatarQueryService.getAvatarURLByUser(avatarViewMode, commenter, "20");
                }
                commenter.put(UserExt.USER_AVATAR_URL, avatarURL);

                comment.put(Comment.COMMENT_T_COMMENTER, commenter);
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error("Gets user comments failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets the user comments with the specified user id, page number and page size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param userId         the specified user id
     * @param anonymous      the specified comment anonymous
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @param viewer         the specified viewer, may be {@code null}
     * @return user comments, return an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getUserComments(final int avatarViewMode, final String userId, final int anonymous,
                                            final int currentPageNum, final int pageSize, final JSONObject viewer) throws Exception {
        final Query query = new Query().addSort(Comment.COMMENT_CREATE_TIME, SortDirection.DESCENDING)
                .setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                        setFilter(CompositeFilterOperator.and(
                                new PropertyFilter(Comment.COMMENT_AUTHOR_ID, FilterOperator.EQUAL, userId),
                                new PropertyFilter(Comment.COMMENT_ANONYMOUS, FilterOperator.EQUAL, anonymous)
                        ));
        try {
            final JSONObject result = commentMapper.get(query);
            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            if (ret.isEmpty()) {
                return ret;
            }

            final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
            final int recordCount = pagination.optInt(Pagination.PAGINATION_RECORD_COUNT);
            final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);

            final JSONObject first = ret.get(0);
            first.put(Pagination.PAGINATION_RECORD_COUNT, recordCount);
            first.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);

            for (final JSONObject comment : ret) {
                comment.put(Comment.COMMENT_CREATE_TIME, new Date(comment.optLong(Comment.COMMENT_CREATE_TIME)));

                final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
                final JSONObject article = articleMapper.get(articleId);

                comment.put(Comment.COMMENT_T_ARTICLE_TITLE,
                        Article.ARTICLE_STATUS_C_INVALID == article.optInt(Article.ARTICLE_STATUS)
                                ? langPropsService.get("articleTitleBlockLabel")
                                : Emotions.convert(article.optString(Article.ARTICLE_TITLE)));
                comment.put(Comment.COMMENT_T_ARTICLE_TYPE, article.optInt(Article.ARTICLE_TYPE));
                comment.put(Comment.COMMENT_T_ARTICLE_PERMALINK, article.optString(Article.ARTICLE_PERMALINK));
                comment.put(Comment.COMMENT_T_ARTICLE_PERFECT, article.optInt(Article.ARTICLE_PERFECT));

                final JSONObject commenter = userMapper.get(userId);
                comment.put(Comment.COMMENT_T_COMMENTER, commenter);

                final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                final JSONObject articleAuthor = userMapper.get(articleAuthorId);
                final String articleAuthorName = articleAuthor.optString(User.USER_NAME);
                if (Article.ARTICLE_ANONYMOUS_C_PUBLIC == article.optInt(Article.ARTICLE_ANONYMOUS)) {
                    comment.put(Comment.COMMENT_T_ARTICLE_AUTHOR_NAME, articleAuthorName);
                    comment.put(Comment.COMMENT_T_ARTICLE_AUTHOR_URL, "/member/" + articleAuthor.optString(User.USER_NAME));
                    final String articleAuthorThumbnailURL = avatarQueryService.getAvatarURLByUser(
                            avatarViewMode, articleAuthor, "48");
                    comment.put(Comment.COMMENT_T_ARTICLE_AUTHOR_THUMBNAIL_URL, articleAuthorThumbnailURL);
                } else {
                    comment.put(Comment.COMMENT_T_ARTICLE_AUTHOR_NAME, UserExt.ANONYMOUS_USER_NAME);
                    comment.put(Comment.COMMENT_T_ARTICLE_AUTHOR_URL, "");
                    comment.put(Comment.COMMENT_T_ARTICLE_AUTHOR_THUMBNAIL_URL, avatarQueryService.getDefaultAvatarURL("48"));
                }

                final String commentId = comment.optString(Keys.OBJECT_ID);
                final int cmtViewMode = UserExt.USER_COMMENT_VIEW_MODE_C_TRADITIONAL;
                final int cmtPage = getCommentPage(articleId, commentId, cmtViewMode, Symphonys.getInt("articleCommentsPageSize"));
                comment.put(Comment.COMMENT_SHARP_URL, "/article/" + articleId + "?p=" + cmtPage + "&m=" + cmtViewMode + "#" + commentId);

                if (Article.ARTICLE_TYPE_C_DISCUSSION == article.optInt(Article.ARTICLE_TYPE)
                        && Article.ARTICLE_ANONYMOUS_C_PUBLIC == article.optInt(Article.ARTICLE_ANONYMOUS)) {
                    final String msgContent = langPropsService.get("articleDiscussionLabel").
                            replace("{user}", UserExt.getUserLink(articleAuthorName));

                    if (null == viewer) {
                        comment.put(Comment.COMMENT_CONTENT, msgContent);
                    } else {
                        final String commenterName = commenter.optString(User.USER_NAME);
                        final String viewerUserName = viewer.optString(User.USER_NAME);
                        final String viewerRole = viewer.optString(User.USER_ROLE);

                        if (!commenterName.equals(viewerUserName) && !Role.ROLE_ID_C_ADMIN.equals(viewerRole)) {
                            final String articleContent = article.optString(Article.ARTICLE_CONTENT);
                            final Set<String> userNames = userQueryService.getUserNames(articleContent);

                            boolean invited = false;
                            for (final String userName : userNames) {
                                if (userName.equals(viewerUserName)) {
                                    invited = true;

                                    break;
                                }
                            }

                            if (!invited) {
                                comment.put(Comment.COMMENT_CONTENT, msgContent);
                            }
                        }
                    }
                }

                processCommentContent(comment);
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error("Gets user comments failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets the article comments with the specified article id, page number and page size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param articleId      the specified article id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @param sortMode       the specified sort mode (traditional: 0, real time: 1)
     * @return comments, return an empty list if not found
     * @throws Exception service exception
     */
    public List<JSONObject> getArticleComments(final int avatarViewMode,
                                               final String articleId, final int currentPageNum, final int pageSize, final int sortMode)
            throws Exception {
        Stopwatchs.start("Get comments");

        final Query query = new Query()
                .setPageCount(1).setCurrentPageNum(currentPageNum).setPageSize(pageSize)
                .setFilter(new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId));

        if (UserExt.USER_COMMENT_VIEW_MODE_C_REALTIME == sortMode) {
            query.addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
        } else {
            query.addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);
        }

        try {
            Stopwatchs.start("Query comments");
            JSONObject result;
            try {
                result = commentMapper.get(query);
            } finally {
                Stopwatchs.end();
            }
            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            organizeComments(avatarViewMode, ret);

            Stopwatchs.start("RevisionUtil, paging, original");
            try {
                for (final JSONObject comment : ret) {
                    final String commentId = comment.optString(Keys.OBJECT_ID);

                    // Fill revision count
                    comment.put(Comment.COMMENT_REVISION_COUNT,
                            revisionQueryService.count(commentId, Revision.DATA_TYPE_C_COMMENT));

                    final String originalCmtId = comment.optString(Comment.COMMENT_ORIGINAL_COMMENT_ID);
                    if (StringUtils.isBlank(originalCmtId)) {
                        continue;
                    }

                    // Fill page number
                    comment.put(Pagination.PAGINATION_CURRENT_PAGE_NUM,
                            getCommentPage(articleId, originalCmtId, sortMode, pageSize));

                    // Fill original comment
                    final JSONObject originalCmt = commentMapper.get(originalCmtId);
                    organizeComment(avatarViewMode, originalCmt);
                    comment.put(Comment.COMMENT_T_ORIGINAL_AUTHOR_THUMBNAIL_URL,
                            originalCmt.optString(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL));
                }
            } finally {
                Stopwatchs.end();
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error("Gets article [" + articleId + "] comments failed", e);
            throw new Exception(e);
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets comments by the specified request json object.
     *
     * @param avatarViewMode    the specified avatar view mode
     * @param requestJSONObject the specified request json object, for example,
     *                          "paginationCurrentPageNum": 1,
     *                          "paginationPageSize": 20,
     *                          "paginationWindowSize": 10,
     *                          , see {@link Pagination} for more details
     * @param commentFields     the specified article fields to return
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "comments": [{
     *         "oId": "",
     *         "commentContent": "",
     *         "commentCreateTime": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @throws Exception service exception
     * @see Pagination
     */

    public JSONObject getComments(final int avatarViewMode,
                                  final JSONObject requestJSONObject, final Map<String, Class<?>> commentFields) throws Exception {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize)
                .addSort(Comment.COMMENT_CREATE_TIME, SortDirection.DESCENDING);
        for (final Map.Entry<String, Class<?>> commentField : commentFields.entrySet()) {
            query.addProjection(commentField.getKey(), commentField.getValue());
        }

        JSONObject result = null;

        try {
            result = commentMapper.get(query);
        } catch (final Exception e) {
            LOGGER.error("Gets comments failed", e);

            throw new Exception(e);
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> comments = CollectionUtils.<JSONObject>jsonArrayToList(data);

        try {
            for (final JSONObject comment : comments) {
                organizeComment(avatarViewMode, comment);

                final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
                final JSONObject article = articleMapper.get(articleId);

                comment.put(Comment.COMMENT_T_ARTICLE_TITLE,
                        Article.ARTICLE_STATUS_C_INVALID == article.optInt(Article.ARTICLE_STATUS)
                                ? langPropsService.get("articleTitleBlockLabel")
                                : Emotions.convert(article.optString(Article.ARTICLE_TITLE)));
                comment.put(Comment.COMMENT_T_ARTICLE_PERMALINK, article.optString(Article.ARTICLE_PERMALINK));
            }
        } catch (final Exception e) {
            LOGGER.error("Organizes comments failed", e);

            throw new Exception(e);
        }

        ret.put(Comment.COMMENTS, comments);

        return ret;
    }

    /**
     * Organizes the specified comments.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param comments       the specified comments
     * @throws Exception Mapper exception
     * @see #organizeComment(int, Comment)
     */
    private void organizeComments(final int avatarViewMode, final List<Comment> comments) throws Exception {
        Stopwatchs.start("Organizes comments");

        try {
            for (final Comment comment : comments) {
                organizeComment(avatarViewMode, comment);
            }
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Organizes the specified comment.
     * <ul>
     * <li>converts comment create time (long) to date type</li>
     * <li>generates comment author thumbnail URL</li>
     * <li>generates comment author URL</li>
     * <li>generates comment author name</li>
     * <li>generates &#64;username home URL</li>
     * <li>markdowns comment content</li>
     * <li>block comment if need</li>
     * <li>generates emotion images</li>
     * <li>generates time ago text</li>
     * <li>anonymous process</li>
     * </ul>
     *
     * @param avatarViewMode the specified avatar view mode
     * @param comment        the specified comment
     * @throws Exception Mapper exception
     */
    private void organizeComment(final int avatarViewMode, final Comment comment) throws Exception {
        Stopwatchs.start("Organize comment");

        try {
            comment.setTimeAgo(Times.getTimeAgo(comment.getCommentCreateTime(), Locales.getLocale()));
            final Date createDate = new Date(comment.getCommentCreateTime());
            comment.setCommentCreateTime(createDate.getTime());
            comment.setCommentCreateTimeStr(DateFormatUtils.format(createDate, "yyyy-MM-dd HH:mm:ss"));

            final String authorId = comment.getCommentAuthorId();
            final UserExt author = userMapper.get(authorId);

            comment.setCommenter(author);
            if (CommentUtil.COMMENT_ANONYMOUS_C_PUBLIC == comment.getCommentAnonymous()) {
                comment.setCommentAuthorName(author.getUserName());
                comment.setCommentAuthorURL(author.getUserURL());
                JSONObject json = new JSONObject(JsonUtil.objectToJson(author));
                final String thumbnailURL = avatarQueryService.getAvatarURLByUser(avatarViewMode, json, "48");
                comment.setCommentAuthorThumbnailURL(thumbnailURL);
            } else {
                comment.setCommentAuthorName(UserExtUtil.ANONYMOUS_USER_NAME);
                comment.setCommentAuthorURL("");
                comment.setCommentAuthorThumbnailURL(avatarQueryService.getDefaultAvatarURL("48"));
            }

            processCommentContent(comment);
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Processes the specified comment content.
     *
     * <ul>
     * <li>Generates &#64;username home URL</li>
     * <li>Markdowns</li>
     * <li>Blocks comment if need</li>
     * <li>Generates emotion images</li>
     * <li>Generates article link with article id</li>
     * </ul>
     *
     * @param comment the specified comment, for example,
     *                "commentContent": "",
     *                ....,
     *                "commenter": {}
     */
    private void processCommentContent(final Comment comment) {
        final UserExt commenter = (UserExt) comment.getCommenter();

        final boolean sync = StringUtils.isNotBlank(comment.getClientCommentId());
        comment.setFromClient(sync);

        if (CommentUtil.COMMENT_STATUS_C_INVALID == comment.getCommentStatus()
                || UserExtUtil.USER_STATUS_C_INVALID == commenter.getUserStatus()) {
            comment.setCommentContent(langPropsService.get("commentContentBlockLabel"));

            return;
        }

        String commentContent = comment.getCommentContent();

        commentContent = shortLinkQueryService.linkArticle(commentContent);
        commentContent = shortLinkQueryService.linkTag(commentContent);
        commentContent = Emotions.convert(commentContent);
        commentContent = Markdowns.toHTML(commentContent);
        commentContent = Markdowns.clean(commentContent, "");
        commentContent = MP3Players.render(commentContent);
        commentContent = VideoPlayers.render(commentContent);

        if (sync) {
            // "<i class='ft-small'>by 88250</i>"
            String syncCommenterName = StringUtils.substringAfter(commentContent, "<i class=\"ft-small\">by ");
            syncCommenterName = StringUtils.substringBefore(syncCommenterName, "</i>");

            if (UserRegisterValidation.invalidUserName(syncCommenterName)) {
                syncCommenterName = UserExtUtil.ANONYMOUS_USER_NAME;
            }

            commentContent = commentContent.replaceAll("<i class=\"ft-small\">by .*</i>", "");

            comment.setCommentAuthorName(syncCommenterName);
        }

        comment.setCommentContent(commentContent);
    }
}
