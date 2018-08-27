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
package cn.he.zhao.bbs.entityUtil;

/**
 * This class defines all comment entity relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.13.0.0, Jun 10, 2018
 * @since 0.2.0
 */
public class CommentUtil {

    //// Transient ////
    /**
     * Key of comment revision count.
     */
    public static final String COMMENT_REVISION_COUNT = "commentRevisionCount";

    /**
     * Key of comment vote.
     */
    public static final String COMMENT_T_VOTE = "commentVote";

    /**
     * Key of commenter.
     */
    public static final String COMMENT_T_COMMENTER = "commenter";

    /**
     * Key of comment author email.
     */
    public static final String COMMENT_T_AUTHOR_EMAIL = "commentAuthorEmail";

    /**
     * Key of comment id.
     */
    public static final String COMMENT_T_ID = "commentId";

    /**
     * Key of comment ids.
     */
    public static final String COMMENT_T_IDS = "commentIds";

    /**
     * Key of comment on symphony article id.
     */
    public static final String COMMENT_T_SYMPHONY_ID = "commentSymphonyArticleId";

    /**
     * Key of comment author thumbnail URL.
     */
    public static final String COMMENT_T_AUTHOR_THUMBNAIL_URL = "commentAuthorThumbnailURL";

    /**
     * Key of comment author name.
     */
    public static final String COMMENT_T_AUTHOR_NAME = "commentAuthorName";

    /**
     * Key of comment author URL.
     */
    public static final String COMMENT_T_AUTHOR_URL = "commentAuthorURL";

    /**
     * Key of comment article title.
     */
    public static final String COMMENT_T_ARTICLE_TITLE = "commentArticleTitle";

    /**
     * Key of comment article type.
     */
    public static final String COMMENT_T_ARTICLE_TYPE = "commentArticleType";

    /**
     * Key of comment article perfect.
     */
    public static final String COMMENT_T_ARTICLE_PERFECT = "commentArticlePerfect";

    /**
     * Key of comment article author name.
     */
    public static final String COMMENT_T_ARTICLE_AUTHOR_NAME = "commentArticleAuthorName";

    /**
     * Key of comment article author URL.
     */
    public static final String COMMENT_T_ARTICLE_AUTHOR_URL = "commentArticleAuthorURL";

    /**
     * Key of comment article author thumbnail URL.
     */
    public static final String COMMENT_T_ARTICLE_AUTHOR_THUMBNAIL_URL = "commentArticleAuthorThumbnailURL";

    /**
     * Key of comment article permalink.
     */
    public static final String COMMENT_T_ARTICLE_PERMALINK = "commentArticlePermalink";

    /**
     * Key of comment thank label.
     */
    public static final String COMMENT_T_THANK_LABEL = "commentThankLabel";

    /**
     * Key of comment nice.
     */
    public static final String COMMENT_T_NICE = "commentNice";

    /**
     * Key of comment replies.
     */
    public static final String COMMENT_T_REPLIES = "commentReplies";

    /**
     * Key of comment original author thumbnail URL.
     */
    public static final String COMMENT_T_ORIGINAL_AUTHOR_THUMBNAIL_URL = "commentOriginalAuthorThumbnailURL";

    //// Status constants
    /**
     * Comment status - valid.
     */
    public static final int COMMENT_STATUS_C_VALID = 0;

    /**
     * Comment status - invalid.
     */
    public static final int COMMENT_STATUS_C_INVALID = 1;

    // Anonymous constants
    /**
     * Comment anonymous - public.
     */
    public static final int COMMENT_ANONYMOUS_C_PUBLIC = 0;

    /**
     * Comment anonymous - anonymous.
     */
    public static final int COMMENT_ANONYMOUS_C_ANONYMOUS = 1;

    // QnA offered constants
    /**
     * Comment offered - not yet.
     */
    public static final int COMMENT_QNA_OFFERED_C_NOT = 0;

    /**
     * Comment offered - yes.
     */
    public static final int COMMENT_QNA_OFFERED_C_YES = 1;

    /**
     * Private constructor.
     */
    private CommentUtil() {
    }
}
