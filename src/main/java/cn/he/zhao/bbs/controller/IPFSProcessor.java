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

import cn.he.zhao.bbs.util.Symphonys;
import org.apache.commons.lang.StringUtils;
import cn.he.zhao.bbs.advice.*;
import cn.he.zhao.bbs.model.*;
import cn.he.zhao.bbs.model.my.*;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import org.apache.commons.lang.StringUtils;
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
 * IPFS(https://ipfs.io) processor.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.2, Feb 3, 2018
 * @since 2.3.0
 */
@Controller
public class IPFSProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(IPFSProcessor.class);

    /**
     * Publishes article markdown files to IPFS.
     *
     * @param request  the specified HTTP servlet request
     * @param response the specified HTTP servlet response

     * @throws Exception exception
     */
    @RequestMapping(value = "/cron/ipfs/articles/publish", method = RequestMethod.GET)
//    @Before(adviceClass = StopwatchStartAdvice.class)
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @StopWatchEndAnno
    public void publishArticles(final HttpServletRequest request, final HttpServletResponse response, Map<String, Object> dataModel)
            throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        dataModel.put(Keys.STATUS_CODE,true);

        final String dir = Symphonys.get("ipfs.dir");
        final String bin = Symphonys.get("ipfs.bin");
        if (StringUtils.isBlank(dir) || StringUtils.isBlank(bin)) {
            return;
        }

        LOGGER.info( "Adding articles to IPFS");
        String output = Execs.exec(bin + " add -r " + dir);
        if (StringUtils.isBlank(output) || !StringUtils.containsIgnoreCase(output, "added")) {
            LOGGER.error( "Executes [ipfs add] failed: " + output);

            return;
        }
        LOGGER.info( "Publishing articles to IPFS");
        final String[] lines = output.split("\n");
        final String lastLine = lines[lines.length - 1];
        final String hash = lastLine.split(" ")[1];
        output = Execs.exec(bin + " name publish " + hash);
        if (StringUtils.isBlank(output) || !StringUtils.containsIgnoreCase(output, "published")) {
            LOGGER.error( "Executes [ipfs name publish] failed: " + output);

            return;
        }
        LOGGER.info( "Published articles to IPFS");
    }
}
