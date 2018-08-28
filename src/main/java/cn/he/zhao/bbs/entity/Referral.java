package cn.he.zhao.bbs.entity;

public class Referral {

    private String oid;

    /**
     * Key of referral user.
     */
    public static final String REFERRAL_USER = "referralUser";

    /**
     * Key of referral data id.
     */
    public static final String REFERRAL_DATA_ID = "referralDataId";

    /**
     * Key of referral type.
     */
    public static final String REFERRAL_TYPE = "referralType";

    /**
     * Key of source IP.
     */
    public static final String REFERRAL_IP = "referralIP";

    /**
     * Key of click.
     */
    public static final String REFERRAL_CLICK = "referralClick";

    /**
     * Key of referral user has point.
     */
    public static final String REFERRAL_USER_HAS_POINT = "referralUserHasPoint";

    /**
     * Key of referral author has point.
     */
    public static final String REFERRAL_AUTHOR_HAS_POINT = "referralAuthorHasPoint";

    // Type constants
    /**
     * Type - Article.
     */
    public static final int REFERRAL_TYPE_C_ARTICLE = 0;
}
