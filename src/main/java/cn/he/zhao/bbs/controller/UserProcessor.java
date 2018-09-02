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

import cn.he.zhao.bbs.exception.RequestProcessAdviceException;
import cn.he.zhao.bbs.spring.Paginator;
import cn.he.zhao.bbs.spring.Requests;
import cn.he.zhao.bbs.util.Escapes;
import cn.he.zhao.bbs.util.Sessions;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.validate.PointTransferValidation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User processor.
 * <ul>
 * <li>User articles (/member/{userName}), GET</li>
 * <li>User comments (/member/{userName}/comments), GET</li>
 * <li>User following users (/member/{userName}/following/users), GET</li>
 * <li>User following tags (/member/{userName}/following/tags), GET</li>
 * <li>User following articles (/member/{userName}/following/articles), GET</li>
 * <li>User followers (/member/{userName}/followers), GET</li>
 * <li>User points (/member/{userName}/points), GET</li>
 * <li>User breezemoons (/member/{userName}/breezemoons), GET</li>
 * <li>Transfer point (/point/transfer), POST</li>
 * <li>Point buy invitecode (/point/buy-invitecode), POST</li>
 * <li>Lists usernames (/users/names), GET</li>
 * <li>Lists emotions (/users/emotions), GET</li>
 * <li>Exports posts(article/comment) to a file (/export/posts), POST</li>
 * <li>Queries invitecode state (/invitecode/state), GET</li>
 * <li>Shows link forge (/member/{userName}/forge/link), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.27.0.2, Jul 9, 2018
 * @since 0.2.0
 */
@Controller
public class UserProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserProcessor.class);

    /**
     * User management service.
     */
    @Autowired
    private UserMgmtService userMgmtService;

    /**
     * Article management service.
     */
    @Autowired
    private ArticleQueryService articleQueryService;

    /**
     * User query service.
     */
    @Autowired
    private UserQueryService userQueryService;

    /**
     * Comment query service.
     */
    @Autowired
    private CommentQueryService commentQueryService;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * FollowUtil query service.
     */
    @Autowired
    private FollowQueryService followQueryService;

    /**
     * EmotionUtil query service.
     */
    @Autowired
    private EmotionQueryService emotionQueryService;

    /**
     * EmotionUtil management service.
     */
    @Autowired
    private EmotionMgmtService emotionMgmtService;

    /**
     * Data entity service.
     */
    @Autowired
    private DataModelService dataModelService;

    /**
     * Avatar query service.
     */
    @Autowired
    private AvatarQueryService avatarQueryService;

    /**
     * PointtransferUtil query service.
     */
    @Autowired
    private PointtransferQueryService pointtransferQueryService;

    /**
     * PointtransferUtil management service.
     */
    @Autowired
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * NotificationUtil management service.
     */
    @Autowired
    private NotificationMgmtService notificationMgmtService;

    /**
     * Post export service.
     */
    @Autowired
    private PostExportService postExportService;

    /**
     * OptionUtil query service.
     */
    @Autowired
    private OptionQueryService optionQueryService;

    /**
     * InvitecodeUtil management service.
     */
    @Autowired
    private InvitecodeMgmtService invitecodeMgmtService;

    /**
     * LinkUtil forge query service.
     */
    @Autowired
    private LinkForgeQueryService linkForgeQueryService;

    /**
     * RoleUtil query service.
     */
    @Autowired
    private RoleQueryService roleQueryService;

    /**
     * InvitecodeUtil query service.
     */
    @Autowired
    private InvitecodeQueryService invitecodeQueryService;

    /**
     * Breezemoon query service.
     */
    @Autowired
    private BreezemoonQueryService breezemoonQueryService;

    /**
     * Shows user home breezemoons page.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestMapping(value = "/member/{userName}/breezemoons", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
//    @After(adviceClass = {CSRFToken.class, PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @UserBlockCheckAnno
    @CSRFTokenAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showHomeBreezemoons(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                                    final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/home/breezemoons.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("userHomeBreezemoonsCnt");
        final int windowSize = Symphonys.getInt("userHomeBreezemoonsWindowSize");

        fillHomeUser(dataModel, user, roleQueryService);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        JSONObject currentUser;
        String currentUserId = null;
        if (isLoggedIn) {
            currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            currentUserId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(currentUserId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final JSONObject result = breezemoonQueryService.getBreezemoons(avatarViewMode, currentUserId, followingId, pageNum, pageSize, windowSize);
        final List<JSONObject> bms = (List<JSONObject>) result.opt(Breezemoon.BREEZEMOONS);
        dataModel.put(Common.USER_HOME_BREEZEMOONS, bms);

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int recordCount = pagination.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil(recordCount / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, recordCount);

        dataModel.put(Common.TYPE, Breezemoon.BREEZEMOONS);
        return "/home/breezemoons.ftl";
    }

    /**
     * Shows user link forge.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestMapping(value = "/member/{userName}/forge/link", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, UserBlockCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @UserBlockCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showLinkForge(Map<String, Object> dataModel, final HttpServletRequest request,
                              final HttpServletResponse response, final String userName) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/home/link-forge.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));
        fillHomeUser(dataModel, user, roleQueryService);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        final List<JSONObject> tags = linkForgeQueryService.getUserForgedLinks(user.optString(Keys.OBJECT_ID));
        dataModel.put(Tag.TAGS, (Object) tags);

        dataModel.put(Common.TYPE, "linkForge");
        return "/home/link-forge.ftl";
    }

    /**
     * Queries invitecode state.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/invitecode/state", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class})
    @StopWatchStartAnno
    @CSRFCheckAnno
    public void queryInvitecode(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final JSONObject ret = Results.falseResult();
//        context.renderJSON(ret);
        dataModel.put(Keys.STATUS_CODE, false);

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        String invitecode = requestJSONObject.optString(Invitecode.INVITECODE);
        if (StringUtils.isBlank(invitecode)) {
            dataModel.put(Keys.STATUS_CODE, -1);
            dataModel.put(Keys.MSG, invitecode + " " + langPropsService.get("notFoundInvitecodeLabel"));

            return;
        }

        invitecode = invitecode.trim();

        final JSONObject result = invitecodeQueryService.getInvitecode(invitecode);

        if (null == result) {
            dataModel.put(Keys.STATUS_CODE, -1);
            dataModel.put(Keys.MSG, langPropsService.get("notFoundInvitecodeLabel"));
        } else {
            final int status = result.optInt(Invitecode.STATUS);
            dataModel.put(Keys.STATUS_CODE, status);

            switch (status) {
                case Invitecode.STATUS_C_USED:
                    dataModel.put(Keys.MSG, langPropsService.get("invitecodeUsedLabel"));

                    break;
                case Invitecode.STATUS_C_UNUSED:
                    String msg = langPropsService.get("invitecodeOkLabel");
                    msg = msg.replace("${time}", DateFormatUtils.format(result.optLong(Keys.OBJECT_ID)
                            + Symphonys.getLong("invitecode.expired"), "yyyy-MM-dd HH:mm"));

                    dataModel.put(Keys.MSG, msg);

                    break;
                case Invitecode.STATUS_C_STOPUSE:
                    dataModel.put(Keys.MSG, langPropsService.get("invitecodeStopLabel"));

                    break;
                default:
                    dataModel.put(Keys.MSG, langPropsService.get("notFoundInvitecodeLabel"));
            }
        }
    }

    /**
     * Point buy invitecode.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/point/buy-invitecode", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class, PermissionCheck.class})
    @LoginCheckAnno
    @CSRFTokenAnno
    @PermissionCheckAnno
    public void pointBuy(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final JSONObject ret = Results.falseResult();
//        context.renderJSON(ret);
        dataModel.put(Keys.STATUS_CODE, false);

        final String allowRegister = optionQueryService.getAllowRegister();
        if (!"2".equals(allowRegister)) {
            return;
        }

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String fromId = currentUser.optString(Keys.OBJECT_ID);
        final String userName = currentUser.optString(User.USER_NAME);

        // 故意先生成后返回校验，所以即使积分不够也是可以兑换成功的
        // 这是为了让积分不够的用户可以通过这个后门兑换、分发邀请码以实现积分“自充”
        // 后期可能会关掉这个【特性】
        final String invitecode = invitecodeMgmtService.userGenInvitecode(fromId, userName);

        final String transferId = pointtransferMgmtService.transfer(fromId, Pointtransfer.ID_C_SYS,
                Pointtransfer.TRANSFER_TYPE_C_BUY_INVITECODE, Pointtransfer.TRANSFER_SUM_C_BUY_INVITECODE,
                invitecode, System.currentTimeMillis());
        final boolean succ = null != transferId;
        dataModel.put(Keys.STATUS_CODE, succ);
        if (!succ) {
            dataModel.put(Keys.MSG, langPropsService.get("exchangeFailedLabel"));
        } else {
            String msg = langPropsService.get("expireTipLabel");
            msg = msg.replace("${time}", DateFormatUtils.format(System.currentTimeMillis()
                    + Symphonys.getLong("invitecode.expired"), "yyyy-MM-dd HH:mm"));
            dataModel.put(Keys.MSG, invitecode + " " + msg);
        }
    }

    /**
     * Shows user home anonymous comments page.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestMapping(value = "/member/{userName}/comments/anonymous", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, UserBlockCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @UserBlockCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showHomeAnonymousComments(Map<String, Object> dataModel, final HttpServletRequest request,
                                          final HttpServletResponse response, final String userName) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/home/comments.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        JSONObject currentUser = null;
        if (isLoggedIn) {
            currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
        }

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        if (null == currentUser || (!currentUser.optString(Keys.OBJECT_ID).equals(user.optString(Keys.OBJECT_ID)))
                && !Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return null;
        }

        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("userHomeCmtsCnt");
        final int windowSize = Symphonys.getInt("userHomeCmtsWindowSize");

        fillHomeUser(dataModel, user, roleQueryService);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        if (isLoggedIn) {
            currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final List<JSONObject> userComments = commentQueryService.getUserComments(
                avatarViewMode, user.optString(Keys.OBJECT_ID), Comment.COMMENT_ANONYMOUS_C_ANONYMOUS,
                pageNum, pageSize, currentUser);
        dataModel.put(Common.USER_HOME_COMMENTS, userComments);

        int recordCount = 0;
        int pageCount = 0;
        if (!userComments.isEmpty()) {
            final JSONObject first = userComments.get(0);
            pageCount = first.optInt(Pagination.PAGINATION_PAGE_COUNT);
            recordCount = first.optInt(Pagination.PAGINATION_RECORD_COUNT);
        }

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, recordCount);

        dataModel.put(Common.TYPE, "commentsAnonymous");

        return "/home/comments.ftl";
    }

    /**
     * Shows user home anonymous articles page.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestMapping(value = "/member/{userName}/articles/anonymous", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, UserBlockCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @UserBlockCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showAnonymousArticles(Map<String, Object> dataModel, final HttpServletRequest request,
                                      final HttpServletResponse response, final String userName) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/home/home.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        JSONObject currentUser = null;
        if (isLoggedIn) {
            currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
        }

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

        if (null == currentUser || (!currentUser.optString(Keys.OBJECT_ID).equals(user.optString(Keys.OBJECT_ID)))
                && !Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return null;
        }

        final int pageNum = Paginator.getPage(request);
        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        dataModel.put(User.USER, user);
        fillHomeUser(dataModel, user, roleQueryService);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        if (isLoggedIn) {
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final int pageSize = Symphonys.getInt("userHomeArticlesCnt");
        final int windowSize = Symphonys.getInt("userHomeArticlesWindowSize");

        final List<JSONObject> userArticles = articleQueryService.getUserArticles(avatarViewMode,
                user.optString(Keys.OBJECT_ID), Article.ARTICLE_ANONYMOUS_C_ANONYMOUS, pageNum, pageSize);
        dataModel.put(Common.USER_HOME_ARTICLES, userArticles);

        int recordCount = 0;
        int pageCount = 0;
        if (!userArticles.isEmpty()) {
            final JSONObject first = userArticles.get(0);
            pageCount = first.optInt(Pagination.PAGINATION_PAGE_COUNT);
            recordCount = first.optInt(Pagination.PAGINATION_RECORD_COUNT);
        }

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, recordCount);

        dataModel.put(Common.IS_MY_ARTICLE, userName.equals(currentUser.optString(User.USER_NAME)));

        dataModel.put(Common.TYPE, "articlesAnonymous");
        return "/home/home.ftl";
    }

    /**
     * Exports posts(article/comment) to a file.
     *

     * @param request the specified request
     */
    @RequestMapping(value = "/export/posts", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class})
    @LoginCheckAnno
    public void exportPosts(Map<String, Object> dataModel, final HttpServletRequest request) {
        dataModel.put(Keys.STATUS_CODE,false);

        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        final String userId = user.optString(Keys.OBJECT_ID);

        final String downloadURL = postExportService.exportPosts(userId);
        if ("-1".equals(downloadURL)) {
            dataModel.put(Keys.MSG, langPropsService.get("insufficientBalanceLabel"));

        } else if (StringUtils.isBlank(downloadURL)) {
            return;
        }

//        context.renderJSON(true).renderJSONValue("url", downloadURL);
        dataModel.put(Keys.STATUS_CODE,true);
        dataModel.put("url", downloadURL);
    }

    /**
     * Shows user home page.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestMapping(value = "/member/{userName}", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @UserBlockCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showHome(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                         final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);
        final int pageNum = Paginator.getPage(request);
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

//        renderer.setTemplateName("/home/home.ftl");

        dataModel.put(User.USER, user);
        fillHomeUser(dataModel, user, roleQueryService);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final int pageSize = Symphonys.getInt("userHomeArticlesCnt");
        final int windowSize = Symphonys.getInt("userHomeArticlesWindowSize");

        final List<JSONObject> userArticles = articleQueryService.getUserArticles(avatarViewMode,
                user.optString(Keys.OBJECT_ID), Article.ARTICLE_ANONYMOUS_C_PUBLIC, pageNum, pageSize);
        dataModel.put(Common.USER_HOME_ARTICLES, userArticles);

        int recordCount = 0;
        int pageCount = 0;
        if (!userArticles.isEmpty()) {
            final JSONObject first = userArticles.get(0);
            pageCount = first.optInt(Pagination.PAGINATION_PAGE_COUNT);
            recordCount = first.optInt(Pagination.PAGINATION_RECORD_COUNT);
        }

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, recordCount);

        final JSONObject currentUser = Sessions.currentUser(request);
        if (null == currentUser) {
            dataModel.put(Common.IS_MY_ARTICLE, false);
        } else {
            dataModel.put(Common.IS_MY_ARTICLE, userName.equals(currentUser.optString(User.USER_NAME)));
        }

        dataModel.put(Common.TYPE, "home");
        return "/home/home.ftl";
    }

    /**
     * Shows user home comments page.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestMapping(value = "/member/{userName}/comments", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @UserBlockCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showHomeComments(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                                 final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/home/comments.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("userHomeCmtsCnt");
        final int windowSize = Symphonys.getInt("userHomeCmtsWindowSize");

        fillHomeUser(dataModel, user, roleQueryService);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        JSONObject currentUser = null;
        if (isLoggedIn) {
            currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final List<JSONObject> userComments = commentQueryService.getUserComments(avatarViewMode,
                user.optString(Keys.OBJECT_ID), Comment.COMMENT_ANONYMOUS_C_PUBLIC, pageNum, pageSize, currentUser);
        dataModel.put(Common.USER_HOME_COMMENTS, userComments);

        int recordCount = 0;
        int pageCount = 0;
        if (!userComments.isEmpty()) {
            final JSONObject first = userComments.get(0);
            pageCount = first.optInt(Pagination.PAGINATION_PAGE_COUNT);
            recordCount = first.optInt(Pagination.PAGINATION_RECORD_COUNT);
        }

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, recordCount);

        dataModel.put(Common.TYPE, "comments");
        return  "/home/comments.ftl";
    }

    /**
     * Shows user home following users page.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestMapping(value = "/member/{userName}/following/users", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @UserBlockCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showHomeFollowingUsers(Map<String, Object> dataModel, final HttpServletRequest request,
                                       final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/home/following-users.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("userHomeFollowingUsersCnt");
        final int windowSize = Symphonys.getInt("userHomeFollowingUsersWindowSize");

        fillHomeUser(dataModel, user, roleQueryService);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final JSONObject followingUsersResult = followQueryService.getFollowingUsers(avatarViewMode,
                followingId, pageNum, pageSize);
        final List<JSONObject> followingUsers = (List<JSONObject>) followingUsersResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWING_USERS, followingUsers);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            for (final JSONObject followingUser : followingUsers) {
                final String homeUserFollowingUserId = followingUser.optString(Keys.OBJECT_ID);

                followingUser.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowingUserId, Follow.FOLLOWING_TYPE_C_USER));
            }
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final int followingUserCnt = followingUsersResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil((double) followingUserCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, followingUserCnt);

        dataModel.put(Common.TYPE, "followingUsers");

        return "/home/following-users.ftl";
    }

    /**
     * Shows user home following tags page.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestMapping(value = "/member/{userName}/following/tags", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @UserBlockCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showHomeFollowingTags(Map<String, Object> dataModel, final HttpServletRequest request,
                                      final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/home/following-tags.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("userHomeFollowingTagsCnt");
        final int windowSize = Symphonys.getInt("userHomeFollowingTagsWindowSize");

        fillHomeUser(dataModel, user, roleQueryService);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final JSONObject followingTagsResult = followQueryService.getFollowingTags(followingId, pageNum, pageSize);
        final List<JSONObject> followingTags = (List<JSONObject>) followingTagsResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWING_TAGS, followingTags);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            for (final JSONObject followingTag : followingTags) {
                final String homeUserFollowingTagId = followingTag.optString(Keys.OBJECT_ID);

                followingTag.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowingTagId, Follow.FOLLOWING_TYPE_C_TAG));
            }
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final int followingTagCnt = followingTagsResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil(followingTagCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, followingTagCnt);

        dataModel.put(Common.TYPE, "followingTags");
        return "/home/following-tags.ftl";
    }

    /**
     * Shows user home following articles page.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestMapping(value = "/member/{userName}/following/articles", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @UserBlockCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showHomeFollowingArticles(Map<String, Object> dataModel, final HttpServletRequest request,
                                          final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/home/following-articles.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("userHomeFollowingArticlesCnt");
        final int windowSize = Symphonys.getInt("userHomeFollowingArticlesWindowSize");

        fillHomeUser(dataModel, user, roleQueryService);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final JSONObject followingArticlesResult = followQueryService.getFollowingArticles(avatarViewMode,
                followingId, pageNum, pageSize);
        final List<JSONObject> followingArticles = (List<JSONObject>) followingArticlesResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWING_ARTICLES, followingArticles);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            for (final JSONObject followingArticle : followingArticles) {
                final String homeUserFollowingArticleId = followingArticle.optString(Keys.OBJECT_ID);

                followingArticle.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowingArticleId, Follow.FOLLOWING_TYPE_C_ARTICLE));
            }
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final int followingArticleCnt = followingArticlesResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil(followingArticleCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, followingArticleCnt);

        dataModel.put(Common.TYPE, "followingArticles");

        return "/home/following-articles.ftl";
    }

    /**
     * Shows user home watching articles page.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestMapping(value = "/member/{userName}/watching/articles", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @UserBlockCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showHomeWatchingArticles(Map<String, Object> dataModel, final HttpServletRequest request,
                                         final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/home/watching-articles.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("userHomeFollowingArticlesCnt");
        final int windowSize = Symphonys.getInt("userHomeFollowingArticlesWindowSize");

        fillHomeUser(dataModel, user, roleQueryService);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final JSONObject followingArticlesResult = followQueryService.getWatchingArticles(avatarViewMode,
                followingId, pageNum, pageSize);
        final List<JSONObject> followingArticles = (List<JSONObject>) followingArticlesResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWING_ARTICLES, followingArticles);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            for (final JSONObject followingArticle : followingArticles) {
                final String homeUserFollowingArticleId = followingArticle.optString(Keys.OBJECT_ID);

                followingArticle.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowingArticleId, Follow.FOLLOWING_TYPE_C_ARTICLE_WATCH));
            }
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final int followingArticleCnt = followingArticlesResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil(followingArticleCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, followingArticleCnt);

        dataModel.put(Common.TYPE, "watchingArticles");

        return "/home/watching-articles.ftl";
    }

    /**
     * Shows user home follower users page.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestMapping(value = "/member/{userName}/followers", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @UserBlockCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showHomeFollowers(Map<String, Object> dataModel, final HttpServletRequest request,
                                  final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/home/followers.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("userHomeFollowersCnt");
        final int windowSize = Symphonys.getInt("userHomeFollowersWindowSize");

        fillHomeUser(dataModel, user, roleQueryService);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        final JSONObject followerUsersResult = followQueryService.getFollowerUsers(avatarViewMode,
                followingId, pageNum, pageSize);
        final List<JSONObject> followerUsers = (List) followerUsersResult.opt(Keys.RESULTS);
        dataModel.put(Common.USER_HOME_FOLLOWER_USERS, followerUsers);

        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, followingId, Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            for (final JSONObject followerUser : followerUsers) {
                final String homeUserFollowerUserId = followerUser.optString(Keys.OBJECT_ID);

                followerUser.put(Common.IS_FOLLOWING, followQueryService.isFollowing(followerId, homeUserFollowerUserId, Follow.FOLLOWING_TYPE_C_USER));
            }

            if (followerId.equals(followingId)) {
                notificationMgmtService.makeRead(followingId, Notification.DATA_TYPE_C_NEW_FOLLOWER);
            }
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final int followerUserCnt = followerUsersResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil((double) followerUserCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Pagination.PAGINATION_RECORD_COUNT, followerUserCnt);

        dataModel.put(Common.TYPE, "followers");

        notificationMgmtService.makeRead(followingId, Notification.DATA_TYPE_C_NEW_FOLLOWER);

        return "/home/followers.ftl";
    }

    /**
     * Shows user home points page.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestMapping(value = "/member/{userName}/points", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class, UserBlockCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @UserBlockCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showHomePoints(Map<String, Object> dataModel, final HttpServletRequest request,
                               final HttpServletResponse response, final String userName) throws Exception {
        final JSONObject user = (JSONObject) request.getAttribute(User.USER);

//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("/home/points.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("userHomePointsCnt");
        final int windowSize = Symphonys.getInt("userHomePointsWindowSize");

        fillHomeUser(dataModel, user, roleQueryService);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
        avatarQueryService.fillUserAvatarURL(avatarViewMode, user);

        final String followingId = user.optString(Keys.OBJECT_ID);
        dataModel.put(Follow.FOLLOWING_ID, followingId);

        final JSONObject userPointsResult
                = pointtransferQueryService.getUserPoints(user.optString(Keys.OBJECT_ID), pageNum, pageSize);
        final List<JSONObject> userPoints
                = CollectionUtils.<JSONObject>jsonArrayToList(userPointsResult.optJSONArray(Keys.RESULTS));
        dataModel.put(Common.USER_HOME_POINTS, userPoints);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            final JSONObject currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            final String followerId = currentUser.optString(Keys.OBJECT_ID);

            final boolean isFollowing = followQueryService.isFollowing(followerId, user.optString(Keys.OBJECT_ID), Follow.FOLLOWING_TYPE_C_USER);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);
        }

        user.put(UserExt.USER_T_CREATE_TIME, new Date(user.getLong(Keys.OBJECT_ID)));

        final int pointsCnt = userPointsResult.optInt(Pagination.PAGINATION_RECORD_COUNT);
        final int pageCount = (int) Math.ceil((double) pointsCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        dataModel.put(Common.TYPE, "points");

        return "/home/points.ftl";
    }

    /**
     * Point transfer.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/point/transfer", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, CSRFCheck.class, PointTransferValidation.class})
    @LoginCheckAnno
    @CSRFCheckAnno
    public void pointTransfer(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final JSONObject ret = Results.falseResult();
//        context.renderJSON(ret);

        dataModel.put(Keys.STATUS_CODE,false);

        try{
            PointTransferValidation.doAdvice(request);
        }catch (RequestProcessAdviceException e){
            dataModel.put(Keys.MSG , e.getJsonObject().get(Keys.MSG));
        }

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final int amount = requestJSONObject.optInt(Common.AMOUNT);
        final JSONObject toUser = (JSONObject) request.getAttribute(Common.TO_USER);
        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);

        final String fromId = currentUser.optString(Keys.OBJECT_ID);
        final String toId = toUser.optString(Keys.OBJECT_ID);

        final String transferId = pointtransferMgmtService.transfer(fromId, toId,
                Pointtransfer.TRANSFER_TYPE_C_ACCOUNT2ACCOUNT, amount, toId, System.currentTimeMillis());
        final boolean succ = null != transferId;
        dataModel.put(Keys.STATUS_CODE, succ);
        if (!succ) {
            dataModel.put(Keys.MSG, langPropsService.get("transferFailLabel"));
        } else {
            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, toId);
            notification.put(Notification.NOTIFICATION_DATA_ID, transferId);

            notificationMgmtService.addPointTransferNotification(notification);
        }
    }

    /**
     * Resets unverified users.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/cron/users/reset-unverified", method = RequestMethod.GET)
    public void resetUnverifiedUsers(Map<String, Object> dataModel,
                                     final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        userMgmtService.resetUnverifiedUsers();

        dataModel.put(Keys.STATUS_CODE,true);
    }

    /**
     * Lists usernames.
     *

     * @param request the specified request
     * @throws Exception exception
     */
    @RequestMapping(value = "/users/names", method = RequestMethod.GET)
    public void listNames(Map<String, Object> dataModel, final HttpServletRequest request) throws Exception {
        dataModel.put(Keys.STATUS_CODE,true);

        final String namePrefix = request.getParameter("name");
        if (StringUtils.isBlank(namePrefix)) {
            final List<JSONObject> admins = userQueryService.getAdmins();
            final List<JSONObject> userNames = new ArrayList<>();
            for (final JSONObject admin : admins) {
                final JSONObject userName = new JSONObject();
                userName.put(User.USER_NAME, admin.optString(User.USER_NAME));
                final String avatar = avatarQueryService.getAvatarURLByUser(UserExt.USER_AVATAR_VIEW_MODE_C_STATIC, admin, "20");
                userName.put(UserExt.USER_AVATAR_URL, avatar);

                userNames.add(userName);
            }

            dataModel.put(Common.USER_NAMES, userNames);

            return;
        }

        final List<JSONObject> userNames = userQueryService.getUserNamesByPrefix(namePrefix);
        dataModel.put(Common.USER_NAMES, userNames);
    }

    /**
     * Lists emotions.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/users/emotions", method = RequestMethod.GET)
    public void getEmotions(Map<String, Object> dataModel, final HttpServletRequest request,
                            final HttpServletResponse response) throws Exception {
        dataModel.put(Keys.STATUS_CODE,false);

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        if (null == currentUser) {
            dataModel.put("emotions", "");

            return;
        }

        final String userId = currentUser.optString(Keys.OBJECT_ID);
        final String emotions = emotionQueryService.getEmojis(userId);

        dataModel.put("emotions", emotions);
    }

    /**
     * Loads usernames.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/cron/users/load-names", method = RequestMethod.GET)
    public void loadUserNames(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        userQueryService.loadUserNames();

        dataModel.put(Keys.STATUS_CODE,true);
    }

    /**
     * Fills home user.
     *
     * @param dataModel the specified data entity
     * @param user      the specified user
     */
    static void fillHomeUser(final Map<String, Object> dataModel, final JSONObject user, final RoleQueryService roleQueryService) {
        Escapes.escapeHTML(user);
        dataModel.put(User.USER, user);

        final String roleId = user.optString(User.USER_ROLE);
        final JSONObject role = roleQueryService.getRole(roleId);
        user.put(Role.ROLE_NAME, role.optString(Role.ROLE_NAME));
    }
}
