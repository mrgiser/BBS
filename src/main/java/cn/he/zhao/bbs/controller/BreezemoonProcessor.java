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
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Paginator;
import cn.he.zhao.bbs.spring.Requests;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.util.Headers;
import cn.he.zhao.bbs.util.Sessions;
import cn.he.zhao.bbs.util.StatusCodes;
import cn.he.zhao.bbs.util.Symphonys;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Breezemoon processor. https://github.com/b3log/symphony/issues/507
 *
 * <ul>
 * <li>Shows watch breezemoons (/watch/breezemoons), GET</li>
 * <li>Adds a breezemoon (/breezemoon), POST</li>
 * <li>Updates a breezemoon (/breezemoon/{id}), PUT</li>
 * <li>Removes a breezemoon (/breezemoon/{id}), DELETE</li>
 * <li>Shows a breezemoon (/breezemoon/{id}), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, May 23, 2018
 * @since 2.8.0
 */
@Controller
public class BreezemoonProcessor {

    /**
     * Breezemoon query service.
     */
    @Autowired
    private BreezemoonQueryService breezemoonQueryService;

    /**
     * Breezemoon management service.
     */
    @Autowired
    private BreezemoonMgmtService breezemoonMgmtService;

    /**
     * Data entity service.
     */
    @Autowired
    private DataModelService dataModelService;

    /**
     * Optiona query service.
     */
    @Autowired
    private OptionQueryService optionQueryService;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Shows breezemoon page.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/watch/breezemoons", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
//    @After(adviceClass = {CSRFToken.class, PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @CSRFTokenAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showWatchBreezemoon(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("breezemoon.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "breezemoon.ftl";
        final int pageNum = Paginator.getPage(request);
        int pageSize = Symphonys.getInt("indexArticlesCnt");
        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        final JSONObject user = Sessions.currentUser(request);
        String currentUserId = null;
        if (null != user) {
            pageSize = user.optInt(UserExt.USER_LIST_PAGE_SIZE);

            if (!UserExt.finshedGuide(user)) {
                return "redirect:" +( SpringUtil.getServerPath() + "/guide");

//                return;
            }

            currentUserId = user.optString(Keys.OBJECT_ID);
        }

        final int windowSize = Symphonys.getInt("latestArticlesWindowSize");
        final JSONObject result = breezemoonQueryService.getFollowingUserBreezemoons(avatarViewMode, currentUserId, pageNum, pageSize, windowSize);
        final List<JSONObject> bms = (List<JSONObject>) result.opt(Breezemoon.BREEZEMOONS);
        dataModel.put(Common.WATCHING_BREEZEMOONS, bms);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        dataModel.put(Common.SELECTED, Common.WATCH);
        dataModel.put(Common.CURRENT, StringUtils.substringAfter(request.getRequestURI(), "/watch"));
        return url;
    }

    /**
     * Adds a breezemoon.
     * <p>
     * The request json object (breezemoon):
     * <pre>
     * {
     *   "breezemoonContent": ""
     * }
     * </pre>
     * </p>
     *
     * @param request the specified request
     */
    @RequestMapping(value = "/breezemoon", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, CSRFCheck.class, PermissionCheck.class})
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @LoginCheckAnno
    @CSRFTokenAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public void addBreezemoon(Map<String, Object> dataModel, final HttpServletRequest request, final JSONObject requestJSONObject) {
        dataModel.put(Keys.STATUS_CODE,false);

        if (isInvalid(dataModel, requestJSONObject)) {
            return;
        }

        final JSONObject breezemoon = new JSONObject();
        final String breezemoonContent = requestJSONObject.optString(Breezemoon.BREEZEMOON_CONTENT);
        breezemoon.put(Breezemoon.BREEZEMOON_CONTENT, breezemoonContent);
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        final String authorId = user.optString(Keys.OBJECT_ID);
        breezemoon.put(Breezemoon.BREEZEMOON_AUTHOR_ID, authorId);
        final String ip = Requests.getRemoteAddr(request);
        breezemoon.put(Breezemoon.BREEZEMOON_IP, ip);
        final String ua = Headers.getHeader(request, Common.USER_AGENT);
        breezemoon.put(Breezemoon.BREEZEMOON_UA, ua);

        try {
            breezemoonMgmtService.addBreezemoon(breezemoon);

            dataModel.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            dataModel.put(Keys.MSG ,e.getMessage());
            dataModel.put(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    /**
     * Updates a breezemoon.
     * <p>
     * The request json object (breezemoon):
     * <pre>
     * {
     *   "breezemoonContent": ""
     * }
     * </pre>
     * </p>
     *
     * @param request the specified request
     */
    @RequestMapping(value = "/breezemoon/{id}", method = RequestMethod.PUT)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, CSRFCheck.class, PermissionCheck.class})
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @LoginCheckAnno
    @CSRFTokenAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public void updateBreezemoon(Map<String, Object> dataModel, final HttpServletRequest request, final JSONObject requestJSONObject,
                                 final String id) {
        dataModel.put(Keys.STATUS_CODE,false);
        if (isInvalid(dataModel, requestJSONObject)) {
            return;
        }

        final JSONObject breezemoon = new JSONObject();
        breezemoon.put(Keys.OBJECT_ID, id);
        final String breezemoonContent = requestJSONObject.optString(Breezemoon.BREEZEMOON_CONTENT);
        breezemoon.put(Breezemoon.BREEZEMOON_CONTENT, breezemoonContent);
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        final String authorId = user.optString(Keys.OBJECT_ID);
        breezemoon.put(Breezemoon.BREEZEMOON_AUTHOR_ID, authorId);
        final String ip = Requests.getRemoteAddr(request);
        breezemoon.put(Breezemoon.BREEZEMOON_IP, ip);
        final String ua = Headers.getHeader(request, Common.USER_AGENT);
        breezemoon.put(Breezemoon.BREEZEMOON_UA, ua);

        try {
            breezemoonMgmtService.updateBreezemoon(breezemoon);

            dataModel.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            dataModel.put(Keys.MSG ,e.getMessage());
            dataModel.put(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    /**
     * Removes a breezemoon.
     *
     * @param request the specified request
     */
    @RequestMapping(value = "/breezemoon/{id}", method = RequestMethod.DELETE)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, CSRFCheck.class, PermissionCheck.class})
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @LoginCheckAnno
    @CSRFTokenAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public void removeBreezemoon(Map<String, Object> dataModel, final HttpServletRequest request, final JSONObject requestJSONObject,
                                 final String id) {
        dataModel.put(Keys.STATUS_CODE,false);

        try {
            breezemoonMgmtService.removeBreezemoon(id);

            dataModel.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch (final Exception e) {
            dataModel.put(Keys.MSG ,e.getMessage());
            dataModel.put(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    private boolean isInvalid(Map<String, Object> dataModel, final JSONObject requestJSONObject) {
        String breezemoonContent = requestJSONObject.optString(Breezemoon.BREEZEMOON_CONTENT);
        breezemoonContent = StringUtils.trim(breezemoonContent);
        final int length = StringUtils.length(breezemoonContent);
        if (1 > length || 512 < length) {
            dataModel.put(Keys.MSG ,langPropsService.get("breezemoonLengthLabel"));
            dataModel.put(Keys.STATUS_CODE, StatusCodes.ERR);

            return true;
        }

        if (optionQueryService.containReservedWord(breezemoonContent)) {
            dataModel.put(Keys.MSG ,langPropsService.get("contentContainReservedWordLabel"));
            dataModel.put(Keys.STATUS_CODE, StatusCodes.ERR);

            return true;
        }

        requestJSONObject.put(Breezemoon.BREEZEMOON_CONTENT, breezemoonContent);

        return false;
    }
}
