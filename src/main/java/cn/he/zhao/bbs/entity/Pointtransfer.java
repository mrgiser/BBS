package cn.he.zhao.bbs.entity;

public class Pointtransfer {

    private String oid;

    /**
     * Key of from user id.
     */
    private String fromId;

    /**
     * Key of to user id.
     */
    private String toId;

    /**
     * Key of sum.
     */
    private Integer sum;

    /**
     * Key of from balance.
     */
    private Integer fromBalance;

    /**
     * Key of to balance.
     */
    private Integer toBalance;

    /**
     * Key of time.
     */
    private Long time;

    /**
     * Key of transfer type.
     */
    private Integer type;

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

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public Integer getSum() {
        return sum;
    }

    public void setSum(Integer sum) {
        this.sum = sum;
    }

    public Integer getFromBalance() {
        return fromBalance;
    }

    public void setFromBalance(Integer fromBalance) {
        this.fromBalance = fromBalance;
    }

    public Integer getToBalance() {
        return toBalance;
    }

    public void setToBalance(Integer toBalance) {
        this.toBalance = toBalance;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }
}
