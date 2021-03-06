package cn.he.zhao.bbs.entity;

public class Tag {

    private String oid;

    /**
     * Key of tag reference count.
     */
    private Integer tagReferenceCount;

    /**
     * Key of tag comment count.
     */
    private Integer tagCommentCount;

    /**
     * Key of tag follower count.
     */
    private Integer tagFollowerCount;

    /**
     * Key of link count.
     */
    private Integer tagLinkCount;

    /**
     * Key of tag title.
     */
    private String tagTitle;

    /**
     * Key of tag URI.
     */
    private String tagURI;

    /**
     * Key of tag icon path.
     */
    private String tagIconPath;

    /**
     * Key of tag CSS.
     */
    private String tagCSS;

    /**
     * Key of tag description.
     */
    private String tagDescription;

    /**
     * Key of tag status.
     */
    private Integer tagStatus;

    /**
     * Key of tag good count.
     */
    private Integer tagGoodCnt;

    /**
     * Key of tag bad count.
     */
    private Integer tagBadCnt;

    /**
     * Key of tag seo title.
     */
    private String tagSeoTitle;

    /**
     * Key of tag seo keywords.
     */
    private String tagSeoKeywords;

    /**
     * Key of tag seo description.
     */
    private String tagSeoDesc;

    /**
     * Key of tag random double value.
     */
    private Double tagRandomDouble;

    //transient
    private transient String tagDescriptionText;

    //transient
    private transient Object tagDomains;

    //transient
    private transient String tagCreatorThumbnailURL;

    private transient Long tagCreatorThumbnailUpdateTime;
    private transient String tagCreatorName;
    private transient Long tagCreateTime;
    private transient String tagTitleLowerCase;
    private transient Object tagLinks;

    public Object getTagLinks() {
        return tagLinks;
    }

    public void setTagLinks(Object tagLinks) {
        this.tagLinks = tagLinks;
    }

    public String getTagTitleLowerCase() {
        return tagTitleLowerCase;
    }

    public void setTagTitleLowerCase(String tagTitleLowerCase) {
        this.tagTitleLowerCase = tagTitleLowerCase;
    }

    public Long getTagCreateTime() {
        return tagCreateTime;
    }

    public void setTagCreateTime(Long tagCreateTime) {
        this.tagCreateTime = tagCreateTime;
    }

    public String getTagCreatorName() {
        return tagCreatorName;
    }

    public void setTagCreatorName(String tagCreatorName) {
        this.tagCreatorName = tagCreatorName;
    }

    public Long getTagCreatorThumbnailUpdateTime() {
        return tagCreatorThumbnailUpdateTime;
    }

    public void setTagCreatorThumbnailUpdateTime(Long tagCreatorThumbnailUpdateTime) {
        this.tagCreatorThumbnailUpdateTime = tagCreatorThumbnailUpdateTime;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Integer getTagReferenceCount() {
        return tagReferenceCount;
    }

    public void setTagReferenceCount(Integer tagReferenceCount) {
        this.tagReferenceCount = tagReferenceCount;
    }

    public Integer getTagCommentCount() {
        return tagCommentCount;
    }

    public void setTagCommentCount(Integer tagCommentCount) {
        this.tagCommentCount = tagCommentCount;
    }

    public Integer getTagFollowerCount() {
        return tagFollowerCount;
    }

    public void setTagFollowerCount(Integer tagFollowerCount) {
        this.tagFollowerCount = tagFollowerCount;
    }

    public Integer getTagLinkCount() {
        return tagLinkCount;
    }

    public void setTagLinkCount(Integer tagLinkCount) {
        this.tagLinkCount = tagLinkCount;
    }

    public String getTagTitle() {
        return tagTitle;
    }

    public void setTagTitle(String tagTitle) {
        this.tagTitle = tagTitle;
    }

    public String getTagURI() {
        return tagURI;
    }

    public void setTagURI(String tagURI) {
        this.tagURI = tagURI;
    }

    public String getTagIconPath() {
        return tagIconPath;
    }

    public void setTagIconPath(String tagIconPath) {
        this.tagIconPath = tagIconPath;
    }

    public String getTagCSS() {
        return tagCSS;
    }

    public void setTagCSS(String tagCSS) {
        this.tagCSS = tagCSS;
    }

    public String getTagDescription() {
        return tagDescription;
    }

    public void setTagDescription(String tagDescription) {
        this.tagDescription = tagDescription;
    }

    public Integer getTagStatus() {
        return tagStatus;
    }

    public void setTagStatus(Integer tagStatus) {
        this.tagStatus = tagStatus;
    }

    public Integer getTagGoodCnt() {
        return tagGoodCnt;
    }

    public void setTagGoodCnt(Integer tagGoodCnt) {
        this.tagGoodCnt = tagGoodCnt;
    }

    public Integer getTagBadCnt() {
        return tagBadCnt;
    }

    public void setTagBadCnt(Integer tagBadCnt) {
        this.tagBadCnt = tagBadCnt;
    }

    public String getTagSeoTitle() {
        return tagSeoTitle;
    }

    public void setTagSeoTitle(String tagSeoTitle) {
        this.tagSeoTitle = tagSeoTitle;
    }

    public String getTagSeoKeywords() {
        return tagSeoKeywords;
    }

    public void setTagSeoKeywords(String tagSeoKeywords) {
        this.tagSeoKeywords = tagSeoKeywords;
    }

    public String getTagSeoDesc() {
        return tagSeoDesc;
    }

    public void setTagSeoDesc(String tagSeoDesc) {
        this.tagSeoDesc = tagSeoDesc;
    }

    public Double getTagRandomDouble() {
        return tagRandomDouble;
    }

    public void setTagRandomDouble(Double tagRandomDouble) {
        this.tagRandomDouble = tagRandomDouble;
    }

    public String getTagDescriptionText() {
        return tagDescriptionText;
    }

    public void setTagDescriptionText(String tagDescriptionText) {
        this.tagDescriptionText = tagDescriptionText;
    }

    public Object getTagDomains() {
        return tagDomains;
    }

    public void setTagDomains(Object tagDomains) {
        this.tagDomains = tagDomains;
    }

    public String getTagCreatorThumbnailURL() {
        return tagCreatorThumbnailURL;
    }

    public void setTagCreatorThumbnailURL(String tagCreatorThumbnailURL) {
        this.tagCreatorThumbnailURL = tagCreatorThumbnailURL;
    }
}
