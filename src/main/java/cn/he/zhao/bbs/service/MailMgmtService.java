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

import cn.he.zhao.bbs.entityUtil.*;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Locales;
import cn.he.zhao.bbs.spring.Strings;
import cn.he.zhao.bbs.util.Mails;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
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
            String optionValue = optionMapper.get(OptionUtil.ID_C_STATISTIC_MEMBER_COUNT).getOptionValue();
            final int memberCount = Integer.getInteger(optionValue);
            final int userSize = memberCount / 7;

            // select receivers 
//            final Query toUserQuery = new Query();
            PageHelper.startPage(1,userSize,"OId ASCE");
//            toUserQuery.setCurrentPageNum(1).setPageCount(1).setPageSize(userSize).
//                    setFilter(CompositeFilterOperator.and(
//                            new PropertyFilter(UserExtUtil.USER_SUB_MAIL_SEND_TIME, FilterOperator.LESS_THAN_OR_EQUAL, sevenDaysAgo),
//                            new PropertyFilter(UserExtUtil.USER_LATEST_LOGIN_TIME, FilterOperator.LESS_THAN_OR_EQUAL, sevenDaysAgo),
//                            new PropertyFilter(UserExtUtil.USER_SUB_MAIL_STATUS, FilterOperator.EQUAL, UserExtUtil.USER_SUB_MAIL_STATUS_ENABLED),
//                            new PropertyFilter(UserExtUtil.USER_STATUS, FilterOperator.EQUAL, UserExtUtil.USER_STATUS_C_VALID),
//                            new PropertyFilter(User.USER_EMAIL, FilterOperator.NOT_LIKE, "%" + UserExtUtil.USER_BUILTIN_EMAIL_SUFFIX)
//                    )).addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);
            final List<UserExt> receivers = userMapper.getMailUser(sevenDaysAgo,UserExtUtil.USER_SUB_MAIL_STATUS_ENABLED,
                                                UserExtUtil.USER_STATUS_C_VALID,"%" + UserExtUtil.USER_BUILTIN_EMAIL_SUFFIX);

            if (receivers.size() < 1) {
                LOGGER.info("No user need send newsletter");

                return;
            }

            final Set<String> toMails = new HashSet<>();

//            final Transaction transaction = userMapper.beginTransaction();
            for (int i = 0; i < receivers.size(); i++) {
                final UserExt user = receivers.get(i);
                final String email = user.getUserEmail();
                if (Strings.isEmail(email)) {
                    toMails.add(email);

                    user.setUserSubMailSendTime( now);
                    userMapper.update(user.getOid(), user);
                }
            }
//            transaction.commit();

            // send to admins by default
            final List<UserExt> admins = userMapper.getAdmins(RoleUtil.ROLE_ID_C_ADMIN);
            for (final UserExt admin : admins) {
                toMails.add(admin.getUserEmail());
            }

            final Map<String, Object> dataModel = new HashMap<>();

            // select nice articles

            PageHelper.startPage(1,Symphonys.getInt("mail.batch.articleSize"));
//            final Query articleQuery = new Query();
//            articleQuery.setCurrentPageNum(1).setPageCount(1).setPageSize(Symphonys.getInt("mail.batch.articleSize")).
//                    setFilter(CompositeFilterOperator.and(
//                            new PropertyFilter(ArticleUtil.ARTICLE_CREATE_TIME, FilterOperator.GREATER_THAN_OR_EQUAL, sevenDaysAgo),
//                            new PropertyFilter(ArticleUtil.ARTICLE_TYPE, FilterOperator.EQUAL, ArticleUtil.ARTICLE_TYPE_C_NORMAL),
//                            new PropertyFilter(ArticleUtil.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, ArticleUtil.ARTICLE_STATUS_C_INVALID),
//                            new PropertyFilter(ArticleUtil.ARTICLE_TAGS, FilterOperator.NOT_LIKE, TagUtil.TAG_TITLE_C_SANDBOX + "%")
//                    )).addSort(ArticleUtil.ARTICLE_PUSH_ORDER, SortDirection.DESCENDING).
//                    addSort(ArticleUtil.ARTICLE_COMMENT_CNT, SortDirection.DESCENDING).
//                    addSort(ArticleUtil.REDDIT_SCORE, SortDirection.DESCENDING);
            final List<Article> articles = articleMapper.selectNiceArticles(sevenDaysAgo);
            if (articles.isEmpty()) {
                LOGGER.info("No article as newsletter to send");

                return;
            }
            articleQueryService.organizeArticles(UserExtUtil.USER_AVATAR_VIEW_MODE_C_STATIC, articles);

            String mailSubject = "";
            int goodCnt = 0;
            for (final Article article : articles) {
                article.setArticleContent(articleQueryService.getArticleMetaDesc(article));

                final int gc = article.getArticleGoodCnt();
                if (gc >= goodCnt) {
                    mailSubject = article.getArticleTitle();
                    goodCnt = gc;
                }
            }

            dataModel.put(ArticleUtil.ARTICLES, (Object) articles);

            // select nice users
            final List<JSONObject> users = userQueryService.getNiceUsers(6);
            dataModel.put(User.USERS, (Object) users);

            final String fromName = langPropsService.get("symphonyEnLabel") + " "
                    + langPropsService.get("weeklyEmailFromNameLabel", Locales.getLocale());
            // TODO: 2018/11/3 datamodel 使用 entity 是否可以？
            Mails.batchSendHTML(fromName, mailSubject, new ArrayList<>(toMails), Mails.TEMPLATE_NAME_WEEKLY, dataModel);

            LOGGER.info("Sent weekly newsletter [" + toMails.size() + "]");
        } catch (final Exception e) {
            LOGGER.error( "Sends weekly newsletter failed", e);
        } finally {
            weeklyNewsletterSending = false;
        }
    }
}
