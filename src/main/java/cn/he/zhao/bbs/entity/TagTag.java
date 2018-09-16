package cn.he.zhao.bbs.entity;

/**
 * 描述:
 * userTag
 *
 * @Author HeFeng
 * @Create 2018-09-02 15:34
 */
public class TagTag {
    private  String oid;
    private  String tag1_oId;
    private  String tag2_oId;
    private  Integer weight;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getTag1_oId() {
        return tag1_oId;
    }

    public void setTag1_oId(String tag1_oId) {
        this.tag1_oId = tag1_oId;
    }

    public String getTag2_oId() {
        return tag2_oId;
    }

    public void setTag2_oId(String tag2_oId) {
        this.tag2_oId = tag2_oId;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}