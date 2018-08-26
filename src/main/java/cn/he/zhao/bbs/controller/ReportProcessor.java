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

import cn.he.zhao.bbs.util.StatusCodes;
import org.apache.commons.lang.StringUtils;
import cn.he.zhao.bbs.advice.*;
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.entity.my.*;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Report processor.
 * <ul>
 * <li>Reports content or users (/report), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jun 26, 2018
 * @since 3.1.0
 */
@Controller
public class ReportProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportProcessor.class);

    /**
     * Report management service.
     */
    @Autowired
    private ReportMgmtService reportMgmtService;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Reports content or users.
     *
     * @param request the specified request
     */
    @RequestMapping(value = "/report", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @StopWatchStartAnno
    @LoginCheckAnno
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchEndAnno
    public void report(Map<String, Object> dataModel, final HttpServletRequest request, final JSONObject requestJSONObject) {
        dataModel.put(Keys.STATUS_CODE,false);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final String dataId = requestJSONObject.optString(Report.REPORT_DATA_ID);
        final int dataType = requestJSONObject.optInt(Report.REPORT_DATA_TYPE);
        final int type = requestJSONObject.optInt(Report.REPORT_TYPE);
        final String memo = StringUtils.trim(requestJSONObject.optString(Report.REPORT_MEMO));

        final JSONObject report = new JSONObject();
        report.put(Report.REPORT_USER_ID, userId);
        report.put(Report.REPORT_DATA_ID, dataId);
        report.put(Report.REPORT_DATA_TYPE, dataType);
        report.put(Report.REPORT_TYPE, type);
        report.put(Report.REPORT_MEMO, memo);

        try {
            reportMgmtService.addReport(report);

            dataModel.put(Keys.STATUS_CODE, StatusCodes.SUCC);
        } catch ( final Exception e) {
            dataModel.put(Keys.MSG ,e.getMessage());
            dataModel.put(Keys.STATUS_CODE, StatusCodes.ERR);
        }
//        catch (final Exception e) {
//            LOGGER.error( "Adds a report failed", e);
//
//            dataModel.put(Keys.MSG ,langPropsService.get("systemErrLabel"));
//            dataModel.put(Keys.STATUS_CODE, StatusCodes.ERR);
//        }
    }

}
