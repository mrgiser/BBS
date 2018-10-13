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

import cn.he.zhao.bbs.entity.Breezemoon;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Breezemoon Mapper.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, May 21, 2018
 * @since 2.8.0
 */
public interface BreezemoonMapper {

    void remove(String id);

    void update(String id, Breezemoon old);

    Breezemoon get(String oId);

    List<Breezemoon> getAll();

    @Select("<script>"
            + "SELECT * FROM breezemoon WHERE breezemoonStatus = #{breezemoonStatus} AND  breezemoonAuthorId IN "
            + "<foreach item='item' index='index' collection='followingUserIds' open='(' separator=',' close=')'>"
            + "#{item}"
            + "</foreach>"
            + "ORDER BY oId DESC"
            + "</script>")
    List<Breezemoon> getFollowingUserBreezemoon(final int breezemoonStatus, List<String> followingUserIds);

    void add(Breezemoon bm);

    @Select("select * from breezemoon WHERE breezemoonStatus=#{breezemoonStatus} AND breezemoonAuthorId = #{authorId}")
    List<Breezemoon> getByUseridAndStatus(final int breezemoonStatus, final String authorId);

    @Select("select * from breezemoon WHERE breezemoonStatus=#{breezemoonStatus} AND breezemoonAuthorId != #{authorId}")
    List<Breezemoon> getByExcludeUseridAndStatus(final int breezemoonStatus, final String authorId);
}
