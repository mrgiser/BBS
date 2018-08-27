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
 * This class defines all article entity relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.32.0.0, Jun 19, 2018
 * @since 0.2.0
 */
public class ArticleUtil {

    //// Transient ////
    /**
     * Key of article revision count.
     */
    public static final String ARTICLE_REVISION_COUNT = "articleRevisionCount";

    /**
     * Key of article latest comment.
     */
    public static final String ARTICLE_T_LATEST_CMT = "articleLatestCmt";

    /**
     * Key of previous article.
     */
    public static final String ARTICLE_T_PREVIOUS = "articlePrevious";

    /**
     * Key of next article.
     */
    public static final String ARTICLE_T_NEXT = "articleNext";

    /**
     * Key of article tag objects.
     */
    public static final String ARTICLE_T_TAG_OBJS = "articleTagObjs";

    /**
     * Key of article vote.
     */
    public static final String ARTICLE_T_VOTE = "articleVote";

    /**
     * Key of article stick flag.
     */
    public static final String ARTICLE_T_IS_STICK = "articleIsStick";

    /**
     * Key of article stick remains.
     */
    public static final String ARTICLE_T_STICK_REMAINS = "articleStickRemains";

    /**
     * Key of article preview content.
     */
    public static final String ARTICLE_T_PREVIEW_CONTENT = "articlePreviewContent";

    /**
     * Key of article thumbnail URL.
     */
    public static final String ARTICLE_T_THUMBNAIL_URL = "articleThumbnailURL";

    /**
     * Key of article view count display format.
     */
    public static final String ARTICLE_T_VIEW_CNT_DISPLAY_FORMAT = "articleViewCntDisplayFormat";

    /**
     * Key of article id.
     */
    public static final String ARTICLE_T_ID = "articleId";

    /**
     * Key of article ids.
     */
    public static final String ARTICLE_T_IDS = "articleIds";

    /**
     * Key of article author.
     */
    public static final String ARTICLE_T_AUTHOR = "articleAuthor";

    /**
     * Key of article author thumbnail URL.
     */
    public static final String ARTICLE_T_AUTHOR_THUMBNAIL_URL = "articleAuthorThumbnailURL";

    /**
     * Key of article author name.
     */
    public static final String ARTICLE_T_AUTHOR_NAME = "articleAuthorName";

    /**
     * Key of article author URL.
     */
    public static final String ARTICLE_T_AUTHOR_URL = "articleAuthorURL";

    /**
     * Key of article author intro.
     */
    public static final String ARTICLE_T_AUTHOR_INTRO = "articleAuthorIntro";

    /**
     * Key of article comments.
     */
    public static final String ARTICLE_T_COMMENTS = "articleComments";

    /**
     * Key of article nice comments.
     */
    public static final String ARTICLE_T_NICE_COMMENTS = "articleNiceComments";

    /**
     * Key of article offered (accepted) comment(answer).
     */
    public static final String ARTICLE_T_OFFERED_COMMENT = "articleOfferedComment";

    /**
     * Key of article participants.
     */
    public static final String ARTICLE_T_PARTICIPANTS = "articleParticipants";

    /**
     * Key of article participant name.
     */
    public static final String ARTICLE_T_PARTICIPANT_NAME = "articleParticipantName";

    /**
     * Key of article participant thumbnail URL.
     */
    public static final String ARTICLE_T_PARTICIPANT_THUMBNAIL_URL = "articleParticipantThumbnailURL";

    /**
     * Key of article participant thumbnail update time.
     */
    public static final String ARTICLE_T_PARTICIPANT_THUMBNAIL_UPDATE_TIME = "articleParticipantThumbnailUpdateTime";

    /**
     * Key of article participant URL.
     */
    public static final String ARTICLE_T_PARTICIPANT_URL = "articleParticipantURL";

    /**
     * Key of article title with Emoj.
     */
    public static final String ARTICLE_T_TITLE_EMOJI = "articleTitleEmoj";

    /**
     * Key of article title with Emoji unicode.
     */
    public static final String ARTICLE_T_TITLE_EMOJI_UNICODE = "articleTitleEmojUnicode";

    /**
     * Key of article heat.
     */
    public static final String ARTICLE_T_HEAT = "articleHeat";

    /**
     * Key of article ToC.
     */
    public static final String ARTICLE_T_TOC = "articleToC";

    /**
     * Key of article original content.
     */
    public static final String ARTICLE_T_ORIGINAL_CONTENT = "articleOriginalContent";

    // Anonymous constants
    /**
     * Article anonymous - public.
     */
    public static final int ARTICLE_ANONYMOUS_C_PUBLIC = 0;

    /**
     * Article anonymous - anonymous.
     */
    public static final int ARTICLE_ANONYMOUS_C_ANONYMOUS = 1;

    // Perfect constants
    /**
     * Article perfect - not perfect.
     */
    public static final int ARTICLE_PERFECT_C_NOT_PERFECT = 0;

    /**
     * Article perfect - perfect.
     */
    public static final int ARTICLE_PERFECT_C_PERFECT = 1;

    // Anonymous view constants
    /**
     * Article anonymous view - use global.
     */
    public static final int ARTICLE_ANONYMOUS_VIEW_C_USE_GLOBAL = 0;

    /**
     * Article anonymous view - not allow.
     */
    public static final int ARTICLE_ANONYMOUS_VIEW_C_NOT_ALLOW = 1;

    /**
     * Article anonymous view - allow.
     */
    public static final int ARTICLE_ANONYMOUS_VIEW_C_ALLOW = 2;

    // Status constants
    /**
     * Article status - valid.
     */
    public static final int ARTICLE_STATUS_C_VALID = 0;

    /**
     * Article status - invalid.
     */
    public static final int ARTICLE_STATUS_C_INVALID = 1;

    /**
     * Article status - locked.
     */
    public static final int ARTICLE_STATUS_C_LOCKED = 2;

    // Type constants
    /**
     * Article type - normal.
     */
    public static final int ARTICLE_TYPE_C_NORMAL = 0;

    /**
     * Article type - discussion.
     */
    public static final int ARTICLE_TYPE_C_DISCUSSION = 1;

    /**
     * Article type - city broadcast.
     */
    public static final int ARTICLE_TYPE_C_CITY_BROADCAST = 2;

    /**
     * Article type - <a href="https://hacpai.com/article/1441942422856">thought</a>.
     */
    public static final int ARTICLE_TYPE_C_THOUGHT = 3;

    /**
     * Article type - <a href="https://github.com/b3log/symphony/issues/486">QnA</a>.
     */
    public static final int ARTICLE_TYPE_C_QNA = 5;

    /**
     * Private constructor.
     */
    private ArticleUtil() {
    }

    /**
     * Checks the specified article type is whether invalid.
     *
     * @param articleType the specified article type
     * @return {@code true} if it is invalid, otherwise returns {@code false}
     */
    public static boolean isInvalidArticleType(final int articleType) {
        return articleType < 0 || articleType > ArticleUtil.ARTICLE_TYPE_C_QNA;
    }
}
