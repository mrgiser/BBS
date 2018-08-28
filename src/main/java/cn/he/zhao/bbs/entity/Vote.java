package cn.he.zhao.bbs.entity;

public class Vote {

    private String oid;

    /**
     * Key of user id.
     */
    private String userId;

    /**
     * Key of type.
     */
    private Integer type;

    /**
     * Key of data type.
     */
    private Integer dataType;

    /**
     * Key of data id.
     */
    private String dataId;

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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
}
