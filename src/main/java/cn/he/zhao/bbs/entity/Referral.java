package cn.he.zhao.bbs.entity;

public class Referral {

    private String oid;

    /**
     * Key of referral user.
     */
    private String referralUser;

    /**
     * Key of referral data id.
     */
    private String referralDataId;

    /**
     * Key of referral type.
     */
    private Integer referralType;

    /**
     * Key of source IP.
     */
    private String referralIP;

    /**
     * Key of click.
     */
    private Integer referralClick;

    /**
     * Key of referral user has point.
     */
    private String referralUserHasPoint;

    /**
     * Key of referral author has point.
     */
    private String referralAuthorHasPoint;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getReferralUser() {
        return referralUser;
    }

    public void setReferralUser(String referralUser) {
        this.referralUser = referralUser;
    }

    public String getReferralDataId() {
        return referralDataId;
    }

    public void setReferralDataId(String referralDataId) {
        this.referralDataId = referralDataId;
    }

    public Integer getReferralType() {
        return referralType;
    }

    public void setReferralType(Integer referralType) {
        this.referralType = referralType;
    }

    public String getReferralIP() {
        return referralIP;
    }

    public void setReferralIP(String referralIP) {
        this.referralIP = referralIP;
    }

    public Integer getReferralClick() {
        return referralClick;
    }

    public void setReferralClick(Integer referralClick) {
        this.referralClick = referralClick;
    }

    public String getReferralUserHasPoint() {
        return referralUserHasPoint;
    }

    public void setReferralUserHasPoint(String referralUserHasPoint) {
        this.referralUserHasPoint = referralUserHasPoint;
    }

    public String getReferralAuthorHasPoint() {
        return referralAuthorHasPoint;
    }

    public void setReferralAuthorHasPoint(String referralAuthorHasPoint) {
        this.referralAuthorHasPoint = referralAuthorHasPoint;
    }
}
