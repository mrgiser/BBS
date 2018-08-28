package cn.he.zhao.bbs.entity;

public class Reward {

    private String oid;

    /**
     * Key of sender id.
     */
    private String senderId;

    /**
     * Key of data id.
     */
    private String dataId;

    /**
     * Key of type.
     */
    private Integer type;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
