package cn.he.zhao.bbs.entity;

/**
 * 描述:
 * userTag
 *
 * @Author HeFeng
 * @Create 2018-09-02 15:34
 */
public class UserTag {
    private  String oid;
    private  String user_oId;
    private  String tag_oId;
    private  Integer type;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getUser_oId() {
        return user_oId;
    }

    public void setUser_oId(String user_oId) {
        this.user_oId = user_oId;
    }

    public String getTag_oId() {
        return tag_oId;
    }

    public void setTag_oId(String tag_oId) {
        this.tag_oId = tag_oId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}