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

import cn.he.zhao.bbs.model.my.*;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

/**
 * Invitecode query service.
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
     * Invitecode Mapper.
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
    public List<JSONObject> getValidInvitecodes(final String generatorId) {
        final Query query = new Query().setFilter(
                CompositeFilterOperator.and(
                        new PropertyFilter(Invitecode.GENERATOR_ID, FilterOperator.EQUAL, generatorId),
                        new PropertyFilter(Invitecode.STATUS, FilterOperator.EQUAL, Invitecode.STATUS_C_UNUSED)
                ));

        try {
            return CollectionUtils.jsonArrayToList(invitecodeMapper.get(query).optJSONArray(Keys.RESULTS));
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
    public JSONObject getInvitecode(final String code) {
        final Query query = new Query().setFilter(new PropertyFilter(Invitecode.CODE, FilterOperator.EQUAL, code));

        try {
            final JSONObject result = invitecodeMapper.get(query);
            final JSONArray codes = result.optJSONArray(Keys.RESULTS);
            if (0 == codes.length()) {
                return null;
            }

            return codes.optJSONObject(0);
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
     *                                                   }, see {@link Pagination} for more details
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
     * @throws ServiceException service exception
     * @see Pagination
     */
    public JSONObject getInvitecodes(final JSONObject requestJSONObject) throws ServiceException {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                addSort(Invitecode.STATUS, SortDirection.DESCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        JSONObject result = null;

        try {
            result = invitecodeMapper.get(query);
        } catch (final MapperException e) {
            LOGGER.error( "Gets invitecodes failed", e);

            throw new ServiceException(e);
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> invitecodes = CollectionUtils.<JSONObject>jsonArrayToList(data);

        ret.put(Invitecode.INVITECODES, invitecodes);

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
     * @throws ServiceException service exception
     */
    public JSONObject getInvitecodeById(final String invitecodeId) throws ServiceException {
        try {
            return invitecodeMapper.get(invitecodeId);
        } catch (final MapperException e) {
            LOGGER.error( "Gets an invitecode failed", e);

            throw new ServiceException(e);
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
     * @throws ServiceException service exception
     */
    public JSONObject getInvitecodeByUserId(final String userId) throws ServiceException {
        final Query query = new Query().setFilter(new PropertyFilter(Invitecode.USER_ID, FilterOperator.EQUAL, userId));

        try {
            final JSONArray data = invitecodeMapper.get(query).optJSONArray(Keys.RESULTS);
            if (1 > data.length()) {
                return null;
            }

            return data.optJSONObject(0);
        } catch (final Exception e) {
            LOGGER.error( "Gets an invitecode failed", e);

            throw new ServiceException(e);
        }
    }
}
