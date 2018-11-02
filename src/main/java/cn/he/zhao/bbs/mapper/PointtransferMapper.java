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

import cn.he.zhao.bbs.entity.Pointtransfer;
import org.apache.ibatis.annotations.Select;
import org.json.JSONObject;

import java.util.List;

public interface PointtransferMapper {

    Integer getActivityEatingSnakeAvg(final String userId) ;

    @Select("select * from pointtransfer WHERE (fromId = #{userId} OR toId = #{userId}) AND  type = #{type}")
    List<Pointtransfer> getByUserIdAndType(final String userId, final int type);

    @Select("select * from pointtransfer WHERE fromId = #{userId} OR toId = #{userId} ")
    List<Pointtransfer> getByUserId(final String userId);

    String add(Pointtransfer pointtransfer);

    @Select("select * from pointtransfer WHERE toId = #{userId} AND (type = #{type OR type = #{type2}}) ")
    Integer getInvitedUserCount(final String userId,final int type, final int type2);

}
