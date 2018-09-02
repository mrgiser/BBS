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

import cn.he.zhao.bbs.spring.Stopwatchs;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

import java.util.Date;

/**
 * LivenessUtil query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Mar 23, 2016
 * @since 1.4.0
 */
@Service
public class LivenessQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LivenessQueryService.class);

    /**
     * LivenessUtil Mapper.
     */
    @Autowired
    private LivenessMapper livenessMapper;

    /**
     * Gets point of current liveness.
     *
     * @param userId the specified user id
     * @return point
     */
    public int getCurrentLivenessPoint(final String userId) {
        Stopwatchs.start("Gets liveness");
        try {
            final String date = DateFormatUtils.format(new Date(), "yyyyMMdd");

            try {
                final JSONObject liveness = livenessMapper.getByUserAndDate(userId, date);
                if (null == liveness) {
                    return 0;
                }

                return Liveness.calcPoint(liveness);
            } catch (final MapperException e) {
                LOGGER.error( "Gets current liveness point failed", e);

                return 0;
            }
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets the yesterday's liveness.
     *
     * @param userId the specified user id
     * @return yesterday's liveness, returns {@code null} if not found
     */
    public JSONObject getYesterdayLiveness(final String userId) {
        final Date yesterday = DateUtils.addDays(new Date(), -1);
        final String date = DateFormatUtils.format(yesterday, "yyyyMMdd");

        try {
            return livenessMapper.getByUserAndDate(userId, date);
        } catch (final MapperException e) {
            LOGGER.error( "Gets yesterday's liveness failed", e);

            return null;
        }
    }
}
