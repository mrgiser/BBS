package cn.he.zhao.bbs.entity;

public class Option {

    private String oid;

    /**
     * Key of option value.
     */
    public static final String OPTION_VALUE = "optionValue";

    /**
     * Key of option category.
     */
    public static final String OPTION_CATEGORY = "optionCategory";

    // oId constants
    /**
     * Key of member count.
     */
    public static final String ID_C_STATISTIC_MEMBER_COUNT = "statisticMemberCount";

    /**
     * Key of article count.
     */
    public static final String ID_C_STATISTIC_ARTICLE_COUNT = "statisticArticleCount";

    /**
     * Key of domain count.
     */
    public static final String ID_C_STATISTIC_DOMAIN_COUNT = "statisticDomainCount";

    /**
     * Key of tag count.
     */
    public static final String ID_C_STATISTIC_TAG_COUNT = "statisticTagCount";

    /**
     * Key of link count.
     */
    public static final String ID_C_STATISTIC_LINK_COUNT = "statisticLinkCount";

    /**
     * Key of comment count.
     */
    public static final String ID_C_STATISTIC_CMT_COUNT = "statisticCmtCount";

    /**
     * Key of max online visitor count.
     */
    public static final String ID_C_STATISTIC_MAX_ONLINE_VISITOR_COUNT = "statisticMaxOnlineVisitorCount";

    /**
     * Key of allow register.
     */
    public static final String ID_C_MISC_ALLOW_REGISTER = "miscAllowRegister";

    /**
     * Key of allow anonymous view.
     */
    public static final String ID_C_MISC_ALLOW_ANONYMOUS_VIEW = "miscAllowAnonymousView";

    /**
     * Key of allow add article.
     */
    public static final String ID_C_MISC_ALLOW_ADD_ARTICLE = "miscAllowAddArticle";

    /**
     * Key of allow add comment.
     */
    public static final String ID_C_MISC_ALLOW_ADD_COMMENT = "miscAllowAddComment";

    /**
     * Key of language.
     */
    public static final String ID_C_MISC_LANGUAGE = "miscLanguage";

    /**
     * Key of side full ad.
     */
    public static final String ID_C_SIDE_FULL_AD = "adSideFull";

    /**
     * Key of header banner.
     */
    public static final String ID_C_HEADER_BANNER = "headerBanner";

    // Category constants
    /**
     * Statistic.
     */
    public static final String CATEGORY_C_STATISTIC = "statistic";

    /**
     * Miscellaneous.
     */
    public static final String CATEGORY_C_MISC = "misc";

    /**
     * Reserved words.
     */
    public static final String CATEGORY_C_RESERVED_WORDS = "reserved-words";

    /**
     * Ad.
     */
    public static final String CATEGORY_C_AD = "ad";

    /**
     * Private constructor.
     */
    private Option() {
    }
}
