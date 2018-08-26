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

import cn.he.zhao.bbs.spring.Locales;
import cn.he.zhao.bbs.spring.Paginator;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.spring.Stopwatchs;
import cn.he.zhao.bbs.util.Emotions;
import cn.he.zhao.bbs.util.Markdowns;
import cn.he.zhao.bbs.util.Sessions;
import cn.he.zhao.bbs.util.Symphonys;
import org.apache.commons.io.IOUtils;
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
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.*;

/**
 * Index processor.
 * <ul>
 * <li>Shows index (/), GET</li>
 * <li>Shows recent articles (/recent), GET</li>
 * <li>Shows watch relevant pages (/watch/*), GET</li>
 * <li>Shows hot articles (/hot), GET</li>
 * <li>Shows perfect articles (/perfect), GET</li>
 * <li>Shows about (/about), GET</li>
 * <li>Shows b3log (/b3log), GET</li>
 * <li>Shows SymHub (/symhub), GET</li>
 * <li>Shows kill browser (/kill-browser), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.14.0.0, May 21, 2018
 * @since 0.2.0
 */
@Controller
public class IndexProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexProcessor.class);

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
     * User management service.
     */
    @Autowired
    private UserMgmtService userMgmtService;

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
     * Shows watch articles or users.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = {"/watch", "/watch/users"}, method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showWatch(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("watch.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "watch.ftl";
        final int pageNum = Paginator.getPage(request);
        int pageSize = Symphonys.getInt("indexArticlesCnt");
        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        final JSONObject user = Sessions.currentUser(request);
        if (null != user) {
            pageSize = user.optInt(UserExt.USER_LIST_PAGE_SIZE);

            if (!UserExt.finshedGuide(user)) {
                return "redirect:" +( SpringUtil.getServerPath() + "/guide");

            }
        }

        dataModel.put(Common.WATCHING_ARTICLES, Collections.emptyList());
        String sortModeStr = StringUtils.substringAfter(request.getRequestURI(), "/watch");
        switch (sortModeStr) {
            case "":
                if (null != user) {
                    final List<JSONObject> followingTagArticles = articleQueryService.getFollowingTagArticles(
                            avatarViewMode, user.optString(Keys.OBJECT_ID), 1, pageSize);
                    dataModel.put(Common.WATCHING_ARTICLES, followingTagArticles);
                }

                break;
            case "/users":
                if (null != user) {
                    final List<JSONObject> followingUserArticles = articleQueryService.getFollowingUserArticles(
                            avatarViewMode, user.optString(Keys.OBJECT_ID), 1, pageSize);
                    dataModel.put(Common.WATCHING_ARTICLES, followingUserArticles);
                }

                break;
        }

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
     * Shows md guide.
     *
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/guide/markdown", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showMDGuide(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("other/md-guide.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "other/md-guide.ftl";

        try (final InputStream inputStream = IndexProcessor.class.getResourceAsStream("/md_guide.md")) {
            final String md = IOUtils.toString(inputStream, "UTF-8");
            String html = Emotions.convert(md);
            html = Markdowns.toHTML(html);

            dataModel.put("md", md);
            dataModel.put("html", html);
        } catch (final Exception e) {
            LOGGER.error( "Loads markdown guide failed", e);
        }

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Shows index.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showIndex(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("index.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "index.ftl";

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        final List<JSONObject> recentArticles = articleQueryService.getIndexRecentArticles(avatarViewMode);
        dataModel.put(Common.RECENT_ARTICLES, recentArticles);

        final List<JSONObject> perfectArticles = articleQueryService.getIndexPerfectArticles();
        dataModel.put(Common.PERFECT_ARTICLES, perfectArticles);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        dataModelService.fillIndexTags(dataModel);

        dataModel.put(Common.SELECTED, Common.INDEX);
        return url;
    }

    /**
     * Shows recent articles.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = {"/recent", "/recent/hot", "/recent/good", "/recent/reply"}, method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showRecent(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("recent.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "recent.ftl";

        final int pageNum = Paginator.getPage(request);
        int pageSize = Symphonys.getInt("indexArticlesCnt");
        final JSONObject user = userQueryService.getCurrentUser(request);
        if (null != user) {
            pageSize = user.optInt(UserExt.USER_LIST_PAGE_SIZE);

            if (!UserExt.finshedGuide(user)) {
                return "redirect:" +( SpringUtil.getServerPath() + "/guide");

            }
        }

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        String sortModeStr = StringUtils.substringAfter(request.getRequestURI(), "/recent");
        int sortMode;
        switch (sortModeStr) {
            case "":
                sortMode = 0;

                break;
            case "/hot":
                sortMode = 1;

                break;
            case "/good":
                sortMode = 2;

                break;
            case "/reply":
                sortMode = 3;

                break;
            default:
                sortMode = 0;
        }

        dataModel.put(Common.SELECTED, Common.RECENT);
        final JSONObject result = articleQueryService.getRecentArticles(avatarViewMode, sortMode, pageNum, pageSize);
        final List<JSONObject> allArticles = (List<JSONObject>) result.get(Article.ARTICLES);
        final List<JSONObject> stickArticles = new ArrayList<>();
        final Iterator<JSONObject> iterator = allArticles.iterator();
        while (iterator.hasNext()) {
            final JSONObject article = iterator.next();
            final boolean stick = article.optInt(Article.ARTICLE_T_STICK_REMAINS) > 0;
            article.put(Article.ARTICLE_T_IS_STICK, stick);
            if (stick) {
                stickArticles.add(article);
                iterator.remove();
            }
        }

        dataModel.put(Common.STICK_ARTICLES, stickArticles);
        dataModel.put(Common.LATEST_ARTICLES, allArticles);

        final JSONObject pagination = result.getJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final List<Integer> pageNums = (List<Integer>) pagination.get(Pagination.PAGINATION_PAGE_NUMS);
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

        dataModel.put(Common.CURRENT, StringUtils.substringAfter(request.getRequestURI(), "/recent"));
        return url;
    }

    /**
     * Shows hot articles.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/hot", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showHotArticles(Map<String, Object> dataModel,
                                final HttpServletRequest request, final HttpServletResponse response) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("hot.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "hot.ftl";

        int pageSize = Symphonys.getInt("indexArticlesCnt");
        final JSONObject user = userQueryService.getCurrentUser(request);
        if (null != user) {
            pageSize = user.optInt(UserExt.USER_LIST_PAGE_SIZE);
        }

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        final List<JSONObject> indexArticles = articleQueryService.getHotArticles(avatarViewMode, pageSize);
        dataModel.put(Common.INDEX_ARTICLES, indexArticles);
        dataModel.put(Common.SELECTED, Common.HOT);

        Stopwatchs.start("Fills");
        try {
            dataModelService.fillHeaderAndFooter(request, response, dataModel);
            if (!(Boolean) dataModel.get(Common.IS_MOBILE)) {
                dataModelService.fillRandomArticles(dataModel);
            }
            dataModelService.fillSideHotArticles(dataModel);
            dataModelService.fillSideTags(dataModel);
            dataModelService.fillLatestCmts(dataModel);
        } finally {
            Stopwatchs.end();
        }
        return  url;
    }

    /**
     * Shows SymHub page.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/symhub", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showSymHub(Map<String, Object> dataModel,
                           final HttpServletRequest request, final HttpServletResponse response) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("other/symhub.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        final List<JSONObject> syms = Symphonys.getSyms();
        dataModel.put("syms", (Object) syms);

        Stopwatchs.start("Fills");
        try {
            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
            dataModelService.fillHeaderAndFooter(request, response, dataModel);
            if (!(Boolean) dataModel.get(Common.IS_MOBILE)) {
                dataModelService.fillRandomArticles(dataModel);
            }
            dataModelService.fillSideHotArticles(dataModel);
            dataModelService.fillSideTags(dataModel);
            dataModelService.fillLatestCmts(dataModel);
        } finally {
            Stopwatchs.end();
        }
        return "other/symhub.ftl";
    }

    /**
     * Shows perfect articles.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/perfect", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showPerfectArticles(Map<String, Object> dataModel,
                                    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("perfect.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        final int pageNum = Paginator.getPage(request);
        int pageSize = Symphonys.getInt("indexArticlesCnt");
        final JSONObject user = userQueryService.getCurrentUser(request);
        if (null != user) {
            pageSize = user.optInt(UserExt.USER_LIST_PAGE_SIZE);
            if (!UserExt.finshedGuide(user)) {
                return "redirect:" +( SpringUtil.getServerPath() + "/guide");

            }
        }

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        final JSONObject result = articleQueryService.getPerfectArticles(avatarViewMode, pageNum, pageSize);
        final List<JSONObject> perfectArticles = (List<JSONObject>) result.get(Article.ARTICLES);
        dataModel.put(Common.PERFECT_ARTICLES, perfectArticles);
        dataModel.put(Common.SELECTED, Common.PERFECT);
        final JSONObject pagination = result.getJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final List<Integer> pageNums = (List<Integer>) pagination.get(Pagination.PAGINATION_PAGE_NUMS);
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

        return "perfect.ftl";
    }

    /**
     * Shows about.
     *
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/about", method = RequestMethod.GET)
//    @Before(adviceClass = StopwatchStartAdvice.class)
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @StopWatchEndAnno
    public void showAbout(final HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        response.setHeader("Location", "https://hacpai.com/article/1440573175609");
        response.flushBuffer();
    }

    /**
     * Shows b3log.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/b3log", method = RequestMethod.GET)
//    @Before(adviceClass = StopwatchStartAdvice.class)
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showB3log(Map<String, Object> dataModel,
                          final HttpServletRequest request, final HttpServletResponse response) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("other/b3log.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);
        return "other/b3log.ftl";
    }

    /**
     * Shows kill browser page with the specified context.
     *

     * @param request  the specified HTTP servlet request
     * @param response the specified HTTP servlet response
     */
    @RequestMapping(value = "/kill-browser", method = RequestMethod.GET)
//    @Before(adviceClass = StopwatchStartAdvice.class)
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @StopWatchEndAnno
    public String showKillBrowser(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response) {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        renderer.setTemplateName("other/kill-browser.ftl");
//        context.setRenderer(renderer);
//
//        final Map<String, Object> dataModel = renderer.getDataModel();
        final Map<String, String> langs = langPropsService.getAll(Locales.getLocale());

        dataModel.putAll(langs);
        Keys.fillRuntime(dataModel);
        dataModelService.fillMinified(dataModel);

        return "other/kill-browser.ftl";
    }
}
