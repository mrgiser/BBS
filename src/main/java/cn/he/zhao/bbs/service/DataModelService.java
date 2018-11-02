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
package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.cache.DomainCache;
import cn.he.zhao.bbs.entityUtil.*;
import cn.he.zhao.bbs.spring.Common;
import cn.he.zhao.bbs.spring.Locales;
import cn.he.zhao.bbs.spring.SpringUtil;
import cn.he.zhao.bbs.spring.Stopwatchs;
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.util.Sessions;
import cn.he.zhao.bbs.util.Symphonys;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Data entity service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.12.2.34, Apr 3, 2018
 * @since 0.2.0
 */
@Service
public class DataModelService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataModelService.class);

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
     * Article query service.
     */
    @Autowired
    private ArticleQueryService articleQueryService;

    /**
     * Tag query service.
     */
    @Autowired
    private TagQueryService tagQueryService;

    /**
     * OptionUtil query service.
     */
    @Autowired
    private OptionQueryService optionQueryService;

    /**
     * User management service.
     */
    @Autowired
    private UserMgmtService userMgmtService;

    /**
     * User query service.
     */
    @Autowired
    private UserQueryService userQueryService;

    /**
     * Avatar query service.
     */
    @Autowired
    private AvatarQueryService avatarQueryService;

    /**
     * Activity query service.
     */
    @Autowired
    private ActivityQueryService activityQueryService;

    /**
     * LivenessUtil query service.
     */
    @Autowired
    private LivenessQueryService livenessQueryService;

    /**
     * RoleUtil query service.
     */
    @Autowired
    private RoleQueryService roleQueryService;

    /**
     * Domain cache.
     */
    @Autowired
    private DomainCache domainCache;

    /**
     * Fills relevant articles.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param dataModel      the specified data entity
     * @param article        the specified article
     * @throws Exception exception
     */
    public void fillRelevantArticles(final int avatarViewMode,
                                     final Map<String, Object> dataModel, final JSONObject article) throws Exception {
        final int articleStatus = article.optInt(ArticleUtil.ARTICLE_STATUS);
        if (ArticleUtil.ARTICLE_STATUS_C_INVALID == articleStatus) {
            dataModel.put(Common.SIDE_RELEVANT_ARTICLES, Collections.emptyList());

            return;
        }

        Stopwatchs.start("Fills relevant articles");
        try {
            dataModel.put(Common.SIDE_RELEVANT_ARTICLES,
                    articleQueryService.getRelevantArticles(
                            avatarViewMode, article, Symphonys.getInt("sideRelevantArticlesCnt")));
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills the latest comments.
     *
     * @param dataModel the specified data entity
     * @throws Exception exception
     */
    public void fillLatestCmts(final Map<String, Object> dataModel) throws Exception {
        Stopwatchs.start("Fills latest comments");
        try {
            // dataModel.put(Common.SIDE_LATEST_CMTS, commentQueryService.getLatestComments(Symphonys.getInt("sizeLatestCmtsCnt")));
            dataModel.put(Common.SIDE_LATEST_CMTS, (Object) Collections.emptyList());
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills random articles.
     *
     * @param dataModel the specified data entity
     */
    public void fillRandomArticles(final Map<String, Object> dataModel) {
        Stopwatchs.start("Fills random articles");
        try {
            dataModel.put(Common.SIDE_RANDOM_ARTICLES, articleQueryService.getSideRandomArticles());
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills side hot articles.
     *
     * @param dataModel the specified data entity
     */
    public void fillSideHotArticles(final Map<String, Object> dataModel) {
        Stopwatchs.start("Fills hot articles");
        try {
            dataModel.put(Common.SIDE_HOT_ARTICLES, articleQueryService.getSideHotArticles());
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills tags.
     *
     * @param dataModel the specified data entity
     * @throws Exception exception
     */
    public void fillSideTags(final Map<String, Object> dataModel) throws Exception {
        Stopwatchs.start("Fills side tags");
        try {
            dataModel.put(Common.SIDE_TAGS, tagQueryService.getTags(Symphonys.getInt("sideTagsCnt")));

            if (!(Boolean) dataModel.get(Common.IS_MOBILE)) {
                fillNewTags(dataModel);
            }
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills index tags.
     *
     * @param dataModel the specified data entity
     * @throws Exception exception
     */
    public void fillIndexTags(final Map<String, Object> dataModel) throws Exception {
        Stopwatchs.start("Fills index tags");
        try {
            for (int i = 0; i < 13; i++) {
                final JSONObject tag = new JSONObject();
                tag.put(TagUtil.TAG_URI, "Sym");
                tag.put(TagUtil.TAG_ICON_PATH, "sym.png");
                tag.put(TagUtil.TAG_TITLE, "Sym");

                dataModel.put(TagUtil.TAG + i, tag);
            }

            final List<JSONObject> tags = tagQueryService.getTags(Symphonys.getInt("sideTagsCnt"));
            for (int i = 0; i < tags.size(); i++) {
                dataModel.put(TagUtil.TAG + i, tags.get(i));
            }

            dataModel.put(TagUtil.TAGS, tags);
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills header.
     *
     * @param request   the specified request
     * @param response  the specified response
     * @param dataModel the specified data entity
     * @throws Exception exception
     */
    private void fillHeader(final HttpServletRequest request, final HttpServletResponse response,
                            final Map<String, Object> dataModel) throws Exception {
        fillMinified(dataModel);
        dataModel.put(Common.STATIC_RESOURCE_VERSION, SpringUtil.getStaticResourceVersion());
        dataModel.put("esEnabled", Symphonys.getBoolean("es.enabled"));
        dataModel.put("algoliaEnabled", Symphonys.getBoolean("algolia.enabled"));
        dataModel.put("algoliaAppId", Symphonys.get("algolia.appId"));
        dataModel.put("algoliaSearchKey", Symphonys.get("algolia.searchKey"));
        dataModel.put("algoliaIndex", Symphonys.get("algolia.index"));

        // fillTrendTags(dataModel);
        fillPersonalNav(request, response, dataModel);

        fillLangs(dataModel);
        fillSideAd(dataModel);
        fillHeaderBanner(dataModel);
        fillSideTips(dataModel);

        fillDomainNav(dataModel);
    }

    /**
     * Fills domain navigation.
     *
     * @param dataModel the specified data entity
     */
    private void fillDomainNav(final Map<String, Object> dataModel) {
        Stopwatchs.start("Fills domain nav");
        try {
            dataModel.put(DomainUtil.DOMAINS, domainCache.getDomains(Integer.MAX_VALUE));
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills footer.
     *
     * @param dataModel the specified data entity
     * @throws Exception exception
     */
    private void fillFooter(final Map<String, Object> dataModel) throws Exception {
        fillSysInfo(dataModel);

        dataModel.put(Common.YEAR, String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));
        dataModel.put(Common.SITE_VISIT_STAT_CODE, Symphonys.get("siteVisitStatCode"));
        dataModel.put(Common.MOUSE_EFFECTS, RandomUtils.nextDouble() > 0.95);
        dataModel.put(Common.MACRO_HEAD_PC_CODE, Symphonys.get(Common.MACRO_HEAD_PC_CODE));
        dataModel.put(Common.MACRO_HEAD_MOBILE_CODE, Symphonys.get(Common.MACRO_HEAD_MOBILE_CODE));
        dataModel.put(Common.FOOTER_PC_CODE, Symphonys.get(Common.FOOTER_PC_CODE));
        dataModel.put(Common.FOOTER_MOBILE_CODE, Symphonys.get(Common.FOOTER_MOBILE_CODE));
    }

    /**
     * Fills header and footer.
     *
     * @param request   the specified request
     * @param response  the specified response
     * @param dataModel the specified data entity
     * @throws Exception exception
     */
    public void fillHeaderAndFooter(final HttpServletRequest request, final HttpServletResponse response,
                                    final Map<String, Object> dataModel) throws Exception {
        Stopwatchs.start("Fills header");
        try {
            final boolean isMobile = (Boolean) request.getAttribute(Common.IS_MOBILE);
            dataModel.put(Common.IS_MOBILE, isMobile);

            fillHeader(request, response, dataModel);
        } finally {
            Stopwatchs.end();
        }

        Stopwatchs.start("Fills footer");
        try {
            fillFooter(dataModel);
        } finally {
            Stopwatchs.end();
        }

        dataModel.put(Common.WEBSOCKET_SCHEME, Symphonys.get("websocket.scheme"));
    }

    /**
     * Fills personal navigation.
     *
     * @param request   the specified request
     * @param response  the specified response
     * @param dataModel the specified data entity
     */
    private void fillPersonalNav(final HttpServletRequest request, final HttpServletResponse response,
                                 final Map<String, Object> dataModel) {
        Stopwatchs.start("Fills personal nav");
        try {
            dataModel.put(Common.IS_LOGGED_IN, false);
            dataModel.put(Common.IS_ADMIN_LOGGED_IN, false);

            if (null == Sessions.currentUser(request) && !userMgmtService.tryLogInWithCookie(request, response)) {
                dataModel.put("loginLabel", langPropsService.get("loginLabel"));

                return;
            }

            JSONObject curUser = null;

            try {
                curUser = userQueryService.getCurrentUser(request);
            } catch ( final Exception e) {
                LOGGER.error( "Gets the current user failed", e);
            }

            if (null == curUser) {
                dataModel.put("loginLabel", langPropsService.get("loginLabel"));

                return;
            }

            dataModel.put(Common.IS_LOGGED_IN, true);
            dataModel.put(Common.LOGOUT_URL, userQueryService.getLogoutURL("/"));

            dataModel.put("logoutLabel", langPropsService.get("logoutLabel"));

            final String userRole = curUser.optString(User.USER_ROLE);
            dataModel.put(User.USER_ROLE, userRole);

            dataModel.put(Common.IS_ADMIN_LOGGED_IN, RoleUtil.ROLE_ID_C_ADMIN.equals(userRole));

            avatarQueryService.fillUserAvatarURL(curUser.optInt(UserExtUtil.USER_AVATAR_VIEW_MODE), curUser);

            final String userId = curUser.optString(Keys.OBJECT_ID);

            final long followingArticleCnt = followQueryService.getFollowingCount(userId, FollowUtil.FOLLOWING_TYPE_C_ARTICLE);
            final long followingTagCnt = followQueryService.getFollowingCount(userId, FollowUtil.FOLLOWING_TYPE_C_TAG);
            final long followingUserCnt = followQueryService.getFollowingCount(userId, FollowUtil.FOLLOWING_TYPE_C_USER);

            curUser.put(Common.FOLLOWING_ARTICLE_CNT, followingArticleCnt);
            curUser.put(Common.FOLLOWING_TAG_CNT, followingTagCnt);
            curUser.put(Common.FOLLOWING_USER_CNT, followingUserCnt);
            final int point = curUser.optInt(UserExtUtil.USER_POINT);
            final int appRole = curUser.optInt(UserExtUtil.USER_APP_ROLE);
            if (UserExtUtil.USER_APP_ROLE_C_HACKER == appRole) {
                curUser.put(UserExtUtil.USER_T_POINT_HEX, Integer.toHexString(point));
            } else {
                curUser.put(UserExtUtil.USER_T_POINT_CC, UserExtUtil.toCCString(point));
            }

            dataModel.put(Common.CURRENT_USER, curUser);

            final Role role = roleQueryService.getRole(userRole);
            curUser.put(RoleUtil.ROLE_NAME, role.getRoleName());

            // final int unreadNotificationCount = notificationQueryService.getUnreadNotificationCount(curUser.optString(Keys.OBJECT_ID));
            dataModel.put(NotificationUtil.NOTIFICATION_T_UNREAD_COUNT, 0); // AJAX polling

            dataModel.put(Common.IS_DAILY_CHECKIN, activityQueryService.isCheckedinToday(userId));
            dataModel.put(Common.USE_CAPTCHA_CHECKIN, Symphonys.getBoolean("geetest.enabled"));

            final int livenessMax = Symphonys.getInt("activitYesterdayLivenessReward.maxPoint");
            final int currentLiveness = livenessQueryService.getCurrentLivenessPoint(userId);
            dataModel.put(LivenessUtil.LIVENESS, (float) (Math.round((float) currentLiveness / livenessMax * 100 * 100)) / 100);
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills minified directory and file postfix for static JavaScript, CSS.
     *
     * @param dataModel the specified data entity
     */
    public void fillMinified(final Map<String, Object> dataModel) {
        switch (SpringUtil.getRuntimeMode()) {
            case DEV:
                dataModel.put(Common.MINI_POSTFIX, "");
                break;
            case PRO:
                dataModel.put(Common.MINI_POSTFIX, Common.MINI_POSTFIX_VALUE);
                break;
            default:
                throw new AssertionError();
        }
    }

    /**
     * Fills the all language labels.
     *
     * @param dataModel the specified data entity
     */
    private void fillLangs(final Map<String, Object> dataModel) {
        Stopwatchs.start("Fills lang");
        try {
            dataModel.putAll(langPropsService.getAll(Locales.getLocale()));
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fills the side ad labels.
     *
     * @param dataModel the specified data entity
     */
    private void fillSideAd(final Map<String, Object> dataModel) {
        final JSONObject adOption = optionQueryService.getOption(OptionUtil.ID_C_SIDE_FULL_AD);
        if (null == adOption) {
            dataModel.put("ADLabel", "");
        } else {
            dataModel.put("ADLabel", adOption.optString(OptionUtil.OPTION_VALUE));
        }
    }

    /**
     * Fills the side tips.
     *
     * @param dataModel the specified data entity
     */
    private void fillSideTips(final Map<String, Object> dataModel) {
        if (RandomUtils.nextFloat() < 0.8) {
            return;
        }

        final List<String> tipsLabels = new ArrayList<>();
        final Map<String, String> labels = langPropsService.getAll(Locales.getLocale());
        for (final Map.Entry<String, String> entry : labels.entrySet()) {
            final String key = entry.getKey();
            if (key.startsWith("tips")) {
                tipsLabels.add(entry.getValue());
            }
        }

        // Builtin for Sym promotion
        tipsLabels.add("<img align=\"absmiddle\" alt=\"tada\" class=\"emoji\" src=\"" + SpringUtil.getStaticServePath() +
                "/emoji/graphics/tada.png\" title=\"tada\"> 本站使用 <a href=\"https://sym.b3log.org\" target=\"_blank\">Sym</a> 搭建，请为它点赞！");
        tipsLabels.add("<img align=\"absmiddle\" alt=\"sparkles\" class=\"emoji\" src=\"" + SpringUtil.getStaticServePath() +
                "/emoji/graphics/sparkles.png\" title=\"sparkles\"> 欢迎使用 <a href=\"https://sym.b3log.org\" target=\"_blank\">Sym</a> 来搭建自己的社区！");

        dataModel.put("tipsLabel", tipsLabels.get(RandomUtils.nextInt(tipsLabels.size())));
    }

    /**
     * Fills the header banner.
     *
     * @param dataModel the specified data entity
     */
    private void fillHeaderBanner(final Map<String, Object> dataModel) {
        final JSONObject adOption = optionQueryService.getOption(OptionUtil.ID_C_HEADER_BANNER);
        if (null == adOption) {
            dataModel.put("HeaderBannerLabel", "");
        } else {
            dataModel.put("HeaderBannerLabel", adOption.optString(OptionUtil.OPTION_VALUE));
        }
    }

    /**
     * Fills trend tags.
     *
     * @param dataModel the specified data entity
     * @throws Exception exception
     */
    private void fillTrendTags(final Map<String, Object> dataModel) throws Exception {
        Stopwatchs.start("Fills trend tags");
        try {
            // dataModel.put(Common.NAV_TREND_TAGS, tagQueryService.getTrendTags(Symphonys.getInt("trendTagsCnt")));
            dataModel.put(Common.NAV_TREND_TAGS, Collections.emptyList());
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Fils new tags.
     *
     * @param dataModel the specified data entity
     * @throws Exception exception
     */
    private void fillNewTags(final Map<String, Object> dataModel) throws Exception {
        dataModel.put(Common.NEW_TAGS, tagQueryService.getNewTags());
    }

    /**
     * Fills system info.
     *
     * @param dataModel the specified data entity
     * @throws Exception exception
     */
    private void fillSysInfo(final Map<String, Object> dataModel) throws Exception {
        dataModel.put(Common.VERSION, SpringUtil.VERSION);
    }
}
