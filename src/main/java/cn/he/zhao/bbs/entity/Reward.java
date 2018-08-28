package cn.he.zhao.bbs.entity;

public class Reward {

    private String oid;

    /**
     * Key of sender id.
     */
    public static final String SENDER_ID = "senderId";

    /**
     * Key of data id.
     */
    public static final String DATA_ID = "dataId";

    /**
     * Key of type.
     */
    public static final String TYPE = "type";

    // Reward type constants
    /**
     * Reward type - reward article.
     */
    public static final int TYPE_C_ARTICLE = 0;

    /**
     * Reward type - comment.
     */
    public static final int TYPE_C_COMMENT = 1;

    /**
     * Reward type - user.
     */
    public static final int TYPE_C_USER = 2;

    /**
     * Reward type - thank article.
     */
    public static final int TYPE_C_THANK_ARTICLE = 3;

    /**
     * Reward type - accept comment.
     */
    public static final int TYPE_C_ACCEPT_COMMENT = 4;

    /**
     * Private constructor.
     */
    private Reward() {
    }
}
