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

import cn.he.zhao.bbs.entity.Comment;
import org.apache.ibatis.annotations.Select;
import org.json.JSONObject;

import java.util.List;

/**
 * Comment Mapper.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.1.0, May 7, 2018
 * @since 0.2.0
 */
public interface CommentMapper {

    String add(final Comment comment);

    void removeComment(final String commentId)  ;

    void remove(final String id)  ;

    Comment get(final String id) ;

    void update(final String id, final Comment comment) ;


    void removeCommentByArticleId(final String commentOnArticleId);

    @Select("select count(*) from comment where articleId = #{articleId} AND OId < #{commentId}")
    Long countByArticleIdAndLessCommentId(final String articleId, final String commentId);

    @Select("select count(*) from comment where articleId = #{articleId} AND OId > #{commentId}")
    Long countByArticleIdAndGREACommentId(final String articleId, final String commentId);

    List<Comment> getByCommentAuthorId(final String commentAuthorId);

    List<Comment> getByCommentOnArticleId(final String commentOnArticleId);

    //Gets the offered (accepted) comment of an article
    @Select("select * from comment WHERE commentOnArticleId = #{articleId} AND commentQnAOffered = 1 AND commentStatus = 0 ORDER BY commentScore DESC")
    List<Comment> getAcceptedCommentsForArticle(final String articleId);
}
