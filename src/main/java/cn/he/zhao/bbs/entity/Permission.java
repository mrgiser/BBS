package cn.he.zhao.bbs.entity;

public class Permission {

    private String oid;

    /**
     * Key of permission category.
     */
    private Integer permissionCategory;

    /**
     * Key of permission id.
     */
    private String permissionId;

    private transient boolean permissionGrant;

    private transient String permissionLabel;

    public String getPermissionLabel() {
        return permissionLabel;
    }

    public void setPermissionLabel(String permissionLabel) {
        this.permissionLabel = permissionLabel;
    }

    public boolean isPermissionGrant() {
        return permissionGrant;
    }

    public void setPermissionGrant(boolean permissionGrant) {
        this.permissionGrant = permissionGrant;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Integer getPermissionCategory() {
        return permissionCategory;
    }

    public void setPermissionCategory(Integer permissionCategory) {
        this.permissionCategory = permissionCategory;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }
}
