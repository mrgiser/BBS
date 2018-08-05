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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * JSON utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Apr 18, 2016
 * @since 1.4.0
 */
public final class JSONs {

    /**
     * Private constructor.
     */
    private JSONs() {
    }

    /**
     * Clones a JSON object from the specified source object.
     *
     * @param src the specified source object
     * @return cloned object
     */
    public static JSONObject clone(final JSONObject src) {
        return new JSONObject(src, jsonArrayToArray(src.names()));
    }

    public static String[] jsonArrayToArray(JSONArray jsonArray) {
        if (null == jsonArray) {
            return (String[])(new Object[0]);
        } else {
            int newLength = jsonArray.length();
            Object[] original = new Object[newLength];

            for(int i = 0; i < newLength; ++i) {
                original[i] = jsonArray.opt(i);
            }

            return Arrays.copyOf(original, newLength, String[].class);
        }
    }
}
