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

import cn.he.zhao.bbs.entityUtil.BreezemoonUtil;
import cn.he.zhao.bbs.entityUtil.CommonUtil;
import cn.he.zhao.bbs.entityUtil.UserExtUtil;
import cn.he.zhao.bbs.spring.Locales;
import cn.he.zhao.bbs.spring.Paginator;
import cn.he.zhao.bbs.spring.Stopwatchs;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.Breezemoon;
import cn.he.zhao.bbs.entity.UserExt;
import cn.he.zhao.bbs.entityUtil.my.CollectionUtils;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.Pagination;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.util.Emotions;
import cn.he.zhao.bbs.util.JsonUtil;
import cn.he.zhao.bbs.util.Markdowns;
import cn.he.zhao.bbs.util.Times;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Breezemoon query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.3, Jun 9, 2018
 * @since 2.8.0
 */
@Service
public class BreezemoonQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BreezemoonQueryService.class);

    /**
     * Breezemoon Mapper.
     */
    @Autowired
    private BreezemoonMapper breezemoonMapper;

    /**
     * User Mapper.
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * FollowUtil query service.
     */
    @Autowired
    private FollowQueryService followQueryService;

    /**
     * Avatar query service.
     */
    @Autowired
    private AvatarQueryService avatarQueryService;

    /**
     * Get following user breezemoons.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param userId         the specified user id, may be {@code null}
     * @param page           the specified page number
     * @param pageSize       the specified page size
     * @param windowSize     the specified window size
     * @return for example, <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "breezemoons": [{
     *         "id": "",
     *         "breezemoonContent": ""
     *      }, ....]
     * }
     * </pre>
     * @throws Exception service exception
     */
    public PageInfo<Breezemoon> getFollowingUserBreezemoons(final int avatarViewMode, final String userId,
                                                  final int page, final int pageSize, final int windowSize) throws Exception {
//        final JSONObject ret = new JSONObject();

        final List<JSONObject> users = (List<JSONObject>) followQueryService.getFollowingUsers(
                avatarViewMode, userId, 1, Integer.MAX_VALUE).opt(Keys.RESULTS);
        if (users.isEmpty()) {
            return getBreezemoons(avatarViewMode, userId, "", page, pageSize, windowSize);
        }

        PageHelper.startPage(page,pageSize);
//        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
//                .setPageSize(pageSize).setCurrentPageNum(page);

        final List<String> followingUserIds = new ArrayList<>();
        for (final JSONObject user : users) {
            followingUserIds.add(user.optString(Keys.OBJECT_ID));
        }
//        query.setFilter(CompositeFilterOperator.and(
//                new PropertyFilter(BreezemoonUtil.BREEZEMOON_STATUS, FilterOperator.EQUAL, BreezemoonUtil.BREEZEMOON_STATUS_C_VALID),
//                new PropertyFilter(BreezemoonUtil.BREEZEMOON_AUTHOR_ID, FilterOperator.IN, followingUserIds)
//        ));

        PageInfo<Breezemoon> result;
        try {
            Stopwatchs.start("Query following user breezemoons");

            result = new PageInfo<>(breezemoonMapper.getFollowingUserBreezemoon(BreezemoonUtil.BREEZEMOON_STATUS_C_VALID, followingUserIds));
            final List<Breezemoon> bms =  result.getList();
//            final List<JSONObject> bms = CollectionUtils.jsonArrayToList(data);
            if (bms.isEmpty()) {
                return getBreezemoons(avatarViewMode, userId, "", page, pageSize, windowSize);
            }

            organizeBreezemoons(avatarViewMode, userId, bms);
//            ret.put(BreezemoonUtil.BREEZEMOONS, (Object) bms);

        } catch (final Exception e) {
            LOGGER.error( "Gets following user breezemoons failed", e);

            throw new Exception(e);
        } finally {
            Stopwatchs.end();
        }

        // TODO: 2018/10/13 取消分页结果
//        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
//        ret.put(Pagination.PAGINATION, pagination);
//        final int pageCount = result.getPages(Pagination.PAGINATION_PAGE_COUNT);
//        final List<Integer> pageNums = Paginator.paginate(page, pageSize, pageCount, windowSize);
//        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        return result;
    }

    /**
     * Get breezemoon with the specified user id, current page number.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param currentUserId  the specified current user id, may be {@code null}
     * @param authorId       the specified user id, empty "" for all users
     * @param page           the specified current page number
     * @param pageSize       the specified page size
     * @param windowSize     the specified window size
     * @return for example, <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "breezemoons": [{
     *         "id": "",
     *         "breezemoonContent": ""
     *      }, ....]
     * }
     * </pre>
     * @throws Exception service exception
     * @see Pagination
     */
    public PageInfo<Breezemoon> getBreezemoons(final int avatarViewMode, final String currentUserId, final String authorId,
                                     final int page, final int pageSize, final int windowSize) throws Exception {
//        final JSONObject ret = new JSONObject();
//        CompositeFilter filter;
//        final Filter statusFilter = new PropertyFilter(BreezemoonUtil.BREEZEMOON_STATUS, FilterOperator.EQUAL, BreezemoonUtil.BREEZEMOON_STATUS_C_VALID);
//        if (StringUtils.isNotBlank(authorId)) {
//            filter = CompositeFilterOperator.and(new PropertyFilter(BreezemoonUtil.BREEZEMOON_AUTHOR_ID, FilterOperator.EQUAL, authorId), statusFilter);
//        } else {
//            filter = CompositeFilterOperator.and(new PropertyFilter(BreezemoonUtil.BREEZEMOON_AUTHOR_ID, FilterOperator.NOT_EQUAL, authorId), statusFilter);
//        }
//        final Query query = new Query().setFilter(filter).addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).setCurrentPageNum(page).setPageSize(20);

        PageHelper.startPage(page,20,"oId DESC");

        PageInfo<Breezemoon> result;
        try{
            if (StringUtils.isNotBlank(authorId)) {
                result = new PageInfo<>( breezemoonMapper.getByUseridAndStatus(BreezemoonUtil.BREEZEMOON_STATUS_C_VALID, authorId));
            } else {
                result = new PageInfo<>( breezemoonMapper.getByExcludeUseridAndStatus(BreezemoonUtil.BREEZEMOON_STATUS_C_VALID, authorId));
            }
        }catch (final Exception e) {
            LOGGER.error( "Get breezemoons failed", e);

            throw new Exception(e);
        }



//        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
//        ret.put(Pagination.PAGINATION, pagination);
//        final int pageCount = result.getPages();
//        final List<Integer> pageNums = Paginator.paginate(page, pageSize, pageCount, windowSize);
//        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final List<Breezemoon> bms = result.getList();
//        final List<JSONObject> bms = CollectionUtils.jsonArrayToList(data);
        try {
            organizeBreezemoons(avatarViewMode, currentUserId, bms);
        } catch (final Exception e) {
            LOGGER.error( "Get breezemoons failed", e);

            throw new Exception(e);
        }

//        ret.put(BreezemoonUtil.BREEZEMOONS, (Object) bms);

        return result;
    }

    /**
     * Get breezemoons by the specified request json object.
     *
     * @param avatarViewMode    the specified avatar view mode
     * @param requestJSONObject the specified request json object, for example,
     *                          "paginationCurrentPageNum": 1,
     *                          "paginationPageSize": 20,
     *                          "paginationWindowSize": 10,
     *                          , see {@link Pagination} for more details
     * @param fields            the specified fields to return
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "breezemoons": [{
     *         "oId": "",
     *         "breezemoonContent": "",
     *         "breezemoonCreateTime": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @throws Exception service exception
     * @see Pagination
     */

    public JSONObject getBreezemoons(final int avatarViewMode,
                                     final JSONObject requestJSONObject, final Map<String, Class<?>> fields) throws Exception {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);

        PageHelper.startPage(currentPageNum,pageSize,"oId DESC");
//        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize)
//                .addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
//        for (final Map.Entry<String, Class<?>> field : fields.entrySet()) {
//            query.addProjection(field.getKey(), field.getValue());
//        }

        PageInfo<Breezemoon> result;
        try {
            result = new PageInfo<>( breezemoonMapper.getAll());
        } catch (final Exception e) {
            LOGGER.error( "Get breezemoons failed", e);

            throw new Exception(e);
        }

        final int pageCount = result.getPages();

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final List<Breezemoon> breezemoons = result.getList();
//        final List<JSONObject> breezemoons = CollectionUtils.jsonArrayToList(data);
        try {
            organizeBreezemoons(avatarViewMode, "admin", breezemoons);
        } catch (final Exception e) {
            LOGGER.error( "Organize breezemoons failed", e);

            throw new Exception(e);
        }

        ret.put(BreezemoonUtil.BREEZEMOONS, breezemoons);

        return ret;
    }

    /**
     * Gets a breezemoon by the specified id.
     *
     * @param breezemoonId the specified id
     * @return breezemoon, return {@code null} if not found
     * @throws Exception service exception
     */
    public Breezemoon getBreezemoon(final String breezemoonId) throws Exception {
        try {
            return breezemoonMapper.get(breezemoonId);
        } catch (final Exception e) {
            LOGGER.error( "Gets a breezemoon [id=" + breezemoonId + "] failed", e);

            throw new Exception(e);
        }
    }

    private void organizeBreezemoons(final int avatarViewMode, final String currentUserId, final List<Breezemoon> breezemoons) throws Exception {
        final Iterator<Breezemoon> iterator = breezemoons.iterator();
        while (iterator.hasNext()) {
            final Breezemoon bm = iterator.next();

            final String authorId = bm.getBreezemoonAuthorId();
            final UserExt author = userMapper.get(authorId);
            if (UserExtUtil.USER_XXX_STATUS_C_PRIVATE == author.getUserBreezemoonStatus()
                    && !StringUtils.equals(currentUserId, authorId) && !"admin".equals(currentUserId)) {
                iterator.remove();

                continue;
            }
            bm.setBreezemoonAuthorName( author.getUserName());

            JSONObject json = new JSONObject(JsonUtil.objectToJson(author));
            bm.setBreezemoonAuthorThumbnailURL( avatarQueryService.getAvatarURLByUser(avatarViewMode, json, "48"));
            final long time = bm.getBreezemoonCreated();
            bm.setTimeAgo( Times.getTimeAgo(time, Locales.getLocale()));
            bm.setBreezemoonCreateTime( new Date(time));
            String content = bm.getBreezemoonContent();
            content = Emotions.convert(content);
            content = Markdowns.toHTML(content);
            content = Markdowns.clean(content, "");
            bm.setBreezemoonContent( content);
        }
    }
}
