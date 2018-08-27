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
import cn.he.zhao.bbs.channel.ChatRoomChannel;
import cn.he.zhao.bbs.exception.RequestProcessAdviceException;
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.util.Emotions;
import cn.he.zhao.bbs.util.Markdowns;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.validate.ChatMsgAddValidation;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.qiniu.util.Auth;
import org.jsoup.Jsoup;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Chat room processor.
 * <ul>
 * <li>Shows char room (/cr, /chat-room, /community), GET</li>
 * <li>Sends chat message (/chat-room/send), POST</li>
 * <li>Receives <a href="https://github.com/b3log/xiaov">XiaoV</a> message (/community/push), POST</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.5.12, May 2, 2018
 * @since 1.4.0
 */
@Controller
public class ChatRoomProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatRoomProcessor.class);

    /**
     * Chat messages.
     */
    public static LinkedList<JSONObject> messages = new LinkedList<>();

    /**
     * Data entity service.
     */
    @Autowired
    private DataModelService dataModelService;

    /**
     * Turing query service.
     */
    @Autowired
    private TuringQueryService turingQueryService;

    /**
     * User management service.
     */
    @Autowired
    private UserMgmtService userMgmtService;

    /**
     * Short link query service.
     */
    @Autowired
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * Notification query service.
     */
    @Autowired
    private NotificationQueryService notificationQueryService;

    /**
     * Notification management service.
     */
    @Autowired
    private NotificationMgmtService notificationMgmtService;

    /**
     * User query service.
     */
    @Autowired
    private UserQueryService userQueryService;

    /**
     * Comment management service.
     */
    @Autowired
    private CommentMgmtService commentMgmtService;

    /**
     * Comment query service.
     */
    @Autowired
    private CommentQueryService commentQueryService;

    /**
     * Article query service.
     */
    @Autowired
    private ArticleQueryService articleQueryService;

    /**
     * XiaoV replies Stm.
     *
     * @param request the specified request
     */
    @RequestMapping(value = "/cron/xiaov", method = RequestMethod.GET)
    public void xiaoVReply(Map<String, Object> dataModel, final HttpServletRequest request) {
        dataModel.put(Keys.STATUS_CODE,false);

        try {
            final JSONObject xiaoV = userQueryService.getUserByName(TuringQueryService.ROBOT_NAME);
            if (null == xiaoV) {
                return;
            }

            final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);
            final String xiaoVUserId = xiaoV.optString(Keys.OBJECT_ID);
            final JSONObject atResult = notificationQueryService.getAtNotifications(
                    avatarViewMode, xiaoVUserId, 1, 1); // Just get the latest one
            final List<JSONObject> notifications = (List<JSONObject>) atResult.get(Keys.RESULTS);
            final JSONObject replyResult = notificationQueryService.getReplyNotifications(
                    avatarViewMode, xiaoVUserId, 1, 1); // Just get the latest one
            notifications.addAll((List<JSONObject>) replyResult.get(Keys.RESULTS));
            for (final JSONObject notification : notifications) {
                if (notification.optBoolean(Notification.NOTIFICATION_HAS_READ)) {
                    continue;
                }

                notificationMgmtService.makeRead(notification);

                String articleId = notification.optString(Article.ARTICLE_T_ID);
                String q = null;
                final int dataType = notification.optInt(Notification.NOTIFICATION_DATA_TYPE);
                switch (dataType) {
                    case Notification.DATA_TYPE_C_AT:
                        q = notification.optString(Common.CONTENT);
                        break;
                    case Notification.DATA_TYPE_C_REPLY:
                        q = notification.optString(Comment.COMMENT_CONTENT);
                        break;
                    default:
                        LOGGER.warn("Unknown notificat data type [" + dataType + "] for XiaoV reply");
                }

                String xiaoVSaid;
                final JSONObject comment = new JSONObject();
                if (StringUtils.isNotBlank(q)) {
                    q = Jsoup.parse(q).text();
                    q = StringUtils.replace(q, "@" + TuringQueryService.ROBOT_NAME + " ", "");

                    xiaoVSaid = turingQueryService.chat(articleId, q);

                    comment.put(Comment.COMMENT_CONTENT, xiaoVSaid);
                    comment.put(Comment.COMMENT_AUTHOR_ID, xiaoVUserId);
                    comment.put(Comment.COMMENT_ON_ARTICLE_ID, articleId);
                    comment.put(Comment.COMMENT_ORIGINAL_COMMENT_ID, notification.optString(Comment.COMMENT_T_ID));

                    commentMgmtService.addComment(comment);
                }
            }

//            dataModel.put(Keys.STATUS_CODE,true);
            dataModel.put(Keys.STATUS_CODE,true);
        } catch (final Exception e) {
            LOGGER.error( "Update user latest comment time failed", e);
        }
    }

    /**
     * Adds a chat message.
     * <p>
     * The request json object (a chat message):
     * <pre>
     * {
     *     "content": ""
     * }
     * </pre>
     * </p>
     *
     * @param request the specified request
     */
    @RequestMapping(value = "/chat-room/send", method = RequestMethod.POST)
//    @Before(adviceClass = {LoginCheck.class, ChatMsgAddValidation.class})
    @LoginCheckAnno
    public synchronized void addChatRoomMsg(Map<String, Object> dataModel, final HttpServletRequest request) {
        dataModel.put(Keys.STATUS_CODE,false);

        try {
            ChatMsgAddValidation.doAdvice(request);
        } catch (RequestProcessAdviceException e) {
            dataModel.put(Keys.MSG,e.getJsonObject().get(Keys.MSG));
            return;
        }

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);
        String content = requestJSONObject.optString(Common.CONTENT);

        content = shortLinkQueryService.linkArticle(content);
        content = shortLinkQueryService.linkTag(content);
        content = Emotions.convert(content);
        content = Markdowns.toHTML(content);
        content = Markdowns.clean(content, "");

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String userName = currentUser.optString(User.USER_NAME);

        final JSONObject msg = new JSONObject();
        msg.put(User.USER_NAME, userName);
        msg.put(UserExt.USER_AVATAR_URL, currentUser.optString(UserExt.USER_AVATAR_URL));
        msg.put(Common.CONTENT, content);

        ChatRoomChannel.notifyChat(msg);

        messages.addFirst(msg);
        final int maxCnt = Symphonys.getInt("chatRoom.msgCnt");
        if (messages.size() > maxCnt) {
            messages.remove(maxCnt);
        }

        if (content.contains("@" + TuringQueryService.ROBOT_NAME + " ")) {
            content = content.replaceAll("@" + TuringQueryService.ROBOT_NAME + " ", "");
            final String xiaoVSaid = turingQueryService.chat(currentUser.optString(User.USER_NAME), content);
            if (null != xiaoVSaid) {
                final JSONObject xiaoVMsg = new JSONObject();
                xiaoVMsg.put(User.USER_NAME, TuringQueryService.ROBOT_NAME);
                xiaoVMsg.put(UserExt.USER_AVATAR_URL, TuringQueryService.ROBOT_AVATAR + "?imageView2/1/w/48/h/48/interlace/0/q/100");
                xiaoVMsg.put(Common.CONTENT, "<p>@" + userName + " " + xiaoVSaid + "</p>");

                ChatRoomChannel.notifyChat(xiaoVMsg);

                messages.addFirst(xiaoVMsg);
                if (messages.size() > maxCnt) {
                    messages.remove(maxCnt);
                }
            }
        }

        dataModel.put(Keys.STATUS_CODE,true);

        currentUser.put(UserExt.USER_LATEST_CMT_TIME, System.currentTimeMillis());
        try {
            userMgmtService.updateUser(currentUser.optString(Keys.OBJECT_ID), currentUser);
        } catch (final Exception e) {
            LOGGER.error( "Update user latest comment time failed", e);
        }
    }

    /**
     * Shows chat room.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = {"/cr", "/chat-room", "/community"}, method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
//    @After(adviceClass = {PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showChatRoom(Map<String, Object> dataModel,
                             final HttpServletRequest request, final HttpServletResponse response) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//        renderer.setTemplateName("chat-room.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "chat-room.ftl";

        dataModel.put(Common.MESSAGES, messages);
        dataModel.put("chatRoomMsgCnt", Symphonys.getInt("chatRoom.msgCnt"));

        // Qiniu file upload authenticate
        final Auth auth = Auth.create(Symphonys.get("qiniu.accessKey"), Symphonys.get("qiniu.secretKey"));
        dataModel.put("qiniuUploadToken", auth.uploadToken(Symphonys.get("qiniu.bucket")));
        dataModel.put("qiniuDomain", Symphonys.get("qiniu.domain"));

        final long imgMaxSize = Symphonys.getLong("upload.img.maxSize");
        dataModel.put("imgMaxSize", imgMaxSize);
        final long fileMaxSize = Symphonys.getLong("upload.file.maxSize");
        dataModel.put("fileMaxSize", fileMaxSize);

        dataModel.put(Common.ONLINE_CHAT_CNT, ChatRoomChannel.SESSIONS.size());

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);
        dataModelService.fillSideTags(dataModel);
        dataModelService.fillLatestCmts(dataModel);

        return url;
    }

    /**
     * XiaoV push API.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/community/push", method = RequestMethod.POST)
//    @Before(adviceClass = StopwatchStartAdvice.class)
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @StopWatchEndAnno
    public synchronized void receiveXiaoV(Map<String, Object> dataModel,
                                          final HttpServletRequest request, final HttpServletResponse response) throws Exception {
//        final String key = Symphonys.get("xiaov.key");
//        if (!key.equals(request.getParameter("key"))) {
//            response.sendError(HttpServletResponse.SC_FORBIDDEN);
//
//            return;
//        }

        final String msg = request.getParameter("msg");
        if (StringUtils.isBlank(msg)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);

            return;
        }

        String user = request.getParameter("user");
        if (StringUtils.isBlank("user")) {
            user = "V";
        }

//        final JSONObject ret = new JSONObject();
//        context.renderJSON(ret);

        final String defaultAvatarURL = Symphonys.get("defaultThumbnailURL");
        final JSONObject chatroomMsg = new JSONObject();
        chatroomMsg.put(User.USER_NAME, user);
        chatroomMsg.put(UserExt.USER_AVATAR_URL, defaultAvatarURL);
        chatroomMsg.put(Common.CONTENT, msg);

        ChatRoomChannel.notifyChat(chatroomMsg);
        messages.addFirst(chatroomMsg);
        final int maxCnt = Symphonys.getInt("chatRoom.msgCnt");
        if (messages.size() > maxCnt) {
            messages.remove(maxCnt);
        }

        dataModel.put(Keys.STATUS_CODE, true);

    }
}
