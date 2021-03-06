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
import cn.he.zhao.bbs.entity.DomainTag;
import org.apache.ibatis.annotations.Select;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


public interface DomainTagMapper {

    String add(DomainTag domainTag);

    Integer remove(final String oId);

    List<DomainTag> getByDomainId(final String domainId) ;

    JSONObject getByDomainId(final String domainId, final int currentPageNum, final int pageSize) ;


    void removeByDomainId(final String domainId);


    List<Domain> getByTagOId(final String tagId, final int currentPageNum, final int pageSize);

    List<DomainTag> getByTagOId(final String tagId);

    @Select("select * from domain_tag WHERE domain_oId = #{domainId} AND tag_oId = #{tagId}")
    List<DomainTag> getByDomain_oIdAndTag_oId(final String domainId, final String tagId);

    @Select("select count(*) from domain_tag WHERE domain_oId = #{domainId} AND tag_oId = #{tagId}")
    Integer countByDomain_oIdAndTag_oId(final String domainId, final String tagId);
}
