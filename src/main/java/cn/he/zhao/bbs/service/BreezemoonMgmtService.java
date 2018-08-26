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
import cn.he.zhao.bbs.entity.Breezemoon;
import cn.he.zhao.bbs.entity.my.Keys;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

/**
 * Breezemoon management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, May 21, 2018
 * @since 2.8.0
 */
@Service
public class BreezemoonMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BreezemoonMgmtService.class);

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
     * Option query service.
     */
    @Autowired
    private OptionQueryService optionQueryService;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Adds a breezemoon with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          "breezemoonContent": "",
     *                          "breezemoonAuthorId": "",
     *                          "breezemoonIP": "",
     *                          "breezemoonUA": ""
     * @throws ServiceException service exception
     */
    @Transactional
    public void addBreezemoon(final JSONObject requestJSONObject) throws ServiceException {
        final String content = requestJSONObject.optString(Breezemoon.BREEZEMOON_CONTENT);
        if (optionQueryService.containReservedWord(content)) {
            throw new ServiceException(langPropsService.get("contentContainReservedWordLabel"));
        }
        final JSONObject bm = new JSONObject();
        bm.put(Breezemoon.BREEZEMOON_CONTENT, content);
        bm.put(Breezemoon.BREEZEMOON_AUTHOR_ID, requestJSONObject.optString(Breezemoon.BREEZEMOON_AUTHOR_ID));
        bm.put(Breezemoon.BREEZEMOON_IP, requestJSONObject.optString(Breezemoon.BREEZEMOON_IP));
        bm.put(Breezemoon.BREEZEMOON_UA, requestJSONObject.optString(Breezemoon.BREEZEMOON_UA));
        final long now = System.currentTimeMillis();
        bm.put(Breezemoon.BREEZEMOON_CREATED, now);
        bm.put(Breezemoon.BREEZEMOON_UPDATED, now);
        bm.put(Breezemoon.BREEZEMOON_STATUS, Breezemoon.BREEZEMOON_STATUS_C_VALID);

        try {
            breezemoonMapper.add(bm);
        } catch (final Exception e) {
            LOGGER.error( "Adds a breezemoon failed", e);

            throw new ServiceException(langPropsService.get("systemErrLabel"));
        }
    }

    /**
     * Updates a breezemoon with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          "oId": "",
     *                          "breezemoonContent": "",
     *                          "breezemoonAuthorId": "",
     *                          "breezemoonIP": "",
     *                          "breezemoonUA": "",
     *                          "breezemoonStatus": "" // optional, 0 as default
     * @throws ServiceException service exception
     */
    @Transactional
    public void updateBreezemoon(final JSONObject requestJSONObject) throws ServiceException {
        final String content = requestJSONObject.optString(Breezemoon.BREEZEMOON_CONTENT);
        if (optionQueryService.containReservedWord(content)) {
            throw new ServiceException(langPropsService.get("contentContainReservedWordLabel"));
        }

        final String id = requestJSONObject.optString(Keys.OBJECT_ID);
        JSONObject old;
        try {
            old = breezemoonMapper.get(id);
        } catch (final Exception e) {
            LOGGER.error( "Gets a breezemoon [id=" + id + "] failed", e);

            throw new ServiceException(langPropsService.get("systemErrLabel"));
        }

        if (null == old) {
            throw new ServiceException(langPropsService.get("queryFailedLabel"));
        }

        old.put(Breezemoon.BREEZEMOON_CONTENT, content);
        old.put(Breezemoon.BREEZEMOON_AUTHOR_ID, requestJSONObject.optString(Breezemoon.BREEZEMOON_AUTHOR_ID));
        old.put(Breezemoon.BREEZEMOON_IP, requestJSONObject.optString(Breezemoon.BREEZEMOON_IP));
        old.put(Breezemoon.BREEZEMOON_UA, requestJSONObject.optString(Breezemoon.BREEZEMOON_UA));
        old.put(Breezemoon.BREEZEMOON_STATUS, requestJSONObject.optInt(Breezemoon.BREEZEMOON_STATUS, Breezemoon.BREEZEMOON_STATUS_C_VALID));
        final long now = System.currentTimeMillis();
        old.put(Breezemoon.BREEZEMOON_UPDATED, now);

        try {
            breezemoonMapper.update(id, old);
        } catch (final Exception e) {
            LOGGER.error( "Updates a breezemoon failed", e);

            throw new ServiceException(langPropsService.get("systemErrLabel"));
        }
    }

    /**
     * Removes a breezemoon with the specified id.
     *
     * @param id the specified id
     * @throws ServiceException service exception
     */
    @Transactional
    public void removeBreezemoon(final String id) throws ServiceException {
        try {
            breezemoonMapper.remove(id);
        } catch (final Exception e) {
            LOGGER.error( "Removes a breezemoon [id=" + id + "] failed", e);

            throw new ServiceException(langPropsService.get("systemErrLabel"));
        }
    }
}
