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

/**
 * Top ranking list processor.
 * <ul>
 * <li>Top balance (/top/balance), GET</li>
 * <li>Top consumption (/top/consumption), GET</li>
 * <li>Top checkin (/top/checkin), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.0.3, Jun 24, 2018
 * @since 1.3.0
 */
@Controller
public class TopProcessor {

    /**
     * Data entity service.
     */
    @Autowired
    private DataModelService dataModelService;

    /**
     * Pointtransfer query service.
     */
    @Autowired
    private PointtransferQueryService pointtransferQueryService;

    /**
     * Activity query service.
     */
    @Autowired
    private ActivityQueryService activityQueryService;

    /**
     * Shows balance ranking list.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/top/balance", method = RequestMethod.GET)
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showBalance(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/top/balance.ftl");
//
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "/top/balance.ftl";
        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        final List<JSONObject> users = pointtransferQueryService.getTopBalanceUsers(avatarViewMode, Symphonys.getInt("topBalanceCnt"));
        dataModel.put(Common.TOP_BALANCE_USERS, users);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);
        return url;
    }

    /**
     * Shows consumption ranking list.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/top/consumption", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showConsumption(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/top/consumption.ftl");
//
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "/top/consumption.ftl";
        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        final List<JSONObject> users = pointtransferQueryService.getTopConsumptionUsers(avatarViewMode, Symphonys.getInt("topConsumptionCnt"));
        dataModel.put(Common.TOP_CONSUMPTION_USERS, users);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);
        return url;
    }

    /**
     * Shows checkin ranking list.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/top/checkin", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showCheckin(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/top/checkin.ftl");
//
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "/top/checkin.ftl";
        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        final List<JSONObject> users = activityQueryService.getTopCheckinUsers(avatarViewMode, Symphonys.getInt("topCheckinCnt"));
        dataModel.put(Common.TOP_CHECKIN_USERS, users);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);
        return url;
    }
}
