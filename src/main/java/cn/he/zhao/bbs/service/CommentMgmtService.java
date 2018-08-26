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

import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.entity.my.CollectionUtils;
import cn.he.zhao.bbs.entity.my.Keys;
import cn.he.zhao.bbs.entity.my.User;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.util.Emotions;
import cn.he.zhao.bbs.util.Symphonys;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Comment management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.14.0.1, Jun 12, 2017
 * @since 0.2.0
 */
@Service
public class CommentMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentMgmtService.class);

    /**
     * Revision Mapper.
     */
    @Autowired
    private RevisionMapper revisionMapper;

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
     * Option Mapper.
     */
    @Autowired
    private OptionMapper optionMapper;

    /**
     * Tag Mapper.
     */
    @Autowired
    private TagMapper tagMapper;

    /**
     * Tag-Article Mapper.
     */
    @Autowired
    private TagArticleMapper tagArticleMapper;

    /**
     * User Mapper.
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * Notification Mapper.
     */
    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * Event manager.
     */
    @Autowired
    private EventManager eventManager;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Pointtransfer management service.
     */
    @Autowired
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * Reward management service.
     */
    @Autowired
    private RewardMgmtService rewardMgmtService;

    /**
     * Reward query service.
     */
    @Autowired
    private RewardQueryService rewardQueryService;

    /**
     * Notification management service.
     */
    @Autowired
    private NotificationMgmtService notificationMgmtService;

    /**
     * Liveness management service.
     */
    @Autowired
    private LivenessMgmtService livenessMgmtService;

    /**
     * Accepts a comment specified with the given comment id.
     *
     * @param commentId
     * @throws ServiceException service exception
     */
    public void acceptComment(final String commentId) throws ServiceException {
        try {
            final JSONObject comment = commentMapper.get(commentId);
            final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
            final Query query = new Query().setFilter(new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId));
            final List<JSONObject> comments = CollectionUtils.jsonArrayToList(commentMapper.get(query).optJSONArray(Keys.RESULTS));
            for (final JSONObject c : comments) {
                final int offered = c.optInt(Comment.COMMENT_QNA_OFFERED);
                if (Comment.COMMENT_QNA_OFFERED_C_YES == offered) {
                    return;
                }
            }

            final String rewardId = Ids.genTimeMillisId();

            final JSONObject article = articleMapper.get(articleId);
            final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
            final String commentAuthorId = comment.optString(Comment.COMMENT_AUTHOR_ID);
            final int offerPoint = article.optInt(Article.ARTICLE_QNA_OFFER_POINT);
            if (Comment.COMMENT_ANONYMOUS_C_PUBLIC == comment.optInt(Comment.COMMENT_ANONYMOUS)) {
                final boolean succ = null != pointtransferMgmtService.transfer(articleAuthorId, commentAuthorId,
                        Pointtransfer.TRANSFER_TYPE_C_QNA_OFFER, offerPoint, rewardId, System.currentTimeMillis());
                if (!succ) {
                    throw new ServiceException(langPropsService.get("transferFailLabel"));
                }
            }

            comment.put(Comment.COMMENT_QNA_OFFERED, Comment.COMMENT_QNA_OFFERED_C_YES);
            final Transaction transaction = commentMapper.beginTransaction();
            commentMapper.update(commentId, comment);
            transaction.commit();

            final JSONObject reward = new JSONObject();
            reward.put(Keys.OBJECT_ID, rewardId);
            reward.put(Reward.SENDER_ID, articleAuthorId);
            reward.put(Reward.DATA_ID, articleId);
            reward.put(Reward.TYPE, Reward.TYPE_C_ACCEPT_COMMENT);
            rewardMgmtService.addReward(reward);

            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, commentAuthorId);
            notification.put(Notification.NOTIFICATION_DATA_ID, rewardId);
            notificationMgmtService.addCommentAcceptNotification(notification);

            livenessMgmtService.incLiveness(articleAuthorId, Liveness.LIVENESS_ACCEPT_ANSWER);
        } catch ( final Exception e) {
            throw e;
        } catch (final Exception e) {
            LOGGER.error( "Accepts a comment [id=" + commentId + "] failed", e);

            throw new ServiceException(langPropsService.get("systemErrLabel"));
        }
    }

    /**
     * Removes a comment specified with the given comment id. A comment is removable if:
     * <ul>
     * <li>No replies</li>
     * <li>No ups, downs</li>
     * <li>No thanks</li>
     * </ul>
     * Sees https://github.com/b3log/symphony/issues/451 for more details.
     *
     * @param commentId the given commentId id
     * @throws ServiceException service exception
     */
    public void removeComment(final String commentId) throws ServiceException {
        JSONObject comment = null;

        try {
            comment = commentMapper.get(commentId);
        } catch (final Exception e) {
            LOGGER.error( "Gets a comment [id=" + commentId + "] failed", e);
        }

        if (null == comment) {
            return;
        }

        final int replyCnt = comment.optInt(Comment.COMMENT_REPLY_CNT);
        if (replyCnt > 0) {
            throw new ServiceException(langPropsService.get("removeCommentFoundReplyLabel"));
        }

        final int ups = comment.optInt(Comment.COMMENT_GOOD_CNT);
        final int downs = comment.optInt(Comment.COMMENT_BAD_CNT);
        if (ups > 0 || downs > 0) {
            throw new ServiceException("removeCommentFoundWatchEtcLabel");
        }

        final int thankCnt = (int) rewardQueryService.rewardedCount(commentId, Reward.TYPE_C_COMMENT);
        if (thankCnt > 0) {
            throw new ServiceException("removeCommentFoundThankLabel");
        }

        // Perform removal
        removeCommentByAdmin(commentId);
    }

    /**
     * Removes a comment specified with the given comment id. Calls this method will remove all existed data related
     * with the specified comment forcibly.
     *
     * @param commentId the given comment id
     */
    @Transactional
    public void removeCommentByAdmin(final String commentId) {
        try {
            commentMapper.removeComment(commentId);
        } catch (final Exception e) {
            LOGGER.error( "Removes a comment error [id=" + commentId + "]", e);
        }
    }

    /**
     * A user specified by the given sender id thanks the author of a comment specified by the given comment id.
     *
     * @param commentId the given comment id
     * @param senderId  the given sender id
     * @throws ServiceException service exception
     */
    public void thankComment(final String commentId, final String senderId) throws ServiceException {
        try {
            final JSONObject comment = commentMapper.get(commentId);

            if (null == comment) {
                return;
            }

            if (Comment.COMMENT_STATUS_C_INVALID == comment.optInt(Comment.COMMENT_STATUS)) {
                return;
            }

            final JSONObject sender = userMapper.get(senderId);
            if (null == sender) {
                return;
            }

            if (UserExt.USER_STATUS_C_VALID != sender.optInt(UserExt.USER_STATUS)) {
                return;
            }

            final String receiverId = comment.optString(Comment.COMMENT_AUTHOR_ID);
            final JSONObject receiver = userMapper.get(receiverId);
            if (null == receiver) {
                return;
            }

            if (UserExt.USER_STATUS_C_VALID != receiver.optInt(UserExt.USER_STATUS)) {
                return;
            }

            if (receiverId.equals(senderId)) {
                throw new ServiceException(langPropsService.get("thankSelfLabel"));
            }

            final int rewardPoint = Symphonys.getInt("pointThankComment");

            if (rewardQueryService.isRewarded(senderId, commentId, Reward.TYPE_C_COMMENT)) {
                return;
            }

            final String rewardId = Ids.genTimeMillisId();

            if (Comment.COMMENT_ANONYMOUS_C_PUBLIC == comment.optInt(Comment.COMMENT_ANONYMOUS)) {
                final boolean succ = null != pointtransferMgmtService.transfer(senderId, receiverId,
                        Pointtransfer.TRANSFER_TYPE_C_COMMENT_REWARD, rewardPoint, rewardId, System.currentTimeMillis());

                if (!succ) {
                    throw new ServiceException(langPropsService.get("transferFailLabel"));
                }
            }

            final JSONObject reward = new JSONObject();
            reward.put(Keys.OBJECT_ID, rewardId);
            reward.put(Reward.SENDER_ID, senderId);
            reward.put(Reward.DATA_ID, commentId);
            reward.put(Reward.TYPE, Reward.TYPE_C_COMMENT);

            rewardMgmtService.addReward(reward);

            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, receiverId);
            notification.put(Notification.NOTIFICATION_DATA_ID, rewardId);

            notificationMgmtService.addCommentThankNotification(notification);

            livenessMgmtService.incLiveness(senderId, Liveness.LIVENESS_THANK);
        } catch (final MapperException e) {
            LOGGER.error( "Thanks a comment[id=" + commentId + "] failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Adds a comment with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          "commentContent": "",
     *                          "commentAuthorId": "",
     *                          "commentOnArticleId": "",
     *                          "commentOriginalCommentId": "", // optional
     *                          "clientCommentId": "" // optional,
     *                          "commentAuthorName": "" // If from client
     *                          "commentIP": "", // optional, default to ""
     *                          "commentUA": "", // optional, default to ""
     *                          "commentAnonymous": int, // optional, default to 0 (public)
     *                          "userCommentViewMode": int
     *                          , see {@link Comment} for more details
     * @return generated comment id
     * @throws ServiceException service exception
     */
    public synchronized String addComment(final JSONObject requestJSONObject) throws ServiceException {
        final long currentTimeMillis = System.currentTimeMillis();
        final String commentAuthorId = requestJSONObject.optString(Comment.COMMENT_AUTHOR_ID);
        JSONObject commenter;
        try {
            commenter = userMapper.get(commentAuthorId);
        } catch (final Exception e) {
            LOGGER.error( "Gets comment author failed", e);

            throw new ServiceException(langPropsService.get("systemErrLabel"));
        }

        if (null == commenter) {
            LOGGER.error( "Not found user [id=" + commentAuthorId + "]");

            throw new ServiceException(langPropsService.get("systemErrLabel"));
        }

        final boolean fromClient = requestJSONObject.has(Comment.COMMENT_CLIENT_COMMENT_ID);
        final String articleId = requestJSONObject.optString(Comment.COMMENT_ON_ARTICLE_ID);
        final String ip = requestJSONObject.optString(Comment.COMMENT_IP);
        String ua = requestJSONObject.optString(Comment.COMMENT_UA);
        final int commentAnonymous = requestJSONObject.optInt(Comment.COMMENT_ANONYMOUS);
        final int commentViewMode = requestJSONObject.optInt(UserExt.USER_COMMENT_VIEW_MODE);

        if (currentTimeMillis - commenter.optLong(UserExt.USER_LATEST_CMT_TIME) < Symphonys.getLong("minStepCmtTime")
                && !Role.ROLE_ID_C_ADMIN.equals(commenter.optString(User.USER_ROLE))
                && !UserExt.DEFAULT_CMTER_ROLE.equals(commenter.optString(User.USER_ROLE))) {
            LOGGER.warn( "Adds comment too frequent [userName={0}]", commenter.optString(User.USER_NAME));
            throw new ServiceException(langPropsService.get("tooFrequentCmtLabel"));
        }

        final String commenterName = commenter.optString(User.USER_NAME);

        JSONObject article;
        try {
            // check if admin allow to add comment
            final JSONObject option = optionMapper.get(Option.ID_C_MISC_ALLOW_ADD_COMMENT);

            if (!"0".equals(option.optString(Option.OPTION_VALUE))) {
                throw new ServiceException(langPropsService.get("notAllowAddCommentLabel"));
            }

            final int balance = commenter.optInt(UserExt.USER_POINT);

            if (Comment.COMMENT_ANONYMOUS_C_ANONYMOUS == commentAnonymous) {
                final int anonymousPoint = Symphonys.getInt("anonymous.point");
                if (balance < anonymousPoint) {
                    String anonymousEnabelPointLabel = langPropsService.get("anonymousEnabelPointLabel");
                    anonymousEnabelPointLabel
                            = anonymousEnabelPointLabel.replace("${point}", String.valueOf(anonymousPoint));
                    throw new ServiceException(anonymousEnabelPointLabel);
                }
            }

            article = articleMapper.get(articleId);

            if (!fromClient && !TuringQueryService.ROBOT_NAME.equals(commenterName)) {
                int pointSum = Pointtransfer.TRANSFER_SUM_C_ADD_COMMENT;

                // Point
                final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                if (articleAuthorId.equals(commentAuthorId)) {
                    pointSum = Pointtransfer.TRANSFER_SUM_C_ADD_SELF_ARTICLE_COMMENT;
                }

                if (balance - pointSum < 0) {
                    throw new ServiceException(langPropsService.get("insufficientBalanceLabel"));
                }
            }
        } catch (final MapperException e) {
            throw new ServiceException(e);
        }

        final int articleAnonymous = article.optInt(Article.ARTICLE_ANONYMOUS);

        final Transaction transaction = commentMapper.beginTransaction();

        try {
            article.put(Article.ARTICLE_COMMENT_CNT, article.optInt(Article.ARTICLE_COMMENT_CNT) + 1);
            article.put(Article.ARTICLE_LATEST_CMTER_NAME, commenter.optString(User.USER_NAME));
            if (Comment.COMMENT_ANONYMOUS_C_ANONYMOUS == commentAnonymous) {
                article.put(Article.ARTICLE_LATEST_CMTER_NAME, UserExt.ANONYMOUS_USER_NAME);
            }
            article.put(Article.ARTICLE_LATEST_CMT_TIME, currentTimeMillis);

            final String ret = Ids.genTimeMillisId();
            final JSONObject comment = new JSONObject();
            comment.put(Keys.OBJECT_ID, ret);

            String content = requestJSONObject.optString(Comment.COMMENT_CONTENT).
                    replace("_esc_enter_88250_", "<br/>"); // Solo client escape

            comment.put(Comment.COMMENT_AUTHOR_ID, commentAuthorId);
            comment.put(Comment.COMMENT_ON_ARTICLE_ID, articleId);
            if (fromClient) {
                comment.put(Comment.COMMENT_CLIENT_COMMENT_ID, requestJSONObject.optString(Comment.COMMENT_CLIENT_COMMENT_ID));

                // Appends original commenter name
                final String authorName = requestJSONObject.optString(Comment.COMMENT_T_AUTHOR_NAME);
                content += " <i class='ft-small'>by " + authorName + "</i>";
            }

            final String originalCmtId = requestJSONObject.optString(Comment.COMMENT_ORIGINAL_COMMENT_ID);
            comment.put(Comment.COMMENT_ORIGINAL_COMMENT_ID, originalCmtId);

            if (StringUtils.isNotBlank(originalCmtId)) {
                final JSONObject originalCmt = commentMapper.get(originalCmtId);
                final int originalCmtReplyCnt = originalCmt.optInt(Comment.COMMENT_REPLY_CNT);
                originalCmt.put(Comment.COMMENT_REPLY_CNT, originalCmtReplyCnt + 1);
                commentMapper.update(originalCmtId, originalCmt);

                notificationMgmtService.makeRead(commentAuthorId, Arrays.asList(originalCmtId));
            }

            content = Emotions.toAliases(content);
            content = content.replaceAll("\\s+$", ""); // https://github.com/b3log/symphony/issues/389
            content += " "; // in case of tailing @user
            content = content.replace(langPropsService.get("uploadingLabel", Locale.SIMPLIFIED_CHINESE), "");
            content = content.replace(langPropsService.get("uploadingLabel", Locale.US), "");

            comment.put(Comment.COMMENT_CONTENT, content);
            comment.put(Comment.COMMENT_CREATE_TIME, System.currentTimeMillis());
            comment.put(Comment.COMMENT_SHARP_URL, "/article/" + articleId + "#" + ret);
            comment.put(Comment.COMMENT_STATUS, Comment.COMMENT_STATUS_C_VALID);
            comment.put(Comment.COMMENT_IP, ip);

            if (StringUtils.length(ua) > Common.MAX_LENGTH_UA) {
                LOGGER.warn( "UA is too long [" + ua + "]");
                ua = StringUtils.substring(ua, 0, Common.MAX_LENGTH_UA);
            }
            comment.put(Comment.COMMENT_UA, ua);

            comment.put(Comment.COMMENT_ANONYMOUS, commentAnonymous);

            final JSONObject cmtCntOption = optionMapper.get(Option.ID_C_STATISTIC_CMT_COUNT);
            final int cmtCnt = cmtCntOption.optInt(Option.OPTION_VALUE);
            cmtCntOption.put(Option.OPTION_VALUE, String.valueOf(cmtCnt + 1));

            articleMapper.update(articleId, article); // Updates article comment count, latest commenter name and time
            optionMapper.update(Option.ID_C_STATISTIC_CMT_COUNT, cmtCntOption); // Updates global comment count
            // Updates tag comment count and User-Tag relation
            final String tagsString = article.optString(Article.ARTICLE_TAGS);
            final String[] tagStrings = tagsString.split(",");
            for (int i = 0; i < tagStrings.length; i++) {
                final String tagTitle = tagStrings[i].trim();
                final JSONObject tag = tagMapper.getByTitle(tagTitle);
                tag.put(Tag.TAG_COMMENT_CNT, tag.optInt(Tag.TAG_COMMENT_CNT) + 1);
                tag.put(Tag.TAG_RANDOM_DOUBLE, Math.random());

                tagMapper.update(tag.optString(Keys.OBJECT_ID), tag);
            }

            // Updates user comment count, latest comment time
            commenter.put(UserExt.USER_COMMENT_COUNT, commenter.optInt(UserExt.USER_COMMENT_COUNT) + 1);
            commenter.put(UserExt.USER_LATEST_CMT_TIME, currentTimeMillis);
            userMapper.update(commenter.optString(Keys.OBJECT_ID), commenter);

            comment.put(Comment.COMMENT_GOOD_CNT, 0);
            comment.put(Comment.COMMENT_BAD_CNT, 0);
            comment.put(Comment.COMMENT_SCORE, 0D);
            comment.put(Comment.COMMENT_REPLY_CNT, 0);
            comment.put(Comment.COMMENT_AUDIO_URL, "");
            comment.put(Comment.COMMENT_QNA_OFFERED, Comment.COMMENT_QNA_OFFERED_C_NOT);

            // Adds the comment
            final String commentId = commentMapper.add(comment);

            // Updates tag-article relation stat.
            final List<JSONObject> tagArticleRels = tagArticleMapper.getByArticleId(articleId);
            for (final JSONObject tagArticleRel : tagArticleRels) {
                tagArticleRel.put(Article.ARTICLE_LATEST_CMT_TIME, currentTimeMillis);
                tagArticleRel.put(Article.ARTICLE_COMMENT_CNT, article.optInt(Article.ARTICLE_COMMENT_CNT));

                tagArticleMapper.update(tagArticleRel.optString(Keys.OBJECT_ID), tagArticleRel);
            }

            // Revision
            final JSONObject revision = new JSONObject();
            revision.put(Revision.REVISION_AUTHOR_ID, comment.optString(Comment.COMMENT_AUTHOR_ID));

            final JSONObject revisionData = new JSONObject();
            revisionData.put(Comment.COMMENT_CONTENT, content);

            revision.put(Revision.REVISION_DATA, revisionData.toString());
            revision.put(Revision.REVISION_DATA_ID, commentId);
            revision.put(Revision.REVISION_DATA_TYPE, Revision.DATA_TYPE_C_COMMENT);

            revisionMapper.add(revision);

            transaction.commit();

            if (!fromClient && Comment.COMMENT_ANONYMOUS_C_PUBLIC == commentAnonymous
                    && Article.ARTICLE_ANONYMOUS_C_PUBLIC == articleAnonymous
                    && !TuringQueryService.ROBOT_NAME.equals(commenterName)) {
                // Point
                final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                if (articleAuthorId.equals(commentAuthorId)) {
                    pointtransferMgmtService.transfer(commentAuthorId, Pointtransfer.ID_C_SYS,
                            Pointtransfer.TRANSFER_TYPE_C_ADD_COMMENT, Pointtransfer.TRANSFER_SUM_C_ADD_SELF_ARTICLE_COMMENT,
                            commentId, System.currentTimeMillis());
                } else {
                    pointtransferMgmtService.transfer(commentAuthorId, articleAuthorId,
                            Pointtransfer.TRANSFER_TYPE_C_ADD_COMMENT, Pointtransfer.TRANSFER_SUM_C_ADD_COMMENT,
                            commentId, System.currentTimeMillis());
                }

                livenessMgmtService.incLiveness(commentAuthorId, Liveness.LIVENESS_COMMENT);
            }

            // Event
            final JSONObject eventData = new JSONObject();
            eventData.put(Comment.COMMENT, comment);
            eventData.put(Common.FROM_CLIENT, fromClient);
            eventData.put(Article.ARTICLE, article);
            eventData.put(UserExt.USER_COMMENT_VIEW_MODE, commentViewMode);

            try {
                eventManager.fireEventAsynchronously(new Event<JSONObject>(EventTypes.ADD_COMMENT_TO_ARTICLE, eventData));
            } catch (final EventException e) {
                LOGGER.error( e.getMessage(), e);
            }

            return ret;
        } catch (final MapperException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.error( "Adds a comment failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Updates the specified comment by the given comment id.
     *
     * @param commentId the given comment id
     * @param comment   the specified comment
     * @throws ServiceException service exception
     */
    public void updateComment(final String commentId, final JSONObject comment) throws ServiceException {
        final Transaction transaction = commentMapper.beginTransaction();

        try {
            final JSONObject oldComment = commentMapper.get(commentId);
            final String oldContent = oldComment.optString(Comment.COMMENT_CONTENT);

            String content = comment.optString(Comment.COMMENT_CONTENT);
            content = Emotions.toAliases(content);
            content = content.replaceAll("\\s+$", ""); // https://github.com/b3log/symphony/issues/389
            content += " "; // in case of tailing @user
            content = content.replace(langPropsService.get("uploadingLabel", Locale.SIMPLIFIED_CHINESE), "");
            content = content.replace(langPropsService.get("uploadingLabel", Locale.US), "");
            comment.put(Comment.COMMENT_CONTENT, content);

            commentMapper.update(commentId, comment);

            final String commentAuthorId = comment.optString(Comment.COMMENT_AUTHOR_ID);
            if (!oldContent.equals(content)) {
                // Revision
                final JSONObject revision = new JSONObject();
                revision.put(Revision.REVISION_AUTHOR_ID, commentAuthorId);

                final JSONObject revisionData = new JSONObject();
                revisionData.put(Comment.COMMENT_CONTENT, content);

                revision.put(Revision.REVISION_DATA, revisionData.toString());
                revision.put(Revision.REVISION_DATA_ID, commentId);
                revision.put(Revision.REVISION_DATA_TYPE, Revision.DATA_TYPE_C_COMMENT);

                revisionMapper.add(revision);
            }

            transaction.commit();

            final JSONObject article = articleMapper.get(comment.optString(Comment.COMMENT_ON_ARTICLE_ID));
            final int articleAnonymous = article.optInt(Article.ARTICLE_ANONYMOUS);
            final int commentAnonymous = comment.optInt(Comment.COMMENT_ANONYMOUS);

            if (Comment.COMMENT_ANONYMOUS_C_PUBLIC == commentAnonymous
                    && Article.ARTICLE_ANONYMOUS_C_PUBLIC == articleAnonymous) {
                // Point
                final long now = System.currentTimeMillis();
                final long createTime = comment.optLong(Keys.OBJECT_ID);
                if (now - createTime > 1000 * 60 * 5) {
                    pointtransferMgmtService.transfer(commentAuthorId, Pointtransfer.ID_C_SYS,
                            Pointtransfer.TRANSFER_TYPE_C_UPDATE_COMMENT,
                            Pointtransfer.TRANSFER_SUM_C_UPDATE_COMMENT, commentId, now);
                }
            }

            final boolean fromClient = comment.has(Comment.COMMENT_CLIENT_COMMENT_ID);

            // Event
            final JSONObject eventData = new JSONObject();
            eventData.put(Common.FROM_CLIENT, fromClient);
            eventData.put(Article.ARTICLE, article);
            eventData.put(Comment.COMMENT, comment);
            try {
                eventManager.fireEventAsynchronously(new Event<>(EventTypes.UPDATE_COMMENT, eventData));
            } catch (final EventException e) {
                LOGGER.error( e.getMessage(), e);
            }
        } catch (final MapperException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.error( "Updates a comment[id=" + commentId + "] failed", e);
            throw new ServiceException(e);
        }
    }
}
