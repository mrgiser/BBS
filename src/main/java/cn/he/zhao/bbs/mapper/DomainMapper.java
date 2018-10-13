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

import cn.he.zhao.bbs.entity.Domain;
import org.apache.ibatis.annotations.Select;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Domain Mapper.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 13, 2013
 * @since 1.4.0
 */

public interface DomainMapper {

    @Select("select * from domain ORDER BY domainSort ASC, domainTagCnt DESC, oId DESC")
    List<Domain> getAllByOrder();

    Domain getByOId(String oid);

    @Select("select * from domain WHERE domainNav = #{domainNav} ORDER BY domainSort ASC, domainTagCnt DESC, oId DESC")
    List<Domain> getByDomainNav(final int domainNav);

    @Select("select * from domain WHERE domainTitle = #{domainTitle} ORDER BY domainSort ASC, domainTagCnt DESC, oId DESC")
    List<Domain> getByDomainTitle(final String domainTitle);

    Domain getByTitle(final String domainTitle) ;

    Domain getByDomainURI(final String domainURI) ;
}
