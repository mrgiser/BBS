package cn.he.zhao.bbs.entity;

public class Link {

    private String oid;

    /**
     * Key of link address.
     */
    private String linkAddr;

    /**
     * Key of link title.
     */
    private String linkTitle;

    /**
     * Key of link type.
     */
    private Integer linkType;

    /**
     * Key of link submit count.
     */
    private Integer linkSubmitCnt;

    /**
     * Key of link click count.
     */
    private Integer linkClickCnt;

    /**
     * Key of link good count.
     */
    private Integer linkGoodCnt;

    /**
     * Key of link bad count.
     */
    private Integer linkBadCnt;

    /**
     * Key of link Baidu reference count.
     */
    private Integer linkBaiduRefCnt;

    /**
     * Key of link score.
     */
    private Double linkScore;

    /**
     * Key of link ping count.
     */
    private Integer linkPingCnt;

    /**
     * Key of link ping error count.
     */
    private Integer linkPingErrCnt;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getLinkAddr() {
        return linkAddr;
    }

    public void setLinkAddr(String linkAddr) {
        this.linkAddr = linkAddr;
    }

    public String getLinkTitle() {
        return linkTitle;
    }

    public void setLinkTitle(String linkTitle) {
        this.linkTitle = linkTitle;
    }

    public Integer getLinkType() {
        return linkType;
    }

    public void setLinkType(Integer linkType) {
        this.linkType = linkType;
    }

    public Integer getLinkSubmitCnt() {
        return linkSubmitCnt;
    }

    public void setLinkSubmitCnt(Integer linkSubmitCnt) {
        this.linkSubmitCnt = linkSubmitCnt;
    }

    public Integer getLinkClickCnt() {
        return linkClickCnt;
    }

    public void setLinkClickCnt(Integer linkClickCnt) {
        this.linkClickCnt = linkClickCnt;
    }

    public Integer getLinkGoodCnt() {
        return linkGoodCnt;
    }

    public void setLinkGoodCnt(Integer linkGoodCnt) {
        this.linkGoodCnt = linkGoodCnt;
    }

    public Integer getLinkBadCnt() {
        return linkBadCnt;
    }

    public void setLinkBadCnt(Integer linkBadCnt) {
        this.linkBadCnt = linkBadCnt;
    }

    public Integer getLinkBaiduRefCnt() {
        return linkBaiduRefCnt;
    }

    public void setLinkBaiduRefCnt(Integer linkBaiduRefCnt) {
        this.linkBaiduRefCnt = linkBaiduRefCnt;
    }

    public Double getLinkScore() {
        return linkScore;
    }

    public void setLinkScore(Double linkScore) {
        this.linkScore = linkScore;
    }

    public Integer getLinkPingCnt() {
        return linkPingCnt;
    }

    public void setLinkPingCnt(Integer linkPingCnt) {
        this.linkPingCnt = linkPingCnt;
    }

    public Integer getLinkPingErrCnt() {
        return linkPingErrCnt;
    }

    public void setLinkPingErrCnt(Integer linkPingErrCnt) {
        this.linkPingErrCnt = linkPingErrCnt;
    }
}
