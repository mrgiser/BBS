package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.entityUtil.PermissionUtil;
import cn.he.zhao.bbs.entityUtil.RoleUtil;
import cn.he.zhao.bbs.entityUtil.UserExtUtil;
import cn.he.zhao.bbs.entityUtil.my.CollectionUtils;
import cn.he.zhao.bbs.entityUtil.my.Keys;
import cn.he.zhao.bbs.entityUtil.my.Pagination;
import cn.he.zhao.bbs.entityUtil.my.User;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.entity.*;

import cn.he.zhao.bbs.service.interf.LangPropsService;
import cn.he.zhao.bbs.spring.Paginator;
import cn.he.zhao.bbs.spring.Strings;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * RoleUtil query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.6.0.0, Jun 23, 2018
 * @since 1.8.0
 */
@Service
public class RoleQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleQueryService.class);

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
     * PermissionUtil Mapper.
     */
    @Autowired
    private PermissionMapper permissionMapper;

    /**
     * User Mapper.
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * Language service.
     */
    @Autowired
    private LangPropsService langPropsService;

    /**
     * Count the specified role's uses.
     *
     * @param roleId the specified role id
     * @return use count, returns integer max value if fails
     */
    public int countUser(final String roleId) {
        try {
//            final Query userCountQuery = new Query().setFilter(new PropertyFilter(User.USER_ROLE, FilterOperator.EQUAL, roleId));

            return (int) userMapper.countByRoleId(roleId);
        } catch (final Exception e) {
            LOGGER.error( "Count role [id=" + roleId + "] uses failed", e);

            return Integer.MAX_VALUE;
        }
    }

    /**
     * Checks whether the specified user has the specified requisite permissions.
     *
     * @param userId               the specified user id
     * @param requisitePermissions the specified requisite permissions
     * @return @code true} if the role has the specified requisite permissions, returns @code false} otherwise
     */
    public boolean userHasPermissions(final String userId, final Set<String> requisitePermissions) {
        try {
            final UserExt user = userMapper.get(userId);
            final String roleId = user.getUserRole();
            final Set<String> permissions = getPermissions(roleId);

            return PermissionUtil.hasPermission(requisitePermissions, permissions);
        } catch (final Exception e) {
            LOGGER.error( "Checks user [" + userId + "] has permission failed", e);

            return false;
        }
    }

    /**
     * Checks whether the specified role has the specified requisite permissions.
     *
     * @param roleId               the specified role id
     * @param requisitePermissions the specified requisite permissions
     * @return @code true} if the role has the specified requisite permissions, returns @code false} otherwise
     */
    public boolean hasPermissions(final String roleId, final Set<String> requisitePermissions) {
        final Set<String> permissions = getPermissions(roleId);

        return PermissionUtil.hasPermission(requisitePermissions, permissions);
    }

    /**
     * Gets an role specified by the given role id.
     *
     * @param roleId the given role id
     * @return an role, returns {@code null} if not found
     */
    public Role getRole(final String roleId) {
        if (UserExtUtil.DEFAULT_CMTER_ROLE.equals(roleId)) { // virtual role
            final Role ret = new Role();

            ret.setRoleName(langPropsService.get(UserExtUtil.DEFAULT_CMTER_ROLE + "NameLabel"));
            ret.setRoleDescription(langPropsService.get(UserExtUtil.DEFAULT_CMTER_ROLE + "DescLabel"));

            return ret;
        }

        try {
            final Role ret = roleMapper.get(roleId);

            if (!Strings.isNumeric(roleId)) {
                ret.setRoleName(langPropsService.get(roleId + "NameLabel"));
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets role failed", e);

            return null;
        }
    }

    /**
     * Gets all permissions and marks grant of a user specified by the given user id.
     *
     * @param userId the given user id
     * @return a map of permissions&lt;permissionId, permission&gt;, returns an empty map if not found
     */
    public Map<String, Permission> getUserPermissionsGrantMap(final String userId) {
        final List<Permission> permissions = getUserPermissionsGrant(userId);
        if (permissions.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, Permission> ret = new HashMap<>();
        for (final Permission permission : permissions) {
            ret.put(permission.getOid(), permission);
        }

        return ret;
    }

    /**
     * Gets all permissions and marks grant of a user specified by the given user id.
     *
     * @param userId the given user id
     * @return a list of permissions, returns an empty list if not found
     */
    public List<Permission> getUserPermissionsGrant(final String userId) {
        try {
            final UserExt user = userMapper.get(userId);
            if (null == user) {
                return getPermissionsGrant(RoleUtil.ROLE_ID_C_VISITOR);
            }

            final String roleId = user.getUserRole();

            return getPermissionsGrant(roleId);
        } catch (final Exception e) {
            LOGGER.error( "Gets user permissions grant failed", e);

            return getPermissionsGrant(RoleUtil.ROLE_ID_C_VISITOR);
        }
    }

    /**
     * Gets grant permissions of a user specified by the given user id.
     *
     * @param userId the given user id
     * @return a list of permissions, returns an empty set if not found
     */
    public Set<String> getUserPermissions(final String userId) {
        try {
            final UserExt user = userMapper.get(userId);
            if (null == user) {
                return Collections.emptySet();
            }

            final String roleId = user.getUserRole();

            return getPermissions(roleId);
        } catch (final Exception e) {
            LOGGER.error( "Gets grant permissions of user [id=" + userId + "] failed", e);

            return Collections.emptySet();
        }
    }

    /**
     * Gets all permissions and marks grant of an role specified by the given role id.
     *
     * @param roleId the given role id
     * @return a map of permissions&lt;permissionId, permission&gt;, returns an empty map if not found
     */
    public Map<String, Permission> getPermissionsGrantMap(final String roleId) {
        final List<Permission> permissions = getPermissionsGrant(roleId);
        if (permissions.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, Permission> ret = new HashMap<>();
        for (final Permission permission : permissions) {
            ret.put(permission.getOid(), permission);
        }

        return ret;
    }

    /**
     * Gets all permissions and marks grant of an role specified by the given role id.
     *
     * @param roleId the given role id
     * @return a list of permissions, returns an empty list if not found
     */
    public List<Permission> getPermissionsGrant(final String roleId) {
        final List<Permission> ret = new ArrayList<>();

        try {
//            final List<JSONObject> permissions = CollectionUtils.jsonArrayToList(
//                    permissionMapper.getAll().optJSONArray(Keys.RESULTS));
            final List<Permission> permissions = permissionMapper.getAll();
            final List<RolePermission> rolePermissions = rolePermissionMapper.getByRoleId(roleId);

            for (final Permission permission : permissions) {
                final String permissionId = permission.getOid();
                permission.setPermissionGrant( false);
                ret.add(permission);

                for (final RolePermission rolePermission : rolePermissions) {
                    final String grantPermissionId = rolePermission.getPermissionId();

                    if (permissionId.equals(grantPermissionId)) {
                        permission.setPermissionGrant( true);

                        break;
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.error( "Gets permissions grant of role [id=" + roleId + "] failed", e);
        }

        return ret;
    }

    /**
     * Gets permissions of an role specified by the given role id.
     *
     * @param roleId the given role id
     * @return a list of permissions, returns an empty list if not found
     */
    public Set<String> getPermissions(final String roleId) {
        final Set<String> ret = new HashSet<>();

        try {
            final List<RolePermission> rolePermissions = rolePermissionMapper.getByRoleId(roleId);
            for (final RolePermission rolePermission : rolePermissions) {
                final String permissionId = rolePermission.getPermissionId();

                ret.add(permissionId);
            }

            return ret;
        } catch (final Exception e) {
            LOGGER.error( "Gets permissions of role [id=" + roleId + "] failed", e);

            return Collections.emptySet();
        }
    }

    /**
     * Gets roles by the specified request json object.
     *
     * @param currentPage the specified current page number
     * @param pageSize    the specified page size
     * @param windowSize  the specified window size
     * @return for example, <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "roles": [{
     *         "oId": "",
     *         "roleName": "",
     *         "roleDescription": "",
     *         "roleUserCount": int,
     *         "permissions": [
     *             {
     *                 "oId": "adUpdateADSide",
     *                 "permissionCategory": int
     *             }, ....
     *         ]
     *     }, ....]
     * }
     * </pre>
     * @throws Exception service exception
//     * @see Pagination
     */
    public JSONObject getRoles(final int currentPage, final int pageSize, final int windowSize)
            throws Exception {
        final JSONObject ret = new JSONObject();

//        final Query query = new Query().setCurrentPageNum(currentPage).setPageSize(pageSize).
//                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        PageHelper.startPage(currentPage,pageSize,"OId DESC");

        PageInfo<Role> result = null;

        try {
            result = new PageInfo<Role> (roleMapper.getAll());
        } catch (final Exception e) {
            LOGGER.error( "Gets roles failed", e);

            throw new Exception(e);
        }

        final int pageCount = result.getPages();
//        final int pageCount = result.getPages(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPage, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final List<Role> roles = result.getList();
//         CollectionUtils.<JSONObject>jsonArrayToList(data);

        try {
            for (final Role role : roles) {
                final List<Permission> permissions = new ArrayList<>();
                role.setPermissions((Object) permissions);

                final String roleId = role.getOid();
                final List<RolePermission> rolePermissions = rolePermissionMapper.getByRoleId(roleId);
                for (final RolePermission rolePermission : rolePermissions) {
                    final String permissionId = rolePermission.getPermissionId();
                    final Permission permission = permissionMapper.get(permissionId);

                    permissions.add(permission);
                }

//                final Query userCountQuery = new Query().
//                        setFilter(new PropertyFilter(User.USER_ROLE, FilterOperator.EQUAL, roleId));
                final int count = (int) userMapper.countByRoleId(roleId);
                role.setRoleUserCount( count);

                // fill description
                if (Strings.isNumeric(roleId)) {
                    continue;
                }

                String roleName = role.getRoleName();
                try {
                    roleName = langPropsService.get(roleId + "NameLabel");
                } catch (final Exception e) {
                    // ignored
                }

                String roleDesc = role.getRoleDescription();
                try {
                    roleDesc = langPropsService.get(roleId + "DescLabel");
                } catch (final Exception e) {
                    // ignored
                }

                role.setRoleName(roleName);
                role.setRoleDescription( roleDesc);
            }
        } catch (final Exception e) {
            LOGGER.error( "Gets role permissions failed", e);

            throw new Exception(e);
        }

        Collections.sort(roles, (o1, o2) -> ((List) o2.getPermissions()).size()
                - ((List) o1.getPermissions()).size());

        ret.put(RoleUtil.ROLES, (Object) roles);

        return ret;
    }
}
