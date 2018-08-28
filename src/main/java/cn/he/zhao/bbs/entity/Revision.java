package cn.he.zhao.bbs.entity;

public class Revision {

    private String oid;

    /**
     * Key of revision data type.
     */
    private Integer revisionDataType;

    /**
     * Key of revision data id.
     */
    private String revisionDataId;

    /**
     * Key of revision data.
     */
    private String revisionData;

    /**
     * Key of revision author id.
     */
    private String revisionAuthorId;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Integer getRevisionDataType() {
        return revisionDataType;
    }

    public void setRevisionDataType(Integer revisionDataType) {
        this.revisionDataType = revisionDataType;
    }

    public String getRevisionDataId() {
        return revisionDataId;
    }

    public void setRevisionDataId(String revisionDataId) {
        this.revisionDataId = revisionDataId;
    }

    public String getRevisionData() {
        return revisionData;
    }

    public void setRevisionData(String revisionData) {
        this.revisionData = revisionData;
    }

    public String getRevisionAuthorId() {
        return revisionAuthorId;
    }

    public void setRevisionAuthorId(String revisionAuthorId) {
        this.revisionAuthorId = revisionAuthorId;
    }
}
