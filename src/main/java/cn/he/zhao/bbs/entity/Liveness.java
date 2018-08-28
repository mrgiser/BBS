package cn.he.zhao.bbs.entity;


public class Liveness {

    private String oid;

    /**
     * Key of user id.
     */
    private String livenessUserId;

    /**
     * Key of liveness date.
     */
    private String livenessDate;

    /**
     * Key of liveness point.
     */
    private Integer livenessPoint;

    /**
     * Key of liveness article.
     */
    private Integer livenessArticle;

    /**
     * Key of liveness comment.
     */
    private Integer livenessComment;

    /**
     * Key of liveness activity.
     */
    private Integer livenessActivity;

    /**
     * Key of liveness thank.
     */
    private Integer livenessThank;

    /**
     * Key of liveness vote.
     */
    private Integer livenessVote;

    /**
     * Key of liveness reward.
     */
    private Integer livenessReward;

    /**
     * Key of liveness PV.
     */
    private Integer livenessPV;

    /**
     * Key of liveness accept answer.
     */
    private Integer livenessAcceptAnswer;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getLivenessUserId() {
        return livenessUserId;
    }

    public void setLivenessUserId(String livenessUserId) {
        this.livenessUserId = livenessUserId;
    }

    public String getLivenessDate() {
        return livenessDate;
    }

    public void setLivenessDate(String livenessDate) {
        this.livenessDate = livenessDate;
    }

    public Integer getLivenessPoint() {
        return livenessPoint;
    }

    public void setLivenessPoint(Integer livenessPoint) {
        this.livenessPoint = livenessPoint;
    }

    public Integer getLivenessArticle() {
        return livenessArticle;
    }

    public void setLivenessArticle(Integer livenessArticle) {
        this.livenessArticle = livenessArticle;
    }

    public Integer getLivenessComment() {
        return livenessComment;
    }

    public void setLivenessComment(Integer livenessComment) {
        this.livenessComment = livenessComment;
    }

    public Integer getLivenessActivity() {
        return livenessActivity;
    }

    public void setLivenessActivity(Integer livenessActivity) {
        this.livenessActivity = livenessActivity;
    }

    public Integer getLivenessThank() {
        return livenessThank;
    }

    public void setLivenessThank(Integer livenessThank) {
        this.livenessThank = livenessThank;
    }

    public Integer getLivenessVote() {
        return livenessVote;
    }

    public void setLivenessVote(Integer livenessVote) {
        this.livenessVote = livenessVote;
    }

    public Integer getLivenessReward() {
        return livenessReward;
    }

    public void setLivenessReward(Integer livenessReward) {
        this.livenessReward = livenessReward;
    }

    public Integer getLivenessPV() {
        return livenessPV;
    }

    public void setLivenessPV(Integer livenessPV) {
        this.livenessPV = livenessPV;
    }

    public Integer getLivenessAcceptAnswer() {
        return livenessAcceptAnswer;
    }

    public void setLivenessAcceptAnswer(Integer livenessAcceptAnswer) {
        this.livenessAcceptAnswer = livenessAcceptAnswer;
    }
}
