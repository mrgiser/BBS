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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

/**
 * PointtransferUtil management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.1.5, Jan 29, 2018
 * @since 1.3.0
 */
@Service
public class PointtransferMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PointtransferMgmtService.class);

    /**
     * PointtransferUtil Mapper.
     */
    @Autowired
    private PointtransferMapper pointtransferMapper;

    /**
     * User Mapper.
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * Transfers point from the specified from id to the specified to id with type, sum, data id and time.
     *
     * @param fromId the specified from id, may be system "sys"
     * @param toId   the specified to id, may be system "sys"
     * @param type   the specified type
     * @param sum    the specified sum
     * @param dataId the specified data id
     * @param time   the specified time
     * @return transfer record id, returns {@code null} if transfer failed
     */
    public synchronized String transfer(final String fromId, final String toId, final int type, final int sum,
                                        final String dataId, final long time) {
        if (StringUtils.equals(fromId, toId)) { // for example the commenter is the article author
            return null;
        }

        final Transaction transaction = pointtransferMapper.beginTransaction();
        try {
            int fromBalance = 0;
            if (!Pointtransfer.ID_C_SYS.equals(fromId)) {
                final JSONObject fromUser = userMapper.get(fromId);
                fromBalance = fromUser.optInt(UserExt.USER_POINT) - sum;
                if (fromBalance < 0) {
                    throw new Exception("Insufficient balance");
                }

                fromUser.put(UserExt.USER_POINT, fromBalance);
                fromUser.put(UserExt.USER_USED_POINT, fromUser.optInt(UserExt.USER_USED_POINT) + sum);
                userMapper.update(fromId, fromUser);
            }

            int toBalance = 0;
            if (!Pointtransfer.ID_C_SYS.equals(toId)) {
                final JSONObject toUser = userMapper.get(toId);
                toBalance = toUser.optInt(UserExt.USER_POINT) + sum;
                toUser.put(UserExt.USER_POINT, toBalance);

                userMapper.update(toId, toUser);
            }

            final JSONObject pointtransfer = new JSONObject();
            pointtransfer.put(Pointtransfer.FROM_ID, fromId);
            pointtransfer.put(Pointtransfer.TO_ID, toId);
            pointtransfer.put(Pointtransfer.SUM, sum);
            pointtransfer.put(Pointtransfer.FROM_BALANCE, fromBalance);
            pointtransfer.put(Pointtransfer.TO_BALANCE, toBalance);
            pointtransfer.put(Pointtransfer.TIME, time);
            pointtransfer.put(Pointtransfer.TYPE, type);
            pointtransfer.put(Pointtransfer.DATA_ID, dataId);

            final String ret = pointtransferMapper.add(pointtransfer);

            transaction.commit();

            return ret;
        } catch (final Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.error( "Transfer [fromId=" + fromId + ", toId=" + toId + ", sum=" + sum + ", type=" +
                    type + ", dataId=" + dataId + "] error", e);

            return null;
        }
    }
}
