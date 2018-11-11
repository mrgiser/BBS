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
import cn.he.zhao.bbs.cache.DomainCache;
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.entity.Character;
import cn.he.zhao.bbs.entityUtil.*;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.Pagination;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.service.*;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.*;
import cn.he.zhao.bbs.util.*;
import cn.he.zhao.bbs.validate.ArticleAddValidation;
import cn.he.zhao.bbs.validate.UserRegisterValidation;
import com.qiniu.util.Auth;
import jodd.util.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Article processor.
 * <ul>
 * <li>Shows an article (/article/{articleId}), GET</li>
 * <li>Shows article pre adding form page (/pre-post), GET</li>
 * <li>Shows article adding form page (/post), GET</li>
 * <li>Adds an article (/post) <em>locally</em>, POST</li>
 * <li>Shows an article updating form page (/update) <em>locally</em>, GET</li>
 * <li>Updates an article (/article/{id}) <em>locally</em>, PUT</li>
 * <li>Markdowns text (/markdown), POST</li>
 * <li>Rewards an article (/article/reward), POST</li>
 * <li>Gets an article preview content (/article/{articleId}/preview), GET</li>
 * <li>Sticks an article (/article/stick), POST</li>
 * <li>Gets an article's revisions (/article/{id}/revisions), GET</li>
 * <li>Gets article image (/article/{articleId}/image), GET</li>
 * <li>Checks article title (/article/check-title), POST</li>
 * <li>Removes an article (/article/{id}/remove), POST</li>
 * </ul>
 * <p>
 * The '<em>locally</em>' means user post an article on Symphony directly rather than receiving an article from
 * externally (for example Rhythm).
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 1.27.2.0, Jun 10, 2018
 * @since 0.2.0
 */
@Controller
public class ArticleProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleProcessor.class);

    /**
     * RevisionUtil query service.
     */
    @Autowired
    private RevisionQueryService revisionQueryService;

    /**
     * Short link query service.
     */
    @Autowired
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * Article management service.
     */
    @Autowired
    private ArticleMgmtService articleMgmtService;

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
     * RewardUtil query service.
     */
    @Autowired
    private RewardQueryService rewardQueryService;

    /**
     * Vote query service.
     */
    @Autowired
    private VoteQueryService voteQueryService;

    /**
     * LivenessUtil management service.
     */
    @Autowired
    private LivenessMgmtService livenessMgmtService;

    /**
     * ReferralUtil management service.
     */
    @Autowired
    private ReferralMgmtService referralMgmtService;

    /**
     * Character query service.
     */
    @Autowired
    private CharacterQueryService characterQueryService;

    /**
     * Domain query service.
     */
    @Autowired
    private DomainQueryService domainQueryService;

    /**
     * Domain cache.
     */
    @Autowired
    private DomainCache domainCache;

    /**
     * Data entity service.
     */
    @Autowired
    private DataModelService dataModelService;

    /**
     * Removes an article.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/article/{id}/remove", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, PermissionCheck.class})
//    @After(adviceClass = {StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @LoginCheckAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public void removeArticle(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                              final String id) throws Exception {
        if (StringUtils.isBlank(id)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String currentUserId = currentUser.optString(Keys.OBJECT_ID);
        final Article article = articleQueryService.getArticle(id);
        if (null == article) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final String authorId = article.getArticleAuthorId();
        if (!authorId.equals(currentUserId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        dataModel.put(Keys.STATUS_CODE,false);
        try {
            articleMgmtService.removeArticle(id);

            dataModel.put(Keys.STATUS_CODE, StatusCodes.SUCC);
            dataModel.put(ArticleUtil.ARTICLE_T_ID, id);
        } catch ( final Exception e) {
            final String msg = e.getMessage();

            dataModel.put("msg",msg);
            dataModel.put(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    /**
     * Checks article title.
     *
     * @param request the specified request
     * @throws Exception exception
     */
    @RequestMapping(value = "/article/check-title", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
//    @After(adviceClass = {StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @LoginCheckAnno
    @StopWatchEndAnno
    public void checkArticleTitle(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        final String currentUserId = currentUser.optString(Keys.OBJECT_ID);
        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);
        String title = requestJSONObject.optString(ArticleUtil.ARTICLE_TITLE);
        title = StringUtils.trim(title);
        String id = requestJSONObject.optString(ArticleUtil.ARTICLE_T_ID);

        final Article article = articleQueryService.getArticleByTitle(title);
        if (null == article) {
            dataModel.put(Keys.STATUS_CODE,true);

            return;
        }

        if (StringUtils.isBlank(id)) { // Add
            final String authorId = article.getArticleAuthorId();

            String msg;
            if (authorId.equals(currentUserId)) {
                msg = langPropsService.get("duplicatedArticleTitleSelfLabel");
                msg = msg.replace("{article}", "<a target='_blank' href='/article/" + article.getOid()
                        + "'>" + title + "</a>");
            } else {
                final UserExt author = userQueryService.getUser(authorId);
                final String userName = author.getUserName();

                msg = langPropsService.get("duplicatedArticleTitleLabel");
                msg = msg.replace("{user}", "<a target='_blank' href='/member/" + userName + "'>" + userName + "</a>");
                msg = msg.replace("{article}", "<a target='_blank' href='/article/" + article.getOid()
                        + "'>" + title + "</a>");
            }

//            final JSONObject ret = new JSONObject();
            dataModel.put(Keys.STATUS_CODE, false);
            dataModel.put(Keys.MSG, msg);

//            context.renderJSON(ret);
        } else { // Update
            final Article oldArticle = articleQueryService.getArticle(id);
            if (oldArticle.getArticleTitle().equals(title)) {
//                dataModel.put(Keys.STATUS_CODE,true);
                dataModel.put(Keys.STATUS_CODE,true);

                return;
            }

            final String authorId = article.getArticleAuthorId();

            String msg;
            if (authorId.equals(currentUserId)) {
                msg = langPropsService.get("duplicatedArticleTitleSelfLabel");
                msg = msg.replace("{article}", "<a target='_blank' href='/article/" + article.getOid()
                        + "'>" + title + "</a>");
            } else {
                final UserExt author = userQueryService.getUser(authorId);
                final String userName = author.getUserName();

                msg = langPropsService.get("duplicatedArticleTitleLabel");
                msg = msg.replace("{user}", "<a target='_blank' href='/member/" + userName + "'>" + userName + "</a>");
                msg = msg.replace("{article}", "<a target='_blank' href='/article/" + article.getOid()
                        + "'>" + title + "</a>");
            }

//            final JSONObject ret = new JSONObject();
            dataModel.put(Keys.STATUS_CODE, false);
            dataModel.put(Keys.MSG, msg);

//            context.renderJSON(ret);
        }
    }

    /**
     * Gets article image.
     *
     * @param articleId the specified article id
     * @throws Exception exception
     */
    @RequestMapping(value = "/article/{articleId}/image", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
//    @After(adviceClass = {StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @LoginCheckAnno
    @StopWatchEndAnno
    public void getArticleImage(Map<String, Object> dataModel, final String articleId) throws Exception {
        final Article article = articleQueryService.getArticle(articleId);
        final String authorId = article.getArticleAuthorId();

        final List<Character> characters = characterQueryService.getWrittenCharacters();

        List<JSONObject> jsonObjects = JsonUtil.listToJSONList(characters);

        final String articleContent = article.getArticleContent();

        final List<BufferedImage> images = new ArrayList<>();
        for (int i = 0; i < articleContent.length(); i++) {
            final String ch = articleContent.substring(i, i + 1);
            final JSONObject chRecord = CharacterUtil.getCharacter(ch, jsonObjects);
            if (null == chRecord) {
                images.add(CharacterUtil.createImage(ch));

                continue;
            }

            final String imgData = chRecord.optString(CharacterUtil.CHARACTER_IMG);
            final byte[] data = Base64.decode(imgData.getBytes());
            final BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
            final BufferedImage newImage = new BufferedImage(50, 50, img.getType());
            final Graphics g = newImage.getGraphics();
            g.setClip(0, 0, 50, 50);
            g.fillRect(0, 0, 50, 50);
            g.drawImage(img, 0, 0, 50, 50, null);
            g.dispose();

            images.add(newImage);
        }

        final int rowCharacterCount = 30;
        final int rows = (int) Math.ceil((double) images.size() / (double) rowCharacterCount);

        final BufferedImage combined = new BufferedImage(30 * 50, rows * 50, Transparency.TRANSLUCENT);
        int row = 0;
        for (int i = 0; i < images.size(); i++) {
            final BufferedImage image = images.get(i);

            final Graphics g = combined.getGraphics();
            g.drawImage(image, (i % rowCharacterCount) * 50, row * 50, null);

            if (0 == (i + 1) % rowCharacterCount) {
                row++;
            }
        }

        ImageIO.write(combined, "PNG", new File("./hp.png"));

        String url = "";

//        final JSONObject ret = new JSONObject();
        dataModel.put(Keys.STATUS_CODE, true);
        dataModel.put(CommonUtil.URL, (Object) url);

//        context.renderJSON(ret);
    }

    /**
     * Gets an article's revisions.
     *
     * @param id      the specified article id
     */
    @RequestMapping(value = "/article/{id}/revisions", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, PermissionCheck.class})
//    @After(adviceClass = {StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @LoginCheckAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public void getArticleRevisions(Map<String, Object> dataModel, final String id) {
        final List<Revision> revisions = revisionQueryService.getArticleRevisions(id);
        final JSONObject ret = new JSONObject();
        ret.put(Keys.STATUS_CODE, true);
        ret.put(RevisionUtil.REVISIONS, (Object) revisions);

//        context.renderJSON(ret);
        dataModel.put(Keys.STATUS_CODE,ret.get(Keys.STATUS_CODE));
        dataModel.put(RevisionUtil.REVISIONS, (Object) revisions);
    }

    /**
     * Shows pre-add article.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/pre-post", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
//    @After(adviceClass = {CSRFToken.class, PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @LoginCheckAnno
    @PermissionGrantAnno
    @CSRFTokenAnno
    @StopWatchEndAnno
    public String showPreAddArticle(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//
//        renderer.setTemplateName("/home/pre-post.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();


        dataModel.put(Common.BROADCAST_POINT, PointtransferUtil.TRANSFER_SUM_C_ADD_ARTICLE_BROADCAST);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
        return "/home/pre-post.ftl";
    }

    /**
     * Fills the domains with tags.
     *
     * @param dataModel the specified data entity
     */
    private void fillDomainsWithTags(final Map<String, Object> dataModel) {
        final List<Domain> domains = domainQueryService.getAllDomains();
        dataModel.put(Common.ADD_ARTICLE_DOMAINS, domains);
        for (final Domain domain : domains) {
            final List<Tag> tags = domainQueryService.getTags(domain.getOid());

            domain.setDomainTags((Object) tags);
        }

        final JSONObject user = (JSONObject) dataModel.get(Common.CURRENT_USER);
        if (null == user) {
            return;
        }

        try {
            final JSONObject followingTagsResult = followQueryService.getFollowingTags(
                    user.optString(Keys.OBJECT_ID), 1, 28);
            final List<JSONObject> followingTags = (List<JSONObject>) followingTagsResult.opt(Keys.RESULTS);
            if (!followingTags.isEmpty()) {
                final Domain userWatched = new Domain();
                userWatched.setOid(String.valueOf(System.currentTimeMillis()));
                userWatched.setDomainTitle(langPropsService.get("notificationFollowingLabel"));
                userWatched.setDomainTags((Object) followingTags);

                domains.add(0, userWatched);
            }
        } catch (final Exception e) {
            LOGGER.error( "Get user [name=" + user.optString(User.USER_NAME) + "] following tags failed", e);
        }
    }

    /**
     * Shows add article.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/post", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
//    @After(adviceClass = {CSRFToken.class, PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @LoginCheckAnno
    @CSRFTokenAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showAddArticle(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//
//        renderer.setTemplateName("/home/post.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "/home/post.ftl";

        // Qiniu file upload authenticate
        final Auth auth = Auth.create(Symphonys.get("qiniu.accessKey"), Symphonys.get("qiniu.secretKey"));
        final String uploadToken = auth.uploadToken(Symphonys.get("qiniu.bucket"));
        dataModel.put("qiniuUploadToken", uploadToken);
        dataModel.put("qiniuDomain", Symphonys.get("qiniu.domain"));

        if (!Symphonys.getBoolean("qiniu.enabled")) {
            dataModel.put("qiniuUploadToken", "");
        }

        final long imgMaxSize = Symphonys.getLong("upload.img.maxSize");
        dataModel.put("imgMaxSize", imgMaxSize);
        final long fileMaxSize = Symphonys.getLong("upload.file.maxSize");
        dataModel.put("fileMaxSize", fileMaxSize);

        String tags = request.getParameter(TagUtil.TAGS);
        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);

        if (StringUtils.isBlank(tags)) {
            tags = "";

            dataModel.put(TagUtil.TAGS, tags);
        } else {
            tags = TagUtil.formatTags(tags);
            final String[] tagTitles = tags.split(",");

            final StringBuilder tagBuilder = new StringBuilder();
            for (final String title : tagTitles) {
                final String tagTitle = title.trim();

                if (Strings.isEmptyOrNull(tagTitle)) {
                    continue;
                }

                if (TagUtil.containsWhiteListTags(tagTitle)) {
                    tagBuilder.append(tagTitle).append(",");

                    continue;
                }

                if (!TagUtil.TAG_TITLE_PATTERN.matcher(tagTitle).matches()) {
                    continue;
                }

                if (tagTitle.length() > TagUtil.MAX_TAG_TITLE_LENGTH) {
                    continue;
                }

                if (!RoleUtil.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))
                        && ArrayUtils.contains(Symphonys.RESERVED_TAGS, tagTitle)) {
                    continue;
                }

                tagBuilder.append(tagTitle).append(",");
            }
            if (tagBuilder.length() > 0) {
                tagBuilder.deleteCharAt(tagBuilder.length() - 1);
            }

            dataModel.put(TagUtil.TAGS, tagBuilder.toString());
        }

        final String type = request.getParameter(Common.TYPE);
        if (StringUtils.isBlank(type)) {
            dataModel.put(ArticleUtil.ARTICLE_TYPE, ArticleUtil.ARTICLE_TYPE_C_NORMAL);
        } else {
            int articleType = ArticleUtil.ARTICLE_TYPE_C_NORMAL;

            try {
                articleType = Integer.valueOf(type);
            } catch (final Exception e) {
                LOGGER.warn( "Gets article type error [" + type + "]", e);
            }

            if (ArticleUtil.isInvalidArticleType(articleType)) {
                articleType = ArticleUtil.ARTICLE_TYPE_C_NORMAL;
            }

            dataModel.put(ArticleUtil.ARTICLE_TYPE, articleType);
        }

        String at = request.getParameter(Common.AT);
        at = StringUtils.trim(at);
        if (StringUtils.isNotBlank(at)) {
            dataModel.put(Common.AT, at + " ");
        }

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        String rewardEditorPlaceholderLabel = langPropsService.get("rewardEditorPlaceholderLabel");
        rewardEditorPlaceholderLabel = rewardEditorPlaceholderLabel.replace("{point}",
                String.valueOf(PointtransferUtil.TRANSFER_SUM_C_ADD_ARTICLE_REWARD));
        dataModel.put("rewardEditorPlaceholderLabel", rewardEditorPlaceholderLabel);
        dataModel.put(Common.BROADCAST_POINT, PointtransferUtil.TRANSFER_SUM_C_ADD_ARTICLE_BROADCAST);

        String articleContentErrorLabel = langPropsService.get("articleContentErrorLabel");
        articleContentErrorLabel = articleContentErrorLabel.replace("{maxArticleContentLength}",
                String.valueOf(ArticleAddValidation.MAX_ARTICLE_CONTENT_LENGTH));
        dataModel.put("articleContentErrorLabel", articleContentErrorLabel);

        final String b3Key = currentUser.optString(UserExtUtil.USER_B3_KEY);
        final String b3ClientAddArticle = currentUser.optString(UserExtUtil.USER_B3_CLIENT_ADD_ARTICLE_URL);
        final String b3ClientUpdateArticle = currentUser.optString(UserExtUtil.USER_B3_CLIENT_UPDATE_ARTICLE_URL);
        dataModel.put("hasB3Key", StringUtils.isNotBlank(b3Key) && StringUtils.isNotBlank(b3ClientAddArticle) && StringUtils.isNotBlank(b3ClientUpdateArticle));

        fillPostArticleRequisite(dataModel, currentUser);
        fillDomainsWithTags(dataModel);

        return url;
    }

    private void fillPostArticleRequisite(final Map<String, Object> dataModel, final JSONObject currentUser) {
        boolean requisite = false;
        String requisiteMsg = "";

//        if (!UserExt.updatedAvatar(currentUser)) {
//            requisite = true;
//            requisiteMsg = langPropsService.get("uploadAvatarThenPostLabel");
//        }

        dataModel.put(Common.REQUISITE, requisite);
        dataModel.put(Common.REQUISITE_MSG, requisiteMsg);
    }

    /**
     * Shows article with the specified article id.
     *
     * @param request   the specified request
     * @param response  the specified response
     * @param articleId the specified article id
     * @throws Exception exception
     */
    @RequestMapping(value = "/article/{articleId}", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
//    @After(adviceClass = {CSRFToken.class, PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @AnonymousViewCheckAnno
    @CSRFTokenAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showArticle(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                            final String articleId) throws Exception {
//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//
//        renderer.setTemplateName("/article.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();

        String url = "/article.ftl";

        final int avatarViewMode = (int) request.getAttribute(UserExtUtil.USER_AVATAR_VIEW_MODE);

        final Article article = articleQueryService.getArticleById(avatarViewMode, articleId);
        if (null == article) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return null;
        }
        JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(article));

        final HttpSession session = request.getSession(false);
        if (null != session) {
            session.setAttribute(ArticleUtil.ARTICLE_T_ID, articleId);
        }

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final String authorId = jsonObject.optString(ArticleUtil.ARTICLE_AUTHOR_ID);
        final UserExt author = userQueryService.getUser(authorId);
        JSONObject jsonObjectAuthor = new JSONObject(author);
        Escapes.escapeHTML(jsonObjectAuthor);

        if (ArticleUtil.ARTICLE_ANONYMOUS_C_PUBLIC == jsonObject.optInt(ArticleUtil.ARTICLE_ANONYMOUS)) {
            jsonObject.put(ArticleUtil.ARTICLE_T_AUTHOR_NAME, jsonObjectAuthor.optString(User.USER_NAME));
            jsonObject.put(ArticleUtil.ARTICLE_T_AUTHOR_URL, jsonObjectAuthor.optString(User.USER_URL));
            jsonObject.put(ArticleUtil.ARTICLE_T_AUTHOR_INTRO, jsonObjectAuthor.optString(UserExtUtil.USER_INTRO));
        } else {
            jsonObject.put(ArticleUtil.ARTICLE_T_AUTHOR_NAME, UserExtUtil.ANONYMOUS_USER_NAME);
            jsonObject.put(ArticleUtil.ARTICLE_T_AUTHOR_URL, "");
            jsonObject.put(ArticleUtil.ARTICLE_T_AUTHOR_INTRO, "");
        }
        dataModel.put(ArticleUtil.ARTICLE, article);

        jsonObject.put(Common.IS_MY_ARTICLE, false);
        jsonObject.put(ArticleUtil.ARTICLE_T_AUTHOR, author);
        jsonObject.put(Common.REWARDED, false);
        jsonObject.put(Common.OFFERED, false);
        jsonObject.put(Common.REWARED_COUNT, rewardQueryService.rewardedCount(articleId, RewardUtil.TYPE_C_ARTICLE));
        jsonObject.put(ArticleUtil.ARTICLE_REVISION_COUNT, revisionQueryService.count(articleId, RevisionUtil.DATA_TYPE_C_ARTICLE));

        articleQueryService.processArticleContent(jsonObject, request);

        String cmtViewModeStr = request.getParameter("m");
        JSONObject currentUser;
        String currentUserId = null;
        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            currentUserId = currentUser.optString(Keys.OBJECT_ID);
            jsonObject.put(Common.IS_MY_ARTICLE, currentUserId.equals(jsonObject.optString(ArticleUtil.ARTICLE_AUTHOR_ID)));

            final boolean isFollowing = followQueryService.isFollowing(currentUserId, articleId, FollowUtil.FOLLOWING_TYPE_C_ARTICLE);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            final boolean isWatching = followQueryService.isFollowing(currentUserId, articleId, FollowUtil.FOLLOWING_TYPE_C_ARTICLE_WATCH);
            dataModel.put(Common.IS_WATCHING, isWatching);

            final int articleVote = voteQueryService.isVoted(currentUserId, articleId);
            jsonObject.put(ArticleUtil.ARTICLE_T_VOTE, articleVote);

            if (currentUserId.equals(jsonObjectAuthor.optString(Keys.OBJECT_ID))) {
                jsonObject.put(Common.REWARDED, true);
            } else {
                jsonObject.put(Common.REWARDED, rewardQueryService.isRewarded(currentUserId, articleId, RewardUtil.TYPE_C_ARTICLE));
            }

            if (Strings.isEmptyOrNull(cmtViewModeStr) || !Strings.isNumeric(cmtViewModeStr)) {
                cmtViewModeStr = currentUser.optString(UserExtUtil.USER_COMMENT_VIEW_MODE);
            }
        } else if (Strings.isEmptyOrNull(cmtViewModeStr) || !Strings.isNumeric(cmtViewModeStr)) {
            cmtViewModeStr = "0";
        }

        final int cmtViewMode = Integer.valueOf(cmtViewModeStr);
        dataModel.put(UserExtUtil.USER_COMMENT_VIEW_MODE, cmtViewMode);

        if (!(Boolean) request.getAttribute(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT)) {
            articleMgmtService.incArticleViewCount(articleId);
        }

        final JSONObject viewer = (JSONObject) request.getAttribute(User.USER);
        if (null != viewer) {
            livenessMgmtService.incLiveness(viewer.optString(Keys.OBJECT_ID), LivenessUtil.LIVENESS_PV);
        }

        dataModelService.fillRelevantArticles(avatarViewMode, dataModel, jsonObject);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);

        // Qiniu file upload authenticate
        final Auth auth = Auth.create(Symphonys.get("qiniu.accessKey"), Symphonys.get("qiniu.secretKey"));
        final String uploadToken = auth.uploadToken(Symphonys.get("qiniu.bucket"));
        dataModel.put("qiniuUploadToken", uploadToken);
        dataModel.put("qiniuDomain", Symphonys.get("qiniu.domain"));

        if (!Symphonys.getBoolean("qiniu.enabled")) {
            dataModel.put("qiniuUploadToken", "");
        }

        final long imgMaxSize = Symphonys.getLong("upload.img.maxSize");
        dataModel.put("imgMaxSize", imgMaxSize);
        final long fileMaxSize = Symphonys.getLong("upload.file.maxSize");
        dataModel.put("fileMaxSize", fileMaxSize);

        // Fill article thank
        Stopwatchs.start("Fills article thank");
        try {
            jsonObject.put(Common.THANKED, rewardQueryService.isRewarded(currentUserId, articleId, RewardUtil.TYPE_C_THANK_ARTICLE));
            jsonObject.put(Common.THANKED_COUNT, rewardQueryService.rewardedCount(articleId, RewardUtil.TYPE_C_THANK_ARTICLE));
            final String articleAuthorId = jsonObject.optString(ArticleUtil.ARTICLE_AUTHOR_ID);
            if (ArticleUtil.ARTICLE_TYPE_C_QNA == jsonObject.optInt(ArticleUtil.ARTICLE_TYPE)) {
                jsonObject.put(Common.OFFERED, rewardQueryService.isRewarded(articleAuthorId, articleId, RewardUtil.TYPE_C_ACCEPT_COMMENT));
                final JSONObject offeredComment = commentQueryService.getOfferedComment(avatarViewMode, cmtViewMode, articleId);
                jsonObject.put(ArticleUtil.ARTICLE_T_OFFERED_COMMENT, offeredComment);
                if (null != offeredComment) {
                    final String offeredCmtId = offeredComment.optString(Keys.OBJECT_ID);
                    offeredComment.put(Common.REWARED_COUNT, rewardQueryService.rewardedCount(offeredCmtId, RewardUtil.TYPE_C_COMMENT));
                    offeredComment.put(Common.REWARDED, rewardQueryService.isRewarded(currentUserId, offeredCmtId, RewardUtil.TYPE_C_COMMENT));
                }
            }
        } finally {
            Stopwatchs.end();
        }

        // Fill previous/next article
        final JSONObject previous = articleQueryService.getPreviousPermalink(articleId);
        final JSONObject next = articleQueryService.getNextPermalink(articleId);
        dataModel.put(ArticleUtil.ARTICLE_T_PREVIOUS, previous);
        dataModel.put(ArticleUtil.ARTICLE_T_NEXT, next);

        String stickConfirmLabel = langPropsService.get("stickConfirmLabel");
        stickConfirmLabel = stickConfirmLabel.replace("{point}", Symphonys.get("pointStickArticle"));
        dataModel.put("stickConfirmLabel", stickConfirmLabel);
        dataModel.put("pointThankArticle", Symphonys.get("pointThankArticle"));
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("articleCommentsPageSize");
        final int windowSize = Symphonys.getInt("articleCommentsWindowSize");

        final int commentCnt = jsonObject.getInt(ArticleUtil.ARTICLE_COMMENT_CNT);
        final int pageCount = (int) Math.ceil((double) commentCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Common.ARTICLE_COMMENTS_PAGE_SIZE, pageSize);

        dataModel.put(Common.DISCUSSION_VIEWABLE, jsonObject.optBoolean(Common.DISCUSSION_VIEWABLE));
        if (!jsonObject.optBoolean(Common.DISCUSSION_VIEWABLE)) {
            jsonObject.put(ArticleUtil.ARTICLE_T_COMMENTS, (Object) Collections.emptyList());
            jsonObject.put(ArticleUtil.ARTICLE_T_NICE_COMMENTS, (Object) Collections.emptyList());

            return null;
        }

        final List<JSONObject> niceComments
                = commentQueryService.getNiceComments(avatarViewMode, cmtViewMode, articleId, 3);
        jsonObject.put(ArticleUtil.ARTICLE_T_NICE_COMMENTS, (Object) niceComments);

        double niceCmtScore = Double.MAX_VALUE;
        if (!niceComments.isEmpty()) {
            niceCmtScore = niceComments.get(niceComments.size() - 1).optDouble(CommentUtil.COMMENT_SCORE, 0D);

            for (final JSONObject comment : niceComments) {
                String thankTemplate = langPropsService.get("thankConfirmLabel");
                thankTemplate = thankTemplate.replace("{point}", String.valueOf(Symphonys.getInt("pointThankComment")))
                        .replace("{user}", comment.optJSONObject(CommentUtil.COMMENT_T_COMMENTER).optString(User.USER_NAME));
                comment.put(CommentUtil.COMMENT_T_THANK_LABEL, thankTemplate);

                final String commentId = comment.optString(Keys.OBJECT_ID);
                if (isLoggedIn) {
                    comment.put(Common.REWARDED, rewardQueryService.isRewarded(currentUserId, commentId, RewardUtil.TYPE_C_COMMENT));
                    final int commentVote = voteQueryService.isVoted(currentUserId, commentId);
                    comment.put(CommentUtil.COMMENT_T_VOTE, commentVote);
                }

                comment.put(Common.REWARED_COUNT, rewardQueryService.rewardedCount(commentId, RewardUtil.TYPE_C_COMMENT));
            }
        }

        // Load comments
        final List<JSONObject> articleComments =
                commentQueryService.getArticleComments(avatarViewMode, articleId, pageNum, pageSize, cmtViewMode);
        jsonObject.put(ArticleUtil.ARTICLE_T_COMMENTS, (Object) articleComments);

        // Fill comment thank
        Stopwatchs.start("Fills comment thank");
        try {
            final String thankTemplate = langPropsService.get("thankConfirmLabel");
            for (final JSONObject comment : articleComments) {
                comment.put(CommentUtil.COMMENT_T_NICE, comment.optDouble(CommentUtil.COMMENT_SCORE, 0D) >= niceCmtScore);

                final String thankStr = thankTemplate.replace("{point}", String.valueOf(Symphonys.getInt("pointThankComment")))
                        .replace("{user}", comment.optJSONObject(CommentUtil.COMMENT_T_COMMENTER).optString(User.USER_NAME));
                comment.put(CommentUtil.COMMENT_T_THANK_LABEL, thankStr);

                final String commentId = comment.optString(Keys.OBJECT_ID);
                if (isLoggedIn) {
                    comment.put(Common.REWARDED,
                            rewardQueryService.isRewarded(currentUserId, commentId, RewardUtil.TYPE_C_COMMENT));
                    final int commentVote = voteQueryService.isVoted(currentUserId, commentId);
                    comment.put(CommentUtil.COMMENT_T_VOTE, commentVote);
                }

                comment.put(Common.REWARED_COUNT, rewardQueryService.rewardedCount(commentId, RewardUtil.TYPE_C_COMMENT));
            }
        } finally {
            Stopwatchs.end();
        }

        // ReferralUtil statistic
        final String referralUserName = request.getParameter("r");
        if (!UserRegisterValidation.invalidUserName(referralUserName)) {
            final UserExt referralUser = userQueryService.getUserByName(referralUserName);
            if (null == referralUser) {
                return url;
            }

            final String viewerIP = Requests.getRemoteAddr(request);

            final JSONObject referral = new JSONObject();
            referral.put(ReferralUtil.REFERRAL_CLICK, 1);
            referral.put(ReferralUtil.REFERRAL_DATA_ID, articleId);
            referral.put(ReferralUtil.REFERRAL_IP, viewerIP);
            referral.put(ReferralUtil.REFERRAL_TYPE, ReferralUtil.REFERRAL_TYPE_C_ARTICLE);
            referral.put(ReferralUtil.REFERRAL_USER, referralUserName);

            referralMgmtService.updateReferral(referral);
        }

        if (StringUtils.isBlank(jsonObject.optString(ArticleUtil.ARTICLE_AUDIO_URL))) {
            final String uid = StringUtils.isBlank(currentUserId) ? "visitor" : currentUserId;

            articleMgmtService.genArticleAudio(article, uid);
        }

        if (StringUtils.isNotBlank(Symphonys.get("ipfs.dir"))) {
            articleMgmtService.saveMarkdown(jsonObject);
        }
        return url;
    }

    /**
     * Adds an article locally.
     * <p>
     * The request json object (an article):
     * <pre>
     * {
     *   "articleTitle": "",
     *   "articleTags": "", // Tags spliting by ','
     *   "articleContent": "",
     *   "articleCommentable": boolean,
     *   "articleType": int,
     *   "articleRewardContent": "",
     *   "articleRewardPoint": int,
     *   "articleQnAOfferPoint": int,
     *   "articleAnonymous": boolean
     * }
     * </pre>
     * </p>
     *
     * @param request the specified request
     */
    @RequestMapping(value = "/article", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, CSRFCheck.class, ArticleAddValidation.class, PermissionCheck.class})
//    @After(adviceClass = StopwatchEndAdvice.class)

//    ArticleAddValidation
    @StopWatchStartAnno
    @LoginCheckAnno
    @CSRFTokenAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public void addArticle(Map<String, Object> dataModel, final HttpServletRequest request, final JSONObject requestJSONObject) {
        dataModel.put(Keys.STATUS_CODE,false);

        final String articleTitle = requestJSONObject.optString(ArticleUtil.ARTICLE_TITLE);
        String articleTags = requestJSONObject.optString(ArticleUtil.ARTICLE_TAGS);
        final String articleContent = requestJSONObject.optString(ArticleUtil.ARTICLE_CONTENT);
        //final boolean articleCommentable = requestJSONObject.optBoolean(ArticleUtil.ARTICLE_COMMENTABLE);
        final boolean articleCommentable = true;
        final int articleType = requestJSONObject.optInt(ArticleUtil.ARTICLE_TYPE, ArticleUtil.ARTICLE_TYPE_C_NORMAL);
        final String articleRewardContent = requestJSONObject.optString(ArticleUtil.ARTICLE_REWARD_CONTENT);
        final int articleRewardPoint = requestJSONObject.optInt(ArticleUtil.ARTICLE_REWARD_POINT);
        final int articleQnAOfferPoint = requestJSONObject.optInt(ArticleUtil.ARTICLE_QNA_OFFER_POINT);
        final String ip = Requests.getRemoteAddr(request);
        final String ua = Headers.getHeader(request, Common.USER_AGENT);
        final boolean isAnonymous = requestJSONObject.optBoolean(ArticleUtil.ARTICLE_ANONYMOUS, false);
        final int articleAnonymous = isAnonymous
                ? ArticleUtil.ARTICLE_ANONYMOUS_C_ANONYMOUS : ArticleUtil.ARTICLE_ANONYMOUS_C_PUBLIC;
        final boolean syncWithSymphonyClient = requestJSONObject.optBoolean(ArticleUtil.ARTICLE_SYNC_TO_CLIENT, false);

        final JSONObject article = new JSONObject();
        article.put(ArticleUtil.ARTICLE_TITLE, articleTitle);
        article.put(ArticleUtil.ARTICLE_CONTENT, articleContent);
        article.put(ArticleUtil.ARTICLE_EDITOR_TYPE, 0);
        article.put(ArticleUtil.ARTICLE_COMMENTABLE, articleCommentable);
        article.put(ArticleUtil.ARTICLE_TYPE, articleType);
        article.put(ArticleUtil.ARTICLE_REWARD_CONTENT, articleRewardContent);
        article.put(ArticleUtil.ARTICLE_REWARD_POINT, articleRewardPoint);
        article.put(ArticleUtil.ARTICLE_QNA_OFFER_POINT, articleQnAOfferPoint);
        article.put(ArticleUtil.ARTICLE_IP, "");
        if (StringUtils.isNotBlank(ip)) {
            article.put(ArticleUtil.ARTICLE_IP, ip);
        }
        article.put(ArticleUtil.ARTICLE_UA, "");
        if (StringUtils.isNotBlank(ua)) {
            article.put(ArticleUtil.ARTICLE_UA, ua);
        }
        article.put(ArticleUtil.ARTICLE_ANONYMOUS, articleAnonymous);
        article.put(ArticleUtil.ARTICLE_SYNC_TO_CLIENT, syncWithSymphonyClient);

        try {
            final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);

            article.put(ArticleUtil.ARTICLE_AUTHOR_ID, currentUser.optString(Keys.OBJECT_ID));

            if (!RoleUtil.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))) {
                articleTags = articleMgmtService.filterReservedTags(articleTags);
            }

            if (ArticleUtil.ARTICLE_TYPE_C_DISCUSSION == articleType && StringUtils.isBlank(articleTags)) {
                articleTags = "小黑屋";
            }

            if (ArticleUtil.ARTICLE_TYPE_C_THOUGHT == articleType && StringUtils.isBlank(articleTags)) {
                articleTags = "思绪";
            }

            article.put(ArticleUtil.ARTICLE_TAGS, articleTags);

            Article articleTmp = JsonUtil.json2Bean(article.toString(),Article.class);
            final String articleId = articleMgmtService.addArticle(articleTmp);

            dataModel.put(Keys.STATUS_CODE, StatusCodes.SUCC);
            dataModel.put(ArticleUtil.ARTICLE_T_ID, articleId);
        } catch ( final Exception e) {
            final String msg = e.getMessage();
            LOGGER.error( "Adds article[title=" + articleTitle + "] failed: {0}", e.getMessage());

            dataModel.put("msg",msg);
            dataModel.put(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    /**
     * Shows update article.
     *

     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestMapping(value = "/update", method = RequestMethod.GET)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
//    @After(adviceClass = {CSRFToken.class, PermissionGrant.class, StopwatchEndAdvice.class})
    @StopWatchStartAnno
    @LoginCheckAnno
    @CSRFTokenAnno
    @PermissionGrantAnno
    @StopWatchEndAnno
    public String showUpdateArticle(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String articleId = request.getParameter("id");
        if (Strings.isEmptyOrNull(articleId)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return null;
        }

        final Article article = articleQueryService.getArticle(articleId);
        if (null == article) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return null;
        }

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        if (null == currentUser
                || !currentUser.optString(Keys.OBJECT_ID).equals(article.getArticleAuthorId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return null;
        }

//        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
//        context.setRenderer(renderer);
//
//        renderer.setTemplateName("/home/post.ftl");
//        final Map<String, Object> dataModel = renderer.getDataModel();
        String url = "/home/post.ftl";

        dataModel.put(ArticleUtil.ARTICLE, article);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        // Qiniu file upload authenticate
        final Auth auth = Auth.create(Symphonys.get("qiniu.accessKey"), Symphonys.get("qiniu.secretKey"));
        final String uploadToken = auth.uploadToken(Symphonys.get("qiniu.bucket"));
        dataModel.put("qiniuUploadToken", uploadToken);
        dataModel.put("qiniuDomain", Symphonys.get("qiniu.domain"));

        if (!Symphonys.getBoolean("qiniu.enabled")) {
            dataModel.put("qiniuUploadToken", "");
        }

        final long imgMaxSize = Symphonys.getLong("upload.img.maxSize");
        dataModel.put("imgMaxSize", imgMaxSize);
        final long fileMaxSize = Symphonys.getLong("upload.file.maxSize");
        dataModel.put("fileMaxSize", fileMaxSize);

        fillDomainsWithTags(dataModel);

        String rewardEditorPlaceholderLabel = langPropsService.get("rewardEditorPlaceholderLabel");
        rewardEditorPlaceholderLabel = rewardEditorPlaceholderLabel.replace("{point}",
                String.valueOf(PointtransferUtil.TRANSFER_SUM_C_ADD_ARTICLE_REWARD));
        dataModel.put("rewardEditorPlaceholderLabel", rewardEditorPlaceholderLabel);
        dataModel.put(Common.BROADCAST_POINT, PointtransferUtil.TRANSFER_SUM_C_ADD_ARTICLE_BROADCAST);

        final String b3logKey = currentUser.optString(UserExtUtil.USER_B3_KEY);
        dataModel.put("hasB3Key", !Strings.isEmptyOrNull(b3logKey));

        fillPostArticleRequisite(dataModel, currentUser);
        return url;
    }

    /**
     * Updates an article locally.
     * <p>
     * The request json object (an article):
     * <pre>
     * {
     *   "articleTitle": "",
     *   "articleTags": "", // Tags spliting by ','
     *   "articleContent": "",
     *   "articleCommentable": boolean,
     *   "articleType": int,
     *   "articleRewardContent": "",
     *   "articleRewardPoint": int,
     *    "articleQnAOfferPoint": int
     * }
     * </pre>
     * </p>
     *

     * @param request  the specified request
     * @param response the specified response
     * @param id       the specified article id
     * @throws Exception exception
     */
    @RequestMapping(value = "/article/{id}", method = RequestMethod.PUT)
//    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, CSRFCheck.class, ArticleUpdateValidation.class,
//            PermissionCheck.class})
//    @After(adviceClass = StopwatchEndAdvice.class)

//    ArticleUpdateValidation
    @StopWatchStartAnno
    @LoginCheckAnno
    @CSRFCheckAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public void updateArticle(Map<String, Object> dataModel, final HttpServletRequest request, final HttpServletResponse response,
                              final String id) throws Exception {
        if (Strings.isEmptyOrNull(id)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final int avatarViewMode = (int) request.getAttribute(UserExtUtil.USER_AVATAR_VIEW_MODE);

        final Article oldArticle = articleQueryService.getArticleById(avatarViewMode, id);
        if (null == oldArticle) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

//        dataModel.put(Keys.STATUS_CODE,false);
        dataModel.put(Keys.STATUS_CODE,false);

        if (ArticleUtil.ARTICLE_STATUS_C_VALID != oldArticle.getArticleStatus()) {
            dataModel.put(Keys.MSG , langPropsService.get("articleLockedLabel"));
//            dataModel.put(Keys.MSG ,langPropsService.get("articleLockedLabel"));
            dataModel.put(Keys.STATUS_CODE, StatusCodes.ERR);

            return;
        }

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String articleTitle = requestJSONObject.optString(ArticleUtil.ARTICLE_TITLE);
        String articleTags = requestJSONObject.optString(ArticleUtil.ARTICLE_TAGS);
        final String articleContent = requestJSONObject.optString(ArticleUtil.ARTICLE_CONTENT);
        //final boolean articleCommentable = requestJSONObject.optBoolean(ArticleUtil.ARTICLE_COMMENTABLE);
        final boolean articleCommentable = true;
        final int articleType = requestJSONObject.optInt(ArticleUtil.ARTICLE_TYPE, ArticleUtil.ARTICLE_TYPE_C_NORMAL);
        final String articleRewardContent = requestJSONObject.optString(ArticleUtil.ARTICLE_REWARD_CONTENT);
        final int articleRewardPoint = requestJSONObject.optInt(ArticleUtil.ARTICLE_REWARD_POINT);
        final int articleQnAOfferPoint = requestJSONObject.optInt(ArticleUtil.ARTICLE_QNA_OFFER_POINT);
        final String ip = Requests.getRemoteAddr(request);
        final String ua = Headers.getHeader(request, Common.USER_AGENT);

        final JSONObject article = new JSONObject();
        article.put(Keys.OBJECT_ID, id);
        article.put(ArticleUtil.ARTICLE_TITLE, articleTitle);
        article.put(ArticleUtil.ARTICLE_CONTENT, articleContent);
        article.put(ArticleUtil.ARTICLE_EDITOR_TYPE, 0);
        article.put(ArticleUtil.ARTICLE_COMMENTABLE, articleCommentable);
        article.put(ArticleUtil.ARTICLE_TYPE, articleType);
        article.put(ArticleUtil.ARTICLE_REWARD_CONTENT, articleRewardContent);
        article.put(ArticleUtil.ARTICLE_REWARD_POINT, articleRewardPoint);
        article.put(ArticleUtil.ARTICLE_QNA_OFFER_POINT, articleQnAOfferPoint);
        article.put(ArticleUtil.ARTICLE_IP, "");
        if (StringUtils.isNotBlank(ip)) {
            article.put(ArticleUtil.ARTICLE_IP, ip);
        }
        article.put(ArticleUtil.ARTICLE_UA, "");
        if (StringUtils.isNotBlank(ua)) {
            article.put(ArticleUtil.ARTICLE_UA, ua);
        }

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        if (null == currentUser
                || !currentUser.optString(Keys.OBJECT_ID).equals(oldArticle.getArticleAuthorId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        article.put(ArticleUtil.ARTICLE_AUTHOR_ID, currentUser.optString(Keys.OBJECT_ID));

        if (!RoleUtil.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))) {
            articleTags = articleMgmtService.filterReservedTags(articleTags);
        }

        if (ArticleUtil.ARTICLE_TYPE_C_DISCUSSION == articleType && StringUtils.isBlank(articleTags)) {
            articleTags = "小黑屋";
        }

        if (ArticleUtil.ARTICLE_TYPE_C_THOUGHT == articleType && StringUtils.isBlank(articleTags)) {
            articleTags = "思绪";
        }

        article.put(ArticleUtil.ARTICLE_TAGS, articleTags);

        try {
            Article bean = JsonUtil.json2Bean(article.toString(),Article.class);
            articleMgmtService.updateArticle(bean);

            dataModel.put(Keys.STATUS_CODE, StatusCodes.SUCC);
            dataModel.put(ArticleUtil.ARTICLE_T_ID, id);
        } catch ( final Exception e) {
            final String msg = e.getMessage();
            LOGGER.error( "Adds article[title=" + articleTitle + "] failed: {0}", e.getMessage());

            dataModel.put("msg",msg);
//            dataModel.put(Keys.STATUS_CODE, StatusCodes.ERR);
            dataModel.put(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    /**
     * Markdowns.
     * <p>
     * Renders the response with a json object, for example,
     * <pre>
     * {
     *     "html": ""
     * }
     * </pre>
     * </p>
     *
     * @param request  the specified http servlet request
     * @param response the specified http servlet response

     */
    @RequestMapping(value = "/markdown", method = RequestMethod.POST)
//    @Before(adviceClass = StopwatchStartAdvice.class)
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @StopWatchEndAnno
    public void markdown2HTML(final HttpServletRequest request, final HttpServletResponse response, Map<String, Object> dataModel) {
//        dataModel.put(Keys.STATUS_CODE,true);
        dataModel.put(Keys.STATUS_CODE, true);
        String markdownText = request.getParameter("markdownText");
        if (Strings.isEmptyOrNull(markdownText)) {
//            dataModel.put("html", "");
            dataModel.put("html", "");
            return;
        }

        markdownText = shortLinkQueryService.linkArticle(markdownText);
        markdownText = shortLinkQueryService.linkTag(markdownText);
        markdownText = Emotions.toAliases(markdownText);
        markdownText = Emotions.convert(markdownText);
        markdownText = Markdowns.toHTML(markdownText);
        markdownText = Markdowns.clean(markdownText, "");
        markdownText = MP3Players.render(markdownText);
        markdownText = VideoPlayers.render(markdownText);

//        dataModel.put("html", markdownText);
//        dataModel.put(Keys.STATUS_CODE,true);
        dataModel.put("html", markdownText);
    }

    /**
     * Gets article preview content.
     * <p>
     * Renders the response with a json object, for example,
     * <pre>
     * {
     *     "html": ""
     * }
     * </pre>
     * </p>
     *
     * @param request   the specified http servlet request
     * @param response  the specified http servlet response
     * @param articleId the specified article id
     * @throws Exception exception
     */
    @RequestMapping(value = "/article/{articleId}/preview", method = RequestMethod.GET)
//    @Before(adviceClass = StopwatchStartAdvice.class)
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @StopWatchEndAnno
    public void getArticlePreviewContent(final HttpServletRequest request, final HttpServletResponse response,
                                         Map<String, Object> dataModel, final String articleId) throws Exception {
        final String content = articleQueryService.getArticlePreviewContent(articleId, request);
        if (StringUtils.isBlank(content)) {
//            context.renderJSON().renderFalseResult();
            dataModel.put(Keys.STATUS_CODE,false);

            return;
        }

//        context.renderJSON(true).renderJSONValue("html", content);
        dataModel.put(Keys.STATUS_CODE,true);
        dataModel.put("html", content);
    }

    /**
     * Article rewards.
     *
     * @param request  the specified http servlet request
     * @param response the specified http servlet response

     * @throws Exception exception
     */
    @RequestMapping(value = "/article/reward", method = RequestMethod.POST)
//    @Before(adviceClass = StopwatchStartAdvice.class)
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @StopWatchEndAnno
    public void reward(final HttpServletRequest request, final HttpServletResponse response, Map<String, Object> dataModel)
            throws Exception {
        final UserExt currentUser = userQueryService.getCurrentUser(request);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final String articleId = request.getParameter(ArticleUtil.ARTICLE_T_ID);
        if (Strings.isEmptyOrNull(articleId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);

            return;
        }

//        dataModel.put(Keys.STATUS_CODE,false);
        dataModel.put(Keys.STATUS_CODE,false);

        try {
            articleMgmtService.reward(articleId, currentUser.getOid());
        } catch ( final Exception e) {
//            dataModel.put(Keys.MSG ,langPropsService.get("transferFailLabel"));
            dataModel.put(Keys.MSG,langPropsService.get("transferFailLabel"));
            return;
        }

        final Article article = articleQueryService.getArticle(articleId);
        JSONObject jsonObject = new JSONObject(JsonUtil.objectToJson(article));
        articleQueryService.processArticleContent(jsonObject, request);

        final String rewardContent = jsonObject.optString(ArticleUtil.ARTICLE_REWARD_CONTENT);
//        context.renderTrueResult().renderJSONValue(ArticleUtil.ARTICLE_REWARD_CONTENT, rewardContent);
        dataModel.put(Keys.STATUS_CODE,true);
        dataModel.put(ArticleUtil.ARTICLE_REWARD_CONTENT, rewardContent);
    }

    /**
     * Article thanks.
     *
     * @param request  the specified http servlet request
     * @param response the specified http servlet response

     * @throws Exception exception
     */
    @RequestMapping(value = "/article/thank", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public void thank(final HttpServletRequest request, final HttpServletResponse response, Map<String, Object> dataModel)
            throws Exception {
        final UserExt currentUser = userQueryService.getCurrentUser(request);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final String articleId = request.getParameter(ArticleUtil.ARTICLE_T_ID);
        if (Strings.isEmptyOrNull(articleId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);

            return;
        }

//        dataModel.put(Keys.STATUS_CODE,false);
        dataModel.put(Keys.STATUS_CODE,false);
        try {
            articleMgmtService.thank(articleId, currentUser.getOid());
        } catch (final Exception e) {
//            dataModel.put(Keys.MSG ,langPropsService.get("transferFailLabel"));
            dataModel.put(Keys.MSG,langPropsService.get("transferFailLabel"));

            return;
        }

//        dataModel.put(Keys.STATUS_CODE,true);
        dataModel.put(Keys.STATUS_CODE,true);
    }

    /**
     * Sticks an article.
     *
     * @param request  the specified HTTP servlet request
     * @param response the specified HTTP servlet response

     * @throws Exception exception
     */
    @RequestMapping(value = "/article/stick", method = RequestMethod.POST)
//    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @PermissionCheckAnno
    @StopWatchEndAnno
    public void stickArticle(final HttpServletRequest request, final HttpServletResponse response, Map<String, Object> dataModel)
            throws Exception {
        final UserExt currentUser = userQueryService.getCurrentUser(request);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final String articleId = request.getParameter(ArticleUtil.ARTICLE_T_ID);
        if (Strings.isEmptyOrNull(articleId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);

            return;
        }

        final Article article = articleQueryService.getArticle(articleId);
        if (null == article) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        if (!currentUser.getOid().equals(article.getArticleAuthorId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

//        dataModel.put(Keys.STATUS_CODE,false);
        dataModel.put(Keys.STATUS_CODE,false);

        try {
            articleMgmtService.stick(articleId);
        } catch (final Exception e) {
//            dataModel.put(Keys.MSG ,e.getMessage());
            dataModel.put(Keys.MSG,e.getMessage());

            return;
        }

//        context.renderTrueResult().renderMsg(langPropsService.get("stickSuccLabel"));
        dataModel.put(Keys.STATUS_CODE,true);
        dataModel.put(Keys.MSG,langPropsService.get("stickSuccLabel"));
    }

    /**
     * Expires a sticked article.
     *
     * @param request  the specified HTTP servlet request
     * @param response the specified HTTP servlet response

     * @throws Exception exception
     */
    @RequestMapping(value = "/cron/article/stick-expire", method = RequestMethod.GET)
//    @Before(adviceClass = StopwatchStartAdvice.class)
//    @After(adviceClass = StopwatchEndAdvice.class)
    @StopWatchStartAnno
    @StopWatchEndAnno
    public void expireStickArticle(final HttpServletRequest request, final HttpServletResponse response, Map<String, Object> dataModel)
            throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        articleMgmtService.expireStick();

//        dataModel.put(Keys.STATUS_CODE,true);
        dataModel.put(Keys.STATUS_CODE,true);
    }
}
