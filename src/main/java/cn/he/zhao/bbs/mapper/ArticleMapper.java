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

import org.json.JSONObject;

import java.util.List;

/**
 * Article Mapper.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.1.5, Apr 6, 2018
 * @since 0.2.0
 */
public interface ArticleMapper {

//    @Autowired
//    ArticleCache articleCache;

    void remove(final String id);

    JSONObject get(final String id);


    void update(final String id, final JSONObject article);


    List<JSONObject> getRandomly(final int fetchSize);

    JSONObject getByTitle(final String articleTitle);
}
