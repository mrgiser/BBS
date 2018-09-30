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

import cn.he.zhao.bbs.entityUtil.InvitecodeUtil;
import cn.he.zhao.bbs.entityUtil.my.Pagination;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import cn.he.zhao.bbs.spring.Paginator;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * InvitecodeUtil query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.3, Sep 20, 2016
 * @since 1.4.0
 */
@Service
public class InvitecodeQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InvitecodeQueryService.class);

    /**
     * InvitecodeUtil Mapper.
     */
    @Autowired
    private InvitecodeMapper invitecodeMapper;

    /**
     * Gets valid invitecodes by the specified generator id.
     *
     * @param generatorId the specified generator id
     * @return for example,      <pre>
     * {
     *     "oId": "",
     *     "code": "",
     *     "memo": "",
     *     ....
     * }
     * </pre>, returns an empty list if not found
     */
    public List<Invitecode> getValidInvitecodes(final String generatorId) {
//        final Query query = new Query().setFilter(
//                CompositeFilterOperator.and(
//                        new PropertyFilter(InvitecodeUtil.GENERATOR_ID, FilterOperator.EQUAL, generatorId),
//                        new PropertyFilter(InvitecodeUtil.STATUS, FilterOperator.EQUAL, InvitecodeUtil.STATUS_C_UNUSED)
//                ));

        try {
            return invitecodeMapper.getByGeneratorIdAndStatus(generatorId, InvitecodeUtil.STATUS_C_UNUSED);
        } catch (final Exception e) {
            LOGGER.error( "Gets valid invitecode failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Gets an invitecode with the specified code.
     *
     * @param code the specified code
     * @return invitecode, returns {@code null} if not found
     */
    public Invitecode getInvitecode(final String code) {
//        final Query query = new Query().setFilter(new PropertyFilter(InvitecodeUtil.CODE, FilterOperator.EQUAL, code));

        try {
            final List<Invitecode> result = invitecodeMapper.getByCore(code);
//            final JSONArray codes = result.optJSONArray(Keys.RESULTS);
            if (0 == result.size()) {
                return null;
            }

            return result.get(0);
        } catch (final Exception e) {
            LOGGER.error( "Gets invitecode error", e);

            return null;
        }
    }

    /**
     * Gets invitecodes by the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,      <pre>
     *                                                   {
     *                                                       "paginationCurrentPageNum": 1,
     *                                                       "paginationPageSize": 20,
     *                                                       "paginationWindowSize": 10
     *                                                   </pre>
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "invitecodes": [{
     *         "oId": "",
     *         "code": "",
     *         "memo": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @throws Exception service exception
     */
    public JSONObject getInvitecodes(final JSONObject requestJSONObject) throws Exception {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
//        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
//                addSort(InvitecodeUtil.STATUS, SortDirection.DESCENDING).
//                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        PageHelper.startPage(currentPageNum,pageSize,"status DESC, OId DESC");

        PageInfo<Invitecode> result = null;

        try {
            result = new PageInfo<>( invitecodeMapper.getAll());
        } catch (final Exception e) {
            LOGGER.error( "Gets invitecodes failed", e);

            throw new Exception(e);
        }

        final int pageCount = result.getPages();

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final List<Invitecode> data = result.getList();
//        final List<JSONObject> invitecodes = CollectionUtils.<JSONObject>jsonArrayToList(data);

        ret.put(InvitecodeUtil.INVITECODES, data);

        return ret;
    }

    /**
     * Gets an invitecode by the specified invitecode id.
     *
     * @param invitecodeId the specified invitecode id
     * @return for example,      <pre>
     * {
     *     "oId": "",
     *     "code": "",
     *     "memo": "",
     *     ....
     * }
     * </pre>, returns {@code null} if not found
     * @throws Exception service exception
     */
    public Invitecode getInvitecodeById(final String invitecodeId) throws Exception {
        try {
            return invitecodeMapper.getByOId(invitecodeId);
        } catch (final Exception e) {
            LOGGER.error( "Gets an invitecode failed", e);

            throw new Exception(e);
        }
    }

    /**
     * Gets an invitecode by the specified user id.
     *
     * @param userId the specified user id
     * @return for example,      <pre>
     * {
     *     "oId": "",
     *     "code": "",
     *     "memo": "",
     *     ....
     * }
     * </pre>, returns {@code null} if not found
     * @throws Exception service exception
     */
    public Invitecode getInvitecodeByUserId(final String userId) throws Exception {
//        final Query query = new Query().setFilter(new PropertyFilter(InvitecodeUtil.USER_ID, FilterOperator.EQUAL, userId));

        try {
            final List<Invitecode> data = invitecodeMapper.getByUserId(userId);
            if (1 > data.size()) {
                return null;
            }

            return data.get(0);
        } catch (final Exception e) {
            LOGGER.error( "Gets an invitecode failed", e);

            throw new Exception(e);
        }
    }
}
