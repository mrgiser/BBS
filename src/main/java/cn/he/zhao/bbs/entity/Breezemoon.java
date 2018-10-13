package cn.he.zhao.bbs.entity;

import java.util.Date;

public class Breezemoon {

    private String oid;

    /**
     * Key of breezemoon content.
     */
    private String breezemoonContent;

    /**
     * Key of breezemoon author id.
     */
    private String breezemoonAuthorId;

    /**
     * Key of breezemoon created at.
     */
    private Long breezemoonCreated;

    /**
     * Key of breezemoon updated at.
     */
    private Long breezemoonUpdated;

    /**
     * Key of breezemoon IP.
     */
    private String breezemoonIP;

    /**
     * Key of breezemoon UA.
     */
    private String breezemoonUA;

    /**
     * Key of breezemoon status.
     */
    private Integer breezemoonStatus;

    private transient String breezemoonAuthorName;

    private transient String breezemoonAuthorThumbnailURL;

    private transient String timeAgo;

    private transient Date breezemoonCreateTime;

    public Date getBreezemoonCreateTime() {
        return breezemoonCreateTime;
    }

    public void setBreezemoonCreateTime(Date breezemoonCreateTime) {
        this.breezemoonCreateTime = breezemoonCreateTime;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public String getBreezemoonAuthorThumbnailURL() {
        return breezemoonAuthorThumbnailURL;
    }

    public void setBreezemoonAuthorThumbnailURL(String breezemoonAuthorThumbnailURL) {
        this.breezemoonAuthorThumbnailURL = breezemoonAuthorThumbnailURL;
    }

    public String getBreezemoonAuthorName() {
        return breezemoonAuthorName;
    }

    public void setBreezemoonAuthorName(String breezemoonAuthorName) {
        this.breezemoonAuthorName = breezemoonAuthorName;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getBreezemoonContent() {
        return breezemoonContent;
    }

    public void setBreezemoonContent(String breezemoonContent) {
        this.breezemoonContent = breezemoonContent;
    }

    public String getBreezemoonAuthorId() {
        return breezemoonAuthorId;
    }

    public void setBreezemoonAuthorId(String breezemoonAuthorId) {
        this.breezemoonAuthorId = breezemoonAuthorId;
    }

    public Long getBreezemoonCreated() {
        return breezemoonCreated;
    }

    public void setBreezemoonCreated(Long breezemoonCreated) {
        this.breezemoonCreated = breezemoonCreated;
    }

    public Long getBreezemoonUpdated() {
        return breezemoonUpdated;
    }

    public void setBreezemoonUpdated(Long breezemoonUpdated) {
        this.breezemoonUpdated = breezemoonUpdated;
    }

    public String getBreezemoonIP() {
        return breezemoonIP;
    }

    public void setBreezemoonIP(String breezemoonIP) {
        this.breezemoonIP = breezemoonIP;
    }

    public String getBreezemoonUA() {
        return breezemoonUA;
    }

    public void setBreezemoonUA(String breezemoonUA) {
        this.breezemoonUA = breezemoonUA;
    }

    public Integer getBreezemoonStatus() {
        return breezemoonStatus;
    }

    public void setBreezemoonStatus(Integer breezemoonStatus) {
        this.breezemoonStatus = breezemoonStatus;
    }
}

