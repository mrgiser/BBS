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
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.Breezemoon;
import cn.he.zhao.bbs.entityUtil.my.Keys;
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
     * OptionUtil query service.
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
     * @throws Exception service exception
     */
    @Transactional
    public void addBreezemoon(final JSONObject requestJSONObject) throws Exception {
        final String content = requestJSONObject.optString(BreezemoonUtil.BREEZEMOON_CONTENT);
        if (optionQueryService.containReservedWord(content)) {
            throw new Exception(langPropsService.get("contentContainReservedWordLabel"));
        }
        final Breezemoon bm = new Breezemoon();
        bm.setBreezemoonContent( content);
        bm.setBreezemoonAuthorId( requestJSONObject.optString(BreezemoonUtil.BREEZEMOON_AUTHOR_ID));
        bm.setBreezemoonIP( requestJSONObject.optString(BreezemoonUtil.BREEZEMOON_IP));
        bm.setBreezemoonUA( requestJSONObject.optString(BreezemoonUtil.BREEZEMOON_UA));
        final long now = System.currentTimeMillis();
        bm.setBreezemoonCreated( now);
        bm.setBreezemoonUpdated( now);
        bm.setBreezemoonStatus( BreezemoonUtil.BREEZEMOON_STATUS_C_VALID);

        try {
            breezemoonMapper.add(bm);
        } catch (final Exception e) {
            LOGGER.error( "Adds a breezemoon failed", e);

            throw new Exception(langPropsService.get("systemErrLabel"));
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
     * @throws Exception service exception
     */
    @Transactional
    public void updateBreezemoon(final JSONObject requestJSONObject) throws Exception {
        final String content = requestJSONObject.optString(BreezemoonUtil.BREEZEMOON_CONTENT);
        if (optionQueryService.containReservedWord(content)) {
            throw new Exception(langPropsService.get("contentContainReservedWordLabel"));
        }

        final String id = requestJSONObject.optString(Keys.OBJECT_ID);
        Breezemoon old;
        try {
            old = breezemoonMapper.get(id);
        } catch (final Exception e) {
            LOGGER.error( "Gets a breezemoon [id=" + id + "] failed", e);

            throw new Exception(langPropsService.get("systemErrLabel"));
        }

        if (null == old) {
            throw new Exception(langPropsService.get("queryFailedLabel"));
        }

        old.setBreezemoonContent( content);
        old.setBreezemoonAuthorId( requestJSONObject.optString(BreezemoonUtil.BREEZEMOON_AUTHOR_ID));
        old.setBreezemoonIP( requestJSONObject.optString(BreezemoonUtil.BREEZEMOON_IP));
        old.setBreezemoonUA( requestJSONObject.optString(BreezemoonUtil.BREEZEMOON_UA));
        old.setBreezemoonStatus( requestJSONObject.optInt(BreezemoonUtil.BREEZEMOON_STATUS, BreezemoonUtil.BREEZEMOON_STATUS_C_VALID));
        final long now = System.currentTimeMillis();
        old.setBreezemoonUpdated( now);

        try {
            breezemoonMapper.update(id, old);
        } catch (final Exception e) {
            LOGGER.error( "Updates a breezemoon failed", e);

            throw new Exception(langPropsService.get("systemErrLabel"));
        }
    }

    /**
     * Removes a breezemoon with the specified id.
     *
     * @param id the specified id
     * @throws Exception service exception
     */
    @Transactional
    public void removeBreezemoon(final String id) throws Exception {
        try {
            breezemoonMapper.remove(id);
        } catch (final Exception e) {
            LOGGER.error( "Removes a breezemoon [id=" + id + "] failed", e);

            throw new Exception(langPropsService.get("systemErrLabel"));
        }
    }
}
