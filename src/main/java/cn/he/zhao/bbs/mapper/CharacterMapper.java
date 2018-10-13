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


import cn.he.zhao.bbs.entity.Character;
import org.apache.catalina.LifecycleState;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Character Mapper.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jun 8, 2016
 * @since 1.4.0
 */
public interface CharacterMapper {

    String add(Character character);

    @Select("select count(DISTINCT characterContent) from character")
    Integer countCharacter();

    List<Character> getAll();

    @Select("select count(*) from character WHERE characterUserId = #{characterUserId}")
    Integer countByCharacterUserId(final String characterUserId);


    @Select("select count(*) from character WHERE characterContent = #{characterContent}")
    Integer countByCharacterContent(final String characterContent);

    @Select("select count(*) from character WHERE characterUserId = #{characterUserId} AND characterContent = #{characterContent}")
    Integer countByCharacterUserIdANDCharacterContent(final String characterUserId,final String characterContent);

}
