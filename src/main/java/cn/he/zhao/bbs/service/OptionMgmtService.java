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

import cn.he.zhao.bbs.entity.Option;
import cn.he.zhao.bbs.mapper.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

/**
 * OptionUtil management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Apr 5, 2016
 * @since 1.1.0
 */
@Service
public class OptionMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OptionMgmtService.class);

    /**
     * OptionUtil Mapper.
     */
    @Autowired
    private OptionMapper optionMapper;

    /**
     * Removes an option.
     *
     * @param id the specified option id
     */
    @Transactional
    public void removeOption(final String id) {
//        final Transaction transaction = optionMapper.beginTransaction();

        try {
            optionMapper.remove(id);

//            transaction.commit();
        } catch (final Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }

            LOGGER.error( "Removes an option failed", e);
        }
    }

    /**
     * Adds the specified option.
     *
     * @param option the specified option
     */
    @Transactional
    public void addOption(final Option option) {
//        final Transaction transaction = optionMapper.beginTransaction();

        try {
            optionMapper.add(option);

//            transaction.commit();
        } catch (final Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }

            LOGGER.error( "Adds an option failed", e);
        }
    }

    /**
     * Updates the specified option by the given option id.
     *
     * @param optionId the given option id
     * @param option the specified option
     * @throws Exception service exception
     */
    @Transactional
    public void updateOption(final String optionId, final Option option) throws Exception {
//        final Transaction transaction = optionMapper.beginTransaction();

        try {
            optionMapper.update(optionId, option);

//            transaction.commit();
        } catch (final Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }

            LOGGER.error( "Updates an option[id=" + optionId + "] failed", e);
            throw new Exception(e);
        }
    }
}
