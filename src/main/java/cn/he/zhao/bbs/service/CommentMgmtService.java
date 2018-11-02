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

import cn.he.zhao.bbs.entityUtil.*;
import cn.he.zhao.bbs.event.AddCommentEvent;
import cn.he.zhao.bbs.event.EventTypes;
import cn.he.zhao.bbs.event.UpdateCommentEvent;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.entityUtil.my.CollectionUtils;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Common;
import cn.he.zhao.bbs.util.Emotions;
import cn.he.zhao.bbs.util.Ids;
import cn.he.zhao.bbs.util.JsonUtil;
import cn.he.zhao.bbs.util.Symphonys;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEvent;

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
     * RevisionUtil Mapper.
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
     * OptionUtil Mapper.
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
     * NotificationUtil Mapper.
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
     * PointtransferUtil management service.
     */
    @Autowired
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * RewardUtil management service.
     */
    @Autowired
    private RewardMgmtService rewardMgmtService;

    /**
     * RewardUtil query service.
     */
    @Autowired
    private RewardQueryService rewardQueryService;

    /**
     * NotificationUtil management service.
     */
    @Autowired
    private NotificationMgmtService notificationMgmtService;

    /**
     * LivenessUtil management service.
     */
    @Autowired
    private LivenessMgmtService livenessMgmtService;

    /**
     * Accepts a comment specified with the given comment id.
     *
     * @param commentId
     * @throws Exception service exception
     */
    @Transactional
    public void acceptComment(final String commentId) throws Exception {
        try {
            final Comment comment = commentMapper.get(commentId);
            final String articleId = comment.getCommentOnArticleId();
//            final Query query = new Query().setFilter(new PropertyFilter(CommentUtil.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId));
//            final List<JSONObject> comments = CollectionUtils.jsonArrayToList(commentMapper.get(query).optJSONArray(Keys.RESULTS));
            final List<Comment> comments = commentMapper.getByCommentOnArticleId(articleId);
            for (final Comment c : comments) {
                final int offered = c.getCommentQnAOffered();
                if (CommentUtil.COMMENT_QNA_OFFERED_C_YES == offered) {
                    return;
                }
            }

            final String rewardId = Ids.genTimeMillisId();

            final Article article = articleMapper.get(articleId);
            final String articleAuthorId = article.getArticleAuthorId();
            final String commentAuthorId = comment.getCommentAuthorId();
            final int offerPoint = article.getArticleQnAOfferPoint();
            if (CommentUtil.COMMENT_ANONYMOUS_C_PUBLIC == comment.getCommentAnonymous()) {
                final boolean succ = null != pointtransferMgmtService.transfer(articleAuthorId, commentAuthorId,
                        PointtransferUtil.TRANSFER_TYPE_C_QNA_OFFER, offerPoint, rewardId, System.currentTimeMillis());
                if (!succ) {
                    throw new Exception(langPropsService.get("transferFailLabel"));
                }
            }

            comment.setCommentQnAOffered( CommentUtil.COMMENT_QNA_OFFERED_C_YES);
//            final Transaction transaction = commentMapper.beginTransaction();
            commentMapper.update(commentId, comment);
//            transaction.commit();

            final Reward reward = new Reward();
            reward.setOid(rewardId);
            reward.setSenderId(articleAuthorId);
            reward.setDataId( articleId);
            reward.setType(RewardUtil.TYPE_C_ACCEPT_COMMENT);
            rewardMgmtService.addReward(reward);

            final Notification notification = new Notification();
            notification.setUserId(commentAuthorId);
            notification.setDataId(rewardId);
            notificationMgmtService.addCommentAcceptNotification(notification);

            livenessMgmtService.incLiveness(articleAuthorId, LivenessUtil.LIVENESS_ACCEPT_ANSWER);
        } catch (final Exception e) {
            LOGGER.error( "Accepts a comment [id=" + commentId + "] failed", e);

            throw new Exception(langPropsService.get("systemErrLabel"));
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
     * @throws Exception service exception
     */
    public void removeComment(final String commentId) throws Exception {
        Comment comment = null;

        try {
            comment = commentMapper.get(commentId);
        } catch (final Exception e) {
            LOGGER.error( "Gets a comment [id=" + commentId + "] failed", e);
        }

        if (null == comment) {
            return;
        }

        final int replyCnt = comment.getCommentReplyCnt();
        if (replyCnt > 0) {
            throw new Exception(langPropsService.get("removeCommentFoundReplyLabel"));
        }

        final int ups = comment.getCommentGoodCnt();
        final int downs = comment.getCommentBadCnt();
        if (ups > 0 || downs > 0) {
            throw new Exception("removeCommentFoundWatchEtcLabel");
        }

        final int thankCnt = (int) rewardQueryService.rewardedCount(commentId, RewardUtil.TYPE_C_COMMENT);
        if (thankCnt > 0) {
            throw new Exception("removeCommentFoundThankLabel");
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
     * @throws Exception service exception
     */
    public void thankComment(final String commentId, final String senderId) throws Exception {
        try {
            final Comment comment = commentMapper.get(commentId);

            if (null == comment) {
                return;
            }

            if (CommentUtil.COMMENT_STATUS_C_INVALID == comment.getCommentStatus()) {
                return;
            }

            final UserExt sender = userMapper.get(senderId);
            if (null == sender) {
                return;
            }

            if (UserExtUtil.USER_STATUS_C_VALID != sender.getUserStatus()) {
                return;
            }

            final String receiverId = comment.getCommentAuthorId();
            final UserExt receiver = userMapper.get(receiverId);
            if (null == receiver) {
                return;
            }

            if (UserExtUtil.USER_STATUS_C_VALID != receiver.getUserStatus()) {
                return;
            }

            if (receiverId.equals(senderId)) {
                throw new Exception(langPropsService.get("thankSelfLabel"));
            }

            final int rewardPoint = Symphonys.getInt("pointThankComment");

            if (rewardQueryService.isRewarded(senderId, commentId, RewardUtil.TYPE_C_COMMENT)) {
                return;
            }

            final String rewardId = Ids.genTimeMillisId();

            if (CommentUtil.COMMENT_ANONYMOUS_C_PUBLIC == comment.getCommentAnonymous()) {
                final boolean succ = null != pointtransferMgmtService.transfer(senderId, receiverId,
                        PointtransferUtil.TRANSFER_TYPE_C_COMMENT_REWARD, rewardPoint, rewardId, System.currentTimeMillis());

                if (!succ) {
                    throw new Exception(langPropsService.get("transferFailLabel"));
                }
            }

            final Reward reward = new Reward();
            reward.setOid(rewardId);
            reward.setSenderId(senderId);
            reward.setDataId(commentId);
            reward.setType(RewardUtil.TYPE_C_COMMENT);

            rewardMgmtService.addReward(reward);

            final Notification notification = new Notification();
            notification.setUserId( receiverId);
            notification.setDataId(rewardId);

            notificationMgmtService.addCommentThankNotification(notification);

            livenessMgmtService.incLiveness(senderId, LivenessUtil.LIVENESS_THANK);
        } catch (final Exception e) {
            LOGGER.error( "Thanks a comment[id=" + commentId + "] failed", e);

            throw new Exception(e);
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
     * @throws Exception service exception
     */
    @Transactional
    public synchronized String addComment(final JSONObject requestJSONObject) throws Exception {
        final long currentTimeMillis = System.currentTimeMillis();
        final String commentAuthorId = requestJSONObject.optString(CommentUtil.COMMENT_AUTHOR_ID);
        UserExt commenter;
        try {
            commenter = userMapper.get(commentAuthorId);
        } catch (final Exception e) {
            LOGGER.error( "Gets comment author failed", e);

            throw new Exception(langPropsService.get("systemErrLabel"));
        }

        if (null == commenter) {
            LOGGER.error( "Not found user [id=" + commentAuthorId + "]");

            throw new Exception(langPropsService.get("systemErrLabel"));
        }

        final boolean fromClient = requestJSONObject.has(CommentUtil.COMMENT_CLIENT_COMMENT_ID);
        final String articleId = requestJSONObject.optString(CommentUtil.COMMENT_ON_ARTICLE_ID);
        final String ip = requestJSONObject.optString(CommentUtil.COMMENT_IP);
        String ua = requestJSONObject.optString(CommentUtil.COMMENT_UA);
        final int commentAnonymous = requestJSONObject.optInt(CommentUtil.COMMENT_ANONYMOUS);
        final int commentViewMode = requestJSONObject.optInt(UserExtUtil.USER_COMMENT_VIEW_MODE);

        if (currentTimeMillis - commenter.getUserLatestCmtTime() < Symphonys.getLong("minStepCmtTime")
                && !RoleUtil.ROLE_ID_C_ADMIN.equals(commenter.getUserRole())
                && !UserExtUtil.DEFAULT_CMTER_ROLE.equals(commenter.getUserRole())) {
            LOGGER.warn( "Adds comment too frequent [userName={0}]", commenter.getUserName());
            throw new Exception(langPropsService.get("tooFrequentCmtLabel"));
        }

        final String commenterName = commenter.getUserName();

        Article article;
        try {
            // check if admin allow to add comment
            final Option option = optionMapper.get(OptionUtil.ID_C_MISC_ALLOW_ADD_COMMENT);

            if (!"0".equals(option.getOptionValue())) {
                throw new Exception(langPropsService.get("notAllowAddCommentLabel"));
            }

            final int balance = commenter.getUserPoint();

            if (CommentUtil.COMMENT_ANONYMOUS_C_ANONYMOUS == commentAnonymous) {
                final int anonymousPoint = Symphonys.getInt("anonymous.point");
                if (balance < anonymousPoint) {
                    String anonymousEnabelPointLabel = langPropsService.get("anonymousEnabelPointLabel");
                    anonymousEnabelPointLabel
                            = anonymousEnabelPointLabel.replace("${point}", String.valueOf(anonymousPoint));
                    throw new Exception(anonymousEnabelPointLabel);
                }
            }

            article = articleMapper.get(articleId);

            if (!fromClient && !TuringQueryService.ROBOT_NAME.equals(commenterName)) {
                int pointSum = PointtransferUtil.TRANSFER_SUM_C_ADD_COMMENT;

                // Point
                final String articleAuthorId = article.getArticleAuthorId();
                if (articleAuthorId.equals(commentAuthorId)) {
                    pointSum = PointtransferUtil.TRANSFER_SUM_C_ADD_SELF_ARTICLE_COMMENT;
                }

                if (balance - pointSum < 0) {
                    throw new Exception(langPropsService.get("insufficientBalanceLabel"));
                }
            }
        } catch (final Exception e) {
            throw new Exception(e);
        }

        final int articleAnonymous = article.getArticleAnonymous();

//        final Transaction transaction = commentMapper.beginTransaction();

        try {
            article.setArticleCommentCount(article.getArticleCommentCount() + 1);
            article.setArticleLatestCmterName(commenter.getUserName());
            if (CommentUtil.COMMENT_ANONYMOUS_C_ANONYMOUS == commentAnonymous) {
                article.setArticleLatestCmterName( UserExtUtil.ANONYMOUS_USER_NAME);
            }
            article.setArticleLatestCmtTime(currentTimeMillis);

            final String ret = Ids.genTimeMillisId();
            final Comment comment = new Comment();
            comment.setOid( ret);

            String content = requestJSONObject.optString(CommentUtil.COMMENT_CONTENT).
                    replace("_esc_enter_88250_", "<br/>"); // Solo client escape

            comment.setCommentAuthorId( commentAuthorId);
            comment.setCommentOnArticleId(articleId);
            if (fromClient) {
                comment.setClientCommentId( requestJSONObject.optString(CommentUtil.COMMENT_CLIENT_COMMENT_ID));

                // Appends original commenter name
                final String authorName = requestJSONObject.optString(CommentUtil.COMMENT_T_AUTHOR_NAME);
                content += " <i class='ft-small'>by " + authorName + "</i>";
            }

            final String originalCmtId = requestJSONObject.optString(CommentUtil.COMMENT_ORIGINAL_COMMENT_ID);
            comment.setCommentOriginalCommentId(originalCmtId);

            if (StringUtils.isNotBlank(originalCmtId)) {
                final Comment originalCmt = commentMapper.get(originalCmtId);
                final int originalCmtReplyCnt = originalCmt.getCommentReplyCnt();
                originalCmt.setCommentReplyCnt(originalCmtReplyCnt + 1);
                commentMapper.update(originalCmtId, originalCmt);

                notificationMgmtService.makeRead(commentAuthorId, Arrays.asList(originalCmtId));
            }

            content = Emotions.toAliases(content);
            content = content.replaceAll("\\s+$", ""); // https://github.com/b3log/symphony/issues/389
            content += " "; // in case of tailing @user
            content = content.replace(langPropsService.get("uploadingLabel", Locale.SIMPLIFIED_CHINESE), "");
            content = content.replace(langPropsService.get("uploadingLabel", Locale.US), "");

            comment.setCommentContent(content);
            comment.setCommentCreateTime(System.currentTimeMillis());
            comment.setCommentSharpURL( "/article/" + articleId + "#" + ret);
            comment.setCommentStatus(CommentUtil.COMMENT_STATUS_C_VALID);
            comment.setCommentIP(ip);

            if (StringUtils.length(ua) > CommonUtil.MAX_LENGTH_UA) {
                LOGGER.warn( "UA is too long [" + ua + "]");
                ua = StringUtils.substring(ua, 0, CommonUtil.MAX_LENGTH_UA);
            }
            comment.setCommentUA( ua);

            comment.setCommentAnonymous( commentAnonymous);

            final Option cmtCntOption = optionMapper.get(OptionUtil.ID_C_STATISTIC_CMT_COUNT);
            final int cmtCnt = Integer.parseInt(cmtCntOption.getOptionValue());
            cmtCntOption.setOptionValue( String.valueOf(cmtCnt + 1));

            articleMapper.update( article); // Updates article comment count, latest commenter name and time
            optionMapper.update(OptionUtil.ID_C_STATISTIC_CMT_COUNT, cmtCntOption); // Updates global comment count
            // Updates tag comment count and User-Tag relation
            final String tagsString = article.getArticleTags();
            final String[] tagStrings = tagsString.split(",");
            for (int i = 0; i < tagStrings.length; i++) {
                final String tagTitle = tagStrings[i].trim();
                final Tag tag = tagMapper.getByTitle(tagTitle);
                tag.setTagCommentCount(tag.getTagCommentCount() + 1);
                tag.setTagRandomDouble( Math.random());

                tagMapper.update(tag.getOid(), tag);
            }

            // Updates user comment count, latest comment time
            commenter.setUserCommentCount(commenter.getUserCommentCount() + 1);
            commenter.setUserLatestCmtTime(currentTimeMillis);
            userMapper.update(commenter.getOid(), commenter);

            comment.setCommentGoodCnt( 0);
            comment.setCommentBadCnt( 0);
            comment.setCommentScore( 0D);
            comment.setCommentReplyCnt( 0);
            comment.setCommentAudioURL( "");
            comment.setCommentQnAOffered(CommentUtil.COMMENT_QNA_OFFERED_C_NOT);

            // Adds the comment
            final String commentId = commentMapper.add(comment);

            // Updates tag-article relation stat.
            final List<TagArticle> tagArticleRels = tagArticleMapper.getByArticleId(articleId);
            for (final TagArticle tagArticleRel : tagArticleRels) {
                tagArticleRel.setArticleLatestCmtTime(currentTimeMillis);
                tagArticleRel.setArticleCommentCount(article.getArticleCommentCount());

                tagArticleMapper.update(tagArticleRel.getOid(), tagArticleRel);
            }

            // RevisionUtil
            final Revision revision = new Revision();
            revision.setRevisionAuthorId(comment.getCommentAuthorId());

            final JSONObject revisionData = new JSONObject();
            revisionData.put(CommentUtil.COMMENT_CONTENT, content);

            revision.setRevisionData(revisionData.toString());
            revision.setRevisionDataId(commentId);
            revision.setRevisionDataType(RevisionUtil.DATA_TYPE_C_COMMENT);

            revisionMapper.add(revision);

//            transaction.commit();

            if (!fromClient && CommentUtil.COMMENT_ANONYMOUS_C_PUBLIC == commentAnonymous
                    && ArticleUtil.ARTICLE_ANONYMOUS_C_PUBLIC == articleAnonymous
                    && !TuringQueryService.ROBOT_NAME.equals(commenterName)) {
                // Point
                final String articleAuthorId = article.getArticleAuthorId();
                if (articleAuthorId.equals(commentAuthorId)) {
                    pointtransferMgmtService.transfer(commentAuthorId, PointtransferUtil.ID_C_SYS,
                            PointtransferUtil.TRANSFER_TYPE_C_ADD_COMMENT, PointtransferUtil.TRANSFER_SUM_C_ADD_SELF_ARTICLE_COMMENT,
                            commentId, System.currentTimeMillis());
                } else {
                    pointtransferMgmtService.transfer(commentAuthorId, articleAuthorId,
                            PointtransferUtil.TRANSFER_TYPE_C_ADD_COMMENT, PointtransferUtil.TRANSFER_SUM_C_ADD_COMMENT,
                            commentId, System.currentTimeMillis());
                }

                livenessMgmtService.incLiveness(commentAuthorId, LivenessUtil.LIVENESS_COMMENT);
            }

            // Event
            final JSONObject eventData = new JSONObject();
            eventData.put(CommentUtil.COMMENT, comment);
            eventData.put(CommonUtil.FROM_CLIENT, fromClient);
            eventData.put(ArticleUtil.ARTICLE, article);
            eventData.put(UserExtUtil.USER_COMMENT_VIEW_MODE, commentViewMode);

            try {
                eventManager.fireEventAsynchronously(new AddCommentEvent(EventTypes.ADD_COMMENT_TO_ARTICLE, eventData));
            } catch (final Exception e) {
                LOGGER.error( e.getMessage(), e);
            }

            return ret;
        } catch (final Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }

            LOGGER.error( "Adds a comment failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Updates the specified comment by the given comment id.
     *
     * @param commentId the given comment id
     * @param comment   the specified comment
     * @throws Exception service exception
     */
    @Transactional
    public void updateComment(final String commentId, final JSONObject comment) throws Exception {
//        final Transaction transaction = commentMapper.beginTransaction();

        try {
            final Comment oldComment = commentMapper.get(commentId);
            final String oldContent = oldComment.getCommentContent();

            String content = comment.optString(CommentUtil.COMMENT_CONTENT);
            content = Emotions.toAliases(content);
            content = content.replaceAll("\\s+$", ""); // https://github.com/b3log/symphony/issues/389
            content += " "; // in case of tailing @user
            content = content.replace(langPropsService.get("uploadingLabel", Locale.SIMPLIFIED_CHINESE), "");
            content = content.replace(langPropsService.get("uploadingLabel", Locale.US), "");
            comment.put(CommentUtil.COMMENT_CONTENT, content);

            Comment commentTmp = JsonUtil.json2Bean(comment.toString(),Comment.class);
            commentMapper.update(commentId, commentTmp);

            final String commentAuthorId = comment.optString(CommentUtil.COMMENT_AUTHOR_ID);
            if (!oldContent.equals(content)) {
                // RevisionUtil
                final Revision revision = new Revision();
                revision.setRevisionAuthorId(commentAuthorId);

                final JSONObject revisionData = new JSONObject();
                revisionData.put(CommentUtil.COMMENT_CONTENT, content);

                revision.setRevisionData(revisionData.toString());
                revision.setRevisionDataId(commentId);
                revision.setRevisionDataType(RevisionUtil.DATA_TYPE_C_COMMENT);

                revisionMapper.add(revision);
            }

//            transaction.commit();

            final Article article = articleMapper.get(comment.optString(CommentUtil.COMMENT_ON_ARTICLE_ID));
            final int articleAnonymous = article.getArticleAnonymous();
            final int commentAnonymous = comment.optInt(CommentUtil.COMMENT_ANONYMOUS);

            if (CommentUtil.COMMENT_ANONYMOUS_C_PUBLIC == commentAnonymous
                    && ArticleUtil.ARTICLE_ANONYMOUS_C_PUBLIC == articleAnonymous) {
                // Point
                final long now = System.currentTimeMillis();
                final long createTime = comment.optLong(Keys.OBJECT_ID);
                if (now - createTime > 1000 * 60 * 5) {
                    pointtransferMgmtService.transfer(commentAuthorId, PointtransferUtil.ID_C_SYS,
                            PointtransferUtil.TRANSFER_TYPE_C_UPDATE_COMMENT,
                            PointtransferUtil.TRANSFER_SUM_C_UPDATE_COMMENT, commentId, now);
                }
            }

            final boolean fromClient = comment.has(CommentUtil.COMMENT_CLIENT_COMMENT_ID);

            // Event
            final JSONObject eventData = new JSONObject();
            eventData.put(Common.FROM_CLIENT, fromClient);
            eventData.put(ArticleUtil.ARTICLE, article);
            eventData.put(CommentUtil.COMMENT, comment);
            try {
                eventManager.fireEventAsynchronously(new UpdateCommentEvent(EventTypes.UPDATE_COMMENT, eventData));
            } catch (final Exception e) {
                LOGGER.error( e.getMessage(), e);
            }
        } catch (final Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }

            LOGGER.error( "Updates a comment[id=" + commentId + "] failed", e);
            throw new Exception(e);
        }
    }
}
