package cn.he.zhao.bbs.entity;

/**
 * 描述:
 * tag article 关系
 *
 * @Author HeFeng
 * @Create 2018-09-02 12:50
 */
public class TagArticle {

    private String oid;

    private String article_oid;

    private String tag_oid;

    private Integer articleCommentCount;

    private Long articleLatestCmtTime;

    private Double redditScore;

    private Integer articlePerfect;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getArticle_oid() {
        return article_oid;
    }

    public void setArticle_oid(String article_oid) {
        this.article_oid = article_oid;
    }

    public String getTag_oid() {
        return tag_oid;
    }

    public void setTag_oid(String tag_oid) {
        this.tag_oid = tag_oid;
    }

    public Integer getArticleCommentCount() {
        return articleCommentCount;
    }

    public void setArticleCommentCount(Integer articleCommentCount) {
        this.articleCommentCount = articleCommentCount;
    }

    public Long getArticleLatestCmtTime() {
        return articleLatestCmtTime;
    }

    public void setArticleLatestCmtTime(Long articleLatestCmtTime) {
        this.articleLatestCmtTime = articleLatestCmtTime;
    }

    public Double getRedditScore() {
        return redditScore;
    }

    public void setRedditScore(Double redditScore) {
        this.redditScore = redditScore;
    }

    public Integer getArticlePerfect() {
        return articlePerfect;
    }

    public void setArticlePerfect(Integer articlePerfect) {
        this.articlePerfect = articlePerfect;
    }
}