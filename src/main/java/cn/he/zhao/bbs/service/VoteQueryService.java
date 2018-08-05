
package cn.he.zhao.bbs.service;

import cn.he.zhao.bbs.model.my.*;
import cn.he.zhao.bbs.util.Symphonys;
import cn.he.zhao.bbs.mapper.*;
import cn.he.zhao.bbs.model.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Vote query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.1, Oct 17, 2016
 * @since 1.3.0
 */
@Service
public class VoteQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(VoteQueryService.class);

    /**
     * Vote Mapper.
     */
    @Autowired
    private VoteMapper voteMapper;

    /**
     * Article Mapper.
     */
    @Autowired
    private ArticleMapper articleMapper;

    /**
     * Comment Mapper.
     */
    @Autowired
    private CommentMapper commentMapper;

    /**
     * Determines whether the specified user dose vote the specified entity.
     *
     * @param userId the specified user id
     * @param dataId the specified entity id
     * @return voted type, returns {@code -1} if has not voted yet
     */
    public int isVoted(final String userId, final String dataId) {
        try {
            final List<Filter> filters = new ArrayList<>();
            filters.add(new PropertyFilter(Vote.USER_ID, FilterOperator.EQUAL, userId));
            filters.add(new PropertyFilter(Vote.DATA_ID, FilterOperator.EQUAL, dataId));

            final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

            final JSONObject result = voteMapper.get(query);
            final JSONArray array = result.optJSONArray(Keys.RESULTS);

            if (0 == array.length()) {
                return -1;
            }

            final JSONObject vote = array.optJSONObject(0);

            return vote.optInt(Vote.TYPE);
        } catch (final MapperException e) {
            LOGGER.error( e.getMessage());

            return -1;
        }
    }

    /**
     * Determines whether the specified data dose belong to the specified user.
     *
     * @param userId the specified user id
     * @param dataId the specified data id
     * @param dataType the specified data type
     * @return {@code true} if it belongs to the user, otherwise returns {@code false}
     */
    public boolean isOwn(final String userId, final String dataId, final int dataType) {
        try {
            if (Vote.DATA_TYPE_C_ARTICLE == dataType) {
                final JSONObject article = articleMapper.get(dataId);
                if (null == article) {
                    LOGGER.error( "Not found article [id={0}]", dataId);

                    return false;
                }

                return article.optString(Article.ARTICLE_AUTHOR_ID).equals(userId);
            } else if (Vote.DATA_TYPE_C_COMMENT == dataType) {
                final JSONObject comment = commentMapper.get(dataId);
                if (null == comment) {
                    LOGGER.error( "Not found comment [id={0}]", dataId);

                    return false;
                }

                return comment.optString(Comment.COMMENT_AUTHOR_ID).equals(userId);
            }

            return false;
        } catch (final MapperException e) {
            LOGGER.error( e.getMessage());

            return false;
        }
    }
}
