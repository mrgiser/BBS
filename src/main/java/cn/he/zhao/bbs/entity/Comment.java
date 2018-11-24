package cn.he.zhao.bbs.entity;

public class Comment {

    private String oid;

    /**
     * Key of client comment id.
     */
    private String clientCommentId;

    /**
     * Key of comment content.
     */
    private String commentContent;

    /**
     * Key of comment create time.
     */
    private Long commentCreateTime;

    private String commentCreateTimeStr;

    public String getCommentCreateTimeStr() {
        return commentCreateTimeStr;
    }

    public void setCommentCreateTimeStr(String commentCreateTimeStr) {
        this.commentCreateTimeStr = commentCreateTimeStr;
    }

    /**
     * Key of comment author id.
     */
    private String commentAuthorId;

    /**
     * Key of comment on article id.
     */
    private String commentOnArticleId;

    /**
     * Key of comment sharp URL.
     */
    private String commentSharpURL;

    /**
     * Key of original comment id.
     */
    private String commentOriginalCommentId;

    /**
     * Key of comment status.
     */
    private Integer commentStatus;

    /**
     * Key of comment IP.
     */
    private String commentIP;

    /**
     * Key of comment UA.
     */
    private String commentUA;

    /**
     * Key of comment anonymous.
     */
    private Integer commentAnonymous;

    /**
     * Key of comment good count.
     */
    private Integer commentGoodCnt;

    /**
     * Key of comment bad count.
     */
    private Integer commentBadCnt;

    /**
     * Key of comment score.
     */
    private Double commentScore;

    /**
     * Key of comment reply count.
     */
    private Integer commentReplyCnt;

    /**
     * Key of comment audio URL.
     */
    private String commentAudioURL;

    /**
     * Key of comment offered. https://github.com/b3log/symphony/issues/486
     */
    private Integer commentQnAOffered;

    private String timeAgo;

    private transient Object commenter;

    private transient String commentAuthorName;

    private transient String commentAuthorURL;

    private transient String commentAuthorThumbnailURL;

    private transient boolean fromClient;

    private transient String commentArticleTitle;

    private transient String commentArticlePermalink;

    public String getCommentArticlePermalink() {
        return commentArticlePermalink;
    }

    public void setCommentArticlePermalink(String commentArticlePermalink) {
        this.commentArticlePermalink = commentArticlePermalink;
    }

    public String getCommentArticleTitle() {
        return commentArticleTitle;
    }

    public void setCommentArticleTitle(String commentArticleTitle) {
        this.commentArticleTitle = commentArticleTitle;
    }

    public boolean isFromClient() {
        return fromClient;
    }

    public void setFromClient(boolean fromClient) {
        this.fromClient = fromClient;
    }

    public String getCommentAuthorThumbnailURL() {
        return commentAuthorThumbnailURL;
    }

    public void setCommentAuthorThumbnailURL(String commentAuthorThumbnailURL) {
        this.commentAuthorThumbnailURL = commentAuthorThumbnailURL;
    }

    public String getCommentAuthorURL() {
        return commentAuthorURL;
    }

    public void setCommentAuthorURL(String commentAuthorURL) {
        this.commentAuthorURL = commentAuthorURL;
    }

    public String getCommentAuthorName() {
        return commentAuthorName;
    }

    public void setCommentAuthorName(String commentAuthorName) {
        this.commentAuthorName = commentAuthorName;
    }

    public Object getCommenter() {
        return commenter;
    }

    public void setCommenter(Object commenter) {
        this.commenter = commenter;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getClientCommentId() {
        return clientCommentId;
    }

    public void setClientCommentId(String clientCommentId) {
        this.clientCommentId = clientCommentId;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public Long getCommentCreateTime() {
        return commentCreateTime;
    }

    public void setCommentCreateTime(Long commentCreateTime) {
        this.commentCreateTime = commentCreateTime;
    }

    public String getCommentAuthorId() {
        return commentAuthorId;
    }

    public void setCommentAuthorId(String commentAuthorId) {
        this.commentAuthorId = commentAuthorId;
    }

    public String getCommentOnArticleId() {
        return commentOnArticleId;
    }

    public void setCommentOnArticleId(String commentOnArticleId) {
        this.commentOnArticleId = commentOnArticleId;
    }

    public String getCommentSharpURL() {
        return commentSharpURL;
    }

    public void setCommentSharpURL(String commentSharpURL) {
        this.commentSharpURL = commentSharpURL;
    }

    public String getCommentOriginalCommentId() {
        return commentOriginalCommentId;
    }

    public void setCommentOriginalCommentId(String commentOriginalCommentId) {
        this.commentOriginalCommentId = commentOriginalCommentId;
    }

    public Integer getCommentStatus() {
        return commentStatus;
    }

    public void setCommentStatus(Integer commentStatus) {
        this.commentStatus = commentStatus;
    }

    public String getCommentIP() {
        return commentIP;
    }

    public void setCommentIP(String commentIP) {
        this.commentIP = commentIP;
    }

    public String getCommentUA() {
        return commentUA;
    }

    public void setCommentUA(String commentUA) {
        this.commentUA = commentUA;
    }

    public Integer getCommentAnonymous() {
        return commentAnonymous;
    }

    public void setCommentAnonymous(Integer commentAnonymous) {
        this.commentAnonymous = commentAnonymous;
    }

    public Integer getCommentGoodCnt() {
        return commentGoodCnt;
    }

    public void setCommentGoodCnt(Integer commentGoodCnt) {
        this.commentGoodCnt = commentGoodCnt;
    }

    public Integer getCommentBadCnt() {
        return commentBadCnt;
    }

    public void setCommentBadCnt(Integer commentBadCnt) {
        this.commentBadCnt = commentBadCnt;
    }

    public Double getCommentScore() {
        return commentScore;
    }

    public void setCommentScore(Double commentScore) {
        this.commentScore = commentScore;
    }

    public Integer getCommentReplyCnt() {
        return commentReplyCnt;
    }

    public void setCommentReplyCnt(Integer commentReplyCnt) {
        this.commentReplyCnt = commentReplyCnt;
    }

    public String getCommentAudioURL() {
        return commentAudioURL;
    }

    public void setCommentAudioURL(String commentAudioURL) {
        this.commentAudioURL = commentAudioURL;
    }

    public Integer getCommentQnAOffered() {
        return commentQnAOffered;
    }

    public void setCommentQnAOffered(Integer commentQnAOffered) {
        this.commentQnAOffered = commentQnAOffered;
    }
}
