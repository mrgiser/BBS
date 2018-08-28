package cn.he.zhao.bbs.entity;

public class Verifycode {

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
     * Key of business type.
     */
    private Integer bizType;

    /**
     * Key of receiver.
     */
    private String receiver;

    /**
     * Key of code.
     */
    private String code;

    /**
     * Key of status.
     */
    private Integer status;

    /**
     * Key of expired.
     */
    private Long expired;

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

    public Integer getBizType() {
        return bizType;
    }

    public void setBizType(Integer bizType) {
        this.bizType = bizType;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getExpired() {
        return expired;
    }

    public void setExpired(Long expired) {
        this.expired = expired;
    }
}
