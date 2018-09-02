package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

import java.util.Set;

/**
 * RoleUtil management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Jun 23, 2018
 * @since 1.8.0
 */
@Service
public class RoleMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleMgmtService.class);

    /**
     * RoleUtil Mapper.
     */
    @Autowired
    private RoleMapper roleMapper;

    /**
     * RoleUtil-PermissionUtil Mapper.
     */
    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    /**
     * User Mapper.
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * Removes the specified role.
     *
     * @param roleId the specified role id
     */
    @Transactional
    public void removeRole(final String roleId) {
        try {
            final Query userCountQuery = new Query().setFilter(new PropertyFilter(User.USER_ROLE, FilterOperator.EQUAL, roleId));
            final int count = (int) userMapper.count(userCountQuery);
            if (0 < count) {
                return;
            }

            rolePermissionMapper.removeByRoleId(roleId);
            roleMapper.remove(roleId);
        } catch (final Exception e) {
            LOGGER.error( "Removes a role [id=" + roleId + "] failed", e);
        }
    }

    /**
     * Adds the specified role.
     *
     * @param role the specified role
     */
    @Transactional
    public void addRole(final JSONObject role) {
        try {
            final String roleName = role.optString(Role.ROLE_NAME);

            final Query query = new Query().
                    setFilter(new PropertyFilter(Role.ROLE_NAME, FilterOperator.EQUAL, roleName));
            if (roleMapper.count(query) > 0) {
                return;
            }

            roleMapper.add(role);
        } catch (final MapperException e) {
            LOGGER.error( "Adds role failed", e);
        }
    }

    /**
     * Updates role permissions.
     *
     * @param roleId the specified role id
     */
    @Transactional
    public void updateRolePermissions(final String roleId, final Set<String> permissionIds) {
        try {
            rolePermissionMapper.removeByRoleId(roleId);

            for (final String permissionId : permissionIds) {
                final JSONObject rel = new JSONObject();
                rel.put(Role.ROLE_ID, roleId);
                rel.put(Permission.PERMISSION_ID, permissionId);

                rolePermissionMapper.add(rel);
            }
        } catch (final MapperException e) {
            LOGGER.error( "Updates role permissions failed", e);
        }
    }
}
