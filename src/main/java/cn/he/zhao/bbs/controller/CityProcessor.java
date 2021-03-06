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
import cn.he.zhao.bbs.entityUtil.ArticleUtil;
import cn.he.zhao.bbs.entityUtil.UserExtUtil;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.Pagination;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Common;
import cn.he.zhao.bbs.spring.Paginator;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.util.JsonUtil;
import cn.he.zhao.bbs.util.Symphonys;
import com.qiniu.util.Json;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.apache.commons.lang.time.DateUtils;
import org.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * City processor.
 * <ul>
 * <li>Shows city articles (/city/{city}), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 1.3.1.9, Apr 26, 2018
 * @since 1.3.0
 */
@Controller
public class CityProcessor {

    /**
     * Article query service.
     */
    @Autowired
    private ArticleQueryService articleQueryService;

    /**
     * Data entity service.
     */
    @Autowired
    private DataModelService dataModelService;

    /**
     * OptionUtil query service.
     */
    @Autowired
    private OptionQueryService optionQueryService;

    /**
     * Avatar query service.
     */
    @Autowired
    private AvatarQueryService avatarQueryService;

    /**
     * User query service.
     */
    @Autowired
    private UserQueryService userQueryService;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langService;

    /**
     * Shows city articles.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param city     the specified city
     * @throws Exception exception
     */
    @RequestMapping(value = {"/city/{city}", "/city/{city}/articles"}, method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @LoginCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showCityArticles(Map<String, Object> dataModel,
                                 final HttpServletRequest request, final HttpServletResponse response, final String city) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//
//        renderer.setTemplateName("city.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "city.ftl";
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        dataModel.put(Common.CURRENT, "");

        final int avatarViewMode = (int) request.getAttribute(UserExtUtil.USER_AVATAR_VIEW_MODE);

        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        List<JSONObject> articles = new ArrayList<>();
        dataModel.put(ArticleUtil.ARTICLES, articles); // an empty list to avoid null check in template
        dataModel.put(Common.SELECTED, Common.CITY);

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        UserExt userExt = JsonUtil.json2Bean(user.toString(),UserExt.class);
        if (!UserExtUtil.finshedGuide(userExt)) {
            return "redirect:" +( SpringUtil.getServerPath() + "/guide");

//            return;
        }

        dataModel.put(UserExtUtil.USER_GEO_STATUS, true);
        dataModel.put(Common.CITY_FOUND, true);
        dataModel.put(Common.CITY, langService.get("sameCityLabel"));

        if (UserExtUtil.USER_GEO_STATUS_C_PUBLIC != user.optInt(UserExtUtil.USER_GEO_STATUS)) {
            dataModel.put(UserExtUtil.USER_GEO_STATUS, false);

            return url;
        }

        final String userCity = user.optString(UserExtUtil.USER_CITY);

        String queryCity = city;
        if ("my".equals(city)) {
            dataModel.put(Common.CITY, userCity);
            queryCity = userCity;
        } else {
            dataModel.put(Common.CITY, city);
        }

        if (StringUtils.isBlank(userCity)) {
            dataModel.put(Common.CITY_FOUND, false);

            return url;
        }

        final int pageNum = Paginator.getPage(request);
        final int pageSize = user.optInt(UserExtUtil.USER_LIST_PAGE_SIZE);
        final int windowSize = Symphonys.getInt("cityArticlesWindowSize");

        final Option statistic = optionQueryService.getOption(queryCity + "-ArticleCount");
        if (null != statistic) {
            articles = articleQueryService.getArticlesByCity(avatarViewMode, queryCity, pageNum, pageSize);
            dataModel.put(ArticleUtil.ARTICLES, articles);
        }

        final int articleCnt = null == statistic ? 0 : Integer.getInteger(statistic.getOptionValue());
        final int pageCount = (int) Math.ceil(articleCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        return url;
    }

    /**
     * Shows city users.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param city     the specified city
     * @throws Exception exception
     */
    @RequestMapping(value = {"/city/{city}/users"}, method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @LoginCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showCityUsers(Map<String, Object> dataModel,
                              final HttpServletRequest request, final HttpServletResponse response, final String city) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//
//        renderer.setTemplateName("city.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "city.ftl";
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        dataModel.put(Common.CURRENT, "/users");

        final int avatarViewMode = (int) request.getAttribute(UserExtUtil.USER_AVATAR_VIEW_MODE);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        List<JSONObject> users = new ArrayList<>();
        dataModel.put(User.USERS, users);
        dataModel.put(Common.SELECTED, Common.CITY);

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        UserExt userExt = JsonUtil.json2Bean(user.toString(),UserExt.class);
        if (!UserExtUtil.finshedGuide(userExt)) {
            return "redirect:" +( SpringUtil.getServerPath() + "/guide");

//            return;
        }

        dataModel.put(UserExtUtil.USER_GEO_STATUS, true);
        dataModel.put(Common.CITY_FOUND, true);
        dataModel.put(Common.CITY, langService.get("sameCityLabel"));
        if (UserExtUtil.USER_GEO_STATUS_C_PUBLIC != user.optInt(UserExtUtil.USER_GEO_STATUS)) {
            dataModel.put(UserExtUtil.USER_GEO_STATUS, false);

            return url;
        }

        final String userCity = user.optString(UserExtUtil.USER_CITY);

        String queryCity = city;
        if ("my".equals(city)) {
            dataModel.put(Common.CITY, userCity);
            queryCity = userCity;
        } else {
            dataModel.put(Common.CITY, city);
        }

        if (StringUtils.isBlank(userCity)) {
            dataModel.put(Common.CITY_FOUND, false);

            return url;
        }

        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("cityUserPageSize");
        final int windowSize = Symphonys.getInt("cityUsersWindowSize");

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Keys.OBJECT_ID, user.optString(Keys.OBJECT_ID));
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);
        final long latestLoginTime = DateUtils.addDays(new Date(), Integer.MIN_VALUE).getTime(); // all users
        requestJSONObject.put(UserExtUtil.USER_LATEST_LOGIN_TIME, latestLoginTime);
        requestJSONObject.put(UserExtUtil.USER_CITY, queryCity);
        final JSONObject result = userQueryService.getUsersByCity(requestJSONObject);
        final JSONArray cityUsers = result.optJSONArray(User.USERS);
        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        if (null != cityUsers && cityUsers.length() > 0) {
            for (int i = 0; i < cityUsers.length(); i++) {
                users.add(cityUsers.getJSONObject(i));
            }
            dataModel.put(User.USERS, users);
        }

        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        return url;
    }
}
