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
import cn.he.zhao.bbs.model.*;
import cn.he.zhao.bbs.model.my.*;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Locales;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Error processor.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.2.0.10, Jun 2, 2018
 * @since 0.2.0
 */
@Controller
public class ErrorProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorProcessor.class);

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Data model service.
     */
    @Autowired
    private DataModelService dataModelService;

    /**
     * Handles the error.
     *
     * @param request    the specified HTTP servlet request
     * @param response   the specified HTTP servlet response
     * @param statusCode the specified status code
     * @throws Exception exception
     */
    @RequestMapping(value = "/error/{statusCode}", method = {RequestMethod.GET, RequestMethod.HEAD})
//    @Before(adviceClass = StopwatchStartAdvice.class)
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String handleErrorPage(Map<String, Object> dataModel, final HttpServletRequest request,
                                final HttpServletResponse response, final String statusCode) throws Exception {
        if (StringUtils.equals("GET", request.getMethod())) {
            final String requestURI = request.getRequestURI();
            final String templateName = statusCode + ".ftl";
            LOGGER.trace( "Shows error page[requestURI={0}, templateName={1}]", requestURI, templateName);

//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            renderer.setTemplateName("error/" + templateName);
//            context.setRenderer(renderer);
//
//            final Map<String, Object> dataModel = renderer.getDataModel();

            String url = "error/" + templateName;
            dataModel.putAll(langPropsService.getAll(Locales.getLocale()));
            dataModelService.fillHeaderAndFooter(request, response, dataModel);
            dataModelService.fillSideHotArticles(dataModel);
            dataModelService.fillRandomArticles(dataModel);
            dataModelService.fillSideTags(dataModel);
            return url;
        } else {
//            context.renderJSON().renderMsg(statusCode);
            dataModel.put(Keys.STATUS_CODE,false);
            dataModel.put(Keys.MSG,statusCode);
            return null;
        }
    }
}
