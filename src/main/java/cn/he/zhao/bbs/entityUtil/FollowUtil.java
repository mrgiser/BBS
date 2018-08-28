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
 * This class defines all follow entity relevant keys.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Jun 3, 2015
 * @since 0.2.5
 */
public class FollowUtil {

    // Following type constants
    /**
     * Following type - user.
     */
    public static final int FOLLOWING_TYPE_C_USER = 0;

    /**
     * Following type - tag.
     */
    public static final int FOLLOWING_TYPE_C_TAG = 1;

    /**
     * Following type - article collect.
     */
    public static final int FOLLOWING_TYPE_C_ARTICLE = 2;

    /**
     * Following type - article watch.
     */
    public static final int FOLLOWING_TYPE_C_ARTICLE_WATCH = 3;

    /**
     * Private constructor.
     */
    private FollowUtil() {
    }
}