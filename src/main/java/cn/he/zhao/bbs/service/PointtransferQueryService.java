package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entityUtil.PointtransferUtil;
import cn.he.zhao.bbs.entityUtil.UserExtUtil;
import cn.he.zhao.bbs.entityUtil.my.CollectionUtils;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.Pagination;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Common;
import cn.he.zhao.bbs.util.Emotions;
import cn.he.zhao.bbs.util.JsonUtil;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PointtransferQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PointtransferQueryService.class);

    /**
     * PointtransferUtil Mapper.
     */
    @Autowired
    private PointtransferMapper pointtransferMapper;

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
     * Comment Mapper.
     */
    @Autowired
    private CommentMapper commentMapper;

    /**
     * RewardUtil Mapper.
     */
    @Autowired
    private RewardMapper rewardMapper;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Avatar query service.
     */
    @Autowired
    private AvatarQueryService avatarQueryService;

    /**
     * Gets the latest pointtransfers with the specified user id, type and fetch size.
     *
     * @param userId    the specified user id
     * @param type      the specified type
     * @param fetchSize the specified fetch size
     * @return pointtransfers, returns an empty list if not found
     */
    public List<Pointtransfer> getLatestPointtransfers(final String userId, final int type, final int fetchSize) {
        final List<Pointtransfer> ret = new ArrayList<>();

//        final List<Filter> userFilters = new ArrayList<>();
//        userFilters.add(new PropertyFilter(PointtransferUtil.FROM_ID, FilterOperator.EQUAL, userId));
//        userFilters.add(new PropertyFilter(PointtransferUtil.TO_ID, FilterOperator.EQUAL, userId));

//        final List<Filter> filters = new ArrayList<>();
//        filters.add(new CompositeFilter(CompositeFilterOperator.OR, userFilters));
//        filters.add(new PropertyFilter(PointtransferUtil.TYPE, FilterOperator.EQUAL, type));

        PageHelper.startPage(1, fetchSize, "oId desc");
//        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).setCurrentPageNum(1)
//                .setPageSize(fetchSize).setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        try {
            final List<Pointtransfer> result = pointtransferMapper.getByUserIdAndType(userId, type);

            return result;
//            return CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));
        } catch (final Exception e) {
            LOGGER.error( "Gets latest pointtransfers error", e);
        }

        return ret;
    }

    /**
     * Gets the top balance users with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param fetchSize      the specified fetch size
     * @return users, returns an empty list if not found
     */
    public List<JSONObject> getTopBalanceUsers(final int avatarViewMode, final int fetchSize) {
        final List<JSONObject> ret = new ArrayList<>();

//        final Query query = new Query().addSort(UserExtUtil.USER_POINT, SortDirection.DESCENDING).setCurrentPageNum(1)
//                .setPageSize(fetchSize).
//                        setFilter(new PropertyFilter(UserExtUtil.USER_JOIN_POINT_RANK,
//                                FilterOperator.EQUAL, UserExtUtil.USER_JOIN_POINT_RANK_C_JOIN));

        PageHelper.startPage(1, fetchSize, "userPoint desc");

        final int moneyUnit = Symphonys.getInt("pointExchangeUnit");
        try {
            final List<UserExt> users = userMapper.getByUserJoinPointRank(UserExtUtil.USER_JOIN_POINT_RANK_C_JOIN);
//            final List<JSONObject> users = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            for (final UserExt user : users) {

                if (UserExtUtil.USER_APP_ROLE_C_HACKER == user.getUserAppRole()) {
                    user.setUserPointHex(Integer.toHexString(user.getUserPoint()));
                } else {
                    user.setUserPointCC( UserExtUtil.toCCString(user.getUserPoint()));
                }
                JSONObject object = new JSONObject(JsonUtil.objectToJson(user));
                object.put(Common.MONEY, (int) Math.floor(user.getUserPoint() / moneyUnit));

                avatarQueryService.fillUserAvatarURL(avatarViewMode, object);

                ret.add(object);
            }
        } catch (final Exception e) {
            LOGGER.error( "Gets top balance users error", e);
        }

        return ret;
    }

    /**
     * Gets the top consumption users with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param fetchSize      the specified fetch size
     * @return users, returns an empty list if not found
     */
    public List<JSONObject> getTopConsumptionUsers(final int avatarViewMode, final int fetchSize) {
        final List<JSONObject> ret = new ArrayList<>();

//        final Query query = new Query().addSort(UserExtUtil.USER_USED_POINT, SortDirection.DESCENDING).setCurrentPageNum(1)
//                .setPageSize(fetchSize).
//                        setFilter(new PropertyFilter(UserExtUtil.USER_JOIN_USED_POINT_RANK,
//                                FilterOperator.EQUAL, UserExtUtil.USER_JOIN_USED_POINT_RANK_C_JOIN));

        PageHelper.startPage(1, fetchSize, "userUsedPoint desc");

        final int moneyUnit = Symphonys.getInt("pointExchangeUnit");
        try {
            final List<UserExt> users = userMapper.getByUserJoinUsedPointRank(UserExtUtil.USER_JOIN_USED_POINT_RANK_C_JOIN);
//            final List<JSONObject> users = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            for (final UserExt user : users) {
                if (UserExtUtil.USER_APP_ROLE_C_HACKER == user.getUserAppRole()) {
                    user.setUserPointHex( Integer.toHexString(user.getUserPoint()));
                } else {
                    user.setUserPointCC( UserExtUtil.toCCString(user.getUserPoint()));
                }
                JSONObject object = new JSONObject(JsonUtil.objectToJson(user));
                object.put(Common.MONEY, (int) Math.floor(user.getUserPoint() / moneyUnit));

                avatarQueryService.fillUserAvatarURL(avatarViewMode, object);

                ret.add(object);
            }
        } catch (final Exception e) {
            LOGGER.error( "Gets top consumption users error", e);
        }

        return ret;
    }

    /**
     * Gets the user points with the specified user id, page number and page size.
     *
     * @param userId         the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         PointtransferUtil
     *     }, ....]
     * }
     * </pre>
     * @throws Exception service exception
     */
    public JSONObject getUserPoints(final String userId, final int currentPageNum, final int pageSize) throws Exception {
//        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
//                .setCurrentPageNum(currentPageNum).setPageSize(pageSize);

        PageHelper.startPage(1, pageSize, "oId desc");
//        final List<Filter> filters = new ArrayList<>();
//        filters.add(new PropertyFilter(PointtransferUtil.FROM_ID, FilterOperator.EQUAL, userId));
//        filters.add(new PropertyFilter(PointtransferUtil.TO_ID, FilterOperator.EQUAL, userId));
//        query.setFilter(new CompositeFilter(CompositeFilterOperator.OR, filters));

        try {
            final List<Pointtransfer> records = pointtransferMapper.getByUserId(userId);
//            final JSONArray records = ret.optJSONArray(Keys.RESULTS);

            for (int i = 0; i < records.size(); i++) {
                final Pointtransfer record = records.get(i);

                record.put(Common.CREATE_TIME, new Date(record.getTime(Pointtransfer.TIME)));

                final String toId = record.getToId();
                final String fromId = record.getFromId();

                String typeStr = record.getType().toString();
                if (("3".equals(typeStr) && userId.equals(toId))
                        || ("5".equals(typeStr) && userId.equals(fromId))
                        || ("9".equals(typeStr) && userId.equals(toId))
                        || ("14".equals(typeStr) && userId.equals(toId))
                        || ("22".equals(typeStr) && userId.equals(toId))
                        || ("34".equals(typeStr) && userId.equals(toId))) {
                    typeStr += "In";
                }

                if (fromId.equals(userId)) {
                    record.put(Common.BALANCE, record.optInt(Pointtransfer.FROM_BALANCE));
                    record.put(Common.OPERATION, "-");
                } else {
                    record.put(Common.BALANCE, record.optInt(Pointtransfer.TO_BALANCE));
                    record.put(Common.OPERATION, "+");
                }

                record.put(Common.DISPLAY_TYPE, langPropsService.get("pointType" + typeStr + "Label"));

                final int type = record.optInt(Pointtransfer.TYPE);
                final String dataId = record.optString(Pointtransfer.DATA_ID);
                String desTemplate = langPropsService.get("pointType" + typeStr + "DesLabel");

                switch (type) {
                    case Pointtransfer.TRANSFER_TYPE_C_DATA_EXPORT:
                        desTemplate = desTemplate.replace("{num}", record.optString(Pointtransfer.DATA_ID));

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_INIT:
                        desTemplate = desTemplate.replace("{point}", record.optString(Pointtransfer.SUM));

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ADD_ARTICLE:
                        final JSONObject addArticle = articleMapper.get(dataId);
                        if (null == addArticle) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String addArticleLink = "<a href=\""
                                + addArticle.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + addArticle.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", addArticleLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_UPDATE_ARTICLE:
                        final JSONObject updateArticle = articleMapper.get(dataId);
                        if (null == updateArticle) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String updateArticleLink = "<a href=\""
                                + updateArticle.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + updateArticle.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", updateArticleLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ADD_COMMENT:
                        final JSONObject comment = commentMapper.get(dataId);

                        if (null == comment) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
                        final JSONObject commentArticle = articleMapper.get(articleId);

                        final String commentArticleLink = "<a href=\""
                                + commentArticle.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + commentArticle.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", commentArticleLink);

                        if ("3In".equals(typeStr)) {
                            final JSONObject commenter = userMapper.get(fromId);
                            final String commenterLink = UserExt.getUserLink(commenter);

                            desTemplate = desTemplate.replace("{user}", commenterLink);
                        }

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_UPDATE_COMMENT:
                        final JSONObject comment32 = commentMapper.get(dataId);

                        if (null == comment32) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleId32 = comment32.optString(Comment.COMMENT_ON_ARTICLE_ID);
                        final JSONObject commentArticle32 = articleMapper.get(articleId32);

                        final String commentArticleLink32 = "<a href=\""
                                + commentArticle32.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + commentArticle32.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", commentArticleLink32);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ADD_ARTICLE_REWARD:
                        final JSONObject addArticleReword = articleMapper.get(dataId);
                        if (null == addArticleReword) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String addArticleRewordLink = "<a href=\""
                                + addArticleReword.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + addArticleReword.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", addArticleRewordLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ARTICLE_REWARD:
                        final JSONObject reward = rewardMapper.get(dataId);
                        String senderId = reward.optString(Reward.SENDER_ID);
                        if ("5In".equals(typeStr)) {
                            senderId = toId;
                        }
                        final String rewardArticleId = reward.optString(Reward.DATA_ID);

                        final JSONObject sender = userMapper.get(senderId);
                        final String senderLink = UserExt.getUserLink(sender);
                        desTemplate = desTemplate.replace("{user}", senderLink);

                        final JSONObject articleReward = articleMapper.get(rewardArticleId);
                        if (null == articleReward) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleRewardLink = "<a href=\""
                                + articleReward.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + articleReward.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleRewardLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_COMMENT_REWARD:
                        final JSONObject reward14 = rewardMapper.get(dataId);
                        JSONObject user14;
                        if ("14In".equals(typeStr)) {
                            user14 = userMapper.get(fromId);
                        } else {
                            user14 = userMapper.get(toId);
                        }
                        final String userLink14 = UserExt.getUserLink(user14);
                        desTemplate = desTemplate.replace("{user}", userLink14);
                        final String articleId14 = reward14.optString(Reward.DATA_ID);
                        final JSONObject article14 = articleMapper.get(articleId14);
                        if (null == article14) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }
                        final String articleLink14 = "<a href=\""
                                + article14.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + article14.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink14);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ARTICLE_THANK:
                        final JSONObject thank22 = rewardMapper.get(dataId);
                        JSONObject user22;
                        if ("22In".equals(typeStr)) {
                            user22 = userMapper.get(fromId);
                        } else {
                            user22 = userMapper.get(toId);
                        }
                        final String articleId22 = thank22.optString(Reward.DATA_ID);
                        final JSONObject article22 = articleMapper.get(articleId22);
                        if (null == article22) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String userLink22 = UserExt.getUserLink(user22);
                        desTemplate = desTemplate.replace("{user}", userLink22);

                        final String articleLink22 = "<a href=\""
                                + article22.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + article22.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink22);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_INVITE_REGISTER:
                        final JSONObject newUser = userMapper.get(dataId);
                        final String newUserLink = UserExt.getUserLink(newUser);
                        desTemplate = desTemplate.replace("{user}", newUserLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_INVITED_REGISTER:
                        final JSONObject referralUser = userMapper.get(dataId);
                        final String referralUserLink = UserExt.getUserLink(referralUser);
                        desTemplate = desTemplate.replace("{user}", referralUserLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_INVITECODE_USED:
                        final JSONObject newUser1 = userMapper.get(dataId);
                        final String newUserLink1 = UserExt.getUserLink(newUser1);
                        desTemplate = desTemplate.replace("{user}", newUserLink1);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_CHECKIN:
                    case Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_YESTERDAY_LIVENESS_REWARD:
                    case Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_1A0001:
                    case Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_1A0001_COLLECT:
                    case Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_CHARACTER:
                    case Pointtransfer.TRANSFER_TYPE_C_BUY_INVITECODE:
                    case Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_EATINGSNAKE:
                    case Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_EATINGSNAKE_COLLECT:
                    case Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_GOBANG:
                    case Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_GOBANG_COLLECT:
                    case Pointtransfer.TRANSFER_TYPE_C_REPORT_HANDLED:
                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_AT_PARTICIPANTS:
                        final JSONObject comment20 = commentMapper.get(dataId);
                        if (null == comment20) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleId20 = comment20.optString(Comment.COMMENT_ON_ARTICLE_ID);
                        final JSONObject atParticipantsArticle = articleMapper.get(articleId20);
                        if (null == atParticipantsArticle) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String ArticleLink20 = "<a href=\""
                                + atParticipantsArticle.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + atParticipantsArticle.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", ArticleLink20);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_STICK_ARTICLE:
                        final JSONObject stickArticle = articleMapper.get(dataId);
                        if (null == stickArticle) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String stickArticleLink = "<a href=\""
                                + stickArticle.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + stickArticle.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", stickArticleLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ACCOUNT2ACCOUNT:
                        JSONObject user9;
                        if ("9In".equals(typeStr)) {
                            user9 = userMapper.get(fromId);
                        } else {
                            user9 = userMapper.get(toId);
                        }

                        final String userLink = UserExt.getUserLink(user9);
                        desTemplate = desTemplate.replace("{user}", userLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_CHECKIN_STREAK:
                        desTemplate = desTemplate.replace("{point}",
                                String.valueOf(Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHECKINT_STREAK));
                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_CHARGE:
                        final String yuan = dataId.split("-")[0];
                        desTemplate = desTemplate.replace("{yuan}", yuan);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_EXCHANGE:
                        final String exYuan = dataId;
                        desTemplate = desTemplate.replace("{yuan}", exYuan);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ABUSE_DEDUCT:
                        desTemplate = desTemplate.replace("{action}", dataId);
                        desTemplate = desTemplate.replace("{point}", record.optString(Pointtransfer.SUM));

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ADD_ARTICLE_BROADCAST:
                        final JSONObject addArticleBroadcast = articleMapper.get(dataId);
                        if (null == addArticleBroadcast) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String addArticleBroadcastLink = "<a href=\""
                                + addArticleBroadcast.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + addArticleBroadcast.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", addArticleBroadcastLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_PERFECT_ARTICLE:
                        final JSONObject perfectArticle = articleMapper.get(dataId);
                        if (null == perfectArticle) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String perfectArticleLink = "<a href=\""
                                + perfectArticle.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + perfectArticle.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", perfectArticleLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_QNA_OFFER:
                        final JSONObject reward34 = rewardMapper.get(dataId);
                        JSONObject user34;
                        if ("34In".equals(typeStr)) {
                            user34 = userMapper.get(fromId);
                        } else {
                            user34 = userMapper.get(toId);
                        }
                        final String userLink34 = UserExt.getUserLink(user34);
                        desTemplate = desTemplate.replace("{user}", userLink34);
                        final String articleId34 = reward34.optString(Reward.DATA_ID);
                        final JSONObject article34 = articleMapper.get(articleId34);
                        if (null == article34) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }
                        final String articleLink34 = "<a href=\""
                                + article34.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + article34.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink34);

                        break;
                    default:
                        LOGGER.warn("Invalid point type [" + type + "]");
                }

                desTemplate = Emotions.convert(desTemplate);

                record.put(Common.DESCRIPTION, desTemplate);
            }
            JSONArray jsonArray = JsonUtil.listToJSONArray(records);
            JSONObject ret = new JSONObject();
            ret.put(Keys.RESULTS, jsonArray);

            final int recordCnt = ret.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT);
            ret.remove(Pagination.PAGINATION);
            ret.put(Pagination.PAGINATION_RECORD_COUNT, recordCnt);

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets user points failed", e);
            throw new Exception(e);
        }
    }
}
