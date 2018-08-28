package cn.he.zhao.bbs.entity;

public class Follow {

    private String oid;

    /**
     * Key of follower id.
     */
    private String followerId;

    /**
     * Key of following id.
     */
    private String followingId;

    /**
     * Key of following type.
     */
    private Integer followingType;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getFollowerId() {
        return followerId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }

    public String getFollowingId() {
        return followingId;
    }

    public void setFollowingId(String followingId) {
        this.followingId = followingId;
    }

    public Integer getFollowingType() {
        return followingType;
    }

    public void setFollowingType(Integer followingType) {
        this.followingType = followingType;
    }
}
