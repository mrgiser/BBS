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
import org.apache.ibatis.annotations.Select;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public interface TagMapper {

    String add(final Tag tag) ;

    void remove(final String id) ;

    void update(final String id, final Tag article) ;

    Tag get(final String oId) ;
    List<Tag> getALL() ;

    List<Tag> getByTagTitle(final String title);

    String getURIByTitle(final String title) ;

    Tag getByURI(final String tagURI) ;

    Tag getByTitle(final String tagTitle) ;

    List<Tag> getMostUsedTags(final int num) ;

    List<Tag> getByArticleId(final String articleId) ;

    @Select("select * from tag where tagStatus != 0")
    List<Tag> getInvalidTags();

    @Select("select * from tag ORDER BY tagReferenceCount DESC")
    List<Tag> getAllDESCByTagReferenceCount();

    @Select("select * from tag ORDER BY tagReferenceCount ASC")
    List<Tag> getAllASCByTagReferenceCount();
}
