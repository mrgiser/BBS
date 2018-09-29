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


import cn.he.zhao.bbs.entity.Revision;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RevisionMapper {

    void remove(final String revisionDataId, int revisionDataType);

    String add(Revision revision);

    @Select("select * from revision where commentId = #{commentId} AND revisionDataType = #{type} ORDER BY oId ASC")
    List<Revision> getByRevisionDataIdAndRevisionDataType(final String commentId, final int type);

    @Select("select coutn(*) from revision where commentId = #{commentId} AND revisionDataType = #{type}")
    Integer countByRevisionDataIdAndRevisionDataType(final String commentId, final int type);

}
