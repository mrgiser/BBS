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
package cn.he.zhao.bbs.mapper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public interface TagUserLinkMapper {

    public int countTagLink(final String tagId) ;

    public void updateTagLinkScore(final String tagId, final String linkId, final double score);

    public void removeByTagIdUserIdAndLinkId(final String tagId, final String userId, final String linkId);

    public void removeByLinkId(final String linkId) ;

    public List<String> getLinkIdsByTagId(final String tagId, final int fetchSize) ;

    public List<String> getTagIdsByLinkId(final String linkId, final int fetchSize) ;

    public List<String> getByTagIdAndUserId(final String tagId, final String userId, final int fetchSize);
}
