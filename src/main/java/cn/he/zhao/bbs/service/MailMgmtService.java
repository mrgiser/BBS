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

import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Locales;
import cn.he.zhao.bbs.util.Mails;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Mail management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.1.2, Jun 19, 2018
 * @since 1.6.0
 */
@Service
public class MailMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MailMgmtService.class);

    /**
     * User Mapper.
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * Article Mapper.
     */
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * OptionUtil Mapper.
     */
    @Autowired
    private OptionMapper optionMapper;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Article query service.
     */
    @Autowired
    private ArticleQueryService articleQueryService;

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
     * Weekly newsletter sending status.
     */
    private boolean weeklyNewsletterSending;

    /**
     * Send weekly newsletter.
     */
    public void sendWeeklyNewsletter() {
        final Calendar calendar = Calendar.getInstance();
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);

        if (13 != hour || 55 > minute) {
            return;
        }

        if (weeklyNewsletterSending) {
            return;
        }

        weeklyNewsletterSending = true;
        LOGGER.info("Sending weekly newsletter....");

        final long now = System.currentTimeMillis();
        final long sevenDaysAgo = now - 1000 * 60 * 60 * 24 * 7;

        try {
            final int memberCount = optionMapper.get(Option.ID_C_STATISTIC_MEMBER_COUNT).optInt(Option.OPTION_VALUE);
            final int userSize = memberCount / 7;

            // select receivers 
            final Query toUserQuery = new Query();
            toUserQuery.setCurrentPageNum(1).setPageCount(1).setPageSize(userSize).
                    setFilter(CompositeFilterOperator.and(
                            new PropertyFilter(UserExt.USER_SUB_MAIL_SEND_TIME, FilterOperator.LESS_THAN_OR_EQUAL, sevenDaysAgo),
                            new PropertyFilter(UserExt.USER_LATEST_LOGIN_TIME, FilterOperator.LESS_THAN_OR_EQUAL, sevenDaysAgo),
                            new PropertyFilter(UserExt.USER_SUB_MAIL_STATUS, FilterOperator.EQUAL, UserExt.USER_SUB_MAIL_STATUS_ENABLED),
                            new PropertyFilter(UserExt.USER_STATUS, FilterOperator.EQUAL, UserExt.USER_STATUS_C_VALID),
                            new PropertyFilter(User.USER_EMAIL, FilterOperator.NOT_LIKE, "%" + UserExt.USER_BUILTIN_EMAIL_SUFFIX)
                    )).addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);
            final JSONArray receivers = userMapper.get(toUserQuery).optJSONArray(Keys.RESULTS);

            if (receivers.length() < 1) {
                LOGGER.info("No user need send newsletter");

                return;
            }

            final Set<String> toMails = new HashSet<>();

            final Transaction transaction = userMapper.beginTransaction();
            for (int i = 0; i < receivers.length(); i++) {
                final JSONObject user = receivers.optJSONObject(i);
                final String email = user.optString(User.USER_EMAIL);
                if (Strings.isEmail(email)) {
                    toMails.add(email);

                    user.put(UserExt.USER_SUB_MAIL_SEND_TIME, now);
                    userMapper.update(user.optString(Keys.OBJECT_ID), user);
                }
            }
            transaction.commit();

            // send to admins by default
            final List<JSONObject> admins = userMapper.getAdmins();
            for (final JSONObject admin : admins) {
                toMails.add(admin.optString(User.USER_EMAIL));
            }

            final Map<String, Object> dataModel = new HashMap<>();

            // select nice articles
            final Query articleQuery = new Query();
            articleQuery.setCurrentPageNum(1).setPageCount(1).setPageSize(Symphonys.getInt("mail.batch.articleSize")).
                    setFilter(CompositeFilterOperator.and(
                            new PropertyFilter(Article.ARTICLE_CREATE_TIME, FilterOperator.GREATER_THAN_OR_EQUAL, sevenDaysAgo),
                            new PropertyFilter(Article.ARTICLE_TYPE, FilterOperator.EQUAL, Article.ARTICLE_TYPE_C_NORMAL),
                            new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, Article.ARTICLE_STATUS_C_INVALID),
                            new PropertyFilter(Article.ARTICLE_TAGS, FilterOperator.NOT_LIKE, Tag.TAG_TITLE_C_SANDBOX + "%")
                    )).addSort(Article.ARTICLE_PUSH_ORDER, SortDirection.DESCENDING).
                    addSort(Article.ARTICLE_COMMENT_CNT, SortDirection.DESCENDING).
                    addSort(Article.REDDIT_SCORE, SortDirection.DESCENDING);
            final List<JSONObject> articles = CollectionUtils.jsonArrayToList(
                    articleMapper.get(articleQuery).optJSONArray(Keys.RESULTS));
            if (articles.isEmpty()) {
                LOGGER.info("No article as newsletter to send");

                return;
            }
            articleQueryService.organizeArticles(UserExt.USER_AVATAR_VIEW_MODE_C_STATIC, articles);

            String mailSubject = "";
            int goodCnt = 0;
            for (final JSONObject article : articles) {
                article.put(Article.ARTICLE_CONTENT, articleQueryService.getArticleMetaDesc(article));

                final int gc = article.optInt(Article.ARTICLE_GOOD_CNT);
                if (gc >= goodCnt) {
                    mailSubject = article.optString(Article.ARTICLE_TITLE);
                    goodCnt = gc;
                }
            }

            dataModel.put(Article.ARTICLES, (Object) articles);

            // select nice users
            final List<JSONObject> users = userQueryService.getNiceUsers(6);
            dataModel.put(User.USERS, (Object) users);

            final String fromName = langPropsService.get("symphonyEnLabel") + " "
                    + langPropsService.get("weeklyEmailFromNameLabel", Locales.getLocale());
            Mails.batchSendHTML(fromName, mailSubject, new ArrayList<>(toMails), Mails.TEMPLATE_NAME_WEEKLY, dataModel);

            LOGGER.info("Sent weekly newsletter [" + toMails.size() + "]");
        } catch (final Exception e) {
            LOGGER.error( "Sends weekly newsletter failed", e);
        } finally {
            weeklyNewsletterSending = false;
        }
    }
}
