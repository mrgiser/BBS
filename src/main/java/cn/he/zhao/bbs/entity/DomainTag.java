package cn.he.zhao.bbs.entity;

/**
 * 描述:
 * userTag
 *
 * @Author HeFeng
 * @Create 2018-09-02 15:34
 */
public class DomainTag {
    private  String oid;
    private  String domain_oId;
    private  String tag_oId;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getDomain_oId() {
        return domain_oId;
    }

    public void setDomain_oId(String domain_oId) {
        this.domain_oId = domain_oId;
    }

    public String getTag_oId() {
        return tag_oId;
    }

    public void setTag_oId(String tag_oId) {
        this.tag_oId = tag_oId;
    }
}