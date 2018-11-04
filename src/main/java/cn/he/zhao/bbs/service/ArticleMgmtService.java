package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entityUtil.*;
import cn.he.zhao.bbs.event.AddArticleEvent;
import cn.he.zhao.bbs.event.EventTypes;
import cn.he.zhao.bbs.event.UpdateArticleEvent;
import cn.he.zhao.bbs.spring.Common;
import cn.he.zhao.bbs.spring.Strings;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArticleMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleMgmtService.class);

    /**
     * Tag max count.
     */
    private static final int TAG_MAX_CNT = 4;

    /**
     * Comment Mapper.
     */
    @Autowired
    private CommentMapper commentMapper;

    /**
     * Article Mapper.
     */
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * Tag Mapper.
     */
    @Autowired
    private TagMapper tagMapper;

    /**
     * Tag-Article Mapper.
     */
    @Autowired
    private TagArticleMapper tagArticleMapper;

    /**
     * User Mapper.
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * User-Tag Mapper.
     */
    @Autowired
    private UserTagMapper userTagMapper;

    /**
     * OptionUtil Mapper.
     */
    @Autowired
    private OptionMapper optionMapper;

    /**
     * NotificationUtil Mapper.
     */
    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * RevisionUtil Mapper.
     */
    @Autowired
    private RevisionMapper revisionMapper;

    /**
     * Tag management service.
     */
    @Autowired
    private TagMgmtService tagMgmtService;

    /**
     * Event manager.
     */
    @Autowired
    private EventManager eventManager;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * PointtransferUtil management service.
     */
    @Autowired
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * RewardUtil management service.
     */
    @Autowired
    private RewardMgmtService rewardMgmtService;

    /**
     * RewardUtil query service.
     */
    @Autowired
    private RewardQueryService rewardQueryService;

    /**
     * FollowUtil query service.
     */
    @Autowired
    private FollowQueryService followQueryService;

    /**
     * Tag query service.
     */
    @Autowired
    private TagQueryService tagQueryService;
    /**
     * NotificationUtil management service.
     */
    @Autowired
    private NotificationMgmtService notificationMgmtService;

    /**
     * LivenessUtil management service.
     */
    @Autowired
    private LivenessMgmtService livenessMgmtService;

    /**
     * Search management service.
     */
    @Autowired
    private SearchMgmtService searchMgmtService;

    /**
     * Audio management service.
     */
    @Autowired
    private AudioMgmtService audioMgmtService;

    /**
     * Determines whether the specified tag title exists in the specified tags.
     *
     * @param tagTitle the specified tag title
     * @param tags     the specified tags
     * @return {@code true} if it exists, {@code false} otherwise
     * @throws JSONException json exception
     */
    private static boolean tagExists(final String tagTitle, final List<Tag> tags) throws JSONException {
        for (final Tag tag : tags) {
            if (tag.getTagTitle().equals(tagTitle)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Removes an article specified with the given article id. An article is removable if:
     * <ul>
     * <li>No comments</li>
     * <li>No watches, collects, ups, downs</li>
     * <li>No rewards</li>
     * <li>No thanks</li>
     * <li>In valid status</li>
     * </ul>
     * Sees https://github.com/b3log/symphony/issues/450 for more details.
     *
     * @param articleId the given article id
     * @throws Exception service exception
     */
    public void removeArticle(final String articleId) throws Exception {
        Article article = null;

        try {
            article = articleMapper.getByOid(articleId);
        } catch (final Exception e) {
            LOGGER.error( "Gets article [id=" + articleId + "] failed", e);
        }

        if (null == article) {
            return;
        }

        if (ArticleUtil.ARTICLE_STATUS_C_VALID != article.getArticleStatus()) {
            throw new Exception(langPropsService.get("articleLockedLabel"));
        }

        final int commentCnt = article.getArticleCommentCount();
        if (commentCnt > 0) {
            throw new Exception(langPropsService.get("removeArticleFoundCmtLabel"));
        }

        final int watchCnt = article.getArticleWatchCnt();
        final int collectCnt = article.getArticleCollectCnt();
        final int ups = article.getArticleGoodCnt();
        final int downs = article.getArticleBadCnt();
        if (watchCnt > 0 || collectCnt > 0 || ups > 0 || downs > 0) {
            throw new Exception(langPropsService.get("removeArticleFoundWatchEtcLabel"));
        }

        final int rewardCnt = (int) rewardQueryService.rewardedCount(articleId, RewardUtil.TYPE_C_ARTICLE);
        if (rewardCnt > 0) {
            throw new Exception(langPropsService.get("removeArticleFoundRewardLabel"));
        }

        final int thankCnt = (int) rewardQueryService.rewardedCount(articleId, RewardUtil.TYPE_C_THANK_ARTICLE);
        if (thankCnt > 0) {
            throw new Exception(langPropsService.get("removeArticleFoundThankLabel"));
        }

        // Perform removal
        removeArticleByAdmin(articleId);
    }

    /**
     * Generates article's audio.
     *
     * @param article the specified article
     * @param userId  the specified user id
     */
    @Transactional
    public void genArticleAudio(final Article article, final String userId) {
        if (ArticleUtil.ARTICLE_TYPE_C_THOUGHT == article.getArticleType()
                || ArticleUtil.ARTICLE_TYPE_C_DISCUSSION == article.getArticleType()) {
            return;
        }

        final String tags = article.getArticleTags();
        if (StringUtils.containsIgnoreCase(tags, TagUtil.TAG_TITLE_C_SANDBOX)) {
            return;
        }

        final String articleId = article.getOid();
        String previewContent = article.getArticleContent();
        previewContent = Markdowns.toHTML(previewContent);
        final Document doc = Jsoup.parse(previewContent);
        final Elements elements = doc.select("a, img, iframe, object, video");
        for (final Element element : elements) {
            element.remove();
        }
        previewContent = Emotions.clear(doc.text());
        previewContent = StringUtils.substring(previewContent, 0, 512);
        final String contentToTTS = previewContent;

        new Thread(() -> {
//            final Transaction transaction = articleMapper.beginTransaction();

            try {
                String audioURL = "";
                if (StringUtils.length(contentToTTS) < 96 || Runes.getChinesePercent(contentToTTS) < 40) {
                    LOGGER.trace("Content is too short to TTS [contentToTTS=" + contentToTTS + "]");
                } else {
                    audioURL = audioMgmtService.tts(contentToTTS, ArticleUtil.ARTICLE, articleId, userId);
                }
                if (StringUtils.isBlank(audioURL)) {
                    return;
                }

                article.setArticleAudioURL( audioURL);

                final Article toUpdate = articleMapper.getByOid(articleId);
                toUpdate.setArticleAudioURL( audioURL);

                articleMapper.update( toUpdate);
//                transaction.commit();

                if (StringUtils.isNotBlank(audioURL)) {
                    LOGGER.debug("Generated article [id=" + articleId + "] audio");
                }
            } catch (final Exception e) {
//                if (transaction.isActive()) {
//                    transaction.rollback();
//                }

                LOGGER.error( "Updates article's audio URL failed", e);
            } finally {
//                JdbcMapper.dispose();
            }
        }).start();
    }

    /**
     * Removes an article specified with the given article id. Calls this method will remove all existed data related
     * with the specified article forcibly.
     *
     * @param articleId the given article id
     */
    @Transactional
    public void removeArticleByAdmin(final String articleId) {
        try {
            final Article article = articleMapper.getByOid(articleId);
            if (null == article) {
                return;
            }

//            Query query = new Query().setFilter(new PropertyFilter(
//                    CommentUtil.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId)).setPageCount(1);
//            final JSONArray comments = commentMapper.get(query).optJSONArray(Keys.RESULTS);
//            final int commentCnt = comments.length();
//            for (int i = 0; i < commentCnt; i++) {
//                final JSONObject comment = comments.optJSONObject(i);
//                final String commentId = comment.optString(Keys.OBJECT_ID);
//
//                commentMapper.removeComment(commentId);
//            }
            // TODO: 2018/9/2 删除该帖子下的的所有评论
            commentMapper.removeCommentByArticleId(articleId);

            //作者帖子数-1
            final String authorId = article.getArticleAuthorId();
            final UserExt author = userMapper.get(authorId);
            author.setUserArticleCount(author.getUserArticleCount() -1);
            userMapper.update(author.getOid(), author);

            //城市帖子数-1
            final String city = article.getArticleCity();
            final String cityStatId = city + "-ArticleCount";
            final Option cityArticleCntOption = optionMapper.get(cityStatId);
            if (null != cityArticleCntOption) {
                Long value =  Long.parseLong(cityArticleCntOption.getOptionValue()) - 1;
                cityArticleCntOption.setOptionValue(value.toString());
                optionMapper.update( cityArticleCntOption);
            }

            final Option articleCntOption = optionMapper.get(OptionUtil.ID_C_STATISTIC_ARTICLE_COUNT);
            Long value =  Long.parseLong(articleCntOption.getOptionValue()) - 1;
            articleCntOption.setOptionValue(value.toString());
            optionMapper.update( articleCntOption);

            articleMapper.remove(articleId);

            // Remove article revisions
//            query = new Query().setFilter(CompositeFilterOperator.and(
//                    new PropertyFilter(RevisionUtil.REVISION_DATA_ID, FilterOperator.EQUAL, articleId),
//                    new PropertyFilter(RevisionUtil.REVISION_DATA_TYPE, FilterOperator.EQUAL, RevisionUtil.DATA_TYPE_C_ARTICLE)
//            ));
//            final JSONArray articleRevisions = revisionMapper.get(articleId, RevisionUtil.DATA_TYPE_C_ARTICLE);
//            for (int i = 0; i < articleRevisions.length(); i++) {
//                final JSONObject articleRevision = articleRevisions.optJSONObject(i);
//                revisionMapper.remove(articleRevision.optString(Keys.OBJECT_ID));
//            }
            // Remove article revisions
            revisionMapper.remove(articleId, RevisionUtil.DATA_TYPE_C_ARTICLE);

            final List<TagArticle> tagArticleRels = tagArticleMapper.getByArticleId(articleId);
            for (final TagArticle tagArticleRel : tagArticleRels) {
                final String tagId = tagArticleRel.getTag_oid();
                final Tag tag = tagMapper.get(tagId);
                int cnt = tag.getTagReferenceCount() - 1;
                cnt = cnt < 0 ? 0 : cnt;
                tag.setTagReferenceCount(cnt);
                tag.setTagRandomDouble(Math.random());

                tagMapper.update(tagId, tag);
            }

            tagArticleMapper.removeByArticleId(articleId);
            notificationMapper.removeByDataId(articleId);

            if (Symphonys.getBoolean("algolia.enabled")) {
                searchMgmtService.removeAlgoliaDocument(article);
            }

            if (Symphonys.getBoolean("es.enabled")) {
                searchMgmtService.removeESDocument(article, ArticleUtil.ARTICLE);
            }
        } catch (final Exception e) {
            LOGGER.error( "Removes an article error [id=" + articleId + "]", e);
        }
    }

    /**
     * Increments the view count of the specified article by the given article id.
     *
     * @param articleId the given article id
     */
    @Transactional
    public void incArticleViewCount(final String articleId) {
        Symphonys.EXECUTOR_SERVICE.submit(() -> {
//            final Transaction transaction = articleMapper.beginTransaction();
            try {
                final Article article = articleMapper.getByOid(articleId);
                if (null == article) {
//                    if (transaction.isActive()) {
//                        transaction.rollback();
//                    }

                    return;
                }

                final int viewCnt = article.getArticleViewCount();
                article.setArticleViewCount(viewCnt + 1);
                article.setArticleRandomDouble( Math.random());

                articleMapper.update( article);

//                transaction.commit();
            } catch (final Exception e) {
//                if (transaction.isActive()) {
//                    transaction.rollback();
//                }

                LOGGER.error( "Incs an article view count failed", e);
            }
        });
    }

    /**
     * Adds an article with the specified request json object.
     *
     * @param article the specified request json object, for example,
     *                          "articleTitle": "",
     *                          "articleTags": "",
     *                          "articleContent": "",
     *                          "articleEditorType": "",
     *                          "articleAuthorId": "",
     *                          "articleCommentable": boolean, // optional, default to true
     *                          "syncWithSymphonyClient": boolean, // optional
     *                          "clientArticleId": "", // optional
     *                          "clientArticlePermalink": "", // optional
     *                          "isBroadcast": boolean, // Client broadcast, optional
     *                          "articleType": int, // optional, default to 0
     *                          "articleRewardContent": "", // optional, default to ""
     *                          "articleRewardPoint": int, // optional, default to 0
     *                          "articleQnAOfferPoint": int, // optional, default to 0
     *                          "articleIP": "", // optional, default to ""
     *                          "articleUA": "", // optional, default to ""
     *                          "articleAnonymous": int, // optional, default to 0 (public)
     *                          "articleAnonymousView": int // optional, default to 0 (use global)
     *                          , see {@link Article} for more details
     * @return generated article id
     * @throws Exception service exception
     */
    public synchronized String addArticle(final Article article) throws Exception {
        final long currentTimeMillis = System.currentTimeMillis();
        final String  clientArticleId = article.getClientArticleId();
        boolean fromClient = false;
        if (clientArticleId != null){
            fromClient = true;
        }

        final String authorId = article.getArticleAuthorId();
        UserExt author = null;

        final int rewardPoint = article.getArticleRewardPoint();//0
        if (rewardPoint < 0) {
            throw new Exception(langPropsService.get("invalidRewardPointLabel"));
        }

        final int articleAnonymous = article.getArticleAnonymous();

//        final boolean syncWithSymphonyClient = article.optBoolean(Article.ARTICLE_SYNC_TO_CLIENT);
        final String syncWithSymphonyClient = article.getSyncWithSymphonyClient();

        String articleTitle = article.getArticleTitle();
        articleTitle = Emotions.toAliases(articleTitle);
        articleTitle = Pangu.spacingText(articleTitle);
        articleTitle = StringUtils.trim(articleTitle);

        final int qnaOfferPoint = article.getArticleQnAOfferPoint();//0

        final int articleType = article.getArticleType();//0
        if (ArticleUtil.ARTICLE_TYPE_C_QNA == articleType && 20 > qnaOfferPoint) { // https://github.com/b3log/symphony/issues/672
            throw new Exception(langPropsService.get("invalidQnAOfferPointLabel"));
        }

        try {
            // check if admin allow to add article
            final Option option = optionMapper.get(OptionUtil.ID_C_MISC_ALLOW_ADD_ARTICLE);

            if (!"0".equals(option.getOptionValue())) {
                throw new Exception(langPropsService.get("notAllowAddArticleLabel"));
            }

            author = userMapper.get(authorId);

            if (currentTimeMillis - Long.parseLong(author.getOid()) < Symphonys.getLong("newbieFirstArticle")) {
                String tip = langPropsService.get("newbieFirstArticleLabel");
                final long time = Long.parseLong(author.getOid()) + Symphonys.getLong("newbieFirstArticle");
                final String timeStr = DateFormatUtils.format(time, "yyyy-MM-dd HH:mm:ss");
                tip = tip.replace("${time}", timeStr);

                throw new Exception(tip);
            }

            if (currentTimeMillis - author.getUserLatestArticleTime() < Symphonys.getLong("minStepArticleTime")
                    && !RoleUtil.ROLE_ID_C_ADMIN.equals(author.getUserRole())) {
                LOGGER.warn( "Adds article too frequent [userName={0}]", author.getUserName());
                throw new Exception(langPropsService.get("tooFrequentArticleLabel"));
            }

            final int balance = author.getUserPoint();
            if (ArticleUtil.ARTICLE_ANONYMOUS_C_ANONYMOUS == articleAnonymous) {
                final int anonymousPoint = Symphonys.getInt("anonymous.point");
                if (balance < anonymousPoint) {
                    String anonymousEnabelPointLabel = langPropsService.get("anonymousEnabelPointLabel");
                    anonymousEnabelPointLabel
                            = anonymousEnabelPointLabel.replace("${point}", String.valueOf(anonymousPoint));
                    throw new Exception(anonymousEnabelPointLabel);
                }
            }

            if (!fromClient && ArticleUtil.ARTICLE_ANONYMOUS_C_PUBLIC == articleAnonymous) {
                // Point
                final long followerCnt = followQueryService.getFollowerCount(authorId, FollowUtil.FOLLOWING_TYPE_C_USER);
                final int addition = (int) Math.round(Math.sqrt(followerCnt));
                final int broadcast = ArticleUtil.ARTICLE_TYPE_C_CITY_BROADCAST == articleType ?
                        PointtransferUtil.TRANSFER_SUM_C_ADD_ARTICLE_BROADCAST : 0;

                final int sum = PointtransferUtil.TRANSFER_SUM_C_ADD_ARTICLE + addition + rewardPoint + qnaOfferPoint + broadcast;

                if (balance - sum < 0) {
                    throw new Exception(langPropsService.get("insufficientBalanceLabel"));
                }
            }

            if (ArticleUtil.ARTICLE_TYPE_C_DISCUSSION != articleType) {
                final Article maybeExist = articleMapper.getByTitle(articleTitle);
                if (null != maybeExist) {
                    final String existArticleAuthorId = maybeExist.getArticleAuthorId();
                    String msg;
                    if (existArticleAuthorId.equals(authorId)) {
                        msg = langPropsService.get("duplicatedArticleTitleSelfLabel");
                        msg = msg.replace("{article}", "<a target='_blank' href='/article/" + maybeExist.getOid()
                                + "'>" + articleTitle + "</a>");
                    } else {
                        final UserExt existArticleAuthor = userMapper.get(existArticleAuthorId);
                        final String userName = existArticleAuthor.getUserName();
                        msg = langPropsService.get("duplicatedArticleTitleLabel");
                        msg = msg.replace("{user}", "<a target='_blank' href='/member/" + userName + "'>" + userName + "</a>");
                        msg = msg.replace("{article}", "<a target='_blank' href='/article/" + maybeExist.getOid()
                                + "'>" + articleTitle + "</a>");
                    }

                    throw new Exception(msg);
                }
            }
        } catch (final Exception e) {
            throw new Exception(e);
        }

//        final Transaction transaction = articleMapper.beginTransaction();

        try {
            final String oid = Ids.genTimeMillisId();
//            final JSONObject article = new JSONObject();
            article.setOid(oid);

//            final String clientArticleId = article.getClientArticleId( );//oid
            final String clientArticlePermalink = article.getClientArticlePermalink();

            article.setArticleTitle( articleTitle);
//            article.put(Article.ARTICLE_TAGS, requestJSONObject.optString(Article.ARTICLE_TAGS));

            String articleContent = article.getArticleContent();
            articleContent = Emotions.toAliases(articleContent);
            //articleContent = StringUtils.trim(articleContent) + " "; https://github.com/b3log/symphony/issues/389
            articleContent = StringUtils.replace(articleContent, langPropsService.get("uploadingLabel", Locale.SIMPLIFIED_CHINESE), "");
            articleContent = StringUtils.replace(articleContent, langPropsService.get("uploadingLabel", Locale.US), "");
            article.setArticleContent( articleContent);

            String rewardContent = article.getArticleRewardContent();
            rewardContent = Emotions.toAliases(rewardContent);
            article.setArticleContent( rewardContent);

//            article.put(Article.ARTICLE_EDITOR_TYPE, requestJSONObject.optString(Article.ARTICLE_EDITOR_TYPE));

            article.setSyncWithSymphonyClient( String.valueOf(fromClient ? true : Boolean.valueOf(author.getSyncWithSymphonyClient())));

            article.setArticleAuthorId(authorId);
            article.setArticleCommentCount(0);
            article.setArticleViewCount(0);
            article.setArticleGoodCnt(0);
            article.setArticleBadCnt(0);
            article.setArticleCollectCnt(0);
            article.setArticleWatchCnt( 0);
            if(article.getArticleCommentable() != null){
                article.setArticleCommentable( article.getArticleCommentable());
            } else {
                article.setArticleCommentable( String.valueOf(true));
            }
            article.setArticleCreateTime( currentTimeMillis);
            article.setArticleUpdateTime( currentTimeMillis);
            article.setArticleLatestCmtTime(0L);
            article.setArticleLatestCmterName("");
            article.setArticlePermalink("/article/" + oid);
            article.setClientArticleId(clientArticleId);
            article.setClientArticlePermalink(clientArticlePermalink);
            article.setArticleRandomDouble( Math.random());
            article.setRedditScore(0D);
            article.setArticleStatus(ArticleUtil.ARTICLE_STATUS_C_VALID);
            article.setArticleType(articleType);
            article.setArticleRewardPoint(rewardPoint);
            article.setArticleQnAOfferPoint( qnaOfferPoint);
            article.setArticlePushOrder( 0);
            String city = "";
            if (UserExtUtil.USER_GEO_STATUS_C_PUBLIC == author.getUserGeoStatus()) {
                city = author.getUserCity();
            }
            article.setArticleCity(city);
            article.setArticleAnonymous(articleAnonymous);
            article.setSyncWithSymphonyClient(syncWithSymphonyClient);
            article.setArticlePerfect(ArticleUtil.ARTICLE_PERFECT_C_NOT_PERFECT);
            if (article.getArticleAnonymousView() == null){
                article.setArticleAnonymousView(ArticleUtil.ARTICLE_ANONYMOUS_VIEW_C_USE_GLOBAL);//
            }

            article.setArticleAudioURL("");

            String articleTags = article.getArticleTags();
            articleTags = TagUtil.formatTags(articleTags);
            boolean sandboxEnv = false;
            if (StringUtils.containsIgnoreCase(articleTags, TagUtil.TAG_TITLE_C_SANDBOX)) {
                articleTags = TagUtil.TAG_TITLE_C_SANDBOX;
                sandboxEnv = true;
            }

            String[] tagTitles = articleTags.split(",");
            if (!sandboxEnv && tagTitles.length < TAG_MAX_CNT && tagTitles.length < 3
                    && ArticleUtil.ARTICLE_TYPE_C_DISCUSSION != articleType
                    && ArticleUtil.ARTICLE_TYPE_C_THOUGHT != articleType && !TagUtil.containsReservedTags(articleTags)) {
                final String content = article.getArticleTitle()
                        + " " + Jsoup.parse("<p>" + article.getArticleContent() + "</p>").text();

                final List<String> genTags = tagQueryService.generateTags(content, 1);
                if (!genTags.isEmpty()) {
                    articleTags = articleTags + "," + StringUtils.join(genTags, ",");
                    articleTags = TagUtil.formatTags(articleTags);
                    articleTags = TagUtil.useHead(articleTags, TAG_MAX_CNT);
                }
            }

            if (StringUtils.isBlank(articleTags)) {
                articleTags = "B3log";
            }

            articleTags = TagUtil.formatTags(articleTags);
            if (ArticleUtil.ARTICLE_TYPE_C_QNA == articleType && !StringUtils.contains(articleTags, "Q&A")) {
                articleTags += ",Q&A";
            }
            article.setArticleTags( articleTags);
            tagTitles = articleTags.split(",");

            tag(tagTitles, article, author);

//            final String ip = article.getArticleIP();
//            article.setArticleIP(ip);

            String ua = article.getArticleUA();
            if (StringUtils.length(ua) > CommonUtil.MAX_LENGTH_UA) {
                ua = StringUtils.substring(ua, 0, CommonUtil.MAX_LENGTH_UA);
            }
            article.setArticleUA( ua);

            article.setArticleStick(0L);

            final Option articleCntOption = optionMapper.get(OptionUtil.ID_C_STATISTIC_ARTICLE_COUNT);
            final int articleCnt = Integer.parseInt(articleCntOption.getOptionValue());
            int tmp = articleCnt + 1;
            articleCntOption.setOptionValue(tmp + "");
            optionMapper.update( articleCntOption);

            if (!StringUtils.isBlank(city)) {
                final String cityStatId = city + "-ArticleCount";
                Option cityArticleCntOption = optionMapper.get(cityStatId);

                if (null == cityArticleCntOption) {
                    cityArticleCntOption = new Option();
                    cityArticleCntOption.setOid( cityStatId);
                    cityArticleCntOption.setOptionValue( 1+"");
                    cityArticleCntOption.setOptionCategory( city + "-statistic");

                    optionMapper.add(cityArticleCntOption);
                } else {
                    final int cityArticleCnt = Integer.parseInt(cityArticleCntOption.getOptionValue());
                    int tmp_val = cityArticleCnt + 1;
                    cityArticleCntOption.setOptionValue(tmp_val+ "");

                    optionMapper.update( cityArticleCntOption);
                }
            }

            author.setUserArticleCount(author.getUserArticleCount() + 1);
            author.setUserLatestArticleTime( currentTimeMillis);
            // Updates user article count (and new tag count), latest article time
            userMapper.update(author.getOid(), author);

            final String articleId = articleMapper.add(article);

            if (ArticleUtil.ARTICLE_TYPE_C_THOUGHT != articleType) {
                // RevisionUtil
                final Revision revision = new Revision();
                revision.setRevisionAuthorId(authorId);

                final JSONObject revisionData = new JSONObject();
                revisionData.put(ArticleUtil.ARTICLE_TITLE, articleTitle);
                revisionData.put(ArticleUtil.ARTICLE_CONTENT, articleContent);

                revision.setRevisionData( revisionData.toString());
                revision.setRevisionDataId( articleId);
                revision.setRevisionDataType( RevisionUtil.DATA_TYPE_C_ARTICLE);

                revisionMapper.add(revision);
            }

//            transaction.commit();

            try {
                Thread.sleep(50); // wait for db write to avoid article duplication
            } catch (final Exception e) {
            }

            // Grows the tag graph
            tagMgmtService.relateTags(article.getArticleTags());

            if (!fromClient && ArticleUtil.ARTICLE_ANONYMOUS_C_PUBLIC == articleAnonymous) {
                // Point
                final long followerCnt = followQueryService.getFollowerCount(authorId, FollowUtil.FOLLOWING_TYPE_C_USER);
                final int addition = (int) Math.round(Math.sqrt(followerCnt));

                pointtransferMgmtService.transfer(authorId, PointtransferUtil.ID_C_SYS,
                        PointtransferUtil.TRANSFER_TYPE_C_ADD_ARTICLE,
                        PointtransferUtil.TRANSFER_SUM_C_ADD_ARTICLE + addition, articleId, System.currentTimeMillis());

                if (rewardPoint > 0) { // Enable reward
                    pointtransferMgmtService.transfer(authorId, PointtransferUtil.ID_C_SYS,
                            PointtransferUtil.TRANSFER_TYPE_C_ADD_ARTICLE_REWARD,
                            PointtransferUtil.TRANSFER_SUM_C_ADD_ARTICLE_REWARD, articleId, System.currentTimeMillis());
                }

                if (ArticleUtil.ARTICLE_TYPE_C_CITY_BROADCAST == articleType) {
                    pointtransferMgmtService.transfer(authorId, PointtransferUtil.ID_C_SYS,
                            PointtransferUtil.TRANSFER_TYPE_C_ADD_ARTICLE_BROADCAST,
                            PointtransferUtil.TRANSFER_SUM_C_ADD_ARTICLE_BROADCAST, articleId, System.currentTimeMillis());
                }

                // LivenessUtil
                livenessMgmtService.incLiveness(authorId, LivenessUtil.LIVENESS_ARTICLE);
            }

            // Event
            final JSONObject eventData = new JSONObject();
            eventData.put(Common.FROM_CLIENT, fromClient);
            eventData.put(ArticleUtil.ARTICLE, article);
            try {
                eventManager.fireEventAsynchronously(new AddArticleEvent(EventTypes.ADD_ARTICLE, eventData));
            } catch (final Exception e) {
                LOGGER.error( e.getMessage(), e);
            }

            return ret;
        } catch (final Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }

            LOGGER.error( "Adds an article failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Updates an article with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          "oId": "",
     *                          "articleTitle": "",
     *                          "articleTags": "",
     *                          "articleContent": "",
     *                          "articleEditorType": "",
     *                          "articleCommentable": boolean, // optional, default to true
     *                          "clientArticlePermalink": "", // optional
     *                          "articleType": int // optional, default to 0
     *                          "articleRewardContent": "", // optional, default to ""
     *                          "articleRewardPoint": int, // optional, default to 0
     *                          "articleQnAOfferPoint": int, // optional, default to 0
     *                          "articleIP": "", // optional, default to ""
     *                          "articleUA": "", // optional default to ""
     *                          , see {@link Article} for more details
     * @throws Exception service exception
     */
    public synchronized void updateArticle(final Article requestJSONObject) throws Exception {
        String articleTitle = requestJSONObject.getArticleTitle();

        // TODO: 2018/9/2 判断str何时为真
        final String fromClientStr = requestJSONObject.getClientArticleId();
        boolean fromClient = false;
        if (fromClientStr == "Y"){
            fromClient = true;
        }

        String articleId;
        Article oldArticle;
        String authorId;
        UserExt author;
        int updatePointSum;
        int articleAnonymous = 0;

        try {
            // check if admin allow to add article
            final Option option = optionMapper.get(OptionUtil.ID_C_MISC_ALLOW_ADD_ARTICLE);

            if (!"0".equals(option.getOptionValue())) {
                throw new Exception(langPropsService.get("notAllowAddArticleLabel"));
            }

            articleId = requestJSONObject.getOid();
            oldArticle = articleMapper.get(articleId);
            authorId = oldArticle.getArticleAuthorId();
            author = userMapper.get(authorId);

            final long followerCnt = followQueryService.getFollowerCount(authorId, FollowUtil.FOLLOWING_TYPE_C_USER);
            int addition = (int) Math.round(Math.sqrt(followerCnt));
            final long collectCnt = followQueryService.getFollowerCount(articleId, FollowUtil.FOLLOWING_TYPE_C_ARTICLE);
            final long watchCnt = followQueryService.getFollowerCount(articleId, FollowUtil.FOLLOWING_TYPE_C_ARTICLE_WATCH);
            addition += (collectCnt + watchCnt) * 2;
            updatePointSum = PointtransferUtil.TRANSFER_SUM_C_UPDATE_ARTICLE + addition;

            articleAnonymous = oldArticle.getArticleAnonymous();

            if (!fromClient && ArticleUtil.ARTICLE_ANONYMOUS_C_PUBLIC == articleAnonymous) {
                // Point
                final int balance = author.getUserPoint();
                if (balance - updatePointSum < 0) {
                    throw new Exception(langPropsService.get("insufficientBalanceLabel"));
                }
            }

            final Article maybeExist = articleMapper.getByTitle(articleTitle);
            if (null != maybeExist) {
                if (!oldArticle.getArticleTitle().equals(articleTitle)) {
                    final String existArticleAuthorId = maybeExist.getArticleAuthorId();
                    String msg;
                    if (existArticleAuthorId.equals(authorId)) {
                        msg = langPropsService.get("duplicatedArticleTitleSelfLabel");
                        msg = msg.replace("{article}", "<a target='_blank' href='/article/" + maybeExist.getOid()
                                + "'>" + articleTitle + "</a>");
                    } else {
                        final UserExt existArticleAuthor = userMapper.get(existArticleAuthorId);
                        final String userName = existArticleAuthor.getUserName();
                        msg = langPropsService.get("duplicatedArticleTitleLabel");
                        msg = msg.replace("{user}", "<a target='_blank' href='/member/" + userName + "'>" + userName + "</a>");
                        msg = msg.replace("{article}", "<a target='_blank' href='/article/" + maybeExist.getOid()
                                + "'>" + articleTitle + "</a>");
                    }

                    throw new Exception(msg);
                }
            }
        } catch (final Exception e) {
            throw new Exception(e);
        }

        final int qnaOfferPoint = requestJSONObject.getArticleQnAOfferPoint();//0 todo 
        if (qnaOfferPoint < oldArticle.getArticleQnAOfferPoint()) { // Increase only to prevent lowering points when adopting answer
            throw new Exception(langPropsService.get("qnaOfferPointMustMoreThanOldLabel"));
        }
        oldArticle.setArticleQnAOfferPoint(qnaOfferPoint);

        // TODO: 2018/9/2 Article.ARTICLE_TYPE_C_NORMAL
        final int articleType = requestJSONObject.getArticleType();

//        final Transaction transaction = articleMapper.beginTransaction();

        try {
            requestJSONObject.setArticleAnonymous( articleAnonymous);
            processTagsForArticleUpdate(oldArticle, requestJSONObject, author);
            userMapper.update(author.getOid(), author);

            articleTitle = Emotions.toAliases(articleTitle);
            articleTitle = Pangu.spacingText(articleTitle);

            final String oldTitle = oldArticle.getArticleTitle();
            oldArticle.setArticleTitle( articleTitle);

            oldArticle.setArticleTags( requestJSONObject.getArticleTags());
            // TODO: 2018/9/2 默认true
            oldArticle.setArticleCommentable( requestJSONObject.getArticleCommentable());
            oldArticle.setArticleType( articleType);

            String articleContent = requestJSONObject.getArticleContent();
            articleContent = Emotions.toAliases(articleContent);
            //articleContent = StringUtils.trim(articleContent) + " "; https://github.com/b3log/symphony/issues/389
            articleContent = articleContent.replace(langPropsService.get("uploadingLabel", Locale.SIMPLIFIED_CHINESE), "");
            articleContent = articleContent.replace(langPropsService.get("uploadingLabel", Locale.US), "");

            final String oldContent = oldArticle.getArticleContent();
            oldArticle.setArticleContent( articleContent);

            final long currentTimeMillis = System.currentTimeMillis();
            final long createTime = Long.parseLong(oldArticle.getOid());
            oldArticle.setArticleUpdateTime( currentTimeMillis);

            // TODO: 2018/9/2 默认为 0
            final int rewardPoint = requestJSONObject.getArticleRewardPoint();
            boolean enableReward = false;
            if (0 < rewardPoint) {
                if (1 > oldArticle.getArticleRewardPoint()) {
                    enableReward = true;
                }

                String rewardContent = requestJSONObject.getArticleRewardContent();
                rewardContent = Emotions.toAliases(rewardContent);
                oldArticle.setArticleRewardContent( rewardContent);
                oldArticle.setArticleRewardPoint( rewardPoint);
            }

            final String ip = requestJSONObject.getArticleIP();
            oldArticle.setArticleIP( ip);

            String ua = requestJSONObject.getArticleUA();
            if (StringUtils.length(ua) > CommonUtil.MAX_LENGTH_UA) {
                ua = StringUtils.substring(ua, 0, CommonUtil.MAX_LENGTH_UA);
            }
            oldArticle.setArticleUA( ua);

            final String clientArticlePermalink = requestJSONObject.getClientArticlePermalink();
            oldArticle.setClientArticlePermalink(clientArticlePermalink);

            articleMapper.update( oldArticle);

            if (ArticleUtil.ARTICLE_TYPE_C_THOUGHT != articleType
                    && (!oldContent.equals(articleContent) || !oldTitle.equals(articleTitle))) {
                // RevisionUtil
                final Revision revision = new Revision();
                revision.setRevisionAuthorId(authorId);

                final JSONObject revisionData = new JSONObject();
                revisionData.put(ArticleUtil.ARTICLE_TITLE, articleTitle);
                revisionData.put(ArticleUtil.ARTICLE_CONTENT, articleContent);

                revision.setRevisionData( revisionData.toString());
                revision.setRevisionDataId( articleId);
                revision.setRevisionDataType( RevisionUtil.DATA_TYPE_C_ARTICLE);

                revisionMapper.add(revision);
            }

//            transaction.commit();

            try {
                Thread.sleep(50); // wait for db write to avoid artitle duplication
            } catch (final Exception e) {
            }

            if (!fromClient && ArticleUtil.ARTICLE_ANONYMOUS_C_PUBLIC == articleAnonymous) {
                if (currentTimeMillis - createTime > 1000 * 60 * 5) {
                    pointtransferMgmtService.transfer(authorId, PointtransferUtil.ID_C_SYS,
                            PointtransferUtil.TRANSFER_TYPE_C_UPDATE_ARTICLE,
                            updatePointSum, articleId, System.currentTimeMillis());
                }

                if (enableReward) {
                    pointtransferMgmtService.transfer(authorId, PointtransferUtil.ID_C_SYS,
                            PointtransferUtil.TRANSFER_TYPE_C_ADD_ARTICLE_REWARD,
                            PointtransferUtil.TRANSFER_SUM_C_ADD_ARTICLE_REWARD, articleId, System.currentTimeMillis());
                }
            }

            // Event
            final JSONObject eventData = new JSONObject();
            eventData.put(CommonUtil.FROM_CLIENT, fromClient);
            eventData.put(ArticleUtil.ARTICLE, oldArticle);
            try {
                eventManager.fireEventAsynchronously(new UpdateArticleEvent(EventTypes.UPDATE_ARTICLE, eventData));
            } catch (final Exception e) {
                LOGGER.error( e.getMessage(), e);
            }
        } catch (final Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }

            LOGGER.error( "Updates an article failed", e);
            throw new Exception(e);
        }
    }

    /**
     * A user specified by the given sender id rewards the author of an article specified by the given article id.
     *
     * @param articleId the given article id
     * @param senderId  the given sender id
     * @throws Exception service exception
     */
    public void reward(final String articleId, final String senderId) throws Exception {
        try {
            final Article article = articleMapper.get(articleId);

            if (null == article) {
                return;
            }

            if (ArticleUtil.ARTICLE_STATUS_C_INVALID == article.getArticleStatus()) {
                return;
            }

            final UserExt sender = userMapper.get(senderId);
            if (null == sender) {
                return;
            }

            if (UserExtUtil.USER_STATUS_C_VALID != sender.getUserStatus()) {
                return;
            }

            final String receiverId = article.getArticleAuthorId();
            final UserExt receiver = userMapper.get(receiverId);
            if (null == receiver) {
                return;
            }

            if (UserExtUtil.USER_STATUS_C_VALID != receiver.getUserStatus()) {
                return;
            }

            if (receiverId.equals(senderId)) {
                return;
            }

            final int rewardPoint = article.getArticleRewardPoint();
            if (rewardPoint < 1) {
                return;
            }

            if (rewardQueryService.isRewarded(senderId, articleId, RewardUtil.TYPE_C_ARTICLE)) {
                return;
            }

            final String rewardId = Ids.genTimeMillisId();

            if (ArticleUtil.ARTICLE_ANONYMOUS_C_PUBLIC == article.getArticleAnonymous()) {
                final boolean succ = null != pointtransferMgmtService.transfer(senderId, receiverId,
                        PointtransferUtil.TRANSFER_TYPE_C_ARTICLE_REWARD, rewardPoint, rewardId, System.currentTimeMillis());

                if (!succ) {
                    throw new Exception();
                }
            }

            final Reward reward = new Reward();
            reward.setOid( rewardId);
            reward.setSenderId(senderId);
            reward.setDataId( articleId);
            reward.setType( RewardUtil.TYPE_C_ARTICLE);

            rewardMgmtService.addReward(reward);

            final Notification notification = new Notification();
            notification.setUserId(receiverId);
            notification.setDataId( rewardId);

            notificationMgmtService.addArticleRewardNotification(notification);

            livenessMgmtService.incLiveness(senderId, LivenessUtil.LIVENESS_REWARD);
        } catch (final Exception e) {
            LOGGER.error( "Rewards an article[id=" + articleId + "] failed", e);
            throw new Exception(e);
        }
    }

    /**
     * A user specified by the given sender id thanks the author of an article specified by the given article id.
     *
     * @param articleId the given article id
     * @param senderId  the given sender id
     * @throws Exception service exception
     */
    public void thank(final String articleId, final String senderId) throws Exception {
        try {
            final Article article = articleMapper.get(articleId);

            if (null == article) {
                return;
            }

            if (ArticleUtil.ARTICLE_STATUS_C_INVALID == article.getArticleStatus()) {
                return;
            }

            final UserExt sender = userMapper.get(senderId);
            if (null == sender) {
                return;
            }

            if (UserExtUtil.USER_STATUS_C_VALID != sender.getUserStatus()) {
                return;
            }

            final String receiverId = article.getArticleAuthorId();
            final UserExt receiver = userMapper.get(receiverId);
            if (null == receiver) {
                return;
            }

            if (UserExtUtil.USER_STATUS_C_VALID != receiver.getUserStatus()) {
                return;
            }

            if (receiverId.equals(senderId)) {
                return;
            }

            if (rewardQueryService.isRewarded(senderId, articleId, RewardUtil.TYPE_C_THANK_ARTICLE)) {
                return;
            }

            final String thankId = Ids.genTimeMillisId();

            if (ArticleUtil.ARTICLE_ANONYMOUS_C_PUBLIC == article.getArticleAnonymous()) {
                final boolean succ = null != pointtransferMgmtService.transfer(senderId, receiverId,
                        PointtransferUtil.TRANSFER_TYPE_C_ARTICLE_THANK,
                        PointtransferUtil.TRANSFER_SUM_C_ARTICLE_THANK, thankId, System.currentTimeMillis());

                if (!succ) {
                    throw new Exception();
                }
            }

            final Reward reward = new Reward();
            reward.setOid( thankId);
            reward.setSenderId( senderId);
            reward.setDataId( articleId);
            reward.setType( RewardUtil.TYPE_C_THANK_ARTICLE);

            rewardMgmtService.addReward(reward);

            final JSONObject notification = new JSONObject();
            notification.put(NotificationUtil.NOTIFICATION_USER_ID, receiverId);
            notification.put(NotificationUtil.NOTIFICATION_DATA_ID, thankId);

            notificationMgmtService.addArticleThankNotification(notification);

            livenessMgmtService.incLiveness(senderId, LivenessUtil.LIVENESS_REWARD);
        } catch (final Exception e) {
            LOGGER.error( "Thanks an article[id=" + articleId + "] failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Sticks an article specified by the given article id.
     *
     * @param articleId the given article id
     * @throws Exception service exception
     */
    @Transactional
    public synchronized void stick(final String articleId) throws Exception {
//        final Transaction transaction = articleMapper.beginTransaction();

        try {
            final Article article = articleMapper.get(articleId);
            if (null == article) {
                return;
            }

            final String authorId = article.getArticleAuthorId();
            final UserExt author = userMapper.get(authorId);
            final int balance = author.getUserPoint();

            if (balance - PointtransferUtil.TRANSFER_SUM_C_STICK_ARTICLE < 0) {
                throw new Exception(langPropsService.get("insufficientBalanceLabel"));
            }

//            final Query query = new Query().
//                    setFilter(new PropertyFilter(ArticleUtil.ARTICLE_STICK, FilterOperator.GREATER_THAN, 0L));
            final List<Article> articles = articleMapper.getByArticleStick(0L);
            if (articles.size() > 1) {
                final Set<String> ids = new HashSet<>();
                for (int i = 0; i < articles.size(); i++) {
                    ids.add(articles.get(i).getOid());
                }

                if (!ids.contains(articleId)) {
                    throw new Exception(langPropsService.get("stickExistLabel"));
                }
            }

            article.setArticleStick( System.currentTimeMillis());

            articleMapper.update( article);

//            transaction.commit();

            final boolean succ = null != pointtransferMgmtService.transfer(article.getArticleAuthorId(),
                    PointtransferUtil.ID_C_SYS, PointtransferUtil.TRANSFER_TYPE_C_STICK_ARTICLE,
                    PointtransferUtil.TRANSFER_SUM_C_STICK_ARTICLE, articleId, System.currentTimeMillis());
            if (!succ) {
                throw new Exception(langPropsService.get("stickFailedLabel"));
            }
        } catch (final Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }

            LOGGER.error( "Sticks an article[id=" + articleId + "] failed", e);

            throw new Exception(langPropsService.get("stickFailedLabel"));
        }
    }

    /**
     * Admin sticks an article specified by the given article id.
     *
     * @param articleId the given article id
     * @throws Exception service exception
     */
    @Transactional
    public synchronized void adminStick(final String articleId) throws Exception {
        try {
            final Article article = articleMapper.get(articleId);
            if (null == article) {
                return;
            }

            article.setArticleStick( Long.MAX_VALUE);

            articleMapper.update( article);
        } catch (final Exception e) {
            LOGGER.error( "Admin sticks an article[id=" + articleId + "] failed", e);

            throw new Exception(langPropsService.get("stickFailedLabel"));
        }
    }

    /**
     * Updates the specified article by the given article id.
     * <p>
     * <b>Note</b>: This method just for admin console.
     * </p>
     *
     * @param articleId the given article id
     * @param article   the specified article
     * @throws Exception service exception
     */
    @Transactional
    public void updateArticleByAdmin(final String articleId, final Article article) throws Exception {
//        final Transaction transaction = articleMapper.beginTransaction();

        try {
            final String authorId = article.getArticleAuthorId();
            final UserExt author = userMapper.get(authorId);

            // TODO: 2018/10/14 str bool 转换
            article.setArticleCommentable( String.valueOf(Boolean.valueOf(article.getArticleCommentable())));
            article.setSyncWithSymphonyClient( author.getSyncWithSymphonyClient());

            final Article oldArticle = articleMapper.get(articleId);

            if (ArticleUtil.ARTICLE_STATUS_C_INVALID == article.getArticleStatus()) {
                article.setArticleTags( "回收站");
            }

            processTagsForArticleUpdate(oldArticle, article, author);

            String articleTitle = article.getArticleTitle();
            articleTitle = Emotions.toAliases(articleTitle);
            article.setArticleTitle(articleTitle);

            if (ArticleUtil.ARTICLE_TYPE_C_THOUGHT == article.getArticleType()) {
                article.setArticleContent( oldArticle.getArticleContent());
            } else {
                String articleContent = article.getArticleContent();
                articleContent = Emotions.toAliases(articleContent);
                article.setArticleContent( articleContent);
            }

            final int perfect = article.getArticlePerfect();
            if (ArticleUtil.ARTICLE_PERFECT_C_PERFECT == perfect) {
                // if it is perfect, allow anonymous view
                article.setArticleAnonymousView( ArticleUtil.ARTICLE_ANONYMOUS_VIEW_C_ALLOW);

                // updates tag-article perfect
                final List<TagArticle> tagArticleRels = tagArticleMapper.getByArticleId(articleId);
                for (final TagArticle tagArticleRel : tagArticleRels) {
                    tagArticleRel.setArticlePerfect( ArticleUtil.ARTICLE_PERFECT_C_PERFECT);

                    tagArticleMapper.update(tagArticleRel.getOid(), tagArticleRel);
                }
            }

            userMapper.update(authorId, author);
            articleMapper.update( article);

//            transaction.commit();

            if (ArticleUtil.ARTICLE_PERFECT_C_NOT_PERFECT == oldArticle.getArticlePerfect()
                    && ArticleUtil.ARTICLE_PERFECT_C_PERFECT == perfect) {
                final Notification notification = new Notification();
                notification.setUserId( authorId);
                notification.setDataId( articleId);

                notificationMgmtService.addPerfectArticleNotification(notification);

                pointtransferMgmtService.transfer(PointtransferUtil.ID_C_SYS, authorId,
                        PointtransferUtil.TRANSFER_TYPE_C_PERFECT_ARTICLE, PointtransferUtil.TRANSFER_SUM_C_PERFECT_ARTICLE,
                        articleId, System.currentTimeMillis());
            }

            if (ArticleUtil.ARTICLE_STATUS_C_INVALID == article.getArticleStatus()) {
                if (Symphonys.getBoolean("algolia.enabled")) {
                    searchMgmtService.removeAlgoliaDocument(article);
                }

                if (Symphonys.getBoolean("es.enabled")) {
                    searchMgmtService.removeESDocument(article, ArticleUtil.ARTICLE);
                }
            }
        } catch (final Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }

            LOGGER.error( "Updates an article[id=" + articleId + "] failed", e);
            throw new Exception(e);
        }
    }

    /**
     * Admin cancels stick an article specified by the given article id.
     *
     * @param articleId the given article id
     * @throws Exception service exception
     */
    @Transactional
    public synchronized void adminCancelStick(final String articleId) throws Exception {
        try {
            final Article article = articleMapper.get(articleId);
            if (null == article) {
                return;
            }

            article.setArticleStick( 0L);

            articleMapper.update( article);
        } catch (final Exception e) {
            LOGGER.error( "Admin cancel sticks an article[id=" + articleId + "] failed", e);

            throw new Exception(langPropsService.get("operationFailedLabel"));
        }
    }

    /**
     * Expires sticked articles.
     *
     * @throws Exception service exception
     */
    @Transactional
    public void expireStick() throws Exception {
        try {
//            final Query query = new Query().
//                    setFilter(new PropertyFilter(Article.ARTICLE_STICK, FilterOperator.GREATER_THAN, 0L));
            final List<Article> articles = articleMapper.getByArticleStick(0L);
            if (articles.size() < 1) {
                return;
            }

            final long stepTime = Symphonys.getLong("stickArticleTime");
            final long now = System.currentTimeMillis();

            for (int i = 0; i < articles.size(); i++) {
                final Article article = articles.get(i);
                final long stick = article.getArticleStick();
                if (stick >= Long.MAX_VALUE) {
                    continue; // Skip admin stick
                }

                final long expired = stick + stepTime;

                if (expired < now) {
                    article.setArticleStick( 0L);
                    articleMapper.update( article);
                }
            }
        } catch (final Exception e) {
            LOGGER.error( "Expires sticked articles failed", e);

            throw new Exception();
        }
    }

    /**
     * Processes tags for article update.
     * <p>
     * <ul>
     * <li>Un-tags old article, decrements tag reference count</li>
     * <li>Removes old article-tag relations</li>
     * <li>Saves new article-tag relations with tag reference count</li>
     * </ul>
     * </p>
     *
     * @param oldArticle the specified old article
     * @param newArticle the specified new article
     * @param author     the specified author
     * @throws Exception exception
     */
    private synchronized void processTagsForArticleUpdate(final Article oldArticle, final Article newArticle,
                                                          final UserExt author) throws Exception {
        final String oldArticleId = oldArticle.getOid();
        final List<Tag> oldTags = tagMapper.getByArticleId(oldArticleId);
        String tagsString = newArticle.getArticleTags();
        tagsString = TagUtil.formatTags(tagsString);
        boolean sandboxEnv = false;
        if (StringUtils.containsIgnoreCase(tagsString, TagUtil.TAG_TITLE_C_SANDBOX)) {
            tagsString = TagUtil.TAG_TITLE_C_SANDBOX;
            sandboxEnv = true;
        }

        String[] tagStrings = tagsString.split(",");
        final int articleType = newArticle.getArticleType();
        if (!sandboxEnv && tagStrings.length < TAG_MAX_CNT && tagStrings.length < 3
                && ArticleUtil.ARTICLE_TYPE_C_DISCUSSION != articleType
                && ArticleUtil.ARTICLE_TYPE_C_THOUGHT != articleType && !TagUtil.containsReservedTags(tagsString)) {
            final String content = newArticle.getArticleTitle()
                    + " " + Jsoup.parse("<p>" + newArticle.getArticleContent() + "</p>").text();
            final List<String> genTags = tagQueryService.generateTags(content, 1);
            if (!genTags.isEmpty()) {
                tagsString = tagsString + "," + StringUtils.join(genTags, ",");
                tagsString = TagUtil.formatTags(tagsString);
                tagsString = TagUtil.useHead(tagsString, TAG_MAX_CNT);
            }
        }

        if (StringUtils.isBlank(tagsString)) {
            tagsString = "B3log";
        }

        tagsString = TagUtil.formatTags(tagsString);
        if (ArticleUtil.ARTICLE_TYPE_C_QNA == articleType && !StringUtils.contains(tagsString, "Q&A")) {
            tagsString += ",Q&A";
        }
        newArticle.setArticleTags( tagsString);
        tagStrings = tagsString.split(",");

        final List<Tag> newTags = new ArrayList<>();

        for (final String tagString : tagStrings) {
            final String tagTitle = tagString.trim();
            Tag newTag = tagMapper.getByTitle(tagTitle);
            if (null == newTag) {
                newTag = new Tag();
                newTag.setTagTitle( tagTitle);
            }

            newTags.add(newTag);
        }

        final List<Tag> tagsDropped = new ArrayList<>();
        final List<Tag> tagsNeedToAdd = new ArrayList<>();

        for (final Tag newTag : newTags) {
            final String newTagTitle = newTag.getTagTitle();

            if (!tagExists(newTagTitle, oldTags)) {
                LOGGER.debug("Tag need to add[title={0}]", newTagTitle);
                tagsNeedToAdd.add(newTag);
            }
        }
        for (final Tag oldTag : oldTags) {
            final String oldTagTitle = oldTag.getTagTitle();

            if (!tagExists(oldTagTitle, newTags)) {
                LOGGER.debug("Tag dropped[title={0}]", oldTag);
                tagsDropped.add(oldTag);
            }
        }

        final int articleCmtCnt = oldArticle.getArticleCommentCount();

        for (final Tag tagDropped : tagsDropped) {
            final String tagId = tagDropped.getOid();
            int refCnt = tagDropped.getTagReferenceCount() - 1;
            refCnt = refCnt < 0 ? 0 : refCnt;
            tagDropped.setTagReferenceCount( refCnt);
            final int tagCmtCnt = tagDropped.getTagCommentCount();
            tagDropped.setTagCommentCount( tagCmtCnt - articleCmtCnt);
            tagDropped.setTagRandomDouble( Math.random());

            tagMapper.update(tagId, tagDropped);
        }

        final String[] tagIdsDropped = new String[tagsDropped.size()];

        for (int i = 0; i < tagIdsDropped.length; i++) {
            final Tag tag = tagsDropped.get(i);
            final String id = tag.getOid();

            tagIdsDropped[i] = id;
        }

        if (0 != tagIdsDropped.length) {
            removeTagArticleRelations(oldArticleId, tagIdsDropped);
            removeUserTagRelations(oldArticle.getArticleAuthorId(), TagUtil.TAG_TYPE_C_ARTICLE, tagIdsDropped);
        }

        tagStrings = new String[tagsNeedToAdd.size()];
        for (int i = 0; i < tagStrings.length; i++) {
            final Tag tag = tagsNeedToAdd.get(i);
            final String tagTitle = tag.getTagTitle();

            tagStrings[i] = tagTitle;
        }

        newArticle.setArticleCommentCount( articleCmtCnt);
        tag(tagStrings, newArticle, author);
    }

    /**
     * Removes tag-article relations by the specified article id and tag ids of the relations to be removed.
     * <p>
     * Removes all relations if not specified the tag ids.
     * </p>
     *
     * @param articleId the specified article id
     * @param tagIds    the specified tag ids of the relations to be removed
     * @throws JSONException       json exception
     * @throws Exception Mapper exception
     */
    private void removeTagArticleRelations(final String articleId, final String... tagIds)
            throws JSONException, Exception {
        final List<String> tagIdList = Arrays.asList(tagIds);
        final List<TagArticle> tagArticleRelations = tagArticleMapper.getByArticleId(articleId);

        for (int i = 0; i < tagArticleRelations.size(); i++) {
            final TagArticle tagArticleRelation = tagArticleRelations.get(i);
            String relationId;

            if (tagIdList.isEmpty()) { // Removes all if un-specified
                relationId = tagArticleRelation.getOid();
                tagArticleMapper.remove(relationId);
            } else if (tagIdList.contains(tagArticleRelation.getTag_oid())) {
                relationId = tagArticleRelation.getOid();
                tagArticleMapper.remove(relationId);
            }
        }
    }

    /**
     * Removes User-Tag relations by the specified user id, type and tag ids of the relations to be removed.
     *
     * @param userId the specified article id
     * @param type   the specified type
     * @param tagIds the specified tag ids of the relations to be removed
     * @throws Exception Mapper exception
     */
    private void removeUserTagRelations(final String userId, final int type, final String... tagIds) throws Exception {
        for (final String tagId : tagIds) {
            userTagMapper.removeByUserIdAndTagId(userId, tagId, type);
        }
    }

    /**
     * Tags the specified article with the specified tag titles.
     *
     * @param tagTitles the specified (new) tag titles
     * @param article   the specified article
     * @param author    the specified author
     * @throws Exception Mapper exception
     */
    private synchronized void tag(final String[] tagTitles, final Article article, final UserExt author)
            throws Exception {
        String articleTags = article.getArticleTags();

        for (final String t : tagTitles) {
            final String tagTitle = t.trim();
            Tag tag = tagMapper.getByTitle(tagTitle);
            String tagId;
            int userTagType;
            final int articleCmtCnt = article.getArticleCommentCount();
            if (null == tag) {
                LOGGER.trace( "Found a new tag [title={0}] in article [title={1}]",
                        tagTitle, article.getArticleTitle());
                tag = new Tag();
                tag.setTagTitle( tagTitle);
                String tagURI = tagTitle;
                tagURI = URLs.encode(tagTitle);
                tag.setTagURI( tagURI);
                tag.setTagCSS( "");
                tag.setTagReferenceCount( 1);
                tag.setTagCommentCount( articleCmtCnt);
                tag.setTagFollowerCount( 0);
                tag.setTagLinkCount( 0);
                tag.setTagDescription( "");
                tag.setTagIconPath( "");
                tag.setTagStatus(0);
                tag.setTagGoodCnt( 0);
                tag.setTagBadCnt( 0);
                tag.setTagSeoTitle( tagTitle);
                tag.setTagSeoKeywords( tagTitle);
                tag.setTagSeoDesc( "");
                tag.setTagRandomDouble( Math.random());

                tagId = tagMapper.add(tag);
                tag.setOid( tagId);
                userTagType = TagUtil.TAG_TYPE_C_CREATOR;

                final Option tagCntOption = optionMapper.get(OptionUtil.ID_C_STATISTIC_TAG_COUNT);
                final int tagCnt = Integer.parseInt(tagCntOption.getOptionValue());
                int tmp = tagCnt + 1;
                tagCntOption.setOptionValue(tmp + "");
                optionMapper.update(OptionUtil.ID_C_STATISTIC_TAG_COUNT, tagCntOption);

                author.setUserTagCount(author.getUserTagCount() + 1);
            } else {
                tagId = tag.getOid();
                LOGGER.trace( "Found a existing tag[title={0}, id={1}] in article[title={2}]",
                        tag.getTagTitle(), tag.getOid(), article.getArticleTitle());
//                final Tag tagTmp = new Tag();
//                tagTmp.setOid(Keys.OBJECT_ID, tagId);
                final String title = tag.getTagTitle();

//                tagTmp.setTagTitle(Tag.TAG_TITLE, title);
                tag.setTagCommentCount( tag.getTagCommentCount() + articleCmtCnt);
//                tagTmp.setTagStatus(Tag.TAG_STATUS, tag.optInt(Tag.TAG_STATUS));
                tag.setTagReferenceCount( tag.getTagReferenceCount() + 1);
//                tagTmp.setTagFollowerCount(Tag.TAG_FOLLOWER_CNT, tag.optInt(Tag.TAG_FOLLOWER_CNT));
//                tagTmp.setTagLinkCount(Tag.TAG_LINK_CNT, tag.optInt(Tag.TAG_LINK_CNT));
//                tagTmp.setTagDescription(Tag.TAG_DESCRIPTION, tag.optString(Tag.TAG_DESCRIPTION));
//                tagTmp.setTagIconPath(Tag.TAG_ICON_PATH, tag.optString(Tag.TAG_ICON_PATH));
//                tagTmp.setTagGoodCnt(Tag.TAG_GOOD_CNT, tag.optInt(Tag.TAG_GOOD_CNT));
//                tagTmp.setTagBadCnt(Tag.TAG_BAD_CNT, tag.optInt(Tag.TAG_BAD_CNT));
//                tagTmp.setTagSeoDesc(Tag.TAG_SEO_DESC, tag.optString(Tag.TAG_SEO_DESC));
//                tagTmp.setTagSeoKeywords(Tag.TAG_SEO_KEYWORDS, tag.optString(Tag.TAG_SEO_KEYWORDS));
//                tagTmp.setTagSeoTitle(Tag.TAG_SEO_TITLE, tag.optString(Tag.TAG_SEO_TITLE));
                tag.setTagRandomDouble( Math.random());
//                tagTmp.setTagURI( tag.optString(Tag.TAG_URI));
//                tagTmp.setTagCSS( tag.optString(Tag.TAG_CSS));

                tagMapper.update(tagId, tag);

                userTagType = TagUtil.TAG_TYPE_C_ARTICLE;
            }

            // Tag-Article relation
            final TagArticle tagArticleRelation = new TagArticle();
            tagArticleRelation.setTag_oid(tagId);
            tagArticleRelation.setArticle_oid(article.getOid());
            tagArticleRelation.setArticleLatestCmtTime( article.getArticleLatestCmtTime());
            tagArticleRelation.setArticleCommentCount(article.getArticleCommentCount());
            tagArticleRelation.setRedditScore( article.getRedditScore( ));//0D
            tagArticleRelation.setArticlePerfect(article.getArticlePerfect());
            tagArticleMapper.add(tagArticleRelation);

            final String authorId = article.getArticleAuthorId();

            // User-Tag relation
            if (TagUtil.TAG_TYPE_C_ARTICLE == userTagType) {
                userTagMapper.removeByUserIdAndTagId(authorId, tagId, TagUtil.TAG_TYPE_C_ARTICLE);
            }

            final UserTag userTagRelation = new UserTag();
            userTagRelation.setTag_oId( tagId);
            if (ArticleUtil.ARTICLE_ANONYMOUS_C_ANONYMOUS == article.getArticleAnonymous()) {
                userTagRelation.setUser_oId("0");
            } else {
                userTagRelation.setUser_oId(authorId);
            }
            userTagRelation.setType( userTagType);
            userTagMapper.add(userTagRelation);
        }

        final String[] tags = articleTags.split(",");
        final StringBuilder builder = new StringBuilder();
        for (final String tagTitle : tags) {
            final Tag tag = tagMapper.getByTitle(tagTitle);

            builder.append(tag.getTagTitle()).append(",");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }

        article.setArticleTags(builder.toString());
    }

    /**
     * Filters the specified article tags.
     *
     * @param articleTags the specified article tags
     * @return filtered tags string
     */
    public String filterReservedTags(final String articleTags) {
        final String[] tags = articleTags.split(",");

        final StringBuilder retBuilder = new StringBuilder();

        for (final String tag : tags) {
            if (!ArrayUtils.contains(Symphonys.RESERVED_TAGS, tag)) {
                retBuilder.append(tag).append(",");
            }
        }
        if (retBuilder.length() > 0) {
            retBuilder.deleteCharAt(retBuilder.length() - 1);
        }

        return retBuilder.toString();
    }

    /**
     * Adds an article with the specified request json object.
     * <p>
     * <b>Note</b>: This method just for admin console.
     * </p>
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          "articleTitle": "",
     *                          "articleTags": "",
     *                          "articleContent": "",
     *                          "articleRewardContent": "",
     *                          "articleRewardPoint": int,
     *                          "userName": "",
     *                          "time": long
     *                          , see {@link Article} for more details
     * @return generated article id
     * @throws Exception service exception
     */
    @Transactional
    public synchronized String addArticleByAdmin(final JSONObject requestJSONObject) throws Exception {
        UserExt author;

        try {
            author = userMapper.getByName(requestJSONObject.optString(User.USER_NAME));
            if (null == author) {
                throw new Exception(langPropsService.get("notFoundUserLabel"));
            }
        } catch (final Exception e) {
            LOGGER.debug( "Admin adds article failed", e);

            throw new Exception(e.getMessage());
        }

//        final Transaction transaction = articleMapper.beginTransaction();

        try {
            final long time = requestJSONObject.optLong(CommonUtil.TIME);
            final String ret = String.valueOf(time);
            final Article article = new Article();
            article.setOid(ret);
            article.setClientArticleId(ret);
            article.setClientArticlePermalink( "");
            article.setArticleAuthorId(author.getOid());
            article.setArticleTitle( Emotions.toAliases(requestJSONObject.optString(ArticleUtil.ARTICLE_TITLE)));
            article.setArticleContent(Emotions.toAliases(requestJSONObject.optString(ArticleUtil.ARTICLE_CONTENT)));
            article.setArticleRewardContent( requestJSONObject.optString(ArticleUtil.ARTICLE_REWARD_CONTENT));
            article.setArticleEditorType(0);
            article.setSyncWithSymphonyClient(String.valueOf(false));
            article.setArticleCommentCount(0);
            article.setArticleViewCount( 0);
            article.setArticleGoodCnt( 0);
            article.setArticleBadCnt( 0);
            article.setArticleCollectCnt( 0);
            article.setArticleWatchCnt(0);
            article.setArticleCommentable(String.valueOf(true));
            article.setArticleCreateTime( time);
            article.setArticleUpdateTime(time);
            article.setArticleLatestCmtTime( 0L);
            article.setArticleLatestCmterName( "");
            article.setArticlePermalink( "/article/" + ret);
            article.setArticleRandomDouble( Math.random());
            article.setRedditScore( 0D);
            article.setArticleStatus( ArticleUtil.ARTICLE_STATUS_C_VALID);
            article.setArticleType(ArticleUtil.ARTICLE_TYPE_C_NORMAL);
            article.setArticleRewardPoint( requestJSONObject.optInt(ArticleUtil.ARTICLE_REWARD_POINT));
            article.setArticleQnAOfferPoint( 0);
            article.setArticlePushOrder( 0);
            article.setArticleCity( "");
            String articleTags = requestJSONObject.optString(ArticleUtil.ARTICLE_TAGS);
            articleTags = TagUtil.formatTags(articleTags);
            boolean sandboxEnv = false;
            if (StringUtils.containsIgnoreCase(articleTags, TagUtil.TAG_TITLE_C_SANDBOX)) {
                articleTags = TagUtil.TAG_TITLE_C_SANDBOX;
                sandboxEnv = true;
            }

            String[] tagTitles = articleTags.split(",");
            if (!sandboxEnv && tagTitles.length < TAG_MAX_CNT && tagTitles.length < 3
                    && !TagUtil.containsReservedTags(articleTags)) {
                final String content = article.getArticleTitle()
                        + " " + Jsoup.parse("<p>" + article.getArticleContent() + "</p>").text();
                final List<String> genTags = tagQueryService.generateTags(content, TAG_MAX_CNT);
                if (!genTags.isEmpty()) {
                    articleTags = articleTags + "," + StringUtils.join(genTags, ",");
                    articleTags = TagUtil.formatTags(articleTags);
                    articleTags = TagUtil.useHead(articleTags, TAG_MAX_CNT);
                }
            }

            if (StringUtils.isBlank(articleTags)) {
                articleTags = "B3log";
            }

            articleTags = TagUtil.formatTags(articleTags);
            article.setArticleTags( articleTags);
            tagTitles = articleTags.split(",");

            tag(tagTitles, article, author);

            final String ip = requestJSONObject.optString(ArticleUtil.ARTICLE_IP);
            article.setArticleIP(ip);

            String ua = requestJSONObject.optString(ArticleUtil.ARTICLE_UA);
            if (StringUtils.length(ua) > CommonUtil.MAX_LENGTH_UA) {
                ua = StringUtils.substring(ua, 0, CommonUtil.MAX_LENGTH_UA);
            }
            article.setArticleUA( ua);

            article.setArticleStick(0L);
            article.setArticleAnonymous( ArticleUtil.ARTICLE_ANONYMOUS_C_PUBLIC);
            article.setArticlePerfect( ArticleUtil.ARTICLE_PERFECT_C_NOT_PERFECT);
            article.setArticleAnonymousView(ArticleUtil.ARTICLE_ANONYMOUS_VIEW_C_USE_GLOBAL);
            article.setArticleAudioURL( "");

            final Option articleCntOption = optionMapper.get(OptionUtil.ID_C_STATISTIC_ARTICLE_COUNT);
            final int articleCnt = Integer.parseInt(articleCntOption.getOptionValue());
            int tmp = articleCnt + 1;
            articleCntOption.setOptionValue(tmp + "");
            optionMapper.update(OptionUtil.ID_C_STATISTIC_ARTICLE_COUNT, articleCntOption);

            author.setUserArticleCount( author.getUserArticleCount() + 1);
            author.setUserLatestArticleTime(time);
            // Updates user article count (and new tag count), latest article time
            userMapper.update(author.getOid(), author);

            final String articleId = articleMapper.add(article);

            // RevisionUtil
            final Revision revision = new Revision();
            revision.setRevisionAuthorId(author.getOid());
            final JSONObject revisionData = new JSONObject();
            revisionData.put(ArticleUtil.ARTICLE_TITLE, article.getArticleTitle());
            revisionData.put(ArticleUtil.ARTICLE_CONTENT, article.getArticleContent());
            revision.setRevisionData( revisionData.toString());
            revision.setRevisionDataId( articleId);
            revision.setRevisionDataType( RevisionUtil.DATA_TYPE_C_ARTICLE);

            revisionMapper.add(revision);

//            transaction.commit();

            // Grows the tag graph
            tagMgmtService.relateTags(article.getArticleTags());

            // Event
            final JSONObject eventData = new JSONObject();
            eventData.put(CommonUtil.FROM_CLIENT, false);
            eventData.put(ArticleUtil.ARTICLE, article);
            try {
                eventManager.fireEventAsynchronously(new Event<>(EventTypes.ADD_ARTICLE, eventData));
            } catch (final Exception e) {
                LOGGER.error( e.getMessage(), e);
            }

            return ret;
        } catch (final Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }

            LOGGER.error( "Admin adds an article failed", e);
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Saves markdown file for the specified article.
     *
     * @param article the specified article
     */
    public void saveMarkdown(final JSONObject article) {
        if (ArticleUtil.ARTICLE_TYPE_C_THOUGHT == article.optInt(ArticleUtil.ARTICLE_TYPE)
                || ArticleUtil.ARTICLE_TYPE_C_DISCUSSION == article.optInt(ArticleUtil.ARTICLE_TYPE)) {
            return;
        }

        final String dir = Symphonys.get("ipfs.dir");
        if (StringUtils.isBlank(dir)) {
            return;
        }

        final Path dirPath = Paths.get(dir);
        try {
            FileUtils.forceMkdir(dirPath.toFile());
        } catch (final Exception e) {
            LOGGER.error( "Creates dir [" + dirPath.toString() + "] for save markdown files failed", e);

            return;
        }

        final String id = article.optString(Keys.OBJECT_ID);
        final String authorName = article.optJSONObject(ArticleUtil.ARTICLE_T_AUTHOR).optString(User.USER_NAME);
        final Path mdPath = Paths.get(dir, "hacpai", authorName, id + ".md");
        try {
            if (mdPath.toFile().exists()) {
                final FileTime lastModifiedTime = Files.getLastModifiedTime(mdPath);
                if (lastModifiedTime.toMillis() + 1000 * 60 * 60 >= System.currentTimeMillis()) {
                    return;
                }
            }
        } catch (final Exception e) {
            LOGGER.error( "Gets last modified time of file [" + mdPath.toString() + "] failed", e);

            return;
        }

        try {
            final Map<String, Object> hexoFront = new LinkedHashMap<>();
            hexoFront.put("title", article.optString(ArticleUtil.ARTICLE_TITLE));
            hexoFront.put("date", DateFormatUtils.format((Date) article.opt(ArticleUtil.ARTICLE_CREATE_TIME), "yyyy-MM-dd HH:mm:ss"));
            hexoFront.put("updated", DateFormatUtils.format((Date) article.opt(ArticleUtil.ARTICLE_UPDATE_TIME), "yyyy-MM-dd HH:mm:ss"));
            final List<String> tags = Arrays.stream(article.optString(ArticleUtil.ARTICLE_TAGS).split(",")).
                    filter(StringUtils::isNotBlank).map(String::trim).collect(Collectors.toList());
            if (tags.isEmpty()) {
                tags.add("Sym");
            }
            hexoFront.put("tags", tags);

            final String text = new Yaml().dump(hexoFront).replaceAll("\n", Strings.LINE_SEPARATOR) + "---" + Strings.LINE_SEPARATOR + article.optString(ArticleUtil.ARTICLE_T_ORIGINAL_CONTENT);
            FileUtils.writeStringToFile(new File(mdPath.toString()), text, "UTF-8");
        } catch (final Exception e) {
            LOGGER.error( "Writes article to markdown file [" + mdPath.toString() + "] failed", e);
        }
    }
}
