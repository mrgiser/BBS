package cn.he.zhao.bbs.entity;

public class Revision {

    private String oid;

    /**
     * Key of revision data type.
     */
    public static final String REVISION_DATA_TYPE = "revisionDataType";

    /**
     * Key of revision data id.
     */
    public static final String REVISION_DATA_ID = "revisionDataId";

    /**
     * Key of revision data.
     */
    public static final String REVISION_DATA = "revisionData";

    /**
     * Key of revision author id.
     */
    public static final String REVISION_AUTHOR_ID = "revisionAuthorId";

    // Data type constants
    /**
     * Data type - article.
     */
    public static final int DATA_TYPE_C_ARTICLE = 0;

    /**
     * Data type - comment.
     */
    public static final int DATA_TYPE_C_COMMENT = 1;
}
