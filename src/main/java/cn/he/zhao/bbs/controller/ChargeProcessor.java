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
import cn.he.zhao.bbs.entityUtil.UserExtUtil;
import cn.he.zhao.bbs.service.*;
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
 * Charge processor.
 * <ul>
 * <li>Shows point charge (/charge/point), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.2, Oct 26, 2016
 * @since 1.3.0
 */
@Controller
public class ChargeProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ChargeProcessor.class);

    /**
     * Data entity service.
     */
    @Autowired
    private DataModelService dataModelService;

    /**
     * Shows charge point.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/charge/point", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showChargePoint(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("charge-point.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final int avatarViewMode = (int) request.getAttribute(UserExtUtil.USER_AVATAR_VIEW_MODE);

        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        return "charge-point.ftl";
    }
}
