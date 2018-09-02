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

import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

/**
 * LivenessUtil management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Jun 12, 2018
 * @since 1.4.0
 */
@Service
public class LivenessMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LivenessMgmtService.class);

    /**
     * LivenessUtil Mapper.
     */
    @Autowired
    private LivenessMapper livenessMapper;

    /**
     * Increments a field of the specified liveness.
     *
     * @param userId the specified user id
     * @param field  the specified field
     */
    @Transactional
    public void incLiveness(final String userId, final String field) {
        Stopwatchs.start("Inc liveness");
        final String date = DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMdd");

        try {
            JSONObject liveness = livenessMapper.getByUserAndDate(userId, date);
            if (null == liveness) {
                liveness = new JSONObject();

                liveness.put(Liveness.LIVENESS_USER_ID, userId);
                liveness.put(Liveness.LIVENESS_DATE, date);
                liveness.put(Liveness.LIVENESS_POINT, 0);
                liveness.put(Liveness.LIVENESS_ACTIVITY, 0);
                liveness.put(Liveness.LIVENESS_ARTICLE, 0);
                liveness.put(Liveness.LIVENESS_COMMENT, 0);
                liveness.put(Liveness.LIVENESS_PV, 0);
                liveness.put(Liveness.LIVENESS_REWARD, 0);
                liveness.put(Liveness.LIVENESS_THANK, 0);
                liveness.put(Liveness.LIVENESS_VOTE, 0);
                liveness.put(Liveness.LIVENESS_VOTE, 0);
                liveness.put(Liveness.LIVENESS_ACCEPT_ANSWER, 0);

                livenessMapper.add(liveness);
            }

            liveness.put(field, liveness.optInt(field) + 1);

            livenessMapper.update(liveness.optString(Keys.OBJECT_ID), liveness);
        } catch (final MapperException e) {
            LOGGER.error( "Updates a liveness [" + date + "] field [" + field + "] failed", e);
        } finally {
            Stopwatchs.end();
        }
    }
}
