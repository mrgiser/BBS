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

import cn.he.zhao.bbs.entity.Notification;
import cn.he.zhao.bbs.entity.UserExt;
import org.apache.ibatis.annotations.Select;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;


public interface NotificationMapper {

    Notification getByOId(final String oId);

    List<Notification> getByUserIdHasReadDataType(final String userId,final boolean read, final int type);

    List<Notification> getByUserIdHasReadDataId(final String userId,final boolean read, final List<String> commentIds);

    List<Notification> getByUserIdHasRead(final String userId,final boolean read);

    void removeByDataId(final String dataId) ;

    void add(Notification notification);

    void update(final String oId, Notification notification);

    Integer remove(final String oId);

    Integer removeByUserIdAndDataType(final String userId, final int type);

    List<Notification> getByUserIdAndDataType(final String userId, final int type);

    Integer countByReadAndUserId(final String userId, final Boolean hasRead);

    Integer countByReadAndUserIdAndDataType(final String userId, final Boolean hasRead, final int dataType);

    Integer countByReadAndUserIdAndDataTypes(final String userId, final Boolean hasRead, int dataType1,int dataType2,int dataType3);


    List<Notification> getByReadAndUserIdAndDataTypes(final String userId, int dataType1,int dataType2,int dataType3);

    @Select("<script>"
            + "SELECT count(*) FROM notification WHERE userId = #{userId} AND hasRead = #{hasRead}" +
            "AND dataType in "
            + "<foreach item='item' index='index' collection='types' open='(' separator=',' close=')'>"
            + "#{item}"
            + "</foreach>"
            + "</script>")
    Integer countUnReadAndUserIdAndDataTypes(final String userId, final Boolean hasRead, List<Integer> types);

    @Select("<script>"
            + "SELECT * FROM notification WHERE userId = #{userId} " +
            "AND dataType in "
            + "<foreach item='item' index='index' collection='types' open='(' separator=',' close=')'>"
            + "#{item}"
            + "</foreach>"
            + "</script>")
    List<Notification> getByUserIdAndDataTypes(final String userId, List<Integer> types);
}
