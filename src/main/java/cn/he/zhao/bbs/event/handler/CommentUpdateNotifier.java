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
package cn.he.zhao.bbs.event.handler;

import cn.he.zhao.bbs.event.UpdateCommentEvent;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class CommentUpdateNotifier implements ApplicationListener<UpdateCommentEvent> {

    /**
     * Logger.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(CommentNotifier.class);

    @Override
    public void onApplicationEvent(UpdateCommentEvent event) {
        final JSONObject data = event.event;
        LOGGER.trace("Processing an event [type={0}, data={1}]", data);
    }
}
