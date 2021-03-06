package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entityUtil.FollowUtil;
import cn.he.zhao.bbs.entityUtil.PointtransferUtil;
import cn.he.zhao.bbs.entityUtil.RoleUtil;
import cn.he.zhao.bbs.entityUtil.UserExtUtil;
import cn.he.zhao.bbs.entityUtil.my.CollectionUtils;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.Pagination;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.spring.Common;
import cn.he.zhao.bbs.spring.Paginator;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import cn.he.zhao.bbs.util.JsonUtil;
import cn.he.zhao.bbs.util.Sessions;
import cn.he.zhao.bbs.util.Times;
import cn.he.zhao.bbs.util.URLs;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * User query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 1.8.7.0, Jan 28, 2018
 * @since 0.2.0
 */
@Service
public class UserQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserQueryService.class);

    /**
     * All usernames.
     */
    public static final List<JSONObject> USER_NAMES = Collections.synchronizedList(new ArrayList<JSONObject>());

    /**
     * User Mapper.
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * FollowUtil Mapper.
     */
    @Autowired
    private FollowMapper followMapper;

    /**
     * Avatar query service.
     */
    @Autowired
    private AvatarQueryService avatarQueryService;

    /**
     * PointtransferUtil Mapper.
     */
    @Autowired
    private PointtransferMapper pointtransferMapper;

    /**
     * RoleUtil query service.
     */
    @Autowired
    private RoleQueryService roleQueryService;

    /**
     * Get nice users with the specified fetch size.
     *
     * @param fetchSize the specified fetch size
     * @return a list of users
     */
    public List<JSONObject> getNiceUsers(int fetchSize) {
        final List<JSONObject> ret = new ArrayList<>();

        final int RANGE_SIZE = 64;

        try {
//            final Query userQuery = new Query()
//            userQuery.setCurrentPageNum(1).setPageCount(1).setPageSize(RANGE_SIZE).
//                    setFilter(new PropertyFilter(UserExtUtil.USER_STATUS, FilterOperator.EQUAL, UserExtUtil.USER_STATUS_C_VALID)).
//                    addSort(UserExtUtil.USER_ARTICLE_COUNT, SortDirection.DESCENDING).
//                    addSort(UserExtUtil.USER_COMMENT_COUNT, SortDirection.DESCENDING);

            PageHelper.startPage(1,RANGE_SIZE,"userArticleCount DESC,userCommentCount DESC");
            final List<UserExt> rangeUsers = userMapper.getByUserStatus(UserExtUtil.USER_STATUS_C_VALID);

            final int realLen = rangeUsers.size();
            if (realLen < fetchSize) {
                fetchSize = realLen;
            }


            final List<Integer> indices = CollectionUtils.getRandomIntegers(0, realLen, fetchSize);

            for (final Integer index : indices) {
                JSONObject json = new JSONObject(JsonUtil.objectToJson(rangeUsers.get(index)));
                ret.add(json);
            }

            for (final JSONObject selectedUser : ret) {
                avatarQueryService.fillUserAvatarURL(UserExtUtil.USER_AVATAR_VIEW_MODE_C_STATIC, selectedUser);
            }
        } catch (final Exception e) {
            LOGGER.error( "Get nice users failed", e);
        }

        return ret;
    }

    /**
     * Gets invite user count of a user specified by the given user id.
     *
     * @param userId the given user id
     * @return invited user count
     */
    public int getInvitedUserCount(final String userId) {
//        final Query query = new Query().setFilter(CompositeFilterOperator.and(
//                new PropertyFilter(PointtransferUtil.TO_ID, FilterOperator.EQUAL, userId),
//                CompositeFilterOperator.or(
//                        new PropertyFilter(PointtransferUtil.TYPE, FilterOperator.EQUAL,
//                                PointtransferUtil.TRANSFER_TYPE_C_INVITECODE_USED),
//                        new PropertyFilter(PointtransferUtil.TYPE, FilterOperator.EQUAL,
//                                PointtransferUtil.TRANSFER_TYPE_C_INVITE_REGISTER))
//        ));

        try {
            return (int) pointtransferMapper.getInvitedUserCount(userId,
                    PointtransferUtil.TRANSFER_TYPE_C_INVITECODE_USED,PointtransferUtil.TRANSFER_TYPE_C_INVITE_REGISTER);
        } catch (final Exception e) {
            LOGGER.error( "Gets invited user count failed", e);

            return 0;
        }
    }

    /**
     * Gets latest logged in users by the specified time.
     *
     * @param time           the specified start time
     * @param currentPageNum the specified current page number
     * @param pageSize       the specified page size
     * @param windowSize     the specified window size
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "users": [{
     *         "oId": "",
     *         "userName": "",
     *         "userEmail": "",
     *         "userPassword": "",
     *         "roleName": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @throws Exception service exception
     * @see Pagination
     */
    public JSONObject getLatestLoggedInUsers(final long time, final int currentPageNum, final int pageSize,
                                             final int windowSize) throws Exception {
        final JSONObject ret = new JSONObject();

        PageHelper.startPage(currentPageNum,pageSize,"OId DESC");
//        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
//                .setCurrentPageNum(currentPageNum).setPageSize(pageSize)
//                .setFilter(CompositeFilterOperator.and(
//                        new PropertyFilter(UserExtUtil.USER_STATUS, FilterOperator.EQUAL, UserExtUtil.USER_STATUS_C_VALID),
//                        new PropertyFilter(UserExtUtil.USER_LATEST_LOGIN_TIME, FilterOperator.GREATER_THAN_OR_EQUAL, time)
//                ));

        PageInfo<UserExt> result = null;
        try {
            result = new PageInfo<>(userMapper.getLatestLoggedByTime(UserExtUtil.USER_STATUS_C_VALID,time));
        } catch (final Exception e) {
            LOGGER.error( "Gets latest logged in user failed", e);

            throw new Exception(e);
        }

//        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);
        final int pageCount = result.getPages();

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final List<UserExt> users = result.getList();
        ret.put(User.USERS, users);

        return ret;
    }

    /**
     * Gets user count of the specified day.
     *
     * @param day the specified day
     * @return user count
     */
    public int getUserCntInDay(final Date day) {
        final long time = day.getTime();
        final long start = Times.getDayStartTime(time);
        final long end = Times.getDayEndTime(time);

//        final Query query = new Query().setFilter(CompositeFilterOperator.and(
//                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, start),
//                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN, end),
//                new PropertyFilter(UserExtUtil.USER_STATUS, FilterOperator.EQUAL, UserExtUtil.USER_STATUS_C_VALID)
//        ));

        try {
            return (int) userMapper.getUserCountByTime(start,end,UserExtUtil.USER_STATUS_C_VALID);
        } catch (final Exception e) {
            LOGGER.error( "Count day user failed", e);

            return 1;
        }
    }

    /**
     * Gets user count of the specified month.
     *
     * @param month the specified month
     * @return user count
     */
    public int getUserCntInMonth(final Date month) {
        final long time = month.getTime();
        final long start = Times.getMonthStartTime(time);
        final long end = Times.getMonthEndTime(time);

//        final Query query = new Query().setFilter(CompositeFilterOperator.and(
//                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, start),
//                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN, end),
//                new PropertyFilter(UserExt.USER_STATUS, FilterOperator.EQUAL, UserExt.USER_STATUS_C_VALID)
//        ));

        try {
            return (int) userMapper.getUserCountByTime(start,end,UserExtUtil.USER_STATUS_C_VALID);
        } catch (final Exception e) {
            LOGGER.error( "Count month user failed", e);

            return 1;
        }
    }

    /**
     * Loads all usernames from database.
     */
    public void loadUserNames() {
        USER_NAMES.clear();

//        final Query query = new Query().setPageCount(1).
//                setFilter(new PropertyFilter(UserExtUtil.USER_STATUS, FilterOperator.EQUAL, UserExtUtil.USER_STATUS_C_VALID)).
//                addProjection(User.USER_NAME, String.class).
//                addProjection(UserExtUtil.USER_AVATAR_URL, String.class);
        try {
            final List<UserExt> array = userMapper.getUserByValid(UserExtUtil.USER_STATUS_C_VALID); // XXX: Performance Issue
//            final JSONArray array = result.get(Keys.RESULTS);
            for (int i = 0; i < array.size(); i++) {
                final UserExt user = array.get(i);

                final JSONObject u = new JSONObject();
                u.put(User.USER_NAME, user.getUserName());
                u.put(UserExtUtil.USER_T_NAME_LOWER_CASE, user.getUserName().toLowerCase());
                JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(user));
                final String avatar = avatarQueryService.getAvatarURLByUser(UserExtUtil.USER_AVATAR_VIEW_MODE_C_STATIC, jsonObject, "20");
                u.put(UserExtUtil.USER_AVATAR_URL, avatar);
                USER_NAMES.add(u);
            }

            Collections.sort(USER_NAMES, (u1, u2) -> {
                final String u1Name = u1.optString(UserExtUtil.USER_T_NAME_LOWER_CASE);
                final String u2Name = u2.optString(UserExtUtil.USER_T_NAME_LOWER_CASE);

                return u1Name.compareTo(u2Name);
            });
        } catch (final Exception e) {
            LOGGER.error( "Loads usernames error", e);
        }
    }

    /**
     * Gets usernames by the specified name prefix.
     *
     * @param namePrefix the specified name prefix
     * @return a list of usernames, for example      <pre>
     * [
     *     {
     *         "userName": "",
     *         "userAvatarURL": "",
     *     }, ....
     * ]
     * </pre>
     */
    public List<JSONObject> getUserNamesByPrefix(final String namePrefix) {
        final JSONObject nameToSearch = new JSONObject();
        nameToSearch.put(UserExtUtil.USER_T_NAME_LOWER_CASE, namePrefix.toLowerCase());

        int index = Collections.binarySearch(USER_NAMES, nameToSearch, (u1, u2) -> {
            String u1Name = u1.optString(UserExtUtil.USER_T_NAME_LOWER_CASE);
            final String inputName = u2.optString(UserExtUtil.USER_T_NAME_LOWER_CASE);

            if (u1Name.length() < inputName.length()) {
                return u1Name.compareTo(inputName);
            }

            u1Name = u1Name.substring(0, inputName.length());

            return u1Name.compareTo(inputName);
        });

        final List<JSONObject> ret = new ArrayList<>();

        if (index < 0) {
            return ret;
        }

        int start = index;
        int end = index;

        while (start > -1 && USER_NAMES.get(start).optString(UserExtUtil.USER_T_NAME_LOWER_CASE).startsWith(namePrefix.toLowerCase())) {
            start--;
        }

        start++;

        if (start < index - 5) {
            end = start + 5;
        } else {
            while (end < USER_NAMES.size() && end < index + 5 && USER_NAMES.get(end).optString(UserExtUtil.USER_T_NAME_LOWER_CASE).startsWith(namePrefix.toLowerCase())) {
                end++;

                if (end >= start + 5) {
                    break;
                }
            }
        }

        return USER_NAMES.subList(start, end);
    }

    /**
     * Gets the current user.
     *
     * @param request the specified request
     * @return the current user, {@code null} if not found
     * @throws Exception service exception
     */
    public UserExt getCurrentUser(final HttpServletRequest request) throws Exception {
        final JSONObject currentUser = Sessions.currentUser(request);
        if (null == currentUser) {
            return null;
        }

        final String id = currentUser.optString(Keys.OBJECT_ID);

        return getUser(id);
    }

    /**
     * Gets the administrators.
     *
     * @return administrators, returns an empty list if not found or error
     * @throws Exception service exception
     */
    public List<UserExt> getAdmins() throws Exception {
        try {
            return userMapper.getAdmins(RoleUtil.ROLE_ID_C_ADMIN);
        } catch (final Exception e) {
            LOGGER.error( "Gets admins failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets the super administrator.
     *
     * @return super administrator
     * @throws Exception service exception
     */
    public UserExt getSA() throws Exception {
        return getAdmins().get(0);
    }

    /**
     * Gets the default commenter.
     *
     * @return default commenter
     * @throws Exception service exception
     */
    public UserExt getDefaultCommenter() throws Exception {
        final UserExt ret = getUserByName(UserExtUtil.DEFAULT_CMTER_NAME);
//        ret.remove(UserExt.USER_T_POINT_HEX);
//        ret.remove(UserExt.USER_T_POINT_CC);

        ret.setUserPointHex("");
        ret.setUserPointCC("");

        return ret;
    }

    /**
     * Gets a user by the specified email.
     *
     * @param email the specified email
     * @return user, returns {@code null} if not found
     * @throws Exception service exception
     */
    public UserExt getUserByEmail(final String email) throws Exception {
        try {
            return userMapper.getByEmail(email);
        } catch (final Exception e) {
            LOGGER.error( "Gets user by email[" + email + "] failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Gets user names from the specified text.
     * <p>
     * A user name is between &#64; and a punctuation, a blank or a line break (\n). For example, the specified text is
     * <pre>&#64;88250 It is a nice day. &#64;Vanessa, we are on the way.</pre> There are two user names in the text,
     * 88250 and Vanessa.
     * </p>
     *
     * @param text the specified text
     * @return user names, returns an empty set if not found
     */
    public Set<String> getUserNames(final String text) {
        final Set<String> ret = new HashSet<>();
        int idx = text.indexOf('@');

        if (-1 == idx) {
            return ret;
        }

        String copy = text.trim();
        copy = copy.replaceAll("\\n", " ");
        //(?=\\pP)匹配标点符号-http://www.cnblogs.com/qixuejia/p/4211428.html
        copy = copy.replaceAll("(?=\\pP)[^@]", " ");
        String[] uNames = StringUtils.substringsBetween(copy, "@", " ");

        String tail = StringUtils.substringAfterLast(copy, "@");
        if (tail.contains(" ")) {
            tail = null;
        }

        if (null != tail) {
            if (null == uNames) {
                uNames = new String[1];
                uNames[0] = tail;
            } else {
                uNames = Arrays.copyOf(uNames, uNames.length + 1);
                uNames[uNames.length - 1] = tail;
            }
        }

        String[] uNames2 = StringUtils.substringsBetween(copy, "@", "<");

        final Set<String> maybeUserNameSet;
        if (null == uNames) {
            uNames = uNames2;

            if (null == uNames) {
                return ret;
            }

            maybeUserNameSet = CollectionUtils.arrayToSet(uNames);
        } else {
            maybeUserNameSet = CollectionUtils.arrayToSet(uNames);

            if (null != uNames2) {
                maybeUserNameSet.addAll(CollectionUtils.arrayToSet(uNames2));
            }
        }

        for (String maybeUserName : maybeUserNameSet) {
            maybeUserName = maybeUserName.trim();
            if (null != getUserByName(maybeUserName)) { // Found a user
                ret.add(maybeUserName);
            }
        }

        return ret;
    }

    /**
     * Gets a user by the specified name.
     *
     * @param name the specified name
     * @return user, returns {@code null} if not found
     */
    public UserExt getUserByName(final String name) {
        try {
            final UserExt ret = userMapper.getByName(name);
            if (null == ret) {
                return null;
            }

            final int point = ret.getUserPoint();
            final int appRole = ret.getUserAppRole();
            if (UserExtUtil.USER_APP_ROLE_C_HACKER == appRole) {
                ret.setUserPointHex(Integer.toHexString(point));
            } else {
                ret.setUserPointCC( UserExtUtil.toCCString(point));
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets user by name[" + name + "] failed", e);

            return null;
        }
    }

    /**
     * Gets users by the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          "userNameOrEmail": "", // optional
     *                          "paginationCurrentPageNum": 1,
     *                          "paginationPageSize": 20,
     *                          "paginationWindowSize": 10,
     *                          , see {@link Pagination} for more details
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "users": [{
     *         "oId": "",
     *         "userName": "",
     *         "userEmail": "",
     *         "userPassword": "",
     *         "roleName": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @throws Exception service exception
     * @see Pagination
     */
    public JSONObject getUsers(final JSONObject requestJSONObject) throws Exception {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);

        PageHelper.startPage(currentPageNum,pageSize,"oId DESC");
//        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
//                setCurrentPageNum(currentPageNum).setPageSize(pageSize);


        final String q;
        if (requestJSONObject.has(Common.QUERY)) {
            q = requestJSONObject.optString(Common.QUERY);
//            final List<Filter> filters = new ArrayList<>();
//            filters.add(new PropertyFilter(User.USER_NAME, FilterOperator.EQUAL, q));
//            filters.add(new PropertyFilter(User.USER_EMAIL, FilterOperator.EQUAL, q));
//            filters.add(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL, q));
//            query.setFilter(new CompositeFilter(CompositeFilterOperator.OR, filters));
        } else {
            // TODO: 2018/11/7 没有查询条件退出
            return null;
        }

        PageInfo<UserExt> result;
        try {
            result = new PageInfo<>(userMapper.getByNameOrEmailOrOId(q));
        } catch (final Exception e) {
            LOGGER.error( "Gets users failed", e);

            throw new Exception(e);
        }

        final int pageCount = result.getPages();
        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final List<UserExt> users = result.getList();
        ret.put(User.USERS, users);

        for (int i = 0; i < users.size(); i++) {
            final UserExt user = users.get(i);
            user.setUserCreateTime(new Date(user.getOid()));

            JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(user));
            avatarQueryService.fillUserAvatarURL(UserExtUtil.USER_AVATAR_VIEW_MODE_C_ORIGINAL, jsonObject);

            final Role role = roleQueryService.getRole(user.getUserRole());
            user.setRoleName(role.getRoleName());
        }

        return ret;
    }

    /**
     * Gets users by the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          "userCity": "",
     *                          "userLatestLoginTime": long, // optional, default to 0
     *                          "paginationCurrentPageNum": 1,
     *                          "paginationPageSize": 20,
     *                          "paginationWindowSize": 10,
     *                          }, see {@link Pagination} for more details
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "users": [{
     *         "oId": "",
     *         "userName": "",
     *         "userEmail": "",
     *         "userPassword": "",
     *         "roleName": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @throws Exception service exception
     * @see Pagination
     */
    public JSONObject getUsersByCity(final JSONObject requestJSONObject) throws Exception {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
        final String city = requestJSONObject.optString(UserExtUtil.USER_CITY);
        final long latestTime = requestJSONObject.optLong(UserExtUtil.USER_LATEST_LOGIN_TIME);

        PageHelper.startPage(currentPageNum,pageSize,"userLatestLoginTime DESC");
//        final Query query = new Query().addSort(UserExtUtil.USER_LATEST_LOGIN_TIME, SortDirection.DESCENDING)
//                .setCurrentPageNum(currentPageNum).setPageSize(pageSize)
//                .setFilter(CompositeFilterOperator.and(
//                        new PropertyFilter(UserExtUtil.USER_CITY, FilterOperator.EQUAL, city),
//                        new PropertyFilter(UserExtUtil.USER_STATUS, FilterOperator.EQUAL, UserExtUtil.USER_STATUS_C_VALID),
//                        new PropertyFilter(UserExtUtil.USER_LATEST_LOGIN_TIME, FilterOperator.GREATER_THAN_OR_EQUAL, latestTime)
//                ));
        PageInfo<UserExt> result = null;
        try {
            result = new PageInfo<>(userMapper.getByLoginTimeAndCity(city, latestTime, UserExtUtil.USER_STATUS_C_VALID));
        } catch (final Exception e) {
            LOGGER.error( "Gets users by city error", e);

            throw new Exception(e);
        }

//        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);
        final int pageCount = result.getPages();

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final List<UserExt> users = result.getList();
        // TODO: 2018/11/7 删除以下的代码是否有影响？
//        try {
//            for (int i = 0; i < users.size(); i++) {
//                UserExt user = users.get(i);
//                users.get(i).setf(Common.IS_FOLLOWING,
//                        followMapper.exists(requestJSONObject.optString(Keys.OBJECT_ID), user.getOid(),
//                                FollowUtil.FOLLOWING_TYPE_C_USER));
//            }
//        } catch (final Exception e) {
//            LOGGER.error( "Fills following failed", e);
//        }
        ret.put(User.USERS, users);
        return ret;
    }

    /**
     * Gets a user by the specified user id.
     *
     * @param userId the specified user id
     * @return for example,      <pre>
     * {
     *     "oId": "",
     *     "userName": "",
     *     "userEmail": "",
     *     "userPassword": "",
     *     ....
     * }
     * </pre>, returns {@code null} if not found
     * @throws Exception service exception
     */
    public UserExt getUser(final String userId) throws Exception {
        try {
            return userMapper.get(userId);
        } catch (final Exception e) {
            LOGGER.error( "Gets a user failed", e);

            throw new Exception(e);
        }
    }

    /**
     * Gets the URL of user logout.
     *
     * @param redirectURL redirect URL after logged in
     * @return logout URL, returns {@code null} if the user is not logged in
     */
    public String getLogoutURL(final String redirectURL) {
        String to = SpringUtil.getServerPath();
        to = URLs.encode(to + redirectURL);

        return SpringUtil.getContextPath() + "/logout?goto=" + to;
    }

    /**
     * Gets the URL of user login.
     *
     * @param redirectURL redirect URL after logged in
     * @return login URL
     */
    public String getLoginURL(final String redirectURL) {
        String to = SpringUtil.getServerPath();
        to = URLs.encode(to + redirectURL);

        return SpringUtil.getContextPath() + "/login?goto=" + to;
    }
}
