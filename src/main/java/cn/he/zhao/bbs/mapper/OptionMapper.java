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

import cn.he.zhao.bbs.entity.Option;
import org.apache.ibatis.annotations.Select;
import org.json.JSONObject;

import java.util.List;

public interface OptionMapper  {


    void remove(final String id) ;

    Option get(final String id) ;

    void update(final Option option) ;
    void update(final String oId, final Option option) ;

    String add(Option cityArticleCntOption);

    @Select("select * from option WHERE optionCategory = #{category}")
    List<Option> getByOptionCategory(final String category);

    @Select("select count(*) from option WHERE optionCategory = #{category} AND optionValue = #{value}")
    Long getByOptionCategoryAndValue(final String category,final String value);
}
