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

import cn.he.zhao.bbs.entity.Tag;
import cn.he.zhao.bbs.entity.TagArticle;
import org.apache.ibatis.annotations.Select;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public interface TagArticleMapper {

     void removeByArticleId(final String articleId) ;

     List<TagArticle> getByArticleId(final String articleId) ;

     List<TagArticle> getByTagId(final String tagId, final int currentPageNum, final int pageSize);

    List<TagArticle> getByTagId(final String tagId);

    String add(TagArticle tagArticleRelation);

    void remove(String relationId);

    @Select("<script>"
            + "SELECT * FROM tagarticle WHERE tag_oId in "
            + "<foreach item='item' index='index' collection='tagIds' open='(' separator=',' close=')'>"
            + "#{item}"
            + "</foreach>"
            + "</script>")
    List<TagArticle> getByTagIds(final List<String> tagIds);

    void update(String oid, TagArticle tagArticleRel);
}
