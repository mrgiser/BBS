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

import cn.he.zhao.bbs.advice.*;
import cn.he.zhao.bbs.exception.RequestProcessAdviceException;
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.spring.Requests;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.util.Networks;
import cn.he.zhao.bbs.util.Symphonys;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * LinkUtil forge processor.
 * <ul>
 * <li>Shows link forge (/link-forge), GET</li>
 * <li>Submits a link into forge (/forge/link), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.1.0.8, Jun 15, 2017
 * @since 1.6.0
 */
@Controller
public class LinkForgeProcessor {

    /**
     * Forge thread.
     */
    private static final ExecutorService FORGE_EXECUTOR_SERVICE = Executors.newFixedThreadPool(1);

    /**
     * LinkUtil forget management service.
     */
    @Autowired
    private LinkForgeMgmtService linkForgeMgmtService;

    /**
     * LinkUtil forge query service.
     */
    @Autowired
    private LinkForgeQueryService linkForgeQueryService;

    /**
     * OptionUtil query service.
     */
    @Autowired
    private OptionQueryService optionQueryService;

    /**
     * Data entity service.
     */
    @Autowired
    private DataModelService dataModelService;

    /**
     * Submits a link into forge.
     *

     * @throws Exception exception
     */
    @RequestMapping(value = "/forge/link", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @LoginCheckAnno
    @StopWatchEndAnno
    public void forgeLink(Map<String, Object> dataModel,HttpServletResponse response, HttpServletRequest request) throws Exception {
        dataModel.put(Keys.STATUS_CODE,true);

        JSONObject requestJSONObject;
        try {
            requestJSONObject = Requests.parseRequestJSONObject(request, response);
        } catch (final Exception e) {
            throw new RequestProcessAdviceException(new JSONObject().put(Keys.MSG, e.getMessage()));
        }

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        final String userId = user.optString(Keys.OBJECT_ID);

        final String url = requestJSONObject.optString(Common.URL);

        FORGE_EXECUTOR_SERVICE.submit(() -> linkForgeMgmtService.forge(url, userId));
    }

    /**
     * Shows link forge.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/forge/link", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showLinkForge(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//
//        renderer.setTemplateName("other/link-forge.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "other/link-forge.ftl";

        final List<JSONObject> tags = linkForgeQueryService.getForgedLinks();
        dataModel.put(Tag.TAGS, (Object) tags);

        dataModel.put(Common.SELECTED, Common.FORGE);

        final JSONObject statistic = optionQueryService.getStatistic();
        final int tagCnt = statistic.optInt(Option.ID_C_STATISTIC_TAG_COUNT);
        dataModel.put(Tag.TAG_T_COUNT, tagCnt);

        final int linkCnt = statistic.optInt(Option.ID_C_STATISTIC_LINK_COUNT);
        dataModel.put(Link.LINK_T_COUNT, linkCnt);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Purges link forge.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/cron/forge/link/purge", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class})
//    @After(adviceClass = {StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @StopWatchEndAnno
    public void purgeLinkForge(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        if (SpringUtil.getServerPath().contains("localhost") || Networks.isIPv4(SpringUtil.getServerHost())
                || SpringUtil.RuntimeMode.DEV ==(SpringUtil.getRuntimeMode())) {
            response.sendError(HttpServletResponse.SC_OK);

            return;
        }

        linkForgeMgmtService.purge();

        dataModel.put(Keys.STATUS_CODE,true);
    }
}
