/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cn.he.zhao.bbs.entityUtil;

/**
 * This class defines all role model relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.2, Dec 14, 2016
 * @since 1.8.0
 */
public final class RoleUtil {

    /**
     * RoleUtil.
     */
    public static final String ROLE = "role";

    /**
     * Roles.
     */
    public static final String ROLES = "roles";

    /**
     * Key of role name.
     */
    public static final String ROLE_NAME = "roleName";

    /**
     * Key of role description.
     */
    public static final String ROLE_DESCRIPTION = "roleDescription";

    /**
     * Key of role id.
     */
    public static final String ROLE_ID = "roleId";

    //// Transient ////
    /**
     * Key of user count.
     */
    public static final String ROLE_T_USER_COUNT = "roleUserCount";

    // RoleUtil name constants
    /**
     * RoleUtil name - default.
     */
    public static final String ROLE_ID_C_DEFAULT = "defaultRole";

    /**
     * RoleUtil name - admin.
     */
    public static final String ROLE_ID_C_ADMIN = "adminRole";

    /**
     * RoleUtil name - leader.
     */
    public static final String ROLE_ID_C_LEADER = "leaderRole";

    /**
     * RoleUtil name - regular.
     */
    public static final String ROLE_ID_C_REGULAR = "regularRole";

    /**
     * RoleUtil name - member.
     */
    public static final String ROLE_ID_C_MEMBER = "memberRole";

    /**
     * RoleUtil name - visitor.
     */
    public static final String ROLE_ID_C_VISITOR = "visitorRole";

    /**
     * Private constructor.
     */
    private RoleUtil() {
    }
}
