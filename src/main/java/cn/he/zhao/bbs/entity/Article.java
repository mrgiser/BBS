package cn.he.zhao.bbs.entity;

/**
 * This class defines all article entity relevant keys.
 *
 */
public class Article {

    private String oid;

    /**
     * Key of article title.
     */
    private String articleTitle;

    /**
     * Key of article tags.
     */
    private String articleTags;

    /**
     * Key of article author id.
     */
    private String articleAuthorId;

    /**
     * Key of article comment count.
     */
    private Integer articleCommentCount;

    /**
     * Key of article view count.
     */
    private Integer articleViewCount;

    /**
     * Key of article content.
     */
    private String articleContent;

    /**
     * Key of article reward content.
     */
    private String articleRewardContent;

    /**
     * Key of article reward point.
     */
    private Integer articleRewardPoint;

    /**
     * Key of article permalink.
     */
    private String articlePermalink;

    /**
     * Key of article create time.
     */
    private Long articleCreateTime;


    /**
     * Key of article update time.
     */
    private Long articleUpdateTime;

    /**
     * Key of article latest comment time.
     */
    private Long articleLatestCmtTime;

    /**
     * Key of article latest commenter name.
     */
    private String articleLatestCmterName;

    /**
     * Key of article random double value.
     */
    private Double articleRandomDouble;

    /**
     * Key of article commentable.
     */
    private String articleCommentable;

    /**
     * Key of article sync to client.
     */
    private String syncWithSymphonyClient;

    /**
     * Key of client article id.
     */
    private String clientArticleId;

    /**
     * Key of client article permalink.
     */
    private String clientArticlePermalink;

    /**
     * Key of article editor type.
     */
    private Integer articleEditorType;

    /**
     * Key of article status.
     */
    private Integer articleStatus;

    /**
     * Key of article type.
     */
    private Integer articleType;

    /**
     * Key of article good count.
     */
    private Integer articleGoodCnt;

    /**
     * Key of article bad count.
     */
    private Integer articleBadCnt;

    /**
     * Key of article collection count.
     */
    private Integer articleCollectCnt;

    /**
     * Key of article watch count.
     */
    private Integer articleWatchCnt;

    /**
     * Key of reddit score.
     */
    private Double redditScore;

    /**
     * Key of article city.
     */
    private String articleCity;

    /**
     * Key of article IP.
     */
    private String articleIP;

    /**
     * Key of article UA.
     */
    private String articleUA;

    /**
     * Key of article stick.
     */
    private Long articleStick;

    /**
     * Key of article anonymous.
     */
    private Integer articleAnonymous;

    /**
     * Key of article perfect.
     */
    private Integer articlePerfect;

    /**
     * Key of article anonymous view.
     */
    private Integer articleAnonymousView;

    /**
     * Key of article audio URL.
     */
    private String articleAudioURL;

    /**
     * Key of article qna offer point. https://github.com/b3log/symphony/issues/486
     */
    private Integer articleQnAOfferPoint;

    /**
     * Key of article push order. https://github.com/b3log/symphony/issues/537
     */
    private Integer articlePushOrder;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public String getArticleTags() {
        return articleTags;
    }

    public void setArticleTags(String articleTags) {
        this.articleTags = articleTags;
    }

    public String getArticleAuthorId() {
        return articleAuthorId;
    }

    public void setArticleAuthorId(String articleAuthorId) {
        this.articleAuthorId = articleAuthorId;
    }

    public Integer getArticleCommentCount() {
        return articleCommentCount;
    }

    public void setArticleCommentCount(Integer articleCommentCount) {
        this.articleCommentCount = articleCommentCount;
    }

    public Integer getArticleViewCount() {
        return articleViewCount;
    }

    public void setArticleViewCount(Integer articleViewCount) {
        this.articleViewCount = articleViewCount;
    }

    public String getArticleContent() {
        return articleContent;
    }

    public void setArticleContent(String articleContent) {
        this.articleContent = articleContent;
    }

    public String getArticleRewardContent() {
        return articleRewardContent;
    }

    public void setArticleRewardContent(String articleRewardContent) {
        this.articleRewardContent = articleRewardContent;
    }

    public Integer getArticleRewardPoint() {
        return articleRewardPoint;
    }

    public void setArticleRewardPoint(Integer articleRewardPoint) {
        this.articleRewardPoint = articleRewardPoint;
    }

    public String getArticlePermalink() {
        return articlePermalink;
    }

    public void setArticlePermalink(String articlePermalink) {
        this.articlePermalink = articlePermalink;
    }

    public Long getArticleCreateTime() {
        return articleCreateTime;
    }

    public void setArticleCreateTime(Long articleCreateTime) {
        this.articleCreateTime = articleCreateTime;
    }

    public Long getArticleUpdateTime() {
        return articleUpdateTime;
    }

    public void setArticleUpdateTime(Long articleUpdateTime) {
        this.articleUpdateTime = articleUpdateTime;
    }

    public Long getArticleLatestCmtTime() {
        return articleLatestCmtTime;
    }

    public void setArticleLatestCmtTime(Long articleLatestCmtTime) {
        this.articleLatestCmtTime = articleLatestCmtTime;
    }

    public String getArticleLatestCmterName() {
        return articleLatestCmterName;
    }

    public void setArticleLatestCmterName(String articleLatestCmterName) {
        this.articleLatestCmterName = articleLatestCmterName;
    }

    public Double getArticleRandomDouble() {
        return articleRandomDouble;
    }

    public void setArticleRandomDouble(Double articleRandomDouble) {
        this.articleRandomDouble = articleRandomDouble;
    }

    public String getArticleCommentable() {
        return articleCommentable;
    }

    public void setArticleCommentable(String articleCommentable) {
        this.articleCommentable = articleCommentable;
    }

    public String getSyncWithSymphonyClient() {
        return syncWithSymphonyClient;
    }

    public void setSyncWithSymphonyClient(String syncWithSymphonyClient) {
        this.syncWithSymphonyClient = syncWithSymphonyClient;
    }

    public String getClientArticleId() {
        return clientArticleId;
    }

    public void setClientArticleId(String clientArticleId) {
        this.clientArticleId = clientArticleId;
    }

    public String getClientArticlePermalink() {
        return clientArticlePermalink;
    }

    public void setClientArticlePermalink(String clientArticlePermalink) {
        this.clientArticlePermalink = clientArticlePermalink;
    }

    public Integer getArticleEditorType() {
        return articleEditorType;
    }

    public void setArticleEditorType(Integer articleEditorType) {
        this.articleEditorType = articleEditorType;
    }

    public Integer getArticleStatus() {
        return articleStatus;
    }

    public void setArticleStatus(Integer articleStatus) {
        this.articleStatus = articleStatus;
    }

    public Integer getArticleType() {
        return articleType;
    }

    public void setArticleType(Integer articleType) {
        this.articleType = articleType;
    }

    public Integer getArticleGoodCnt() {
        return articleGoodCnt;
    }

    public void setArticleGoodCnt(Integer articleGoodCnt) {
        this.articleGoodCnt = articleGoodCnt;
    }

    public Integer getArticleBadCnt() {
        return articleBadCnt;
    }

    public void setArticleBadCnt(Integer articleBadCnt) {
        this.articleBadCnt = articleBadCnt;
    }

    public Integer getArticleCollectCnt() {
        return articleCollectCnt;
    }

    public void setArticleCollectCnt(Integer articleCollectCnt) {
        this.articleCollectCnt = articleCollectCnt;
    }

    public Integer getArticleWatchCnt() {
        return articleWatchCnt;
    }

    public void setArticleWatchCnt(Integer articleWatchCnt) {
        this.articleWatchCnt = articleWatchCnt;
    }

    public Double getRedditScore() {
        return redditScore;
    }

    public void setRedditScore(Double redditScore) {
        this.redditScore = redditScore;
    }

    public String getArticleCity() {
        return articleCity;
    }

    public void setArticleCity(String articleCity) {
        this.articleCity = articleCity;
    }

    public String getArticleIP() {
        return articleIP;
    }

    public void setArticleIP(String articleIP) {
        this.articleIP = articleIP;
    }

    public String getArticleUA() {
        return articleUA;
    }

    public void setArticleUA(String articleUA) {
        this.articleUA = articleUA;
    }

    public Long getArticleStick() {
        return articleStick;
    }

    public void setArticleStick(Long articleStick) {
        this.articleStick = articleStick;
    }

    public Integer getArticleAnonymous() {
        return articleAnonymous;
    }

    public void setArticleAnonymous(Integer articleAnonymous) {
        this.articleAnonymous = articleAnonymous;
    }

    public Integer getArticlePerfect() {
        return articlePerfect;
    }

    public void setArticlePerfect(Integer articlePerfect) {
        this.articlePerfect = articlePerfect;
    }

    public Integer getArticleAnonymousView() {
        return articleAnonymousView;
    }

    public void setArticleAnonymousView(Integer articleAnonymousView) {
        this.articleAnonymousView = articleAnonymousView;
    }

    public String getArticleAudioURL() {
        return articleAudioURL;
    }

    public void setArticleAudioURL(String articleAudioURL) {
        this.articleAudioURL = articleAudioURL;
    }

    public Integer getArticleQnAOfferPoint() {
        return articleQnAOfferPoint;
    }

    public void setArticleQnAOfferPoint(Integer articleQnAOfferPoint) {
        this.articleQnAOfferPoint = articleQnAOfferPoint;
    }

    public Integer getArticlePushOrder() {
        return articlePushOrder;
    }

    public void setArticlePushOrder(Integer articlePushOrder) {
        this.articlePushOrder = articlePushOrder;
    }
}
