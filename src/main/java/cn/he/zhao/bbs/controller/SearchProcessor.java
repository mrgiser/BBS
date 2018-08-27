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

import cn.he.zhao.bbs.spring.Paginator;
import cn.he.zhao.bbs.util.Escapes;
import cn.he.zhao.bbs.util.Symphonys;
import org.apache.commons.lang.StringUtils;
import cn.he.zhao.bbs.advice.*;
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Search processor.
 * <ul>
 * <li>Searches keyword (/search), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.4.1, Dec 8, 2017
 * @since 1.4.0
 */
@Controller
public class SearchProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchProcessor.class);

    /**
     * Search query service.
     */
    @Autowired
    private SearchQueryService searchQueryService;

    /**
     * Article query service.
     */
    @Autowired
    private ArticleQueryService articleQueryService;

    /**
     * User query service.
     */
    @Autowired
    private UserQueryService userQueryService;

    /**
     * Data entity service.
     */
    @Autowired
    private DataModelService dataModelService;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Searches.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/search", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String search(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("search-articles.ftl");
        String url = "search-articles.ftl";

        if (!Symphonys.getBoolean("es.enabled") && !Symphonys.getBoolean("algolia.enabled")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return null;
        }

//        final Map<String, Object> dataModel = renderer.getDataModel();
        String keyword = request.getParameter("key");
        if (StringUtils.isBlank(keyword)) {
            keyword = "";
        }
        dataModel.put(Common.KEY, Escapes.escapeHTML(keyword));

        final int pageNum = Paginator.getPage(request);
        int pageSize = Symphonys.getInt("indexArticlesCnt");
        final JSONObject user = userQueryService.getCurrentUser(request);
        if (null != user) {
            pageSize = user.optInt(UserExt.USER_LIST_PAGE_SIZE);
        }
        final List<JSONObject> articles = new ArrayList<>();
        int total = 0;

        if (Symphonys.getBoolean("es.enabled")) {
            final JSONObject result = searchQueryService.searchElasticsearch(Article.ARTICLE, keyword, pageNum, pageSize);
            if (null == result || 0 != result.optInt("status")) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);

                return null;
            }

            final JSONObject hitsResult = result.optJSONObject("hits");
            final JSONArray hits = hitsResult.optJSONArray("hits");

            for (int i = 0; i < hits.length(); i++) {
                final JSONObject article = hits.optJSONObject(i).optJSONObject("_source");
                articles.add(article);
            }

            total = result.optInt("total");
        }

        if (Symphonys.getBoolean("algolia.enabled")) {
            final JSONObject result = searchQueryService.searchAlgolia(keyword, pageNum, pageSize);
            if (null == result) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);

                return null;
            }

            final JSONArray hits = result.optJSONArray("hits");

            for (int i = 0; i < hits.length(); i++) {
                final JSONObject article = hits.optJSONObject(i);
                articles.add(article);
            }

            total = result.optInt("nbHits");
            if (total > 1000) {
                total = 1000; // Algolia limits the maximum number of search results to 1000
            }
        }

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        articleQueryService.organizeArticles(avatarViewMode, articles);
        final Integer participantsCnt = Symphonys.getInt("latestArticleParticipantsCnt");
        articleQueryService.genParticipants(avatarViewMode, articles, participantsCnt);

        dataModel.put(Article.ARTICLES, articles);

        final int pageCount = (int) Math.ceil(total / (double) pageSize);
        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, Symphonys.getInt("defaultPaginationWindowSize"));
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        String searchEmptyLabel = langPropsService.get("searchEmptyLabel");
        searchEmptyLabel = searchEmptyLabel.replace("${key}", keyword);
        dataModel.put("searchEmptyLabel", searchEmptyLabel);
        return  url;
    }
}
