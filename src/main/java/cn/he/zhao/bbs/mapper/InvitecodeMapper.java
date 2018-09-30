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


import cn.he.zhao.bbs.entity.Invitecode;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface InvitecodeMapper {

    String add(final Invitecode invitecode);

    Integer update(final String OId, final Invitecode invitecode);

    List<Invitecode> getByUserId(final String userId);

    List<Invitecode> getByCore(final String core);

    List<Invitecode> getByGeneratorIdAndStatus(final String generatorId, final int status);

    Invitecode getByOId(final  String oId);

    List<Invitecode> getAll();

    Integer remove(final String oId);

    @Select("select * from invitecode WHERE status = #{status} AND generatorId != #{generatorId}  AND oId <= expired")
    List<Invitecode> getExpiredInvitecodes(final int status, final String generatorId, final String expired);

}
