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
import cn.he.zhao.bbs.entityUtil.*;
import cn.he.zhao.bbs.event.handler.ArticleBaiduSender;
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.entityUtil.my.CollectionUtils;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.Pagination;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.*;
import cn.he.zhao.bbs.util.Escapes;
import cn.he.zhao.bbs.util.JsonUtil;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.validate.UserRegister2Validation;
import cn.he.zhao.bbs.validate.UserRegisterValidation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.*;

/**
 * Admin processor.
 * <ul>
 * <li>Shows admin index (/admin), GET</li>
 * <li>Shows users (/admin/users), GET</li>
 * <li>Shows a user (/admin/user/{userId}), GET</li>
 * <li>Shows add user (/admin/add-user), GET</li>
 * <li>Adds a user (/admin/add-user), POST</li>
 * <li>Updates a user (/admin/user/{userId}), POST</li>
 * <li>Updates a user's email (/admin/user/{userId}/email), POST</li>
 * <li>Updates a user's username (/admin/user/{userId}/username), POST</li>
 * <li>Charges a user's point (/admin/user/{userId}/charge-point), POST</li>
 * <li>Exchanges a user's point (/admin/user/{userId}/exchange-point), POST</li>
 * <li>Deducts a user's abuse point (/admin/user/{userId}/abuse-point), POST</li>
 * <li>Shows articles (/admin/articles), GET</li>
 * <li>Shows an article (/admin/article/{articleId}), GET</li>
 * <li>Updates an article (/admin/article/{articleId}), POST</li>
 * <li>Removes an article (/admin/remove-article), POST</li>
 * <li>Shows add article (/admin/add-article), GET</li>
 * <li>Adds an article (/admin/add-article), POST</li>
 * <li>Show comments (/admin/comments), GET</li>
 * <li>Shows a comment (/admin/comment/{commentId}), GET</li>
 * <li>Updates a comment (/admin/comment/{commentId}), POST</li>
 * <li>Removes a comment (/admin/remove-comment), POST</li>
 * <li>Show breezemoons (/admin/breezemoons), GET</li>
 * <li>Shows a breezemoon (/admin/breezemoon/{breezemoonId}), GET</li>
 * <li>Updates a breezemoon (/admin/breezemoon/{breezemoonId}), POST</li>
 * <li>Removes a breezemoon (/admin/remove-breezemoon), POST</li>
 * <li>Shows domains (/admin/domains, GET</li>
 * <li>Show a domain (/admin/domain/{domainId}, GET</li>
 * <li>Updates a domain (/admin/domain/{domainId}), POST</li>
 * <li>Shows tags (/admin/tags), GET</li>
 * <li>Removes unused tags (/admin/tags/remove-unused), POST</li>
 * <li>Show a tag (/admin/tag/{tagId}), GET</li>
 * <li>Shows add tag (/admin/add-tag), GET</li>
 * <li>Adds a tag (/admin/add-tag), POST</li>
 * <li>Updates a tag (/admin/tag/{tagId}), POST</li>
 * <li>Generates invitecodes (/admin/invitecodes/generate), POST</li>
 * <li>Shows invitecodes (/admin/invitecodes), GET</li>
 * <li>Show an invitecode (/admin/invitecode/{invitecodeId}), GET</li>
 * <li>Updates an invitecode (/admin/invitecode/{invitecodeId}), POST</li>
 * <li>Shows miscellaneous (/admin/misc), GET</li>
 * <li>Updates miscellaneous (/admin/misc), POST</li>
 * <li>Search index (/admin/search/index), POST</li>
 * <li>Search index one article (/admin/search-index-article), POST</li>
 * <li>Shows ad (/admin/ad), GET</li>
 * <li>Updates ad (/admin/ad), POST</li>
 * <li>Shows role permissions (/admin/role/{roleId}/permissions), GET</li>
 * <li>Updates role permissions (/admin/role/{roleId}/permissions), POST</li>
 * <li>Removes an role (/admin/role/{roleId}/remove), POST</li>
 * <li>Adds an role (/admin/role), POST</li>
 * <li>Show reports (/admin/reports), GET</li>
 * <li>Makes a report as handled (/admin/report/{reportId}), GET</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author Bill Ho
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 2.29.0.0, Jul 15, 2018
 * @since 1.1.0
 */
@Controller
public class AdminProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminProcessor.class);

    /**
     * Pagination window size.
     */
    private static final int WINDOW_SIZE = 15;

    /**
     * Pagination page size.
     */
    private static final int PAGE_SIZE = 20;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

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
     * Article query service.
     */
    @Autowired
    private ArticleQueryService articleQueryService;

    /**
     * Article management service.
     */
    @Autowired
    private ArticleMgmtService articleMgmtService;

    /**
     * Comment query service.
     */
    @Autowired
    private CommentQueryService commentQueryService;

    /**
     * Comment management service.
     */
    @Autowired
    private CommentMgmtService commentMgmtService;

    /**
     * OptionUtil query service.
     */
    @Autowired
    private OptionQueryService optionQueryService;

    /**
     * OptionUtil management service.
     */
    @Autowired
    private OptionMgmtService optionMgmtService;

    /**
     * Domain query service.
     */
    @Autowired
    private DomainQueryService domainQueryService;

    /**
     * Tag query service.
     */
    @Autowired
    private TagQueryService tagQueryService;

    /**
     * Domain management service.
     */
    @Autowired
    private DomainMgmtService domainMgmtService;

    /**
     * Tag management service.
     */
    @Autowired
    private TagMgmtService tagMgmtService;

    /**
     * PointtransferUtil management service.
     */
    @Autowired
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * PointtransferUtil query service.
     */
    @Autowired
    private PointtransferQueryService pointtransferQueryService;

    /**
     * NotificationUtil management service.
     */
    @Autowired
    private NotificationMgmtService notificationMgmtService;

    /**
     * Search management service.
     */
    @Autowired
    private SearchMgmtService searchMgmtService;

    /**
     * InvitecodeUtil query service.
     */
    @Autowired
    private InvitecodeQueryService invitecodeQueryService;

    /**
     * InvitecodeUtil management service.
     */
    @Autowired
    private InvitecodeMgmtService invitecodeMgmtService;

    /**
     * RoleUtil query service.
     */
    @Autowired
    private RoleQueryService roleQueryService;

    /**
     * RoleUtil management service.
     */
    @Autowired
    private RoleMgmtService roleMgmtService;

    /**
     * Data entity service.
     */
    @Autowired
    private DataModelService dataModelService;

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
     * ReportUtil management service.
     */
    @Autowired
    private ReportMgmtService reportMgmtService;

    /**
     * ReportUtil query service.
     */
    @Autowired
    private ReportQueryService reportQueryService;

    /**
     * Makes a report as ignored .
     *
     * @param response the specified response
     * @param reportId the specified report id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/report/ignore/{reportId}", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String makeReportIgnored(final HttpServletRequest request, final HttpServletResponse response, final String reportId) throws Exception {
        reportMgmtService.makeReportIgnored(reportId);

//        return "redirect:" +(SpringUtil.getServerPath() + "/admin/reports");
        return "redirect:"+ SpringUtil.getServerPath() + "/admin/reports";
    }

    /**
     * Makes a report as handled .
     *
     * @param response the specified response
     * @param reportId the specified report id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/report/{reportId}", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String makeReportHandled(final HttpServletRequest request, final HttpServletResponse response, final String reportId) throws Exception {
        reportMgmtService.makeReportHandled(reportId);

//        return "redirect:" +(SpringUtil.getServerPath() + "/admin/reports");
        return "redirect:"+ SpringUtil.getServerPath() + "/admin/reports";
    }

    /**
     * Shows reports.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/reports", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showReports(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/reports.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "admin/reports.ftl";

        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final JSONObject result = reportQueryService.getReports(requestJSONObject);
        dataModel.put(ReportUtil.REPORTS, CollectionUtils.jsonArrayToList(result.optJSONArray(ReportUtil.REPORTS)));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        return url;
    }

    /**
     * Removes an role.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/role/{roleId}/remove", method = RequestMethod.POST)
    @StopWatchStartAnno
    @LoginCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String removeRole(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response, final String roleId) throws Exception {
        final int count = roleQueryService.countUser(roleId);
        if (0 < count) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();
            String result = "admin/error.ftl";

            dataModel.put(Keys.MSG, "Still [" + count + "] users are using this role.");
            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return result;
        }

        roleMgmtService.removeRole(roleId);

//        return "redirect:" +( SpringUtil.getServerPath() + "/admin/roles");
        return "redirect:"+ SpringUtil.getServerPath() + "/admin/roles";
    }

    /**
     * Show admin breezemoons.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/breezemoons", method = RequestMethod.GET)
    @StopWatchStartAnno
    @LoginCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showBreezemoons(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/breezemoons.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "admin/breezemoons.ftl";

        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;
        final int avatarViewMode = (int) request.getAttribute(UserExtUtil.USER_AVATAR_VIEW_MODE);

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final Map<String, Class<?>> fields = new HashMap<>();
        fields.put(Keys.OBJECT_ID, String.class);
        fields.put(BreezemoonUtil.BREEZEMOON_CONTENT, String.class);
        fields.put(BreezemoonUtil.BREEZEMOON_CREATED, Long.class);
        fields.put(BreezemoonUtil.BREEZEMOON_AUTHOR_ID, String.class);
        fields.put(BreezemoonUtil.BREEZEMOON_STATUS, Integer.class);

        final JSONObject result = breezemoonQueryService.getBreezemoons(avatarViewMode, requestJSONObject, fields);
        dataModel.put(BreezemoonUtil.BREEZEMOONS, CollectionUtils.jsonArrayToList(result.optJSONArray(BreezemoonUtil.BREEZEMOONS)));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        return url;
    }

    /**
     * Shows a breezemoon.
     *
     * @param request      the specified request
     * @param response     the specified response
     * @param breezemoonId the specified breezemoon id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/breezemoon/{breezemoonId}", method = RequestMethod.GET)
    @StopWatchStartAnno
    @LoginCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showBreezemoon(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                               final String breezemoonId) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/breezemoon.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "admin/breezemoons.ftl";

        final Breezemoon breezemoon = breezemoonQueryService.getBreezemoon(breezemoonId);
        JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(breezemoon));
        Escapes.escapeHTML(jsonObject);
        dataModel.put(BreezemoonUtil.BREEZEMOON, breezemoon);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        return url;
    }

    /**
     * Updates a breezemoon.
     *
     * @param request      the specified request
     * @param response     the specified response
     * @param breezemoonId the specified breezemoon id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/breezemoon/{breezemoonId}", method = RequestMethod.POST)
    @StopWatchStartAnno
    @LoginCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String updateBreezemoon(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                                 final String breezemoonId) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/breezemoon.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "admin/breezemoon.ftl";

        Breezemoon breezemoon = breezemoonQueryService.getBreezemoon(breezemoonId);
        JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(breezemoon));

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            if (name.equals(BreezemoonUtil.BREEZEMOON_STATUS)) {
                jsonObject.put(name, Integer.valueOf(value));
            } else {
                jsonObject.put(name, value);
            }
        }

        breezemoonMgmtService.updateBreezemoon(jsonObject);

        breezemoon = breezemoonQueryService.getBreezemoon(breezemoonId);
        dataModel.put(BreezemoonUtil.BREEZEMOON, breezemoon);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        return url;
    }

    /**
     * Removes a breezemoon.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/remove-breezemoon", method = RequestMethod.POST)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public String removeBreezemoon(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String id = request.getParameter(Common.ID);
        breezemoonMgmtService.removeBreezemoon(id);

//        return "redirect:" +(SpringUtil.getServerPath() + "/admin/breezemoons");
        return "redirect:" + SpringUtil.getServerPath() + "/admin/breezemoons";
    }

    /**
     * Removes unused tags.
     *
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/tags/remove-unused", method = RequestMethod.POST)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public void removeUnusedTags(Map<String, Object> dataModel) throws Exception {
//        dataModel.put(Keys.STATUS_CODE,true);
        dataModel.put(Keys.STATUS_CODE,true);

        tagMgmtService.removeUnusedTags();
    }

    /**
     * Adds an role.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/role", method = RequestMethod.POST)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public String addRole(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String roleName = request.getParameter(RoleUtil.ROLE_NAME);
        if (StringUtils.isBlank(roleName)) {
//            return "redirect:" +( SpringUtil.getServerPath() + "/admin/roles");
            return "redirect:" +SpringUtil.getServerPath() + "/admin/roles";
        }

        final String roleDesc = request.getParameter(RoleUtil.ROLE_DESCRIPTION);

        final Role role = new Role();
        role.setRoleName( roleName);
        role.setRoleDescription( roleDesc);

        roleMgmtService.addRole(role);

//        return "redirect:" +(SpringUtil.getServerPath() + "/admin/roles");
        return "redirect:" + SpringUtil.getServerPath() + "/admin/roles";
    }

    /**
     * Updates role permissions.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/role/{roleId}/permissions", method = RequestMethod.POST)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public String updateRolePermissions(Map<String, Object> dataModel,
                                      final HttpServletRequest request, final HttpServletResponse response,
                                      final String roleId) throws Exception {
        final Map<String, String[]> parameterMap = request.getParameterMap();
        final Set<String> permissionIds = parameterMap.keySet();

        roleMgmtService.updateRolePermissions(roleId, permissionIds);

//        return "redirect:" +( SpringUtil.getServerPath() + "/admin/role/" + roleId + "/permissions");
        return "redirect:" + SpringUtil.getServerPath() + "/admin/role/" + roleId + "/permissions";
    }

    /**
     * Shows role permissions.
     *
     * @param request  the specified request
     * @param response the specified response
     * @param roleId   the specified role id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/role/{roleId}/permissions", method = RequestMethod.GET)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showRolePermissions(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                                    final String roleId)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/role-permissions.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "admin/role-permissions.ftl";

        final Role role = roleQueryService.getRole(roleId);
        dataModel.put(RoleUtil.ROLE, role);

        final Map<String, List<Permission>> categories = new TreeMap<>();

        final List<Permission> permissions = roleQueryService.getPermissionsGrant(roleId);
        for (final Permission permission : permissions) {
            final String label = permission.getOid() + "PermissionLabel";
            permission.setPermissionLabel(langPropsService.get(label));

            String category = String.valueOf(permission.getPermissionCategory());
            category = langPropsService.get(category + "PermissionLabel");

            final List<Permission> categoryPermissions = categories.computeIfAbsent(category, k -> new ArrayList<>());
            categoryPermissions.add(permission);
        }

        dataModel.put(PermissionUtil.PERMISSION_T_CATEGORIES, categories);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        return url;
    }

    /**
     * Shows roles.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/roles", method = RequestMethod.GET)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showRoles(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/roles.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "admin/roles.ftl";

        final JSONObject result = roleQueryService.getRoles(1, Integer.MAX_VALUE, 10);
        final List<JSONObject> roles = (List<JSONObject>) result.opt(RoleUtil.ROLES);

        dataModel.put(RoleUtil.ROLES, roles);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Updates side ad.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/ad/side", method = RequestMethod.POST)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String updateSideAd(Map<String, Object> dataModel,
                             final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String sideFullAd = request.getParameter("sideFullAd");

        Option adOption = optionQueryService.getOption(OptionUtil.ID_C_SIDE_FULL_AD);
        if (null == adOption) {
            adOption = new Option();
            adOption.setOid(OptionUtil.ID_C_SIDE_FULL_AD);
            adOption.setOptionCategory(OptionUtil.CATEGORY_C_AD);
            adOption.setOptionValue(sideFullAd);

            optionMgmtService.addOption(adOption);
        } else {
            adOption.setOptionValue( sideFullAd);

            optionMgmtService.updateOption(OptionUtil.ID_C_SIDE_FULL_AD, adOption);
        }

//        return "redirect:" +( SpringUtil.getServerPath() + "/admin/ad");
        return "redirect:" + SpringUtil.getServerPath() + "/admin/ad";
    }

    /**
     * Updates banner.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/ad/banner", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String updateBanner(Map<String, Object> dataModel,
                             final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String headerBanner = request.getParameter("headerBanner");

        Option adOption = optionQueryService.getOption(OptionUtil.ID_C_HEADER_BANNER);
        if (null == adOption) {
            adOption = new Option();
            adOption.setOid(OptionUtil.ID_C_HEADER_BANNER);
            adOption.setOptionCategory( OptionUtil.CATEGORY_C_AD);
            adOption.setOptionValue(headerBanner);

            optionMgmtService.addOption(adOption);
        } else {
            adOption.setOptionValue( headerBanner);

            optionMgmtService.updateOption(OptionUtil.ID_C_HEADER_BANNER, adOption);
        }

//        return "redirect:" +( SpringUtil.getServerPath() + "/admin/ad");
        return "redirect:" + SpringUtil.getServerPath() + "/admin/ad";

    }

    /**
     * Shows ad.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/ad", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showAd(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/ad.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "admin/ad.ftl";

        dataModel.put("sideFullAd", "");
        dataModel.put("headerBanner", "");

        final Option sideAdOption = optionQueryService.getOption(OptionUtil.ID_C_SIDE_FULL_AD);
        if (null != sideAdOption) {
            dataModel.put("sideFullAd", sideAdOption.getOptionValue());
        }

        final Option headerBanner = optionQueryService.getOption(OptionUtil.ID_C_HEADER_BANNER);
        if (null != headerBanner) {
            dataModel.put("headerBanner", headerBanner.getOptionValue());
        }

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Shows add tag.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/add-tag", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showAddTag(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/add-tag.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "admin/add-tag.ftl";

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Adds a tag.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/add-tag", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String addTag(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        String title = StringUtils.trim(request.getParameter(TagUtil.TAG_TITLE));
        try {
            if (Strings.isEmptyOrNull(title)) {
                throw new Exception(langPropsService.get("tagsErrorLabel"));
            }

            title = TagUtil.formatTags(title);

            if (!TagUtil.containsWhiteListTags(title)) {
                if (!TagUtil.TAG_TITLE_PATTERN.matcher(title).matches()) {
                    throw new Exception(langPropsService.get("tagsErrorLabel"));
                }

                if (title.length() > TagUtil.MAX_TAG_TITLE_LENGTH) {
                    throw new Exception(langPropsService.get("tagsErrorLabel"));
                }
            }
        } catch (final Exception e) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();
            String url = "admin/error.ftl";

            dataModel.put(Keys.MSG, e.getMessage());

            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return url;
        }

        final JSONObject admin = (JSONObject) request.getAttribute(User.USER);
        final String userId = admin.optString(Keys.OBJECT_ID);

        String tagId;
        try {
            tagId = tagMgmtService.addTag(userId, title);
        } catch (final Exception e) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();
            String url = "admin/error.ftl";

            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return url;
        }

//        return "redirect:" +(SpringUtil.getServerPath() + "/admin/tag/" + tagId);
        return "redirect:" + SpringUtil.getServerPath() + "/admin/ad";
    }

    /**
     * Sticks an article.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/stick-article", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public String stickArticle(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String articleId = request.getParameter(ArticleUtil.ARTICLE_T_ID);
        articleMgmtService.adminStick(articleId);

//        return "redirect:" +( SpringUtil.getServerPath() + "/admin/articles");
        return "redirect:" + SpringUtil.getServerPath() + "/admin/ad";
    }

    /**
     * Cancels stick an article.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/cancel-stick-article", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public String stickCancelArticle(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String articleId = request.getParameter(ArticleUtil.ARTICLE_T_ID);
        articleMgmtService.adminCancelStick(articleId);

//        return "redirect:" +( SpringUtil.getServerPath() + "/admin/articles");
        return "redirect:" + SpringUtil.getServerPath() + "/admin/ad";
    }

    /**
     * Generates invitecodes.
     *
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/invitecodes/generate", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public String generateInvitecodes(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String quantityStr = request.getParameter("quantity");
        int quantity = 20;
        try {
            quantity = Integer.valueOf(quantityStr);
        } catch (final NumberFormatException e) {
            // ignore
        }

        String memo = request.getParameter("memo");
        if (StringUtils.isBlank(memo)) {
            memo = "注册帖";
        }

        invitecodeMgmtService.adminGenInvitecodes(quantity, memo);

//        return "redirect:" +( SpringUtil.getServerPath() + "/admin/invitecodes");
        return "redirect:" + SpringUtil.getServerPath() + "/admin/ad";
    }

    /**
     * Shows admin invitecodes.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/invitecodes", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showInvitecodes(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/invitecodes.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "admin/invitecodes.ftl";
        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final JSONObject result = invitecodeQueryService.getInvitecodes(requestJSONObject);
        dataModel.put(InvitecodeUtil.INVITECODES, CollectionUtils.jsonArrayToList(result.optJSONArray(InvitecodeUtil.INVITECODES)));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return  url;
    }

    /**
     * Shows an invitecode.
     *
     * @param request      the specified request
     * @param response     the specified response
     * @param invitecodeId the specified invitecode id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/invitecode/{invitecodeId}", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showInvitecode(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                               final String invitecodeId) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/invitecode.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "admin/invitecode.ftl";

        final Invitecode invitecode = invitecodeQueryService.getInvitecodeById(invitecodeId);
        dataModel.put(InvitecodeUtil.INVITECODE, invitecode);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Updates an invitecode.
     *

     * @param request      the specified request
     * @param response     the specified response
     * @param invitecodeId the specified invitecode id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/invitecode/{invitecodeId}", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String updateInvitecode(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                                 final String invitecodeId) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/invitecode.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "admin/invitecode.ftl";

        Invitecode invitecode = invitecodeQueryService.getInvitecodeById(invitecodeId);

        JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(invitecode));

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            jsonObject.put(name, value);
        }

        invitecode = JsonUtil.json2Bean(jsonObject.toString(),Invitecode.class);

        invitecodeMgmtService.updateInvitecode(invitecodeId, invitecode);

        invitecode = invitecodeQueryService.getInvitecodeById(invitecodeId);
        dataModel.put(InvitecodeUtil.INVITECODE, invitecode);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Shows add article.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/add-article", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showAddArticle(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/add-article.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "admin/add-article.ftl";

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Adds an article.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/add-article", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String addArticle(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String userName = request.getParameter(User.USER_NAME);
        final UserExt author = userQueryService.getUserByName(userName);
        if (null == author) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, langPropsService.get("notFoundUserLabel"));
            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return "admin/error.ftl";
        }

        final String timeStr = request.getParameter(Common.TIME);
        final String articleTitle = request.getParameter(ArticleUtil.ARTICLE_TITLE);
        final String articleTags = request.getParameter(ArticleUtil.ARTICLE_TAGS);
        final String articleContent = request.getParameter(ArticleUtil.ARTICLE_CONTENT);
        String rewardContent = request.getParameter(ArticleUtil.ARTICLE_REWARD_CONTENT);
        final String rewardPoint = request.getParameter(ArticleUtil.ARTICLE_REWARD_POINT);

        long time = System.currentTimeMillis();

        try {
            final Date date = DateUtils.parseDate(timeStr, new String[]{"yyyy-MM-dd'T'HH:mm"});

            time = date.getTime();
            final int random = RandomUtils.nextInt(9999);
            time += random;
        } catch (final ParseException e) {
            LOGGER.error( "Parse time failed, using current time instead");
        }

        final JSONObject article = new JSONObject();
        article.put(ArticleUtil.ARTICLE_TITLE, articleTitle);
        article.put(ArticleUtil.ARTICLE_TAGS, articleTags);
        article.put(ArticleUtil.ARTICLE_CONTENT, articleContent);
        article.put(ArticleUtil.ARTICLE_REWARD_CONTENT, rewardContent);
        article.put(ArticleUtil.ARTICLE_REWARD_POINT, Integer.valueOf(rewardPoint));
        article.put(User.USER_NAME, userName);
        article.put(Common.TIME, time);

        try {
            articleMgmtService.addArticleByAdmin(article);
        } catch (final Exception e) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return "admin/error.ftl";
        }

        return "redirect:" +( SpringUtil.getServerPath() + "/admin/articles");
    }

    /**
     * Adds a reserved word.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/add-reserved-word", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public String addReservedWord(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        String word = request.getParameter(Common.WORD);
        word = StringUtils.trim(word);
        if (StringUtils.isBlank(word)) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, langPropsService.get("invalidReservedWordLabel"));
            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return "admin/error.ftl";
        }

        if (optionQueryService.isReservedWord(word)) {
            return "redirect:" +( SpringUtil.getServerPath() + "/admin/reserved-words");
        }

        try {
            final JSONObject reservedWord = new JSONObject();
            reservedWord.put(OptionUtil.OPTION_CATEGORY, OptionUtil.CATEGORY_C_RESERVED_WORDS);
            reservedWord.put(OptionUtil.OPTION_VALUE, word);

            Option option = JsonUtil.json2Bean(reservedWord.toString(),Option.class);
            optionMgmtService.addOption(option);
        } catch (final Exception e) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return "admin/error.ftl";
        }

        return "redirect:" +( SpringUtil.getServerPath() + "/admin/reserved-words");
//        return "redirect:" + SpringUtil.getServerPath() + "/admin/ad";
    }

    /**
     * Shows add reserved word.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/add-reserved-word", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showAddReservedWord(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/add-reserved-word.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return "admin/add-reserved-word.ftl";
    }

    /**
     * Updates a reserved word.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param id       the specified reserved wordid
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/reserved-word/{id}", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String updateReservedWord(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                                   final String id) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/reserved-word.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        final Option word = optionQueryService.getOption(id);
        dataModel.put(Common.WORD, word);

        JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(word));

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            jsonObject.put(name, value);
        }

        Option option = JsonUtil.json2Bean(jsonObject.toString(),Option.class);

        optionMgmtService.updateOption(id, option);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return "admin/reserved-word.ftl";
    }

    /**
     * Shows reserved words.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/reserved-words", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showReservedWords(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/reserved-words.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModel.put(Common.WORDS, optionQueryService.getReservedWords());

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return "admin/reserved-words.ftl";
    }

    /**
     * Shows a reserved word.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param id       the specified reserved word id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/reserved-word/{id}", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showReservedWord(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                                 final String id) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/reserved-word.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        final Option word = optionQueryService.getOption(id);
        dataModel.put(Common.WORD, word);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return "admin/reserved-word.ftl";
    }

    /**
     * Removes a reserved word.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/remove-reserved-word", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public String removeReservedWord(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String id = request.getParameter("id");
        optionMgmtService.removeOption(id);

        return "redirect:" +( SpringUtil.getServerPath() + "/admin/reserved-words");
//        return "redirect:" + SpringUtil.getServerPath() + "/admin/ad";
    }

    /**
     * Removes a comment.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/remove-comment", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public String removeComment(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String commentId = request.getParameter(CommentUtil.COMMENT_T_ID);
        commentMgmtService.removeCommentByAdmin(commentId);

        return "redirect:" +( SpringUtil.getServerPath() + "/admin/comments");
//        return "redirect:" + SpringUtil.getServerPath() + "/admin/ad";
    }

    /**
     * Removes an article.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/remove-article", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public String removeArticle(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String articleId = request.getParameter(ArticleUtil.ARTICLE_T_ID);
        articleMgmtService.removeArticleByAdmin(articleId);

        return "redirect:" +( SpringUtil.getServerPath() + "/admin/articles");
//        return "redirect:" + SpringUtil.getServerPath() + "/admin/ad";
    }

    /**
     * Shows admin index.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showIndex(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/index.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        dataModel.put(Common.ONLINE_VISITOR_CNT, optionQueryService.getOnlineVisitorCount());
        dataModel.put(Common.ONLINE_MEMBER_CNT, optionQueryService.getOnlineMemberCount());

        final JSONObject statistic = optionQueryService.getStatistic();
        dataModel.put(OptionUtil.CATEGORY_C_STATISTIC, statistic);

        return "admin/index.ftl";
    }

    /**
     * Shows admin users.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/users", method = RequestMethod.GET)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showUsers(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/users.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);
        final String query = request.getParameter(Common.QUERY);
        if (!Strings.isEmptyOrNull(query)) {
            requestJSONObject.put(Common.QUERY, query);
        }

        final JSONObject result = userQueryService.getUsers(requestJSONObject);
        dataModel.put(User.USERS, CollectionUtils.jsonArrayToList(result.optJSONArray(User.USERS)));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return "admin/users.ftl";
    }

    /**
     * Shows a user.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userId   the specified user id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/user/{userId}", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showUser(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                         final String userId) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/user.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        final UserExt user = userQueryService.getUser(userId);
        JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(user));
        Escapes.escapeHTML(jsonObject);
        dataModel.put(User.USER, jsonObject);

        final JSONObject result = roleQueryService.getRoles(1, Integer.MAX_VALUE, 10);
        final List<JSONObject> roles = (List<JSONObject>) result.opt(RoleUtil.ROLES);
        dataModel.put(RoleUtil.ROLES, roles);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return "admin/user.ftl";
    }

    /**
     * Shows add user.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/add-user", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showAddUser(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/add-user.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return "admin/add-user.ftl";
    }

    /**
     * Adds a user.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/add-user", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String addUser(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String userName = request.getParameter(User.USER_NAME);
        final String email = request.getParameter(User.USER_EMAIL);
        final String password = request.getParameter(User.USER_PASSWORD);
        final String appRole = request.getParameter(UserExtUtil.USER_APP_ROLE);

        final boolean nameInvalid = UserRegisterValidation.invalidUserName(userName);
        final boolean emailInvalid = !Strings.isEmail(email);
        final boolean passwordInvalid = UserRegister2Validation.invalidUserPassword(password);

        if (nameInvalid || emailInvalid || passwordInvalid) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();

            if (nameInvalid) {
                dataModel.put(Keys.MSG, langPropsService.get("invalidUserNameLabel"));
            } else if (emailInvalid) {
                dataModel.put(Keys.MSG, langPropsService.get("invalidEmailLabel"));
            } else if (passwordInvalid) {
                dataModel.put(Keys.MSG, langPropsService.get("invalidPasswordLabel"));
            }

            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return "admin/error.ftl";
        }

        String userId;
        try {
            final JSONObject user = new JSONObject();
            user.put(User.USER_NAME, userName);
            user.put(User.USER_EMAIL, email);
            user.put(User.USER_PASSWORD, MD5.hash(password));
            user.put(UserExtUtil.USER_APP_ROLE, appRole);
            user.put(UserExtUtil.USER_STATUS, UserExtUtil.USER_STATUS_C_VALID);

            final JSONObject admin = (JSONObject) request.getAttribute(User.USER);
            user.put(UserExtUtil.USER_LANGUAGE, admin.optString(UserExtUtil.USER_LANGUAGE));

            userId = userMgmtService.addUser(user);
        } catch (final Exception e) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return "admin/error.ftl";
        }

        return "redirect:" +( SpringUtil.getServerPath() + "/admin/user/" + userId);
//        return "redirect:" + SpringUtil.getServerPath() + "/admin/ad";
    }

    /**
     * Updates a user.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userId   the specified user id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/user/{userId}", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String updateUser(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                           final String userId) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/user.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        UserExt user = userQueryService.getUser(userId);
        JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(user));

        dataModel.put(User.USER, jsonObject);
        final String oldRole = user.getUserRole();

        final JSONObject result = roleQueryService.getRoles(1, Integer.MAX_VALUE, 10);
        final List<JSONObject> roles = (List<JSONObject>) result.opt(RoleUtil.ROLES);
        dataModel.put(RoleUtil.ROLES, roles);

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            switch (name) {
                case UserExtUtil.USER_POINT:
                case UserExtUtil.USER_APP_ROLE:
                case UserExtUtil.USER_STATUS:
                case UserExtUtil.USER_COMMENT_VIEW_MODE:
                case UserExtUtil.USER_AVATAR_VIEW_MODE:
                case UserExtUtil.USER_LIST_PAGE_SIZE:
                case UserExtUtil.USER_LIST_VIEW_MODE:
                case UserExtUtil.USER_NOTIFY_STATUS:
                case UserExtUtil.USER_SUB_MAIL_STATUS:
                case UserExtUtil.USER_KEYBOARD_SHORTCUTS_STATUS:
                case UserExtUtil.USER_REPLY_WATCH_ARTICLE_STATUS:
                case UserExtUtil.USER_GEO_STATUS:
                case UserExtUtil.USER_ARTICLE_STATUS:
                case UserExtUtil.USER_COMMENT_STATUS:
                case UserExtUtil.USER_FOLLOWING_USER_STATUS:
                case UserExtUtil.USER_FOLLOWING_TAG_STATUS:
                case UserExtUtil.USER_FOLLOWING_ARTICLE_STATUS:
                case UserExtUtil.USER_WATCHING_ARTICLE_STATUS:
                case UserExtUtil.USER_BREEZEMOON_STATUS:
                case UserExtUtil.USER_FOLLOWER_STATUS:
                case UserExtUtil.USER_POINT_STATUS:
                case UserExtUtil.USER_ONLINE_STATUS:
                case UserExtUtil.USER_UA_STATUS:
                case UserExtUtil.USER_TIMELINE_STATUS:
                case UserExtUtil.USER_FORGE_LINK_STATUS:
                case UserExtUtil.USER_JOIN_POINT_RANK:
                case UserExtUtil.USER_JOIN_USED_POINT_RANK:
                    jsonObject.put(name, Integer.valueOf(value));

                    break;
                case User.USER_PASSWORD:
                    final String oldPwd = jsonObject.getString(name);
                    if (!oldPwd.equals(value) && !Strings.isEmptyOrNull(value)) {
                        jsonObject.put(name, MD5.hash(value));
                    }

                    break;
                case UserExtUtil.SYNC_TO_CLIENT:
                    jsonObject.put(UserExtUtil.SYNC_TO_CLIENT, Boolean.valueOf(value));

                    break;
                default:
                    jsonObject.put(name, value);

                    break;
            }
        }

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        if (!RoleUtil.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))) {
            jsonObject.put(User.USER_ROLE, oldRole);
        }

        user = JsonUtil.json2Bean(jsonObject.toString(),UserExt.class);
        userMgmtService.updateUser(userId, user);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        return "admin/user.ftl";
    }

    /**
     * Updates a user's email.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userId   the specified user id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/user/{userId}/email", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String updateUserEmail(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                                final String userId) throws Exception {
        final UserExt user = userQueryService.getUser(userId);
        final String oldEmail = user.getUserEmail();
        final String newEmail = request.getParameter(User.USER_EMAIL);

        if (oldEmail.equals(newEmail)) {
            return "redirect:" +( SpringUtil.getServerPath() + "/admin/user/" + userId);
//            return "redirect:" + SpringUtil.getServerPath() + "/admin/ad";
//            return;
        }

        user.setUserEmail(newEmail);

        try {
            userMgmtService.updateUserEmail(userId, user);
        } catch (final Exception e) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return "admin/error.ftl";
        }

        return "redirect:" +( SpringUtil.getServerPath() + "/admin/user/" + userId);
//        return "redirect:" + SpringUtil.getServerPath() + "/admin/ad";
    }

    /**
     * Updates a user's username.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userId   the specified user id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/user/{userId}/username", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String updateUserName(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                               final String userId) throws Exception {
        final UserExt user = userQueryService.getUser(userId);
        final String oldUserName = user.getUserName();
        final String newUserName = request.getParameter(User.USER_NAME);

        if (oldUserName.equals(newUserName)) {
            return "redirect:" + ( SpringUtil.getServerPath() + "/admin/user/" + userId);
//            return "redirect:" + SpringUtil.getServerPath() + "/admin/ad";
//            return;
        }

        user.setUserName(newUserName);

        try {
            userMgmtService.updateUserName(userId, user);
        } catch (final Exception e) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return "admin/error.ftl";
        }

        return "redirect:" +( SpringUtil.getServerPath() + "/admin/user/" + userId);
    }

    /**
     * Charges a user's point.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userId   the specified user id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/user/{userId}/charge-point", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String chargePoint(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                            final String userId) throws Exception {
        final String pointStr = request.getParameter(Common.POINT);
        final String memo = request.getParameter("memo");

        if (StringUtils.isBlank(pointStr) || StringUtils.isBlank(memo) || !Strings.isNumeric(memo.split("-")[0])) {
            LOGGER.warn("Charge point memo format error");

            return "redirect:" +(SpringUtil.getServerPath() + "/admin/user/" + userId);

//            return;
        }

        try {
            final int point = Integer.valueOf(pointStr);

            final String transferId = pointtransferMgmtService.transfer(PointtransferUtil.ID_C_SYS, userId,
                    PointtransferUtil.TRANSFER_TYPE_C_CHARGE, point, memo, System.currentTimeMillis());

            final JSONObject notification = new JSONObject();
            notification.put(NotificationUtil.NOTIFICATION_USER_ID, userId);
            notification.put(NotificationUtil.NOTIFICATION_DATA_ID, transferId);

            notificationMgmtService.addPointChargeNotification(notification);
//        } catch (final NumberFormatException | Exception e) {
        } catch (Exception e) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return "admin/error.ftl";
        }

        return "redirect:" +(SpringUtil.getServerPath() + "/admin/user/" + userId);
    }

    /**
     * Deducts a user's abuse point.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userId   the specified user id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/user/{userId}/abuse-point", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String abusePoint(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                           final String userId) throws Exception {
        final String pointStr = request.getParameter(Common.POINT);

        try {
            final int point = Integer.valueOf(pointStr);

            final UserExt user = userQueryService.getUser(userId);
            final int currentPoint = user.getUserPoint();

            if (currentPoint - point < 0) {
//                final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//                context.setRenderer(renderer);
//                renderer.setTemplateName("admin/error.ftl");
//                final Map<String, Object> dataModel = renderer.getDataModel();

                dataModel.put(Keys.MSG, langPropsService.get("insufficientBalanceLabel"));
                dataModelService.fillHeaderAndFooter(request, response, dataModel);

                return "admin/error.ftl";
            }

            final String memo = request.getParameter(Common.MEMO);

            final String transferId = pointtransferMgmtService.transfer(userId, PointtransferUtil.ID_C_SYS,
                    PointtransferUtil.TRANSFER_TYPE_C_ABUSE_DEDUCT, point, memo, System.currentTimeMillis());

            final JSONObject notification = new JSONObject();
            notification.put(NotificationUtil.NOTIFICATION_USER_ID, userId);
            notification.put(NotificationUtil.NOTIFICATION_DATA_ID, transferId);

            notificationMgmtService.addAbusePointDeductNotification(notification);
        } catch (final Exception e) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return "admin/error.ftl";
        }

        return "redirect:" +(SpringUtil.getServerPath() + "/admin/user/" + userId);
    }

    /**
     * Compensates a user's initial point.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userId   the specified user id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/user/{userId}/init-point", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String initPoint(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                          final String userId) throws Exception {
        try {
            final UserExt user = userQueryService.getUser(userId);
            if (null == user
                    || UserExtUtil.USER_STATUS_C_VALID != user.getUserStatus()
                    || UserExtUtil.NULL_USER_NAME.equals(user.getRoleName())) {
                return "redirect:" + (SpringUtil.getServerPath() + "/admin/user/" + userId);

//                return;
            }

            final List<Pointtransfer> records
                    = pointtransferQueryService.getLatestPointtransfers(userId, PointtransferUtil.TRANSFER_TYPE_C_INIT, 1);
            if (records.isEmpty()) {
                pointtransferMgmtService.transfer(PointtransferUtil.ID_C_SYS, userId, PointtransferUtil.TRANSFER_TYPE_C_INIT,
                        PointtransferUtil.TRANSFER_SUM_C_INIT, userId, Long.valueOf(userId));
            }
//        } catch (final IOException | NumberFormatException | Exception e) {
        }catch (Exception e) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return "admin/error.ftl";
        }

        return "redirect:" +(SpringUtil.getServerPath() + "/admin/user/" + userId);
    }

    /**
     * Exchanges a user's point.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param userId   the specified user id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/user/{userId}/exchange-point", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String exchangePoint(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                              final String userId) throws Exception {
        final String pointStr = request.getParameter(Common.POINT);

        try {
            final int point = Integer.valueOf(pointStr);

            final UserExt user = userQueryService.getUser(userId);
            final int currentPoint = user.getUserPoint();

            if (currentPoint - point < Symphonys.getInt("pointExchangeMin")) {
//                final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//                context.setRenderer(renderer);
//                renderer.setTemplateName("admin/error.ftl");
//                final Map<String, Object> dataModel = renderer.getDataModel();

                dataModel.put(Keys.MSG, langPropsService.get("insufficientBalanceLabel"));
                dataModelService.fillHeaderAndFooter(request, response, dataModel);

                return "admin/error.ftl";
            }

            final String memo = String.valueOf(Math.floor(point / (double) Symphonys.getInt("pointExchangeUnit")));

            final String transferId = pointtransferMgmtService.transfer(userId, PointtransferUtil.ID_C_SYS,
                    PointtransferUtil.TRANSFER_TYPE_C_EXCHANGE, point, memo, System.currentTimeMillis());

            final JSONObject notification = new JSONObject();
            notification.put(NotificationUtil.NOTIFICATION_USER_ID, userId);
            notification.put(NotificationUtil.NOTIFICATION_DATA_ID, transferId);

            notificationMgmtService.addPointExchangeNotification(notification);
        } catch (final Exception e) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();

            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return "admin/error.ftl";
        }

        return "redirect:" +(SpringUtil.getServerPath() + "/admin/user/" + userId);
    }

    /**
     * Shows admin articles.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/articles", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showArticles(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/articles.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final String articleId = request.getParameter("id");
        if (!Strings.isEmptyOrNull(articleId)) {
            requestJSONObject.put(Keys.OBJECT_ID, articleId);
        }

        final Map<String, Class<?>> articleFields = new HashMap<>();
        articleFields.put(Keys.OBJECT_ID, String.class);
        articleFields.put(ArticleUtil.ARTICLE_TITLE, String.class);
        articleFields.put(ArticleUtil.ARTICLE_PERMALINK, String.class);
        articleFields.put(ArticleUtil.ARTICLE_CREATE_TIME, Long.class);
        articleFields.put(ArticleUtil.ARTICLE_VIEW_CNT, Integer.class);
        articleFields.put(ArticleUtil.ARTICLE_COMMENT_CNT, Integer.class);
        articleFields.put(ArticleUtil.ARTICLE_AUTHOR_ID, String.class);
        articleFields.put(ArticleUtil.ARTICLE_TAGS, String.class);
        articleFields.put(ArticleUtil.ARTICLE_STATUS, Integer.class);
        articleFields.put(ArticleUtil.ARTICLE_STICK, Long.class);

        final int avatarViewMode = (int) request.getAttribute(UserExtUtil.USER_AVATAR_VIEW_MODE);

        final JSONObject result = articleQueryService.getArticles(avatarViewMode, requestJSONObject, articleFields);
        dataModel.put(ArticleUtil.ARTICLES, CollectionUtils.jsonArrayToList(result.optJSONArray(ArticleUtil.ARTICLES)));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        return "admin/articles.ftl";
    }

    /**
     * Shows an article.
     *
     * @param request   the specified request
     * @param response  the specified response
     * @param articleId the specified article id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/article/{articleId}", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showArticle(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                            final String articleId) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/article.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        final Article article = articleQueryService.getArticle(articleId);

        JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(article));
        Escapes.escapeHTML(jsonObject);
        dataModel.put(ArticleUtil.ARTICLE, jsonObject);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return "admin/article.ftl";
    }

    /**
     * Updates an article.
     *
     * @param request   the specified request
     * @param response  the specified response
     * @param articleId the specified article id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/article/{articleId}", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String updateArticle(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                              final String articleId) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/article.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "admin/article.ftl";

        Article article = articleQueryService.getArticle(articleId);
        JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(article));

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            if (name.equals(ArticleUtil.ARTICLE_REWARD_POINT)
                    || name.equals(ArticleUtil.ARTICLE_QNA_OFFER_POINT)
                    || name.equals(ArticleUtil.ARTICLE_STATUS)
                    || name.equals(ArticleUtil.ARTICLE_TYPE)
                    || name.equals(ArticleUtil.ARTICLE_GOOD_CNT)
                    || name.equals(ArticleUtil.ARTICLE_BAD_CNT)
                    || name.equals(ArticleUtil.ARTICLE_PERFECT)
                    || name.equals(ArticleUtil.ARTICLE_ANONYMOUS_VIEW)
                    || name.equals(ArticleUtil.ARTICLE_PUSH_ORDER)) {
                jsonObject.put(name, Integer.valueOf(value));
            } else {
                jsonObject.put(name, value);
            }
        }

        final String articleTags = TagUtil.formatTags(jsonObject.optString(ArticleUtil.ARTICLE_TAGS));
        jsonObject.put(ArticleUtil.ARTICLE_TAGS, articleTags);

        article = JsonUtil.json2Bean(jsonObject.toString(),Article.class);
        articleMgmtService.updateArticleByAdmin(articleId, article);

        article = articleQueryService.getArticle(articleId);
        dataModel.put(ArticleUtil.ARTICLE, article);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Shows admin comments.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/comments", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showComments(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/comments.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "admin/comments.ftl";

        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final Map<String, Class<?>> commentFields = new HashMap<>();
        commentFields.put(Keys.OBJECT_ID, String.class);
        commentFields.put(CommentUtil.COMMENT_CREATE_TIME, String.class);
        commentFields.put(CommentUtil.COMMENT_AUTHOR_ID, String.class);
        commentFields.put(CommentUtil.COMMENT_ON_ARTICLE_ID, String.class);
        commentFields.put(CommentUtil.COMMENT_SHARP_URL, String.class);
        commentFields.put(CommentUtil.COMMENT_STATUS, Integer.class);
        commentFields.put(CommentUtil.COMMENT_CONTENT, String.class);

        final int avatarViewMode = (int) request.getAttribute(UserExtUtil.USER_AVATAR_VIEW_MODE);

        final JSONObject result = commentQueryService.getComments(avatarViewMode, requestJSONObject, commentFields);
        dataModel.put(CommentUtil.COMMENTS, CollectionUtils.jsonArrayToList(result.optJSONArray(CommentUtil.COMMENTS)));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Shows a comment.
     *
     * @param request   the specified request
     * @param response  the specified response
     * @param commentId the specified comment id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/comment/{commentId}", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showComment(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                            final String commentId) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/comment.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "admin/comment.ftl";

        final Comment comment = commentQueryService.getComment(commentId);
        JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(comment));
        Escapes.escapeHTML(jsonObject);
        dataModel.put(CommentUtil.COMMENT, jsonObject);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Updates a comment.
     *
     * @param request   the specified request
     * @param response  the specified response
     * @param commentId the specified comment id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/comment/{commentId}", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String updateComment(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                              final String commentId) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/comment.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "admin/comment.ftl";

        Comment comment = commentQueryService.getComment(commentId);
        JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(comment));

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            if (name.equals(CommentUtil.COMMENT_STATUS)
                    || name.equals(CommentUtil.COMMENT_GOOD_CNT)
                    || name.equals(CommentUtil.COMMENT_BAD_CNT)) {
                jsonObject.put(name, Integer.valueOf(value));
            } else {
                jsonObject.put(name, value);
            }
        }

        commentMgmtService.updateComment(commentId, jsonObject);

        comment = commentQueryService.getComment(commentId);
        dataModel.put(CommentUtil.COMMENT, comment);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Shows admin miscellaneous.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/misc", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showMisc(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/misc.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "admin/misc.ftl";

        final List<JSONObject> misc = optionQueryService.getMisc();
        dataModel.put(OptionUtil.OPTIONS, misc);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Updates admin miscellaneous.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/misc", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String updateMisc(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/misc.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "admin/misc.ftl";

        List<JSONObject> misc = new ArrayList<>();

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            final JSONObject option = new JSONObject();
            option.put(Keys.OBJECT_ID, name);
            option.put(OptionUtil.OPTION_VALUE, value);
            option.put(OptionUtil.OPTION_CATEGORY, OptionUtil.CATEGORY_C_MISC);

            misc.add(option);
        }

        for (final JSONObject option : misc) {
            Option option1 = JsonUtil.json2Bean(option.toString(),Option.class);
            optionMgmtService.updateOption(option.getString(Keys.OBJECT_ID), option1);
        }

        misc = optionQueryService.getMisc();
        dataModel.put(OptionUtil.OPTIONS, misc);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Shows admin tags.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/tags", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showTags(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/tags.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "admin/tags.ftl";
        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final String tagTitle = request.getParameter(Common.TITLE);
        if (!Strings.isEmptyOrNull(tagTitle)) {
            requestJSONObject.put(TagUtil.TAG_TITLE, tagTitle);
        }

        final Map<String, Class<?>> tagFields = new HashMap<>();
        tagFields.put(Keys.OBJECT_ID, String.class);
        tagFields.put(TagUtil.TAG_TITLE, String.class);
        tagFields.put(TagUtil.TAG_DESCRIPTION, String.class);
        tagFields.put(TagUtil.TAG_ICON_PATH, String.class);
        tagFields.put(TagUtil.TAG_COMMENT_CNT, Integer.class);
        tagFields.put(TagUtil.TAG_REFERENCE_CNT, Integer.class);
        tagFields.put(TagUtil.TAG_FOLLOWER_CNT, Integer.class);
        tagFields.put(TagUtil.TAG_STATUS, Integer.class);
        tagFields.put(TagUtil.TAG_GOOD_CNT, Integer.class);
        tagFields.put(TagUtil.TAG_BAD_CNT, Integer.class);
        tagFields.put(TagUtil.TAG_URI, String.class);
        tagFields.put(TagUtil.TAG_CSS, String.class);

        final JSONObject result = tagQueryService.getTags(requestJSONObject, tagFields);
        dataModel.put(TagUtil.TAGS, CollectionUtils.jsonArrayToList(result.optJSONArray(TagUtil.TAGS)));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Shows a tag.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param tagId    the specified tag id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/tag/{tagId}", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showTag(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                        final String tagId) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/tag.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "admin/tag.ftl";

        final Tag tag = tagQueryService.getTag(tagId);
        dataModel.put(TagUtil.TAG, tag);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Updates a tag.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param tagId    the specified tag id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/tag/{tagId}", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String updateTag(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                          final String tagId) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/tag.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "admin/tag.ftl";

        Tag tag = tagQueryService.getTag(tagId);

        final String oldTitle = tag.getTagTitle();

        JSONObject jsonObject =new JSONObject( JsonUtil.objectToJson(tag));

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = request.getParameter(name);

            if (name.equals(TagUtil.TAG_REFERENCE_CNT)
                    || name.equals(TagUtil.TAG_COMMENT_CNT)
                    || name.equals(TagUtil.TAG_FOLLOWER_CNT)
                    || name.contains(TagUtil.TAG_LINK_CNT)
                    || name.contains(TagUtil.TAG_STATUS)
                    || name.equals(TagUtil.TAG_GOOD_CNT)
                    || name.equals(TagUtil.TAG_BAD_CNT)) {
                jsonObject.put(name, Integer.valueOf(value));
            } else {
                jsonObject.put(name, value);
            }
        }

        final String newTitle = jsonObject.optString(TagUtil.TAG_TITLE);

        tag = JsonUtil.json2Bean(jsonObject.toString(),Tag.class);
        if (oldTitle.equalsIgnoreCase(newTitle)) {
            tagMgmtService.updateTag(tagId, tag);
        }

        tag = tagQueryService.getTag(tagId);
        dataModel.put(TagUtil.TAG, tag);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Shows admin domains.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/domains", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showDomains(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/domains.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "admin/domains.ftl";

        final int pageNum = Paginator.getPage(request);
        final int pageSize = PAGE_SIZE;
        final int windowSize = WINDOW_SIZE;

        final JSONObject requestJSONObject = new JSONObject();
        requestJSONObject.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        requestJSONObject.put(Pagination.PAGINATION_PAGE_SIZE, pageSize);
        requestJSONObject.put(Pagination.PAGINATION_WINDOW_SIZE, windowSize);

        final String domainTitle = request.getParameter(Common.TITLE);
        if (!Strings.isEmptyOrNull(domainTitle)) {
            requestJSONObject.put(DomainUtil.DOMAIN_TITLE, domainTitle);
        }

        final Map<String, Class<?>> domainFields = new HashMap<>();
        domainFields.put(Keys.OBJECT_ID, String.class);
        domainFields.put(DomainUtil.DOMAIN_TITLE, String.class);
        domainFields.put(DomainUtil.DOMAIN_DESCRIPTION, String.class);
        domainFields.put(DomainUtil.DOMAIN_ICON_PATH, String.class);
        domainFields.put(DomainUtil.DOMAIN_STATUS, String.class);
        domainFields.put(DomainUtil.DOMAIN_URI, String.class);

        final JSONObject result = domainQueryService.getDomains(requestJSONObject, domainFields);
        dataModel.put(Common.ALL_DOMAINS, CollectionUtils.jsonArrayToList(result.optJSONArray(DomainUtil.DOMAINS)));

        final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
        final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONArray pageNums = pagination.optJSONArray(Pagination.PAGINATION_PAGE_NUMS);
        dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.opt(0));
        dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.opt(pageNums.length() - 1));
        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, CollectionUtils.jsonArrayToList(pageNums));

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Shows a domain.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param domainId the specified domain id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/domain/{domainId}", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showDomain(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                           final String domainId) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/domain.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "admin/domain.ftl";

        final Domain domain = domainQueryService.getDomain(domainId);
        dataModel.put(DomainUtil.DOMAIN, domain);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Updates a domain.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param domainId the specified domain id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/domain/{domainId}", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String updateDomain(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                             final String domainId) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/domain.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "admin/domain.ftl";

        Domain domain = domainQueryService.getDomain(domainId);
        final String oldTitle = domain.getDomainTitle();
        JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(domain));

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            String value = request.getParameter(name);

            if (DomainUtil.DOMAIN_ICON_PATH.equals(name)) {
                value = StringUtils.replace(value, "\"", "'");
            }

            jsonObject.put(name, value);
        }

        jsonObject.remove(DomainUtil.DOMAIN_T_TAGS);

        final String newTitle = jsonObject.optString(DomainUtil.DOMAIN_TITLE);

        domain = JsonUtil.json2Bean(jsonObject.toString(),Domain.class);
        if (oldTitle.equalsIgnoreCase(newTitle)) {
            domainMgmtService.updateDomain(domainId, domain);
        }

        domain = domainQueryService.getDomain(domainId);
        dataModel.put(DomainUtil.DOMAIN, domain);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Shows add domain.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/add-domain", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showAddDomain(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("admin/add-domain.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "admin/add-domain.ftl";

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return url;
    }

    /**
     * Adds a domain.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/add-domain", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String addDomain(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String domainTitle = request.getParameter(DomainUtil.DOMAIN_TITLE);

        if (StringUtils.isBlank(domainTitle)) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();
            String url = "admin/error.ftl";

            dataModel.put(Keys.MSG, langPropsService.get("invalidDomainTitleLabel"));

            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return url;
        }

        if (null != domainQueryService.getByTitle(domainTitle)) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();
            String url = "admin/error.ftl";

            dataModel.put(Keys.MSG, langPropsService.get("duplicatedDomainLabel"));

            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return url;
        }

        String domainId;
        try {
            final JSONObject domain = new JSONObject();
            domain.put(DomainUtil.DOMAIN_TITLE, domainTitle);
            domain.put(DomainUtil.DOMAIN_URI, domainTitle);
            domain.put(DomainUtil.DOMAIN_DESCRIPTION, domainTitle);
            domain.put(DomainUtil.DOMAIN_STATUS, DomainUtil.DOMAIN_STATUS_C_VALID);

            domainId = domainMgmtService.addDomain(domain);
        } catch (final Exception e) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();
            String url = "admin/error.ftl";

            dataModel.put(Keys.MSG, e.getMessage());
            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return url;
        }

        return "redirect:" +(SpringUtil.getServerPath() + "/admin/domain/" + domainId);
    }

    /**
     * Removes a domain.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/remove-domain", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public String removeDomain(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String domainId = request.getParameter(DomainUtil.DOMAIN_T_ID);
        domainMgmtService.removeDomain(domainId);

        return "redirect:" +(SpringUtil.getServerPath() + "/admin/domains");
    }

    /**
     * Adds a tag into a domain.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param domainId the specified domain id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/domain/{domainId}/add-tag", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String addDomainTag(Map<String, Object> dataModel,
                             final HttpServletRequest request, final HttpServletResponse response, final String domainId)
            throws Exception {
        String tagTitle = request.getParameter(TagUtil.TAG_TITLE);
        final Tag tag = tagQueryService.getTagByTitle(tagTitle);

        String tagId;
        if (tag != null) {
            tagId = tag.getOid();
        } else {
            try {
                if (Strings.isEmptyOrNull(tagTitle)) {
                    throw new Exception(langPropsService.get("tagsErrorLabel"));
                }

                tagTitle = TagUtil.formatTags(tagTitle);

                if (!TagUtil.containsWhiteListTags(tagTitle)) {
                    if (!TagUtil.TAG_TITLE_PATTERN.matcher(tagTitle).matches()) {
                        throw new Exception(langPropsService.get("tagsErrorLabel"));
                    }

                    if (tagTitle.length() > TagUtil.MAX_TAG_TITLE_LENGTH) {
                        throw new Exception(langPropsService.get("tagsErrorLabel"));
                    }
                }
            } catch (final Exception e) {
//                final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//                context.setRenderer(renderer);
//                renderer.setTemplateName("admin/error.ftl");
//                final Map<String, Object> dataModel = renderer.getDataModel();
                String url = "admin/error.ftl";

                dataModel.put(Keys.MSG, e.getMessage());

                dataModelService.fillHeaderAndFooter(request, response, dataModel);

                return url;
            }

            final JSONObject admin = (JSONObject) request.getAttribute(User.USER);
            final String userId = admin.optString(Keys.OBJECT_ID);

            try {
                tagId = tagMgmtService.addTag(userId, tagTitle);
            } catch (final Exception e) {
//                final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//                context.setRenderer(renderer);
//                renderer.setTemplateName("admin/error.ftl");
//                final Map<String, Object> dataModel = renderer.getDataModel();
                String url = "admin/error.ftl";

                dataModel.put(Keys.MSG, e.getMessage());
                dataModelService.fillHeaderAndFooter(request, response, dataModel);

                return url;
            }
        }

        if (domainQueryService.containTag(tagTitle, domainId)) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();
            String url = "admin/error.ftl";

            String msg = langPropsService.get("domainContainTagLabel");
            msg = msg.replace("{tag}", tagTitle);

            dataModel.put(Keys.MSG, msg);

            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return url;
        }

        final DomainTag domainTag = new DomainTag();
        domainTag.setDomain_oId(domainId);
        domainTag.setTag_oId(tagId);

        domainMgmtService.addDomainTag(domainTag);

        return "redirect:" +(SpringUtil.getServerPath() + "/admin/domain/" + domainId);
    }

    /**
     * Removes a tag from a domain.
     *

     * @param request  the specified request
     * @param response the specified response
     * @param domainId the specified domain id
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/domain/{domainId}/remove-tag", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @PermissionCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String removeDomain(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                             final String domainId)
            throws Exception {
        final String tagTitle = request.getParameter(TagUtil.TAG_TITLE);
        final Tag tag = tagQueryService.getTagByTitle(tagTitle);

        if (null == tag) {
//            final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//            context.setRenderer(renderer);
//            renderer.setTemplateName("admin/error.ftl");
//            final Map<String, Object> dataModel = renderer.getDataModel();
            String url = "admin/error.ftl";

            dataModel.put(Keys.MSG, langPropsService.get("invalidTagLabel"));

            dataModelService.fillHeaderAndFooter(request, response, dataModel);

            return url;
        }

        final JSONObject domainTag = new JSONObject();
        domainTag.put(DomainUtil.DOMAIN + "_" + Keys.OBJECT_ID, domainId);
        domainTag.put(TagUtil.TAG + "_" + Keys.OBJECT_ID, tag.getOid());

        domainMgmtService.removeDomainTag(domainId, tag.getOid());

        return "redirect:" +(SpringUtil.getServerPath() + "/admin/domain/" + domainId);
    }

    /**
     * Search index.
     *
     */
    @RequestMapping(value = "/admin/search/index", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public void searchIndex(Map<String, Object> dataModel) {
        dataModel.put(Keys.STATUS_CODE,true);
//        dataModel.put(Keys.STATUS_CODE,true);

        if (Symphonys.getBoolean("es.enabled")) {
            searchMgmtService.rebuildESIndex();
        }

        if (Symphonys.getBoolean("algolia.enabled")) {
            searchMgmtService.rebuildAlgoliaIndex();
        }

        new Thread(() -> {
            try {
                final JSONObject stat = optionQueryService.getStatistic();
                final int articleCount = stat.optInt(OptionUtil.ID_C_STATISTIC_ARTICLE_COUNT);
                final int pages = (int) Math.ceil((double) articleCount / 50.0);

                for (int pageNum = 1; pageNum <= pages; pageNum++) {
                    final List<JSONObject> articles = articleQueryService.getValidArticles(pageNum, 50, ArticleUtil.ARTICLE_TYPE_C_NORMAL, ArticleUtil.ARTICLE_TYPE_C_CITY_BROADCAST);

                    for (final JSONObject article : articles) {
                        if (Symphonys.getBoolean("algolia.enabled")) {
                            searchMgmtService.updateAlgoliaDocument(article);
                        }

                        if (Symphonys.getBoolean("es.enabled")) {
                            searchMgmtService.updateESDocument(article, ArticleUtil.ARTICLE);
                        }
                    }

                    LOGGER.info("Indexed page [" + pageNum + "]");
                }

                LOGGER.info("Index finished");
            } catch (final Exception e) {
                LOGGER.error( "Search index failed", e);
            } finally {
                // TODO: 2018/8/20 下面一句怎么改
//                JdbcRepository.dispose();
            }
        }).start();
    }

    /**
     * Search index one article.
     *
     * @throws Exception exception
     */
    @RequestMapping(value = "/admin/search-index-article", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public void searchIndexArticle(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String articleId = request.getParameter(ArticleUtil.ARTICLE_T_ID);
        final Article article = articleQueryService.getArticle(articleId);

        if (null == article || ArticleUtil.ARTICLE_TYPE_C_DISCUSSION == article.getArticleType()
                || ArticleUtil.ARTICLE_TYPE_C_THOUGHT == article.getArticleType()) {
            return;
        }

        JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(article));

        if (Symphonys.getBoolean("algolia.enabled")) {
            searchMgmtService.updateAlgoliaDocument(jsonObject);

            final String articlePermalink = SpringUtil.getServerPath() + article.getArticlePermalink();
            ArticleBaiduSender.sendToBaidu(articlePermalink);
        }

        if (Symphonys.getBoolean("es.enabled")) {
            searchMgmtService.updateESDocument(jsonObject, ArticleUtil.ARTICLE);

            final String articlePermalink = SpringUtil.getServerPath() + article.getArticlePermalink();
            ArticleBaiduSender.sendToBaidu(articlePermalink);
        }

        response.sendRedirect(SpringUtil.getServerPath() + "/admin/articles");
    }
}
