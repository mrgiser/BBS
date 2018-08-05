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
package cn.he.zhao.bbs.util;

import org.json.JSONObject;

/**
 * Result utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 2.1.0.1, Jul 16, 2015
 * @since 0.2.0
 */
public final class Results {
    private static final String STATUS_CODE = "sc";

    /**
     * Constructs a default (false) result.
     *
     * @return a false result,      <pre>
     * {
     *     "sc": false
     * }
     * </pre>
     */
    public static JSONObject falseResult() {
        return new JSONObject().put(STATUS_CODE, false);
    }

    /**
     * Constructs a default (true) result.
     *
     * @return a true result,      <pre>
     * {
     *     "sc": true
     * }
     * </pre>
     */
    public static JSONObject trueResult() {
        return new JSONObject().put(STATUS_CODE, true);
    }

    /**
     * Private constructor.
     */
    private Results() {
    }
}
