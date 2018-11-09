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
package cn.he.zhao.bbs.controller;

import cn.he.zhao.bbs.channel.ArticleChannel;
import cn.he.zhao.bbs.channel.ArticleListChannel;
import cn.he.zhao.bbs.channel.ChatRoomChannel;
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.spring.Common;
import cn.he.zhao.bbs.util.Symphonys;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Running status processor.
 * <ul>
 * <li>ReportUtil running status (/cron/status), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.4, Apr 3, 2018
 * @since 1.3.0
 */
@Controller
public class StatusProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(StatusProcessor.class);

    /**
     * OptionUtil query service.
     */
    @Autowired
    private OptionQueryService optionQueryService;

    /**
     * Reports running status.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/cron/status", method = RequestMethod.GET)
    public void reportStatus(Map<String, Object> dataModel,
                             final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

//        final JSONObject ret = new JSONObject();
//        context.renderJSON(ret);
        dataModel.put(Common.ONLINE_VISITOR_CNT, optionQueryService.getOnlineVisitorCount());
        dataModel.put(Common.ONLINE_MEMBER_CNT, optionQueryService.getOnlineMemberCount());
        dataModel.put(Common.ONLINE_CHAT_CNT, ChatRoomChannel.SESSIONS.size());
        dataModel.put(Common.ARTICLE_CHANNEL_CNT, ArticleChannel.SESSIONS.size());
        dataModel.put(Common.ARTICLE_LIST_CHANNEL_CNT, ArticleListChannel.SESSIONS.size());

        final JSONObject memory = new JSONObject();
        dataModel.put("memory", memory);

        final int mb = 1024 * 1024;
        final Runtime runtime = Runtime.getRuntime();
        memory.put("total", runtime.totalMemory() / mb);
        memory.put("free", runtime.freeMemory() / mb);
        memory.put("used", (runtime.totalMemory() - runtime.freeMemory()) / mb);
        memory.put("max", runtime.maxMemory() / mb);

        // TODO: 2018/8/20  dataModel.toString
//        LOGGER.info(dataModel.toString(SpringUtil.JSON_PRINT_INDENT_FACTOR));
        dataModel.put(Keys.STATUS_CODE, true);
    }
}
