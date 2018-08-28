package cn.he.zhao.bbs.entity;

public class Notification {

    private String oid;

    /**
     * Key of user id.
     */
    private String userId;

    /**
     * Key of data id.
     */
    private String dataId;

    /**
     * Key of data type.
     */
    private Integer dataType;

    /**
     * Key of has read.
     */
    private String hasRead;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public String getHasRead() {
        return hasRead;
    }

    public void setHasRead(String hasRead) {
        this.hasRead = hasRead;
    }
}
