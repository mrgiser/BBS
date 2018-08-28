package cn.he.zhao.bbs.entity;

public class Role {

    private String oid;

    /**
     * Key of role name.
     */
    private String roleName;

    /**
     * Key of role description.
     */
    private String roleDescription;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }
}
