package cn.he.zhao.bbs.entity;

public class Invitecode {

    private String oid;

    /**
     * Key of code.
     */
    private String code;

    /**
     * Key of generator id.
     */
    private String generatorId;

    /**
     * Key of user id.
     */
    private String userId;

    /**
     * Key of use time.
     */
    private Long useTime;

    /**
     * Key of status.
     */
    private Integer status;

    /**
     * Key of memo.
     */
    private String memo;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getGeneratorId() {
        return generatorId;
    }

    public void setGeneratorId(String generatorId) {
        this.generatorId = generatorId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getUseTime() {
        return useTime;
    }

    public void setUseTime(Long useTime) {
        this.useTime = useTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
