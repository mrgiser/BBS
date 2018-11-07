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
            final JSONArray jsonArray = JsonUtil.listToJSONArray(records);

            for (int i = 0; i < jsonArray.length(); i++) {

                final JSONObject record = jsonArray.optJSONObject(i);

                record.put(Common.CREATE_TIME, new Date(record.optLong(PointtransferUtil.TIME)));

                final String toId = record.optString(PointtransferUtil.TO_ID);
                final String fromId = record.optString(PointtransferUtil.FROM_ID);

                String typeStr = record.optString(PointtransferUtil.TYPE);
                if (("3".equals(typeStr) && userId.equals(toId))
                        || ("5".equals(typeStr) && userId.equals(fromId))
                        || ("9".equals(typeStr) && userId.equals(toId))
                        || ("14".equals(typeStr) && userId.equals(toId))
                        || ("22".equals(typeStr) && userId.equals(toId))) {
                    typeStr += "In";
                }

                if (fromId.equals(userId)) {
                    record.put(Common.BALANCE, record.optInt(PointtransferUtil.FROM_BALANCE));
                    record.put(Common.OPERATION, "-");
                } else {
                    record.put(Common.BALANCE, record.optInt(PointtransferUtil.TO_BALANCE));
                    record.put(Common.OPERATION, "+");
                }

                record.put(Common.DISPLAY_TYPE, langPropsService.get("pointType" + typeStr + "Label"));

                final int type = record.optInt(PointtransferUtil.TYPE);
                final String dataId = record.optString(PointtransferUtil.DATA_ID);
                String desTemplate = langPropsService.get("pointType" + typeStr + "DesLabel");

                switch (type) {
                    case PointtransferUtil.TRANSFER_TYPE_C_DATA_EXPORT:
                        desTemplate = desTemplate.replace("{num}", record.optString(PointtransferUtil.DATA_ID));

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_INIT:
                        desTemplate = desTemplate.replace("{point}", record.optString(PointtransferUtil.SUM));

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_ADD_ARTICLE:
                        final Article addArticle = articleMapper.get(dataId);
                        if (null == addArticle) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String addArticleLink = "<a href=\""
                                + addArticle.getArticlePermalink() + "\">"
                                + addArticle.getArticleTitle() + "</a>";
                        desTemplate = desTemplate.replace("{article}", addArticleLink);

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_UPDATE_ARTICLE:
                        final Article updateArticle = articleMapper.get(dataId);
                        if (null == updateArticle) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String updateArticleLink = "<a href=\""
                                + updateArticle.getArticlePermalink() + "\">"
                                + updateArticle.getArticleTitle() + "</a>";
                        desTemplate = desTemplate.replace("{article}", updateArticleLink);

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_ADD_COMMENT:
                        final Comment comment = commentMapper.get(dataId);

                        if (null == comment) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleId = comment.getCommentOnArticleId();
                        final Article commentArticle = articleMapper.get(articleId);

                        final String commentArticleLink = "<a href=\""
                                + commentArticle.getArticlePermalink() + "\">"
                                + commentArticle.getArticleTitle() + "</a>";
                        desTemplate = desTemplate.replace("{article}", commentArticleLink);

                        if ("3In".equals(typeStr)) {
                            final UserExt commenter = userMapper.get(fromId);
                            final String commenterLink = UserExtUtil.getUserLink(commenter);

                            desTemplate = desTemplate.replace("{user}", commenterLink);
                        }

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_UPDATE_COMMENT:
                        final Comment comment32 = commentMapper.get(dataId);

                        if (null == comment32) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleId32 = comment32.getCommentOnArticleId();
                        final Article commentArticle32 = articleMapper.get(articleId32);

                        final String commentArticleLink32 = "<a href=\""
                                + commentArticle32.getArticlePermalink() + "\">"
                                + commentArticle32.getArticleTitle() + "</a>";
                        desTemplate = desTemplate.replace("{article}", commentArticleLink32);

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_ADD_ARTICLE_REWARD:
                        final Article addArticleReword = articleMapper.get(dataId);
                        if (null == addArticleReword) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String addArticleRewordLink = "<a href=\""
                                + addArticleReword.getArticlePermalink() + "\">"
                                + addArticleReword.getArticleTitle() + "</a>";
                        desTemplate = desTemplate.replace("{article}", addArticleRewordLink);

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_ARTICLE_REWARD:
                        final Reward reward = rewardMapper.get(dataId);
                        String senderId = reward.getSenderId();
                        if ("5In".equals(typeStr)) {
                            senderId = toId;
                        }
                        final String rewardArticleId = reward.getDataId();

                        final UserExt sender = userMapper.get(senderId);
                        final String senderLink = UserExtUtil.getUserLink(sender);
                        desTemplate = desTemplate.replace("{user}", senderLink);

                        final Article articleReward = articleMapper.get(rewardArticleId);
                        if (null == articleReward) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleRewardLink = "<a href=\""
                                + articleReward.getArticlePermalink() + "\">"
                                + articleReward.getArticleTitle() + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleRewardLink);

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_COMMENT_REWARD:
                        final Reward reward14 = rewardMapper.get(dataId);
                        UserExt user14;
                        if ("14In".equals(typeStr)) {
                            user14 = userMapper.get(fromId);
                        } else {
                            user14 = userMapper.get(toId);
                        }
                        final String commentId14 = reward14.getDataId();
                        final Comment comment14 = commentMapper.get(commentId14);
                        if (null == comment14) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleId14 = comment14.getCommentOnArticleId();

                        final String userLink14 = UserExtUtil.getUserLink(user14);
                        desTemplate = desTemplate.replace("{user}", userLink14);

                        final Article article14 = articleMapper.get(articleId14);
                        final String articleLink = "<a href=\""
                                + article14.getArticlePermalink() + "\">"
                                + article14.getArticleTitle() + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink);

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_ARTICLE_THANK:
                        final Reward thank22 = rewardMapper.get(dataId);
                        UserExt user22;
                        if ("22In".equals(typeStr)) {
                            user22 = userMapper.get(fromId);
                        } else {
                            user22 = userMapper.get(toId);
                        }
                        final String articleId22 = thank22.getDataId();
                        final Article article22 = articleMapper.get(articleId22);
                        if (null == article22) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String userLink22 = UserExtUtil.getUserLink(user22);
                        desTemplate = desTemplate.replace("{user}", userLink22);

                        final String articleLink22 = "<a href=\""
                                + article22.getArticlePermalink() + "\">"
                                + article22.getArticleTitle() + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink22);

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_INVITE_REGISTER:
                        final UserExt newUser = userMapper.get(dataId);
                        final String newUserLink = UserExtUtil.getUserLink(newUser);
                        desTemplate = desTemplate.replace("{user}", newUserLink);

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_INVITED_REGISTER:
                        final UserExt referralUser = userMapper.get(dataId);
                        final String referralUserLink = UserExtUtil.getUserLink(referralUser);
                        desTemplate = desTemplate.replace("{user}", referralUserLink);

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_INVITECODE_USED:
                        final UserExt newUser1 = userMapper.get(dataId);
                        final String newUserLink1 = UserExtUtil.getUserLink(newUser1);
                        desTemplate = desTemplate.replace("{user}", newUserLink1);

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_CHECKIN:
                    case PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_YESTERDAY_LIVENESS_REWARD:
                    case PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_1A0001:
                    case PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_1A0001_COLLECT:
                    case PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_CHARACTER:
                    case PointtransferUtil.TRANSFER_TYPE_C_BUY_INVITECODE:
                    case PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_EATINGSNAKE:
                    case PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_EATINGSNAKE_COLLECT:
                    case PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_GOBANG:
                    case PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_GOBANG_COLLECT:
                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_AT_PARTICIPANTS:
                        final Comment comment20 = commentMapper.get(dataId);
                        if (null == comment20) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleId20 = comment20.getCommentOnArticleId();
                        final Article atParticipantsArticle = articleMapper.get(articleId20);
                        if (null == atParticipantsArticle) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String ArticleLink20 = "<a href=\""
                                + atParticipantsArticle.getArticlePermalink() + "\">"
                                + atParticipantsArticle.getArticleTitle() + "</a>";
                        desTemplate = desTemplate.replace("{article}", ArticleLink20);

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_STICK_ARTICLE:
                        final Article stickArticle = articleMapper.get(dataId);
                        if (null == stickArticle) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String stickArticleLink = "<a href=\""
                                + stickArticle.getArticlePermalink() + "\">"
                                + stickArticle.getArticleTitle() + "</a>";
                        desTemplate = desTemplate.replace("{article}", stickArticleLink);

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_ACCOUNT2ACCOUNT:
                        UserExt user9;
                        if ("9In".equals(typeStr)) {
                            user9 = userMapper.get(fromId);
                        } else {
                            user9 = userMapper.get(toId);
                        }

                        final String userLink = UserExtUtil.getUserLink(user9);
                        desTemplate = desTemplate.replace("{user}", userLink);

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_ACTIVITY_CHECKIN_STREAK:
                        desTemplate = desTemplate.replace("{point}",
                                String.valueOf(PointtransferUtil.TRANSFER_SUM_C_ACTIVITY_CHECKINT_STREAK));
                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_CHARGE:
                        final String yuan = dataId.split("-")[0];
                        desTemplate = desTemplate.replace("{yuan}", yuan);

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_EXCHANGE:
                        final String exYuan = dataId;
                        desTemplate = desTemplate.replace("{yuan}", exYuan);

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_ABUSE_DEDUCT:
                        desTemplate = desTemplate.replace("{action}", dataId);
                        desTemplate = desTemplate.replace("{point}", record.optString(PointtransferUtil.SUM));

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_ADD_ARTICLE_BROADCAST:
                        final Article addArticleBroadcast = articleMapper.get(dataId);
                        if (null == addArticleBroadcast) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String addArticleBroadcastLink = "<a href=\""
                                + addArticleBroadcast.getArticlePermalink() + "\">"
                                + addArticleBroadcast.getArticleTitle() + "</a>";
                        desTemplate = desTemplate.replace("{article}", addArticleBroadcastLink);

                        break;
                    case PointtransferUtil.TRANSFER_TYPE_C_PERFECT_ARTICLE:
                        final Article perfectArticle = articleMapper.get(dataId);
                        if (null == perfectArticle) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String perfectArticleLink = "<a href=\""
                                + perfectArticle.getArticlePermalink() + "\">"
                                + perfectArticle.getArticleTitle() + "</a>";
                        desTemplate = desTemplate.replace("{article}", perfectArticleLink);

                        break;
                    default:
                        LOGGER.warn("Invalid point type [" + type + "]");
                }

                desTemplate = Emotions.convert(desTemplate);

                record.put(Common.DESCRIPTION, desTemplate);
            }
//            JSONArray jsonArray = JsonUtil.listToJSONArray(records);
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
