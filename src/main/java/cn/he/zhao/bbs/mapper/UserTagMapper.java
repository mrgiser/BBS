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

import cn.he.zhao.bbs.entity.UserTag;
import org.apache.ibatis.annotations.Select;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public interface UserTagMapper {

    void removeByUserIdAndTagId(final String userId, final String tagId, final int type) ;

    JSONObject getByUserId(final String userId, final int currentPageNum, final int pageSize) ;

    List<UserTag> getByTagId(final String tagId, final int currentPageNum, final int pageSize) ;

    String add(UserTag userTagRelation);

    @Select("select * from user_tag where user_oid = #{tagId} AND (type = 0 OR type = 2) ORDER BY oId ASC")
    List<UserTag> getByTagIdAndCretorType(final String tagId);

    @Select("select * from user_tag where user_oid = #{tagId} AND type = 1 ORDER BY oId ASC")
    List<UserTag> getByTagIdAndArticleType(final String tagId);
}
