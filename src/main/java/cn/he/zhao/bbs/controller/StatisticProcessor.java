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
import cn.he.zhao.bbs.util.Times;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
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
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Data statistic processor.
 * <ul>
 * <li>Shows data statistic (/statistic), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.2.1.0, Apr 4, 2018
 * @since 1.4.0
 */
@Controller
public class StatisticProcessor {

    /**
     * Month days.
     */
    private final List<String> monthDays = new ArrayList<>();

    /**
     * User counts.
     */
    private final List<Integer> userCnts = new ArrayList<>();

    /**
     * Article counts.
     */
    private final List<Integer> articleCnts = new ArrayList<>();

    /**
     * Comment counts.
     */
    private final List<Integer> commentCnts = new ArrayList<>();

    /**
     * History months.
     */
    private final List<String> months = new ArrayList<>();

    /**
     * History user counts.
     */
    private final List<Integer> historyUserCnts = new ArrayList<>();

    /**
     * History article counts.
     */
    private final List<Integer> historyArticleCnts = new ArrayList<>();

    /**
     * History comment counts.
     */
    private final List<Integer> historyCommentCnts = new ArrayList<>();

    /**
     * User query service.
     */
    @Autowired
    private UserQueryService userQueryService;

    /**
     * Article query service.
     */
    @Autowired
    private ArticleQueryService articleQueryService;

    /**
     * Comment query service.
     */
    @Autowired
    private CommentQueryService commentQueryService;

    /**
     * Option query service.
     */
    @Autowired
    private OptionQueryService optionQueryService;

    /**
     * Data model service.
     */
    @Autowired
    private DataModelService dataModelService;

    /**
     * Loads statistic data.
     *
     * @param request  the specified HTTP servlet request
     * @param response the specified HTTP servlet response

     * @throws Exception exception
     */
    @RequestMapping(value = "/cron/stat", method = RequestMethod.GET)
//    @Before(adviceClass = StopwatchStartAdvice.class)
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @StopWatchEndAnno
    public void loadStatData(final HttpServletRequest request, final HttpServletResponse response, Map<String, Object> dataModel)
            throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final Date end = new Date();
        final Date dayStart = DateUtils.addDays(end, -30);

        monthDays.clear();
        userCnts.clear();
        articleCnts.clear();
        commentCnts.clear();
        months.clear();
        historyArticleCnts.clear();
        historyCommentCnts.clear();
        historyUserCnts.clear();

        for (int i = 0; i < 31; i++) {
            final Date day = DateUtils.addDays(dayStart, i);
            monthDays.add(DateFormatUtils.format(day, "yyyy-MM-dd"));

            final int userCnt = userQueryService.getUserCntInDay(day);
            userCnts.add(userCnt);

            final int articleCnt = articleQueryService.getArticleCntInDay(day);
            articleCnts.add(articleCnt);

            final int commentCnt = commentQueryService.getCommentCntInDay(day);
            commentCnts.add(commentCnt);
        }

        final JSONObject firstAdmin = userQueryService.getAdmins().get(0);
        final long monthStartTime = Times.getMonthStartTime(firstAdmin.optLong(Keys.OBJECT_ID));
        final Date monthStart = new Date(monthStartTime);

        int i = 1;
        while (true) {
            final Date month = DateUtils.addMonths(monthStart, i);

            if (month.after(end)) {
                break;
            }

            i++;

            months.add(DateFormatUtils.format(month, "yyyy-MM"));

            final int userCnt = userQueryService.getUserCntInMonth(month);
            historyUserCnts.add(userCnt);

            final int articleCnt = articleQueryService.getArticleCntInMonth(month);
            historyArticleCnts.add(articleCnt);

            final int commentCnt = commentQueryService.getCommentCntInMonth(month);
            historyCommentCnts.add(commentCnt);
        }

        dataModel.put(Keys.STATUS_CODE,true);
    }

    /**
     * Shows data statistic.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/statistic", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showStatistic(Map<String, Object> dataModel,
                              final HttpServletRequest request, final HttpServletResponse response) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("statistic.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "statistic.ftl";

        dataModel.put("monthDays", monthDays);
        dataModel.put("userCnts", userCnts);
        dataModel.put("articleCnts", articleCnts);
        dataModel.put("commentCnts", commentCnts);

        dataModel.put("months", months);
        dataModel.put("historyUserCnts", historyUserCnts);
        dataModel.put("historyArticleCnts", historyArticleCnts);
        dataModel.put("historyCommentCnts", historyCommentCnts);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        dataModel.put(Common.ONLINE_VISITOR_CNT, optionQueryService.getOnlineVisitorCount());
        dataModel.put(Common.ONLINE_MEMBER_CNT, optionQueryService.getOnlineMemberCount());

        final JSONObject statistic = optionQueryService.getStatistic();
        dataModel.put(Option.CATEGORY_C_STATISTIC, statistic);

        return url;
    }
}
