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

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This class defines all link entity relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.0.5, Jun 28, 2017
 * @since 1.6.0
 */
public class LinkUtil {

    // Type constants
    /**
     * Link type - forge.
     */
    public static final int LINK_TYPE_C_FORGE = 0;

    // Address constants
    /**
     * Link blacklist.
     */
    public static final Set<String> LINK_ADDR_C_BLACKLIST = new HashSet<>(Arrays.asList(
            "hacpai"));

    /**
     * Private constructor.
     */
    private LinkUtil() {
    }

    /**
     * Checks whether the specified link address in blacklist.
     *
     * @param linkAddr the specified link address
     * @return {@code true} if it in blacklist, otherwise returns {@code false}
     */
    public static final boolean inAddrBlacklist(final String linkAddr) {
        for (final String site : LINK_ADDR_C_BLACKLIST) {
            if (StringUtils.containsIgnoreCase(linkAddr, site)) {
                return true;
            }
        }

        return false;
    }
}
